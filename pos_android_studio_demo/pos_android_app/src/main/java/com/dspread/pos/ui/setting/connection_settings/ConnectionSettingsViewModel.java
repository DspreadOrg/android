package com.dspread.pos.ui.setting.connection_settings;

import android.app.Application;
import android.text.TextUtils;

import com.dspread.pos.TerminalApplication;
import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;

public class ConnectionSettingsViewModel extends BaseViewModel {
    // The name of the currently connected device
    public final ObservableField<String> deviceName = new ObservableField<>(getApplication().getString(R.string.no_device));
    public final ObservableField<String> bluetoothDeviceName = new ObservableField<>(getApplication().getString(R.string.no_device));
    public final ObservableField<String> uartDeviceName = new ObservableField<>(getApplication().getString(R.string.no_device));

    //Device connection status
    public final ObservableBoolean deviceConnected = new ObservableBoolean(false);

    // Current transaction type
    public final ObservableField<String> transactionType = new ObservableField<>("");

    // Current card mode
    public final ObservableField<String> cardMode = new ObservableField<>("");

    // Current currency code
    public final ObservableField<String> currencyCode = new ObservableField<>("");

    // Event: Select Device
    // public final SingleLiveEvent<Void> selectDeviceEvent = new SingleLiveEvent<>();


    public final SingleLiveEvent<Void> selectBluetoothEvent = new SingleLiveEvent<>();

    public final SingleLiveEvent<Void> selectUartEvent = new SingleLiveEvent<>();

    public final SingleLiveEvent<Void> selectUsbEvent = new SingleLiveEvent<>();


    // Event: Transaction Type Click
    public final SingleLiveEvent<Void> transactionTypeClickEvent = new SingleLiveEvent<>();

    // Event: Card Mode Click
    public final SingleLiveEvent<Void> cardModeClickEvent = new SingleLiveEvent<>();

    // Event: Currency Code Click
    public final SingleLiveEvent<Void> currencyCodeClickEvent = new SingleLiveEvent<>();


    //isShowUSBImageView

    public final ObservableField<Boolean> isShowBluetoothImageView = new ObservableField<>(false);
    public final ObservableField<Boolean> isShowBluetoothTextView = new ObservableField<>(false);


    public final ObservableField<Boolean> isShowUartImageView = new ObservableField<>(false);
    public final ObservableField<Boolean> isShowUartTextView = new ObservableField<>(false);


    public final ObservableField<Boolean> isShowUSBImageView = new ObservableField<>(false);
    public final ObservableField<Boolean> isShowUsbTextView = new ObservableField<>(false);


    private TerminalApplication baseApplication;
    private POS_TYPE currentPOSType;

    public ConnectionSettingsViewModel(@NonNull Application application) {
        super(application);
        loadSettings();
        if (baseApplication == null) {
            baseApplication = (TerminalApplication) BaseApplication.getInstance();
        }
    }

    /**
     * Load settings from SharedReferences
     */
    public void loadSettings() {
        // Load device name
        String bluetoothName = SPUtils.getInstance().getString("bluetoothName");
        //bluetoothAddress
        String bluetoothAddress = SPUtils.getInstance().getString("bluetoothAddress");
        if (!TextUtils.isEmpty(bluetoothName) && !TextUtils.isEmpty(bluetoothAddress)) {
            isShowBluetoothImageView.set(true);
            isShowBluetoothTextView.set(true);
            deviceConnected.set(true);
            SPUtils.getInstance().put("deviceAddress", bluetoothAddress);
        }

        // Load transaction type
        String savedTransType = SPUtils.getInstance().getString("transactionType", "");
        if (savedTransType == null || "".equals(savedTransType)) {
            SPUtils.getInstance().put("transactionType", "GOODS");
            savedTransType = "GOODS";
        }
        transactionType.set(savedTransType);

        // Loading card mode
        String savedCardMode = SPUtils.getInstance().getString("cardMode", "");
        if (savedCardMode == null || "".equals(savedCardMode)) {
            if (DeviceUtils.isSmartDevices()) {
                SPUtils.getInstance().put("cardMode", "SWIPE_TAP_INSERT_CARD_NOTUP");
                savedCardMode = "SWIPE_TAP_INSERT_CARD_NOTUP";
            } else {
                SPUtils.getInstance().put("cardMode", "SWIPE_TAP_INSERT_CARD");
                savedCardMode = "SWIPE_TAP_INSERT_CARD";
            }
        }
        cardMode.set(savedCardMode);

        // Load currency code
        String savedCurrencyCode = SPUtils.getInstance().getString("currencyName", "");
        if (savedCurrencyCode == null || "".equals(savedCurrencyCode)) {
            SPUtils.getInstance().put("currencyCode", 156);
            savedCurrencyCode = "CNY";
        }
        currencyCode.set(savedCurrencyCode);
    }

    public BindingCommand selectBluetoothCommand = new BindingCommand(() -> {
        TRACE.d("selectBluetoothCommand XX");
        selectBluetoothEvent.call();
    });


    public BindingCommand selectUartCommand = new BindingCommand(() -> {
        TRACE.d("selectUartCommand XX");
        selectUartEvent.call();
    });


    public BindingCommand selectUSBCommand = new BindingCommand(() -> {
        TRACE.d("selectUSBCommand XX");
        selectUsbEvent.call();
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
}
