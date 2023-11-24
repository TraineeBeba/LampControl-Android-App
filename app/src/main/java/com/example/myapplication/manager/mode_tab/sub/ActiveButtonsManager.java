package com.example.myapplication.manager.mode_tab.sub;

import static com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil.resetButtonAppearance;
import static com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil.setActiveButtonAppearance;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.example.myapplication.R;
import com.example.myapplication.manager.mode_tab.TabManager;
import com.example.myapplication.manager.mode_tab.model.ModeColorData;
import com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil;

import java.util.ArrayList;
import java.util.List;

public class ActiveButtonsManager {
    private final Context context;
    private List<Button> activeColorButtons;

    private Button selectedActiveBtn = null;

    public ActiveButtonsManager(Context context, View view, int mode){
        this.context = context;
        initTabActiveColorButtons(view, mode);
        initTabActiveColorButtonsListeners();
    }

    private void initTabActiveColorButtons(View view, int mode) {
        activeColorButtons = new ArrayList<>();

        switch (mode) {
            case 0:
                this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode1_1));
                break;
            case 1:
                this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_1));
                this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_2));
                this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_3));
                this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode2_4));
                break;
            case 2:
                this.activeColorButtons.add(view.findViewById(R.id.activeColorBtnMode3_1));
                break;
        }
    }

    private void toggleActiveColorButton(Button button) {
        if (button.equals(selectedActiveBtn)) {
            resetButtonAppearance(button);
            TabManager.selectedActiveBtn = null;
            selectedActiveBtn = null;
        } else {
            if (selectedActiveBtn != null) {
                resetButtonAppearance(selectedActiveBtn);
            }
            setActiveButtonAppearance(button);
            selectedActiveBtn = button;
            TabManager.selectedActiveBtn = button;
            // Reset appearance of all color picker buttons
            for (Button colorPickerButton :  ColorPickerManager.getColorPickerButtons()) {
                resetButtonAppearance(colorPickerButton);
            }
        }
        updateColorPickerButtonState();
    }

    private boolean isAnyActiveColorButtonSelected() {
        return selectedActiveBtn != null;
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

    public void updateActiveButtonColors(List<ModeColorData.RGBColor> colors) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);

        for (int i = 0; i < activeColorButtons.size() && i < colors.size(); i++) {
            Button button = activeColorButtons.get(i);
            Log.d("updateActiveButtonColors", "button: " + button.getId());
            ModeColorData.RGBColor color = colors.get(i);
            int red = color.red;
            int green = color.green;
            int blue = color.blue;
            Log.d("updateActiveButtonColors", "red: " + red + " green: " + green + " blue: " + blue);

            int savedColor = Color.rgb(red, green, blue);
            Drawable background = button.getBackground();
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(savedColor);
            }

            // Save the updated color to preferences
            sharedPref.edit().putInt("activeColor" + button.getId(), android.graphics.Color.rgb(red, green, blue)).apply();
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
}