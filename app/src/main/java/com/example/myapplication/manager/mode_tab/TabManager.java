package com.example.myapplication.manager.mode_tab;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.manager.mode_tab.sub.ChangeColorTabManager;
import com.example.myapplication.manager.mode_tab.sub.ColorPickerManager;
import com.example.myapplication.manager.mode_tab.model.ModeTab;
import com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil;

import java.util.ArrayList;
import java.util.List;

public class TabManager {
    private final Context context;
    private ConstraintLayout currentImageView;
    private ChangeColorTabManager changeColorTabManager;
    private ColorPickerManager colorPickerManager;
    private List<ModeTab> modeTabs;
    public static Button selectedActiveBtn;
    private Mode lastVisibleMode = null; // Store the last visible mode
    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;

    private TextView textViewOneMode;
    private TextView textViewTwoMode;
    private TextView textViewThreeMode;


    public TabManager(Context context, View view) {
        this.context = context;

        initModeTabs(context, view);
        this.colorPickerManager = new ColorPickerManager(context, view, modeTabs);
        this.changeColorTabManager = new ChangeColorTabManager(context, view, colorPickerManager);
        this.currentImageView = view.findViewById(R.id.modeImage);
        this.textViewOneMode = view.findViewById(R.id.textView2);
        this.textViewTwoMode = view.findViewById(R.id.textView4);
        this.textViewThreeMode = view.findViewById(R.id.textView3);
        changeTab(LampCache.getMode());
        loadColorData();
        resetAllBtns();
    }

    private void textWhite(TextView txt){
        txt.setShadowLayer(
                10,
                0,
                10,
                0xAAFFFFFF
        );
    }

    private void textBlack(TextView txt){
        txt.setShadowLayer(
                10,
                0,
                10,
                0xAA000000
        );
    }
    public static Button getSelectedActiveColorBtn() {
        return selectedActiveBtn;
    }


    private void initModeTabs(Context context, View view) {
        modeTabs = new ArrayList<>();
        for (Mode mode : Mode.values()) {
            int tabLayoutId = context.getResources().getIdentifier("groupActiveColors" + (mode.getModeNumber() + 1), "id", context.getPackageName());
            modeTabs.add(new ModeTab(context, view, view.findViewById(tabLayoutId), mode.getModeNumber()));
        }
    }

    private void loadColorData() {
        try {
            Log.d("COLOR READ", "COLOR READ");
            MainActivity.getBleCommunicationUtil().readActiveColors();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Log.d("LightFragment", "Error loading color data", e);
        }
    }


    public void updateVisualMode(int modeNumber) {
        Mode mode = Mode.fromModeNumber(modeNumber);
        if (modeTabs.get(mode.getModeNumber()).getTabLayout().getVisibility() != View.VISIBLE) {
            changeTab(mode);
        }
    }

    public void changeTab(Mode mode) {
        resetAllBtns();
        updateTabVisibility(mode);
        updateLampMode(mode.getModeNumber());
    }

    private void resetAllBtns() {
        selectedActiveBtn = null;
        colorPickerManager.resetAll();
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getTabActiveButtonsManager().getActiveColorButtons()) {
                ButtonAppearanceUtil.resetButtonAppearance(activeColorButton);
            }
        }
    }

    private void updateTabVisibility(Mode activeMode) {
        LampCache.setMode(activeMode);
        for (int i = 0; i < modeTabs.size(); i++) {
            modeTabs.get(i).getTabLayout().setVisibility(i == activeMode.getModeNumber() ? View.VISIBLE : View.INVISIBLE);
        }

        updateTextViewColors(activeMode);
        ModeTab.currentMode = activeMode;
        currentImageView.setBackground(context.getDrawable(activeMode.getDrawableResId()));
    }

    private void updateTextViewColors(Mode mode) {
        textBlack(textViewOneMode);
        textBlack(textViewTwoMode);
        textBlack(textViewThreeMode);

        switch (mode) {
            case MODE_ONE:
                textWhite(textViewOneMode);
                break;
            case MODE_TWO:
                textWhite(textViewTwoMode);
                break;
            case MODE_THREE:
                textWhite(textViewThreeMode);
                break;
        }
    }
    private void updateLampMode(int modeNumber) {
        // Cancel any existing callbacks
        debounceHandler.removeCallbacks(debounceRunnable);

        // Define the runnable
        debounceRunnable = () -> {
            if (LampCache.isOn() == Lamp.OFF) return;

            try {
                byte[] mode = new byte[]{(byte) modeNumber};
                MainActivity.getBleCommunicationUtil().writeMode(mode);
            } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
                Log.e("LightFragment", "Error updating lamp mode", e);
            }
        };

        // Schedule the runnable after a specified delay (e.g., 500 milliseconds)
        debounceHandler.postDelayed(debounceRunnable, 100);
    }


//    public void disableAllTabs() {
//        for (int i = 0; i < tabInfoList.size(); i++) {
//            if (tabInfoList.get(i).getTabLayout().getVisibility() == View.VISIBLE) {
//                lastVisibleMode = Mode.fromModeNumber(i); // Save the last visible mode
//                tabInfoList.get(i).getTabLayout().setVisibility(View.INVISIBLE);
//            }
//        }
//        currentImageView.setImageResource(0); // Remove any image resource
//    }

    public void restoreLastVisibleTab() {
        if (lastVisibleMode != null) {
            changeTab(lastVisibleMode);
            lastVisibleMode = null; // Reset the last visible mode
        }
    }

    public List<ModeTab> getTabs() {
        return modeTabs;
    }

    public void setAllActiveColorButtonsEnabled(boolean b) {
        for (ModeTab tab : modeTabs) {
            tab.getTabActiveButtonsManager().setAllActiveColorButtonsEnabled(b); // Disable all active color buttons
        }
    }

    public static boolean isAnyActiveColorButtonSelected(){
        return selectedActiveBtn != null;
    }
}
