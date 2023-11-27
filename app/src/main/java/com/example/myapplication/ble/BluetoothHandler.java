package com.example.myapplication.ble;

import static com.welie.blessed.BluetoothBytesParser.asHexString;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.myapplication.MainActivity;
import com.example.myapplication.ble.callback.BluetoothConnectionCallback;
import com.example.myapplication.ble.callback.BluetoothScanCallback;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.ConnectionState;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.PhyOptions;
import com.welie.blessed.PhyType;
import com.welie.blessed.ScanFailure;
import com.welie.blessed.WriteType;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothHandler {

    // Intent constants
    public static final String LAMP_STATE_UPDATE_ACTION = "com.example.myapplication.LAMP_STATE_UPDATE";
    public static final String EXTRA_LAMP_STATE = "EXTRA_LAMP_STATE";
    public static final String DISCONNECT_LAMP_STATE_UPDATE_ACTION = "com.example.myapplication.DISCONNECT_LAMP_STATE_UPDATE_ACTION";
    public static final String CONNECT_LAMP_STATE_UPDATE_ACTION = "com.example.myapplication.CONNECT_LAMP_STATE_UPDATE_ACTION";
    public static final String BRIGHTNESS_UPDATE_ACTION = "com.example.myapplication.BRIGHTNESS_UPDATE_ACTION";
    public static final String EXTRA_BRIGHTNESS = "EXTRA_BRIGHTNESS";
    public static final String MODE_UPDATE_ACTION = "com.example.myapplication.MODE_UPDATE_ACTION";
    public static final String EXTRA_MODE = "EXTRA_MODE";

    public static final String COLOR_DATA_UPDATE_ACTION = "com.example.myapplication.COLOR_DATA_UPDATE_ACTION";
    public static final String EXTRA_COLOR_DATA = "EXTRA_COLOR_DATA";


    public static final UUID LC_SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    public static final UUID LAMP_SWITCH_CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    public static final UUID LAMP_BRIGHTNESS_CHARACTERISTIC_UUID = UUID.fromString("c8caddf7-38c7-4b6e-891e-5550e4776a79");
    public static final UUID LAMP_MODE_CHARACTERISTIC_UUID = UUID.fromString("1f9df20a-949d-4278-8051-72a01b6a7ca3");
    public static final UUID LAMP_COLOR_CHARACTERISTIC_UUID = UUID.fromString("6421350a-fe82-480f-af3f-5c24925ec0ae");

    // Local variables
    private static final long SCAN_PERIOD = 10000; // 10 seconds
    public BluetoothCentralManager central;
    private static BluetoothHandler instance = null;
    private  Context context;
    private final Handler handler = new Handler();
    public Map<String, BluetoothPeripheral> discoveredPeripherals = new HashMap<>();

    private BluetoothPeripheral peripheral = null;

    public void disconnectPeripheral(BluetoothPeripheral selectedPeripheral) {
        central.cancelConnection(selectedPeripheral);
    }

    private BluetoothConnectionCallback connectionCallback;

    public void setBluetoothConnectionCallback(BluetoothConnectionCallback callback) {
        this.connectionCallback = callback;
    }

    private BluetoothScanCallback scanCallback;

    public BluetoothPeripheral getDiscoveredPeripheral(String address) {
        return discoveredPeripherals.get(address);
    }

    public List<BluetoothPeripheral> getConnectedDevices() {
        return central.getConnectedPeripherals();
    }

    public void connectPeripheral(BluetoothPeripheral peripheral) {
        if (isAnyPeripheralConnected()) {
            disconnectPeripheral(this.peripheral);
            Log.d("BluetoothHandler", "A device is already connected. Ignoring the new connection request.");
            return;
        }
        central.connectPeripheral(peripheral, peripheralCallback);
    }

    private boolean isAnyPeripheralConnected() {
        return this.peripheral != null && this.peripheral.getState() == ConnectionState.CONNECTED;
    }

    public void setBluetoothScanCallback(BluetoothScanCallback callback) {
        this.scanCallback = callback;
    }
    private void sendLampStateUpdateBroadcast(String lampState) {
        Intent intent = new Intent(LAMP_STATE_UPDATE_ACTION);
        intent.putExtra(EXTRA_LAMP_STATE, lampState);
        context.sendBroadcast(intent);
    }

    private void sendConnectLampStateUpdateBroadcast() {
        Intent intent = new Intent(CONNECT_LAMP_STATE_UPDATE_ACTION);
        context.sendBroadcast(intent);
    }
    private void sendDisconnectLampStateUpdateBroadcast() {
        Intent intent = new Intent(DISCONNECT_LAMP_STATE_UPDATE_ACTION);
        context.sendBroadcast(intent);
    }

    private void sendBrightnessUpdateBroadcast(String brightness) {
        Intent intent = new Intent(BRIGHTNESS_UPDATE_ACTION);
        intent.putExtra(EXTRA_BRIGHTNESS, brightness);
        context.sendBroadcast(intent);
    }

    private void sendModeUpdateBroadcast(String modeStr) {
        Intent intent = new Intent(MODE_UPDATE_ACTION);
        intent.putExtra(EXTRA_MODE, modeStr);
        context.sendBroadcast(intent);
    }

    public BluetoothGattCharacteristic getCharacteristic(UUID serviceUUID, UUID characteristicUUID) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        if (this.peripheral == null || this.peripheral.getState() != ConnectionState.CONNECTED) {
            throw new BluetoothNotConnectedException("Bluetooth device not connected");
        }

        BluetoothGattCharacteristic characteristic = this.peripheral.getCharacteristic(serviceUUID, characteristicUUID);
        if (characteristic == null) {
            throw new CharacteristicNotFoundException("Characteristic not found");
        }

        return characteristic;
    }

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            // Request a higher MTU, iOS always asks for 185
            peripheral.requestMtu(185);
            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);
            peripheral.setPreferredPhy(PhyType.LE_2M, PhyType.LE_2M, PhyOptions.S2);
            peripheral.readPhy();

            peripheral.setNotify(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID, true);
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                final boolean isNotifying = peripheral.isNotifying(characteristic);
                Log.d("NOTIFICATION",String.format("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid()));
            } else {
                Log.d("NOTIFICATION",String.format("ERROR: Changing notification state failed for %s (%s)", characteristic.getUuid(), status));
            }
        }
        //
        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Log.d("NOTIFICATION",String.format("SUCCESS: Writing <%s> to <%s>", asHexString(value), characteristic.getUuid()));
            } else {
                Log.d("NOTIFICATION",String.format("ERROR: Failed writing <%s> to <%s>", asHexString(value), characteristic.getUuid()));
            }
        }


        @Override
            public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            BluetoothBytesParser parser = new BluetoothBytesParser(value);

            if (characteristicUUID.equals(LAMP_SWITCH_CHARACTERISTIC_UUID)) {
                Log.d("CharacteristicUpdate", new String(value));
                String lampState = new String(value);
                sendLampStateUpdateBroadcast(lampState);
            } else if(characteristicUUID.equals(LAMP_BRIGHTNESS_CHARACTERISTIC_UUID)){
//                Log.d("CharacteristicUpdate", "BRIGHTNESS CHANGED");

                int brightness = parser.getUInt8();
//                Log.d("BLE", "Brightness value: " + brightness);

                String brightnessStr = String.valueOf(brightness);
                sendBrightnessUpdateBroadcast(brightnessStr);
            } else if(characteristicUUID.equals(LAMP_MODE_CHARACTERISTIC_UUID)){
//                Log.d("MODECharacteristicUpdate", new String(value));
                int mode = parser.getUInt8();
                String modeStr = String.valueOf(mode);
                sendModeUpdateBroadcast(modeStr);
            } if (characteristicUUID.equals(LAMP_COLOR_CHARACTERISTIC_UUID)) {
                // Handle the color data as per the format
                Intent intent = new Intent(COLOR_DATA_UPDATE_ACTION);
                intent.putExtra(EXTRA_COLOR_DATA, value); // Send the raw byte array
                context.sendBroadcast(intent);
            }

        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            Log.d("Scan",String.format("new MTU set: %d", mtu));
        }
    };



    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {


        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            peripheral.readCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            //Todo check if this is needed and correct
            synchronized (BluetoothHandler.this) {
                BluetoothHandler.getInstance(context).peripheral = peripheral;
            }

            sendConnectLampStateUpdateBroadcast();

//            if (connectionCallback != null) {
//                connectionCallback.onDeviceConnected();
//            }


            central.stopScan();

            Log.d("Scan",String.format("connected to '%s'", peripheral.getName()));
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            //Todo check if this is needed and correct
            BluetoothHandler.getInstance(context).peripheral = null;
            if (connectionCallback != null) {
                connectionCallback.onDeviceDisconnected();
            }
            Log.d("Scan",String.format("connection '%s' failed with status %s", peripheral.getName(), status));
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            //Todo check if this is needed and correct
            synchronized (BluetoothHandler.this) {
                if (BluetoothHandler.getInstance(context).peripheral != null && BluetoothHandler.getInstance(context).peripheral.equals(peripheral)) {
                    BluetoothHandler.getInstance(context).peripheral = null;
                }
            }

//            if (connectionCallback != null) {
//                connectionCallback.onDeviceDisconnected();
//            }
            sendDisconnectLampStateUpdateBroadcast();
            Log.d("Scan",String.format("disconnected '%s' with status %s", peripheral.getName(), status));

            // Reconnect to this device when it becomes available again
//            handler.postDelayed(() -> central.autoConnectPeripheral(peripheral, peripheralCallback), 5000);
        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
//            Log.d("onDiscoveredPeripheral",String.format("Found peripheral '%s'", peripheral.getName()));

            if (scanCallback != null) {
                String peripheralAddress = peripheral.getAddress();
                String name = peripheral.getName();
                if(!name.trim().isEmpty() && !peripheralAddress.trim().isEmpty() && !discoveredPeripherals.containsKey(peripheralAddress)) {
                    discoveredPeripherals.put(peripheralAddress, peripheral);
                    scanCallback.onDeviceDiscovered(name, peripheralAddress);
                    Log.d("onDiscoveredPeripheral",String.format("Found peripheral '%s' with address '%s'", peripheral.getName(), peripheralAddress));
                }
            }
            // Removed the stopScan() call

            //Todo check if this is needed
//            if (peripheral.getName().contains("Contour") && peripheral.getBondState() == BondState.NONE) {
//                // Create a bond immediately to avoid double pairing popups
//                central.createBond(peripheral, peripheralCallback);
//            } else {
//                central.connectPeripheral(peripheral, peripheralCallback);
//            }
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Log.d("Scan","State changed ");
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
//                central.startPairingPopupHack();
//                startScan();
            } else if (state == BluetoothAdapter.STATE_OFF) {
                if (connectionCallback != null) {
                    connectionCallback.onDeviceDisconnected();
                }
                sendDisconnectLampStateUpdateBroadcast();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            sendDisconnectLampStateUpdateBroadcast();
            Log.d("Scan","scanning failed with error " + scanFailure);
        }
    };

    public static synchronized BluetoothHandler getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothHandler(context.getApplicationContext());
        }
        return instance;
    }

    private BluetoothHandler(Context context) {
        Log.d("Scan","BluetoothHandler constructor");
        this.context = context;
        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler());
