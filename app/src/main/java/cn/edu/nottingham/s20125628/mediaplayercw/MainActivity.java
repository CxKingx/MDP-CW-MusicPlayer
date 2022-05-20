package cn.edu.nottingham.s20125628.mediaplayercw;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created By Kevin Ferdinand 20125628 for MDP CW1 2022
 * Each references for the relevant function is above the functions or at the beginning of the file
 * and a compiled version in README
 * Tested in VM : Pixel 2 API 29 Android ver 10
 * Tested in Physical Machine : Samsung A52S with API 31 Android ver 12
 * E:\Apps\MediaPlayerCW\app\build\intermediates\apk\debug
 */
//https://stackoverflow.com/questions/13568798/list-all-music-in-mediastore-with-the-paths for MediaStore
//https://developer.android.com/reference/android/content/ContentResolver
public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener {
    public static final int BackgroundChangeRequest = 1;
    private static final String TAG = "MainActivity";
    Intent serviceIntent;
    String ColorChange;
    RelativeLayout main_layout;
    RecyclerView recyclerView;
    TextView noSongText,SongTitle,SongCurrentDuration,SongMaxDuration;
    ArrayList<SongData> songsList = new ArrayList<>();
    SongData CurrentSong;

    Context ServiceContext;
    // To continously update between Service and RunOnThread
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG,"Hi from Receive Update");
            SongCurrentDuration.setText(intent.getStringExtra("SongProgress"));
            SongMaxDuration.setText(intent.getStringExtra("SongDuration"));
        }
    };

    //https://stackoverflow.com/questions/62202471/how-to-get-a-permission-request-in-new-activityresult-api-1-3-0-alpha05
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                  // Reload Main Activity
                    Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                    finish();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.e(TAG, "onActivityResult: PERMISSION DENIED");

                }
            });


    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                                int resultCode = activityResult.getResultCode();
                                Intent data = activityResult.getData();
                                if (resultCode == RESULT_OK) {
                                    ColorChange = data.getStringExtra("colorchange");
                                    main_layout.setBackgroundColor(Color.parseColor(ColorChange));

                                }
                                else{
                                    Toast.makeText(MainActivity.this, "Operation canceled", Toast.LENGTH_LONG).show();
                                }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"Hi from Oncreate");
        setContentView(R.layout.activity_main);
        ColorChange="#87CEFA"; //Sky Blue
        main_layout = (RelativeLayout) findViewById(R.id.layout);
        SongTitle = findViewById(R.id.songTitle);
        recyclerView = findViewById(R.id.recycler_view);
        noSongText = findViewById(R.id.no_songs_text);
        SongTitle.setText("Now Playing : .....");
        SongCurrentDuration = findViewById(R.id.currentDuration);
        SongMaxDuration=findViewById(R.id.CurrentSongMaxDuration);

        if(!checkPermission()){
            requestPermission();
            return;
        }

        LoadMusicFiles();

        // if no songs at all
        if(songsList.size()==0){
            noSongText.setVisibility(View.VISIBLE);
        }else{ // Show all songs
            //recyclerview Pass the SongList to the adaptor class so it will create the recycler view
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            MusicListAdapter SonglistAdapter = new MusicListAdapter(songsList,getApplicationContext(),this);
            recyclerView.setAdapter(SonglistAdapter);
        }

        //https://stackoverflow.com/questions/14814714/update-textview-every-second
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    if (serviceIntent!=null){
                        serviceIntent.setAction("UPDATE");
                        startService(serviceIntent);
                    }

                new Handler().postDelayed(this,1000);
            }
        });

    }// OnCreate End

    // Takes a Raw Duration in miliseconds and turn into minutes and seconds
    public String MiliSecondTimeFormat(String SongDuration){
        String Result;
        long DurationRaw=Long.parseLong(SongDuration);
        long minutes = (DurationRaw / 1000) / 60;
        long seconds = (DurationRaw / 1000) % 60;

        if (seconds<10){
            Result= minutes+":0"+seconds;
        }
        else{
            Result = minutes+":"+seconds;
        }
        return Result;
    }

    //https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati
    //https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application/
    public boolean checkPermission(){
        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))==PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    public void LoadMusicFiles(){
        // To simplify passing the data into the Songlist class
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION
        };
        // Search for all mp3 files in the whole phone
        String selection = MediaStore.Audio.Media.IS_MUSIC +" != 0";
        // query where media is stored
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // This will read the music files, by inserting projection we can easily identify the info that we want
        Cursor cursor = getContentResolver().query(uri,projection,selection,null,null);
        // see all song files and add them
        while(cursor.moveToNext()){
            // Audio Model is Path Title Duration
            SongData songData = new SongData(cursor.getString(0),cursor.getString(1),cursor.getString(2));
            //Recheck if song exists
            if(new File(songData.getPath()).exists()){
                songsList.add(songData);
            } //Else do nothing so it will not crash
        }
    }
    // Launch Activity to change background colour
    public void StartBackgroundChangeActivity(View view) {
        System.out.printf("Launch Background CHange");
        Intent intent = new Intent(MainActivity.this, ChangeBackground.class);
        intent.putExtra("CurBGColor",ColorChange);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
        activityResultLauncher.launch(intent);
    }

    //Request to access External Storage
    void requestPermission(){
        // If permission denied, ask for permission #Change
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this,"Read Permission is required to load songs,please enable them",Toast.LENGTH_SHORT).show();
            //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }else//Ask permission
            //ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    //https://stackoverflow.com/questions/28296708/get-clicked-item-and-its-position-in-recyclerview
    // When a song is clicked, pass the position to main activity so we can play it
    @Override
    public void recyclerViewListClicked(View v, int position) {
        Log.d(TAG,"Click on Song postioin "+ position);
        CurrentSong = songsList.get(position);
        SongTitle.setText(new StringBuilder().append("Now Playing: ").append(CurrentSong.getTitle()));

        SongMaxDuration.setText(new StringBuilder().append(MiliSecondTimeFormat(String.valueOf(CurrentSong.getDuration()))).append("").toString());
        SongCurrentDuration.setText("0:00");

        Log.d(TAG,"Preparing Servcew");
        if (serviceIntent!=null){
            stopService(serviceIntent);
        }
        // start at 0
        ServiceContext = getApplicationContext();
        serviceIntent = new Intent(this, SongService.class);
        serviceIntent.putExtra("CurrentSongObject",CurrentSong);
        serviceIntent.setAction("LOAD");
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
        Log.d(TAG,"Starting Servcew");
        ServiceContext.startService(serviceIntent);
        //startService(serviceIntent);

    }

    public void PausePlaySong(View view) {
        serviceIntent.setAction("PLAYPAUSE");
        startService(serviceIntent);
        serviceIntent.setAction("UPDATE");
        startService(serviceIntent);
    }

    public void StopSong(View view) {
        serviceIntent.setAction("STOP");
        startService(serviceIntent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(recyclerView!=null){
            recyclerView.setAdapter(new MusicListAdapter(songsList,getApplicationContext(),this));
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    // So main activity doesnt creat a new one if we just press back
    //https://stackoverflow.com/questions/10795669/after-pressing-a-back-button-activities-doesnt-resumes-to-last-activity
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.edu.nottingham.s20125628.mediaplayercw");
        registerReceiver(broadcastReceiver, intentFilter);
    }


}