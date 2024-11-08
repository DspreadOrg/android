package com.dspread.demoui.interfaces;

import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public interface TransactionCallback {
    void onRequestSetAmount();

    void onRequestWaitingUser();

    void onRequestTime();

    void onRequestSelectEmvApp(ArrayList<String> appList);

    void onQposRequestPinResult(List<String> dataList, int offlineTime);

    void onQposPinMapSyncResult(boolean isSuccess, boolean isNeedPin);

    void onRequestSetPin();

    void onReturnGetPinResult(Hashtable<String, String> result);

    void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData);

    void onRequestOnlineProcess(String tlv);

    void onRequestTransactionResult(QPOSService.TransactionResult transactionResult);

    void onRequestBatchData(String tlv);

    void onQposIsCardExist(boolean cardIsExist);

    void onRequestDisplay(QPOSService.Display displayMsg);

    void onReturnReversalData(String tlv);

    void onReturnGetPinInputResult(int num);

    void onGetCardNoResult(String cardNo);

    void onGetCardInfoResult(Hashtable<String, String> cardInfo);

    void onEmvICCExceptionData(String tlv);

    void onTradeCancelled();
}
