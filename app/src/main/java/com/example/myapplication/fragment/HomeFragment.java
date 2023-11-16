package com.example.myapplication.fragment;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import android.annotation.SuppressLint;
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
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampViewState;
import com.example.myapplication.ble.BluetoothHandler;
import com.welie.blessed.WriteType;


public class HomeFragment extends Fragment {
    private FrameLayout homeLayout;
    private ImageView toggleView;
    private Button btnToggleLamp;
    private Button btnNavWifi;
    private Button btnNavLight;

    private final BroadcastReceiver lampStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.LAMP_STATE_UPDATE_ACTION)) {
                String lampState = intent.getStringExtra(BluetoothHandler.EXTRA_LAMP_STATE);
                updateVisual(Lamp.valueOf(lampState));
            }
        }
    };


    private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION)) {
                updateVisual(Lamp.OFF);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home, container, false);

        initView(view);
        initBtnListeners();
        registerReceivers();
        setUpLamp();

        return view;
    }

    private void setUpLamp() {
        try {
            readLampState();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLampState() {
        try {
            readLampState();
            Lamp toggleState = Lamp.getToggle(LampViewState.getIsLampOn());
            writeLampState(toggleState.name().getBytes());
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateVisual(Lamp lampState) {
        if (lampState == Lamp.OFF){
            toggleView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            homeLayout.setBackgroundResource(R.drawable.homescreen__background_off);
        } else if (lampState == Lamp.ON){
            toggleView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            homeLayout.setBackgroundResource(R.drawable.homescreen__background_on);
        }

        LampViewState.setIsLampOn(lampState);
        Log.d("LAMP STATE", lampState.name());
    }

    private void writeLampState(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
    }

    private void readLampState() throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
        bluetoothHandler.readCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
    }


    private void initBtnListeners() {
        btnToggleLamp.setOnClickListener(v -> toggleLampState());

        btnNavLight.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.homeLayout, FragmentType.LIGHT);
            }
        });

        btnNavWifi.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.homeLayout, FragmentType.WIFI);
            }
        });
    }

    private void initView(View view) {
        toggleView = view.findViewById(R.id.image_turn);
        homeLayout = view.findViewById(R.id.homeLayout);
        btnToggleLamp = view.findViewById(R.id.Button_on);
        btnNavWifi = view.findViewById(R.id.button_wifi);
        btnNavLight = view.findViewById(R.id.button_light);
        btnNavWifi = view.findViewById(R.id.button_wifi);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unRegisterReceivers();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerReceivers() {
        IntentFilter filter = new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION);
        IntentFilter filter1 = new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION);
        getActivity().registerReceiver(lampStateReceiver, filter);
        getActivity().registerReceiver(disconnectReceiver, filter1);
    }

    private void unRegisterReceivers() {
        getActivity().unregisterReceiver(lampStateReceiver);
        getActivity().unregisterReceiver(disconnectReceiver);
    }

}