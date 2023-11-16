package com.example.myapplication.constant;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.LightFragment;
import com.example.myapplication.fragment.WifiFragment;

public enum FragmentType {
    HOME, LIGHT, WIFI;
    // Method to get the corresponding fragment instance
    public static Fragment createFragment(FragmentType type) {
        Log.d("FRAGMENT", "Fragment сreated: " + type);
        switch (type) {
            case HOME:
                return new HomeFragment();
            case LIGHT:
                return new LightFragment();
            case WIFI:
                return new WifiFragment();
            default:
                throw new IllegalArgumentException("Unknown Fragment Type");
        }
    }
}


