package com.example.myapplication.manager.mode_tab.model;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.constant.Mode;
import com.example.myapplication.manager.mode_tab.sub.ActiveButtonsManager;

import java.util.ArrayList;
import java.util.List;

public class ModeTab {
    private final ActiveButtonsManager activeButtonsManager;
    private Button selectedActiveColorBtn;

    public static Mode currentMode = Mode.MODE_ONE;

    private List<Integer> buttonColors;

    private ConstraintLayout tabLayout;
    public ModeTab(Context context, View view, ConstraintLayout tabLayout, int modeNumber) {
        this.tabLayout = tabLayout;
        this.activeButtonsManager = new ActiveButtonsManager(context, view, modeNumber);
        this.buttonColors = new ArrayList<>();
        // Initialize other ModeTab fields
    }

    // Getters and setters for tabLayout and drawableResId
    public ConstraintLayout getTabLayout() {
        return tabLayout;
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

    public ActiveButtonsManager getTabActiveButtonsManager() {
        return activeButtonsManager;
    }

    public void updateTabActiveButtonColors(List<ModeColorData.RGBColor> colors) {
        activeButtonsManager.updateActiveButtonColors(colors);
    }


}
