package com.dspread.pos.ui.setting.connection_settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dspread.pos.TerminalApplication;
import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.R;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;

public class ConnectionSettingsViewModel extends BaseViewModel {
    // The name of the currently connected device
    public final ObservableField<String> deviceName = new ObservableField<>(getApplication().getString(R.string.no_device));

    //Device connection status
    public final ObservableBoolean deviceConnected = new ObservableBoolean(false);

    // Current transaction type
    public final ObservableField<String> transactionType = new ObservableField<>("");

    // Current card mode
    public final ObservableField<String> cardMode = new ObservableField<>("");

    // Current currency code
    public final ObservableField<String> currencyCode = new ObservableField<>("");

    // Event: Select Device
    public final SingleLiveEvent<Void> selectDeviceEvent = new SingleLiveEvent<>();

    // Event: Transaction Type Click
    public final SingleLiveEvent<Void> transactionTypeClickEvent = new SingleLiveEvent<>();

    // Event: Card Mode Click
    public final SingleLiveEvent<Void> cardModeClickEvent = new SingleLiveEvent<>();

    // Event: Currency Code Click
    public final SingleLiveEvent<Void> currencyCodeClickEvent = new SingleLiveEvent<>();
    private TerminalApplication baseApplication;
    private POS_TYPE currentPOSType;

    public ConnectionSettingsViewModel(@NonNull Application application) {
        super(application);
        loadSettings();
        if(baseApplication == null){
            baseApplication = (TerminalApplication) BaseApplication.getInstance();
        }
    }

    /**
     * Load settings from SharedReferences
     */
    public void loadSettings() {
        // Load device name
        String savedDeviceName = SPUtils.getInstance().getString("device_type", "");
        if(!"".equals(savedDeviceName)){
            deviceName.set(savedDeviceName);
            if(savedDeviceName.equals(POS_TYPE.UART.name())){
                currentPOSType = POS_TYPE.UART;
            }else if(savedDeviceName.equals(POS_TYPE.USB.name())){
                currentPOSType = POS_TYPE.USB;
            }else  if(savedDeviceName.equals(POS_TYPE.BLUETOOTH.name())){
                currentPOSType = POS_TYPE.BLUETOOTH;
            }
            deviceConnected.set(true);
        }else {
            savedDeviceName = getApplication().getString(R.string.no_device);
        }
        updateDeviceName(savedDeviceName);

        // Load transaction type
        String savedTransType = SPUtils.getInstance().getString("transactionType", "");
        if (savedTransType == null || "".equals(savedTransType)) {
            SPUtils.getInstance().put("transactionType","GOODS");
            savedTransType = "GOODS";
        }
        transactionType.set(savedTransType);

        // Loading card mode
        String savedCardMode = SPUtils.getInstance().getString("cardMode", "");
        if (savedCardMode == null || "".equals(savedCardMode)) {
            if(DeviceUtils.isSmartDevices()) {
                SPUtils.getInstance().put("cardMode", "SWIPE_TAP_INSERT_CARD_NOTUP");
                savedCardMode = "SWIPE_TAP_INSERT_CARD_NOTUP";
            }else {
                SPUtils.getInstance().put("cardMode", "SWIPE_TAP_INSERT_CARD");
                savedCardMode = "SWIPE_TAP_INSERT_CARD";
            }
        }
        cardMode.set(savedCardMode);

        // Load currency code
        String savedCurrencyCode = SPUtils.getInstance().getString("currencyName", "");
        if (savedCurrencyCode == null || "".equals(savedCurrencyCode)) {
            SPUtils.getInstance().put("currencyCode",156);
            savedCurrencyCode = "CNY";
        }
        currencyCode.set(savedCurrencyCode);
    }

    /**
     * Save settings to SharedReferences
     */
    public void saveSettings() {
        // Save device connection status
        if("".equals(deviceName.get())||getApplication().getString(R.string.no_device).equals(deviceName.get())) {
            // Save device name
            SPUtils.getInstance().put("device_type", "");
        }else {
            if(deviceName.get().contains(POS_TYPE.BLUETOOTH.name())){
                SPUtils.getInstance().put("device_type", POS_TYPE.BLUETOOTH.name());
            }else {
                SPUtils.getInstance().put("device_type", deviceName.get());
            }
        }
    }
    public BindingCommand<Boolean> toggleDeviceCommand = new BindingCommand<>(isChecked -> {
        deviceConnected.set(isChecked);
        String deviceType = SPUtils.getInstance().getString("device_type", "");
        if (!"".equals(deviceType)) {
            if (isChecked) {
                selectDeviceEvent.call();
            } else {
                POSManager.getInstance().close();
                SPUtils.getInstance().put("device_type","");
                updateDeviceName(getApplication().getString(R.string.no_device));
            }
        }
        saveSettings();
    });

    /**
     * Select device command
     */
    public BindingCommand selectDeviceCommand = new BindingCommand(() -> {
        selectDeviceEvent.call();
    });

    /**
     * Transaction Type Click Command
     */
    public BindingCommand transactionTypeCommand = new BindingCommand(() -> {
        transactionTypeClickEvent.call();
    });

    /**
     * Card mode click command
     */
    public BindingCommand cardModeCommand = new BindingCommand(() -> {
        cardModeClickEvent.call();
    });

    /**
     * Currency code click command
     */
    public BindingCommand currencyCodeCommand = new BindingCommand(() -> {
        currencyCodeClickEvent.call();
    });

    /**
     * Update device name
     */
    public void updateDeviceName(String name) {
        deviceName.set(name);
        saveSettings();
    }
}
