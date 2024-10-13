package com.dspread.demoui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.ui.fragment.DeviceUpdataFragment;
import com.dspread.demoui.ui.fragment.SettingFragment;
import com.dspread.demoui.utils.ConstantUtil;
import com.dspread.demoui.utils.DUKPK2009_CBC;
import com.dspread.demoui.utils.ParseASN1Util;
import com.dspread.demoui.utils.SpUtils;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.widget.pinpad.keyboard.KeyBoardNumInterface;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.utils.BASE64Decoder;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import static com.dspread.demoui.activity.BaseApplication.getApplicationInstance;
import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.xuexiang.xutil.resource.ResUtils.getString;

public class MyQposClass extends CQPOSService {
    public  static KeyboardUtil keyboardUtil;
    private static Dialog dialog;
    private ListView appListView;
    boolean disconnect=false;
    public static List<String> dataLists;
    public static String datavalue;
    @Override
    public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
        TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
        dismissDialog();
        String cardNo = "";
        String msg = "";
        if (result == QPOSService.DoTradeResult.NONE) {
            msg = getString(R.string.no_card_detected);
            Log.w("paymentActivity", "msg==" + msg);
        } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
            msg = getString(R.string.try_another_interface);
            Log.w("paymentActivity", "msg==" + msg);
        } else if (result == QPOSService.DoTradeResult.ICC) {
            TRACE.d("EMV ICC Start");
            pos.doEmvApp(QPOSService.EmvOption.START);
        } else if (result == QPOSService.DoTradeResult.NOT_ICC) {
            msg = getString(R.string.card_inserted);
            Log.w("paymentActivity", "msg==" + msg);
        } else if (result == QPOSService.DoTradeResult.BAD_SWIPE) {
            msg = getString(R.string.bad_swipe);
            Log.w("paymentActivity", "msg==" + msg);
        } else if (result == QPOSService.DoTradeResult.CARD_NOT_SUPPORT) {
            msg = "GPO NOT SUPPORT";
            Log.w("paymentActivity", "msg==" + msg);
        } else if (result == QPOSService.DoTradeResult.PLS_SEE_PHONE) {
            msg = "PLS SEE PHONE";
            Log.w("paymentActivity", "msg==" + msg);
        } else if (result == QPOSService.DoTradeResult.MCR) {//Magnetic card
            String content = getString(R.string.card_swiped);
            String formatID = decodeData.get("formatID");
            if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                String serviceCode = decodeData.get("serviceCode");
                String trackblock = decodeData.get("trackblock");
                String psamId = decodeData.get("psamId");
                String posId = decodeData.get("posId");
                String pinblock = decodeData.get("pinblock");
                String macblock = decodeData.get("macblock");
                String activateCode = decodeData.get("activateCode");
                String trackRandomNumber = decodeData.get("trackRandomNumber");
                content += getString(R.string.format_id) + " " + formatID + "\n";
                content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                content += getString(R.string.service_code) + " " + serviceCode + "\n";
                content += "trackblock: " + trackblock + "\n";
                content += "psamId: " + psamId + "\n";
                content += "posId: " + posId + "\n";
                content += getString(R.string.pinBlock) + " " + pinblock + "\n";
                content += "macblock: " + macblock + "\n";
                content += "activateCode: " + activateCode + "\n";
                content += "trackRandomNumber: " + trackRandomNumber + "\n";
                cardNo = maskedPAN;
            } else if (formatID.equals("FF")) {
                String type = decodeData.get("type");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
                content += "cardType:" + " " + type + "\n";
                content += "track_1:" + " " + encTrack1 + "\n";
                content += "track_2:" + " " + encTrack2 + "\n";
                content += "track_3:" + " " + encTrack3 + "\n";
            } else {
                String orderID = decodeData.get("orderId");
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
                String serviceCode = decodeData.get("serviceCode");
                String track1Length = decodeData.get("track1Length");
                String track2Length = decodeData.get("track2Length");
                String track3Length = decodeData.get("track3Length");
                String encTracks = decodeData.get("encTracks");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
                String partialTrack = decodeData.get("partialTrack");
                String pinKsn = decodeData.get("pinKsn");
                String trackksn = decodeData.get("trackksn");
                String pinBlock = decodeData.get("pinBlock");
                String encPAN = decodeData.get("encPAN");
                String trackRandomNumber = decodeData.get("trackRandomNumber");
                String pinRandomNumber = decodeData.get("pinRandomNumber");
                if (orderID != null && !"".equals(orderID)) {
                    content += "orderID:" + orderID;
                }
                content += getString(R.string.format_id) + " " + formatID + "\n";
                content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
                content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                content += getString(R.string.trackksn) + " " + trackksn + "\n";
                content += getString(R.string.service_code) + " " + serviceCode + "\n";
                content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
                content += "encPAN: " + encPAN + "\n";
                content += "trackRandomNumber: " + trackRandomNumber + "\n";
                content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
                cardNo = maskedPAN;
                String realPan = null;
                if (!TextUtils.isEmpty(trackksn) && !TextUtils.isEmpty(encTrack2)) {
                    String clearPan = DUKPK2009_CBC.getData(trackksn, encTrack2, DUKPK2009_CBC.Enum_key.DATA, DUKPK2009_CBC.Enum_mode.CBC);
                    content += "encTrack2:" + " " + clearPan + "\n";
                    realPan = clearPan.substring(0, maskedPAN.length());
                    content += "realPan:" + " " + realPan + "\n";
                }
                if (!TextUtils.isEmpty(pinKsn) && !TextUtils.isEmpty(pinBlock) && !TextUtils.isEmpty(realPan)) {
                    String date = DUKPK2009_CBC.getData(pinKsn, pinBlock, DUKPK2009_CBC.Enum_key.PIN, DUKPK2009_CBC.Enum_mode.CBC);
                    String parsCarN = "0000" + realPan.substring(realPan.length() - 13, realPan.length() - 1);
                    String s = DUKPK2009_CBC.xor(parsCarN, date);
                    content += "PIN:" + " " + s + "\n";
                }
            }
            if(decodeData.get("maskedPAN")!=null&&!"".equals(decodeData.get("maskedPAN"))){
            sendRequestToBackend(content);
            }else{
                Mydialog.ErrorDialog((Activity) getApplicationInstance, getString(R.string.trade_returnfailed), null);
            }
        } else if ((result == QPOSService.DoTradeResult.NFC_ONLINE) || (result == QPOSService.DoTradeResult.NFC_OFFLINE)) {
//                nfcLog = decodeData.get("nfcLog");
            String content = getString(R.string.tap_card);
            String formatID = decodeData.get("formatID");
            if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                String serviceCode = decodeData.get("serviceCode");
                String trackblock = decodeData.get("trackblock");
                String psamId = decodeData.get("psamId");
                String posId = decodeData.get("posId");
                String pinblock = decodeData.get("pinblock");
                String macblock = decodeData.get("macblock");
                String activateCode = decodeData.get("activateCode");
                String trackRandomNumber = decodeData.get("trackRandomNumber");

                content += getString(R.string.format_id) + " " + formatID + "\n";
                content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";

                content += getString(R.string.service_code) + " " + serviceCode + "\n";
                content += "trackblock: " + trackblock + "\n";
                content += "psamId: " + psamId + "\n";
                content += "posId: " + posId + "\n";
                content += getString(R.string.pinBlock) + " " + pinblock + "\n";
                content += "macblock: " + macblock + "\n";
                content += "activateCode: " + activateCode + "\n";
                content += "trackRandomNumber: " + trackRandomNumber + "\n";
                cardNo = maskedPAN;

            } else {
                String maskedPAN = decodeData.get("maskedPAN");
                String expiryDate = decodeData.get("expiryDate");
                String cardHolderName = decodeData.get("cardholderName");
                String serviceCode = decodeData.get("serviceCode");
                String track1Length = decodeData.get("track1Length");
                String track2Length = decodeData.get("track2Length");
                String track3Length = decodeData.get("track3Length");
                String encTracks = decodeData.get("encTracks");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
                String partialTrack = decodeData.get("partialTrack");
                String pinKsn = decodeData.get("pinKsn");
                String trackksn = decodeData.get("trackksn");
                String pinBlock = decodeData.get("pinBlock");
                String encPAN = decodeData.get("encPAN");
                String trackRandomNumber = decodeData.get("trackRandomNumber");
                String pinRandomNumber = decodeData.get("pinRandomNumber");

                content += getString(R.string.format_id) + " " + formatID + "\n";
                content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                content += getString(R.string.trackksn) + " " + trackksn + "\n";
                content += getString(R.string.service_code) + " " + serviceCode + "\n";
                content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
                content += "encPAN: " + encPAN + "\n";
                content += "trackRandomNumber: " + trackRandomNumber + "\n";
                content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
                cardNo = maskedPAN;
            }
            sendRequestToBackend(content);
        } else if ((result == QPOSService.DoTradeResult.NFC_DECLINED)) {
            msg = getString(R.string.transaction_declined);
        } else if (result == QPOSService.DoTradeResult.NO_RESPONSE) {
            msg = getString(R.string.card_no_response);
        }
        if (msg != null && !"".equals(msg)) {
            if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
                if (!msg.equals(getApplicationInstance.getString(R.string.bad_swipe))) {
                    autoTrade(msg);
                }else{
                    Toast.makeText(getApplicationInstance,msg,Toast.LENGTH_SHORT).show();
                }
            } else {
                Mydialog.ErrorDialog((Activity) getApplicationInstance, msg, null);
            }
        }
    }

    @Override
    public void onQposInfoResult(Hashtable<String, String> posInfoData) {
//            tvTitle.setText(getString(R.string.get_info));
        dismissDialog();
        TRACE.d("onQposInfoResult" + posInfoData.toString());
        String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
        String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
        String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
        String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
        String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
        String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
        String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
        String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
        String batteryPercentage = posInfoData.get("batteryPercentage") == null ? "" : posInfoData.get("batteryPercentage");
        String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
        String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
        String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? "" : posInfoData.get("PCI_firmwareVersion");
        String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? "" : posInfoData.get("PCI_hardwareVersion");
        String compileTime = posInfoData.get("compileTime") == null ? "" : posInfoData.get("compileTime");
        String content = "";
        content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
        content += getString(R.string.firmware_version) + firmwareVersion + "\n";
//        content += getString(R.string.usb) + isUsbConnected + "\n";
//        content += getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
//        content += getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationInstance.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale * 100;

        content += getString(R.string.battery_percentage) + batteryPct +"%"+ "\n";
//			}
        content += getString(R.string.hardware_version) + hardwareVersion + "\n";
//        content += "SUB : " + SUB + "\n";
        content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
        content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
        content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
        content += "PCI FirmwareVresion:" + pciFirmwareVersion + "\n";
        content += "PCI HardwareVersion:" + pciHardwareVersion + "\n";
        content += "compileTime:" + compileTime + "\n";
        Constants.transData.setPosInfo(content);
        getposInfo("posinfo");

    }

    /**
     * @see QPOSService.QPOSServiceListener#onRequestTransactionResult(QPOSService.TransactionResult)
     */
    @Override
    public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
        TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
        if (transactionResult == QPOSService.TransactionResult.CARD_REMOVED) {
        }
        dismissDialog();
        String msg = "";
        if (transactionResult == QPOSService.TransactionResult.APPROVED) {
            TRACE.d("TransactionResult.APPROVED");
//                 msg = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": $" + amounts + "\n";
//                if (!cashbackAmounts.equals("")) {
//                    msg += getString(R.string.cashback_amount) + ": INR" + cashbackAmounts;
//                }
        } else if (transactionResult == QPOSService.TransactionResult.TERMINATED) {
            msg = getString(R.string.transaction_terminated);
        } else if (transactionResult == QPOSService.TransactionResult.DECLINED) {
            msg = getString(R.string.transaction_declined);
        } else if (transactionResult == QPOSService.TransactionResult.CANCEL) {
            msg = getString(R.string.transaction_cancel);
        } else if (transactionResult == QPOSService.TransactionResult.CAPK_FAIL) {
            msg = getString(R.string.transaction_capk_fail);
        } else if (transactionResult == QPOSService.TransactionResult.NOT_ICC) {
            msg = getString(R.string.transaction_not_icc);
        } else if (transactionResult == QPOSService.TransactionResult.SELECT_APP_FAIL) {
            msg = getString(R.string.transaction_app_fail);
        } else if (transactionResult == QPOSService.TransactionResult.DEVICE_ERROR) {
            msg = getString(R.string.transaction_device_error);
        } else if (transactionResult == QPOSService.TransactionResult.TRADE_LOG_FULL) {
            msg = "the trade log has fulled!pls clear the trade log!";
        } else if (transactionResult == QPOSService.TransactionResult.CARD_NOT_SUPPORTED) {
            msg = getString(R.string.card_not_supported);
        } else if (transactionResult == QPOSService.TransactionResult.MISSING_MANDATORY_DATA) {
            msg = getString(R.string.missing_mandatory_data);
        } else if (transactionResult == QPOSService.TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
            msg = getString(R.string.card_blocked_or_no_evm_apps);
        } else if (transactionResult == QPOSService.TransactionResult.INVALID_ICC_DATA) {
            msg = getString(R.string.invalid_icc_data);
        } else if (transactionResult == QPOSService.TransactionResult.FALLBACK) {
            msg = "trans fallback";
        } else if (transactionResult == QPOSService.TransactionResult.NFC_TERMINATED) {
            msg = "NFC Terminated";
        } else if (transactionResult == QPOSService.TransactionResult.CARD_REMOVED) {
            msg = "CARD REMOVED";
        } else if (transactionResult == QPOSService.TransactionResult.CONTACTLESS_TRANSACTION_NOT_ALLOW) {
            msg = "TRANS NOT ALLOW";
        } else if (transactionResult == QPOSService.TransactionResult.CARD_BLOCKED) {
            msg = "CARD BLOCKED";
        } else if (transactionResult == QPOSService.TransactionResult.TRANS_TOKEN_INVALID) {
            msg = "TOKEN INVALID";
        } else if (transactionResult == QPOSService.TransactionResult.APP_BLOCKED) {
            msg = "APP BLOCKED";
        }
        initInfo();
        if (!"".equals(msg)) {
            if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
                autoTrade(msg);
            } else {
                Mydialog.ErrorDialog((Activity) getApplicationInstance, msg, new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm() {
                        ((Activity) getApplicationInstance).finish();
                        Mydialog.ErrorDialog.dismiss();

                    }
                });
            }
        }
    }

    @Override
    public void onRequestBatchData(String tlv) {
        dismissDialog();
        TRACE.d("ICC trade finished");
        String content = getString(R.string.batch_data);
        content += tlv;
        if(getApplicationInstance!=null) {
            Intent intent = new Intent(getApplicationInstance, SuccessActivity.class);
            intent.putExtra("paytment", "payment");
            intent.putExtra("tradeResut", content);
            getApplicationInstance.startActivity(intent);
            ((Activity) getApplicationInstance).finish();
        }
        Constants.transData.setInputMoney("");
        Constants.transData.setPayType("");
        Constants.transData.setCashbackAmounts("");
        Constants.transData.setPayment("");
    }


    @Override
    public void onQposIdResult(Hashtable<String, String> posIdTable) {
        dismissDialog();
        TRACE.w("onQposIdResult():" + posIdTable.toString());
        String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
        String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
        String psamId = posIdTable.get("psamId") == null ? "" : posIdTable.get("psamId");
        String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable.get("nfcID");
        String content = "";
        content += getString(R.string.posId) + posId + "\n";
        content += "csn: " + csn + "\n";
        content += "conn: " + pos.getBluetoothState() + "\n";
        content += "psamId: " + psamId + "\n";
        content += "NFCId: " + NFCId + "\n";
        Constants.transData.setSN(posId);
          if("getposid".equals(Constants.transData.getPayType())){
            Constants.transData.setPosId(content);
            getposInfo("posid");
        }
    }

    @Override
    public void onRequestSelectEmvApp(ArrayList<String> appList) {
        TRACE.d("onRequestSelectEmvApp():" + appList.toString());
        TRACE.d("Please select App -- S，emv card config");
        dismissDialog();
        dialog = new Dialog(getApplicationInstance);
        dialog.setContentView(R.layout.emv_app_dialog);
        dialog.setTitle(R.string.please_select_app);
        String[] appNameList = new String[appList.size()];
        for (int i = 0; i < appNameList.length; ++i) {

            appNameList[i] = appList.get(i);
        }
        appListView = (ListView) dialog.findViewById(R.id.appList);
        appListView.setAdapter(new ArrayAdapter<String>(getApplicationInstance, android.R.layout.simple_list_item_1, appNameList));
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                pos.selectEmvApp(position);
                TRACE.d("select emv app position = " + position);
                dismissDialog();
            }

        });
        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pos.cancelSelectEmvApp();
                dismissDialog();
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
        TRACE.d("onRequestWaitingUser()");
        dismissDialog();
