package com.example.myapplication.fragment;

public class Cat {
    private String name;
    private String additionalInfo; // This could be age, breed, etc.
    private boolean isConnected = false;

    public Cat(String name, String additionalInfo) {
        this.name = name;
        this.additionalInfo = additionalInfo;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
