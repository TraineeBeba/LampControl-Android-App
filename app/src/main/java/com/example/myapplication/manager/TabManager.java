package com.example.myapplication.manager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.model.ModeTab;
import com.example.myapplication.model.TabInfo;
import com.example.myapplication.util.BLECommunicationUtil;

import java.util.List;

public class TabManager {
    private BLECommunicationUtil bluetoothComm;
    private List<TabInfo> tabInfoList;
    private ImageView currentImageView;
    private static ActiveButtonsManager colorBtnManager;

    private List<ModeTab> modeTabs;

    public TabManager(BLECommunicationUtil bluetoothComm, List<ModeTab> modeTabs, List<TabInfo> tabInfoList, ImageView currentImageView, View parentView) {
        this.bluetoothComm = bluetoothComm;
        this.tabInfoList = tabInfoList;
        this.currentImageView = currentImageView;
        this.modeTabs = modeTabs;
        initColorPicker(parentView);
        initActiveButtonManager();
    }

    public ActiveButtonsManager getActiveButtonsManager() {
        return colorBtnManager;
    }

    private void initActiveButtonManager() {
        colorBtnManager = new ActiveButtonsManager(modeTabs, ModeTab.getColorPickerButtons());
    }

    private void initColorPicker(View parentView) {
        for (int i = 1; i <= ModeTab.COLOR_PICKER_BUTTONS_COUNT; i++) {
            int buttonId = parentView.getResources().getIdentifier("colorBtn" + i, "id", parentView.getContext().getPackageName());
            Button button = parentView.findViewById(buttonId);
            ModeTab.getColorPickerButtons().add(button);
        }
    }
    public void updateVisualMode(int modeNumber) {
        Mode mode = Mode.fromModeNumber(modeNumber);
        if (tabInfoList.get(mode.getModeNumber()).getTabLayout().getVisibility() != View.VISIBLE) {
            changeTab(mode);
        }
    }

    public void changeTab(Mode mode) {
        colorBtnManager.resetAll();
        updateTabVisibility(mode);
        updateLampMode(mode.getModeNumber());
    }

    private void updateTabVisibility(Mode activeMode) {
        LampCache.setMode(activeMode.getModeNumber());
        for (int i = 0; i < tabInfoList.size(); i++) {
            tabInfoList.get(i).getTabLayout().setVisibility(i == activeMode.getModeNumber() ? View.VISIBLE : View.INVISIBLE);
        }
        currentImageView.setImageResource(activeMode.getDrawableResId());
    }
    private void updateLampMode(int modeNumber) {
        if (LampCache.isOn() == Lamp.OFF) return;

        try {
            byte[] mode = new byte[]{(byte) modeNumber};
            bluetoothComm.writeMode(mode);
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Log.e("LightFragment", "Error updating lamp mode", e);
//            Toast.makeText(getContext(), "Error updating mode: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
