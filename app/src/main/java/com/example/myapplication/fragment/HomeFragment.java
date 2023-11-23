package com.example.myapplication.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.example.myapplication.FragmentBroadcastListener;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.StateAnimation;
import com.example.myapplication.util.BLECommunicationUtil;
import com.example.myapplication.util.BroadcastReceiverUtil;

public class HomeFragment extends Fragment implements FragmentBroadcastListener {
    private FrameLayout homeLayout;
    private ImageView toggleView;
    private Button btnNavWifi, btnNavLight, btnToggleLamp;
    private BroadcastReceiverUtil receiverUtil;
    private ImageView cloudImageLeftBlack;

    private ImageView Sunshine;
    private ImageView cloudImageLeftWhite;
    private ImageView cloudImageRightBlack;
    private ImageView cloudImageRightWhite;
    Lamp currentState = Lamp.OFF;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("HOME", "onCreateView " + this);

        View view = inflater.inflate(R.layout.home, container, false);
//        bluetoothComm = new BLECommunicationUtil(getContext());

        initView(view);
        initBtnListeners();
        loadState();

        return view;
    }



    private void loadState() {
        updateVisual(LampCache.isOn());
        try {
            MainActivity.getBleCommunicationUtil().readLampState();
        } catch (NullPointerException | BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLampState() {
        currentState = Lamp.getToggle(LampCache.isOn()); // Expected state after toggle
        updateVisual(currentState); // Update UI immediately

        try {
            MainActivity.getBleCommunicationUtil().readLampState();
            Lamp toggleState = Lamp.getToggle(LampCache.isOn());
            MainActivity.getBleCommunicationUtil().writeLampState(toggleState.name().getBytes());
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
        Log.d("updateVisual", "Before");
        if (lampState == currentState) {
            return;
        }

        Log.d("updateVisual", "After");
        switch (lampState) {
            case OFF:
                toggleView.setImageResource(R.drawable.turn_off_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_off);


                if (StateAnimation.finalX_Left != 0){
                    animationBtnOff();
                    StateAnimation.finalX_Left = cloudImageLeftBlack.getTranslationX()+400;
                    StateAnimation.finalX_Right = cloudImageRightBlack.getTranslationX()-300;
                }
                else{
                    animationStopBtnOff();
                }

                currentState = Lamp.OFF;
                break;
            case ON:

                toggleView.setImageResource(R.drawable.turn_on_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_on);

                // 0 start position
                if (StateAnimation.finalX_Left == 0){
                    animationBtnOn();
                    StateAnimation.finalX_Left = cloudImageLeftBlack.getTranslationX()-400;
                    StateAnimation.finalX_Right = cloudImageRightBlack.getTranslationX()+300;
                }
                else{
                    animationStopBtnOn();
                }

                currentState = Lamp.ON;
                break;
        }
    }

    public void animationStopBtnOff() {
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left, 1.0f, 1.0f);
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left , 0.0f, 0.0f);
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right , 1.0f, 1.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right , 0.0f, 0.0f);
        animateCloud(Sunshine, "translationX", 0, 0 , 0.0f, 0.0f);
    }

    private void initBtnListeners() {
        btnToggleLamp.setOnClickListener(v ->{
            toggleLampState();
            btnToggleLamp.setEnabled(false);

            new Handler(Looper.getMainLooper()).postDelayed(() -> btnToggleLamp.setEnabled(true), 1100);
        });

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

    public void animationBtnOn() {
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left - 400, 1.0f, 0.0f);
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left - 400, 0.0f, 1.0f);
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right + 300, 1.0f, 0.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right + 300, 0.0f, 1.0f);
        animateCloud(Sunshine, "translationX", 0, 0 , 0.0f, 1.0f);
    }

    public void animationStopBtnOn() {
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left, 0.0f, 0.0f);
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left, 1.0f, 1.0f);
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right, 0.0f, 0.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right, 1.0f, 1.0f);
        animateCloud(Sunshine, "translationX", 0, 0, 1.0f, 1.0f);
    }

    public void animationBtnOff() {
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left+400, 0.0f, 1.0f); // Повернення до первісного положення
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left+400, 1.0f, 0.0f); // Повернення до первісного положення
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right-300,0.0f, 1.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right-300, 1.0f, 0.0f);
        animateCloud(Sunshine, "translationX", 0, 0 , 1.0f, 0.0f);
    }


    private void animateCloud(ImageView cloud, String property, float startValue, float endValue, float startAlpha, float endAlpha) {
        ObjectAnimator translationAnimator = ObjectAnimator.ofFloat(cloud, property, startValue, endValue);
        translationAnimator.setDuration(1000); // Тривалість анімації переміщення

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(cloud, "alpha", startAlpha, endAlpha);
        alphaAnimator.setDuration(1000); // Тривалість анімації прозорості

        // Виконання обох анімацій одночасно
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translationAnimator, alphaAnimator);

        animatorSet.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("HOME", "DESTROY " + this.toString());
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentFragmentListener(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentFragmentListener(null);
        }
    }

    @Override
    public void onLampStateUpdate(Lamp state) {
        updateVisual(state);
    }

    @Override
    public void onDisconnect() {
        updateVisual(Lamp.OFF);
    }
}




