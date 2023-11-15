package com.example.myapplication.ble;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.BondState;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.PhyOptions;
import com.welie.blessed.PhyType;
import com.welie.blessed.ScanFailure;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BluetoothHandler {

    // Intent constants
    public static final String LAMP_STATE_UPDATE_ACTION = "com.example.myapplication.LAMP_STATE_UPDATE";
    public static final String DISCONNECT_LAMP_STATE_UPDATE_ACTION = "com.example.myapplication.DISCONNECT_LAMP_STATE_UPDATE_ACTION";
    public static final String EXTRA_LAMP_STATE = "EXTRA_LAMP_STATE";

    public static final String MEASUREMENT_EXTRA_PERIPHERAL = "blessed.measurement.peripheral";

    public static final UUID LC_SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    public static final UUID LAMP_SWITCH_CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    public static final UUID LAMP_BRIGHTNESS_CHARACTERISTIC_UUID = UUID.fromString("c8caddf7-38c7-4b6e-891e-5550e4776a79");
    public static final UUID LAMP_MODE_CHARACTERISTIC_UUID = UUID.fromString("1f9df20a-949d-4278-8051-72a01b6a7ca3");

    // Contour Glucose Service
    public static final UUID CONTOUR_SERVICE_UUID = UUID.fromString("00000000-0002-11E2-9E96-0800200C9A66");
    private static final UUID CONTOUR_CLOCK = UUID.fromString("00001026-0002-11E2-9E96-0800200C9A66");

    // Local variables
    public BluetoothCentralManager central;
    private static BluetoothHandler instance = null;
    private  Context context;
    private final Handler handler = new Handler();
    private int currentTimeCounter = 0;

    public static String address;

    private void sendLampStateUpdateBroadcast(String lampState) {
        Intent intent = new Intent(LAMP_STATE_UPDATE_ACTION);
        intent.putExtra(EXTRA_LAMP_STATE, lampState);
        context.sendBroadcast(intent);
    }

    private void sendDisconnectLampStateUpdateBroadcast() {
        Intent intent = new Intent(DISCONNECT_LAMP_STATE_UPDATE_ACTION);
        context.sendBroadcast(intent);
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

            // Read manufacturer and model number from the Device Information Service
//            peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID);
//            peripheral.readCharacteristic(DIS_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID);

            peripheral.readPhy();

//            // Turn on notifications for Current Time Service and write it if possible
//            BluetoothGattCharacteristic currentTimeCharacteristic = peripheral.getCharacteristic(CTS_SERVICE_UUID, CURRENT_TIME_CHARACTERISTIC_UUID);
//            if (currentTimeCharacteristic != null) {
//                peripheral.setNotify(currentTimeCharacteristic, true);
//
//                // If it has the write property we write the current time
//                if ((currentTimeCharacteristic.getProperties() & PROPERTY_WRITE) > 0) {
//                    // Write the current time unless it is an Omron device
//                    if (!isOmronBPM(peripheral.getName())) {
//                        BluetoothBytesParser parser = new BluetoothBytesParser();
//                        parser.setCurrentTime(Calendar.getInstance());
//                        peripheral.writeCharacteristic(currentTimeCharacteristic, parser.getValue(), WriteType.WITH_RESPONSE);
//                    }
//                }
//            }
//
//            // Try to turn on notifications for other characteristics
//            peripheral.readCharacteristic(BTS_SERVICE_UUID, BATTERY_LEVEL_CHARACTERISTIC_UUID);




            peripheral.setNotify(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID, true);
            BluetoothGattCharacteristic characteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            Log.e(TAG, "CHARACTERISTIc: "+ characteristic.getUuid().toString());
            for (BluetoothGattDescriptor descriptor:characteristic.getDescriptors()){
                Log.e(TAG, "BluetoothGattDescriptor: "+descriptor.getUuid().toString());
            }

            characteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID);
            Log.e(TAG, "CHARACTERISTIc: "+ characteristic.getUuid().toString());
            for (BluetoothGattDescriptor descriptor:characteristic.getDescriptors()){
                Log.e(TAG, "BluetoothGattDescriptor: "+descriptor.getUuid().toString());
            }
            characteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID);
            Log.e(TAG, "CHARACTERISTIc: "+ characteristic.getUuid().toString());
            for (BluetoothGattDescriptor descriptor:characteristic.getDescriptors()){
                Log.e(TAG, "BluetoothGattDescriptor: "+descriptor.getUuid().toString());
            }
            peripheral.setNotify(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(HTS_SERVICE_UUID, TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(HRS_SERVICE_UUID, HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(PLX_SERVICE_UUID, PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID, true);
//            peripheral.setNotify(PLX_SERVICE_UUID, PLX_SPOT_MEASUREMENT_CHAR_UUID, true);
//            peripheral.setNotify(WSS_SERVICE_UUID, WSS_MEASUREMENT_CHAR_UUID, true);
//            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, true);
//            peripheral.setNotify(CONTOUR_SERVICE_UUID, CONTOUR_CLOCK, true);
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                final boolean isNotifying = peripheral.isNotifying(characteristic);
                Log.d("NOTIFICATION",String.format("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid()));
//                if (characteristic.getUuid().equals(LAMP_SWITCH_CHARACTERISTIC_UUID)) {
////                    Log.d("NOTIFICATION",String.format("LAMP STATE CHANGED"));
//                }
//                else if (characteristic.getUuid().equals(GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID)) {
//                    writeGetAllGlucoseMeasurements(peripheral);
//                }
            } else {
                Log.d("NOTIFICATION",String.format("ERROR: Changing notification state failed for %s (%s)", characteristic.getUuid(), status));
            }
        }
        //
        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Log.d("NOTIFICATION",String.format("SUCCESS: Writing <%s> to <%s>", new String(value), characteristic.getUuid()));
            } else {
                Log.d("NOTIFICATION",String.format("ERROR: Failed writing <%s> to <%s>", new String(value), characteristic.getUuid()));
            }
        }


        @Override
            public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            BluetoothBytesParser parser = new BluetoothBytesParser(value);

            if (characteristicUUID.equals(LAMP_SWITCH_CHARACTERISTIC_UUID)) {
                Log.d("NOTIFICATION",String.format("LAMP STATE CHANGED"));
                Log.d("NOTIFICATION", new String(value));
                String lampState = new String(value);
                sendLampStateUpdateBroadcast(lampState);
            } else if(characteristicUUID.equals(LAMP_BRIGHTNESS_CHARACTERISTIC_UUID)){
                Log.d("NOTIFICATION",String.format("BRIGHTNESS CHANGED"));
                Log.d("NOTIFICATION", new String(value));
            } else if(characteristicUUID.equals(LAMP_MODE_CHARACTERISTIC_UUID)){
                Log.d("NOTIFICATION",String.format("MODE CHANGED"));
                Log.d("NOTIFICATION", new String(value));
            }
//                BloodPressureMeasurement measurement = new BloodPressureMeasurement(value);
//                Intent intent = new Intent(MEASUREMENT_BLOODPRESSURE);
//                intent.putExtra(MEASUREMENT_BLOODPRESSURE_EXTRA, measurement);
//                sendMeasurement(intent, peripheral);
//                Log.d("Scan",String.format("%s", measurement));
//            } else if (characteristicUUID.equals(LAMP_BRIGHTNESS_CHARACTERISTIC_UUID)) {
//                TemperatureMeasurement measurement = new TemperatureMeasurement(value);
//                Intent intent = new Intent(MEASUREMENT_TEMPERATURE);
//                intent.putExtra(MEASUREMENT_TEMPERATURE_EXTRA, measurement);
//                sendMeasurement(intent, peripheral);
//                Log.d("Scan",String.format("%s", measurement));
//            } else if (characteristicUUID.equals(LAMP_MODE_CHARACTERISTIC_UUID)) {
//                TemperatureMeasurement measurement = new TemperatureMeasurement(value);
//                Intent intent = new Intent(MEASUREMENT_TEMPERATURE);
//                intent.putExtra(MEASUREMENT_TEMPERATURE_EXTRA, measurement);
//                sendMeasurement(intent, peripheral);
//                Log.d("Scan",String.format("%s", measurement));
//
//            }
        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            Log.d("Scan",String.format("new MTU set: %d", mtu));
        }
        //
        private void sendMeasurement(@NotNull Intent intent, @NotNull BluetoothPeripheral peripheral ) {
            intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, peripheral.getAddress());
            intent.setPackage(context.getPackageName());
            context.sendBroadcast(intent);
        }
