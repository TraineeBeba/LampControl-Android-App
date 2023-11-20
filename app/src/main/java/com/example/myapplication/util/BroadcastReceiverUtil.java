package com.example.myapplication.util;

import static com.example.myapplication.util.BrightnessModeUtil.getSeekBarPosition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.Mode;

import java.util.HashMap;
import java.util.List;

public class BroadcastReceiverUtil {
    private Context context;
    private Callback callback;
    private BroadcastReceiver brightnessReceiver;
    private BroadcastReceiver modeReceiver;
    private BroadcastReceiver lampStateReceiver;
    private BroadcastReceiver disconnectReceiver;

    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;

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

        default  void onColorsUpdate(HashMap<Integer, HashMap<Integer, List<Integer>>> colorData) {
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
                    Log.d("Brightness", "Brightness: " + brightness);
                    callback.onBrightnessUpdate(brightness);

                    LampCache.setBrightness(brightness);
                    LampCache.setSeekBarPos(getSeekBarPosition(brightness));

                }
            }
        };

        modeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothHandler.MODE_UPDATE_ACTION)) {
                    String modeStr = intent.getStringExtra(BluetoothHandler.EXTRA_MODE);
                    int mode = Integer.valueOf(modeStr);
                    Log.d("Mode", "Mode: " + Mode.fromModeNumber(mode));
                    callback.onModeUpdate(mode);

                }
            }
        };

        lampStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                debounceHandler.removeCallbacks(debounceRunnable);
                debounceRunnable = () -> {
                    String lampStateStr = intent.getStringExtra(BluetoothHandler.EXTRA_LAMP_STATE);
                    Lamp lampState = Lamp.valueOf(lampStateStr);
                    LampCache.setIsOn(lampState);

                    Log.d("Class", this.toString());
                    Log.d("GET", "State: " + lampState.name());
                    callback.onLampStateUpdate(lampState);
                };
                debounceHandler.postDelayed(debounceRunnable, 100); // Adjust delay as needed
            }
        };

        disconnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION)) {
                    Log.d("Disconnect", "Disconnect: ");
                    callback.onDisconnect();
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