//        findDevices();
    }

    public void findDevices() {
        if(central!=null){
            central.startPairingPopupHack();
            startScan();
        }
    }

//    public void startScan() {
//        handler.postDelayed(() -> central.scanForPeripheralsWithServices(new UUID[]{
//                        LC_SERVICE_UUID,
//                }
//        ),1000);
//    }

    public void startScan() {
        handler.postDelayed(() -> {
            central.scanForPeripheralsWithServices(new UUID[]{LC_SERVICE_UUID});
//            central.scanForPeripherals();
            handler.postDelayed(this::stopScanIfNeeded, SCAN_PERIOD); // Stop scanning after a period
        }, 1000);
    }

    private void stopScanIfNeeded() {
        if (central.isScanning()) {
            central.stopScan();
        }
    }

    public void readCharacteristic(UUID serviceUUID, UUID characteristicUUID) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        getCharacteristic(serviceUUID, characteristicUUID);
        peripheral.readCharacteristic(serviceUUID, characteristicUUID);
    }

    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] newValue, WriteType writeType) throws BluetoothNotConnectedException, CharacteristicNotFoundException {
        BluetoothGattCharacteristic characteristic = getCharacteristic(serviceUUID, characteristicUUID);
        peripheral.writeCharacteristic(characteristic, newValue, writeType);
    }
}