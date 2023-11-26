package com.example.myapplication.constant;

import androidx.fragment.app.Fragment;

import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.LightFragment;
import com.example.myapplication.fragment.BLEFragment;

public enum FragmentType {
    HOME, LIGHT, WIFI;
    public static Fragment createFragment(FragmentType type) {
        switch (type) {
            case HOME:
                return new HomeFragment();
            case LIGHT:
                return new LightFragment();
            case WIFI:
                return new BLEFragment();
            default:
                throw new IllegalArgumentException("Unknown Fragment Type");
        }
    }
}


