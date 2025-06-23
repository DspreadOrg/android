package com.dspread.pos.posAPI;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.common.enums.TransCardMode;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class POS {
    private static volatile POS instance;
    private QPOSService pos;
    private Context context;
    private BasePOSService listener;
    private POS(Context context) {
        this.context = context.getApplicationContext();
        this.listener = new BasePOSService();
    }

    public static void init(Context context) {
        getInstance(context);
    }

    public static POS getInstance(Context context) {
        if (instance == null) {
            synchronized (POS.class) {
                if (instance == null) {
                    instance = new POS(context);
                }
            }
        }
        return instance;
    }

    public static POS getInstance() {
        if (instance == null) {
            throw new IllegalStateException("POS must be initialized with context first");
        }
        return instance;
    }

    public void open(QPOSService.CommunicationMode mode) {
        pos = QPOSService.getInstance(context, mode);
        if (pos == null) {
            return;
        }

        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
        pos.setContext(context);
        pos.initListener(listener);
    }

    public QPOSService getQPOSService() {
        return pos;
    }

    public void clearPosService(){
        pos = null;
    }

    public boolean isPOSReady() {
        return pos != null;
    }

    public void setDeviceAddress(String address){
        pos.setDeviceAddress(address);
    }

    public void openUart(){
        pos.openUart();
    }

    public void openUsb(UsbDevice device){
        pos.openUsb(device);
    }

    public void scanQPos2Mode(Context context, long time){
        pos.scanQPos2Mode(context,time);
    }
    public void stopScanQPos2Mode(){
        pos.stopScanQPos2Mode();
    }

    public void stopScanQposBLE(){
        pos.stopScanQposBLE();
    }

    public void connectBLE(String deviceAddress){
        pos.connectBLE(deviceAddress);
    }

    public void connectBluetoothDevice(boolean isAutoBind, int time, String deviceAddress){
        pos.connectBluetoothDevice(isAutoBind,time,deviceAddress);
    }
    public void setCardTradeMode(){
        String modeName = SPUtils.getInstance().getString("cardMode","");
        if(modeName.isEmpty()){
            if(DeviceUtils.isSmartDevices()){
                pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
            }else {
                pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD);
            }
        }else {
            QPOSService.CardTradeMode mode = TransCardMode.valueOf(modeName).getCardTradeModeValue();
            TRACE.i("card mode = " + mode);
            pos.setCardTradeMode(mode);
        }
    }
    public void doTrade(int timeout){
        pos.doTrade(timeout);
    }
    
    public void setAmount(String amount, String cashbackAmounts, String currencyCode, QPOSService.TransactionType transactionType){
        pos.setAmount(amount, cashbackAmounts, currencyCode, transactionType);
    }
    
    public void sendTime(String terminalTime){
        pos.sendTime(terminalTime);
    }
    
    public void selectEmvApp(int position){
        pos.selectEmvApp(position);
    }
    
    public void cancelSelectEmvApp(){
        pos.cancelSelectEmvApp();
    }
    
    public void pinMapSync(String value, int timeout){
        pos.pinMapSync(value, timeout);
    }
    
    public void cancelPin(){
        pos.cancelPin();
    }

    public boolean isOnlinePin(){
        return pos.isOnlinePin();
    }

    public int getCvmPinTryLimit(){
        return pos.getCvmPinTryLimit();
    }
    
    public void bypassPin(){
        pos.sendPin("".getBytes());
    }
    
    public void sendCvmPin(String pinBlock, boolean isEncrypted){
        pos.sendCvmPin(pinBlock, isEncrypted);
    }

    public Hashtable<String,String> getEncryptData(){
        return pos.getEncryptData();
    }

    public Hashtable<String, String> getNFCBatchData(){
        return pos.getNFCBatchData();
    }

    public void doEmvApp(QPOSService.EmvOption option){
        pos.doEmvApp(option);
    }
    
    public void sendOnlineProcessResult(String tlv){
        pos.sendOnlineProcessResult(tlv);
    }
    
    public void cancelTrade(){
        pos.cancelTrade();
    }

    public Hashtable<String, String> anlysEmvIccData(String tlv){
        return pos.anlysEmvIccData(tlv);
    }

    public void close(POS_TYPE posType) {
        TRACE.d("start close");
        if (pos == null || posType == null) {
            TRACE.d("return close");
        } else if (posType == POS_TYPE.BLUETOOTH) {
            pos.disconnectBT();
        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            pos.disconnectBLE();
        } else if (posType == POS_TYPE.UART) {
            pos.closeUart();
        } else if (posType == POS_TYPE.USB) {
            pos.closeUsb();
        }else {
            pos.disconnectBT();
        }
    }
}
