package com.example.myapplication.frame;

import static com.example.myapplication.ble.BluetoothHandler.LAMP_SWITCH_CHARACTERISTIC_UUID;
import static com.example.myapplication.ble.BluetoothHandler.LC_SERVICE_UUID;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.FrameLayout;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.State;
import com.example.myapplication.ble.BluetoothHandler;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.WriteType;


public class HomeFrame extends Fragment {

    private FrameLayout  mainLayoutView;
    private ImageView imageView; // image for button off/on
    private Button btnToggleLamp;
    private Button buttonWifi;
    private Button buttonLight;
//<<<<<<< Updated upstream
//    private Button buttonLight;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.activity_main, container, false);
//
//        imageView = view.findViewById(R.id.image_turn);
//        mainLayoutView = view.findViewById(R.id.mainLayout1);
//
//        setUpLamp();
//
////        toggleLamp();
//        Log.d("HUI", "ONCREATE");
//        // Ініціалізація кнопки для вмикання/вимикання лампи
//        buttonToggleLamp = view.findViewById(R.id.Button_on);
//        buttonToggleLamp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleLamp();
//            }
//        });
//
//        // Ініціалізація іншої кнопки
//
//
//        return view;
//}
//=======
//>>>>>>> Stashed changes

    private void setUpLamp() {
        if (!State.isLampOn){
            Log.d("AAAA ", "On");
            imageView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_off);

        } else{
            Log.d("AAAA", "Off");
            imageView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_on);

        }
    }


    public void toggleLamp() {
        // lamp tracking off/on

        if (State.isLampOn){
            Log.d("AAAA ", "On");
            imageView.setImageResource(R.drawable.turn_off_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_off);
            State.isLampOn = false;
        }
        else{
            Log.d("AAAA", "Off");
            imageView.setImageResource(R.drawable.turn_on_image); // changes  picture for button
            mainLayoutView.setBackgroundResource(R.drawable.homescreen__background_on);
            State.isLampOn = true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_main, container, false);

        imageView = view.findViewById(R.id.image_turn);
        mainLayoutView = view.findViewById(R.id.mainLayout1);
        btnToggleLamp = view.findViewById(R.id.Button_on);
        buttonWifi = view.findViewById(R.id.button_wifi);

        setUpLamp();

        btnToggleLamp.setOnClickListener(v -> {
            toggleLamp();
            toggleLampState();
        });

        buttonLight = view.findViewById(R.id.button_light);
        buttonLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showFromHomeToLightFragment();
                }
            }
        });

        buttonWifi = view.findViewById(R.id.button_wifi);
        buttonWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showFromHomeToWifiFragment();
                }
            }
        });

        return view;
    }

    private void toggleLampState() {
        // Assuming you have already connected to the peripheral and discovered services
        String address = BluetoothHandler.address;
        BluetoothPeripheral peripheral = getPeripheral(address);
        if (peripheral != null) {
            BluetoothGattCharacteristic lampStateCharacteristic = peripheral.getCharacteristic(LC_SERVICE_UUID, LAMP_SWITCH_CHARACTERISTIC_UUID);
            if (lampStateCharacteristic != null) {
                String currentState = lampStateCharacteristic.getStringValue(0);
                // Toggle the lamp state
                String newState = "OFF".equals(currentState) ? "ON" : "OFF";
                byte[] newValue = newState.getBytes();
                peripheral.writeCharacteristic(lampStateCharacteristic, newValue, WriteType.WITH_RESPONSE);
            }
        }
    }

    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(getContext()).central;
        return central.getPeripheral(peripheralAddress);
    }

////    @SuppressLint("UnspecifiedRegisterReceiverFlag")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
////        registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Context.RECEIVER_NOT_EXPORTED : 0 ;
//        } else {
//        }
//
//
//
//    }



}