//
//        private void writeContourClock(@NotNull BluetoothPeripheral peripheral) {
//            Calendar calendar = Calendar.getInstance();
//            int offsetInMinutes = calendar.getTimeZone().getRawOffset() / 60000;
//            int dstSavingsInMinutes = calendar.getTimeZone().getDSTSavings() / 60000;
//            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
//            BluetoothBytesParser parser = new BluetoothBytesParser(ByteOrder.LITTLE_ENDIAN);
//            parser.setIntValue(1, FORMAT_UINT8);
//            parser.setIntValue(calendar.get(Calendar.YEAR), FORMAT_UINT16);
//            parser.setIntValue(calendar.get(Calendar.MONTH) + 1, FORMAT_UINT8);
//            parser.setIntValue(calendar.get(Calendar.DAY_OF_MONTH), FORMAT_UINT8);
//            parser.setIntValue(calendar.get(Calendar.HOUR_OF_DAY), FORMAT_UINT8);
//            parser.setIntValue(calendar.get(Calendar.MINUTE), FORMAT_UINT8);
//            parser.setIntValue(calendar.get(Calendar.SECOND), FORMAT_UINT8);
//            parser.setIntValue(offsetInMinutes + dstSavingsInMinutes, FORMAT_SINT16);
//            peripheral.writeCharacteristic(CONTOUR_SERVICE_UUID, CONTOUR_CLOCK, parser.getValue(), WriteType.WITH_RESPONSE);
//        }
//
//        private void writeGetAllGlucoseMeasurements(@NotNull BluetoothPeripheral peripheral) {
//            byte OP_CODE_REPORT_STORED_RECORDS = 1;
//            byte OPERATOR_ALL_RECORDS = 1;
//            final byte[] command = new byte[] {OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS};
//            peripheral.writeCharacteristic(GLUCOSE_SERVICE_UUID, GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, command, WriteType.WITH_RESPONSE);
//        }
    };

    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {


        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            address = peripheral.getAddress();

            peripheral.setNotify(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID, true);

            peripheral.readCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            peripheral.readCharacteristic(LC_SERVICE_UUID, LAMP_BRIGHTNESS_CHARACTERISTIC_UUID);
            peripheral.readCharacteristic(LC_SERVICE_UUID, LAMP_MODE_CHARACTERISTIC_UUID);

            Log.d("Scan",String.format("connected to '%s'", peripheral.getName()));
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            address = null;
            Log.d("Scan",String.format("connection '%s' failed with status %s", peripheral.getName(), status));
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            sendDisconnectLampStateUpdateBroadcast();
            address = null;
            Log.d("Scan",String.format("disconnected '%s' with status %s", peripheral.getName(), status));
            // Reconnect to this device when it becomes available again
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    central.autoConnectPeripheral(peripheral, peripheralCallback);
                }
            }, 5000);
        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            Log.d("Scan","FOUND peripheral ");
            central.stopScan();

            if (peripheral.getName().contains("Contour") && peripheral.getBondState() == BondState.NONE) {
                // Create a bond immediately to avoid double pairing popups
                central.createBond(peripheral, peripheralCallback);
            } else {
                Log.d("Scan","CONNECT");
                central.connectPeripheral(peripheral, peripheralCallback);
            }
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Log.d("Scan","State changed ");
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                central.startPairingPopupHack();
                startScan();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            Log.d("Scan","scanning failed with error " + scanFailure.toString());
        }
    };

    public static synchronized BluetoothHandler getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothHandler(context.getApplicationContext());
        }
        return instance;
    }

    private BluetoothHandler(Context context) {
        this.context = context;

        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler());

        central.startPairingPopupHack();
        startScan();
    }

    private void startScan() {
        handler.postDelayed(() -> central.scanForPeripheralsWithServices(new UUID[]{
                        LC_SERVICE_UUID,
                }
        ),1000);
    }
    //
    private boolean isOmronBPM(final String name) {
        return name.contains("BLESmart_") || name.contains("BLEsmart_");
    }
}