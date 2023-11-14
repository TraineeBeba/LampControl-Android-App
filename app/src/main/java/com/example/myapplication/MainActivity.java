package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("AAAA ", "ShowStart");
        setContentView(R.layout.activity_main);

        // Початкове завантаження HomeFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout1, new HomeFrame())
                .commit();


    }


    public void showHomeFragment() {
        Log.d("AAAA ", "showHome");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout2, new HomeFrame())
                .commit();
    }

//    public void showLightFragment(View view) {
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, new LightFragment())
//                .commit();
//    }

    public void showWifiFragment() {
        Log.d("AAAA ", "showWifi");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout1, new WifiFrame())
                .commit();

    }


}