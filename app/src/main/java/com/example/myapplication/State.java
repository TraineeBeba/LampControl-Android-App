package com.example.myapplication;

public abstract class State {
    public static boolean isLampOn = false;
    public static int valueBrightnessText = (int) (3*100)/6; // 3 - middle seekBar, 6+1 - count step
    public static int seekBarposition = 3;

    public static int numberMode = 0;
}
