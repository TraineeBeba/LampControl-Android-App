package com.example.myapplication.util;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_BRIGHTNESS_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_COLOR_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_MODE_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.welie.blessed.WriteType;

public class BLECommunicationUtil {
    private BluetoothHandler bluetoothHandler;
    private Context context;

    public BLECommunicationUtil(Context context) {
        this.context = context;
        this.bluetoothHandler = BluetoothHandler.getInstance(context);
    }

    public void readLampState() throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        Log.d("READ LAMP STATE", "READ LAMP STATE");
        bluetoothHandler.readCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
    }

    public void writeLampState(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
    }

    public void readMode() throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        bluetoothHandler.readCharacteristic(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID);
    }

    public void writeMode(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
    }

    public void writeBrightness(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
    }

    public void readBrightness() throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        bluetoothHandler.readCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID);
    }

    public void writeColor(byte[] newValue) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        bluetoothHandler.writeCharacteristic(LC_SERVICE_UUID, LAMP_COLOR_CHARACTERISTIC_UUID, newValue, WriteType.WITH_RESPONSE);
    }

}
