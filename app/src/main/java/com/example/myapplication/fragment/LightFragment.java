package com.example.myapplication.fragment;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.example.myapplication.MainActivity;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.manager.ActiveButtonsManager;
import com.example.myapplication.manager.ColorPanelManager;
import com.example.myapplication.manager.SeekBarManager;
import com.example.myapplication.manager.TabManager;
import com.example.myapplication.model.ModeTab;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.model.TabInfo;
import com.example.myapplication.util.BLECommunicationUtil;
import com.example.myapplication.util.BrightnessModeUtil;
import com.example.myapplication.util.BroadcastReceiverUtil;
import com.github.mata1.simpledroidcolorpicker.pickers.CircleColorPicker;

import java.util.ArrayList;
import java.util.List;

public class LightFragment extends Fragment {
    private BroadcastReceiverUtil receiverUtil;
    private BLECommunicationUtil bluetoothComm;
    private final List<ModeTab> modeTabs = new ArrayList<>();
    private RelativeLayout panelAddColor;
    private final List<TabInfo> tabInfoList = new ArrayList<>();
    private Button btnNavHome, btnNavWifi, btnMode1, btnMode2, btnMode3, button_add_color, backToPanelModeBtn;
    private SeekBar seekBar;
    private TextView percentageText;
    private ColorPanelManager colorPanelManager;

    CircleColorPicker rcp1;

    private TabManager tabManager;
    private SeekBarManager seekBarManager;

    private Handler debounceHandler = new Handler();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.light, container, false);
        bluetoothComm = new BLECommunicationUtil(getContext());

        initView(view);
        initBtnListeners();
        initBroadcastReceiver();

        initColorPickerButtons(view);
        ActiveButtonsManager activeButtonsManager = new ActiveButtonsManager(getContext(), bluetoothComm, modeTabs);
        tabManager = new TabManager(bluetoothComm, getContext(), tabInfoList, view.findViewById(R.id.modeImage), activeButtonsManager);
        colorPanelManager = new ColorPanelManager(getContext(), view, rcp1, activeButtonsManager);
        seekBarManager = new SeekBarManager(seekBar, percentageText, bluetoothComm);

        seekBarManager.setupSeekBar();
        seekBarManager.setupListeners();

        tabManager.getActiveButtonsManager().resetAll();


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
        rcp1 = view.findViewById(R.id.rcp1);
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
        panelAddColor = view.findViewById(R.id.panelAddColor);
        backToPanelModeBtn = view.findViewById(R.id.backToPanelModeBtn);

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

    private void initBroadcastReceiver() {
        receiverUtil = new BroadcastReceiverUtil(getContext(), new BroadcastReceiverUtil.Callback() {
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
                        break;
                    case ON:
                        seekBarManager.getSeekBar().setEnabled(true);
                        try {
                            bluetoothComm.readBrightness();
                        } catch (BluetoothNotConnectedException |
                                 CharacteristicNotFoundException e) {
                            Log.d("Exc", "Exc");
                        }
                        break;
                }
            }
            @Override
            public void onDisconnect() {
                seekBarManager.getSeekBar().setEnabled(false);
            }
        });

        receiverUtil.registerReceivers();
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
        super.onDestroyView();
        receiverUtil.unregisterReceivers();
    }

}
