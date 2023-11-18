package com.example.myapplication.manager;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.myapplication.model.ModeTab;

import java.util.List;

public class ActiveButtonsManager {
    private final List<ModeTab> modeTabs;
    private final List<Button> colorPickerButtons;

    public ActiveButtonsManager(List<ModeTab> modeTabs, List<Button> colorPickerButtons) {
        this.modeTabs = modeTabs;
        this.colorPickerButtons = colorPickerButtons;
        initActiveColorButtonsListenrs();
        initColorPickerButtonsListenrs();
    }

    // Modify onColorPickerButtonClick method
    private void onColorPickerButtonClick(View v) {
        Button clickedButton = (Button) v;
        if (isAnyActiveColorButtonSelected()) {
            // Reset appearance for all color picker buttons
            for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
                resetButtonAppearance(colorPickerButton);
            }

            // Then, change the appearance of the clicked button and active color button
            for (ModeTab modeTab : modeTabs) {
                Button selectedActiveButton = modeTab.getSelectedActiveColorBtn();
                if (selectedActiveButton != null) {
                    changeActiveColorButtonColor(selectedActiveButton, clickedButton);
                    setActiveButtonAppearance(clickedButton); // Indicate selection
                }
            }
        }
    }

    private boolean isAnyActiveColorButtonSelected() {
        for (ModeTab modeTab : modeTabs) {
            if (modeTab.getSelectedActiveColorBtn() != null) {
                return true;
            }
        }
        return false;
    }

    private void toggleActiveColorButton(Button button) {
        ModeTab tabByButton = getTabByButton(button);
        if (tabByButton != null) {
            Button selectedActiveBtn = tabByButton.getSelectedActiveColorBtn();

            if (button.equals(selectedActiveBtn)) {
                resetButtonAppearance(button);
                tabByButton.setSelectedActiveColorBtn(null);
            } else {
                if (selectedActiveBtn != null) {
                    resetButtonAppearance(selectedActiveBtn);
                }
                setActiveButtonAppearance(button);
                tabByButton.setSelectedActiveColorBtn(button);

                // Reset appearance of all color picker buttons
                for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
                    resetButtonAppearance(colorPickerButton);
                }
            }
            updateColorPickerButtonState();
        }
    }

    private void changeActiveColorButtonColor(Button activeColorButton, Button colorPickerButton) {
        Drawable colorPickerBackground = colorPickerButton.getBackground();
        if (colorPickerBackground instanceof GradientDrawable) {
            int color = ((GradientDrawable) colorPickerBackground).getColor().getDefaultColor();

            Drawable activeColorBackground = activeColorButton.getBackground();
            if (activeColorBackground instanceof GradientDrawable) {
                ((GradientDrawable) activeColorBackground).setColor(color);
            }

            Log.d("Change Btn color", "Color changed to: " + Integer.toHexString(color));
        }
    }


    @Nullable
    private ModeTab getTabByButton(Button clickedButton) {
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                if(activeColorButton == clickedButton)
                    return modeTab;
            }
        }
        return null;
    }

    private void resetButtonAppearance(Button button) {
        button.setScaleX(1f);
        button.setScaleY(1f);
        ((GradientDrawable)button.getBackground()).setStroke(3, Color.WHITE);
    }

    private void setActiveButtonAppearance(Button button) {
        button.setScaleX(1.1f);
        button.setScaleY(1.1f);
        ((GradientDrawable)button.getBackground()).setStroke(10, Color.WHITE);
    }

    private void resetAllColorPickerBtn(){
        for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
            resetButtonAppearance(colorPickerButton);
        }
    }
    private void resetAllActiveColorButtons() {
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                resetButtonAppearance(activeColorButton);
                modeTab.setSelectedActiveColorBtn(null);
            }
        }
    }

    private void updateColorPickerButtonState() {
        boolean activeColorButtonSelected = isAnyActiveColorButtonSelected();
        for (Button colorPickerButton : ModeTab.getColorPickerButtons()) {
            colorPickerButton.setEnabled(activeColorButtonSelected);
            if (!activeColorButtonSelected) {
                resetButtonAppearance(colorPickerButton);
            }
        }
    }

    private void initActiveColorButtonsListenrs() {
        View.OnClickListener activeColorButtonListener = v -> {
            toggleActiveColorButton((Button) v);
        };

        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                activeColorButton.setOnClickListener(activeColorButtonListener);
            }
        }
    }

    private void initColorPickerButtonsListenrs() {
        for (Button colorPickerButton : colorPickerButtons) {
            colorPickerButton.setOnClickListener(this::onColorPickerButtonClick);
        }
    }

    public void resetAll(){
        resetAllActiveColorButtons();
        resetAllColorPickerBtn();
    }
}