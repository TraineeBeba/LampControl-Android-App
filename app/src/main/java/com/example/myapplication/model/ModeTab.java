package com.example.myapplication.model;

import android.widget.Button;

import com.example.myapplication.constant.Mode;

import java.util.ArrayList;
import java.util.List;

public class ModeTab {
    public static final int COLOR_PICKER_BUTTONS_COUNT = 15;
//    static private Button selectedColorPickerBtn;
    private Button selectedActiveColorBtn;
    private List<Button> activeColorButtons = new ArrayList<>();

    public static Mode currentMode = Mode.MODE_ONE;
    public static List<Button> colorPickerButtons;


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
}
