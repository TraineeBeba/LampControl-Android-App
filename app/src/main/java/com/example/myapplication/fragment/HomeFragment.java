package com.example.myapplication.fragment;

import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;

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
    private ImageView cloudImageLeftBlack;

    private ImageView Sunshine;
    private ImageView cloudImageLeftWhite;
    private ImageView cloudImageRightBlack;
    private ImageView cloudImageRightWhite;

    private float finalX_Left, finalY_Left;
    private float finalX_Right, finalY_Right;

    Animation fadeTransition;
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
//        if(Lamp.OFF == currentState){
//            updateVisual(Lamp.ON);
//        } else{
//            updateVisual(Lamp.OFF);
//        }
    }

    public void updateVisual(Lamp lampState) {
        if (lampState == currentState) {
            return;
        }

        switch (lampState) {
            case OFF:
                toggleView.setImageResource(R.drawable.turn_off_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_off);

                animationBtnOff();



                currentState = Lamp.OFF;
                break;
            case ON:

                toggleView.setImageResource(R.drawable.turn_on_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_on);


                animationBtnOn();




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

        Sunshine = view.findViewById(R.id.sunshine);
        cloudImageLeftBlack = view.findViewById(R.id.cloud_left_black);
        cloudImageLeftWhite = view.findViewById(R.id.cloud_left_white);
        cloudImageRightBlack = view.findViewById(R.id.cloud_right_black);
        cloudImageRightWhite = view.findViewById(R.id.cloud_right_white);
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

    public void animationBtnOn() {

        animateCloud(cloudImageLeftBlack, "translationX", finalX_Left, finalX_Left - 400, 1.0f, 0.0f); // Змініть значення за потребою
        animateCloud(cloudImageLeftWhite, "translationX", finalX_Left, finalX_Left - 400, 0.0f, 1.0f); // Змініть значення за потребою
        animateCloud(cloudImageRightBlack, "translationX", finalX_Left, finalX_Left + 300, 1.0f, 0.0f);
        animateCloud(cloudImageRightWhite, "translationX", finalX_Left, finalX_Left + 300, 0.0f, 1.0f);
        animateCloud(Sunshine, "translationX", finalX_Left, finalX_Left , 0.0f, 1.0f);


    }

    public void animationBtnOff() {

        animateCloud(cloudImageLeftBlack, "translationX", finalX_Left - 400, finalX_Left, 0.0f, 1.0f); // Повернення до первісного положення
        animateCloud(cloudImageLeftWhite, "translationX", finalX_Left - 400, finalX_Left, 1.0f, 0.0f); // Повернення до первісного положення
        animateCloud(cloudImageRightBlack, "translationX", finalX_Left + 300, finalX_Left,0.0f, 1.0f);
        animateCloud(cloudImageRightWhite, "translationX", finalX_Left + 300, finalX_Left, 1.0f, 0.0f);
        animateCloud(Sunshine, "translationX", finalX_Left, finalX_Left , 1.0f, 0.0f);



    }


    private void animateCloud(ImageView cloud, String property, float startValue, float endValue, float startAlpha, float endAlpha) {
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(cloud, property, startValue, endValue);
        translationAnimator.setDuration(2000); // Тривалість анімації переміщення

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(cloud, "alpha", startAlpha, endAlpha);
        alphaAnimator.setDuration(2000); // Тривалість анімації прозорості

        // Виконання обох анімацій одночасно
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationAnimator, alphaAnimator);
        animatorSet.start();
    }




}




