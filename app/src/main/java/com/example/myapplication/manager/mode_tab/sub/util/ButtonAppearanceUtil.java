package com.example.myapplication.manager.mode_tab.sub.util;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.Button;

public class ButtonAppearanceUtil {

    public static void resetButtonAppearance(Button button) {
        button.setScaleX(1f);
        button.setScaleY(1f);
        ((GradientDrawable)button.getBackground()).setStroke(3, Color.WHITE);
    }

    public static void setActiveButtonAppearance(Button button) {
        button.setScaleX(1.1f);
        button.setScaleY(1.1f);
        ((GradientDrawable)button.getBackground()).setStroke(10, Color.WHITE);
    }

    public static int getDefaultColorFromDrawable(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            return ((GradientDrawable) drawable).getColor().getDefaultColor();
        }
        return Color.WHITE;
    }
}
