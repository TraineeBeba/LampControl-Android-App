package com.example.myapplication.fragment;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_BRIGHTNESS_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_MODE_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

// Для зміни фону кнопок
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.github.mata1.simpledroidcolorpicker.pickers.CircleColorPicker;

import com.example.myapplication.MainActivity;
import com.example.myapplication.ModeTab;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampViewState;
import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.util.BrightnessModeUtil;
import com.welie.blessed.WriteType;

import java.util.ArrayList;
import java.util.List;

public class LightFragment extends Fragment {
    private Button btnNavHome;
    private Button btnNavWifi;

    private Button btnMode1;
    private Button btnMode2;
    private Button btnMode3;

    List<LinearLayout> modeTabsVisual = new ArrayList<>();

    private Button button_add_color; // Btn next panel add color
    private Button buttonChangeColor; // Btn change color in paliter
    private Button backToPanelModeBtn; // Btn back
    private Button fixColor;

    private RelativeLayout panelAddColor;
    private RelativeLayout panelMode;
    private CircleColorPicker ccp; // picker


    private List<ModeTab> modeTabs = new ArrayList<>();

    private boolean isSeekBarDisabled = false; // Add this flag
    private ImageView currentImageView;

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
                updateVisualBar(brightness);
            }
        }
    };

    private final BroadcastReceiver modeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.MODE_UPDATE_ACTION)) {
                String modeStr = intent.getStringExtra(BluetoothHandler.EXTRA_MODE);
                int mode = Integer.valueOf(modeStr);
                updateVisualMode(mode);
            }
        }
    };

    private void updateVisualMode(int mode) {
        Log.d("MODE", String.valueOf(mode));
        if(mode == 0){
            changeTab(mode, R.drawable.mode_one_on);
        } else if(mode == 1){
            changeTab(mode, R.drawable.mode_two_on);
        } else if(mode == 2){
            changeTab(mode, R.drawable.mode_three_on);
        }

    }

    private void updateVisualBar(int brightness) {
        int currentPosition = seekBar.getProgress();
        int calculatedPosition = BrightnessModeUtil.getSeekBarPosition(brightness);

        if (calculatedPosition != currentPosition) {
            LampViewState.setSeekBarPos(calculatedPosition);
            seekBar.setProgress(calculatedPosition);
        }

        int brightnessPercentageText = BrightnessModeUtil.calculatePercentage(calculatedPosition);
        LampViewState.setBrightnessPercentageText(brightnessPercentageText);
        textView.setText(brightnessPercentageText + " %");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.light, container, false);

        initTabs();
        initView(view);
        initBtnListeners(view);
        saveMode(LampViewState.getNumberMode());
        setUpBrightness();

        registerReceivers();

        // ((ShapeDrawable)background).getPaint().setColor(Color.RED);

        return view;
    }

    private void initTabs() {
        for (int i = 0; i < 3; i++) {
            modeTabs.add(new ModeTab());
        }
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
    private void writeMode(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
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
        currentImageView = view.findViewById(R.id.modeImage);
        btnNavHome = view.findViewById(R.id.button_home);
        btnNavWifi = view.findViewById(R.id.button_wifi);
        btnMode1 = view.findViewById(R.id.button_one_color);
        btnMode2 = view.findViewById(R.id.button_rainbow);
        btnMode3 = view.findViewById(R.id.button_data_night);

        modeTabsVisual.add((LinearLayout) view.findViewById(R.id.groupActiveColors1));
        modeTabsVisual.add((LinearLayout) view.findViewById(R.id.groupActiveColors2));
        modeTabsVisual.add((LinearLayout) view.findViewById(R.id.groupActiveColors3));

        ///////

        button_add_color = view.findViewById(R.id.addColorBtn);
        buttonChangeColor = view.findViewById(R.id.buttonChangeColor);
        backToPanelModeBtn = view.findViewById(R.id.backToPanelModeBtn);
        panelAddColor = view.findViewById(R.id.panelAddColor);
        panelMode = view.findViewById(R.id.panelMode);
        ccp = (CircleColorPicker)view.findViewById(R.id.rcp1);
        buttonChangeColor = view.findViewById(R.id.buttonChangeColor);
        fixColor = view.findViewById(R.id.fixColor);


//        modeTab1Visual = );
//        modeTab2Visual = view.findViewById(R.id.groupActiveColors2);
//        modeTab3Visual = view.findViewById(R.id.groupActiveColors3);

        // background = button_ActiveColor1_1.getBackground();

        modeTabs.get(0).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode1_1));

        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_1));
        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_2));
        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_3));
        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_4));

        modeTabs.get(2).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode3_1));

        //        textView.setText(LampViewState.getBrightnessPercentageText());
    }

    private void initBtnListeners(View view) {
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
                    textView.setText(BrightnessModeUtil.calculatePercentage(progress) + " %");
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
            changeTab(0, R.drawable.mode_one_on);
        });

        btnMode2.setOnClickListener(v -> {
            changeTab(1, R.drawable.mode_two_on);
        });

        btnMode3.setOnClickListener(v -> {
            changeTab(2, R.drawable.mode_three_on);
        });


        initActiveColorButtons();
        initColorPickerButtons(view);

        disableAllActiveColorButtons();
        disableAllColorPickerBtn();

        button_add_color.setOnClickListener(v -> {
            panelMode.setVisibility(View.INVISIBLE);
            panelAddColor.setVisibility(View.VISIBLE);

        });

        backToPanelModeBtn.setOnClickListener(v -> {
            panelMode.setVisibility(View.VISIBLE);
            panelAddColor.setVisibility(View.INVISIBLE);

        });

        buttonChangeColor.setOnClickListener(v -> {
            // змінюєм колір кружбалика в палітрі

        });

    }

    private void changeTab(int modeNumber, int drawableResId) {
        for (int i = 0; i < modeTabsVisual.size(); i++) {
            if(i == modeNumber){
                modeTabsVisual.get(i).setVisibility(View.VISIBLE);
            } else{
                modeTabsVisual.get(i).setVisibility(View.INVISIBLE);
            }
        }

        currentImageView.setImageResource(drawableResId);
        LampViewState.setNumberMode(modeNumber);

        // Reset active color buttons and color picker buttons
        disableAllActiveColorButtons();
        disableAllColorPickerBtn();

        try {
            readLampState();
            if(LampViewState.getIsLampOn() == Lamp.ON){
                byte[] mode = new byte[]{(byte) modeNumber};
                writeMode(mode);
            }
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            // Handle exception
        }
    }

    private void updateColorPickerButtonState() {
        boolean activeColorButtonSelected = isAnyActiveColorButtonSelected();
        for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
            colorPickerButton.setEnabled(activeColorButtonSelected);
            if (!activeColorButtonSelected) {
                resetButtonAppearance(colorPickerButton);
            }
        }
    }



    private void initActiveColorButtons() {
        View.OnClickListener activeColorButtonListener = v -> {
            toggleActiveColorButton((Button) v);
        };

        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                activeColorButton.setOnClickListener(activeColorButtonListener);
            }
        }
    }

    private void initColorPickerButtons(View view) {
        for (int i = 1; i <= ModeTab.COLOR_PICKER_BUTTONS_COUNT; i++) {
            int buttonId = getResources().getIdentifier("colorBtn" + i + "_1", "id", getContext().getPackageName());
            Button button = view.findViewById(buttonId);
            button.setOnClickListener(this::onColorPickerButtonClick);
            ModeTab.getColorPickerButtons().add(button);
        }
    }