//            mllchrccard.setVisibility(View.VISIBLE);

    }

    @Override
    public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
        super.onQposRequestPinResult(dataList, offlineTime);
        dataLists = dataList;
        Log.w("dataList","dataList=="+dataList);
        for (int i=0;i<dataList.size();i++){
            Log.w("dataList","dataList="+ dataList.get(i));
        }
        Log.w("myQposClass","PaymentUartActivity.flag=="+PaymentUartActivity.flag);
        try {
            dismissDialog();
//            if(!PaymentUartActivity.flag) {
                Log.w("myQposClass","PaymentUartActivity.flag=22="+PaymentUartActivity.flag);
                MyKeyboardView.setKeyBoardListener(new KeyBoardNumInterface() {
                    @Override
                    public void getNumberValue(String value) {
                        datavalue = value;
                        Log.w("myQposClass","PaymentUartActivity.flag=23="+value);
                        pos.pinMapSync(value, 20);
                    }
                });
                if (getApplicationInstance != null) {
//                    PowerManager pm = (PowerManager) getApplicationInstance.getSystemService(Context.POWER_SERVICE);
//                    boolean isScreenOn = pm.isInteractive();
//                    if(isScreenOn) {
                        keyboardUtil = new KeyboardUtil((Activity) getApplicationInstance, dataLists);
//                    }
//                }
            }
        } catch (Exception e) {
            Log.e("e", "e:" + e);
        }
    }

    @Override
    public void onReturnGetKeyBoardInputResult(String result) {
        super.onReturnGetKeyBoardInputResult(result);
        Log.w("checkUactivity", "onReturnGetKeyBoardInputResult");
    }

    @Override
    public void onReturnGetPinInputResult(int num) {
        super.onReturnGetPinInputResult(num);
        String s = "";
        if (num == -1) {
//                if (keyboardUtil != null) {
//                    keyboardUtil.hide();
//                    pinpadEditText.setVisibility(View.GONE);
//                }
        } else {
            for (int i = 0; i < num; i++) {
                s += "*";
            }
//                pinpadEditText.setText(s);
            KeyboardUtil.pinpadEditText.setText(s);
        }
    }

    @Override
    public void onRequestSetAmount() {
        TRACE.d("onRequestSetAmount()");
        String transactionTypeString = Constants.transData.getPayType();
        QPOSService.TransactionType transactionType = QPOSService.TransactionType.GOODS;
        if (transactionTypeString != null) {
            if (transactionTypeString.equals("GOODS")) {
                transactionType = QPOSService.TransactionType.GOODS;
            } else if (transactionTypeString.equals("SERVICES")) {
                transactionType = QPOSService.TransactionType.SERVICES;
            } else if (transactionTypeString.equals("CASH")) {
                transactionType = QPOSService.TransactionType.CASH;
            } else if (transactionTypeString.equals("CASHBACK")) {
                transactionType = QPOSService.TransactionType.CASHBACK;
            } else if (transactionTypeString.equals("INQUIRY")) {
                transactionType = QPOSService.TransactionType.INQUIRY;
            } else if (transactionTypeString.equals("TRANSFER")) {
                transactionType = QPOSService.TransactionType.TRANSFER;
            } else if (transactionTypeString.equals("ADMIN")) {
                transactionType = QPOSService.TransactionType.ADMIN;
            } else if (transactionTypeString.equals("CASHDEPOSIT")) {
                transactionType = QPOSService.TransactionType.CASHDEPOSIT;
            } else if (transactionTypeString.equals("PAYMENT")) {
                transactionType = QPOSService.TransactionType.PAYMENT;
            } else if (transactionTypeString.equals("PBOCLOG||ECQ_INQUIRE_LOG")) {
                transactionType = QPOSService.TransactionType.PBOCLOG;
            } else if (transactionTypeString.equals("SALE")) {
                transactionType = QPOSService.TransactionType.SALE;
            } else if (transactionTypeString.equals("PREAUTH")) {
                transactionType = QPOSService.TransactionType.PREAUTH;
            } else if (transactionTypeString.equals("ECQ_DESIGNATED_LOAD")) {
                transactionType = QPOSService.TransactionType.ECQ_DESIGNATED_LOAD;
            } else if (transactionTypeString.equals("ECQ_UNDESIGNATED_LOAD")) {
                transactionType = QPOSService.TransactionType.ECQ_UNDESIGNATED_LOAD;
            } else if (transactionTypeString.equals("ECQ_CASH_LOAD")) {
                transactionType = QPOSService.TransactionType.ECQ_CASH_LOAD;
            } else if (transactionTypeString.equals("ECQ_CASH_LOAD_VOID")) {
                transactionType = QPOSService.TransactionType.ECQ_CASH_LOAD_VOID;
            } else if (transactionTypeString.equals("CHANGE_PIN")) {
                transactionType = QPOSService.TransactionType.UPDATE_PIN;
            } else if (transactionTypeString.equals("REFOUND")) {
                transactionType = QPOSService.TransactionType.REFUND;
            } else if (transactionTypeString.equals("SALES_NEW")) {
                transactionType = QPOSService.TransactionType.SALES_NEW;
            }
            String amounts = Constants.transData.getInputMoney();
            String cashbackAmounts = Constants.transData.getCashbackAmounts();
            Log.w("amounts", "amounts==" + amounts);
            Log.w("cashbackAmounts", "cashbackAmounts==" + cashbackAmounts);
            if (cashbackAmounts == null) {
                cashbackAmounts = "";
            }
            pos.setAmount(amounts, cashbackAmounts, "156", transactionType);
        }
    }

    /**
     * @see QPOSService.QPOSServiceListener#onRequestIsServerConnected()
     */
    @Override
    public void onRequestIsServerConnected() {
        TRACE.d("onRequestIsServerConnected()");
        pos.isServerConnected(true);
    }

    @Override
    public void onRequestOnlineProcess(final String tlv) {
        TRACE.d("onRequestOnlineProcess" + tlv);
        dismissDialog();
        Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
        TRACE.d("anlysEmvIccData(tlv):" + decodeData.toString());
        OkGo.<String>post(Constants.backendUploadUrl)
                .tag(this)
                .headers("X-RapidAPI-Key", Constants.rapidAPIKey)
                .headers("X-RapidAPI-Host", Constants.rapidAPIHost)
                .params("tlv", tlv)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        TRACE.i("onStart==");
                        Mydialog.loading((Activity) getApplicationInstance, getString(R.string.networking));
                    }


                    @Override
                    public void onSuccess(Response<String> response) {
                        dismissDialog();
                        String str = "8A023030";//Currently the default value,
                        // should be assigned to the server to return data,
                        // the data format is TLV
                        pos.sendOnlineProcessResult(str);//Script notification/55domain/ICCDATA
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        dismissDialog();
                        return null;
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dismissDialog();
                        initInfo();
                        TRACE.i("onError==");
                        if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
                            if(getApplicationInstance!=null){
                            Toast.makeText(getApplicationInstance,getString(R.string.network_failed),Toast.LENGTH_SHORT).show();
                            }
                            pos.sendOnlineProcessResult("8A025A33");
                        }else {
                            Mydialog.ErrorDialog((Activity) getApplicationInstance, getString(R.string.network_failed), new Mydialog.OnMyClickListener() {
                                @Override
                                public void onCancel() {

                                }

                                @Override
                                public void onConfirm() {
                                    pos.sendOnlineProcessResult("8A025A33");
                                }
                            });

                        }
                    }
                });

    }

    @Override
    public void onRequestTime() {
        TRACE.d("onRequestTime");
        dismissDialog();
        String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        pos.sendTime(terminalTime);
    }


    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        dismissDialog();
        String msg = "";
