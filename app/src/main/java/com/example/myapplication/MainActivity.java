package com.example.myapplication;

import static com.example.myapplication.util.BrightnessModeUtil.getSeekBarPosition;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.view.WindowManager;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.model.ModeColorData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private FragmentBroadcastListener currentFragmentListener;
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(lampStateReceiver, new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION));
        registerReceiver(brightnessReceiver, new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION));
        registerReceiver(modeReceiver, new IntentFilter(BluetoothHandler.MODE_UPDATE_ACTION));
        registerReceiver(disconnectReceiver, new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION));
        registerReceiver(colorDataReceiver, new IntentFilter(BluetoothHandler.COLOR_DATA_UPDATE_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(lampStateReceiver);
        unregisterReceiver(brightnessReceiver);
        unregisterReceiver(modeReceiver);
        unregisterReceiver(disconnectReceiver);
        unregisterReceiver(colorDataReceiver);

    }

    public void setCurrentFragmentListener(FragmentBroadcastListener listener) {
        this.currentFragmentListener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO versions'
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.home);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.homeLayout, new HomeFragment())
                .commit();
    }

    public void navigateToFragment(int layoutId, FragmentType fragmentType) {
        Fragment newFragment = FragmentType.createFragment(fragmentType);
        getSupportFragmentManager().beginTransaction()
                .replace(layoutId, newFragment)
                .commit();
    }


    private final ActivityResultLauncher<String[]> blePermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                for(String key : result.keySet()) {
                        Log.d("ActivityResultLauncher", String.format("%s, %s", key, result));
                }
            });

    private final ActivityResultLauncher<Intent> enableBleRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    onResume();
                }
            });

    @Override
    protected void onResume() {
        super.onResume();

        if (getBluetoothManager().getAdapter() != null) {
            if (!isBluetoothEnabled()) {
                enableBleRequest.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            } else {
                checkPermissions();
            }
        } else {
            Log.d("LOCATION","This device has no Bluetooth hardware");
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        BluetoothHandler.getInstance(getApplicationContext());
    }

    @NotNull
    private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                blePermissionRequest.launch(missingPermissions);
            } else {
                permissionsGranted();
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
        } else return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                initBluetoothHandler();
            }
        } else {
            initBluetoothHandler();
        }
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.d("LOCATION","could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices() {
        if (!areLocationServicesEnabled()) {
            new AlertDialog.Builder(com.example.myapplication.MainActivity.this)
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    private BroadcastReceiver lampStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragmentListener != null) {
                String lampStateStr = intent.getStringExtra(BluetoothHandler.EXTRA_LAMP_STATE);
                Lamp lampState = Lamp.valueOf(lampStateStr);
                LampCache.setIsOn(lampState);
                currentFragmentListener.onLampStateUpdate(lampState);
            }
        }
    };

    private BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION)) {
                String brightnessStr = intent.getStringExtra(BluetoothHandler.EXTRA_BRIGHTNESS);
                int brightness = Integer.valueOf(brightnessStr);
                Log.d("Brightness", "Brightness: " + brightness);
                currentFragmentListener.onBrightnessUpdate(brightness);

                LampCache.setBrightness(brightness);
                LampCache.setSeekBarPos(getSeekBarPosition(brightness));

            }
        }
    };

    private BroadcastReceiver modeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.MODE_UPDATE_ACTION)) {
                String modeStr = intent.getStringExtra(BluetoothHandler.EXTRA_MODE);
                int mode = Integer.valueOf(modeStr);
                Log.d("Mode", "Mode: " + Mode.fromModeNumber(mode));
                currentFragmentListener.onModeUpdate(mode);

            }
        }
    };


    private BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION)) {
                Log.d("Disconnect", "Disconnect: ");
                currentFragmentListener.onDisconnect();
            }
        }
    };

    private BroadcastReceiver colorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothHandler.COLOR_DATA_UPDATE_ACTION)) {
                Log.d("colorDataReceiver", "RECIEVED");
                byte[] rawColorData = intent.getByteArrayExtra(BluetoothHandler.EXTRA_COLOR_DATA);

                List<ModeColorData> colorData = ModeColorData.parseColorData(rawColorData);

                if (currentFragmentListener != null) {
                    currentFragmentListener.onColorDataUpdate(colorData);
                }
            }
        }
    };


}