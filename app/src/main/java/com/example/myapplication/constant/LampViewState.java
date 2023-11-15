package com.example.myapplication.constant;

public abstract class LampViewState {
    private static boolean isLampOn = false;
    private static int valueBrightnessText = 10;
    private static int seekBarPosition = 0;
    private static int numberMode = 0;
    private static int previousProgress = 0;

    public static boolean isIsLampOn() {
        return isLampOn;
    }

    public static void setIsLampOn(boolean isLampOn) {
        LampViewState.isLampOn = isLampOn;
    }

    public static int getValueBrightnessText() {
        return valueBrightnessText;
    }

    public static void setValueBrightnessText(int valueBrightnessText) {
        LampViewState.valueBrightnessText = valueBrightnessText;
    }

    public static int getSeekBarPosition() {
        return seekBarPosition;
    }

    public static void setSeekBarPosition(int seekBarPosition) {
        LampViewState.seekBarPosition = seekBarPosition;
    }

    public static int getNumberMode() {
        return numberMode;
    }

    public static void setNumberMode(int numberMode) {
        LampViewState.numberMode = numberMode;
    }

    public static int getPreviousProgress() {
        return previousProgress;
    }

    public static void setPreviousProgress(int previousProgress) {
        LampViewState.previousProgress = previousProgress;
    }
}
