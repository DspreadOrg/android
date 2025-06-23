package com.dspread.pos.ui.setting.device_selection;

import android.app.Application;
import android.hardware.usb.UsbDevice;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.dspread.pos.TerminalApplication;
import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.posAPI.POS;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class DeviceSelectionViewModel extends BaseViewModel {
    // The currently selected connection method
    public final ObservableField<String> selectedConnectionMethod = new ObservableField<>();
    public final ObservableField<String> connectBtnTitle = new ObservableField<>("Connect");
    public final ObservableField<String> bluetoothAddress = new ObservableField<>();
    public final ObservableField<String> bluetoothName = new ObservableField<>();
    public final ObservableField<Boolean> isConnecting = new ObservableField<>(false);
    public SingleLiveEvent<Void> showUsbDeviceDialogEvent = new SingleLiveEvent<>();

    // Connection method options
    public final String[] connectionMethods = {"BLUETOOTH", "UART", "USB"};

    // POS_TTYPE corresponding to the connection method
    public final POS_TYPE[] posTypes = {POS_TYPE.BLUETOOTH, POS_TYPE.UART, POS_TYPE.USB};

    // Event: Connection method selection completed
    public final SingleLiveEvent<POS_TYPE> connectionMethodSelectedEvent = new SingleLiveEvent<>();

    public final SingleLiveEvent<POS_TYPE> startScanBluetoothEvent = new SingleLiveEvent<>();

    // Index of the currently selected connection method
    public final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>(-1);
    public String connectedDeviceName;
    public POS_TYPE currentPOSType;

    public DeviceSelectionViewModel(@NonNull Application application) {
        super(application);
        if(!"".equals(SPUtils.getInstance().getString("device_type"))){
            connectedDeviceName = SPUtils.getInstance().getString("device_type");
            if(connectedDeviceName.equals(POS_TYPE.UART.name())){
                currentPOSType = POS_TYPE.UART;
            }else if(connectedDeviceName.equals(POS_TYPE.USB.name())){
                currentPOSType = POS_TYPE.USB;
            }else if(connectedDeviceName.contains(POS_TYPE.BLUETOOTH.name())){
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
                selectedConnectionMethod.set(connectionMethods[i]);
                break;
            }
        }
    }

    /**
     * Connection method selection command
     */
    public BindingCommand<String> connectionMethodRadioSelectedCommand = new BindingCommand<>(radioText -> {
        TRACE.i("radio btn selected ="+radioText);
        if(connectedDeviceName != null && connectedDeviceName.equals(radioText)){
            connectBtnTitle.set(getApplication().getString(R.string.disconnect));
        } else{
            connectBtnTitle.set(getApplication().getString(R.string.str_connect));
            if(connectionMethods[0].equals(radioText)){
                selectedIndex.setValue(0);
                if(currentPOSType != null && currentPOSType == posTypes[selectedIndex.getValue()]){
                    return;
                }
                POS.getInstance().open(QPOSService.CommunicationMode.BLUETOOTH);
                startScanBluetoothEvent.setValue(POS_TYPE.BLUETOOTH);
            }else if(connectionMethods[1].equals(radioText)){
                selectedIndex.setValue(1);
            }else  if(connectionMethods[2].equals(radioText)){
                selectedIndex.setValue(2);
            }else {
                selectedIndex.setValue(-1);
            }
        }
    });

    public void startScanBluetooth(){
        POS.getInstance().scanQPos2Mode(getApplication(),20);
    }

    public void stopScanBluetooth(){
        POS.getInstance().stopScanQPos2Mode();
    }

    /**
     * Confirm the selection command
     */
    public BindingCommand confirmSelectionCommand = new BindingCommand(() -> {
        Integer index = selectedIndex.getValue();
        if (index != null && index >= 0 && index < connectionMethods.length && !getApplication().getString(R.string.disconnect).equals(connectBtnTitle.get())) {
            // Trigger selection completion event
            isConnecting.set(true);
            if(!"".equals(SPUtils.getInstance().getString("device_type"))){
                TRACE.i("currentPOSType = "+currentPOSType);
                POS.getInstance().close(currentPOSType);
            }
            openDevice(posTypes[index]);
        } else if(getApplication().getString(R.string.disconnect).equals(connectBtnTitle.get())){
            POS.getInstance().close(currentPOSType);
            POS.getInstance().clearPosService();
        }else {
            ToastUtils.showShort("Pls choose one connection method!");
        }
    });

    public void openDevice(POS_TYPE posType){
        if(posType == POS_TYPE.USB){
            openUSBDeviceEvent();
        }else if(posType == POS_TYPE.UART){
            POS.getInstance().open(QPOSService.CommunicationMode.UART);
            POS.getInstance().openUart();
        }else {
            connectBluetooth(posType, bluetoothAddress.get());
        }
    }

    private void openUSBDeviceEvent() {
        showUsbDeviceDialogEvent.call();
    }

    public void openUSBDevice(UsbDevice usbDevice) {
        POS.getInstance().open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
        POS.getInstance().openUsb(usbDevice);
    }

    /**
     * Connect Bluetooth devices
     */
    public void connectBluetooth(POS_TYPE posType, String blueTootchAddress){
        if (posType == null || blueTootchAddress == null) {
            TRACE.d("return close");
        } else if (posType == POS_TYPE.BLUETOOTH) {
            POS.getInstance().stopScanQPos2Mode();
            POS.getInstance().setDeviceAddress(blueTootchAddress);
            POS.getInstance().connectBluetoothDevice(true, 25, blueTootchAddress);
        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            POS.getInstance().stopScanQposBLE();
            POS.getInstance().connectBLE(blueTootchAddress);
        }
    }

}
