package com.example.myapplication.model;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ModeColorData {
    public int modeIndex;
    public List<RGBColor> colors;

    public ModeColorData(int modeIndex, List<RGBColor> colors) {
        this.modeIndex = modeIndex;
        this.colors = colors;
    }

    public static class RGBColor {
        public int red;
        public int green;
        public int blue;

        public RGBColor(int r, int g, int b) {
            this.red = r;
            this.green = g;
            this.blue = b;
        }
    }


    public static List<ModeColorData> parseColorData(byte[] data) {

        List<ModeColorData> modeColors = new ArrayList<>();

        if (data != null && data.length > 0) {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            int modeCount = buffer.get() & 0xFF;

            for (int modeIndex = 0; modeIndex < modeCount; modeIndex++) {
                int currentModeIndex = buffer.get() & 0xFF;
                int colorCount = buffer.get() & 0xFF;

                List<RGBColor> colors = new ArrayList<>();
                for (int colorIndex = 0; colorIndex < colorCount; colorIndex++) {
                    int red = buffer.get() & 0xFF;
                    int green = buffer.get() & 0xFF;
                    int blue = buffer.get() & 0xFF;
                    colors.add(new RGBColor(red, green, blue));
                }

                modeColors.add(new ModeColorData(currentModeIndex, colors));
            }
        }

        return modeColors;
    }

}
