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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import me.goldze.mvvmhabit.utils.SPUtils;

public class MyQposClass extends CQPOSService {
    private final QPOSCallbackManager callbackManager = QPOSCallbackManager.getInstance();

    @CallbackChange(
        description = "The callback is in child thread",
        type = CallbackChange.ChangeType.MODIFIED
    )
    @Override
    public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
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
        for (BaseQPOSCallback callback : callbackManager.callbackMap.values()) {
            callback.onError(errorState);
        }
    }

    @Override
    public void onQposInfoResult(Hashtable<String, String> posInfoData) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onQposInfoResult(posInfoData);
        }
    }

    /**
     */
    @Override
    public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
        TRACE.d("parent onRequestTransactionResult()" + transactionResult.toString());
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestTransactionResult(transactionResult);
        }
    }

    @Override
    public void onRequestBatchData(String tlv) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestBatchData(tlv);
        }
    }

    @Override
    public void onRequestTransactionLog(String tlv) {

    }

    @Override
    public void onQposIdResult(Hashtable<String, String> posIdTable) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onQposIdResult(posIdTable);
        }
    }

    @Override
    public void onRequestSelectEmvApp(ArrayList<String> appList) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestSelectEmvApp(appList);
        }
    }

    @Override
    public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
        TRACE.d("onRequestWaitingUser()");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestWaitingUser();
        }

    }

    @Override
    public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
        super.onQposRequestPinResult(dataList, offlineTime);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onQposRequestPinResult(dataList,offlineTime);
        }
    }

    @Override
    public void onReturnGetKeyBoardInputResult(String result) {
        super.onReturnGetKeyBoardInputResult(result);
        Log.w("checkUactivity", "onReturnGetKeyBoardInputResult");
    }

    @Override
    public void onReturnGetPinInputResult(int num) {
        TRACE.i("parent onReturnGetPinInputResult"+num);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnGetPinInputResult(num);
        }
    }

    @Override
    public void onRequestSetAmount() {
        TRACE.d("parent onRequestSetAmount()");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestSetAmount();
        }
    }

    /**
     */
    @Override
    public void onRequestIsServerConnected() {
        TRACE.d("onRequestIsServerConnected()");

    }

    @Override
    public void onRequestOnlineProcess(final String tlv) {
//        TRACE.d("onRequestOnlineProcess" + tlv);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestOnlineProcess(tlv);
        }
    }

    @Override
    public void onRequestTime() {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestTime();
        }
    }


    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestDisplay(displayMsg);
        }
    }

    @Override
    public void onRequestFinalConfirm() {

    }

    @Override
    public void onRequestNoQposDetected() {
        TRACE.d("onRequestNoQposDetected()");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestNoQposDetected();
        }
    }

    @Override
    public void onRequestQposConnected() {
        TRACE.d("parent onRequestQposConnected()");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestQposConnected();
        }
    }

    @Override
    public void onRequestQposDisconnected() {
        TRACE.d("parent disconnected()");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        SPUtils.getInstance().put("device_type","");
        SPUtils.getInstance().put("isConnected",false);
        SPUtils.getInstance().put("isConnectedAutoed",false);
        if (callback != null) {
            callback.onRequestQposDisconnected();
        }
    }

    @Override
    public void onReturnReversalData(String tlv) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnReversalData(tlv);
        }
    }

    @Override
    public void onReturnServerCertResult(String serverSignCert, String serverEncryptCert) {
        super.onReturnServerCertResult(serverSignCert, serverEncryptCert);
    }

    @Override
    public void onReturnGetPinResult(Hashtable<String, String> result) {
        TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnGetPinResult(result);
        }
    }

    @Override
    public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
        TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
    }

    @Override
    public void onReturnPowerOffIccResult(boolean arg0) {
        TRACE.d("onReturnPowerOffIccResult(boolean arg0):" + arg0);
    }

    @Override
    public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
        TRACE.d("onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);

    }

    @Override
    public void onReturnSetSleepTimeResult(boolean isSuccess) {
        TRACE.d("onReturnSetSleepTimeResult(boolean isSuccess):" + isSuccess);
        String content = "";
        if (isSuccess) {
            content = "set the sleep time success.";
        } else {
            content = "set the sleep time failed.";
        }
    }

    @Override
    public void onGetCardNoResult(String cardNo) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onGetCardNoResult(cardNo);
        }
    }

    @Override
    public void onRequestCalculateMac(String calMac) {
        TRACE.d("onRequestCalculateMac(String calMac):" + calMac);
    }

    @Override
    public void onRequestSignatureResult(byte[] arg0) {
        TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());
    }

    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
        TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestUpdateWorkKeyResult(result);
        }
    }

    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result, Hashtable<String, String> checkValue) {
        super.onRequestUpdateWorkKeyResult(result, checkValue);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestUpdateWorkKeyResult(result,checkValue);
        }
    }

    @Override
    public void onReturnCustomConfigResult(boolean isSuccess, String result) {
        TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + "--result--" + result);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnCustomConfigResult(isSuccess,result);
        }
    }

    @Override
    public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
        super.onRequestSetPin(isOfflinePin, tryNum);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestSetPin(isOfflinePin,tryNum);
        }
    }

    @Override
    public void onRequestSetPin() {
        TRACE.i("onRequestSetPin()");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestSetPin();
        }
    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess) {
        TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnSetMasterKeyResult(isSuccess);
        }
    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess, Hashtable<String, String> result) {
        super.onReturnSetMasterKeyResult(isSuccess, result);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnSetMasterKeyResult(isSuccess,result);
        }
    }

    @Override
    public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
        TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());
    }

    @Override
    public void onBluetoothBondFailed() {
        TRACE.d("onBluetoothBondFailed()");
//            statusEditText.setText("bond failed");
    }

    @Override
    public void onBluetoothBondTimeout() {
        TRACE.d("onBluetoothBondTimeout()");
//            statusEditText.setText("bond timeout");
    }

    @Override
    public void onBluetoothBonded() {
        TRACE.d("onBluetoothBonded()");
//            statusEditText.setText("bond success");
    }

    @Override
    public void onBluetoothBonding() {
        TRACE.d("onBluetoothBonding()");
//            statusEditText.setText("bonding .....");
    }

    @Override
    public void onReturniccCashBack(Hashtable<String, String> result) {
        TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());

    }

    @Override
    public void onLcdShowCustomDisplay(boolean arg0) {
        TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
    }

    @Override
    public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
        TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onUpdatePosFirmwareResult(arg0);
        }
    }

    @Override
    public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
        TRACE.d("onReturnDownloadRsaPublicKey(HashMap<String, String> map):" + map.toString());
    }

    @Override
    public void onGetPosComm(int mod, String amount, String posid) {
        TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
    }

    @Override
    public void onPinKey_TDES_Result(String arg0) {
        TRACE.d("onPinKey_TDES_Result(String arg0):" + arg0);
//            statusEditText.setText("result:" + arg0);
    }

    @Override
    public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
        TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        String upgradeInfo;
    }

    @Override
    public void onEmvICCExceptionData(String arg0) {
        TRACE.d("onEmvICCExceptionData(String arg0):" + arg0);
    }

    @Override
    public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
        TRACE.d("onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
    }

    @Override
    public void onGetInputAmountResult(boolean arg0, String arg1) {
        TRACE.d("onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
    }

    @Override
    public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnNFCApduResult(arg0,arg1,arg2);
        }
    }

    @Override
    public void onReturnPowerOffNFCResult(boolean arg0) {
        TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnPowerOffNFCResult(arg0);
        }
    }

    @Override
    public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnPowerOnNFCResult(arg0,arg1,arg2,arg3);
        }
    }

    @Override
    public void onCbcMacResult(String result) {
        TRACE.d("onCbcMacResult(String result):" + result);
    }

    @Override
    public void onReadBusinessCardResult(boolean arg0, String arg1) {
        TRACE.d(" onReadBusinessCardResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
    }

    @Override
    public void onWriteBusinessCardResult(boolean arg0) {
        TRACE.d(" onWriteBusinessCardResult(boolean arg0):" + arg0);
    }

    @Override
    public void onConfirmAmountResult(boolean arg0) {
        TRACE.d("onConfirmAmountResult(boolean arg0):" + arg0);
    }

    @Override
    public void onQposIsCardExist(boolean cardIsExist) {
        TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onQposIsCardExist(cardIsExist);
        }
    }

    @Override
    public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onSearchMifareCardResult(arg0);
        }
    }

    @Override
    public void onBatchReadMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
        if (cardData != null) {
            TRACE.d("onBatchReadMifareCardResult(boolean arg0):" + msg + cardData.toString());
        }
    }

    @Override
    public void onBatchWriteMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
        if (cardData != null) {
            TRACE.d("onBatchWriteMifareCardResult(boolean arg0):" + msg + cardData.toString());
        }
    }

    @Override
    public void onSetBuzzerResult(boolean arg0) {
        TRACE.d("onSetBuzzerResult(boolean arg0):" + arg0);
        if (arg0) {
//                statusEditText.setText("Set buzzer success");
        } else {
//                statusEditText.setText("Set buzzer failed");
        }
    }

    @Override
    public void onSetBuzzerTimeResult(boolean b) {
        TRACE.d("onSetBuzzerTimeResult(boolean b):" + b);
    }

    @Override
    public void onSetBuzzerStatusResult(boolean b) {
        TRACE.d("onSetBuzzerStatusResult(boolean b):" + b);
    }

    @Override
    public void onGetBuzzerStatusResult(String s) {
        TRACE.d("onGetBuzzerStatusResult(String s):" + s);
    }

    @Override
    public void onSetManagementKey(boolean arg0) {
        TRACE.d("onSetManagementKey(boolean arg0):" + arg0);
        if (arg0) {
//                statusEditText.setText("Set master key success");
        } else {
//                statusEditText.setText("Set master key failed");
        }
    }

    @Override
    public void onReturnUpdateIPEKResult(boolean arg0) {
        TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReturnUpdateIPEKResult(arg0);
        }
    }

    @Override
    public void onReturnUpdateEMVRIDResult(boolean arg0) {
        TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);
    }

    @Override
    public void onReturnUpdateEMVResult(boolean arg0) {
        TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);
    }

    @Override
    public void onBluetoothBoardStateResult(boolean arg0) {
        TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice arg0) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onDeviceFound(arg0);
        }
    }

    @Override
    public void onSetSleepModeTime(boolean arg0) {
        TRACE.d("onSetSleepModeTime(boolean arg0):" + arg0);
//            if (arg0) {
//                statusEditText.setText("set the Sleep timee Success");
//            } else {
//                statusEditText.setText("set the Sleep timee unSuccess");
//            }
    }

    @Override
    public void onReturnGetEMVListResult(String arg0) {
        TRACE.d("onReturnGetEMVListResult(String arg0):" + arg0);
        if (arg0 != null && arg0.length() > 0) {
//                statusEditText.setText("The emv list is : " + arg0);
        }
    }

    @Override
    public void onWaitingforData(String arg0) {
        TRACE.d("onWaitingforData(String arg0):" + arg0);
    }

    @Override
    public void onRequestDeviceScanFinished() {
        TRACE.d("onRequestDeviceScanFinished()");
//            Toast.makeText(CheckActivity.this, R.string.scan_over, Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onRequestUpdateKey(String arg0) {
        TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onRequestUpdateKey(arg0);
        }
    }

    @Override
    public void onGetKeyCheckValue(Hashtable<String, String> checkValue) {
        super.onGetKeyCheckValue(checkValue);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onGetKeyCheckValue(checkValue);
        }
    }

    @Override
    public void onReturnGetQuickEmvResult(boolean arg0) {
        TRACE.d("onReturnGetQuickEmvResult(boolean arg0):" + arg0);
    }

    @Override
    public void onQposDoGetTradeLogNum(String arg0) {
        TRACE.d("onQposDoGetTradeLogNum(String arg0):" + arg0);
        int a = Integer.parseInt(arg0, 16);
        if (a >= 188) {
//                statusEditText.setText("the trade num has become max value!!");
            return;
        }
//            statusEditText.setText("get log num:" + a);
    }

    @Override
    public void onQposDoTradeLog(boolean arg0) {
        TRACE.d("onQposDoTradeLog(boolean arg0) :" + arg0);
        if (arg0) {
//                statusEditText.setText("clear log success!");
        } else {
//                statusEditText.setText("clear log fail!");
        }
    }

    @Override
    public void onAddKey(boolean arg0) {
        TRACE.d("onAddKey(boolean arg0) :" + arg0);
        if (arg0) {
//                statusEditText.setText("ksn add 1 success");
        } else {
//                statusEditText.setText("ksn add 1 failed");
        }
    }

    @Override
    public void onEncryptData(Hashtable<String, String> resultTable) {
        if (resultTable != null) {
            TRACE.d("onEncryptData(String arg0) :" + resultTable);
//                mllinfo.setVisibility(View.VISIBLE);
//                mtvinfo.setText("onEncryptData: " + resultTable);
//                mllchrccard.setVisibility(View.GONE);

        }
    }


    @Override
    public void onQposKsnResult(Hashtable<String, String> arg0) {
        TRACE.d("onQposKsnResult(Hashtable<String, String> arg0):" + arg0.toString());
        String pinKsn = arg0.get("pinKsn");
        String trackKsn = arg0.get("trackKsn");
        String emvKsn = arg0.get("emvKsn");
        TRACE.d("get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);

    }

    @Override
    public void onRequestDevice() {
        Log.w("onRequestDevice", "onRequestDevice");
    }

    @Override
    public void onTradeCancelled() {
        TRACE.d("onTradeCancelled");
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onTradeCancelled();
        }
    }

    @Override
    public void onFinishMifareCardResult(boolean arg0) {
        TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onFinishMifareCardResult(arg0);
        }
    }

    @Override
    public void onVerifyMifareCardResult(boolean arg0) {
        TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onVerifyMifareCardResult(arg0);
        }
    }

    @Override
    public void onReadMifareCardResult(Hashtable<String, String> arg0) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onReadMifareCardResult(arg0);
        }
    }

    @Override
    public void onWriteMifareCardResult(boolean arg0) {
        TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onWriteMifareCardResult(arg0);
        }
    }

    @Override
    public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
        MyCustomQPOSCallback callback = callbackManager.getCallback(MyCustomQPOSCallback.class);
        if (callback != null) {
            callback.onOperateMifareCardResult(arg0);
        }
    }

    @Override
    public void getMifareCardVersion(Hashtable<String, String> arg0) {

    }

    @Override
    public void getMifareFastReadData(Hashtable<String, String> arg0) {

    }

    @Override
    public void getMifareReadData(Hashtable<String, String> arg0) {

    }

    @Override
    public void writeMifareULData(String arg0) {

    }

    @Override
    public void verifyMifareULData(Hashtable<String, String> arg0) {

    }

    @Override
    public void onGetSleepModeTime(String arg0) {

    }

    @Override
    public void onGetShutDownTime(String arg0) {

    }

    @Override
    public void onQposDoSetRsaPublicKey(boolean arg0) {

    }
}
