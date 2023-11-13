package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class ThirdActivity extends AppCompatActivity {

    private Button button_home;
    private Button button_tighle;
    private Button button_wifi;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectionscreen__one);

//        ImageButton imageButton = findViewById(R.layout.Button_on)


        button_home = findViewById(R.id.button_home);
        button_tighle = findViewById(R.id.button_light);
        button_wifi = findViewById(R.id.button_wifi);



    }



    public void menu1(View view) {
        // lamp tracking off/on

        Log.d("AAAA", "Page is home");
        Intent intent = new Intent(ThirdActivity.this, MainActivity.class);
        startActivity(intent);

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