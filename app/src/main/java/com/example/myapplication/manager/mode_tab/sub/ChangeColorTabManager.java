package com.example.myapplication.manager.mode_tab.sub;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;
import com.github.mata1.simpledroidcolorpicker.pickers.CircleColorPicker;

import java.util.ArrayList;
import java.util.List;

public class ChangeColorTabManager {
    private final List<Button> colorButtons = new ArrayList<>();
    private final Context context;
    private final SharedPreferences sharedPref;
    private final CircleColorPicker circleColorPicker;
    private final ColorPickerManager colorPickerManager;
    private Button selectedButton = null;

    public ChangeColorTabManager(Context context, View view, ColorPickerManager colorPickerManager) {
        this.context = context;
        this.colorPickerManager = colorPickerManager;
        this.sharedPref = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        this.circleColorPicker = view.findViewById(R.id.rcp1);
        initColorButtons(view);
        setupColorPickerListener();
    }

    private void initColorButtons(View parentView) {
        for (int i = 5; i <= 15; i++) {
            int colorButtonId = parentView.getResources().getIdentifier("colorBtn" + i + "_2", "id", parentView.getContext().getPackageName());
            Button colorButton = parentView.findViewById(colorButtonId);
            if (colorButton != null) {
                colorButtons.add(colorButton);
                colorButton.setOnClickListener(this::onColorButtonClick);


                // Calculate the corresponding colorPickerButton ID
                int pickerButtonId = parentView.getResources().getIdentifier("colorBtn" + i + "_1", "id", parentView.getContext().getPackageName());
                Button addColorPicker = parentView.findViewById(pickerButtonId);

                Drawable background = addColorPicker.getBackground();
                int defaultColor = getDefaultColorFromDrawable(background); // Custom method to get color

                // Retrieve the saved color for the corresponding colorPickerButton
                int savedColor = sharedPref.getInt("pickerColor" + pickerButtonId, defaultColor); // Default to white if not found

                // Set the color of the colorButton
                ((GradientDrawable) colorButton.getBackground()).setColor(savedColor);
            }
        }
        deselectAll();
    }

    private int getDefaultColorFromDrawable(Drawable drawable) {
        if (drawable instanceof GradientDrawable) {
            return ((GradientDrawable) drawable).getColor().getDefaultColor();
        }
        return Color.WHITE;
    }

    private void onColorButtonClick(View v) {
        Button clickedButton = (Button) v;

        // Check if the clicked button is already selected
        if (clickedButton.equals(selectedButton)) {
            // Deselect the button
            deselectButton(clickedButton);
            selectedButton = null; // Clear the selection
        } else {
            // If another button is selected, deselect it
            if (selectedButton != null) {
                deselectButton(selectedButton);
            }
            // Select the new button
            selectButton(clickedButton);
            selectedButton = clickedButton;
        }
    }

    private void selectButton(Button button) {
        button.setScaleX(1.1f);
        button.setScaleY(1.1f);
        ((GradientDrawable)button.getBackground()).setStroke(10, Color.WHITE);
    }

    private void deselectButton(Button button) {
        button.setScaleX(1f);
        button.setScaleY(1f);
        ((GradientDrawable)button.getBackground()).setStroke(3, Color.WHITE);
    }

    private void deselectAll(){
        for (Button colorButton : colorButtons) {
            deselectButton(colorButton);
        }
    }

    private void setupColorPickerListener() {
        circleColorPicker.setOnColorChangedListener(color -> {
            if (selectedButton != null) {
                ((GradientDrawable) selectedButton.getBackground()).setColor(color);
                int index = colorButtons.indexOf(selectedButton) + 5; // Shift of 5
                colorPickerManager.updateColorPickerButtonColorAtIndex(index, color);
            }
        });
    }
}
