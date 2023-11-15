package com.example.myapplication.fragment;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_BRIGHTNESS_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
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

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.LampViewState;
import com.example.myapplication.ble.BluetoothHandler;
import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ConnectionState;
import com.welie.blessed.WriteType;

public class LightFragment extends Fragment {
    private Button buttonHome;
    private Button buttonWifi;

    private Button button_mode1;
    private Button button_mode2;
    private Button button_mode3;

    private boolean isSeekBarDisabled = false; // Add this flag
    private ImageView imageViewMode;

    private int maxOld = 9;
    private int positionBar;
    SeekBar seekBar;
    TextView textView;

    //TODO on device disconnected

    private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION)) {
                String brightnessStr = intent.getStringExtra(BluetoothHandler.EXTRA_BRIGHTNESS);
                Log.d("AAAA", brightnessStr);

                int brightness = Integer.valueOf(brightnessStr);
                int seekBarPosition = getSeekBarPosition(brightness);

                seekBar.setProgress(seekBarPosition);
            }
        }
    };

    private static int getSeekBarPosition(int brightness) {
        // Define the ranges
        int minBrightness = 25;
        int maxBrightness = 250;
        int minSeekBar = 0;
        int maxSeekBar = 9;

        // Calculate the scaling factor
        double scaleFactor = (double) (maxSeekBar - minSeekBar) / (maxBrightness - minBrightness);

        // Apply the scaling factor to get the corresponding SeekBar position
        int seekBarPosition = (int)((brightness - minBrightness) * scaleFactor);
        return seekBarPosition;
    }


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

    private void setUpBrightness() {

        String address = BluetoothHandler.address;
        if (address == null) {
//            Toast.makeText(getContext(), "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothPeripheral peripheral = getPeripheral(address);
        if (peripheral != null && peripheral.getState() == ConnectionState.CONNECTED) {
            BluetoothGattCharacteristic brightnessCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID);
            BluetoothGattCharacteristic lampStateCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            if (lampStateCharacteristic != null) {
                byte[] value = lampStateCharacteristic.getValue();
                if(value != null){
                    String currentState = new String(value);

                    if(currentState.equals("ON")){
                        if (brightnessCharacteristic != null) {
                            peripheral.readCharacteristic(brightnessCharacteristic);
                            BluetoothBytesParser parser = new BluetoothBytesParser(brightnessCharacteristic.getValue());
                            Integer uInt8 = parser.getUInt8();
                            Log.d("VALUE", String.valueOf(uInt8));
                            int seekBarPosition = getSeekBarPosition(uInt8);
                            seekBar.setProgress(seekBarPosition);
                            int valueBrightnessText = scaleValue(seekBarPosition);
                            textView.setText(valueBrightnessText + " %");
                            LampViewState.setValueBrightnessText(valueBrightnessText);
                        }
                    } else {
                        seekBar.setProgress(LampViewState.getSeekBarPosition());
                    }
                } else {
                    seekBar.setProgress(LampViewState.getSeekBarPosition());
                }

            }

        } else {
//            Toast.makeText(getContext(), "Bluetooth device is not connected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.light, container, false);

        seekBar = view.findViewById(R.id.seekBar);

        textView = view.findViewById(R.id.textviewbar);
        imageViewMode = view.findViewById(R.id.modeImage);
        saveMode(LampViewState.getNumberMode());

        textView.setText(String.valueOf(LampViewState.getValueBrightnessText() + " %"));

        setUpBrightness();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isSeekBarDisabled){
                    Log.d("DISABLED", String.valueOf(LampViewState.getPreviousProgress()));
                    seekBar.setProgress(LampViewState.getPreviousProgress()); // Revert to the previous value
                    Toast.makeText(getContext(), "SeekBar is disabled while adjusting", Toast.LENGTH_SHORT).show();
                } else {// Цей метод викликається кожного разу, коли змінюється поточне положення ковзанця
//                    Log.d("ABLED", String.valueOf(progress));
                    LampViewState.setPreviousProgress(progress);
//                    Log.d("ABLED PREVIOUS", String.valueOf(State.previousProgress));
                    int valueBrightnessText = scaleValue(progress);
                    textView.setText(valueBrightnessText + " %");
                    LampViewState.setValueBrightnessText(valueBrightnessText);
                    LampViewState.setSeekBarPosition(seekBar.getProgress()); // position seekBar
                    int value = (progress + 1) * 25;
                    Log.d("BRIGHTNESS", String.valueOf(value));
                    setBrightness(value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                String address = BluetoothHandler.address;
                if (address == null) {
                    seekBar.setEnabled(false); // Disable the SeekBar
//                    Toast.makeText(getContext(), "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
                    return;
                }
                BluetoothPeripheral peripheral = getPeripheral(address);
                BluetoothGattCharacteristic lampStateCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
                if (lampStateCharacteristic != null) {
                    String currentState = new String(lampStateCharacteristic.getValue());

                    if(currentState.equals("OFF")){
                        isSeekBarDisabled = true;
//                        Toast.makeText(getContext(), "SeekBar is disabled while adjusting", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarDisabled = false; // Reset the flag

            }
        });

        buttonHome = view.findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
//                    .replace(, new HomeFrame())
                    ((MainActivity) getActivity()).navigateToFragment(R.id.lightLayout, FragmentType.HOME);
                }
            }
        });

        buttonWifi = view.findViewById(R.id.button_wifi);
        buttonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToFragment(R.id.lightLayout, FragmentType.WIFI);
                }
            }
        });

        button_mode1 = view.findViewById(R.id.button_one_color);
        button_mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewMode.setImageResource(R.drawable.mode_one_on);
                LampViewState.setNumberMode(0);
            }
        });

        button_mode2 = view.findViewById(R.id.button_rainbow);
        button_mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewMode.setImageResource(R.drawable.mode_two_on);
                LampViewState.setNumberMode(1);
            }
        });

        button_mode3 = view.findViewById(R.id.button_data_night);
        button_mode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewMode.setImageResource(R.drawable.mode_three_on);
                LampViewState.setNumberMode(2);
            }
        });

        IntentFilter filter1 = new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION);
        getActivity().registerReceiver(brightnessReceiver, filter1);

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
            BluetoothGattCharacteristic lampStateCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            if (lampStateCharacteristic != null) {
                String currentState = new String(lampStateCharacteristic.getValue());

                if(currentState.equals("ON")){
                    if (lampBrightnessCharacteristic != null) {
                        byte[] brightnessValue = new byte[]{(byte) value};

                        peripheral.writeCharacteristic(lampBrightnessCharacteristic, brightnessValue, WriteType.WITH_RESPONSE);
                    }
                }
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
