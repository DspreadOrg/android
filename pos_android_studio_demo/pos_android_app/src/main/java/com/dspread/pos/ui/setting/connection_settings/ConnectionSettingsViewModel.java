package com.dspread.pos.ui.setting.connection_settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dspread.pos.MyBaseApplication;
import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.posAPI.POSCommand;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.R;
import com.dspread.xpos.QPOSService;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class ConnectionSettingsViewModel extends BaseViewModel {
    // 当前连接的设备名称
    public final ObservableField<String> deviceName = new ObservableField<>("No device");
    
    // 设备连接状态
    public final ObservableBoolean deviceConnected = new ObservableBoolean(false);
    
    // 当前交易类型
    public final ObservableField<String> transactionType = new ObservableField<>("");
    
    // 当前卡片模式
    public final ObservableField<String> cardMode = new ObservableField<>("");
    
    // 当前货币代码
    public final ObservableField<String> currencyCode = new ObservableField<>("");
    
    // 事件：选择设备
    public final SingleLiveEvent<Void> selectDeviceEvent = new SingleLiveEvent<>();
    
    // 事件：交易类型点击
    public final SingleLiveEvent<Void> transactionTypeClickEvent = new SingleLiveEvent<>();
    
    // 事件：卡片模式点击
    public final SingleLiveEvent<Void> cardModeClickEvent = new SingleLiveEvent<>();
    
    // 事件：货币代码点击
    public final SingleLiveEvent<Void> currencyCodeClickEvent = new SingleLiveEvent<>();
    private MyBaseApplication baseApplication;
    private POS_TYPE currentPOSType;

    public ConnectionSettingsViewModel(@NonNull Application application) {
        super(application);
        loadSettings();
        if(baseApplication == null){
            baseApplication = (MyBaseApplication) BaseApplication.getInstance();
        }
    }

    /**
     * 从SharedPreferences加载设置
     */
    public void loadSettings() {
        // 加载设备连接状态
        deviceConnected.set(SPUtils.getInstance().getBoolean("isConnected", false));
        
        // 加载设备名称
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
        }
        
        // 加载交易类型
        String savedTransType = SPUtils.getInstance().getString("transactionType", "");
        if (savedTransType == null || "".equals(savedTransType)) {
            SPUtils.getInstance().put("transactionType","GOODS");
            savedTransType = "GOODS";
        }
        transactionType.set(savedTransType);
        
        // 加载卡片模式
        String savedCardMode = SPUtils.getInstance().getString("cardMode", "");
        if (savedCardMode == null || "".equals(savedCardMode)) {
            SPUtils.getInstance().put("cardMode","SWIPE_TAP_INSERT_CARD_NOTUP");
            savedCardMode = "SWIPE_TAP_INSERT_CARD_NOTUP";
        }
        cardMode.set(savedCardMode);
        
        // 加载货币代码
        String savedCurrencyCode = SPUtils.getInstance().getString("currencyName", "");
        if (savedCurrencyCode == null || "".equals(savedCurrencyCode)) {
            SPUtils.getInstance().put("currencyCode",156);
            savedCurrencyCode = "CNY";
        }
        currencyCode.set(savedCurrencyCode);
    }

    /**
     * 保存设置到SharedPreferences
     */
    public void saveSettings() {
        // 保存设备连接状态
        SPUtils.getInstance().put("isConnected", deviceConnected.get());
        if("".equals(deviceName)||"No device".equals(deviceName)) {
            // 保存设备名称
            SPUtils.getInstance().put("device_type", "");
        }else {
            if(deviceName.get().contains(POS_TYPE.BLUETOOTH.name())){
                SPUtils.getInstance().put("device_type", POS_TYPE.BLUETOOTH.name());
            }else {
                SPUtils.getInstance().put("device_type", deviceName.get());
            }
        }
    }
    public BindingCommand<Boolean> toggleDeviceCommand = new BindingCommand<>(new BindingConsumer<Boolean>() {
        @Override
        public void call(Boolean isChecked) {
            deviceConnected.set(isChecked);
            String deviceType = SPUtils.getInstance().getString("device_type", "");
            if (!"".equals(deviceType)) {
                if (isChecked) {
                    selectDeviceEvent.call();
                } else {
                    POSCommand.getInstance().close(currentPOSType);
                    SPUtils.getInstance().put("isConnectedAutoed",false);
                    updateDeviceName("No device");
                }
            }
            saveSettings();
        }
    });
//    /**
//     * 设备开关切换命令
//     */
//    public BindingCommand toggleDeviceCommand = new BindingCommand(() -> {
//        TRACE.i("is statrt click switch check == ");
//        boolean isConnectedAutoed = SPUtils.getInstance().getBoolean("isConnectedAutoed");
//        deviceConnected.set(!deviceConnected.get());
//         {
//
//            String deviceType = SPUtils.getInstance().getString("device_type", "");
//            if (deviceConnected.get() && !isConnectedAutoed) {
//                selectDeviceEvent.call();
//            } else {
//                if (!"".equals(deviceType)) {
//                    POSCommand.getInstance().close(DeviceUtils.getDevicePosType(deviceType));
//                }
//                SPUtils.getInstance().put("isConnectedAutoed",false);
//                updateDeviceName("No device");
//            }
//            saveSettings();
//        }
//    });

    /**
     * 选择设备命令
     */
    public BindingCommand selectDeviceCommand = new BindingCommand(() -> {
        selectDeviceEvent.call();
    });

    /**
     * 交易类型点击命令
     */
    public BindingCommand transactionTypeCommand = new BindingCommand(() -> {
        transactionTypeClickEvent.call();
    });

    /**
     * 卡片模式点击命令
     */
    public BindingCommand cardModeCommand = new BindingCommand(() -> {
        cardModeClickEvent.call();
    });

    /**
     * 货币代码点击命令
     */
    public BindingCommand currencyCodeCommand = new BindingCommand(() -> {
        currencyCodeClickEvent.call();
    });

    /**
     * 更新设备名称
     */
    public void updateDeviceName(String name) {
        deviceName.set(name);
        saveSettings();
    }
}
