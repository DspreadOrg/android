package com.dspread.demoui.activity;

import static com.dspread.demoui.BaseApplication.getApplicationInstance;
import static com.dspread.demoui.utils.QPOSUtil.HexStringToByteArray;
import static com.xuexiang.xutil.resource.ResUtils.getString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import com.dspread.demoui.R;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.beans.GlobalErrorEvent;
import com.dspread.demoui.interfaces.BluetoothConnectCallback;
import com.dspread.demoui.interfaces.ConnectStateCallback;
import com.dspread.demoui.interfaces.MifareCardOperationCallback;
import com.dspread.demoui.interfaces.PosInfoCallback;
import com.dspread.demoui.interfaces.PosUpdateCallback;
import com.dspread.demoui.interfaces.TransactionCallback;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.QPOSUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

public class MyQposClass extends CQPOSService {
    private static ConnectStateCallback stateCallback;
    private static PosInfoCallback posInfoCallback;
    private static TransactionCallback transactionCallback;
    private static BluetoothConnectCallback bluetoothConnectCallback;

    private static PosUpdateCallback posUpdateCallback;
    private static MifareCardOperationCallback mifareCardOperationCallback;

    public static void setMifareCardOperationCallback(MifareCardOperationCallback mifareCardOperationCallback) {
        MyQposClass.mifareCardOperationCallback = mifareCardOperationCallback;
    }

    public static void setPosUpdateCallback(PosUpdateCallback posUpdateCallback) {
        MyQposClass.posUpdateCallback = posUpdateCallback;
    }

    public static void setStateCallback(ConnectStateCallback stateCallback) {
        MyQposClass.stateCallback = stateCallback;
    }

    public static void setPosInfoCallback(PosInfoCallback posInfoCallback) {
        MyQposClass.posInfoCallback = posInfoCallback;
    }

    public static void setBluetoothConnectCallback(BluetoothConnectCallback bluetoothConnectCallback) {
        MyQposClass.bluetoothConnectCallback = bluetoothConnectCallback;
    }

    public static void setTransactionCallback(TransactionCallback transactionCallback) {
        MyQposClass.transactionCallback = transactionCallback;
    }

