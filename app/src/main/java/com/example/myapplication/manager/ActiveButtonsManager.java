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
    private final BLECommunicationUtil bluetoothComm;

    public ActiveButtonsManager(Context context, BLECommunicationUtil bluetoothComm, List<ModeTab> modeTabs) {
        this.context = context;
        this.bluetoothComm = bluetoothComm;
        this.modeTabs = modeTabs;
        initActiveColorButtonsListeners();
        initColorPickerButtonsListeners();
    }

    // Modify onColorPickerButtonClick method
    private void onColorPickerButtonClick(View v) {
        Button clickedButton = (Button) v;
        if (isAnyActiveColorButtonSelected()) {
            // Reset appearance for all color picker buttons
            for (Button colorPickerButton : ModeTab.colorPickerButtons) {
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
                for (Button colorPickerButton : ModeTab.colorPickerButtons) {
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

                // Extract RGB values
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);

                // Log or use the RGB values as needed
                Log.d("ColorPicker", "R: " + red + ", G: " + green + ", B: " + blue);
                ((GradientDrawable) activeColorBackground).setColor(color);
                try {
                    byte[] data = new byte[2 + 1 * 3];
                    data[0] = (byte) 0; // First byte is the mode
                    data[1] = (byte) 1; // Second byte is the number of colors

                    int index = 2; // Start index for color data
//                    for (int color : colors) {
                        data[index] = (byte) ((red >> 16) & 0xff); // Red component
                        data[index + 1] = (byte) ((green >> 8) & 0xff); // Green component
                        data[index + 2] = (byte) (blue & 0xff); // Blue component
                        index += 3;
//                    }

//                        byte[] colorData = new byte[]{(byte) value};
                        bluetoothComm.writeColor(data);

                } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
                    Log.d("Color", e.getMessage());
                }

            }

            saveActiveColorToPreferences(activeColorButton.getId(), color);

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
}