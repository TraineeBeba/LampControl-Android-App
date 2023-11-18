package com.example.myapplication.fragment;

import android.os.Bundle;
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
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.util.BLECommunicationUtil;
import com.example.myapplication.util.BroadcastReceiverUtil;

public class HomeFragment extends Fragment {
    private BLECommunicationUtil bluetoothComm;
    private FrameLayout homeLayout;
    private ImageView toggleView;
    private Button btnNavWifi, btnNavLight, btnToggleLamp;
    private BroadcastReceiverUtil receiverUtil;
    Lamp currentState = Lamp.OFF;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);
        bluetoothComm = new BLECommunicationUtil(getContext());

        initView(view);
        initBtnListeners();
        initBroadcastReceiver();
        loadState();

        return view;
    }

    private void loadState() {
        updateVisual(LampCache.isOn());
        try {
            bluetoothComm.readLampState();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLampState() {
        currentState = Lamp.getToggle(LampCache.isOn()); // Expected state after toggle
        updateVisual(currentState); // Update UI immediately

        try {
            bluetoothComm.readLampState();
            Lamp toggleState = Lamp.getToggle(LampCache.isOn());
            bluetoothComm.writeLampState(toggleState.name().getBytes());
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateVisual(Lamp lampState) {
        if (lampState == currentState) {
            return;
        }

        switch (lampState) {
            case OFF:
                toggleView.setImageResource(R.drawable.turn_off_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_off);
                currentState = Lamp.OFF;
                break;
            case ON:
                toggleView.setImageResource(R.drawable.turn_on_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_on);
                currentState = Lamp.ON;
                break;
        }
        LampCache.setIsOn(lampState);
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

    private void initBroadcastReceiver() {
        receiverUtil = new BroadcastReceiverUtil(getContext(), new BroadcastReceiverUtil.Callback() {
            @Override
            public void onLampStateUpdate(Lamp lampState) {
                updateVisual(lampState);
            }
            @Override
            public void onDisconnect() {
                updateVisual(Lamp.OFF);
            }
        });

        receiverUtil.registerReceivers();
    }

    @Override
    public void onDestroyView() {
//        Log.d("HOME", "DESTROY");
        super.onDestroyView();
        receiverUtil.unregisterReceivers();
    }
}