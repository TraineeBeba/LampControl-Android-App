package com.example.myapplication.model;

import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

public class TabInfo {
    private ConstraintLayout tabLayout;
    private int drawableResId;

    public TabInfo(ConstraintLayout tabLayout, int drawableResId) {
        this.tabLayout = tabLayout;
        this.drawableResId = drawableResId;
    }

    public ConstraintLayout getTabLayout() {
        return tabLayout;
    }

    public void setTabLayout(ConstraintLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
    }
}
