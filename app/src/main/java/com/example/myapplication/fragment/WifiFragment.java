package com.example.myapplication.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.constant.FragmentType;
import com.welie.blessed.BluetoothPeripheral;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class WifiFragment extends Fragment implements BluetoothHandler.BluetoothConnectionCallback{
    private Button buttonHome, buttonLight, search_lamp, back_to_conn_btn, update_btn;
    private ImageView update_btn_img;
    private ConstraintLayout panel_connection1,  panel_connection2;
    private ConstraintLayout wifilayout;
    private ListView listView;
    TextView text_connect_successfully, text_connection_already;

    private Handler handler = new Handler();
    private ArrayAdapter<String> adapter;
    List<String> catNames = new ArrayList<>();
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connection, container, false);

        wifilayout = view.findViewById(R.id.wifiLayout);
        panel_connection1 = view.findViewById(R.id.panel_connection1);
        panel_connection2 = view.findViewById(R.id.panel_connection2);
        update_btn_img = view.findViewById(R.id.update_btn_img);

        listView = view.findViewById(R.id.listView);
        context = getActivity();
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, catNames);
        adapter.clear();
        listView.setAdapter(adapter);

        text_connect_successfully = view.findViewById(R.id.text_connect_successfully);
        text_connection_already = view.findViewById(R.id.text_connection_already);

        initConnectedDevices();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedDeviceName = catNames.get(position);
            BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
            BluetoothPeripheral selectedPeripheral = bluetoothHandler.getDiscoveredPeripheral(selectedDeviceName);
            if (selectedPeripheral != null) {
                bluetoothHandler.connectPeripheral(selectedPeripheral);
//                text_connect_successfully.setVisibility(View.VISIBLE);
//                text_connection_already.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Device: " + selectedPeripheral.getAddress(), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, "Device not found", Toast.LENGTH_SHORT).show();
            }
        });

        BluetoothHandler.getInstance(getContext()).setBluetoothScanCallback((deviceName, deviceAddress) -> handler.post(() -> {
//            if (!catNames.contains(deviceName + " " + deviceAddress)) {
//                catNames.add(deviceName + " " + deviceAddress);
//                adapter.notifyDataSetChanged();
//            }
            if (!catNames.contains(deviceName)) {
                catNames.add(deviceName);
                adapter.notifyDataSetChanged();
            }
        }));


        buttonHome = view.findViewById(R.id.button_home);
        buttonHome.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.wifiLayout, FragmentType.HOME);
            }
        });

        buttonLight = view.findViewById(R.id.button_light);
        buttonLight.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.wifiLayout, FragmentType.LIGHT);
            }
        });

        Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.circle);

        search_lamp = view.findViewById(R.id.search_lamp);
        search_lamp.setOnClickListener(v -> {
            panel_connection1.setVisibility(View.INVISIBLE);
            panel_connection2.setVisibility(View.VISIBLE);

            // Check permissions and initialize Bluetooth
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).tryToEnableBLEAndStartScanning();
            }
            if( ((MainActivity) getActivity()).isBluetoothEnabled()) startScanning(rotateAnimation);
        });


        back_to_conn_btn = view.findViewById(R.id.back_to_conn_btn);
        back_to_conn_btn.setOnClickListener(v -> {
            panel_connection1.setVisibility(View.VISIBLE);
            panel_connection2.setVisibility(View.INVISIBLE);
        });

        update_btn = view.findViewById(R.id.update_btn);
        update_btn.setOnClickListener(v -> {
            // Clear the existing items from the list
//            catNames.clear();
//            adapter.notifyDataSetChanged();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).tryToEnableBLEAndStartScanning();
            }

            if( ((MainActivity) getActivity()).isBluetoothEnabled()) startScanning(rotateAnimation);
        });
        return view;
    }

    private void initConnectedDevices() {
        //TODO при переході між сторінками не зберігає попередній список знайдених пристроїв
        if(checkPermissions()) {
            BluetoothHandler.getInstance(getContext()).setBluetoothConnectionCallback(this);
            List<BluetoothPeripheral> connectedDevices = BluetoothHandler.getInstance(context).getConnectedDevices();
            connectedDevices.forEach(device -> {
                String deviceName = device.getName();
                if (!catNames.contains(deviceName)) {
                    catNames.add(deviceName);
                }
            });
            adapter.notifyDataSetChanged();
            if(connectedDevices.size()>0){
//                text_connect_successfully.setVisibility(View.VISIBLE);
//                text_connection_already.setText(text_connection_already.getText() + "\n" + connectedDevices.get(0).getName());
                text_connection_already.setVisibility(View.VISIBLE);
            } else{
//                text_connect_successfully.setVisibility(View.INVISIBLE);
                text_connection_already.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean checkPermissions() {
        return ((MainActivity) getActivity()).getMissingPermissions(((MainActivity) getActivity()).getRequiredPermissions()).length == 0;
    }

    private void startScanning(Animation rotateAnimation) {
        if(checkPermissions()) {
            List<BluetoothPeripheral> connectedDevices = BluetoothHandler.getInstance(context).getConnectedDevices();
            if(connectedDevices.size()>0){
//                text_connect_successfully.setVisibility(View.VISIBLE);
//                text_connection_already.setText(text_connection_already.getText() + "\n" + connectedDevices.get(0).getName());
                text_connect_successfully.setVisibility(View.VISIBLE);
            } else {
                BluetoothHandler.getInstance(context).findDevices();
                update_btn_img.startAnimation(rotateAnimation);
            }
        }
    }

    @Override
    public void onDeviceConnected() {
        requireActivity().runOnUiThread(() -> {
            if (update_btn_img != null) {
                update_btn_img.clearAnimation();

                List<BluetoothPeripheral> connectedDevices = BluetoothHandler.getInstance(context).getConnectedDevices();
                if(connectedDevices.size()>0) {
                    //TODO знаходимо потрібней елемент і апдейтимо відображення
//                    catNames.clear();
//                    catNames.add(connectedDevices.get(0).getName());
                    adapter.notifyDataSetChanged();
                    text_connect_successfully.setVisibility(View.VISIBLE);
                    text_connection_already.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDeviceDisconnected() {
        catNames.clear();
        adapter.notifyDataSetChanged();
        text_connection_already.setVisibility(View.INVISIBLE);
        text_connect_successfully.setVisibility(View.INVISIBLE);
    }
}