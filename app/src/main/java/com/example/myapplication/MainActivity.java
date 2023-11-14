package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.frame.HomeFrame;
import com.example.myapplication.frame.LightFrame;
import com.example.myapplication.frame.WifiFrame;


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


    public void showFromLightToHomeFragment() {
        Log.d("AAAA ", "showHome");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout2, new HomeFrame())
                .commit();
    }

    public void showFromWifiToHomeFragment() {
        Log.d("AAAA ", "showHome");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout3, new HomeFrame())
                .commit();
    }

    public void showFromHomeToLightFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout1, new LightFrame())
                .commit();
    }

    public void showFromWifiToLightFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout3, new LightFrame())
                .commit();
    }

    public void showFromHomeToWifiFragment() {
        Log.d("AAAA ", "showWifi");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout1, new WifiFrame())
                .commit();
    }

    public void showFromLightToWifiFragment() {
        Log.d("AAAA ", "showWifi");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainLayout2, new WifiFrame())
                .commit();
    }


}