//    private void initColorPickerButtons(View view) {
//        int[] colors = {/* array of colors */}; // Define your color array
//        for (int i = 1; i <= ModeTab.COLOR_PICKER_BUTTONS_COUNT; i++) {
//            int buttonId = getResources().getIdentifier("colorBtn" + i, "id", getContext().getPackageName());
//            Button button = view.findViewById(buttonId);
//            Drawable background = button.getBackground();
//            if (background instanceof GradientDrawable) {
//                ((GradientDrawable) background).setColor(colors[i-1]); // Set unique color
//            }
//            button.setOnClickListener(this::onColorPickerButtonClick);
//            ModeTab.getColorPickerButtons().add(button);
//        }
//    }


    // Modify onColorPickerButtonClick method
    private void onColorPickerButtonClick(View v) {
        Button clickedButton = (Button) v;
        if (isAnyActiveColorButtonSelected()) {
            // Reset appearance for all color picker buttons
            for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
                resetButtonAppearance(colorPickerButton);
            }

            // Then, change the appearance of the clicked button and active color button
            for (ModeTab modeTab : modeTabs) {
                Button selectedActiveButton = modeTab.getSelectedActiveColorBtn();
                if (selectedActiveButton != null) {
                    changeActiveColorButtonColor(selectedActiveButton, clickedButton);
                    setActiveButtonAppearance(clickedButton); // Indicate selection
                }
            }
        }
    }

    private boolean isAnyActiveColorButtonSelected() {
        for (ModeTab modeTab : modeTabs) {
            if (modeTab.getSelectedActiveColorBtn() != null) {
                return true;
            }
        }
        return false;
    }

    private void toggleActiveColorButton(Button button) {
        ModeTab tabByButton = getTabByButton(button);
        if (tabByButton != null) {
            Button selectedActiveBtn = tabByButton.getSelectedActiveColorBtn();

            if (button.equals(selectedActiveBtn)) {
                resetButtonAppearance(button);
                tabByButton.setSelectedActiveColorBtn(null);
            } else {
                if (selectedActiveBtn != null) {
                    resetButtonAppearance(selectedActiveBtn);
                }
                setActiveButtonAppearance(button);
                tabByButton.setSelectedActiveColorBtn(button);

                // Reset appearance of all color picker buttons
                for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
                    resetButtonAppearance(colorPickerButton);
                }
            }
            updateColorPickerButtonState();
        }
    }



    private void changeActiveColorButtonColor(Button activeColorButton, Button colorPickerButton) {
        Drawable colorPickerBackground = colorPickerButton.getBackground();
        if (colorPickerBackground instanceof GradientDrawable) {
            int color = ((GradientDrawable) colorPickerBackground).getColor().getDefaultColor();

            Drawable activeColorBackground = activeColorButton.getBackground();
            if (activeColorBackground instanceof GradientDrawable) {

                ((GradientDrawable) activeColorBackground).setColor(color);
            }

            Log.d("Change Btn color", "Color changed to: " + Integer.toHexString(color));
        }
    }


    @Nullable
    private ModeTab getTabByButton(Button clickedButton) {
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                if(activeColorButton == clickedButton)
                    return modeTab;
            }
        }
        return null;
    }

    private void resetButtonAppearance(Button button) {
        button.setScaleX(1f);
        button.setScaleY(1f);
        ((GradientDrawable)button.getBackground()).setStroke(3, Color.WHITE);
    }

    private void setActiveButtonAppearance(Button button) {
        button.setScaleX(1.1f);
        button.setScaleY(1.1f);
        ((GradientDrawable)button.getBackground()).setStroke(10, Color.WHITE);
    }

    private void disableAllColorPickerBtn(){
        for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
            resetButtonAppearance(colorPickerButton);
        }
    }
    private void disableAllActiveColorButtons() {
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                resetButtonAppearance(activeColorButton);
                modeTab.setSelectedActiveColorBtn(null);
            }
        }
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

        IntentFilter filter2 = new IntentFilter(BluetoothHandler.MODE_UPDATE_ACTION);
        getActivity().registerReceiver(modeReceiver, filter2);
    }

    private void unRegisterReceivers() {
        getActivity().unregisterReceiver(brightnessReceiver);
        getActivity().unregisterReceiver(modeReceiver);
    }

    private void saveMode(int numbermode){
        switch (numbermode){
            case 0:
                currentImageView.setImageResource(R.drawable.mode_one_on);

                break;
            case 1:
                currentImageView.setImageResource(R.drawable.mode_two_on);

                break;
            case 2:
                currentImageView.setImageResource(R.drawable.mode_three_on);
                break;
        }
    }
}
