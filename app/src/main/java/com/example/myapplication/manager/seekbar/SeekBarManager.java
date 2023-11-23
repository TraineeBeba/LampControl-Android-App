package com.example.myapplication.manager.seekbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.manager.seekbar.util.BrightnessModeUtil;

public class SeekBarManager {
    private final SeekBar seekBar;
    private final TextView percentageText;
    private final Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;

    public SeekBarManager(View view) {
        this.seekBar = view.findViewById(com.example.myapplication.R.id.seekBar);
        this.percentageText = view.findViewById(R.id.textviewbar);
        setupSeekBar();
        setupListeners();
    }

    private void setupSeekBar() {
        seekBar.setProgress(LampCache.getSeekBarPos());
        percentageText.setText(LampCache.getBrightnessText() + " %");
        try {
            MainActivity.getBleCommunicationUtil().readLampState();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            seekBar.setEnabled(false);
        }
    }

    private void setBrightness(int value) {
        try {
            if(LampCache.isOn() == Lamp.ON){
                byte[] brightnessValue = new byte[]{(byte) value};
                MainActivity.getBleCommunicationUtil().writeBrightness(brightnessValue);
            }
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            updatePercentText(0);
        }
    }

    private void setupListeners() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                debounceHandler.removeCallbacks(debounceRunnable);

                debounceRunnable = () -> {
                    int brightnessValue = (progress + 1) * 25;
                    setBrightness(brightnessValue);
                };
                percentageText.setText(BrightnessModeUtil.calculatePercentage(progress) + " %");

                debounceHandler.postDelayed(debounceRunnable, 200);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                try {
                    MainActivity.getBleCommunicationUtil().readLampState();
                } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
                    seekBar.setEnabled(false);
                    Log.d("A", "A");
                }
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void updateVisualBar(int brightness) {
        int calculatedPosition = BrightnessModeUtil.getSeekBarPosition(brightness);

        if (calculatedPosition != seekBar.getProgress()) {
            seekBar.setProgress(calculatedPosition);
            updatePercentText(calculatedPosition);
        }
    }

    public void updatePercentText(int calculatedPosition) {
        int brightnessPercentageText = BrightnessModeUtil.calculatePercentage(calculatedPosition);
        percentageText.setText(brightnessPercentageText + " %");
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setPercentageText(String s) {
        percentageText.setText(s);
    }
}