//        if (displayMsg == QPOSService.Display.CLEAR_DISPLAY_MSG) {
//            msg = "";
//        } else if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
//                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PaymentActivity.this);
//                builder.setTitle("Audio");
//                builder.setMessage("Success,Contine ready");
//                builder.setPositiveButton("Confirm", null);
//                builder.show();
//        } else if (displayMsg == QPOSService.Display.PLEASE_WAIT) {
//            msg = getString(R.string.wait);
//        } else if (displayMsg == QPOSService.Display.REMOVE_CARD) {
//            msg = getString(R.string.remove_card);
//        } else if (displayMsg == QPOSService.Display.TRY_ANOTHER_INTERFACE) {
//            msg = getString(R.string.try_another_interface);
//        else if (displayMsg == QPOSService.Display.PROCESSING) {
//
//            msg = getString(R.string.processing);
//
//        } else if (displayMsg == QPOSService.Display.INPUT_PIN_ING) {
//            msg = "please input pin on pos";
//
//        } else if (displayMsg == QPOSService.Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == QPOSService.Display.INPUT_LAST_OFFLINE_PIN) {
//            msg = "please input offline pin on pos";

//        } else
            if (displayMsg == QPOSService.Display.MAG_TO_ICC_TRADE) {
            msg = "please insert chip card on pos";
//        } else if (displayMsg == QPOSService.Display.CARD_REMOVED) {
//            msg = "card removed";
//        } else if (displayMsg == QPOSService.Display.TRANSACTION_TERMINATED) {
//            msg = "transaction terminated";
//                mrllayout.setVisibility(View.GONE);
        }else if(displayMsg == QPOSService.Display.PlEASE_TAP_CARD_AGAIN){
                msg = getString(R.string.please_tap_card_again);
        }
