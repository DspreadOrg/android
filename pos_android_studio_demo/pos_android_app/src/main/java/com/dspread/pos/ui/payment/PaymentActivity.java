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

import com.dspread.pos.TerminalApplication;
import com.dspread.pos.common.enums.TransCardMode;
import com.dspread.pos.common.manager.QPOSCallbackManager;
import com.dspread.pos.posAPI.POS;
import com.dspread.pos.posAPI.PaymentServiceCallback;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.payment.pinkeyboard.KeyboardUtil;
import com.dspread.pos.ui.payment.pinkeyboard.MyKeyboardView;
import com.dspread.pos.ui.payment.pinkeyboard.PinPadDialog;
import com.dspread.pos.ui.payment.pinkeyboard.PinPadView;
import com.dspread.pos.utils.BitmapReadyListener;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.HandleTxnsResultUtils;
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
import me.goldze.mvvmhabit.utils.ToastUtils;

public class PaymentActivity extends BaseActivity<ActivityPaymentBinding, PaymentViewModel> implements PaymentServiceCallback {
    
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
    private int changePinTimes;

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
        QPOSCallbackManager.getInstance().registerPaymentCallback(this);
        binding.setVariable(BR.viewModel, viewModel);
        viewModel.setmContext(this);
        binding.pinpadEditText.setText("");
        viewModel.titleText.set("Paymenting");
        Intent intent = getIntent();
        if (intent != null) {
            amount = intent.getStringExtra("amount");
            SPUtils.getInstance().getString("transactionType");
            if(!SPUtils.getInstance().getString("transactionType").isEmpty()){
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
        viewModel.isOnlineSuccess.observe(this, aBoolean -> {
            if (aBoolean) {
                if (isICC) {
                    POS.getInstance().sendOnlineProcessResult("8A023030");
                } else {
                    if (DeviceUtils.isPrinterDevices()) {
                        handleSendReceipt();
                    }
                    viewModel.setTransactionSuccess();
                }
            } else {
                if (isICC) {
                    POS.getInstance().sendOnlineProcessResult("8A023035");
                } else {
                    viewModel.setTransactionFailed("Transaction failed because of the network!");
                }
            }
        });
        viewModel.isContinueTrx.observe(this, aBoolean -> {
            if (aBoolean) {
                startTransaction();
            }
        });
    }

    private void startTransaction() {
        isICC = false;
        changePinTimes = 0;
        if(!POS.getInstance().isPOSReady()){
            ToastUtils.showShort("Pls connect your devices first!");
            return;
        }
        POS.getInstance().setCardTradeMode();
        POS.getInstance().doTrade(20);
    }

    @Override
    public void onRequestSetAmount() {
        TRACE.d("onRequestSetAmount()");
        transactionType =HandleTxnsResultUtils.getTransactionType(transactionTypeString);

        // get the currency code, and default value is 156
        int currencyCode = SPUtils.getInstance().getInt("currencyCode");
        currencyCode = (currencyCode <= 0) ? 156 : currencyCode;
        TRACE.i("currencyCode = " + currencyCode + " amounts = " + amount);
        POS.getInstance().setAmount(amount, cashbackAmounts, String.valueOf(currencyCode), transactionType);
    }

    @Override
    public void onRequestWaitingUser() {
        viewModel.setWaitingStatus(true);
    }

    @Override
    public void onRequestTime() {
//        dismissDialog();
        String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        TRACE.d("onRequestTime: "+terminalTime);
        POS.getInstance().sendTime(terminalTime);
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
            ListView  appListView = dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<>(PaymentActivity.this, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener((parent, view, position, id) -> {
                POS.getInstance().selectEmvApp(position);
                TRACE.d("select emv app position = " + position);
                dialog.dismiss();
            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> {
                POS.getInstance().cancelSelectEmvApp();
                dialog.dismiss();
            });
            dialog.show();
        });
    }

    @Override
    public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
        TRACE.d("onQposRequestPinResult = " + dataList+"\nofflineTime: "+offlineTime);
        runOnUiThread(() -> {
            if (POS.getInstance().isPOSReady()) {
                viewModel.stopLoading();
                viewModel.showPinpad.set(true);
                boolean onlinePin = POS.getInstance().isOnlinePin();
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
                        int cvmPinTryLimit = POS.getInstance().getCvmPinTryLimit();
                        TRACE.d("PinTryLimit:" + cvmPinTryLimit);
                        if (cvmPinTryLimit == 1) {
                            viewModel.titleText.set(getString(R.string.input_offlinePin_last));
                        } else {
                            viewModel.titleText.set(getString(R.string.input_offlinePin));
                        }
                    }
                }
            }
            binding.pinpadEditText.setText("");
            MyKeyboardView.setKeyBoardListener(value -> {
                if (POS.getInstance().isPOSReady()) {
                    POS.getInstance().pinMapSync(value, 20);
                }
            });
            if (POS.getInstance().isPOSReady()) {
                keyboardUtil = new KeyboardUtil(PaymentActivity.this, binding.scvText, dataList);
                keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, binding.pinpadEditText);//Random keyboard
            }
        });
    }

    @Override
    public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
        TRACE.d("onRequestSetPin = " + isOfflinePin+"\ntryNum: "+tryNum);
        runOnUiThread(() -> {
            if(transactionType == QPOSService.TransactionType.UPDATE_PIN){
                changePinTimes ++;
                if(changePinTimes == 1){
                    viewModel.titleText.set(getString(R.string.input_pin_old));
                }else if(changePinTimes == 2 || changePinTimes == 4 ){
                    viewModel.titleText.set(getString(R.string.input_pin_new));
                }else if(changePinTimes == 3 ||changePinTimes == 5){
                    viewModel.titleText.set(getString(R.string.input_new_pin_confirm));
                }
            }else {
                if(isOfflinePin){
                    viewModel.titleText.set(getString(R.string.input_offlinePin));
                }else {
                    viewModel.titleText.set(getString(R.string.input_onlinePin));
                }
            }
            viewModel.stopLoading();
            viewModel.showPinpad.set(true);
        });
    }

    @Override
    public void onRequestSetPin() {
        TRACE.i("onRequestSetPin()");
        runOnUiThread(() -> {
            viewModel.titleText.set(getString(R.string.input_pin));
            pinPadDialog = new PinPadDialog(PaymentActivity.this);
            pinPadDialog.getPayViewPass().setRandomNumber(true).setPayClickListener(POS.getInstance().getQPOSService(), new PinPadView.OnPayClickListener() {

                @Override
                public void onCencel() {
                    POS.getInstance().cancelPin();
                    pinPadDialog.dismiss();
                }

                @Override
                public void onPaypass() {
                    POS.getInstance().bypassPin();
                    pinPadDialog.dismiss();
                }

                @Override
                public void onConfirm(String password) {
                    String pinBlock = QPOSUtil.buildCvmPinBlock(POS.getInstance().getEncryptData(), password);// build the ISO format4 pin block
                    POS.getInstance().sendCvmPin(pinBlock, true);
                    pinPadDialog.dismiss();
                }
            });
        });
    }

    @Override
    public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
        TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
        runOnUiThread(() -> {
            viewModel.showPinpad.set(false);
            String msg = "";
            if (!POS.getInstance().isPOSReady()) {
                viewModel.setTransactionFailed("Pls open device");
                return;
            }

            switch (result) {
                case ICC:
                    isICC = true;
                    viewModel.startLoading(getString(R.string.icc_card_inserted));
                    POS.getInstance().doEmvApp(QPOSService.EmvOption.START);
                    return;
                case MCR:
                    HandleTxnsResultUtils.handleMCRResult(decodeData,this,binding,viewModel);
                    return;
                case NFC_ONLINE:
                case NFC_OFFLINE:
                    HandleTxnsResultUtils.handleNFCResult(decodeData, this,binding,viewModel);
                    return;
                default:
                    msg = HandleTxnsResultUtils.getTradeResultMessage(result, this);
                    if (!msg.isEmpty()) {
                        if(result != QPOSService.DoTradeResult.PLS_SEE_PHONE) {
                            viewModel.setTransactionFailed(msg);
                        } else {
                            ToastUtils.showShort(msg);
                        }
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
               Hashtable<String, String> decodeData = POS.getInstance().anlysEmvIccData(tlv);
                String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                        + ", \"tlv\": " + tlv + "}";
                viewModel.sendDingTalkMessage(true,data);
            }
        });
    }

    @Override
    public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
        TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
        isChangePin = false;
        runOnUiThread(() -> {
            String msg = HandleTxnsResultUtils.getTransactionResultMessage(transactionResult, PaymentActivity.this);
            if (!msg.isEmpty()) {
                viewModel.setTransactionFailed(msg);
            }
        });
    }

    @Override
    public void onRequestBatchData(String tlv) {
        runOnUiThread(() -> {
            TRACE.d("onRequestBatchData = "+tlv);
            String content = getString(R.string.batch_data);
            content += tlv;
            PaymentModel paymentModel = viewModel.setTransactionSuccess(content);
            binding.tvReceipt.setMovementMethod(LinkMovementMethod.getInstance());
            Spanned receiptContent = ReceiptGenerator.generateICCReceipt(paymentModel);
            binding.tvReceipt.setText(receiptContent);
            if(DeviceUtils.isPrinterDevices()){
                handleSendReceipt();
            }
        });
    }

    @Override
    public void onRequestDisplay(QPOSService.Display displayMsg) {
        TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
        runOnUiThread(() -> {
            String msg = "";
            if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PaymentActivity.this);
                builder.setTitle("Audio");
                builder.setMessage("Success,Contine ready");
                builder.setPositiveButton("Confirm", null);
                builder.show();
            } else if (displayMsg == QPOSService.Display.INPUT_NEW_PIN) {
                isChangePin = true;
                timeOfPinInput++;
            } else if (displayMsg == QPOSService.Display.INPUT_NEW_PIN_CHECK_ERROR) {
                msg = getString(R.string.input_new_pin_check_error);
                timeOfPinInput = 0;
            }else {
                msg = HandleTxnsResultUtils.getDisplayMessage(displayMsg,PaymentActivity.this);
            }
            viewModel.startLoading(msg);
        });
    }

    @Override
    public void onReturnReversalData(String tlv) {
        runOnUiThread(() -> {
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
            viewModel.setTransactionFailed(content);
        });
    }

    @Override
    public void onReturnGetPinInputResult(int num) {
        TRACE.i("onReturnGetPinInputResult  ===" + num);
        runOnUiThread(() -> {
            StringBuilder s = new StringBuilder();
            if (num == -1) {
                binding.pinpadEditText.setText("");
                viewModel.showPinpad.set(false);
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
            } else {
                for (int i = 0; i < num; i++) {
                    s.append("*");
                }
                binding.pinpadEditText.setText(s.toString());
            }
        });
    }

    @Override
    public void onGetCardNoResult(String cardNo) {
        TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
    }

    @Override
    public void onGetCardInfoResult(Hashtable<String, String> cardInfo) {
    }

    @Override
    public void onEmvICCExceptionData(String tlv) {
        runOnUiThread(() -> {
            String msg = "Transaction is reversal :\n"+tlv;
            viewModel.setTransactionFailed(msg);
        });
    }

    @Override
    public void onTradeCancelled() {
        TRACE.d("onTradeCancelled");
        runOnUiThread(() -> {
//                viewModel.setTransactionFailed("Transaction is canceled!");
        });
    }

    @Override
    public void onError(QPOSService.Error error) {
        runOnUiThread(() -> {
            viewModel.setTransactionFailed(error.name());
            if(keyboardUtil != null){
                keyboardUtil.hide();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        POS.getInstance().cancelTrade();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogFileConfig.getInstance(this).readLog();
        QPOSCallbackManager.getInstance().unregisterPaymentCallback();
        PrinterHelper.getInstance().close();
    }

    private void convertReceiptToBitmap(final BitmapReadyListener listener) {
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

    private void handleSendReceipt() {
        convertReceiptToBitmap(bitmap -> {
            if (bitmap != null) {
                binding.btnSendReceipt.setVisibility(View.VISIBLE);
            } else {
                binding.btnSendReceipt.setVisibility(View.GONE);
            }
        });
    }
}