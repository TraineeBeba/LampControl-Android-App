package com.example.myapplication.manager.mode_tab.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.constant.Mode;
import com.example.myapplication.manager.mode_tab.sub.ColorPickerManager;
import com.example.myapplication.manager.mode_tab.sub.TabActiveButtonsManager;

import java.util.ArrayList;
import java.util.List;

public class ModeTab {
    private final TabActiveButtonsManager tabActiveButtonsManager;
    private Button selectedActiveColorBtn;

    public static Mode currentMode = Mode.MODE_ONE;

    private List<Integer> buttonColors;

    private ConstraintLayout tabLayout;
    private int drawableResId;

    public ModeTab(Context context, View view, ConstraintLayout tabLayout, int drawableResId) {
        this.tabLayout = tabLayout;
        this.drawableResId = drawableResId;
        this.tabActiveButtonsManager = new TabActiveButtonsManager(context, view);
        this.buttonColors = new ArrayList<>();
        // Initialize other ModeTab fields
    }

    // Getters and setters for tabLayout and drawableResId
    public ConstraintLayout getTabLayout() {
        return tabLayout;
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

    public void setTabLayout(ConstraintLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
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

    public TabActiveButtonsManager getTabActiveButtonsManager() {
        return tabActiveButtonsManager;
    }

    public void updateTabActiveButtonColors(List<ModeColorData.RGBColor> colors) {
        tabActiveButtonsManager.updateActiveButtonColors(colors);
    }


}
