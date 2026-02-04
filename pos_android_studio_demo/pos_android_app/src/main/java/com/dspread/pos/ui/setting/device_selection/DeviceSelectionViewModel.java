package com.dspread.pos.ui.setting.device_selection;

import android.app.Application;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.TRACE;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;

public class DeviceSelectionViewModel extends BaseViewModel {
    // The currently selected connection method
    // public final ObservableField<String> selectedConnectionMethod = new ObservableField<>();
    // public final ObservableField<String> connectBtnTitle = new ObservableField<>("Connect");
    // public final ObservableField<String> bluetoothAddress = new ObservableField<>();
    // public final ObservableField<String> bluetoothName = new ObservableField<>();
    public final ObservableField<Boolean> isConnecting = new ObservableField<>(false);
    //isShowDeviceSelectionList

    public final ObservableField<Boolean> isShowDeviceSelectionList = new ObservableField<>(false);



    //isShowDeviceSelectedView
    public final ObservableField<Boolean> isShowDeviceSelectedView = new ObservableField<>(false);


    // Connection method options
    public final String[] connectionMethods = {"BLUETOOTH", "UART", "USB"};

    // POS_TTYPE corresponding to the connection method
    public final POS_TYPE[] posTypes = {POS_TYPE.BLUETOOTH, POS_TYPE.UART, POS_TYPE.USB};

    public final SingleLiveEvent<POS_TYPE> startScanBluetoothEvent = new SingleLiveEvent<>();

    public SingleLiveEvent<Void> showUsbDeviceDialogEvent = new SingleLiveEvent<>();

    public final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>(-1);
    public String connectedDeviceName;
    public POS_TYPE currentPOSType;


    public DeviceSelectionViewModel(@NonNull Application application) {
        super(application);
        if (!"".equals(SPUtils.getInstance().getString("device_type"))) {
            connectedDeviceName = SPUtils.getInstance().getString("device_type");
            if (connectedDeviceName.equals(POS_TYPE.UART.name())) {
                currentPOSType = POS_TYPE.UART;
            } else if (connectedDeviceName.equals(POS_TYPE.USB.name())) {
                currentPOSType = POS_TYPE.USB;
            } else if (connectedDeviceName.contains(POS_TYPE.BLUETOOTH.name())) {
                currentPOSType = POS_TYPE.BLUETOOTH;
            }
            loadSelectedConnectionMethod(connectedDeviceName);
        }
    }


    /**
     * Load the selected connection method
     */
    private void loadSelectedConnectionMethod(String savedConnectionType) {
        // Set the selection based on the saved connection type
        for (int i = 0; i < posTypes.length; i++) {
            if (posTypes[i].name().equals(savedConnectionType)) {
                selectedIndex.setValue(i);
                //selectedConnectionMethod.set(connectionMethods[i]);
                break;
            }
        }
    }

    /**
     * Connection method selection command
     */
/*
    public BindingCommand<String> connectionMethodRadioSelectedCommand = new BindingCommand<>(radioText -> {
        TRACE.i("radio btn selected =" + radioText);
        {
            POSManager.getInstance().close();
            if (connectionMethods[0].equals(radioText)) {
                selectedIndex.setValue(0);
                startScanBluetoothEvent.setValue(POS_TYPE.BLUETOOTH);
                SPUtils.getInstance().put("device_type", "BLUETOOTH");
            } else if (connectionMethods[1].equals(radioText)) {
                selectedIndex.setValue(1);
                SPUtils.getInstance().put("device_type", "UART");
                finish();
            } else if (connectionMethods[2].equals(radioText)) {
                selectedIndex.setValue(2);
                showUsbDeviceDialogEvent.call();
                setShowDeviceSelectionList(false); // Hide Bluetooth list view
                SPUtils.getInstance().put("device_type", "USB");
            } else {
                selectedIndex.setValue(-1);
            }
        }
    });
*/

    public void setShowDeviceSelectionList(boolean isShow) {
        isShowDeviceSelectionList.set(isShow);
    }

}
