package com.example.myapplication.constant;

public abstract class LampViewState {
    private static Lamp isLampOn = Lamp.OFF;
    private static int brightnessPercentageText = 10;
    private static int seekBarPos = 0;
    private static int prevSeekBarPos = 0;
    private static int numberMode = 0;

    public static Lamp getIsLampOn() {
        return isLampOn;
    }

    public static void setIsLampOn(Lamp isLampOn) {
        LampViewState.isLampOn = isLampOn;
    }

    public static int getBrightnessPercentageText() {
        return brightnessPercentageText;
    }

    public static void setBrightnessPercentageText(int brightnessPercentageText) {
        LampViewState.brightnessPercentageText = brightnessPercentageText;
    }

    public static int getSeekBarPos() {
        return seekBarPos;
    }

    public static void setSeekBarPos(int seekBarPos) {
        LampViewState.seekBarPos = seekBarPos;
    }

    public static int getNumberMode() {
        return numberMode;
    }

    public static void setNumberMode(int numberMode) {
        LampViewState.numberMode = numberMode;
    }

    public static int getPrevSeekBarPos() {
        return prevSeekBarPos;
    }

    public static void setPrevSeekBarPos(int prevSeekBarPos) {
        LampViewState.prevSeekBarPos = prevSeekBarPos;
    }
}
