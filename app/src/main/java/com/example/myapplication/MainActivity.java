package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainLayoutView;
    private Button button_home;
    private Button button_tighle;
    private Button button_wifi;

    private ImageView imageView; // image for button off/on
    private boolean isLampOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ImageButton imageButton = findViewById(R.layout.Button_on)
        imageView = findViewById(R.id.image_turn);
        mainLayoutView = findViewById(R.id.mainLayout);
        button_home = findViewById(R.id.button_home);
        button_tighle = findViewById(R.id.button_light);
        button_wifi = findViewById(R.id.button_wifi);



    }


    public void toggleLamp(View view) {
        // lamp tracking off/on

        if (isLampOn){
            Log.d("AAAA ", "On");
            imageView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_on);
            isLampOn = false;
        }
        else{
            Log.d("AAAA", "Off");
            imageView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_off);
            isLampOn = true;
        }

    }


    public void menu1(View view) {
        // lamp tracking off/on

        Log.d("AAAA", "Page is home");

    }

    public void menu2(View view) {
        // lamp tracking off/on
        Log.d("AAAA", "Page is light");


    }

    public void menu3(View view) {
        // lamp tracking off/on
        Log.d("AAAA", "Page is wifi");


    }

}