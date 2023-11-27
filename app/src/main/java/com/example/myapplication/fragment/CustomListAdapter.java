package com.example.myapplication.fragment;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<BLEDeviceListItem> {
    
    public CustomListAdapter(Context context, List<BLEDeviceListItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_item_1, parent, false);
        }

        BLEDeviceListItem item = getItem(position);
        if(item == null) {
            return convertView;
        }

        TextView mainField = convertView.findViewById(R.id.mainField);
        TextView additionalField = convertView.findViewById(R.id.additionalField);
        TextView statusField = convertView.findViewById(R.id.statusField); // New TextView

        mainField.setText(item.getName());
        additionalField.setText(item.getAddress());

        if (item.isConnected()) {
            Log.d("CUSTOM_LIST_ADAPTER", "CONNECTED");
            convertView.setBackgroundColor(Color.rgb(68, 72, 84));
            statusField.setText("Підключено"); // Set text when connected
        } else {
            Log.d("CUSTOM_LIST_ADAPTER", "NOT CONNECTED");
            convertView.setBackgroundColor(Color.TRANSPARENT);
            statusField.setText(""); // Clear text when not connected
        }
//
//        TextView mainField = convertView.findViewById(R.id.mainField);
//        TextView additionalField = convertView.findViewById(R.id.additionalField);

        mainField.setText(item.getName());
        additionalField.setText(item.getAddress());

        return convertView;
    }
}
