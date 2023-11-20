package com.example.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.widget.Button;

import com.example.myapplication.constant.Mode;

import java.util.ArrayList;
import java.util.List;

public class ModeTab {
    public static final int COLOR_PICKER_BUTTONS_COUNT = 15;
//    static private Button selectedColorPickerBtn;
    private Button selectedActiveColorBtn;
    private List<Button> activeColorButtons = new ArrayList<>();

    private List<Integer> buttonColors;

    public static Mode currentMode = Mode.MODE_ONE;
    public static List<Button> colorPickerButtons;

    public ModeTab() {
        buttonColors = new ArrayList<>();
        // Initialize buttonColors with default values or actual colors
    }

    public int getColorForButtonIndex(int index) {
        if (index >= 0 && index < buttonColors.size()) {
            return buttonColors.get(index);
        } else {
            return Color.WHITE;
        }
    }

    // Method to update color for a specific button
    public void updateColorForButtonIndex(int index, int color) {
        if (index >= 0 && index < buttonColors.size()) {
            buttonColors.set(index, color);
        }
    }


    public List<Button> getActiveColorButtons() {
        return activeColorButtons;
    }


//    public static Button getSelectedColorPickerBtn() {
//        return selectedColorPickerBtn;
//    }
//
//    public static void setSelectedColorPickerBtn(Button selectedColorPickerBtn) {
//        ModeTab.selectedColorPickerBtn = selectedColorPickerBtn;
//    }

    public Button getSelectedActiveColorBtn() {
        return selectedActiveColorBtn;
    }

    public void setSelectedActiveColorBtn(Button selectedActiveColorBtn) {
        this.selectedActiveColorBtn = selectedActiveColorBtn;
    }

    public void setActiveColorButtons(List<Button> activeColorButtons) {
        this.activeColorButtons = activeColorButtons;
    }

    public static void setColorPickerButtons(List<Button> colorPickerButtons) {
        ModeTab.colorPickerButtons = colorPickerButtons;
    }

//    public static void setColorPickerButtons(List<Button> colorPickerButtons) {
//        ModeTab.colorPickerButtons = colorPickerButtons;
//    }

    public void updateActiveButtonColors(Context context, List<ModeColorData.RGBColor> colors) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);

        for (int i = 0; i < activeColorButtons.size() && i < colors.size(); i++) {
            Button button = activeColorButtons.get(i);
            ModeColorData.RGBColor color = colors.get(i);
            Log.d("updateActiveButtonColors", "" + color.red + color.green + color.blue);

            Drawable background = button.getBackground();
            if (background instanceof GradientDrawable) {
                // If the background is a GradientDrawable, update its color
                GradientDrawable gradientDrawable = (GradientDrawable) background;
                gradientDrawable.setColor(android.graphics.Color.rgb(color.red, color.green, color.blue));
            } else {
                // If the background is not a GradientDrawable, update the button's background color
                button.setBackgroundColor(android.graphics.Color.rgb(color.red, color.green, color.blue));
            }

            // Save the updated color to preferences
            sharedPref.edit().putInt("activeColor" + button.getId(), android.graphics.Color.rgb(color.red, color.green, color.blue)).apply();
        }
    }
}
