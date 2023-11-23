package com.example.myapplication.constant;

import com.example.myapplication.manager.seekbar.util.BrightnessModeUtil;

public abstract class LampCache {
    private static Lamp isOn = Lamp.OFF;
    private static int brightness = 25;
    private static int seekBarPos = 0;
    private static int mode = 0;


    public static Lamp isOn() {
        return isOn;
    }

    public static void setIsOn(Lamp isOn) {
        LampCache.isOn = isOn;
    }

    public static int getBrightnessText() {
        int seekBarPosition = BrightnessModeUtil.getSeekBarPosition(brightness);
        return BrightnessModeUtil.calculatePercentage(seekBarPosition);
    }

    public static int getBrightness() {
        return brightness;
    }

    public static void setBrightness(int brightness) {
        LampCache.brightness = brightness;
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int mode) {
        LampCache.mode = mode;
    }


    public static int getSeekBarPos() {
        return seekBarPos;
    }

    public static void setSeekBarPos(int seekBarPos) {
        LampCache.seekBarPos = seekBarPos;
    }
}
