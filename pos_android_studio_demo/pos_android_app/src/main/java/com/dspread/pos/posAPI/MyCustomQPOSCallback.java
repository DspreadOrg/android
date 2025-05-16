package com.dspread.pos.posAPI;

import android.bluetooth.BluetoothDevice;

import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public interface MyCustomQPOSCallback extends BaseQPOSCallback {
//    transaction callback
    default  void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {}
    default  void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {}

    default void onRequestSetAmount(){}

    default void onRequestWaitingUser(){}

    default void onRequestTime(){}

    default void onRequestSelectEmvApp(ArrayList<String> appList){}

    default void onQposRequestPinResult(List<String> dataList, int offlineTime){}

    default void onQposPinMapSyncResult(boolean isSuccess, boolean isNeedPin){}

    default void onRequestSetPin(boolean isOfflinePin, int tryNum){}
    default void onRequestSetPin(){}

    default void onReturnGetPinResult(Hashtable<String, String> result){}

//    default void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData){}

    default void onRequestOnlineProcess(String tlv){}

//    default void onRequestTransactionResult(QPOSService.TransactionResult transactionResult){}

    default void onRequestBatchData(String tlv){}

    default void onQposIsCardExist(boolean cardIsExist){}

    default void onRequestDisplay(QPOSService.Display displayMsg){}

    default void onReturnReversalData(String tlv){}

    default void onReturnGetPinInputResult(int num){}

    default void onGetCardNoResult(String cardNo){}

    default void onGetCardInfoResult(Hashtable<String, String> cardInfo){}

    default void onEmvICCExceptionData(String tlv){}

    default void onTradeCancelled(){}

//    connection callback
    default void onDeviceFound(BluetoothDevice device) {}
    default void onRequestDeviceScanFinished() {}
    default void onRequestNoQposDetected() {}
    default void onRequestQposConnected() {}
    default void onRequestQposDisconnected() {}

//    mifare card callback
    default void onReturnNFCApduResult(boolean result, String apdu, int code) {}
    default void onReturnPowerOffNFCResult(boolean result) {}
    default void onReturnPowerOnNFCResult(boolean result, String data1, String data2, int code) {}
    default void onSearchMifareCardResult(Hashtable<String, String> result) {}
    default void onFinishMifareCardResult(boolean result) {}
    default void onVerifyMifareCardResult(boolean result) {}
    default void onReadMifareCardResult(Hashtable<String, String> result) {}
    default void onWriteMifareCardResult(boolean result) {}
    default void onOperateMifareCardResult(Hashtable<String, String> result) {}

//    posinfo callback
    default void onQposInfoResult(Hashtable<String, String> posInfoData) {}
    default void onQposIdResult(Hashtable<String, String> posIdTable) {}
    default void onRequestUpdateKey(String result) {}
    default void onGetKeyCheckValue(Hashtable<String, String> checkValue) {}

//    pos update callback
    default void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {}
    default void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result, Hashtable<String, String> checkValue) {}
    default void onReturnCustomConfigResult(boolean isSuccess, String result) {}
    default void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult result) {}
    default void onReturnSetMasterKeyResult(boolean isSuccess) {}
    default void onReturnSetMasterKeyResult(boolean isSuccess, Hashtable<String, String> result) {}
    default void onReturnUpdateIPEKResult(boolean result) {}
}
