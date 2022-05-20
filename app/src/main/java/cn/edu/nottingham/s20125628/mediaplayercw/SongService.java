package cn.edu.nottingham.s20125628.mediaplayercw;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.jetbrains.annotations.Nullable;
//https://www.geeksforgeeks.org/services-in-android-with-example/
//https://stackoverflow.com/questions/46838443/play-music-with-background-service
//https://coderanch.com/t/603994/Passing-variable-Activity-running-Service
public class SongService extends Service implements MediaPlayer.OnPreparedListener {
    SongData CurrentSong;
    MP3Player mp3Player;
    NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent){return null;}

    //Context.startService will call onCreate and onStartCommand, while startservice  will only call onStartCommand
    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "Service Successfully Created", Toast.LENGTH_LONG).show();
        Log.d("1","Created Service");

        //Create the notification
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Song Notification","Song Notification", NotificationManager.IMPORTANCE_LOW);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        Log.d("1","Start Command");
        String songState;
        CurrentSong = (SongData) intent.getSerializableExtra("CurrentSongObject");

        SongActions(intent);
        StartNotification();

        return START_NOT_STICKY;
    }
    // Prepare Notification that will show the Current Song Playing and its swipeable and clickable to return to main activity
    public void StartNotification(){
        Intent notificationIntent = new Intent(this, MainActivity. class );
        notificationIntent.addCategory(Intent. CATEGORY_LAUNCHER ) ;
        notificationIntent.setAction(Intent. ACTION_MAIN ) ;
        notificationIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent. FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1,
                notificationIntent,PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"Song Notification");
        builder.setContentTitle("Now Playing");
        builder.setContentText(new StringBuilder().append(CurrentSong.getTitle()));
        builder.setSmallIcon(R.drawable.music_icon);
        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);
        //builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0));

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());
    }
    // https://www.tutorialspoint.com/how-to-update-ui-from-intent-service-in-android
    // https://stackoverflow.com/questions/23968240/updating-activity-textview-from-service-using-service-binding
    // Get Action to pause,resume.stop and load songs, and also update the timer in run on ui thread
    public void SongActions(Intent intent){
        Log.d("TAG","Song Update");
        if (intent.getAction().equals("PLAYPAUSE")) {
            if(mp3Player.getState()== MP3Player.MP3PlayerState.PAUSED){
                mp3Player.play();
            }
            else{
                mp3Player.pause();
            }
        }
        else if (intent.getAction().equals("STOP")){
            mp3Player.stop();
        }
        else if (intent.getAction().equals("LOAD")) {
            mp3Player = new MP3Player();
            mp3Player.load(CurrentSong.getPath());
            mp3Player.play();
        }
        else if (intent.getAction().equals("UPDATE")){
            Log.d("1","Sending Update");
            Intent CurSongDataIntent = new Intent();
            CurSongDataIntent.setAction("cn.edu.nottingham.s20125628.mediaplayercw");

            CurSongDataIntent.putExtra("SongProgress", MiliSecondTimeFormat2(String.valueOf(mp3Player.getProgress())));
            CurSongDataIntent.putExtra("SongDuration", MiliSecondTimeFormat2(String.valueOf(mp3Player.getDuration())));

            sendBroadcast(CurSongDataIntent);
        }
    }
    // Long int format to proper viewable time
    public String MiliSecondTimeFormat2(String SongDuration){
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

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }
    // Stop the song when app is closed
    @Override
    public void onDestroy() {
        //Toast.makeText(this, "Service Stopped and Music Stopped", Toast.LENGTH_LONG).show();
        if (mp3Player!=null){
            mp3Player.stop();
        }
        manager.cancelAll();
        //manager.deleteNotificationChannel( "Song Notification" );

    }

    // https://stackoverflow.com/questions/19568315/how-to-handle-running-service-when-app-is-killed-by-swiping-in-android
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        //stop service
        stopSelf();
    }
}
