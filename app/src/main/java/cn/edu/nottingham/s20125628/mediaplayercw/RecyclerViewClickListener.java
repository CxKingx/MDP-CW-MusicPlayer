package cn.edu.nottingham.s20125628.mediaplayercw;

import android.view.View;
//https://stackoverflow.com/questions/35008860/how-to-pass-values-from-recycleadapter-to-mainactivity-or-other-activities
//https://stackoverflow.com/questions/28296708/get-clicked-item-and-its-position-in-recyclerview

//Helper to register function and send variables from recycler view to main activity
public interface RecyclerViewClickListener {
    public void recyclerViewListClicked(View v, int position);
}
