package com.example.myapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.constant.FragmentType;


public class WifiFragment extends Fragment {
    private Button buttonHome;
    private Button buttonLight;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connection, container, false);

        buttonHome = view.findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToFragment(R.id.connectionLayout, FragmentType.HOME);
                }
            }
        });

        buttonLight = view.findViewById(R.id.button_light);
        buttonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToFragment(R.id.connectionLayout, FragmentType.LIGHT);
                }
            }
        });

        return view;
    }

}