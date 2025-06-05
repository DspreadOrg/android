package com.dspread.pos.ui.payment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.lifecycle.Observer;

import com.dspread.pos.posAPI.MyCustomQPOSCallback;
import com.dspread.pos.common.manager.QPOSCallbackManager;
import com.dspread.pos.posAPI.POSCommand;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.payment.pinkeyboard.KeyboardUtil;
import com.dspread.pos.ui.payment.pinkeyboard.MyKeyboardView;
import com.dspread.pos.ui.payment.pinkeyboard.PinPadDialog;
import com.dspread.pos.ui.payment.pinkeyboard.PinPadView;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.LogFileConfig;
import com.dspread.pos.utils.QPOSUtil;
import com.dspread.pos.utils.ReceiptGenerator;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentBinding;
import com.dspread.xpos.QPOSService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;

public class PaymentActivity extends BaseActivity<ActivityPaymentBinding, PaymentViewModel> implements MyCustomQPOSCallback {
    
    private String amount;
    private String transactionTypeString;
    private String cashbackAmounts;
    private QPOSService.TransactionType transactionType;
    private KeyboardUtil keyboardUtil;
    private boolean isChangePin = false;
    private int timeOfPinInput;
    public PinPadDialog pinPadDialog;
    private boolean isICC;
    private LogFileConfig logFileConfig;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_payment;
    }
    
    @Override
    public int initVariableId() {
        return BR.viewModel;
    }
    
    @Override
    public void initData() {
        logFileConfig = LogFileConfig.getInstance(this);
        QPOSCallbackManager.getInstance().registerCallback(MyCustomQPOSCallback.class, this);
        binding.setVariable(BR.viewModel, viewModel);
        viewModel.setmContext(this);
        binding.pinpadEditText.setText("");
        viewModel.titleText.set("Paymenting");
        Intent intent = getIntent();
        if (intent != null) {
            amount = intent.getStringExtra("amount");
            if(SPUtils.getInstance().getString("transactionType") != null && !"".equals(SPUtils.getInstance().getString("transactionType"))){
                transactionTypeString = SPUtils.getInstance().getString("transactionType");
            }else {
                transactionTypeString = "GOODS";
            }
            cashbackAmounts = intent.getStringExtra("cashbackAmounts");
            viewModel.setAmount(amount);
        }

        startTransaction();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.isOnlineSuccess.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    if(isICC){
                        POSCommand.getInstance().sendOnlineProcessResult("8A023030");
                    }else {
                        if(DeviceUtils.isPrinterDevices()){
                            handleSendReceipt();
                        }
                        viewModel.setTransactionSuccess();
                    }
                }else {
                    if(isICC){
                        POSCommand.getInstance().sendOnlineProcessResult("8A023035");
                    }else {
                        viewModel.setTransactionFailed("Transaction failed because of the network!");
                    }
                }
            }
        });
        viewModel.isContinueTrx.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    startTransaction();
                }
            }
        });
    }

    private void startTransaction() {
        isICC = false;
        POSCommand.getInstance().setCardTradeMode();
        POSCommand.getInstance().doTrade(20);
    }

    @Override
    public void onRequestSetAmount() {
        TRACE.d("onRequestSetAmount()");
        if (transactionTypeString != null) {
            if (transactionTypeString.equals("GOODS")) {
                transactionType = QPOSService.TransactionType.GOODS;
            } else if (transactionTypeString.equals("SERVICES")) {
                transactionType = QPOSService.TransactionType.SERVICES;
            } else if (transactionTypeString.equals("CASH")) {
                transactionType = QPOSService.TransactionType.CASH;
            } else if (transactionTypeString.equals("CASHBACK")) {
                transactionType = QPOSService.TransactionType.CASHBACK;
            } else if (transactionTypeString.equals("PURCHASE_REFUND")) {
                transactionType = QPOSService.TransactionType.REFUND;
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
            int currencyCode = SPUtils.getInstance().getInt("currencyCode");
            if(currencyCode == -1 || currencyCode == 0){
                currencyCode = 156;
            }
            TRACE.i("currencyCode = "+String.valueOf(currencyCode));
            POSCommand.getInstance().setAmount(amount, cashbackAmounts, String.valueOf(currencyCode), transactionType);
        }
    }

    @Override
    public void onRequestWaitingUser() {
        viewModel.setWaitingStatus(true);
    }

    @Override
    public void onRequestTime() {
        dismissDialog();
        String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        TRACE.d("onRequestTime: "+terminalTime);
        POSCommand.getInstance().sendTime(terminalTime);
    }

    @Override
    public void onRequestSelectEmvApp(ArrayList<String> appList) {
        TRACE.d("onRequestSelectEmvApp():" + appList.toString());
        runOnUiThread(() -> {
         Dialog  dialog = new Dialog(PaymentActivity.this);
            dialog.setContentView(R.layout.emv_app_dialog);
            dialog.setTitle(R.string.please_select_app);
            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {

                appNameList[i] = appList.get(i);
            }
            ListView  appListView = (ListView) dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<String>(PaymentActivity.this, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    POSCommand.getInstance().selectEmvApp(position);
                    TRACE.d("select emv app position = " + position);
                    dismissDialog();
                }

            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    POSCommand.getInstance().cancelSelectEmvApp();
                    dismissDialog();
                }
            });
            dialog.show();
        });
    }

    @Override
    public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
        TRACE.d("onQposRequestPinResult = " + dataList+"\nofflineTime: "+offlineTime);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (POSCommand.getInstance().pos != null) {
                    viewModel.stopLoading();
                    viewModel.showPinpad.set(true);
                    boolean onlinePin = POSCommand.getInstance().isOnlinePin();
                    if (keyboardUtil != null) {
                        keyboardUtil.hide();
                    }
                    if (isChangePin) {
                        if (timeOfPinInput == 1) {
                            viewModel.titleText.set(getString(R.string.input_new_pin_first_time));
                        } else if (timeOfPinInput == 2) {
                            viewModel.titleText.set(getString(R.string.input_new_pin_confirm));
                            timeOfPinInput = 0;
                        }

                    } else {
                        if (onlinePin) {
                            viewModel.titleText.set(getString(R.string.input_onlinePin));
                        } else {
                            int cvmPinTryLimit = POSCommand.getInstance().getCvmPinTryLimit();
                            TRACE.d("PinTryLimit:" + cvmPinTryLimit);
                            if (cvmPinTryLimit == 1) {
                                viewModel.titleText.set(getString(R.string.input_offlinePin_last));
                            } else {
                                viewModel.titleText.set(getString(R.string.input_offlinePin));
                            }
                        }
                    }
                }
                MyKeyboardView.setKeyBoardListener(value -> {
                    if (POSCommand.getInstance().pos != null) {
                        POSCommand.getInstance().pinMapSync(value, 20);
                    }
                });
                if (POSCommand.getInstance().pos != null) {
                    keyboardUtil = new KeyboardUtil(PaymentActivity.this, binding.scvText, dataList);
                    keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, binding.pinpadEditText);//Random keyboard
                }
            }
        });
    }

    @Override
    public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
        TRACE.d("onRequestSetPin = " + isOfflinePin+"\ntryNum: "+tryNum);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                viewModel.titleText.set(getString(R.string.input_pin));
                viewModel.stopLoading();
                viewModel.showPinpad.set(true);
            }
        });
    }

    @Override
    public void onRequestSetPin() {
        TRACE.i("onRequestSetPin()");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewModel.titleText.set(getString(R.string.input_pin));
                pinPadDialog = new PinPadDialog(PaymentActivity.this);
                pinPadDialog.getPayViewPass().setRandomNumber(true).setPayClickListener(POSCommand.getInstance().pos, new PinPadView.OnPayClickListener() {

                    @Override
                    public void onCencel() {
                        POSCommand.getInstance().cancelPin();
                        pinPadDialog.dismiss();
                    }

                    @Override
                    public void onPaypass() {
//                        POSClass.getInstance().bypassPin();
                        POSCommand.getInstance().bypassPin();
                        pinPadDialog.dismiss();
                    }

                    @Override
                    public void onConfirm(String password) {
                        String pinBlock = QPOSUtil.buildCvmPinBlock(POSCommand.getInstance().getEncryptData(), password);// build the ISO format4 pin block
                        POSCommand.getInstance().sendCvmPin(pinBlock, true);
                        pinPadDialog.dismiss();
                    }
                });
            }
        });
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
    public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
        TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewModel.showPinpad.set(false);
                String cardNo = "";
                String msg = "";
                if (POSCommand.getInstance().pos == null) {
                    msg = "Pls open device";
                    viewModel.setTransactionFailed(msg);
                    return;
                }
                if (result == QPOSService.DoTradeResult.NONE) {
                    msg = getString(R.string.no_card_detected);
                } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
                    msg = getString(R.string.try_another_interface);
                } else if (result == QPOSService.DoTradeResult.ICC) {
                    isICC = true;
                    viewModel.startLoading(getString(R.string.icc_card_inserted));
                    POSCommand.getInstance().doEmvApp(QPOSService.EmvOption.START);
                } else if (result == QPOSService.DoTradeResult.NOT_ICC) {
                    msg = getString(R.string.card_inserted);
                } else if (result == QPOSService.DoTradeResult.BAD_SWIPE) {
                    msg = getString(R.string.bad_swipe);
                } else if (result == QPOSService.DoTradeResult.CARD_NOT_SUPPORT) {
                    msg = "GPO NOT SUPPORT";
                } else if (result == QPOSService.DoTradeResult.PLS_SEE_PHONE) {
                    msg = "PLS SEE PHONE";
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
                    }
                    if (content != null && !"".equals(content)) {
                        binding.tvReceipt.setMovementMethod(LinkMovementMethod.getInstance());
                        Spanned receiptContent = ReceiptGenerator.generateMSRReceipt(decodeData,"000013");
                        binding.tvReceipt.setText(receiptContent);
                        String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                        String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                                + ", \"tlv\": " + content + "}";
                        TRACE.i("msr result = "+receiptContent.toString());

                        viewModel.sendDingTalkMessage(false,content,data);
                    }
                } else if ((result == QPOSService.DoTradeResult.NFC_ONLINE) || (result == QPOSService.DoTradeResult.NFC_OFFLINE)) {
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
                    binding.tvReceipt.setMovementMethod(LinkMovementMethod.getInstance());
                    Spanned receiptContent = ReceiptGenerator.generateMSRReceipt(decodeData,"000015");
                    binding.tvReceipt.setText(receiptContent);
                    Hashtable<String, String> h = POSCommand.getInstance().getNFCBatchData();
                    String tlv = h.get("tlv");
                    TRACE.i("NFC Batch data: "+tlv);
                    String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                            + ", \"tlv\": " + tlv + "}";
                    viewModel.sendDingTalkMessage(false,tlv,data);
                } else if ((result == QPOSService.DoTradeResult.NFC_DECLINED)) {
                    msg = getString(R.string.transaction_declined);
                } else if (result == QPOSService.DoTradeResult.NO_RESPONSE) {
                    getString(R.string.card_no_response);
                } else {
                    msg = getString(R.string.unknown_error);
                }
                if (msg != null && !"".equals(msg)) {
                    viewModel.setTransactionFailed(msg);
                }
            }
        });
    }

    @Override
    public void onRequestOnlineProcess(final String tlv) {
        TRACE.d("onRequestOnlineProcess" + tlv);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewModel.showPinpad.set(false);
                viewModel.startLoading(getString(R.string.online_process_requested));
               Hashtable<String, String> decodeData = POSCommand.getInstance().anlysEmvIccData(tlv);
                String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                        + ", \"tlv\": " + tlv + "}";
                viewModel.sendDingTalkMessage(true,tlv,data);
            }
        });
    }

    @Override
    public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
        TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
        isChangePin = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (transactionResult == QPOSService.TransactionResult.CARD_REMOVED) {
                }
                String msg = "";
                if (transactionResult == QPOSService.TransactionResult.APPROVED) {
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
                } else {
                    msg = transactionResult.name();
                }
                if (!"".equals(msg)) {
                    viewModel.setTransactionFailed(msg);
                }
            }
        });
    }

    @Override
    public void onRequestBatchData(String tlv) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TRACE.d("ICC trade finished");
                String content = getString(R.string.batch_data);
                content += tlv;
                PaymentModel paymentModel = viewModel.setTransactionSuccess(content);
                binding.tvReceipt.setMovementMethod(LinkMovementMethod.getInstance());
                Spanned receiptContent = ReceiptGenerator.generateICCReceipt(paymentModel);
                binding.tvReceipt.setText(receiptContent);
                if(DeviceUtils.isPrinterDevices()){
                    handleSendReceipt();
                }
            }
        });
    }

    @Override
    public void onQposIsCardExist(boolean cardIsExist) {
        TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);

    }

    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String msg = "";
                if (displayMsg == QPOSService.Display.CLEAR_DISPLAY_MSG) {
                    msg = "";
                } else if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PaymentActivity.this);
                    builder.setTitle("Audio");
                    builder.setMessage("Success,Contine ready");
                    builder.setPositiveButton("Confirm", null);
                    builder.show();
                } else if (displayMsg == QPOSService.Display.PLEASE_WAIT) {
                    msg = getString(R.string.wait);
                } else if (displayMsg == QPOSService.Display.REMOVE_CARD) {
                    msg = getString(R.string.remove_card);
                } else if (displayMsg == QPOSService.Display.TRY_ANOTHER_INTERFACE) {
                    msg = getString(R.string.try_another_interface);
                } else if (displayMsg == QPOSService.Display.PROCESSING) {

                    msg = getString(R.string.processing);

                } else if (displayMsg == QPOSService.Display.INPUT_PIN_ING) {
                    msg = "please input pin on pos";

                } else if (displayMsg == QPOSService.Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == QPOSService.Display.INPUT_LAST_OFFLINE_PIN) {
                    msg = "please input offline pin on pos";

                } else if (displayMsg == QPOSService.Display.MAG_TO_ICC_TRADE) {
                    msg = "please insert chip card on pos";
                } else if (displayMsg == QPOSService.Display.CARD_REMOVED) {
                    msg = "card removed";
                } else if (displayMsg == QPOSService.Display.TRANSACTION_TERMINATED) {
                    msg = "transaction terminated";
                } else if (displayMsg == QPOSService.Display.PlEASE_TAP_CARD_AGAIN) {
                    msg = getString(R.string.please_tap_card_again);
                } else if (displayMsg == QPOSService.Display.INPUT_NEW_PIN) {
//                msg = getString(R.string.input_new_pin);
                    isChangePin = true;
                    timeOfPinInput++;
                } else if (displayMsg == QPOSService.Display.INPUT_NEW_PIN_CHECK_ERROR) {
                    msg = getString(R.string.input_new_pin_check_error);
                    timeOfPinInput = 0;
                }
                viewModel.startLoading(msg);
            }
        });
    }

    @Override
    public void onReturnReversalData(String tlv) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content = getString(R.string.reversal_data);
                content += tlv;
                TRACE.d("onReturnReversalData(): " + tlv);
                viewModel.setTransactionFailed(content);
            }
        });
    }

    @Override
    public void onReturnGetPinInputResult(int num) {
        TRACE.i("onReturnGetPinInputResult  ===" + num);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String s = "";
                if (num == -1) {
                    binding.pinpadEditText.setText("");
                    viewModel.showPinpad.set(false);
                    if (keyboardUtil != null) {
                        keyboardUtil.hide();
                    }
                } else {
                    for (int i = 0; i < num; i++) {
                        s += "*";
                    }
                    binding.pinpadEditText.setText(s);
                }
            }
        });
    }

    @Override
    public void onGetCardNoResult(String cardNo) {
        TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

    }

    @Override
    public void onGetCardInfoResult(Hashtable<String, String> cardInfo) {
    }

    @Override
    public void onEmvICCExceptionData(String tlv) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = "Transaction is reversal :\n"+tlv;
                viewModel.setTransactionFailed(msg);
            }
        });
    }

    @Override
    public void onTradeCancelled() {
        TRACE.d("onTradeCancelled");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                viewModel.setTransactionFailed("Transaction is canceled!");
            }
        });
    }

    @Override
    public void onError(QPOSService.Error error) {
        MyCustomQPOSCallback.super.onError(error);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewModel.setTransactionFailed(error.name());
                if(keyboardUtil != null){
                    keyboardUtil.hide();
                }
            }
        });

        TRACE.w(error.name());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        POSCommand.getInstance().cancelTrade();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogFileConfig.getInstance(this).readLog();
        QPOSCallbackManager.getInstance().unregisterCallback(MyCustomQPOSCallback.class);
        PrinterHelper.getInstance().close();
    }

    private void convertReceiptToBitmap(final OnBitmapReadyListener listener) {
        binding.tvReceipt.post(new Runnable() {
            @Override
            public void run() {
                if (binding.tvReceipt.getWidth() <= 0) {
                    binding.tvReceipt.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            binding.tvReceipt.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            Bitmap bitmap = viewModel.convertReceiptToBitmap(binding.tvReceipt);
                            if (listener != null) {
                                listener.onBitmapReady(bitmap);
                            }
                        }
                    });
                } else {
                    Bitmap bitmap = viewModel.convertReceiptToBitmap(binding.tvReceipt);
                    if (listener != null) {
                        listener.onBitmapReady(bitmap);
                    }
                }
            }
        });
    }

    // bitmap callback
    public interface OnBitmapReadyListener {
        void onBitmapReady(Bitmap bitmap);
    }

    private void handleSendReceipt() {
        convertReceiptToBitmap(new OnBitmapReadyListener() {
            @Override
            public void onBitmapReady(Bitmap bitmap) {
                if (bitmap != null) {
                    binding.btnSendReceipt.setVisibility(View.VISIBLE);
                } else {
                    binding.btnSendReceipt.setVisibility(View.GONE);
                }
            }
        });
    }
}