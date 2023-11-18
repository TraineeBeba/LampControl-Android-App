package com.example.myapplication.manager;

import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.util.BLECommunicationUtil;
import com.example.myapplication.util.BrightnessModeUtil;

public class SeekBarManager {
    private SeekBar seekBar;
    private TextView percentageText;
    private BLECommunicationUtil bluetoothComm;
    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;

    public SeekBarManager(SeekBar seekBar, TextView percentageText, BLECommunicationUtil bluetoothComm) {
        this.seekBar = seekBar;
        this.percentageText = percentageText;
        this.bluetoothComm = bluetoothComm;

        setupSeekBar();
    }

    public void setupSeekBar() {
        seekBar.setProgress(LampCache.getSeekBarPos());
        percentageText.setText(LampCache.getBrightnessText() + " %");
        try {
            bluetoothComm.readLampState();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            seekBar.setEnabled(false);
        }
    }

    private void setBrightness(int value) {
        try {
            if(LampCache.isOn() == Lamp.ON){
                byte[] brightnessValue = new byte[]{(byte) value};
                bluetoothComm.writeBrightness(brightnessValue);
            }
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            updatePercentText(0);
        }
    }

    public void setupListeners() {
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
                    bluetoothComm.readLampState();
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
}
