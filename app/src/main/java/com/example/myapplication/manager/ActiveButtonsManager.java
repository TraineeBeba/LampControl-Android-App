package com.example.myapplication.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.example.myapplication.MainActivity;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.model.ModeTab;
import com.example.myapplication.util.BLECommunicationUtil;

import java.util.List;

public class ActiveButtonsManager {

    private final List<ModeTab> modeTabs;
    private final Context context;

    public ActiveButtonsManager(Context context, List<ModeTab> modeTabs) {
        this.context = context;
        this.modeTabs = modeTabs;
        initActiveColorButtonsListeners();
        initColorPickerButtonsListeners();


    }


    // Modify onColorPickerButtonClick method
    private void onColorPickerButtonClick(View v) {
        Log.d("onColorPickerButtonClick", "onColorPickerButtonClick");
        Button clickedButton = (Button) v;
        if (isAnyActiveColorButtonSelected()) {
            // Reset appearance for all color picker buttons
            for (Button colorPickerButton : ModeTab.colorPickerButtons) {
                resetButtonAppearance(colorPickerButton);
            }

            setActiveButtonAppearance(clickedButton);

            for (ModeTab modeTab : modeTabs) {
                Button selectedActiveButton = modeTab.getSelectedActiveColorBtn();
                if (selectedActiveButton != null) {
                    // Extract RGB values
                    Drawable colorPickerBackground = clickedButton.getBackground();
                    if (colorPickerBackground instanceof GradientDrawable) {
                        int color = ((GradientDrawable) colorPickerBackground).getColor().getDefaultColor();
                        int red = Color.red(color);
                        int green = Color.green(color);
                        int blue = Color.blue(color);

                        // Find active color button index
                        int activeButtonIndex = modeTab.getActiveColorButtons().indexOf(selectedActiveButton);
                        int modeNumber = ModeTab.currentMode.getModeNumber();

                        try {
                            byte[] data = new byte[5];
                            data[0] = (byte) modeNumber; // Mode
                            data[1] = (byte) activeButtonIndex; // Active color index
                            data[2] = (byte) red; // Red component
                            data[3] = (byte) green; // Green component
                            data[4] = (byte) blue; // Blue component

                            MainActivity.getBleCommunicationUtil().writeColor(data);

                        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
                            Log.d("Color", e.getMessage());
                        }
                        ((GradientDrawable) selectedActiveButton.getBackground()).setColor(color);
                        saveActiveColorToPreferences(selectedActiveButton.getId(), color);
                    }
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
                for (Button colorPickerButton : ModeTab.colorPickerButtons) {
                    resetButtonAppearance(colorPickerButton);
                }
            }
            updateColorPickerButtonState();
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
        for (Button colorPickerButton : ModeTab.colorPickerButtons) {
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
        for (Button colorPickerButton : ModeTab.colorPickerButtons) {
            colorPickerButton.setEnabled(activeColorButtonSelected);
            if (!activeColorButtonSelected) {
                resetButtonAppearance(colorPickerButton);
            }
        }
    }

    private void initActiveColorButtonsListeners() {
        View.OnClickListener activeColorButtonListener = v -> {
            toggleActiveColorButton((Button) v);
        };

        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                activeColorButton.setOnClickListener(activeColorButtonListener);

                // Apply saved color or default
                Drawable background = activeColorButton.getBackground();
                int defaultColor = getDefaultColorFromDrawable(background); // Define your default color
                int savedColor = sharedPref.getInt("activeColor" + activeColorButton.getId(), defaultColor);
                if (background instanceof GradientDrawable) {
                    ((GradientDrawable) background).setColor(savedColor);
                }
            }
        }
    }


    private void initColorPickerButtonsListeners() {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        for (Button colorPickerButton : ModeTab.colorPickerButtons) {
            colorPickerButton.setOnClickListener(this::onColorPickerButtonClick);

            Drawable background = colorPickerButton.getBackground();
            int defaultColor = getDefaultColorFromDrawable(background); // Custom method to get color

            int savedColor = sharedPref.getInt("pickerColor" + colorPickerButton.getId(), defaultColor);
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(savedColor);
            }
        }
    }

    private int getDefaultColorFromDrawable(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            return ((GradientDrawable) drawable).getColor().getDefaultColor();
        }
        return Color.WHITE;
    }


    public void resetAll(){
        resetAllActiveColorButtons();
        resetAllColorPickerBtn();
    }

    public void updateColorPickerButtonColorAtIndex(int index, int color) {
        if (index >= 0 && index < ModeTab.colorPickerButtons.size()) {
            Log.d("UPDATE", this.toString());
            Button colorPickerButton = ModeTab.colorPickerButtons.get(index);
//            Log.d("BUTTON", String.valueOf(colorPickerButton.getId()));
            ((GradientDrawable) colorPickerButton.getBackground()).setColor(color);
            savePickerColorToPreferences(colorPickerButton.getId(), color);
        }
    }

    private void saveActiveColorToPreferences(int buttonId, int color) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("activeColor" + buttonId, color);
        editor.apply();
    }

    private void savePickerColorToPreferences(int buttonId, int color) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("pickerColor" + buttonId, color);
        editor.apply();
    }

    public void setAllActiveColorButtonsEnabled(boolean isEnabled) {
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getActiveColorButtons()) {
                activeColorButton.setEnabled(isEnabled);

                // Reset appearance if disabling
                if (!isEnabled) {
                    resetButtonAppearance(activeColorButton);

                }
            }
        }
    }
}