    @Override
    public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
        TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
        if(transactionCallback != null){
            transactionCallback.onDoTradeResult(result, decodeData);
        }
    }

    @Override
    public void onQposInfoResult(Hashtable<String, String> posInfoData) {
//            tvTitle.setText(getString(R.string.get_info));
        if(posInfoCallback != null){
            posInfoCallback.onQposInfoResult(posInfoData);
        }
    }

    /**
     */
    @Override
    public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
        TRACE.d("parent onRequestTransactionResult()" + transactionResult.toString());
        if(transactionCallback != null){
            transactionCallback.onRequestTransactionResult(transactionResult);
        }
    }

    @Override
    public void onRequestBatchData(String tlv) {
        if(transactionCallback != null){
            transactionCallback.onRequestBatchData(tlv);
        }
    }

    @Override
    public void onRequestTransactionLog(String tlv) {

    }

    @Override
    public void onQposIdResult(Hashtable<String, String> posIdTable) {
        if(posInfoCallback != null){
            posInfoCallback.onQposIdResult(posIdTable);
        }
    }

    @Override
    public void onRequestSelectEmvApp(ArrayList<String> appList) {
        if(transactionCallback != null){
            transactionCallback.onRequestSelectEmvApp(appList);
        }
    }

    @Override
    public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
        TRACE.d("onRequestWaitingUser()");
        if(transactionCallback != null){
            transactionCallback.onRequestWaitingUser();
        }

    }

    @Override
    public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
        super.onQposRequestPinResult(dataList, offlineTime);
        if(transactionCallback != null){
            transactionCallback.onQposRequestPinResult(dataList, offlineTime);
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
        if(transactionCallback != null){
            transactionCallback.onReturnGetPinInputResult(num);
        }
    }

    @Override
    public void onRequestSetAmount() {
        TRACE.d("parent onRequestSetAmount()");
        if(transactionCallback != null){
            transactionCallback.onRequestSetAmount();
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
        TRACE.d("onRequestOnlineProcess" + tlv);
        if(transactionCallback != null){
            transactionCallback.onRequestOnlineProcess(tlv);
        }
    }

    @Override
    public void onRequestTime() {
        if(transactionCallback != null){
            transactionCallback.onRequestTime();
        }
    }


    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        TRACE.d("patent onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        if(transactionCallback != null){
            transactionCallback.onRequestDisplay(displayMsg);
        }
    }

    @Override
    public void onRequestFinalConfirm() {
        TRACE.d("onRequestFinalConfirm() ");
        TRACE.d("onRequestFinalConfirm - S");
        dismissDialog();
//            if (!isPinCanceled) {
//                dialog = new Dialog(PaymentActivity.this);
//                dialog.setContentView(R.layout.confirm_dialog);
//                dialog.setTitle(getString(R.string.confirm_amount));
//
//                String message = getString(R.string.amount) + ": $" + amounts;
//                if (!cashbackAmounts.equals("")) {
//                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmounts;
//                }
//                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);
//                dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        pos.finalConfirm(true);
//                        dialog.dismiss();
//                    }
//                });
//                dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        pos.finalConfirm(false);
//                        dialog.dismiss();
//                    }
//                });
//                dialog.show();
//            } else {
//                pos.finalConfirm(false);
//            }
    }

    @Override
    public void onRequestNoQposDetected() {
        TRACE.d("onRequestNoQposDetected()");
        if(stateCallback != null){
            stateCallback.onRequestNoQposDetected();
        }
        if(bluetoothConnectCallback != null){
            bluetoothConnectCallback.onRequestNoQposDetected();
        }
    }

    @Override
    public void onRequestQposConnected() {
        TRACE.d("parent onRequestQposConnected()");
        if(stateCallback != null){
            stateCallback.onRequestQposConnected();
        }
        if(bluetoothConnectCallback != null){
            bluetoothConnectCallback.onRequestQposConnected();
        }
    }

    @Override
    public void onRequestQposDisconnected() {
        if(stateCallback != null){
            stateCallback.onRequestQposDisconnected();
        }
        if(bluetoothConnectCallback != null){
            bluetoothConnectCallback.onRequestQposDisconnected();
        }
    }

    @Override
    public void onError(QPOSService.Error errorState) {
        TRACE.i("onerror "+errorState);
        EventBus.getDefault().post(new GlobalErrorEvent(errorState));
//        if(transactionCallback != null){
//            transactionCallback.onError(errorState);
//        }
    }

    @Override
    public void onReturnReversalData(String tlv) {
        if(transactionCallback != null){
            transactionCallback.onReturnReversalData(tlv);
        }
    }

    @Override
    public void onReturnServerCertResult(String serverSignCert, String serverEncryptCert) {
        super.onReturnServerCertResult(serverSignCert, serverEncryptCert);
    }

    @Override
    public void onReturnGetPinResult(Hashtable<String, String> result) {
        TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
//        String pinBlock = result.get("pinBlock");
//        String pinKsn = result.get("pinKsn");
//        String content = "get pin result\n";
//        content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
//        content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
//        TRACE.i(content);
        if(transactionCallback != null){
            transactionCallback.onReturnGetPinResult(result);
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
//            statusEditText.setText(content);
    }

    @Override
    public void onGetCardNoResult(String cardNo) {
        if(transactionCallback != null){
            transactionCallback.onGetCardNoResult(cardNo);
        }
    }

    @Override
    public void onRequestCalculateMac(String calMac) {
        TRACE.d("onRequestCalculateMac(String calMac):" + calMac);
        if (calMac != null && !"".equals(calMac)) {
            calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
        }
//            statusEditText.setText("calMac: " + calMac);
        TRACE.d("calMac_result: calMac=> e: " + calMac);
    }

    @Override
    public void onRequestSignatureResult(byte[] arg0) {
        TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());
    }

    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
        TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
        if(posUpdateCallback != null){
            posUpdateCallback.onRequestUpdateWorkKeyResult(result);
        }
    }

    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result, Hashtable<String, String> checkValue) {
        super.onRequestUpdateWorkKeyResult(result, checkValue);
        if(posUpdateCallback != null){
            posUpdateCallback.onRequestUpdateWorkKeyResult(result, checkValue);
        }
    }

    @Override
    public void onReturnCustomConfigResult(boolean isSuccess, String result) {
        TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + "--result--" + result);
        if(posUpdateCallback != null){
            posUpdateCallback.onReturnCustomConfigResult(isSuccess, result);
        }
    }

    @Override
    public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
        super.onRequestSetPin(isOfflinePin, tryNum);
        if(transactionCallback != null){
            transactionCallback.onRequestSetPin(isOfflinePin,tryNum);
        }
    }

    @Override
    public void onRequestSetPin() {
        TRACE.i("onRequestSetPin()");
        if(transactionCallback != null){
            transactionCallback.onRequestSetPin();
        }
//            tvTitle.setText(getString(R.string.input_pin));
//        dismissDialog();
////            Mydialog.pinpadDialog(CheckActivity.this, pos);
//            mllchrccard.setVisibility(View.GONE);
//            Paydialog = new PayPassDialog(PaymentActivity.this);
//            Paydialog.getPayViewPass().setRandomNumber(true).setPayClickListener(new PayPassView.OnPayClickListener() {
//
//                @Override
//                public void onCencel() {
//                    pos.cancelPin();
//                    Paydialog.dismiss();
//                }
//
//                @Override
//                public void onPaypass() {
////                pos.bypassPin();
//                    pos.sendPin("".getBytes());
//                    Paydialog.dismiss();
//                }
//
//                @Override
//                public void onConfirm(String password) {
//                    if (password.length() >= 4 && password.length() <= 12) {
//                        Log.w("password", "password==" + password);
////                        pos.sendPin(password);
//                        String newPin = "";
//                        //this part is used to enctypt the plaintext pin with random seed
//                        if (pos.getCvmKeyList() != null && !("").equals(pos.getCvmKeyList())) {
//                            String keyList = Util.convertHexToString(pos.getCvmKeyList());
//                            for (int i = 0; i < password.length(); i++) {
//                                for (int j = 0; j < keyList.length(); j++) {
//                                    if (keyList.charAt(j) == password.charAt(i)) {
//                                        newPin = newPin + Integer.toHexString(j) + "";
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        String pinBlock = buildCvmPinBlock(pos.getEncryptData(), newPin);// build the ISO format4 pin block
//                        Log.w("password", "pinBlock==" + pinBlock);
//                        pos.sendCvmPin(pinBlock, true);
//                        Paydialog.dismiss();
//                    } else {
//                        Toast.makeText(PaymentActivity.this, "The length just can input 4 - 12 digits", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//
//            });
    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess) {
        TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
        if(posUpdateCallback != null){
            posUpdateCallback.onReturnSetMasterKeyResult(isSuccess);
        }
    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess, Hashtable<String, String> result) {
        super.onReturnSetMasterKeyResult(isSuccess, result);
        if(posUpdateCallback != null){
            posUpdateCallback.onReturnSetMasterKeyResult(isSuccess, result);
        }
    }

    @Override
    public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
        TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());
        StringBuilder sb = new StringBuilder();
        sb.append("APDU Responses: \n");
        for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
            sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
        }
