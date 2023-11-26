package com.example.myapplication.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ble.BluetoothHandler;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.listener.FragmentBroadcastListener;
import com.welie.blessed.BluetoothPeripheral;

import android.widget.ImageView;
import android.widget.ListView;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class BLEFragment extends Fragment implements FragmentBroadcastListener {

    private Button navHomeBtn, navLightBtn,/* searchLampBtn,*/ /*backToConnBtn,*/ refreshBtn;
    private ImageView refreshBtnImg;
    private ConstraintLayout panelConn1, panelConn2/*, wifilayout*/;
    private ListView deviceListView;
    TextView /*textConnectSuccessfully,*/ textConnectionAlready;
    Animation rotateAnimation;

    private BluetoothHandler bluetoothHandler;

    private Handler handler = new Handler();
    private List<BLEDeviceListItem> deviceListItems = new ArrayList<>();
    private CustomListAdapter listAdapter;
    private Context context;

    private static final long DEBOUNCE_TIME_MS = 200; // Debounce time in milliseconds
    private long lastListItemClickTime = 0;
    private long lastButtonClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connection, container, false);

        initializeViews(view);
        initializeBluetoothHandler();
        setupListView();
        setupButtons();

        return view;
    }
    
    private void initializeBluetoothHandler() {
        context = getActivity();
        bluetoothHandler = BluetoothHandler.getInstance(context);
//        bluetoothHandler.setBluetoothConnectionCallback(this);
        bluetoothHandler.setBluetoothScanCallback(this::handleBluetoothScan);
    }

    private void initializeViews(View view) {
//        wifilayout = view.findViewById(R.id.wifiLayout);
        panelConn1 = view.findViewById(R.id.panel_connection1);
        panelConn2 = view.findViewById(R.id.panel_connection2);
        refreshBtnImg = view.findViewById(R.id.update_btn_img);
        deviceListView = view.findViewById(R.id.listView);
//        textConnectSuccessfully = view.findViewById(R.id.text_connect_successfully);
        textConnectionAlready = view.findViewById(R.id.text_connection_already);
//        backToConnBtn = view.findViewById(R.id.back_to_conn_btn);
        refreshBtn = view.findViewById(R.id.update_btn);
        navHomeBtn = view.findViewById(R.id.button_home);
        navLightBtn = view.findViewById(R.id.button_light);
//        searchLampBtn = view.findViewById(R.id.search_lamp);
        rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.circle);
    }

    private void setupListView() {
        listAdapter = new CustomListAdapter(getContext(), deviceListItems);
        listAdapter.clear();
        deviceListView.setAdapter(listAdapter);
        deviceListView.setOnItemClickListener(new DebouncedOnClickListener(DEBOUNCE_TIME_MS) {
            @Override
            public void onDebouncedClick(AdapterView<?> parent, View view, int position, long id) {
                handleListItemClick(parent, view, position, id);
            }
        });
        setUpDeviceList();
    }

    private abstract class DebouncedOnClickListener implements AdapterView.OnItemClickListener {
        private final long minimumInterval;

        public DebouncedOnClickListener(long minimumInterval) {
            this.minimumInterval = minimumInterval;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastListItemClickTime > minimumInterval) {
                lastListItemClickTime = currentTime;
                onDebouncedClick(parent, view, position, id);
            }
        }

        public abstract void onDebouncedClick(AdapterView<?> parent, View view, int position, long id);
    }

    private abstract class DebouncedButtonClickListener implements View.OnClickListener {
        private final long minimumInterval;

        public DebouncedButtonClickListener(long minimumInterval) {
            this.minimumInterval = minimumInterval;
        }

        @Override
        public void onClick(View v) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastButtonClickTime > minimumInterval) {
                lastButtonClickTime = currentTime;
                onDebouncedClick(v);
            }
        }

        public abstract void onDebouncedClick(View v);
    }
    private void setupButtons() {
        navHomeBtn.setOnClickListener(v -> navigateTo(FragmentType.HOME));
        navLightBtn.setOnClickListener(v -> navigateTo(FragmentType.LIGHT));
//        backToConnBtn.setOnClickListener(v -> togglePanelVisibility(true));
//        searchLampBtn.setOnClickListener(v -> {
////            togglePanelVisibility(false);
//            performBluetoothOperation();
//        });
        refreshBtn.setOnClickListener(new DebouncedButtonClickListener(DEBOUNCE_TIME_MS) {
            @Override
            public void onDebouncedClick(View v) {
                performBluetoothOperation();
            }
        });
    }

    private void handleListItemClick(AdapterView<?> parent, View view, int position, long id) {
        BLEDeviceListItem deviceListItem = deviceListItems.get(position);
        String deviceAddress = deviceListItem.getAddress();
        BluetoothPeripheral device = bluetoothHandler.getDiscoveredPeripheral(deviceAddress);

        if (device != null) {
            if (isDeviceConnected(deviceAddress)) {
                bluetoothHandler.disconnectPeripheral(device);
                deviceListItem.setViewConnected(true);
                listAdapter.notifyDataSetChanged();
            } else {
                List<BluetoothPeripheral> connectedDevices = bluetoothHandler.getConnectedDevices();
                if(connectedDevices.size() == 0) {
                    Toast.makeText(context, "Підключення до " + deviceListItem.getName() + " " + deviceAddress, Toast.LENGTH_SHORT).show();
                    bluetoothHandler.connectPeripheral(device);
                } else {
                    Toast.makeText(context, "Відключіть підключений пристрій", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(context, "Пристрій не знайдено", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDeviceConnected(String deviceAddress) {
        List<BluetoothPeripheral> connectedDevices = bluetoothHandler.getConnectedDevices();
        for (BluetoothPeripheral connectedDevice : connectedDevices) {
            if(connectedDevice.getAddress().equals(deviceAddress)) {
                return true;
            }
        }
        return false;
    }

    private void handleBluetoothScan(String deviceName, String deviceAddress) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                boolean deviceExists = false;
                for (BLEDeviceListItem item : deviceListItems) {
                    if (item.getAddress().equals(deviceAddress)) {
                        deviceExists = true;
                        break;
                    }
                }

                if (!deviceExists) {
                    BLEDeviceListItem newDevice = new BLEDeviceListItem(deviceName, deviceAddress);
                    deviceListItems.add(newDevice);
                    listAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setUpDeviceList() {
        //TODO при переході між сторінками не зберігає попередній список знайдених пристроїв
//        if(checkPermissions()) {
//            bluetoothHandler.setBluetoothConnectionCallback(this);
            List<BluetoothPeripheral> connectedDevices = BluetoothHandler.getInstance(context).getConnectedDevices();
            connectedDevices.forEach(device -> {
                String deviceName = device.getName();
                if (!deviceListItems.contains(deviceName)) {
                    deviceListItems.add(new BLEDeviceListItem(deviceName, device.getAddress()));
                }
            });
            updateConnectedDeviceUI();
    }


    private boolean checkPermissions() {
        return ((MainActivity) getActivity()).getMissingPermissions(((MainActivity) getActivity()).getRequiredPermissions()).length == 0;
    }

    private void startScanning(Animation rotateAnimation) {
//        if(checkPermissions()) {
            List<BluetoothPeripheral> connectedDevices = bluetoothHandler.getConnectedDevices();
            if(connectedDevices.size()>0){
                Toast.makeText(context, "Відключіть спочатку підключений пристрій, будь ласка", Toast.LENGTH_SHORT).show();
                updateConnectedDeviceUI();
            } else {
                bluetoothHandler.discoveredPeripherals.clear();
                deviceListItems.clear();
                listAdapter.notifyDataSetChanged();
                bluetoothHandler.findDevices();
                refreshBtnImg.startAnimation(rotateAnimation);
            }
//        }
    }

    @Override
    public void onConnect() {
        if (isAdded() && getActivity() != null) {
            requireActivity().runOnUiThread(() -> {
                if (refreshBtnImg != null) {
                    refreshBtnImg.clearAnimation();
                    updateConnectedDeviceUI();
                }
            });
        }
    }

    @Override
    public void onDisconnect() {
        updateConnectedDeviceUI();
    }

    private void updateConnectedDeviceUI() {
        List<BluetoothPeripheral> connectedDevices = bluetoothHandler.getConnectedDevices();
        if(connectedDevices.size()>0) {
            for (BLEDeviceListItem BLEDeviceListItem : deviceListItems) {
                if(isDeviceConnected(BLEDeviceListItem.getAddress()))
                    BLEDeviceListItem.setViewConnected(true);
            }
            textConnectionAlready.setVisibility(View.VISIBLE);
        } else {
            for (BLEDeviceListItem BLEDeviceListItem : deviceListItems) {
                BLEDeviceListItem.setViewConnected(false);
            }
            textConnectionAlready.setVisibility(View.INVISIBLE);
        }
        listAdapter.notifyDataSetChanged();
    }

    private void navigateTo(FragmentType fragmentType) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(R.id.wifiLayout, fragmentType);
        }
    }

    private void togglePanelVisibility(boolean showPanel1) {
        panelConn1.setVisibility(showPanel1 ? View.VISIBLE : View.INVISIBLE);
        panelConn2.setVisibility(showPanel1 ? View.INVISIBLE : View.VISIBLE);
    }

    private void performBluetoothOperation() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.tryToEnableBLEAndStartScanning();
            if(mainActivity.isBluetoothEnabled() && checkPermissions()) {
                startScanning(rotateAnimation);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentFragmentListener(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setCurrentFragmentListener(null);
        }
    }

}