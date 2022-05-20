package cn.edu.nottingham.s20125628.mediaplayercw;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class ChangeBackground extends AppCompatActivity {
    public String ColorCode="#87CEFA";
    public ConstraintLayout backgroundchange;

    //Get Background Color from Main so it matches with main activity color
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_background);
        backgroundchange = (ConstraintLayout) findViewById(R.id.layout2);
        Bundle bundle = getIntent().getExtras();
        ColorCode = bundle.getString("CurBGColor");
        ChangeAppBackground();

    }

    // Send back the color code to main activity
    public void launchBackFirstActivity(View view) {
        System.out.printf("Launching BACK");
        // Return the New Color Code to main activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("colorchange",ColorCode);

        resultIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }

    // Functions to set Color to Background
    public void ChangeBackgroundOrange(View view){
        ColorCode = "#FFA500";
        ChangeAppBackground();
    }
    public void ChangeBackgroundRed(View view){
        ColorCode = "#FF0000";
        ChangeAppBackground();
    }
    public void ChangeBackgroundSkyBlue(View view){
        ColorCode = "#87ceeb";
        ChangeAppBackground();
    }
    public void ChangeBackgroundPurple(View view){
        ColorCode = "#c31cd9";
        ChangeAppBackground();
    }
    public void ChangeBackgroundPink(View view){
        ColorCode = "#ff00dd";
        ChangeAppBackground();

    }
    public void ChangeAppBackground( ){
        backgroundchange.setBackgroundColor(Color.parseColor(ColorCode));
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        backgroundchange.setBackgroundColor(Color.parseColor(ColorCode));
    }
}