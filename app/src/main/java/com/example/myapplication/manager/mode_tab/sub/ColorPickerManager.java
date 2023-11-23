package com.example.myapplication.manager.mode_tab.sub;

import static com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil.resetButtonAppearance;
import static com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil.setActiveButtonAppearance;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.MainActivity;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.manager.mode_tab.TabManager;
import com.example.myapplication.manager.mode_tab.model.ModeTab;
import com.example.myapplication.manager.mode_tab.sub.util.ButtonAppearanceUtil;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerManager {
    public static final int COLOR_PICKER_BUTTONS_COUNT = 15;
    private final List<ModeTab> modeTabs;
    private static List<Button> colorPickerButtons;
    private Context context;

    public ColorPickerManager(Context context, View view, List<ModeTab> modeTabs) {
        this.modeTabs = modeTabs;
        this.context = context;
        initColorPickerButtons(view);
        initColorPickerButtonsListeners(context);
    }

    private void initColorPickerButtons(View view) {
    colorPickerButtons = new ArrayList<>();
        for (int i = 1; i <= COLOR_PICKER_BUTTONS_COUNT; i++) {
            int buttonId = view.getResources().getIdentifier("colorBtn" + i + "_1", "id", view.getContext().getPackageName());
            Button button = view.findViewById(buttonId);
            if (button != null) {
                colorPickerButtons.add(button);
            }
        }
    }

    private void initColorPickerButtonsListeners(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        for (Button colorPickerButton : colorPickerButtons) {
            colorPickerButton.setOnClickListener(this::onColorPickerButtonClick);

            Drawable background = colorPickerButton.getBackground();
            int defaultColor = ButtonAppearanceUtil.getDefaultColorFromDrawable(background); // Custom method to get color

            int savedColor = sharedPref.getInt("pickerColor" + colorPickerButton.getId(), defaultColor);
            if (background instanceof GradientDrawable) {
                ((GradientDrawable) background).setColor(savedColor);
            }
        }
    }

    private void onColorPickerButtonClick(View v) {
        Log.d("onColorPickerButtonClick", "onColorPickerButtonClick");
        Button clickedButton = (Button) v;
        if (TabManager.isAnyActiveColorButtonSelected()) {
            // Reset appearance for all color picker buttons
            for (Button colorPickerButton : colorPickerButtons) {
                resetButtonAppearance(colorPickerButton);
            }

            setActiveButtonAppearance(clickedButton);

            Button selectedActiveButton = TabManager.getSelectedActiveColorBtn();
            if (selectedActiveButton != null) {
                // Extract RGB values
                Drawable colorPickerBackground = clickedButton.getBackground();
                if (colorPickerBackground instanceof GradientDrawable) {
                    int color = ((GradientDrawable) colorPickerBackground).getColor().getDefaultColor();
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);

//                     Find active color button index
                    ModeTab modeTab = findTabByActiveBtn(selectedActiveButton);
                    if (modeTab != null){
                        List<Button> activeColorButtons = modeTab.getTabActiveButtonsManager().getActiveColorButtons();
                        int activeButtonIndex = activeColorButtons.indexOf(selectedActiveButton);
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
                        modeTab.getTabActiveButtonsManager().saveActiveColorToPreferences(selectedActiveButton.getId(), color);
                    }
                }
            }

        }
    }

    private ModeTab findTabByActiveBtn(Button selectedActiveButton) {
        for (ModeTab modeTab : modeTabs) {
            for (Button activeColorButton : modeTab.getTabActiveButtonsManager().getActiveColorButtons()) {
                if(activeColorButton == selectedActiveButton) return modeTab;
            }
        }
        return null;
    }

    public void updateColorPickerButtonColorAtIndex(int index, int color) {
        if (index >= 0 && index < colorPickerButtons.size()) {
            Log.d("UPDATE", this.toString());
            Button colorPickerButton = colorPickerButtons.get(index);
//            Log.d("BUTTON", String.valueOf(colorPickerButton.getId()));
            ((GradientDrawable) colorPickerButton.getBackground()).setColor(color);
            savePickerColorToPreferences(colorPickerButton.getId(), color);
        }
    }

    private void savePickerColorToPreferences(int buttonId, int color) {
        SharedPreferences sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("pickerColor" + buttonId, color);
        editor.apply();
    }

    public static List<Button> getColorPickerButtons() {
        return colorPickerButtons;
    }

    public void resetAll() {
        for (Button colorPickerButton : colorPickerButtons) {
            ButtonAppearanceUtil.resetButtonAppearance(colorPickerButton);
        }
    }
}
