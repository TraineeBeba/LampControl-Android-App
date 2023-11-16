package com.example.myapplication.frame;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_BRIGHTNESS_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import androidx.fragment.app.Fragment;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;


import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.BluetoothHandler;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ConnectionState;
import com.welie.blessed.WriteType;

import android.graphics.drawable.GradientDrawable;

public class LightFrame extends Fragment {
    private Button buttonHome;
    private Button buttonWifi;

    private Button button_mode1;
    private Button button_mode2;
    private Button button_mode3;
    private Button button_add_color;
    private Button button_ActiveColor1_1;


    private GradientDrawable drawable;
    private ImageView imageViewMode;



    private int maxOld = 9; // count step for bringe
    private int positionBar;
    SeekBar seekBar;
    TextView textView;

    private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION)) {
                String brightnessStr = intent.getStringExtra(BluetoothHandler.EXTRA_BRIGHTNESS);
                Log.d("AAAA", brightnessStr);

                int brightness = Integer.valueOf(brightnessStr);

                // Define the ranges
                int minBrightness = 25;
                int maxBrightness = 250;
                int minSeekBar = 0;
                int maxSeekBar = 9;

                // Calculate the scaling factor
                double scaleFactor = (double) (maxSeekBar - minSeekBar) / (maxBrightness - minBrightness);

                // Apply the scaling factor to get the corresponding SeekBar position
                int seekBarPosition = (int)((brightness - minBrightness) * scaleFactor);

                seekBar.setProgress(seekBarPosition);
            }
        }
    };


    public int scaleValue(double inputValue) {
        int minOld = 0;
        int minNew = 10;
        int maxNew = 100;
        int scaledValue = (int) (minNew + ((inputValue - minOld) / (maxOld - minOld)) * (maxNew - minNew));
        return scaledValue;
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

        groupActiveLayout1 = view.findViewById(R.id.groupActiveColors1);
        groupActiveLayout2 = view.findViewById(R.id.groupActiveColors2);
        groupActiveLayout3 = view.findViewById(R.id.groupActiveColors3);





        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Цей метод викликається кожного разу, коли змінюється поточне положення ковзанця
                int valueBrightnessText = scaleValue(progress);
                textView.setText(valueBrightnessText + " %");
                State.valueBrightnessText = valueBrightnessText;
                State.seekBarposition = seekBar.getProgress(); // position seekBar
                int value = (progress + 1) * 25;
                Log.d("BRIGHTNESS" , String.valueOf(value));
                setBrightness(value);
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



        IntentFilter filter1 = new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION);
        getActivity().registerReceiver(brightnessReceiver, filter1);


        button_add_color = view.findViewById(R.id.addColorBtn);
        button_add_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        button_ActiveColor1_1 = view.findViewById(R.id.activeColorBtnMode1_1);
        button_ActiveColor1_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    private void setBrightness(int value) {
        String address = BluetoothHandler.address;
        if (address == null) {
            Toast.makeText(getContext(), "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothPeripheral peripheral = getPeripheral(address);
        if (peripheral != null && peripheral.getState() == ConnectionState.CONNECTED) {
            BluetoothGattCharacteristic lampBrightnessCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID);
            if (lampBrightnessCharacteristic != null) {
//                peripheral.readCharacteristic(lampStateCharacteristic);
//                String currentState = new String(lampBrightnessCharacteristic.getValue());
//                Log.d("VALUE", currentState);
//                // Toggle the lamp state
//                String newState = "OFF".equals(currentState) ? "ON" : "OFF";
                byte[] brightnessValue = new byte[]{(byte) value};

                peripheral.writeCharacteristic(lampBrightnessCharacteristic, brightnessValue, WriteType.WITH_RESPONSE);
            }
        } else {
            Toast.makeText(getContext(), "Bluetooth device is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(getContext()).central;
        return central.getPeripheral(peripheralAddress);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(brightnessReceiver);
    }

}
