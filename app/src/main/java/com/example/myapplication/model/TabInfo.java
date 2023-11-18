package com.example.myapplication.model;

import android.widget.LinearLayout;

public class TabInfo {
    private LinearLayout tabLayout;
    private int drawableResId;

    public TabInfo(LinearLayout tabLayout, int drawableResId) {
        this.tabLayout = tabLayout;
        this.drawableResId = drawableResId;
    }

    public LinearLayout getTabLayout() {
        return tabLayout;
    }

    public void setTabLayout(LinearLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
    }
}
