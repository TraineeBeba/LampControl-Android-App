package com.example.myapplication.fragment;

import androidx.fragment.app.Fragment;

import android.content.BroadcastReceiver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.example.myapplication.FragmentBroadcastListener;
import com.example.myapplication.MainActivity;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.manager.ActiveButtonsManager;
import com.example.myapplication.manager.ColorPanelManager;
import com.example.myapplication.manager.SeekBarManager;
import com.example.myapplication.manager.TabManager;
import com.example.myapplication.model.ModeColorData;
import com.example.myapplication.model.ModeTab;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.model.TabInfo;
import com.example.myapplication.util.BLECommunicationUtil;
import com.example.myapplication.util.BroadcastReceiverUtil;
import com.github.mata1.simpledroidcolorpicker.pickers.CircleColorPicker;

import java.util.ArrayList;
import java.util.List;

public class LightFragment extends Fragment implements FragmentBroadcastListener {
    private BroadcastReceiverUtil receiverUtil;
    private BLECommunicationUtil bluetoothComm;
    private final List<ModeTab> modeTabs = new ArrayList<>();
    private RelativeLayout panelAddColor;
    private final List<TabInfo> tabInfoList = new ArrayList<>();
    private Button btnNavHome, btnNavWifi, btnMode1, btnMode2, btnMode3, backToPanelModeBtn;
    private ImageButton button_add_color;
    private SeekBar seekBar;
    private TextView percentageText;
    private ColorPanelManager colorPanelManager;

    CircleColorPicker rcp1;

    private TabManager tabManager;
    private SeekBarManager seekBarManager;
    // Broadcast Receivers
    private BroadcastReceiver brightnessUpdateReceiver;
    private BroadcastReceiver modeUpdateReceiver;
    private BroadcastReceiver lampStateUpdateReceiver;
    private BroadcastReceiver disconnectReceiver;
    ActiveButtonsManager activeButtonsManager;
    private Handler debounceHandler = new Handler();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.light, container, false);

        Log.d("LIGHT", "CREATE " + this);
        bluetoothComm = new BLECommunicationUtil(getContext());

//        registerReceivers();

        initView(view);
        initBtnListeners();
//        initBroadcastReceiver();

        initColorPickerButtons(view);

        activeButtonsManager = new ActiveButtonsManager(getContext(), bluetoothComm, modeTabs);
        tabManager = new TabManager(bluetoothComm, getContext(), tabInfoList, view.findViewById(R.id.modeImage), activeButtonsManager);
        colorPanelManager = new ColorPanelManager(getContext(), view, rcp1, activeButtonsManager);
        seekBarManager = new SeekBarManager(seekBar, percentageText, bluetoothComm);

        seekBarManager.setupSeekBar();
        seekBarManager.setupListeners();

        tabManager.getActiveButtonsManager().resetAll();
        activeButtonsManager.setAllActiveColorButtonsEnabled(false); // Disable all active color buttons

        return view;
    }

    private void initColorPickerButtons(View parentView) {
        ModeTab.colorPickerButtons = new ArrayList<>();
            for (int i = 1; i <= ModeTab.COLOR_PICKER_BUTTONS_COUNT; i++) {
                int buttonId = parentView.getResources().getIdentifier("colorBtn" + i + "_1", "id", parentView.getContext().getPackageName());
                Button button = parentView.findViewById(buttonId);
                if (button != null) {
                    ModeTab.colorPickerButtons.add(button);
                }
            }
    }

    private void initView(View view) {
//        rcp1 = view.findViewById(R.id.rcp1);
        seekBar = view.findViewById(R.id.seekBar);
        percentageText = view.findViewById(R.id.textviewbar);
        btnNavHome = view.findViewById(R.id.button_home);
        btnNavWifi = view.findViewById(R.id.button_wifi);
        btnMode1 = view.findViewById(R.id.button_one_color);
        btnMode2 = view.findViewById(R.id.button_rainbow);
        btnMode3 = view.findViewById(R.id.button_data_night);
        button_add_color = view.findViewById(R.id.addColorBtn);

        for (Mode mode : Mode.values()) {
            int tabLayoutId = getResources().getIdentifier("groupActiveColors" + (mode.getModeNumber() + 1), "id", getContext().getPackageName());
            tabInfoList.add(new TabInfo(view.findViewById(tabLayoutId), mode.getDrawableResId()));
        }

        button_add_color = view.findViewById(R.id.addColorBtn);
//        panelAddColor = view.findViewById(R.id.panelAddColor);
//        backToPanelModeBtn = view.findViewById(R.id.backToPanelModeBtn);

        for (int i = 0; i < 3; i++) {
            modeTabs.add(new ModeTab());
        }
        modeTabs.get(0).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode1_1));

        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_1));
        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_2));
        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_3));
        modeTabs.get(1).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode2_4));

        modeTabs.get(2).getActiveColorButtons().add(view.findViewById(R.id.activeColorBtnMode3_1));
    }

    private void initBtnListeners() {
        btnNavHome.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.lightLayout, FragmentType.HOME);
            }
        });

        btnNavWifi.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(R.id.lightLayout, FragmentType.WIFI);
            }
        });

        btnMode1.setOnClickListener(v -> {
            tabManager.changeTab(Mode.MODE_ONE);
        });

        btnMode2.setOnClickListener(v -> {
            tabManager.changeTab(Mode.MODE_TWO);
        });

        btnMode3.setOnClickListener(v -> {
            tabManager.changeTab(Mode.MODE_THREE);
        });

        button_add_color.setOnClickListener(v -> {
            btnMode1.setEnabled(false);
            btnMode2.setEnabled(false);
            btnMode3.setEnabled(false);

//            tabManager.disableAllTabs();
            panelAddColor.setVisibility(View.VISIBLE);

        });

        backToPanelModeBtn.setOnClickListener(v -> {
            btnMode1.setEnabled(true);
            btnMode2.setEnabled(true);
            btnMode3.setEnabled(true);

//            tabManager.restoreLastVisibleTab();
            panelAddColor.setVisibility(View.INVISIBLE);

        });

    }

    @Override
    public void onDestroyView() {
        Log.d("LIGHT", "DESTROY " + this);
//        bluetoothComm = null;
//        unregisterReceivers();
        super.onDestroyView();
//        receiverUtil.unregisterReceivers();
    }