//            Log.w("displayMsg==", "displayMsg==" + msg);
//            Toast.makeText(CheckActivity.this, msg, Toast.LENGTH_SHORT).show();
//            Mydialog.loading(PaymentActivity.this, msg);
        if (msg != null && !"".equals(msg)) {
            if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
                if (!msg.equals(getApplicationInstance.getString(R.string.bad_swipe))) {
                    autoTrade(msg);
                }else{
                    Toast.makeText(getApplicationInstance,msg,Toast.LENGTH_SHORT).show();
                }
            } else {
                Mydialog.ErrorDialog((Activity) getApplicationInstance, msg, null);
            }
        }
    }



    @Override
    public void onRequestNoQposDetected() {
        TRACE.d("onRequestNoQposDetected()");
        dismissDialog();
        Mydialog.ErrorDialog((Activity) getApplicationInstance, getString(R.string.no_device_detected), new Mydialog.OnMyClickListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onConfirm() {
                ((Activity) getApplicationInstance).finishAffinity();
            }
        });
        Log.w("onRequestNoQposDetected", "No pos detected.");
    }

    @Override
    public void onRequestQposConnected() {
        TRACE.d("onRequestQposConnected()");
        dismissDialog();
        if (Constants.transData.getSN()==null) {
            pos.getQposId();
        }
    }
    @Override
    public void onRequestQposDisconnected() {
        dismissDialog();
        if (!SettingFragment.uartFlag) {
            Mydialog.ErrorDialog((Activity) getApplicationInstance, getString(R.string.device_unplugged), null);
        }
        SettingFragment.uartFlag=false;
        disconnect = true;
        TRACE.d("onRequestQposDisconnected()");
    }

    @Override
    public void onError(QPOSService.Error errorState) {
        TRACE.d("onError: " + errorState.toString());

        String msg = "";
        if (errorState == QPOSService.Error.CMD_NOT_AVAILABLE) {
            msg = getString(R.string.command_not_available);
        } else if (errorState == QPOSService.Error.TIMEOUT) {
            msg = getString(R.string.payment_timeout);
        } else if (errorState == QPOSService.Error.DEVICE_RESET) {
            dismissDialog();
//            msg = getString(R.string.device_reset);
        } else if (errorState == QPOSService.Error.UNKNOWN) {
            msg = getString(R.string.unknown_error);
        } else if (errorState == QPOSService.Error.DEVICE_BUSY) {
            if(!disconnect) {
            pos.resetPosStatus();
            }
            msg = getString(R.string.device_busy);
        } else if (errorState == QPOSService.Error.INPUT_OUT_OF_RANGE) {
            msg = getString(R.string.out_of_range);
        } else if (errorState == QPOSService.Error.INPUT_INVALID_FORMAT) {
            msg = getString(R.string.invalid_format);
        } else if (errorState == QPOSService.Error.INPUT_ZERO_VALUES) {
            msg = getString(R.string.zero_values);
        } else if (errorState == QPOSService.Error.INPUT_INVALID) {
            msg = getString(R.string.input_invalid);
        } else if (errorState == QPOSService.Error.CASHBACK_NOT_SUPPORTED) {
            msg = getString(R.string.cashback_not_supported);
        } else if (errorState == QPOSService.Error.CRC_ERROR) {
            msg = getString(R.string.crc_error);
        } else if (errorState == QPOSService.Error.COMM_ERROR) {
            msg = getString(R.string.comm_error);
        } else if (errorState == QPOSService.Error.MAC_ERROR) {
            msg = getString(R.string.mac_error);
        } else if (errorState == QPOSService.Error.APP_SELECT_TIMEOUT) {
            msg = getString(R.string.app_select_timeout_error);
        } else if (errorState == QPOSService.Error.CMD_TIMEOUT) {
            msg = getString(R.string.cmd_timeout);
        } else if (errorState == QPOSService.Error.ICC_ONLINE_TIMEOUT) {
            if (pos == null) {
                return;
            }
            pos.resetPosStatus();
            msg = getString(R.string.device_reset);
        }
        if(!disconnect) {
            dismissDialog();
            if (!"".equals(msg)) {
                if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
                    autoTrade(msg);
                } else {
                    Mydialog.ErrorDialog((Activity) getApplicationInstance, msg, new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onConfirm() {
                            if (!"com.dspread.demoui.activity.MainActivity".equals(getApplicationInstance.getClass().getName())) {
                                ((Activity) getApplicationInstance).finish();
                            }
                            Mydialog.ErrorDialog.dismiss();
                        }
                    });
                }
            }

        }
        initInfo();
    }

    @Override
    public void onReturnReversalData(String tlv) {
        String content = getString(R.string.reversal_data);
        content += tlv;
        TRACE.d("onReturnReversalData(): " + tlv);
//        Intent intent = new Intent(getApplicationInstance,SuccessActivity.class);
//        intent.putExtra("paytment","payment");
//        intent.putExtra("tradeResut",content);
//        getApplicationInstance.startActivity(intent);
//        ((Activity) getApplicationInstance).finish();
    }



    @Override
    public void onReturnGetPinResult(Hashtable<String, String> result) {
        TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
        String pinBlock = result.get("pinBlock");
        String pinKsn = result.get("pinKsn");
        String content = "get pin result\n";
        content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
        content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
        TRACE.i(content);
    }


    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
        TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
        dismissDialog();
        String msg="";
        if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
            msg="update work key success";
        } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
            msg="update work key fail";
        } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
            msg="update work key packet vefiry error";
        } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
            msg="update work key packet len error";
        }
        ShowInfoDialog(msg);
    }

    @Override
    public void onReturnCustomConfigResult(boolean isSuccess, String result) {
        TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + "--result--" + result);
        String upgradeInfo;
        Mydialog.Ldialog.dismiss();
        if (isSuccess) {
            upgradeInfo = "update EMV success";
        } else {
            upgradeInfo = "update EMV Fail";
        }
        ShowInfoDialog(upgradeInfo);

    }



    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess) {
        TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
        String upgradeInfo;
        if (isSuccess) {
            upgradeInfo = "Set Masterkey Success";
        } else {
            upgradeInfo = "Set Masterkey Fail";
        }
        ShowInfoDialog(upgradeInfo);
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
    public void onLcdShowCustomDisplay(boolean arg0) {
        TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
    }

    @Override
    public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
        TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
        dismissDialog();
        if (arg0 != QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
            DeviceUpdataFragment.UpdateThread.concelFlag = true;
            ShowInfoDialog("update firmware fail");
        } else {

        }
    }

    @Override
    public void onReturnUpdateIPEKResult(boolean arg0) {
        TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);
        String upgradeInfo;
        if (arg0) {
            upgradeInfo = "update IPEK success";
        } else {
            upgradeInfo = "update IPEK Fail";
        }
        ShowInfoDialog(upgradeInfo);

    }



    @Override
    public void onBluetoothBoardStateResult(boolean arg0) {
        TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
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
    public void onRequestUpdateKey(String arg0) {
        TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
        Constants.transData.setUpdateCheckValue("update checkvalue : " + arg0);
        getposInfo("updatekey");
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
    public void onGetKeyCheckValue(Hashtable<String, String> checkValue) {
        super.onGetKeyCheckValue(checkValue);
        dismissDialog();

        String MKSK_TMK_KCV = "MKSK_TMK_KCV : " + checkValue.get("MKSK_TMK_KCV");
        String DUKPT_PIN_IPEK_KCV = "DUKPT_PIN_IPEK_KCV : " + checkValue.get("DUKPT_PIN_IPEK_KCV");
        String DUKPT_PIN_KSN = "DUKPT_PIN_KSN : " + checkValue.get("DUKPT_PIN_KSN");
        String DUKPT_EMV_IPEK_KCV = "DUKPT_EMV_IPEK_KCV : " + checkValue.get("DUKPT_EMV_IPEK_KCV");
        String DUKPT_EMV_KSN = "DUKPT_EMV_KSN : " + checkValue.get("DUKPT_EMV_KSN");
        String DUKPT_TRK_IPEK_KCV = "DUKPT_TRK_IPEK_KCV : " + checkValue.get("DUKPT_TRK_IPEK_KCV");
        String DUKPT_TRK_KSN = "DUKPT_TRK_KSN : " + checkValue.get("DUKPT_TRK_KSN");
        String MKSK_PIK_KCV = "MKSK_PIK_KCV : " + checkValue.get("MKSK_PIK_KCV");
        String MKSK_TDK_KCV = "MKSK_TDK_KCV : " + checkValue.get("MKSK_TDK_KCV");
        String MKSK_MCK_KCV = "MKSK_MCK_KCV : " + checkValue.get("MKSK_MCK_KCV");
        String TCK_KCV = "TCK_KCV : " + checkValue.get("TCK_KCV");
        String MAGK_KCV = "MAGK_KCV : " + checkValue.get("MAGK_KCV");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(MKSK_TMK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_PIN_IPEK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_PIN_KSN);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_EMV_IPEK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_EMV_KSN);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_TRK_IPEK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_TRK_KSN);
        stringBuffer.append("\n");
        stringBuffer.append(MKSK_PIK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(MKSK_TDK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(MKSK_MCK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(TCK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(MAGK_KCV);

        Constants.transData.setKeyCheckValue(stringBuffer.toString());
        getposInfo("keycheckvalue");
    }



    @Override
    public void onTradeCancelled() {
        TRACE.d("onTradeCancelled");
        dismissDialog();
    }


    @Override
    public void onReturnDeviceSigningCertResult(String certificates, String certificatesTree) {
        TRACE.d("onReturnDeviceSigningCertResult:" + certificates + "\n" + certificatesTree);
        String command = "    <string name=\"pedi_command\">&#091;AOPEDI&#059;VS2&#059;RF0&#059;TD1&#059;CC%s&#059;RG%s&#059;CE%s&#059;&#093;</string>\n";
        command = ParseASN1Util.addTagToCommand(command, "CD", certificates);
        TRACE.i("request the RKMS command is " + command);
        String pediRespose = "[AOPEDI;ANY;CC308203B33082029BA00302010202074EB0D60000987E300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330363030303030305A170D3330303330373030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D56311D301B06035504410C14447370726561645F417573524B4D533130312D56311D301B06035504030C14447370726561645F417573524B4D533130312D5630820122300D06092A864886F70D01010105000382010F003082010A0282010100D7FD40DD513EE82491FABA3EB734C3FE69C79973797007A2183EC9C468F73D8E1CB669DDA6DC32CA125F9FAEAC0C0556893C9196FB123B06BC9B880EEF367CD17000C7E0ECF7313DD2D396F29C8D977A65946258BE5A4133462F0675161407EED3D263BC20E9271B9070DCC1A6376F89E7E9E2B304BC756E3E3B61B869A2E39F11067D00B5BA3817673A730F42DC4C037FC214207C70A1E3E43F7D7494E71EBDD5BB0E9AFAE32E422DB90B85E230DF406FB12470AD0360FD7BDFDD1A29BCE91655A835129858A0E9EB04845A80F1E9F8EAA20C67C6B8A61113D6FFDD7DF5719778A03A30F69B0DD9033D5E975F723CC18792CC6988250A7DBD20901450651A810203010001300D06092A864886F70D01010B050003820101008F002AE3AFB49C2E7D99CC7B933617D180CB4E8EA13CBCBE7469FC4E5124033F06E4C3B0DAB3C6CA4625E3CD53E7B86C247CDF100E266059366F8FEEC746507E1B0D0924029805AAB89FCE1482946B8B0C1F546DD56B399AB48891B731148C878EF4D02AE641717A3D381C7B62011B76A6FFBF20846217EB68149C96B4B134F980060A542DBE2F32BF7AD308F26A279B41C65E32D4E260AE68B3010685CE36869EFF09D211CE64401F417A72F29F49A2EE713ACC37C29AECBFEBE571EF11D883815F54FA3E52A917CC3D6B008A3E3C52164FF5591D869026D248873F15DE531104F329C279FC5B6BC28ABC833F8C31BEF47783A5D5B9C534A57530D9AE463DC3;CD308203B33082029BA00302010202074EB0D60000987C300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330373030303030305A170D3330303330383030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D45311D301B06035504410C14447370726561645F417573524B4D533130312D45311D301B06035504030C14447370726561645F417573524B4D533130312D4530820122300D06092A864886F70D01010105000382010F003082010A0282010100A62A4935B57BA478F41B6C8B3F79E84DB61E516FEC8D5BE3E86FD296C6906625E0316A77F59D6D5075811BA7BB0801366BA7E370B758E3E1DCE005008C13D368536C2216FAF8AF70EBC6B5D1D231AFD19D6270DDBEA6535B46135D1DE11F374978A655FAA8C2A0DDC933CF82E9DC69DABF8676D0E81762D9B01799C83A8DF3EE70584AA4543EBBDAB02A0EFCA6A276588893DD28BD096400E315ECF5FE91EC210EEC2BE8763FEFB57D1448CC7D0FCDC3BDCE4B7BAAD546E0E5E99281B4F1AB052E1B0361977406B6B57B32353E9F338BED29E55E2D1F65C4322B5850D45146D5A66BFE8323C0D3E78E55A8945B622E15295B9176454A868399990B31D7B104CF0203010001300D06092A864886F70D01010B05000382010100296101AC1ED80EF9DD845D03F2D1F822B4AEFD50E0A1F47FA98105155327FDA6CE52BCD650BE1EB6DCD7F3CDF73325E85EE979EF0364970ADF6ED3A247B2E3E2D83D877BEBD66B20F3983E8DF8932F82F30C3FAF980ADF72E9FEE30EBAFC42B19FB1EAEC74BAE16E2D4EF245D18B58FB560A64C9B515EA065ECA7AE81D6ED0B97A24636E1E70EE3F2F3A3364C17C6B36BE82588BBED79F23914D4E4E7E1E3FC2A5438FAB0535D37D6FA52009ACD37B6F413700BBF440B6B94E4F12C7F465B8AAC2A03776AAB9AFBAE42FE19664DC0B4E3D8A90EB185529CABE39335AEC58295E1E073A765733410FD769345E9B99C0AA0CBE3FA815661857DCF7EA3BD35EFB4C;RD04916CCC6289600A55118FC37AF0999E;]";
        String cc = ParseASN1Util.parseToken(pediRespose, "CC");
        String cd = ParseASN1Util.parseToken(pediRespose, "CD");
        BASE64Decoder base64Decoder = new BASE64Decoder();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getposInfo(String posinfo) {
        Intent intent = new Intent(getApplicationInstance, SuccessActivity.class);
        intent.putExtra("posinfo", posinfo);
        getApplicationInstance.startActivity(intent);
    }

    public void ShowInfoDialog(String upgradeInfo) {

        Mydialog.ShowInfoDialog(getApplicationInstance, upgradeInfo, new Mydialog.OnMyClickListener() {
            @Override
            public void onCancel() {
                Mydialog.ShowInfoDialog.dismiss();
            }

            @Override
            public void onConfirm() {
                Mydialog.ShowInfoDialog.dismiss();
            }
        });
    }

    private void sendRequestToBackend(String data) {
        dismissDialog();
        OkGo.<String>post(Constants.backendUploadUrl)
                .tag(this)
                .headers("X-RapidAPI-Key", Constants.rapidAPIKey)
                .headers("X-RapidAPI-Host", Constants.rapidAPIHost)
                .params("data", data)
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                        TRACE.i("onStart==");
                        Mydialog.loading((Activity) getApplicationInstance, getString(R.string.networking));
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        dismissDialog();
                        Log.w("application","getApplicationInstance="+getApplicationInstance);
                        if(getApplicationInstance!=null) {
                            Intent intent = new Intent(getApplicationInstance, SuccessActivity.class);
                            intent.putExtra("paytment", "payment");
                            intent.putExtra("tradeResut", data);
                            getApplicationInstance.startActivity(intent);
                            ((Activity) getApplicationInstance).finish();
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        dismissDialog();
                        return null;
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        dismissDialog();
                        initInfo();
                        if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
                            autoTrade(getString(R.string.network_failed));
                        }else {
                            TRACE.i("onError==");
                            Mydialog.ErrorDialog((Activity) getApplicationInstance, getString(R.string.network_failed), null);
                        }
                        }
                });
    }

    public void initInfo() {
        if (Constants.transData.getPosId() != null && !"".equals(Constants.transData.getPosId())) {
            Constants.transData.setPosId("");
        }
        if (Constants.transData.getPosInfo() != null && !"".equals(Constants.transData.getPosInfo())) {
            Constants.transData.setPosInfo("");
        }
        if (Constants.transData.getPayment() != null && !"".equals(Constants.transData.getPayment())) {
            Constants.transData.setPayment("");
        }
        if (Constants.transData.getCashbackAmounts() != null && !"".equals(Constants.transData.getCashbackAmounts())) {
            Constants.transData.setCashbackAmounts("");
        }
        if (Constants.transData.getUpdateCheckValue() != null && !"".equals(Constants.transData.getUpdateCheckValue())) {
            Constants.transData.setUpdateCheckValue("");

        }
        if (Constants.transData.getKeyCheckValue() != null && !"".equals(Constants.transData.getKeyCheckValue())) {
            Constants.transData.setKeyCheckValue("");
        }
        if (Constants.transData.getInputMoney() != null && !"".equals(Constants.transData.getInputMoney())) {
            Constants.transData.setInputMoney("");
        }
        if (Constants.transData.getPayType() != null && !"".equals(Constants.transData.getPayType())) {
            Constants.transData.setPayType("");
        }

    }

    public static void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
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

    public void autoTrade(String msg) {
        dismissDialog();
        if(getApplicationInstance!=null) {
            Toast.makeText(getApplicationInstance, msg, Toast.LENGTH_SHORT).show();
        }
        if ("autoTrade".equals(Constants.transData.getAutoTrade())) {
            int failKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.FailKey, 0);
            SpUtils.putInt(getApplicationInstance, ConstantUtil.FailKey,failKey + 1);
            Constants.transData.setFialSub(failKey);
           // Constants.transData.setFialSub(Constants.transData.getFialSub() + 1);


            int subKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.SubKey, 0);
            SpUtils.putInt(getApplicationInstance,ConstantUtil.SubKey,subKey + 1);
            Constants.transData.setSub(subKey);


            //Constants.transData.setSub(Constants.transData.getSub() + 1);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(getApplicationInstance!=null) {
                ((Activity) getApplicationInstance).finish();
            }

        }
    }
}
