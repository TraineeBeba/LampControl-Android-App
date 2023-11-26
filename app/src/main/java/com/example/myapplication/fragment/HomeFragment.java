package com.example.myapplication.fragment;

import android.animation.AnimatorSet;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.animation.ObjectAnimator;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.widget.Toast;

import com.example.myapplication.listener.FragmentBroadcastListener;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.StateAnimation;

public class HomeFragment extends Fragment implements FragmentBroadcastListener {
    private ConstraintLayout homeLayout;
    private Button btnNavWifi, btnNavLight;
    private ImageButton btnToggleLamp;
    private ImageView cloudImageLeftBlack, Sunshine, cloudImageLeftWhite, cloudImageRightBlack, cloudImageRightWhite;
    Lamp currentState = Lamp.OFF;


    private int widthImageCloud;
    private float widthDpCloud;
    private int offset;

    private int screenWidth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("HOME", "onCreateView " + this);

        View view = inflater.inflate(R.layout.home, container, false);

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
//            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
//            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void updateVisual(Lamp lampState) {
        resetAnimations();
        Log.d("lampState", "lampState " + lampState);
        Log.d("currentState", "currentState " + lampState);
        if (lampState == currentState) {
            return;
        }

        Log.d("updateVisual", "After");
        switch (lampState) {
            case OFF:
                btnToggleLamp.setBackgroundResource(R.drawable.turn_off_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_off);


                if (StateAnimation.finalX_Left != 0){
                    animationBtnOff();
                    StateAnimation.finalX_Left = cloudImageLeftBlack.getTranslationX()+offset;
                    StateAnimation.finalX_Right = cloudImageRightBlack.getTranslationX()-offset;
                }
                else{
                    animationStopBtnOff();
                }

                currentState = Lamp.OFF;
                break;
            case ON:

                btnToggleLamp.setBackgroundResource(R.drawable.turn_on_image);
                homeLayout.setBackgroundResource(R.drawable.homescreen__background_on);

                // 0 start position
                if (StateAnimation.finalX_Left == 0){
                    animationBtnOn();
                    StateAnimation.finalX_Left = cloudImageLeftBlack.getTranslationX()-offset;
                    StateAnimation.finalX_Right = cloudImageRightBlack.getTranslationX()+offset;
                }
                else{
                    animationStopBtnOn();
                }

                currentState = Lamp.ON;
                break;
        }
    }

    private void resetAnimations() {
        // Cancel or reset all ongoing animations
        cloudImageLeftBlack.clearAnimation();
        cloudImageLeftWhite.clearAnimation();
        cloudImageRightBlack.clearAnimation();
        cloudImageRightWhite.clearAnimation();
        Sunshine.clearAnimation();
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
//        toggleView = view.findViewById(R.id.image_turn);
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


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        cloudImageLeftBlack.post(() -> {
            widthImageCloud = cloudImageLeftBlack.getWidth();
//                widthDpCloud = widthImageCloud / getResources().getDisplayMetrics().density;

            offset = (int) (screenWidth*0.05 + widthImageCloud / 2);
            Log.d("widthImageCloud", String.valueOf(widthImageCloud));
            Log.d("screenWidth", String.valueOf(screenWidth));
            Log.d("offset", String.valueOf(offset));
        });
    }

    public void animationBtnOn() {
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left - offset, 1.0f, 0.0f);
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left - offset, 0.0f, 1.0f);
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right + offset, 1.0f, 0.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right + offset, 0.0f, 1.0f);
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
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left+offset, 0.0f, 1.0f); // Повернення до первісного положення
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left+offset, 1.0f, 0.0f); // Повернення до первісного положення
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right-offset,0.0f, 1.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right-offset, 1.0f, 0.0f);
        animateCloud(Sunshine, "translationX", 0, 0 , 1.0f, 0.0f);
    }

    public void animationStopBtnOff() {
        animateCloud(cloudImageLeftBlack, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left, 1.0f, 1.0f);
        animateCloud(cloudImageLeftWhite, "translationX", StateAnimation.finalX_Left, StateAnimation.finalX_Left , 0.0f, 0.0f);
        animateCloud(cloudImageRightBlack, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right , 1.0f, 1.0f);
        animateCloud(cloudImageRightWhite, "translationX", StateAnimation.finalX_Right, StateAnimation.finalX_Right , 0.0f, 0.0f);
        animateCloud(Sunshine, "translationX", 0, 0 , 0.0f, 0.0f);
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

    @Override
    public void onConnect() {
        try {
            MainActivity.getBleCommunicationUtil().readLampState();
        } catch (NullPointerException | BluetoothNotConnectedException | CharacteristicNotFoundException e) {
//            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}




