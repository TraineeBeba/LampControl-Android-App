package com.example.myapplication.frame;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.State;

public class LightFrame extends Fragment {
    private Button buttonHome;
    private Button buttonWifi;

    private Button button_mode1;
    private Button button_mode2;
    private Button button_mode3;

    private ImageView imageViewMode;

    private int countState = 7;
    private int positionBar;
    SeekBar seekBar;
    TextView textView;

    private int transform(int count_step, int step_now){
        return (int) (step_now*100) / (count_step-1);

    }

    private void saveMode(int numbermode){
        switch (numbermode){
            case 0:
                imageViewMode.setImageResource(R.drawable.mode_one_on);
                break;
            case 1:
                imageViewMode.setImageResource(R.drawable.mode_two_on);
                break;
            case 2:
                imageViewMode.setImageResource(R.drawable.mode_three_on);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modes_screen, container, false);


        Log.d("PISUN", "onCreate");
        seekBar = view.findViewById(R.id.seekBar);

        textView = view.findViewById(R.id.textviewbar);
        imageViewMode = view.findViewById(R.id.modeImage);
        saveMode(State.numberMode);

        textView.setText(String.valueOf(State.valueBrightnessText + " %"));
        seekBar.setProgress(State.seekBarposition);



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Цей метод викликається кожного разу, коли змінюється поточне положення ковзанця
                textView.setText(String.valueOf(transform(countState, progress) + " %"));
                State.valueBrightnessText = transform(countState, progress);
                State.seekBarposition = seekBar.getProgress(); // position seekBar
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Цей метод викликається, коли користувач починає торкатися ковзанця
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Цей метод викликається, коли користувач закінчує торкатися ковзанця

            }
        });

        buttonHome = view.findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showFromLightToHomeFragment();
                }
            }
        });

        buttonWifi = view.findViewById(R.id.button_wifi);
        buttonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showFromLightToWifiFragment();
                }
            }
        });

        button_mode1 = view.findViewById(R.id.button_one_color);
        button_mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewMode.setImageResource(R.drawable.mode_one_on);
                State.numberMode = 0;
            }
        });

        button_mode2 = view.findViewById(R.id.button_rainbow);
        button_mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewMode.setImageResource(R.drawable.mode_two_on);
                State.numberMode = 1;
            }
        });

        button_mode3 = view.findViewById(R.id.button_data_night);
        button_mode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewMode.setImageResource(R.drawable.mode_three_on);
                State.numberMode = 2;
            }
        });


        return view;
    }

}
