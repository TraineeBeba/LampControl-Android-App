package com.example.myapplication.util;

import static com.example.myapplication.util.BrightnessModeUtil.getSeekBarPosition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.Mode;

public class BroadcastReceiverUtil {
    private Context context;
    private Callback callback;
    private BroadcastReceiver brightnessReceiver;
    private BroadcastReceiver modeReceiver;
    private BroadcastReceiver lampStateReceiver;
    private BroadcastReceiver disconnectReceiver;

    public interface Callback {
        default  void onLampStateUpdate(Lamp lampState) {
            // Default implementation (empty)
        }

        default  void onBrightnessUpdate(int brightness) {
            // Default implementation (empty)
        }

        default  void onModeUpdate(int mode) {
            // Default implementation (empty)
        }

        default  void onDisconnect() {
            // Default implementation (empty)
        }
    }

    public BroadcastReceiverUtil(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
        initReceivers();
    }

    private void initReceivers() {
        brightnessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION)) {
                    String brightnessStr = intent.getStringExtra(BluetoothHandler.EXTRA_BRIGHTNESS);
                    int brightness = Integer.valueOf(brightnessStr);
                    callback.onBrightnessUpdate(brightness);

                    LampCache.setBrightness(brightness);
                    LampCache.setSeekBarPos(getSeekBarPosition(brightness));

                    Log.d("GET", "Brightness: " + brightness);
                }
            }
        };

        modeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothHandler.MODE_UPDATE_ACTION)) {
                    String modeStr = intent.getStringExtra(BluetoothHandler.EXTRA_MODE);
                    int mode = Integer.valueOf(modeStr);
                    callback.onModeUpdate(mode);

                    Log.d("GET", "Mode: " + Mode.fromModeNumber(mode));
                }
            }
        };

        lampStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothHandler.LAMP_STATE_UPDATE_ACTION)) {
                    String lampStateStr = intent.getStringExtra(BluetoothHandler.EXTRA_LAMP_STATE);
                    Lamp lampState = Lamp.valueOf(lampStateStr);
                    LampCache.setIsOn(lampState);

                    callback.onLampStateUpdate(lampState);

                    Log.d("GET", "State: " + lampState.name());
                }
            }
        };

        disconnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION)) {
                    callback.onDisconnect();
                    Log.d("GET", "Disconnect: ");
                }
            }
        };
    }

    public void registerReceivers() {
        IntentFilter brightnessFilter = new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION);
        context.registerReceiver(brightnessReceiver, brightnessFilter);

        IntentFilter modeFilter = new IntentFilter(BluetoothHandler.MODE_UPDATE_ACTION);
        context.registerReceiver(modeReceiver, modeFilter);

        IntentFilter lampStateFilter = new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION);
        context.registerReceiver(lampStateReceiver, lampStateFilter);

        IntentFilter disconnectFilter = new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION);
        context.registerReceiver(disconnectReceiver, disconnectFilter);
    }

    public void unregisterReceivers() {
        context.unregisterReceiver(brightnessReceiver);
        context.unregisterReceiver(modeReceiver);
        context.unregisterReceiver(lampStateReceiver);
        context.unregisterReceiver(disconnectReceiver);
    }
}
