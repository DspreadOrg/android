package com.dspread.pos.posAPI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.common.enums.TransCardMode;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.FileUtils;
import com.dspread.pos.utils.HandleTxnsResultUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos.utils.USBClass;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import me.goldze.mvvmhabit.utils.SPUtils;

public class POSManager {
    private static volatile POSManager instance;
    private QPOSService pos;
    private Context context;
    private QPOSServiceListener listener;
    // Callback management
    private final List<ConnectionServiceCallback> connectionCallbacks = new CopyOnWriteArrayList<>();
    private final List<PaymentServiceCallback> transactionCallbacks = new CopyOnWriteArrayList<>();
    private Handler mainHandler;
    private CountDownLatch connectLatch;
    private PaymentResult paymentResult;
    private POS_TYPE posType;
    private boolean isICC;

    private POSManager(Context context) {
        this.context = context.getApplicationContext();
        this.listener = new QPOSServiceListener();
        mainHandler = new Handler(Looper.getMainLooper());
        paymentResult = new PaymentResult();
    }

    /**
     * Initialize POSManager with application context
     * @param context Application context
     */
    public static void init(Context context) {
        getInstance(context);
    }

    public static POSManager getInstance(Context context) {
        if (instance == null) {
            synchronized (POSManager.class) {
                if (instance == null) {
                    instance = new POSManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Get singleton instance of POSManager
     * @return POSManager instance
     */
    public static POSManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("POS must be initialized with context first");
        }
        return instance;
    }

    /**
     * Connect to POS device
     * @param deviceAddress Device address (Bluetooth address or USB port)
     * @param callback Callback to handle connection events
     */
    public void connect(String deviceAddress, ConnectionServiceCallback callback) {
        connectLatch = new CountDownLatch(1);
        registerConnectionCallback(callback);

        // start connect
        connect(deviceAddress);
        try {
            boolean waitSuccess = connectLatch.await(5, TimeUnit.SECONDS);
            if (!waitSuccess) {
                TRACE.i("Connection timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Connection interrupted", e);
        }

    }

    public void connect(String deviceAddress){
            if(!deviceAddress.isEmpty()){
                if(deviceAddress.contains(":")){
                    posType = POS_TYPE.BLUETOOTH;
                    initMode(QPOSService.CommunicationMode.BLUETOOTH);
                    pos.setDeviceAddress(deviceAddress);
                    pos.connectBluetoothDevice(true, 25, deviceAddress);
                }else {
                    posType = POS_TYPE.USB;
                    UsbDevice usbDevice = USBClass.getMdevices().get(deviceAddress);
                    initMode(QPOSService.CommunicationMode.USB);
                    pos.openUsb(usbDevice);
                }
            }else {
                posType = POS_TYPE.UART;
                initMode(QPOSService.CommunicationMode.UART);
                pos.openUart();
            }
    }

    public void initMode(QPOSService.CommunicationMode mode) {
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


    public void setICC(boolean ICC) {
        isICC = ICC;
    }

    /**
     * Check if device is ready for transaction
     * @return true if device is connected and ready
     */
    public boolean isDeviceReady() {
        return pos != null;
    }

    public void setDeviceAddress(String address){
        pos.setDeviceAddress(address);
    }

    public QPOSService.TransactionType getTransType(){
        String transactionTypeString = SPUtils.getInstance().getString("transactionType","");
        if (transactionTypeString.isEmpty()) {
            transactionTypeString = "GOODS";
        }
        return HandleTxnsResultUtils.getTransactionType(transactionTypeString);
    }
    public QPOSService.CardTradeMode getCardTradeMode(){
        String modeName = SPUtils.getInstance().getString("cardMode","");
        QPOSService.CardTradeMode cardTradeMode;
        if(modeName.isEmpty()){
            if(DeviceUtils.isSmartDevices()){
//                pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                cardTradeMode = QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP;
            }else {
                cardTradeMode = QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD;
            }
        }else {
            cardTradeMode = TransCardMode.valueOf(modeName).getCardTradeModeValue();
        }
        return cardTradeMode;
    }

    /**
     * Start a payment transaction
     * @param amount Transaction amount
     * @param callback Callback to handle payment events
     */
    public void startTransaction(String amount,PaymentServiceCallback callback){
        if(!isDeviceReady()){
            return;
        }
        getDeviceId();
        if(callback != null) {
            registerPaymentCallback(callback);
        }

        int currencyCode = SPUtils.getInstance().getInt("currencyCode",156);
        if(pos !=null) {
            pos.setCardTradeMode(getCardTradeMode());
            pos.setAmount(amount, "", String.valueOf(currencyCode), getTransType());
            pos.doTrade(60);
        }
    }

    public void getDeviceId(){
        if(pos !=null) {
            Hashtable<String, Object> posIdTable = pos.syncGetQposId(5);
            String posId = posIdTable.get("posId") == null ? "" : (String) posIdTable.get("posId");
            SPUtils.getInstance().put("posID", posId);
            TRACE.i("posid :" + SPUtils.getInstance().getString("posID"));
        }
    }

    /**
     * Cancel ongoing transaction
     */
    public void cancelTransaction(){
        if(pos != null){
            pos.cancelTrade();
        }
    }
    
    public void sendTime(String terminalTime){
        pos.sendTime(terminalTime);
    }
    
    public void selectEmvApp(int position){
        pos.selectEmvApp(position);
    }
    
    public void cancelSelectEmvApp(){
        if(pos!=null) {
            pos.cancelSelectEmvApp();
        }
    }
    
    public void pinMapSync(String value, int timeout){
        if(pos!=null) {
            pos.pinMapSync(value, timeout);
        }
    }
    
    public void cancelPin(){
        if(pos!=null) {
            pos.cancelPin();
        }
    }

    public boolean isOnlinePin(){
        return pos.isOnlinePin();
    }

    public int getCvmPinTryLimit(){
        return pos.getCvmPinTryLimit();
    }
    
    public void bypassPin(){
        if (pos!=null) {
            pos.sendPin("".getBytes());
        }
    }
    
    public void sendCvmPin(String pinBlock, boolean isEncrypted){
        if(pos!=null) {
            pos.sendCvmPin(pinBlock, isEncrypted);
        }
    }

    public Hashtable<String,String> getEncryptData(){
        return pos.getEncryptData();
    }

    public Hashtable<String, String> getNFCBatchData(){
        return pos.getNFCBatchData();
    }
    
    public void sendOnlineProcessResult(String tlv){
        if(pos!=null) {
            pos.sendOnlineProcessResult(tlv);
        }
    }

    public Hashtable<String, String> anlysEmvIccData(String tlv){
        return pos.anlysEmvIccData(tlv);
    }

    public void updateEMVConfig(String fileName){
        //ex: emv_profile_tlv.xml
        pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine(fileName,context)));
    }

    public void updateDeviceFirmware(Activity activity, String blueTootchAddress){
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
        } else {
            byte[] data = FileUtils.readAssetsLine("CR100_master.asc", activity);
            if(data != null){
                int updateResult = pos.updatePosFirmware(data, blueTootchAddress);
                if(updateResult== -1){
                    Toast.makeText(activity, "please keep the device charging", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void close() {
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
        SPUtils.getInstance().put("deviceAddress","");
        clearPosService();
    }

    /**
     * register payment callback
     */
    public void registerPaymentCallback(PaymentServiceCallback callback) {
        if (callback != null && !transactionCallbacks.contains(callback)) {
            transactionCallbacks.add(callback);
        }
    }

    /**
     * register connection service callback
     */
    public void registerConnectionCallback(ConnectionServiceCallback callback) {
        if (callback != null && !connectionCallbacks.contains(callback)) {
            connectionCallbacks.add(callback);
        }
    }

    public void unregisterCallbacks() {
        connectionCallbacks.clear();
        transactionCallbacks.clear();
    }

    private void notifyConnectionCallbacks(CallbackAction<ConnectionServiceCallback> action) {
        mainHandler.post(() -> {
            for (ConnectionServiceCallback  callback : connectionCallbacks) {
                try {
                    action.execute(callback);
                } catch (Exception e) {
                    TRACE.e("Error in connection callback: " + e.getMessage());
                }
            }
        });
    }

    private void notifyTransactionCallbacks(CallbackAction<PaymentServiceCallback> action) {
        mainHandler.post(() -> {
            for (PaymentServiceCallback callback : transactionCallbacks) {
                try {
                    action.execute(callback);
                } catch (Exception e) {
                    TRACE.e("Error in transaction callback: " + e.getMessage());
                }
            }
        });
    }

    @FunctionalInterface
    private interface CallbackAction<T> {
        void execute(T callback) throws Exception;
    }

    private class QPOSServiceListener extends CQPOSService {

        @Override
        public void onRequestQposConnected() {
            connectLatch.countDown();
            SPUtils.getInstance().put("device_type",posType.name());
            SPUtils.getInstance().put("isConnected",true);
            notifyConnectionCallbacks(cb -> cb.onRequestQposConnected());
        }

        @Override
        public void onRequestQposDisconnected() {
            SPUtils.getInstance().put("isConnected",false);
            SPUtils.getInstance().put("device_type","");
            clearPosService();
            connectLatch.countDown();
            notifyConnectionCallbacks(cb -> cb.onRequestQposDisconnected());
        }

        @Override
        public void onRequestNoQposDetected() {
            SPUtils.getInstance().put("isConnected",false);
            SPUtils.getInstance().put("device_type","");
            clearPosService();
            connectLatch.countDown();
            notifyConnectionCallbacks(cb -> cb.onRequestNoQposDetected());
        }

        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            // Handle ICC card for EMV processing
            setICC(false);
            if (result == QPOSService.DoTradeResult.ICC) {
                setICC(true);
                notifyTransactionCallbacks(cb -> cb.onReturnCardInserted());
                paymentResult.setTransactionType(result.name());
                if (pos != null) {
                    pos.doEmvApp(QPOSService.EmvOption.START);
                }

            }else if(result == QPOSService.DoTradeResult.NFC_OFFLINE || result == QPOSService.DoTradeResult.NFC_ONLINE ||result == QPOSService.DoTradeResult.MCR){
                paymentResult = HandleTxnsResultUtils.handleTransactionResult(paymentResult,decodeData);
                paymentResult.setTransactionType(result.name());
                notifyTransactionCallbacks(cb -> cb.onTransactionCompleted(paymentResult));
            } else {
                String msg = HandleTxnsResultUtils.getTradeResultMessage(result,context);
                notifyTransactionCallbacks(cb -> cb.onTransactionFailed(msg, null));
            }
        }

        @Override
        public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
            String msg = HandleTxnsResultUtils.getTransactionResultMessage(transactionResult, context);
            paymentResult.setStatus(msg);
            if (!msg.isEmpty()) {
                notifyTransactionCallbacks(cb -> cb.onTransactionFailed(msg,null));
            }else {
                notifyTransactionCallbacks(cb -> cb.onTransactionResult(paymentResult));
            }

        }

        @Override
        public void onRequestWaitingUser() {
            notifyTransactionCallbacks(cb -> cb.onRequestWaitingUser());
        }

        @Override
        public void onRequestTime() {
            notifyTransactionCallbacks(cb -> cb.onRequestTime());
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            notifyTransactionCallbacks(cb -> cb.onRequestSelectEmvApp(appList));
        }

        @Override
        public void onRequestOnlineProcess(String tlv) {
            notifyTransactionCallbacks(cb -> cb.onRequestOnlineProcess(tlv));
        }

        @Override
        public void onRequestBatchData(String tlv) {
            paymentResult.setTlv(tlv);
            notifyTransactionCallbacks(cb -> cb.onTransactionCompleted(paymentResult));
        }

        @Override
        public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
            notifyTransactionCallbacks(cb -> cb.onRequestSetPin(isOfflinePin, tryNum));
        }

        @Override
        public void onRequestDisplay(QPOSService.Display displayMsg) {
            TRACE.i("parent onRequestDisplay");
            notifyTransactionCallbacks(cb -> cb.onRequestDisplay(displayMsg));
        }

        @Override
        public void onError(QPOSService.Error errorState) {
            notifyTransactionCallbacks(cb -> cb.onTransactionFailed(errorState.name(),null));
        }

        @Override
        public void onReturnReversalData(String tlv) {
            paymentResult.setTlv(tlv);
            notifyTransactionCallbacks(cb -> cb.onTransactionCompleted(paymentResult));
        }

        @Override
        public void onEmvICCExceptionData(String tlv) {
            notifyTransactionCallbacks(cb -> cb.onTransactionFailed("Decline",tlv));
        }

        @Override
        public void onGetCardInfoResult(Hashtable<String, String> cardInfo) {
            notifyTransactionCallbacks(cb -> cb.onGetCardInfoResult(cardInfo));
        }

        @Override
        public void onRequestSetPin() {
            notifyTransactionCallbacks(cb -> cb.onRequestSetPin());
        }

        @Override
        public void onReturnGetPinInputResult(int num) {
            notifyTransactionCallbacks(cb -> cb.onReturnGetPinInputResult(num));
        }

        @Override
        public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
            notifyTransactionCallbacks(cb -> cb.onQposRequestPinResult(dataList,offlineTime));
        }

        @Override
        public void onTradeCancelled() {
            notifyTransactionCallbacks(cb -> cb.onTransactionFailed("Cancel",null));
        }
    }

}
