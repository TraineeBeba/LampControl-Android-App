package com.example.myapplication.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import android.widget.RelativeLayout;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.constant.FragmentType;
import com.welie.blessed.BluetoothPeripheral;

import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class WifiFragment extends Fragment {
    private Button buttonHome;
    private Button buttonLight;
    private Button search_lamp;
    private Button back_to_conn_btn;
    private Button update_btn;
    private ListView listView;
    private RelativeLayout panel1;
    private RelativeLayout panel2;
    private FrameLayout wifilayout;

    private Handler handler = new Handler();
    private ArrayAdapter<String> adapter;
    List<String> catNames = new ArrayList<>();
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connection, container, false);

        wifilayout = view.findViewById(R.id.wifiLayout);
//        panel1 = view.findViewById(R.id.panel1);
//        panel2 = view.findViewById(R.id.panel2);

        listView = view.findViewById(R.id.listView);
        context = getActivity();
        adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, catNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedDeviceName = catNames.get(position);
            BluetoothHandler bluetoothHandler = BluetoothHandler.getInstance(getContext());
            BluetoothPeripheral selectedPeripheral = bluetoothHandler.getDiscoveredPeripheral(selectedDeviceName);
            if (selectedPeripheral != null) {
                bluetoothHandler.connectPeripheral(selectedPeripheral);
                Toast.makeText(context, "Connecting to: " + selectedDeviceName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Device not found", Toast.LENGTH_SHORT).show();
            }
        });

        BluetoothHandler.getInstance(getContext()).setBluetoothScanCallback(deviceName -> handler.post(() -> {
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

        search_lamp = view.findViewById(R.id.search_lamp);
        search_lamp.setOnClickListener(v -> {
            wifilayout.setBackgroundResource(R.drawable.lamps_near_background);
            panel1.setVisibility(View.INVISIBLE);
            panel2.setVisibility(View.VISIBLE);

            // Check permissions and initialize Bluetooth
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).tryToEnableBLEAndStartScanning();
            }
            if(((MainActivity) getActivity()).getMissingPermissions(((MainActivity) getActivity()).getRequiredPermissions()).length == 0) {
                BluetoothHandler.getInstance(context).findDevices();
            }
        });

        back_to_conn_btn = view.findViewById(R.id.back_to_conn_btn);
        back_to_conn_btn.setOnClickListener(v -> {
            wifilayout.setBackgroundResource(R.drawable.search_lamp_background);
            panel1.setVisibility(View.VISIBLE);
            panel2.setVisibility(View.INVISIBLE);
        });
        back_to_conn_btn = view.findViewById(R.id.back_to_conn_btn);
        back_to_conn_btn.setOnClickListener(v -> {
            wifilayout.setBackgroundResource(R.drawable.search_lamp_background);
            panel1.setVisibility(View.VISIBLE);
            panel2.setVisibility(View.INVISIBLE);
        });

        update_btn = view.findViewById(R.id.update_btn);
        update_btn.setOnClickListener(v -> {
            // Clear the existing items from the list
            catNames.clear();
            adapter.notifyDataSetChanged();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).tryToEnableBLEAndStartScanning();
            }

            if(((MainActivity) getActivity()).getMissingPermissions(((MainActivity) getActivity()).getRequiredPermissions()).length == 0) {
                BluetoothHandler.getInstance(context).findDevices();
            }
        });
        return view;
    }

}