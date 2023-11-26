package com.example.myapplication.ble.callback;

public interface BluetoothScanCallback {
        void onDeviceDiscovered(String deviceName, String deviceAddress);
    }