package com.example.myapplication.frame;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.State;


public class HomeFrame extends Fragment {

    private FrameLayout  mainLayoutView;
    private ImageView imageView; // image for button off/on
    private Button buttonToggleLamp;
    private Button buttonWifi;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);

        imageView = view.findViewById(R.id.image_turn);
        mainLayoutView = view.findViewById(R.id.mainLayout1);

        setUpLamp();

//        toggleLamp();
        Log.d("HUI", "ONCREATE");
        // Ініціалізація кнопки для вмикання/вимикання лампи
        buttonToggleLamp = view.findViewById(R.id.Button_on);
        buttonToggleLamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLamp();
            }
        });

        // Ініціалізація іншої кнопки
        buttonWifi = view.findViewById(R.id.button_wifi);
        buttonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showWifiFragment();
                }
            }
        });

        return view;
}

    private void setUpLamp() {
        if (!State.isLampOn){
            Log.d("AAAA ", "On");
            imageView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_off);

        } else{
            Log.d("AAAA", "Off");
            imageView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_on);

        }
    }


    public void toggleLamp() {
        // lamp tracking off/on

        if (State.isLampOn){
            Log.d("AAAA ", "On");
            imageView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_off);
            State.isLampOn = false;
        }
        else{
            Log.d("AAAA", "Off");
            imageView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_on);
            State.isLampOn = true;
        }
    }

}