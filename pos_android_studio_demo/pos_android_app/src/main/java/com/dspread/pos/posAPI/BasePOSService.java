package com.dspread.pos.posAPI;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.dspread.pos.common.annotations.CallbackChange;
import com.dspread.pos.common.manager.QPOSCallbackManager;
import com.dspread.pos.utils.TRACE;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import me.goldze.mvvmhabit.utils.SPUtils;

public class BasePOSService extends CQPOSService {
    private final QPOSCallbackManager callbackManager = QPOSCallbackManager.getInstance();

    @CallbackChange(
        description = "The callback is in child thread",
        type = CallbackChange.ChangeType.MODIFIED
    )
    @Override
    public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onDoTradeResult(result, decodeData);
        }
    }

    @CallbackChange(
        description = "Error callback will now notify all registered listeners",
        type = CallbackChange.ChangeType.MODIFIED
    )
    @Override
    public void onError(QPOSService.Error errorState) {
        PaymentServiceCallback paymentCallback = callbackManager.getPaymentCallback();
        if (paymentCallback != null) {
            paymentCallback.onError(errorState);
        }
    }

    @Override
    public void onQposInfoResult(Hashtable<String, String> posInfoData) {
    }

    @Override
    public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
        TRACE.d("parent onRequestTransactionResult()" + transactionResult.toString());
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestTransactionResult(transactionResult);
        }
    }

    @Override
    public void onRequestBatchData(String tlv) {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestBatchData(tlv);
        }
    }

    @Override
    public void onQposIdResult(Hashtable<String, String> posIdTable) {

    }

    @Override
    public void onRequestSelectEmvApp(ArrayList<String> appList) {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestSelectEmvApp(appList);
        }
    }

    @Override
    public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
        TRACE.d("onRequestWaitingUser()");
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestWaitingUser();
        }
    }

    @Override
    public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
        super.onQposRequestPinResult(dataList, offlineTime);
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onQposRequestPinResult(dataList,offlineTime);
        }
    }

    @Override
    public void onReturnGetKeyBoardInputResult(String result) {
        super.onReturnGetKeyBoardInputResult(result);
        Log.w("checkUactivity", "onReturnGetKeyBoardInputResult");
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onReturnGetKeyBoardInputResult(result);
        }
    }

    @Override
    public void onReturnGetPinInputResult(int num) {
        TRACE.i("parent onReturnGetPinInputResult"+num);
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onReturnGetPinInputResult(num);
        }
    }

    @Override
    public void onRequestSetAmount() {
        TRACE.d("parent onRequestSetAmount()");
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestSetAmount();
        }
    }

    /**
     */
    @Override
    public void onRequestIsServerConnected() {
        TRACE.d("onRequestIsServerConnected()");
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestIsServerConnected();
        }
    }

    @Override
    public void onRequestOnlineProcess(final String tlv) {
//        TRACE.d("onRequestOnlineProcess" + tlv);
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestOnlineProcess(tlv);
        }
    }

    @Override
    public void onRequestTime() {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestTime();
        }
    }


    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestDisplay(displayMsg);
        }
    }

    @Override
    public void onRequestFinalConfirm() {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestFinalConfirm();
        }
    }

    @Override
    public void onRequestNoQposDetected() {
        TRACE.d("onRequestNoQposDetected()");
        ConnectionServiceCallback callback = callbackManager.getConnectionCallback();
        if (callback != null) {
            callback.onRequestNoQposDetected();
        }
    }

    @Override
    public void onRequestQposConnected() {
        TRACE.d("parent onRequestQposConnected()");
        ConnectionServiceCallback callback = callbackManager.getConnectionCallback();
        if (callback != null) {
            callback.onRequestQposConnected();
        }
    }

    @Override
    public void onRequestQposDisconnected() {
        TRACE.d("parent disconnected()");
        ConnectionServiceCallback callback = callbackManager.getConnectionCallback();
        SPUtils.getInstance().put("device_type","");
        SPUtils.getInstance().put("isConnected",false);
        SPUtils.getInstance().put("isConnectedAutoed",false);
        if (callback != null) {
            callback.onRequestQposDisconnected();
        }
    }

    @Override
    public void onReturnReversalData(String tlv) {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onReturnReversalData(tlv);
        }
    }


    @Override
    public void onReturnGetPinResult(Hashtable<String, String> result) {
        TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onReturnGetPinResult(result);
        }
    }

    @Override
    public void onGetCardNoResult(String cardNo) {
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onGetCardNoResult(cardNo);
        }
    }

    @Override
    public void onGetCardInfoResult(Hashtable<String, String> cardInfo) {
        super.onGetCardInfoResult(cardInfo);
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onGetCardInfoResult(cardInfo);
        }
    }

    @Override
    public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
        super.onRequestSetPin(isOfflinePin, tryNum);
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestSetPin(isOfflinePin,tryNum);
        }
    }

    @Override
    public void onRequestSetPin() {
        TRACE.i("onRequestSetPin()");
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onRequestSetPin();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice arg0) {
        ConnectionServiceCallback callback = callbackManager.getConnectionCallback();
        if (callback != null) {
            callback.onDeviceFound(arg0);
        }
    }

    @Override
    public void onRequestDeviceScanFinished() {
        TRACE.d("onRequestDeviceScanFinished()");
    }

    @Override
    public void onTradeCancelled() {
        TRACE.d("onTradeCancelled");
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onTradeCancelled();
        }
    }

    @Override
    public void onEmvICCExceptionData(String tlv) {
        super.onEmvICCExceptionData(tlv);
        PaymentServiceCallback callback = callbackManager.getPaymentCallback();
        if (callback != null) {
            callback.onEmvICCExceptionData(tlv);
        }
    }
}
