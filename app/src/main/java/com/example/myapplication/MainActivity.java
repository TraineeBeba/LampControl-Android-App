package com.example.myapplication;

import static com.example.myapplication.manager.seekbar.util.BrightnessModeUtil.getSeekBarPosition;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;
import android.Manifest;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.listener.FragmentBroadcastListener;
import com.example.myapplication.manager.mode_tab.model.ModeColorData;
import com.example.myapplication.util.BLECommunicationUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private static BLECommunicationUtil bleCommunicationUtil = null;
    //TODO currentFragmentListener correct null check
    private FragmentBroadcastListener currentFragmentListener;

    public static BLECommunicationUtil getBleCommunicationUtil() throws BluetoothNotConnectedException {
        if(bleCommunicationUtil == null) throw new BluetoothNotConnectedException("bleCommunicationUtil is null");
        return bleCommunicationUtil;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        //TODO where to put this?
        super.onStart();
        registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0;
            registerReceiver(lampStateReceiver, new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION), flags);
            registerReceiver(brightnessReceiver, new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION), flags);
            registerReceiver(modeReceiver, new IntentFilter(BluetoothHandler.MODE_UPDATE_ACTION), flags);
            registerReceiver(disconnectReceiver, new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION), flags);
            registerReceiver(colorDataReceiver, new IntentFilter(BluetoothHandler.COLOR_DATA_UPDATE_ACTION), flags);
        } else {
            registerReceiver(lampStateReceiver, new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION));
            registerReceiver(brightnessReceiver, new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION));
            registerReceiver(modeReceiver, new IntentFilter(BluetoothHandler.MODE_UPDATE_ACTION));
            registerReceiver(disconnectReceiver, new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION));
            registerReceiver(colorDataReceiver, new IntentFilter(BluetoothHandler.COLOR_DATA_UPDATE_ACTION));
        }
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
        //TODO version check
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final BroadcastReceiver lampStateReceiver = new BroadcastReceiver() {
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

    private final BroadcastReceiver brightnessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragmentListener != null) {
                if (intent.getAction().equals(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION)) {
                    String brightnessStr = intent.getStringExtra(BluetoothHandler.EXTRA_BRIGHTNESS);
                    int brightness = Integer.parseInt(brightnessStr);
                    Log.d("Brightness", "Brightness: " + brightness);
                    currentFragmentListener.onBrightnessUpdate(brightness);

                    LampCache.setBrightness(brightness);
                    LampCache.setSeekBarPos(getSeekBarPosition(brightness));

                }
            }

        }
    };

    private final BroadcastReceiver modeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragmentListener != null) {
                if (intent.getAction().equals(BluetoothHandler.MODE_UPDATE_ACTION)) {
                    String modeStr = intent.getStringExtra(BluetoothHandler.EXTRA_MODE);
                    int mode = Integer.parseInt(modeStr);
                    Log.d("Mode", "Mode: " + Mode.fromModeNumber(mode));
                    currentFragmentListener.onModeUpdate(mode);

                }
            }
        }
    };


    private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragmentListener != null) {
                if (intent.getAction().equals(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION)) {
                    Log.d("Disconnect", "Disconnect: ");
                    currentFragmentListener.onDisconnect();
                }
            }
        }
    };


    private final BroadcastReceiver colorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragmentListener != null) {
                if (intent.getAction().equals(BluetoothHandler.COLOR_DATA_UPDATE_ACTION)) {
                    Log.d("colorDataReceiver", "RECEIVED");
                    byte[] rawColorData = intent.getByteArrayExtra(BluetoothHandler.EXTRA_COLOR_DATA);

                    List<ModeColorData> colorData = ModeColorData.parseColorData(rawColorData);

                    if (currentFragmentListener != null) {
                        currentFragmentListener.onColorDataUpdate(colorData);
                    }
                }
            }
        }
    };

    private final BroadcastReceiver locationServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentFragmentListener != null) {
                String action = intent.getAction();
                if (action != null && action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                    boolean isEnabled = areLocationServicesEnabled();
                    Log.d("locationServiceStateReceiver", String.format("Location service state changed to: %s", isEnabled ? "on" : "off"));
                    checkPermissions();
                }
            }
        }
    };
    public final ActivityResultLauncher<Intent> enableBleRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
            });

    private final ActivityResultLauncher<String[]> blePermissionRequest = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                for(String key : result.keySet()) {
                    Log.d("blePermissionRequest", String.format("%s, %s", key, result));
                }
            });

    public String[] getMissingPermissions(String[] requiredPermissions) {
        Log.d("getMissingPermissions","getMissingPermissions");
        List<String> missingPermissions = new ArrayList<>();
        //TODO check if this is correct
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
//        }
        return missingPermissions.toArray(new String[0]);
    }

    public String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (areLocationServicesEnabled()) {
                initBluetoothHandler();
            } else {
                // Location services are not enabled, show dialog or handle accordingly
                Log.d("checkLocationServices","checkLocationServices");
                promptForLocationService();
            }
        } else {
            initBluetoothHandler();
        }
    }

    private boolean areLocationServicesEnabled() {
        //TODO check if this is correct
        Log.d("areLocationServicesEnabled","areLocationServicesEnabled");
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

    private void promptForLocationService() {
        // Show dialog to enable location services
        new AlertDialog.Builder(this)
                .setTitle("Location services are not enabled")
                .setMessage("Scanning for Bluetooth peripherals requires location services to be enabled.")
                .setPositiveButton("Enable", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        bleCommunicationUtil = new BLECommunicationUtil(BluetoothHandler.getInstance(getApplicationContext()));
    }

    @NotNull
    public BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    public void tryToEnableBLEAndStartScanning() {
        Log.d("Enable BLE try","Trying to enable BLE");
        if (getBluetoothManager().getAdapter() != null) {
            if (!isBluetoothEnabled()) {
                String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
                if (missingPermissions.length == 0) {
                    enableBleRequest.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                } else {
                    checkPermissions();
                }
            } else {
                checkPermissions();
            }
        } else {
            Log.d("Bluetooth","This device has no Bluetooth hardware");
        }
    }

    private void checkPermissions() {
        Log.d("checkPermissions","checkPermissions");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                //What it do?
                Log.d("checkPermissions","if");
                for (String permission : missingPermissions) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response!
                        showPermissionExplanation(permission);
                    } else {
                        // No explanation needed; request the permission
                        blePermissionRequest.launch(missingPermissions);
                        break;
                    }
                }
            } else {
                Log.d("checkPermissions","else");
                permissionsGranted();
            }
//        }
    }

    private void showPermissionExplanation(String permission) {
        String message;
        String title;

        // Customize the message based on the permission
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                title = "Потрібен дозвіл на розташування";
                message = "Цій програмі потрібен доступ до місцезнаходження для пошуку пристроїв Bluetooth. Увімкніть розташування в налаштуваннях програми.";
                break;
            case Manifest.permission.BLUETOOTH_SCAN:
                title = "Потрібен дозвіл на сканування Bluetooth";
                message = "Цій програмі потрібен дозвіл на сканування Bluetooth, щоб виявити пристрої Bluetooth поблизу. Увімкніть цей дозвіл у налаштуваннях програми.";
                break;

            //TODO check needed permissions
            default:
                title = "Additional Permission Required";
                message = "This app needs additional permissions to function correctly. Please check app settings.";
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("App Settings", (dialog, which) -> {
                    // Direct the user to the app settings
                    openAppSettings();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


}