//    private void registerReceivers() {
//        // Initialize and register Brightness Update Receiver
//        brightnessUpdateReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                int brightness = intent.getIntExtra(BluetoothHandler.EXTRA_BRIGHTNESS, 0);
//                seekBarManager.updateVisualBar(brightness);
//            }
//        };
//        requireActivity().registerReceiver(brightnessUpdateReceiver, new IntentFilter(BluetoothHandler.BRIGHTNESS_UPDATE_ACTION));
//
//        // Initialize and register Mode Update Receiver
//        modeUpdateReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                int mode = intent.getIntExtra(BluetoothHandler.EXTRA_MODE, 0);
//                tabManager.updateVisualMode(mode);
//            }
//        };
//        requireActivity().registerReceiver(modeUpdateReceiver, new IntentFilter(BluetoothHandler.MODE_UPDATE_ACTION));
//
//        // Initialize and register Lamp State Update Receiver
//        lampStateUpdateReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String lampStateStr = intent.getStringExtra(BluetoothHandler.EXTRA_LAMP_STATE);
//                Lamp lampState = Lamp.valueOf(lampStateStr);
//                Log.d("Light LampStateReceiver", "State: " + lampState.name());
//                // Handle lamp state update
//                // ...
//            }
//        };
//        requireActivity().registerReceiver(lampStateUpdateReceiver, new IntentFilter(BluetoothHandler.LAMP_STATE_UPDATE_ACTION));
//
//        // Initialize and register Disconnect Receiver
//        disconnectReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // Handle disconnect event
//                // ...
//            }
//        };
//        getActivity().registerReceiver(disconnectReceiver, new IntentFilter(BluetoothHandler.DISCONNECT_LAMP_STATE_UPDATE_ACTION));
//    }
//    @Override
//    public void onPause() {
//        Log.d("Pause", "LIGHT PAUSE");
//        super.onPause();
//        unregisterReceivers();
//    }
//
//    @Override
//    public void onResume() {
//        Log.d("Resume", "LIGHT RESUME");
//        super.onResume();
//        registerReceivers();
//    }
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

    @Override
    public void onBrightnessUpdate(int brightness) {
        seekBarManager.updateVisualBar(brightness);
    }
    @Override
    public void onModeUpdate(int mode) {
        tabManager.updateVisualMode(mode);
    }
    @Override
    public void onLampStateUpdate(Lamp lampState) {
        switch (lampState) {
            case OFF:
                seekBarManager.getSeekBar().setEnabled(false);
                percentageText.setText(LampCache.getBrightnessText() + " %");
                activeButtonsManager.setAllActiveColorButtonsEnabled(false); // Disable all active color buttons
                break;
            case ON:
                seekBarManager.getSeekBar().setEnabled(true);
                activeButtonsManager.setAllActiveColorButtonsEnabled(true); // Enable all active color buttons
                try {
                    bluetoothComm.readBrightness();
                    bluetoothComm.readMode();
                    bluetoothComm.readActiveColors();
                } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
                    Log.d("Exc", "Exc");
                }
                break;
        }
    }

    @Override
    public void onColorDataUpdate(List<ModeColorData> colorDataList) {
        Log.d("onColorDataUpdate", "onColorDataUpdate");
        for (ModeColorData colorData : colorDataList) {
            int modeIndex = colorData.modeIndex;
            List<ModeColorData.RGBColor> colors = colorData.colors;

            if (modeIndex >= 0 && modeIndex < modeTabs.size()) {
                ModeTab tab = modeTabs.get(modeIndex);
                Log.d("Tab", String.valueOf(modeIndex));
                tab.updateActiveButtonColors(getContext(), colors);
            }
        }
    }


    @Override
    public void onDisconnect() {
        seekBarManager.getSeekBar().setEnabled(false);
        activeButtonsManager.setAllActiveColorButtonsEnabled(false); // Disable all active color buttons
    }


}
