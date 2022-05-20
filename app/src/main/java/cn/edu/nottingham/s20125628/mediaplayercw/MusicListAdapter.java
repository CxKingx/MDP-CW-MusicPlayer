package cn.edu.nottingham.s20125628.mediaplayercw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
//https://www.youtube.com/watch?v=18VcnYN5_LM&ab_channel=Stevdza-San Recycler View Reference
//https://medium.com/geekculture/everything-you-should-know-to-create-a-recyclerview-3defdb660a2f
// Adapter for Recycler View
public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder>{
    ArrayList<SongData> songsList;
    Context context;

    private static final String TAG = "MyActivity";
    private static RecyclerViewClickListener itemListener;


    public MusicListAdapter(ArrayList<SongData> songsList, Context context, RecyclerViewClickListener itemListener) {
        this.songsList = songsList;
        this.context = context;
        MusicListAdapter.itemListener = itemListener;
    }

    //Prepare all the rows for Song list
    //This gets called num 1
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        //assign  recycler item to viewholder
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item,parent,false);

        return new MusicListAdapter.ViewHolder(view);
    }

    // Set the text of all the holders
    //This gets called num 3
    @Override
    public void onBindViewHolder( MusicListAdapter.ViewHolder holder, int position) {
        SongData songData = songsList.get(position);

        holder.titleTextView.setText(songData.getTitle());
        // Pass songData.getDuration() because it is in miliseconds
        holder.DurationTextView.setText(DurationFormatFix(songData.getDuration()));
        holder.songPosition.setText(String.valueOf(holder.getAdapterPosition()+1));

    }

    //To change from milisecond to minutes and second to display in song list
    public String DurationFormatFix(String SongDuration){
        String Result;
        long DurationLong=Long.parseLong(SongDuration);
        long minutes = (DurationLong / 1000) / 60;
        long seconds = (DurationLong / 1000) % 60;

        if (seconds<10){
            Result= minutes+":0"+seconds;
        }
        else{
            Result = minutes+":"+seconds;
        }

        return Result;
    }
    @Override
    public int getItemCount() {
        return songsList.size();
    }

    //This gets called num 2
    // The holder for Recycler view
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleTextView;
        TextView DurationTextView;
        TextView songPosition;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            DurationTextView= itemView.findViewById(R.id.music_duration_text);
            songPosition =itemView.findViewById(R.id.listofSong);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Pass Previous Song position
            itemListener.recyclerViewListClicked(view, this.getAdapterPosition());
        }
    }

}
