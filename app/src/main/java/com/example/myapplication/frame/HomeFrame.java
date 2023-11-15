package com.example.myapplication.frame;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.State;
import com.example.myapplication.ble.BluetoothHandler;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ConnectionState;
import com.welie.blessed.WriteType;


public class HomeFrame extends Fragment {

    private FrameLayout  mainLayoutView;
    private ImageView imageView; // image for button off/on
    private Button btnToggleLamp;
    private Button buttonWifi;
    private Button buttonLight;

    private final BroadcastReceiver lampStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.LAMP_STATE_UPDATE_ACTION)) {
                String lampState = intent.getStringExtra(BluetoothHandler.EXTRA_LAMP_STATE);
                toggleVisual(lampState);
            }
        }
    };

    private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION)) {
                toggleVisual("OFF");
            }
        }
    };

    private void setUpLamp() {

        String address = BluetoothHandler.address;
        if (address == null) {
            Toast.makeText(getContext(), "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothPeripheral peripheral = getPeripheral(address);
        if (peripheral != null && peripheral.getState() == ConnectionState.CONNECTED) {
            BluetoothGattCharacteristic lampStateCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            if (lampStateCharacteristic != null) {
                peripheral.readCharacteristic(lampStateCharacteristic);
                String currentState = new String(lampStateCharacteristic.getValue());
                Log.d("VALUE", currentState);
                toggleVisual(currentState);
            }
        } else {
            Toast.makeText(getContext(), "Bluetooth device is not connected", Toast.LENGTH_SHORT).show();
        }

    }


    public void toggleVisual(String value) {
        if (value.equals("OFF")){
            imageView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_off);
        } else if (value.equals("ON")){
            imageView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_on);
            State.isLampOn = true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);

        imageView = view.findViewById(R.id.image_turn);
        mainLayoutView = view.findViewById(R.id.mainLayout1);
        btnToggleLamp = view.findViewById(R.id.Button_on);
        buttonWifi = view.findViewById(R.id.button_wifi);

        setUpLamp();

        btnToggleLamp.setOnClickListener(v -> {
            toggleLampState();
        });

        buttonLight = view.findViewById(R.id.button_light);
        buttonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showFromHomeToLightFragment();
                }
            }
        });

        buttonWifi = view.findViewById(R.id.button_wifi);
        buttonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showFromHomeToWifiFragment();
                }
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION);
        IntentFilter filter1 = new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION);
        getActivity().registerReceiver(lampStateReceiver, filter);
        getActivity().registerReceiver(disconnectReceiver, filter1);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(lampStateReceiver);
        getActivity().unregisterReceiver(disconnectReceiver);
    }

    private void toggleLampState() {
        String address = BluetoothHandler.address;
        if (address == null) {
            Toast.makeText(getContext(), "No Bluetooth device connected", Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothPeripheral peripheral = getPeripheral(address);
        if (peripheral != null && peripheral.getState() == ConnectionState.CONNECTED) {
            BluetoothGattCharacteristic lampStateCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            if (lampStateCharacteristic != null) {
//                peripheral.readCharacteristic(lampStateCharacteristic);
                String currentState = new String(lampStateCharacteristic.getValue());
                Log.d("VALUE", currentState);
                // Toggle the lamp state
                String newState = "OFF".equals(currentState) ? "ON" : "OFF";
                byte[] newValue = newState.getBytes();
                peripheral.writeCharacteristic(lampStateCharacteristic, newValue, WriteType.WITH_RESPONSE);
                toggleVisual(newState);
            }
        } else {
            toggleVisual("OFF");
            Toast.makeText(getContext(), "Bluetooth device is not connected", Toast.LENGTH_SHORT).show();
        }
    }


    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(getContext()).central;
        return central.getPeripheral(peripheralAddress);
    }

////    @SuppressLint("UnspecifiedRegisterReceiverFlag")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
////        registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0 ;
//        } else {
//        }
//
//
//
//    }



}