//            statusEditText.setText("\n" + sb.toString());
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
        String s = "serviceCode: " + result.get("serviceCode");
        s += "\n";
        s += "trackblock: " + result.get("trackblock");
//            statusEditText.setText(s);
    }

    @Override
    public void onLcdShowCustomDisplay(boolean arg0) {
        TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
    }

    @Override
    public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
        TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
        if(posUpdateCallback != null){
            posUpdateCallback.onUpdatePosFirmwareResult(arg0);
        }
    }

    @Override
    public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
        TRACE.d("onReturnDownloadRsaPublicKey(HashMap<String, String> map):" + map.toString());
        if (map == null) {
            TRACE.d("MainActivity++++++++++++++map == null");
            return;
        }
        String randomKeyLen = map.get("randomKeyLen");
        String randomKey = map.get("randomKey");
        String randomKeyCheckValueLen = map.get("randomKeyCheckValueLen");
        String randomKeyCheckValue = map.get("randomKeyCheckValue");
        TRACE.d("randomKey" + randomKey + "    \n    randomKeyCheckValue" + randomKeyCheckValue);
//            statusEditText.setText("randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:" + randomKeyCheckValue);
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
        if(mifareCardOperationCallback != null){
            mifareCardOperationCallback.onReturnNFCApduResult(arg0,arg1,arg2);
        }
    }

    @Override
    public void onReturnPowerOffNFCResult(boolean arg0) {
        TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        if(mifareCardOperationCallback != null){
            mifareCardOperationCallback.onReturnPowerOffNFCResult(arg0);
        }
    }

    @Override
    public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
        if(mifareCardOperationCallback != null){
            mifareCardOperationCallback.onReturnPowerOnNFCResult(arg0, arg1, arg2, arg3);
        }
    }

    @Override
    public void onCbcMacResult(String result) {
        TRACE.d("onCbcMacResult(String result):" + result);
        if (result == null || "".equals(result)) {
//                statusEditText.setText("cbc_mac:false");
        } else {
//                statusEditText.setText("cbc_mac: " + result);
        }
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
//        if (cardIsExist) {
////                statusEditText.setText("cardIsExist:" + cardIsExist);
//        } else {
////                statusEditText.setText("cardIsExist:" + cardIsExist);
//        }
        if(transactionCallback != null){
            transactionCallback.onQposIsCardExist(cardIsExist);
        }
    }

    @Override
    public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
        if (mifareCardOperationCallback != null) {
            mifareCardOperationCallback.onSearchMifareCardResult(arg0);
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
        if(posUpdateCallback != null){
            posUpdateCallback.onReturnUpdateIPEKResult(arg0);
        }
    }

    @Override
    public void onReturnUpdateEMVRIDResult(boolean arg0) {
        dismissDialog();
        TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);
