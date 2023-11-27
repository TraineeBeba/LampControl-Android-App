package com.example.myapplication.fragment;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;



import com.example.myapplication.listener.FragmentBroadcastListener;
import com.example.myapplication.MainActivity;
import com.example.myapplication.constant.Lamp;
import com.example.myapplication.constant.Mode;
import com.example.myapplication.manager.seekbar.SeekBarManager;
import com.example.myapplication.manager.mode_tab.TabManager;
import com.example.myapplication.manager.mode_tab.model.ModeColorData;
import com.example.myapplication.manager.mode_tab.model.ModeTab;
import com.example.myapplication.R;
import com.example.myapplication.ble.exception.BluetoothNotConnectedException;
import com.example.myapplication.ble.exception.CharacteristicNotFoundException;
import com.example.myapplication.constant.FragmentType;
import com.example.myapplication.constant.LampCache;
import com.example.myapplication.util.BLECommunicationUtil;

import java.util.List;

public class LightFragment extends Fragment implements FragmentBroadcastListener {
    private ConstraintLayout panelAddColor;
    private Button btnNavHome, btnNavWifi, btnMode1, btnMode2, btnMode3;
    AppCompatImageButton backToPanelModeBtn;
    private Button button_add_color;
    private TabManager tabManager;
    private SeekBarManager seekBarManager;

    private TextView textViewOneMode;
    private TextView textViewTwoMode;
    private TextView textViewThreeMode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.light, container, false);

        Log.d("LIGHT", "CREATE " + this);

        initView(view);
        initBtnListeners();

        tabManager = new TabManager(getContext(), view);
        seekBarManager = new SeekBarManager(view);

        tabManager.setAllActiveColorButtonsEnabled(false); // Disable all active color buttons

        return view;
    }

    private void initView(View view) {
        btnNavHome = view.findViewById(R.id.button_home);
        btnNavWifi = view.findViewById(R.id.button_wifi);
        btnMode1 = view.findViewById(R.id.button_one_color);
        btnMode2 = view.findViewById(R.id.button_rainbow);
        btnMode3 = view.findViewById(R.id.button_data_night);
        button_add_color = view.findViewById(R.id.addColorBtn);
        panelAddColor = view.findViewById(R.id.panelAddColor);
        backToPanelModeBtn = view.findViewById(R.id.backToPanelModeBtn);
        textViewOneMode = view.findViewById(R.id.textView2);
        textViewTwoMode = view.findViewById(R.id.textView4);
        textViewThreeMode = view.findViewById(R.id.textView3);
    }

    private void textWhite(TextView txt){
        txt.setShadowLayer(
                10,
                0,
                10,
                0xAAFFFFFF
        );
    }

    private void textBlack(TextView txt){
        txt.setShadowLayer(
                10,
                0,
                10,
                0xAA000000
        );
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
            saveTabSelection("MODE_ONE");

            textWhite(textViewOneMode);
            textBlack(textViewTwoMode);
            textBlack(textViewThreeMode);

        });

        btnMode2.setOnClickListener(v -> {
            tabManager.changeTab(Mode.MODE_TWO);
            textBlack(textViewOneMode);
            textWhite(textViewTwoMode);
            textBlack(textViewThreeMode);
        });

        btnMode3.setOnClickListener(v -> {
            tabManager.changeTab(Mode.MODE_THREE);
            textBlack(textViewOneMode);
            textBlack(textViewTwoMode);
            textWhite(textViewThreeMode);
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
        super.onDestroyView();
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
                setUIEnabled(false);
                seekBarManager.setPercentageText(LampCache.getBrightnessText() + " %");
                break;
            case ON:
                setUIEnabled(true);
                try {
                    BLECommunicationUtil bleCommunicationUtil = MainActivity.getBleCommunicationUtil();
                    bleCommunicationUtil.readBrightness();
                    bleCommunicationUtil.readMode();
                    MainActivity.getBleCommunicationUtil().readActiveColors();
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

            List<ModeTab> tabs = tabManager.getTabs();
            if (modeIndex >= 0 && modeIndex < tabs.size()) {
                ModeTab tab = tabs.get(modeIndex);
                Log.d("Tab", String.valueOf(modeIndex));
                tab.updateTabActiveButtonColors(colors);

            }
        }
    }


    @Override
    public void onDisconnect() {
        seekBarManager.getSeekBar().setEnabled(false);
        tabManager.setAllActiveColorButtonsEnabled(false); // Disable all active color buttons
    }

    @Override
    public void onConnect() {
        try {
            MainActivity.getBleCommunicationUtil().readActiveColors();
        } catch (BluetoothNotConnectedException | CharacteristicNotFoundException e) {
            Log.d("Exc", "Exc");
        }
    }


    private void setUIEnabled(boolean enabled) {
        seekBarManager.getSeekBar().setEnabled(enabled);
        tabManager.setAllActiveColorButtonsEnabled(enabled);
    }
}
