package com.dspread.pos.ui.setting.device_selection;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.dspread.pos.MyBaseApplication;
import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.posAPI.POSCommand;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos.utils.USBClass;
import com.dspread.pos_new_android_app.R;
import com.dspread.xpos.QPOSService;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class DeviceSelectionViewModel extends BaseViewModel {
    // 当前选中的连接方式
    public final ObservableField<String> selectedConnectionMethod = new ObservableField<>();
    public final ObservableField<String> connectBtnTitle = new ObservableField<>("Connect");
    public final ObservableField<String> bluetoothAddress = new ObservableField<>();
    public final ObservableField<String> bluetoothName = new ObservableField<>();
    public final ObservableField<Boolean> isConnecting = new ObservableField<>(false);

    // 连接方式选项
    public final String[] connectionMethods = {"BLUETOOTH", "UART", "USB"};

    // 连接方式对应的POS_TYPE
    public final POS_TYPE[] posTypes = {POS_TYPE.BLUETOOTH, POS_TYPE.UART, POS_TYPE.USB};

    // 事件：连接方式选择完成
    public final SingleLiveEvent<POS_TYPE> connectionMethodSelectedEvent = new SingleLiveEvent<>();

    public final SingleLiveEvent<POS_TYPE> startScanBluetoothEvent = new SingleLiveEvent<>();

    // 当前选中的连接方式索引
    public final MutableLiveData<Integer> selectedIndex = new MutableLiveData<>(-1);
    public String connectedDeviceName;
    private MyBaseApplication myBaseApplication;
    public POS_TYPE currentPOSType;

    public DeviceSelectionViewModel(@NonNull Application application) {
        super(application);
        if(myBaseApplication == null){
            myBaseApplication = (MyBaseApplication) BaseApplication.getInstance();
        }
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
     * 加载已选择的连接方式
     */
    private void loadSelectedConnectionMethod(String savedConnectionType) {
        // 根据保存的连接类型设置选中项
        for (int i = 0; i < posTypes.length; i++) {
            if (posTypes[i].name().equals(savedConnectionType)) {
                selectedIndex.setValue(i);
                selectedConnectionMethod.set(connectionMethods[i]);
                break;
            }
        }
    }

    /**
     * 连接方式选择命令
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
                myBaseApplication.open(QPOSService.CommunicationMode.BLUETOOTH, getApplication());
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
        POSCommand.getInstance().scanQPos2Mode(getApplication(),20);
    }

    public void stopScanBluetooth(){
        POSCommand.getInstance().stopScanQPos2Mode();
    }

    /**
     * 确认选择命令
     */
    public BindingCommand confirmSelectionCommand = new BindingCommand(() -> {
        Integer index = selectedIndex.getValue();
        if (index != null && index >= 0 && index < connectionMethods.length && !getApplication().getString(R.string.disconnect).equals(connectBtnTitle.get())) {
            // 触发选择完成事件
            isConnecting.set(true);
            if(!"".equals(SPUtils.getInstance().getString("device_type"))){
                TRACE.i("currentPOSType = "+currentPOSType);
                POSCommand.getInstance().close(currentPOSType);
            }
            openDevice(posTypes[index]);
        } else if(getApplication().getString(R.string.disconnect).equals(connectBtnTitle.get())){
            POSCommand.getInstance().close(currentPOSType);
        }else {
            ToastUtils.showShort("Pls choose one connection method!");
        }
    });

    public void openDevice(POS_TYPE posType){
        if(posType == POS_TYPE.USB){
            openUSBDevice();
        }else if(posType == POS_TYPE.UART){
            myBaseApplication.open(QPOSService.CommunicationMode.UART, getApplication());
            POSCommand.getInstance().setDeviceAddress("/dev/ttyS1");
            POSCommand.getInstance().openUart();
        }else {
            connectBluetooth(posType, bluetoothAddress.get());
        }
    }

    private void openUSBDevice() {
        USBClass usb = new USBClass();
        ArrayList<String> deviceList = usb.GetUSBDevices(getApplication());

        if (deviceList == null) {
            Toast.makeText(getApplication(), "No Permission", Toast.LENGTH_SHORT).show();
            return;
        }
        final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);
        if (items.length == 1) {
            String selectedDevice = (String) items[0];
            UsbDevice usbDevice = USBClass.getMdevices().get(selectedDevice);
            myBaseApplication.open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM, getApplication());
            POSCommand.getInstance().openUsb(usbDevice);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getApplication());
            builder.setTitle("Select a Reader");
            if (items.length == 0) {
                builder.setMessage(getApplication().getString(R.string.setting_disusb));
                builder.setPositiveButton(getApplication().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items.length > item) {
                        String selectedDevice = items[item].toString();
                        dialog.dismiss();
                        UsbDevice usbDevice = USBClass.getMdevices().get(selectedDevice);
                        myBaseApplication.open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM, getApplication());
                        POSCommand.getInstance().openUsb(usbDevice);
                    }
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    /**
     * 连接蓝牙设备
     */
    public void connectBluetooth(POS_TYPE posType, String blueTootchAddress){
        if (posType == null || blueTootchAddress == null) {
            TRACE.d("return close");
        } else if (posType == POS_TYPE.BLUETOOTH) {
            POSCommand.getInstance().stopScanQPos2Mode();
            POSCommand.getInstance().connectBluetoothDevice(true, 25, blueTootchAddress);
        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            POSCommand.getInstance().stopScanQposBLE();
            POSCommand.getInstance().connectBLE(blueTootchAddress);
        }
    }

}
