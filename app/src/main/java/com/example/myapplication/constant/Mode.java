package com.example.myapplication.constant;

import com.example.myapplication.R;

public enum Mode {
    MODE_ONE(0, R.drawable.mode_one_on),
    MODE_TWO(1, R.drawable.mode_two_on),
    MODE_THREE(2, R.drawable.mode_three_on);

    private final int modeNumber;
    private final int drawableResId;

    Mode(int modeNumber, int drawableResId) {
        this.modeNumber = modeNumber;
        this.drawableResId = drawableResId;
    }

    public int getModeNumber() {
        return modeNumber;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public static Mode fromModeNumber(int modeNumber) {
        for (Mode mode : values()) {
            if (mode.getModeNumber() == modeNumber) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid mode number: " + modeNumber);
    }
}
