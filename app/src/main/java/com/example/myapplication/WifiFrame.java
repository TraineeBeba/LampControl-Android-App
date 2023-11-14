package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class WifiFrame extends Fragment {
    private FrameLayout  mainLayoutView;
    private ImageView imageView; // image for button off/on
    private Button buttonHome;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connectionscreen__one, container, false);


//        imageView = view.findViewById(R.id.image_turn);
//        mainLayoutView = view.findViewById(R.id.mainLayout1);
//        Інші ініціалізації віджетів


        // Ініціалізація іншої кнопки
        buttonHome = view.findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showHomeFragment();
                }
            }
        });

        return view;
    }

}