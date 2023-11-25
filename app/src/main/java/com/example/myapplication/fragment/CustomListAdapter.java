package com.example.myapplication.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<Cat> {
    
    public CustomListAdapter(Context context, List<Cat> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_list_item_1, parent, false);
        }

        Cat item = getItem(position);

        TextView mainField = convertView.findViewById(R.id.mainField);
        TextView additionalField = convertView.findViewById(R.id.additionalField);

        mainField.setText(item.getName());
        additionalField.setText(item.getAdditionalInfo());

        return convertView;
    }
}
