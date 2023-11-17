package com.example.myapplication;

import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class ModeTab {
    public static final int COLOR_PICKER_BUTTONS_COUNT = 15;
    static private Button selectedColorPickerBtn;
    private Button selectedActiveColorBtn;
    private List<Button> activeColorButtons = new ArrayList<>(COLOR_PICKER_BUTTONS_COUNT);

    static private List<Button> colorPickerButtons = new ArrayList<>();


    public List<Button> getActiveColorButtons() {
        return activeColorButtons;
    }


    public static Button getSelectedColorPickerBtn() {
        return selectedColorPickerBtn;
    }

    public static void setSelectedColorPickerBtn(Button selectedColorPickerBtn) {
        ModeTab.selectedColorPickerBtn = selectedColorPickerBtn;
    }

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

    public static List<Button> getColorPickerButtons() {
        return colorPickerButtons;
    }

//    public static void setColorPickerButtons(List<Button> colorPickerButtons) {
//        ModeTab.colorPickerButtons = colorPickerButtons;
//    }
}
