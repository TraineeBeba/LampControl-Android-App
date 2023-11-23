package com.example.myapplication.manager.mode_tab.sub;

import static com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil.resetButtonAppearance;
import static com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil.setActiveButtonAppearance;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.example.myapplication.R;
import com.example.myapplication.manager.mode_tab.TabManager;
import com.example.myapplication.manager.mode_tab.model.ModeColorData;
import com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil;

import java.util.ArrayList;
import java.util.List;

public class TabActiveButtonsManager {
    private final Context context;
    private List<Button> activeColorButtons;

    private Button selectedActiveBtn = null;

    public TabActiveButtonsManager(Context context, View view){
        this.context = context;
        initTabActiveColorButtons(view);
        initTabActiveColorButtonsListeners();
    }

    private void initTabActiveColorButtons(View view) {
        activeColorButtons = new ArrayList<>();
        this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode1_1));
        this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_1));
        this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_2));
        this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_3));
        this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_4));
        this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode3_1));
    }


    private void toggleActiveColorButton(Button button) {
        if (button.equals(selectedActiveBtn)) {
            resetButtonAppearance(button);
            TabManager.selectedActiveBtn = null;
        } else {
            if (selectedActiveBtn != null) {
                resetButtonAppearance(selectedActiveBtn);
            }
            setActiveButtonAppearance(button);
            TabManager.selectedActiveBtn = selectedActiveBtn;
            // Reset appearance of all color picker buttons
            for (Button colorPickerButton :  ColorPickerManager.getColorPickerButtons()) {
                resetButtonAppearance(colorPickerButton);
            }
        }
        updateColorPickerButtonState();
    }

    private boolean isAnyActiveColorButtonSelected() {
        if (selectedActiveBtn != null) {
            return true;
        }
        return false;
    }

    private void updateColorPickerButtonState() {
        boolean activeColorButtonSelected = isAnyActiveColorButtonSelected();
        for (Button colorPickerButton : ColorPickerManager.getColorPickerButtons()) {
            colorPickerButton.setEnabled(activeColorButtonSelected);
            if (!activeColorButtonSelected) {
                resetButtonAppearance(colorPickerButton);
            }
        }
    }

    private void initTabActiveColorButtonsListeners() {
        View.OnClickListener activeColorButtonListener = v -> toggleActiveColorButton((Button) v);

        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        for (Button activeColorButton : activeColorButtons) {
            activeColorButton.setOnClickListener(activeColorButtonListener);

            // Apply saved color or default
            Drawable background = activeColorButton.getBackground();
            int defaultColor = ButtonAppearanceUtil.getDefaultColorFromDrawable(background); // Define your default color
            int savedColor = sharedPref.getInt("activeColor" + activeColorButton.getId(), defaultColor);
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(savedColor);
            }
        }
    }


//
//    public void resetAll(){
//        resetAllActiveColorButtons();
//        resetAllColorPickerBtn();
//    }

    public void updateActiveButtonColors(List<ModeColorData.RGBColor> colors) {
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

    void saveActiveColorToPreferences(int buttonId, int color) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("activeColor" + buttonId, color);
        editor.apply();
    }

    public void setAllActiveColorButtonsEnabled(boolean isEnabled) {
        for (Button activeColorButton : activeColorButtons) {
            activeColorButton.setEnabled(isEnabled);

//             Reset appearance if disabling
                if (!isEnabled) {
                    resetButtonAppearance(activeColorButton);

                }
        }
    }

    public List<Button> getActiveColorButtons() {
        return activeColorButtons;
    }

    //    public void setAllActiveColorButtonsEnabled(boolean isEnabled) {
//        for (ModeTab modeTab : modeTabs) {
//            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
//                activeColorButton.setEnabled(isEnabled);
//
//                // Reset appearance if disabling
//                if (!isEnabled) {
//                    resetButtonAppearance(activeColorButton);
//
//                }
//            }
//        }
//    }
}