//            mllinfo.setVisibility(View.VISIBLE);
//            mbtnNewpay.setVisibility(View.GONE);
//            mtvinfo.setText("updateEmvDidResult: " + arg0);
//            mllchrccard.setVisibility(View.GONE);
//            tradeSuccess.setVisibility(View.GONE);
    }

    @Override
    public void onReturnUpdateEMVResult(boolean arg0) {
        dismissDialog();
        TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);
//            mllinfo.setVisibility(View.VISIBLE);
//            mbtnNewpay.setVisibility(View.GONE);
//            mtvinfo.setText("updateEmvAppResult: " + arg0);
//            mllchrccard.setVisibility(View.GONE);
//            tradeSuccess.setVisibility(View.GONE);

    }

    @Override
    public void onBluetoothBoardStateResult(boolean arg0) {
        TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice arg0) {
        if(bluetoothConnectCallback != null){
            bluetoothConnectCallback.onDeviceFound(arg0);
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
        if(bluetoothConnectCallback != null){
            bluetoothConnectCallback.onRequestDeviceScanFinished();
        }
    }

    @Override
    public void onRequestUpdateKey(String arg0) {
        TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
        if(posInfoCallback != null){
            posInfoCallback.onRequestUpdateKey(arg0);
        }
    }

    @Override
    public void onGetKeyCheckValue(Hashtable<String, String> checkValue) {
        super.onGetKeyCheckValue(checkValue);
        if(posInfoCallback != null){
            posInfoCallback.onGetKeyCheckValue(checkValue);
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
        dismissDialog();
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
        if(transactionCallback != null){
            transactionCallback.onTradeCancelled();
        }
    }

    @Override
    public void onFinishMifareCardResult(boolean arg0) {
        TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);
        if (mifareCardOperationCallback != null) {
            mifareCardOperationCallback.onFinishMifareCardResult(arg0);
        }
    }

    @Override
    public void onVerifyMifareCardResult(boolean arg0) {
        TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);
        if (mifareCardOperationCallback != null) {
            mifareCardOperationCallback.onVerifyMifareCardResult(arg0);
        }
    }

    @Override
    public void onReadMifareCardResult(Hashtable<String, String> arg0) {
        if (mifareCardOperationCallback != null) {
            mifareCardOperationCallback.onReadMifareCardResult(arg0);
        }
    }

    @Override
    public void onWriteMifareCardResult(boolean arg0) {
        TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);
        if (mifareCardOperationCallback != null) {
            mifareCardOperationCallback.onWriteMifareCardResult(arg0);
        }
    }

    @Override
    public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
        if (mifareCardOperationCallback != null) {
            mifareCardOperationCallback.onOperateMifareCardResult(arg0);
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

    public static void dismissDialog() {

        if (Mydialog.ErrorDialog != null) {
            Mydialog.ErrorDialog.dismiss();
        }
        if (Mydialog.manualExitDialog != null) {
            Mydialog.manualExitDialog.dismiss();
        }
        if (Mydialog.Ldialog != null) {
            Mydialog.Ldialog.dismiss();
        }

        if (Mydialog.onlingDialog != null) {
            Mydialog.onlingDialog.dismiss();
        }

    }
}
