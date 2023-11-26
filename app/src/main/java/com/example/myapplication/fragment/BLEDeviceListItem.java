package com.example.myapplication.fragment;

public class BLEDeviceListItem {
    private String name;
    private String address; // This could be age, breed, etc.
    private boolean isConnected = false;

    public BLEDeviceListItem(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setViewConnected(boolean connected) {
        isConnected = connected;
    }
}
