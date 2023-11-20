package com.example.myapplication;

import com.example.myapplication.constant.Lamp;
import com.example.myapplication.model.ModeColorData;

import java.util.HashMap;
import java.util.List;

public interface FragmentBroadcastListener {
    default  void onLampStateUpdate(Lamp lampState) {
        // Default implementation (empty)
    }

    default  void onBrightnessUpdate(int brightness) {
        // Default implementation (empty)
    }

    default  void onModeUpdate(int mode) {
        // Default implementation (empty)
    }

    default  void onColorsUpdate(HashMap<Integer, HashMap<Integer, List<Integer>>> colorData) {
        // Default implementation (empty)
    }

    default  void onDisconnect() {
        // Default implementation (empty)
    }

    default void onColorDataUpdate(List<ModeColorData> colorData){
        // Default implementation (empty)
    }
}