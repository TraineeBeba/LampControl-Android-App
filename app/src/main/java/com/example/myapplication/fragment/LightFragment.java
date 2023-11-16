package com.example.myapplication.fragment;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_BRIGHTNESS_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import androidx.fragment.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampViewState;
import com.example.myapplication.ble.BluetoothHandler;
import com.welie.blessed.WriteType;

public class LightFragment extends Fragment {
    private Button btnNavHome;
    private Button btnNavWifi;

    private Button btnMode1;
    private Button btnMode2;
    private Button btnMode3;

    private boolean isSeekBarDisabled = false; // Add this flag
    private ImageView imageViewMode;

    private int maxOld = 9;
    SeekBar seekBar;
    TextView textView;

    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;


    //TODO on device disconnected

    private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION)) {
                String brightnessStr = intent.getStringExtra(BluetoothHandler.EXTRA_BRIGHTNESS);
                int brightness = Integer.valueOf(brightnessStr);
                updateVisual(brightness);
            }
        }
    };

    private void updateVisual(int brightness) {
        Log.d("Brightness", String.valueOf(brightness));

        int currentPosition = seekBar.getProgress();
        int calculatedPosition = getSeekBarPosition(brightness);


        if(calculatedPosition != currentPosition){
            LampViewState.setSeekBarPos(calculatedPosition);
            Log.d("UPDAAAATE", "UPDAAAATE");
            seekBar.setProgress(calculatedPosition);
        }

        int brightnessPercentageText = calculatePercentage(calculatedPosition);
        LampViewState.setBrightnessPercentageText(brightnessPercentageText);
        textView.setText(brightnessPercentageText + " %");
    }

    private static int getSeekBarPosition(int brightness) {
        // Define the ranges
        int minBrightness = 25;
        int maxBrightness = 250;
        int minSeekBar = 0;
        int maxSeekBar = 9;

        // Calculate the scaling factor
        double scaleFactor = (double) (maxSeekBar - minSeekBar) / (maxBrightness - minBrightness);

        // Apply the scaling factor to get the corresponding SeekBar position
        return (int)((brightness - minBrightness) * scaleFactor);
    }


    public int calculatePercentage(double inputValue) {
        int minOld = 0;
        int minNew = 10;
        int maxNew = 100;
        return (int) (minNew + ((inputValue - minOld) / (maxOld - minOld)) * (maxNew - minNew));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.light, container, false);

        initView(view);
        initBtnListeners();
        saveMode(LampViewState.getNumberMode());
        setUpBrightness();

        registerReceivers();

        return view;
    }

    private void setUpBrightness() {
        try {
            readLampState();
            if(LampViewState.getIsLampOn() == Lamp.ON){
                readBrightness();
            }
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setBrightness(int value) {
        try {
            readLampState();
            if(LampViewState.getIsLampOn() == Lamp.ON){
                byte[] brightnessValue = new byte[]{(byte) value};
                writeBrightness(brightnessValue);
            }
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void readLampState() throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
        bluetoothHandler.readCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
    }
    private void writeBrightness(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
    }

    private void readBrightness() throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
        bluetoothHandler.readCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID);
    }

    private void initView(View view) {
        seekBar = view.findViewById(R.id.seekBar);
        textView = view.findViewById(R.id.textviewbar);
        imageViewMode = view.findViewById(R.id.modeImage);
        btnNavHome = view.findViewById(R.id.button_home);
        btnNavWifi = view.findViewById(R.id.button_wifi);
        btnMode1 = view.findViewById(R.id.button_one_color);
        btnMode2 = view.findViewById(R.id.button_rainbow);
        btnMode3 = view.findViewById(R.id.button_data_night);

        //        textView.setText(LampViewState.getBrightnessPercentageText());
    }

    private void initBtnListeners() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                debounceHandler.removeCallbacks(debounceRunnable);

                if (isSeekBarDisabled){
                    seekBar.setProgress(LampViewState.getPrevSeekBarPos());
                } else {
                    debounceRunnable = () -> {
                        int brightnessValue = (progress + 1) * 25;
                        setBrightness(brightnessValue);
                    };
                    textView.setText(calculatePercentage(progress) + " %");
                }

                debounceHandler.postDelayed(debounceRunnable, 200);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                try {
                    readLampState();
                    if(LampViewState.getIsLampOn() == Lamp.OFF){
                        isSeekBarDisabled = true;
                    }
                } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
                    isSeekBarDisabled = true;
//                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarDisabled = false;
            }
        });

        btnNavHome.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.lightLayout, FragmentType.HOME);
            }
        });

        btnNavWifi.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.lightLayout, FragmentType.WIFI);
            }
        });

        btnMode1.setOnClickListener(v -> {
            imageViewMode.setImageResource(R.drawable.mode_one_on);
            LampViewState.setNumberMode(0);
        });

        btnMode2.setOnClickListener(v -> {
            imageViewMode.setImageResource(R.drawable.mode_two_on);
            LampViewState.setNumberMode(1);
        });

        btnMode3.setOnClickListener(v -> {
            imageViewMode.setImageResource(R.drawable.mode_three_on);
            LampViewState.setNumberMode(2);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        debounceHandler.removeCallbacks(debounceRunnable);
        unRegisterReceivers();
    }

    private void registerReceivers() {
        IntentFilter filter1 = new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION);
        getActivity().registerReceiver(brightnessReceiver, filter1);
    }

    private void unRegisterReceivers() {
        getActivity().unregisterReceiver(brightnessReceiver);
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

}
