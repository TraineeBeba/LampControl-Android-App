package com.example.myapplication.manager.seekbar.util;

public class BrightnessModeUtil {

    // Constants for brightness calculation
    private static final int MIN_BRIGHTNESS = 25;
    private static final int MAX_BRIGHTNESS = 250;
    private static final int MIN_SEEK_BAR = 0;
    private static final int MAX_SEEK_BAR = 9;

    /**
     * Calculates the position of the SeekBar based on the brightness value.
     *
     * @param brightness The brightness value to convert.
     * @return The position on the SeekBar corresponding to the brightness value.
     */
    public static int getSeekBarPosition(int brightness) {
        double scaleFactor = (double) (MAX_SEEK_BAR - MIN_SEEK_BAR) / (MAX_BRIGHTNESS - MIN_BRIGHTNESS);
        return (int) ((brightness - MIN_BRIGHTNESS) * scaleFactor);
    }

    /**
     * Calculates the percentage representation of a given input value.
     *
     * @param inputValue The input value to convert to percentage.
     * @return The percentage value.
     */
    public static int calculatePercentage(double inputValue) {
        int minOld = 0;
        int minNew = 10;
        int maxNew = 100;
        return (int) (minNew + ((inputValue - minOld) / (MAX_SEEK_BAR - minOld)) * (maxNew - minNew));
    }

    // Additional utility methods as needed...
}
