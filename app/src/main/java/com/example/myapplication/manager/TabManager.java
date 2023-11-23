package com.example.myapplication.manager;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myapplication.MainActivity;
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
    private List<TabInfo> tabInfoList;
    private ImageView currentImageView;
    private static ActiveButtonsManager colorBtnManager;
    private Mode lastVisibleMode = null; // Store the last visible mode


    private List<ModeTab> modeTabs;
    private Context context;

    public TabManager( Context context, List<TabInfo> tabInfoList, ImageView currentImageView, ActiveButtonsManager activeButtonsManager) {
        this.context = context;
        this.tabInfoList = tabInfoList;
        this.currentImageView = currentImageView;
        colorBtnManager = activeButtonsManager; // Use passed instance
        loadColorData();

    }

    private void loadColorData() {
        try {
            Log.d("COLOR READ", "COLOR READ");
            MainActivity.getBleCommunicationUtil().readActiveColors();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Log.d("LightFragment", "Error loading color data", e);
        }
    }

    public ActiveButtonsManager getActiveButtonsManager() {
        return colorBtnManager;
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

        ModeTab.currentMode = activeMode;
        currentImageView.setImageResource(activeMode.getDrawableResId());
    }
    private void updateLampMode(int modeNumber) {
        if (LampCache.isOn() == Lamp.OFF) return;

        try {
            byte[] mode = new byte[]{(byte) modeNumber};
            MainActivity.getBleCommunicationUtil().writeMode(mode);
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Log.e("LightFragment", "Error updating lamp mode", e);
//            Toast.makeText(getContext(), "Error updating mode: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void disableAllTabs() {
        for (int i = 0; i < tabInfoList.size(); i++) {
            if (tabInfoList.get(i).getTabLayout().getVisibility() == View.VISIBLE) {
                lastVisibleMode = Mode.fromModeNumber(i); // Save the last visible mode
                tabInfoList.get(i).getTabLayout().setVisibility(View.INVISIBLE);
            }
        }
        currentImageView.setImageResource(0); // Remove any image resource
    }

    public void restoreLastVisibleTab() {
        if (lastVisibleMode != null) {
            changeTab(lastVisibleMode);
            lastVisibleMode = null; // Reset the last visible mode
        }
    }
}
