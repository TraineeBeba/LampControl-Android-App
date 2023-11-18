package com.example.myapplication.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import android.widget.RelativeLayout;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.constant.FragmentType;

import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.content.Context;



public class WifiFragment extends Fragment {
    private Button buttonHome;
    private Button buttonLight;
    private Button search_lamp;
    private ListView listView;
    private RelativeLayout panel1;
    private RelativeLayout panel2;
    private FrameLayout wifilayout;

    private Context context;
    final String[] catNames = new String[] {
            "Рыжик", "Барсик", "Мурзик", "Мурка", "Васька",
            "Томасина", "Кристина", "Пушок", "Дымка", "Кузя",
            "Китти", "Масяня", "Симба"
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connection, container, false);
        wifilayout = view.findViewById(R.id.wifiLayout);
        panel1 = view.findViewById(R.id.panel1);
        panel2 = view.findViewById(R.id.panel2);
        Log.d("asd", "HUY");
        listView = view.findViewById(R.id.listView);
        context = getActivity();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, catNames);
        listView.setAdapter(adapter);

        buttonHome = view.findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("asd", "HUY4");
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToFragment(R.id.wifiLayout, FragmentType.HOME);
                }
            }
        });

        buttonLight = view.findViewById(R.id.button_light);
        buttonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("asd", "HUY3");
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToFragment(R.id.wifiLayout, FragmentType.LIGHT);
                }
            }
        });

        search_lamp = view.findViewById(R.id.search_lamp);
        search_lamp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("asd", "HUY2");
                wifilayout.setBackgroundResource(R.drawable.lamps_near_background);
                panel1.setVisibility(View.INVISIBLE);
                panel2.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

}