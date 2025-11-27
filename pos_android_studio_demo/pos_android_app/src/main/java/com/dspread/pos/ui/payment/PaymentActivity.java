package com.dspread.pos.ui.payment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dspread.pos.posAPI.ConnectionServiceCallback;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.posAPI.PaymentServiceCallback;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.payment.pinkeyboard.KeyboardUtil;
import com.dspread.pos.ui.payment.pinkeyboard.MyKeyboardView;
import com.dspread.pos.ui.payment.pinkeyboard.PinPadDialog;
import com.dspread.pos.ui.payment.pinkeyboard.PinPadView;
import com.dspread.pos.utils.AdvancedBinDetector;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.HandleTxnsResultUtils;
import com.dspread.pos.utils.LogFileConfig;
import com.dspread.pos.utils.QPOSUtil;
import com.dspread.pos.utils.ReceiptGenerator;
import com.dspread.pos.utils.TLV;
import com.dspread.pos.utils.TLVParser;
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
import java.util.concurrent.atomic.AtomicBoolean;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class PaymentActivity extends BaseActivity<ActivityPaymentBinding, PaymentViewModel> implements PaymentServiceCallback {

    private String amount;
    private String deviceAddress;
    private KeyboardUtil keyboardUtil;
    public PinPadDialog pinPadDialog;
    private boolean isPinBack = false;//去掉
    private PaymentServiceCallback paymentServiceCallback;
    private String terminalTime;
    private String maskedPAN;
    private ConnectionServiceCallback connectionCallback;
    private Handler handler;
    private Runnable runnable;
    private int currentIndex = 0;
    private AtomicBoolean isStarting = new AtomicBoolean(false);
    private final int[] imageResources = {R.mipmap.ic_insert_new_d70, R.mipmap.ic_tap_new_d70, R.mipmap.ic_swipe_new_d70};
    private final String[] textResources = {"<span style='color:red'>Insert</span><span style='color:black'>, tap or swipe</span>", "<span style='color:black'>Insert, </span><span style='color:red'>tap</span> <span style='color:black'>or swipe</span>", "<span style='color:black'>Insert, tap or </span><span style='color:red'>swipe</span>"};

    @Override
    public int initContentView(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        return R.layout.activity_payment;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    /**
     * Initialize payment activity data
     * Sets up initial UI state and starts transaction
     */
    @Override
    public void initData() {
        binding.setVariable(BR.viewModel, viewModel);
        binding.pinpadEditText.setText("");
        viewModel.titleText.set("Paymenting");

        paymentServiceCallback = new PaymentCallback();
        amount = getIntent().getStringExtra("amount");
        deviceAddress = getIntent().getStringExtra("deviceAddress");
        viewModel.displayAmount(DeviceUtils.convertAmountToCents(amount));//ui

        initConnectionCallback();
        startTransaction();
        showCardImage();
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.isOnlineSuccess.observe(this, aBoolean -> {
            if (aBoolean) {
                viewModel.setTransactionSuccess();
                paymentStatus(amount, maskedPAN, terminalTime, "");
            } else {
                viewModel.setTransactionFailed("Transaction failed because of the network!");
                paymentStatus("", "", "", "Transaction failed because of the network!");
            }
        });
    }

    private void showCardImage() {
        if ("D70".equals(DeviceUtils.getPhoneModel())) {
            ViewGroup.LayoutParams params = binding.ivCloseBlackD70.getLayoutParams();
            params.width = 26;
            params.height = 26;
            binding.ivCloseBlackD70.setLayoutParams(params);
            binding.ivCloseBlackD70.setImageResource(R.mipmap.btn_close_black);
            binding.ivCloseBlackD70.setVisibility(View.VISIBLE);
            binding.ivCloseBlack.setVisibility(View.GONE);
            setupImageSwitcher();
        } else {
            setupAnimationBasedOnDeviceModel();
            binding.ivCloseBlackD70.setVisibility(View.GONE);
            binding.ivCloseBlack.setVisibility(View.VISIBLE);
        }
    }

    private void setupImageSwitcher() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                currentIndex = (currentIndex + 1) % imageResources.length;
                updateContent();
                handler.postDelayed(this, 2000);
            }
        };

        updateContent();
        handler.postDelayed(runnable, 2000);
    }

    private void updateContent() {
        binding.d70ImageView.setImageResource(imageResources[currentIndex]);
        binding.txtWaitInsertTapCard.setText(Html.fromHtml(textResources[currentIndex], Html.FROM_HTML_MODE_COMPACT));
    }

    /**
     * Dynamically set Lottie animations according to the device model
     */
    private void setupAnimationBasedOnDeviceModel() {
        String deviceModel = DeviceUtils.getPhoneModel();
        TRACE.d("model:" + deviceModel);
        if ("D20".equals(deviceModel)) {
            binding.animationView.setAnimation("D20_checkCardImg.json");
            binding.animationView.setImageAssetsFolder("D20_images/");
        } else if ("D80".equals(deviceModel)) {
            binding.animationView.setAnimation("D80_checkCard.json");
            binding.animationView.setImageAssetsFolder("D80_images/");
        } else if ("D60".equals(deviceModel)) {
            binding.animationView.setAnimation("D60_checkCard.json");
            binding.animationView.setImageAssetsFolder("D60_images/");
        } else {//D30
            binding.animationView.setAnimation("D30_checkCard.json");
            binding.animationView.setImageAssetsFolder("D30_images/");
        }
        binding.animationView.loop(true);
        binding.animationView.playAnimation();
    }

    private void initConnectionCallback() {
        connectionCallback = new ConnectionServiceCallback() {
            @Override
            public void onRequestNoQposDetected() {
                ToastUtils.showLong("Device connected fail");
            }

            @Override
            public void onRequestQposConnected() {
                ToastUtils.showLong("Device connected");
            }

            @Override
            public void onRequestQposDisconnected() {
                ToastUtils.showLong("Device disconnected");
                finish();
            }
        };
    }

    /**
     * Start payment transaction in background thread
     * Handles device connection and transaction initialization
     */
    private void startTransaction() {
        new Thread(() -> {
            if (!POSManager.getInstance().isDeviceReady()) {
                POSManager.getInstance().connect(deviceAddress, connectionCallback);
            } else {
                // if device has connected, just register connection callback
                POSManager.getInstance().registerConnectionCallback(connectionCallback);
            }

            POSManager.getInstance().startTransaction(amount, paymentServiceCallback);
        }).start();
    }

    /**
     * Inner class to handle payment callbacks
     * Implements all payment related events and UI updates
     */
    private class PaymentCallback implements PaymentServiceCallback {

        @Override
        public void onRequestTime() {
            terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            TRACE.d("onRequestTime: " + terminalTime);
            POSManager.getInstance().sendTime(terminalTime);
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            Dialog dialog = new Dialog(PaymentActivity.this);
            dialog.setContentView(R.layout.emv_app_dialog);
            dialog.setTitle(R.string.please_select_app);
            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {
                appNameList[i] = appList.get(i);
            }
            ListView appListView = dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<>(PaymentActivity.this, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener((parent, view, position, id) -> {
                POSManager.getInstance().selectEmvApp(position);
                TRACE.d("select emv app position = " + position);
                dialog.dismiss();
            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(v -> {
                POSManager.getInstance().cancelSelectEmvApp();
                dialog.dismiss();
            });
            dialog.show();
        }

        /**
         * Handle PIN input request
         * Sets up PIN pad and keyboard for user input
         *
         * @param dataList    List of PIN data
         * @param offlineTime Offline PIN try count
         */
        @Override
        public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
            TRACE.d("onQposRequestPinResult = " + dataList + "\nofflineTime: " + offlineTime);
            if (POSManager.getInstance().isDeviceReady()) {
                viewModel.stopLoading();
                viewModel.clearErrorState();
                viewModel.showPinpad.set(true);
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
            }
            binding.pinpadEditText.setText("");
            MyKeyboardView.setKeyBoardListener(value -> {
                if (POSManager.getInstance().isDeviceReady()) {
                    POSManager.getInstance().pinMapSync(value, 20);
                }
            });
            if (POSManager.getInstance().isDeviceReady()) {
                keyboardUtil = new KeyboardUtil(PaymentActivity.this, binding.scvText, dataList);
                keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, binding.pinpadEditText);//Random keyboard
            }
        }

        @Override
        public void onRequestSetPin() {
            TRACE.i("onRequestSetPin()");
            isPinBack = true;
            // Clear previous error state when entering PIN input
            viewModel.clearErrorState();
            //CR100 devices
            viewModel.titleText.set(getString(R.string.input_pin));
            pinPadDialog = new PinPadDialog(PaymentActivity.this);
            pinPadDialog.getPayViewPass().setRandomNumber(true).setPayClickListener(POSManager.getInstance().getQPOSService(), new PinPadView.OnPayClickListener() {

                @Override
                public void onCencel() {
                    POSManager.getInstance().cancelPin();
                    pinPadDialog.dismiss();
                }

                @Override
                public void onPaypass() {
                    POSManager.getInstance().bypassPin();
                    pinPadDialog.dismiss();
                }

                @Override
                public void onConfirm(String password) {
                    String pinBlock = QPOSUtil.buildISO4PinBlock(POSManager.getInstance().computeISOPinBlockStringHashtable(), password);// build the ISO format4 pin block
                    POSManager.getInstance().sendCvmPin(pinBlock, true);
                    pinPadDialog.dismiss();
                }
            });

        }

        @Override
        public void onRequestDisplay(QPOSService.Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
            String msg = "";
            if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PaymentActivity.this);
                builder.setTitle("Audio");
                builder.setMessage("Success,Contine ready");
                builder.setPositiveButton("Confirm", null);
                builder.show();
            } else if(displayMsg == QPOSService.Display.INPUT_ONLINE_PIN || displayMsg == QPOSService.Display.INPUT_OFFLINE_PIN){
                TRACE.d("onRequestDisplay(Display displayMsg):pin input" + displayMsg.toString());
                viewModel.stopLoading();
                viewModel.clearErrorState();
                viewModel.showPinpad.set(true);
                binding.animationView.pauseAnimation();
            }else {
                msg = HandleTxnsResultUtils.getDisplayMessage(displayMsg, PaymentActivity.this);
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                binding.animationView.pauseAnimation();
                viewModel.startLoading(msg);
            }
        }

        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.i("onDoTradeResult = "+result);
            PaymentResult paymentResult = new PaymentResult();
            if (result == QPOSService.DoTradeResult.ICC) {
                viewModel.cardInsertedState();
                POSManager.getInstance().doEmvAPP();
            } else if (result == QPOSService.DoTradeResult.NFC_ONLINE || result == QPOSService.DoTradeResult.NFC_OFFLINE) {
                Hashtable<String, String> batchData = POSManager.getInstance().getNFCBatchData();
                String tlv = batchData.get("tlv");
                TRACE.i("NFC Batch data: " + tlv);

                paymentResult.setAmount(amount);
                HandleTxnsResultUtils.handleDoTradeResult(paymentResult, decodeData, viewModel);
                binding.animationView.pauseAnimation();
                maskedPAN = paymentResult.getMaskedPAN();
            } else if (result == QPOSService.DoTradeResult.MCR) {
                paymentResult.setAmount(amount);
                HandleTxnsResultUtils.handleDoTradeResult(paymentResult, decodeData, viewModel);
                binding.animationView.pauseAnimation();
                maskedPAN = paymentResult.getMaskedPAN();
            } else if (result == QPOSService.DoTradeResult.PLS_SEE_PHONE) {
                viewModel.showPinpad.set(false);
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
                viewModel.titleText.set(getString(R.string.pls_see_phone));
            }
        }

        @Override
        public void onTransactionResult(PaymentResult result) {
            if (result.getStatus() != null && result.getStatus().equals(QPOSService.TransactionResult.APPROVED.name())) {
                binding.animationView.pauseAnimation();
                result.setAmount(amount);
                if (result.getTlv() != null) {
                    String content = getString(R.string.batch_data);
                    content += result.getTlv();
                    PaymentModel paymentModel = viewModel.setTransactionSuccess(content);
                    binding.tvReceipt.setMovementMethod(LinkMovementMethod.getInstance());
                    Spanned receiptContent = ReceiptGenerator.generateICCReceipt(paymentModel);
                    binding.tvReceipt.setText(receiptContent);
                    List<TLV> list = TLVParser.parse(result.getTlv());
                    TLV tlvMaskedPan = TLVParser.searchTLV(list, "C4");
                    paymentStatus(amount, tlvMaskedPan == null ? paymentModel.getCardNo() : tlvMaskedPan.value, terminalTime, "");
                }
            } else {
                viewModel.showPinpad.set(false);
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
                if (result.getStatus() != null && !result.getStatus().isEmpty()) {
                    paymentStatus("", "", "", result.getStatus());
                    viewModel.setTransactionFailed(result.getStatus());
                    viewModel.setTransactionErr(result.getStatus());
                }
                finish();
            }
        }

        /**
         * Handle online process request
         * Sends transaction data to server for online authorization
         *
         * @param tlv TLV format transaction data
         */
        @Override
        public void onRequestOnlineProcess(final String tlv) {
            TRACE.d("onRequestOnlineProcess" + tlv);
            viewModel.showPinpad.set(false);
            viewModel.startLoading(getString(R.string.online_process_requested));
            Hashtable<String, String> decodeData = POSManager.getInstance().anlysEmvIccData(tlv);
            PaymentModel paymentModel = new PaymentModel();
            paymentModel.setAmount(amount);
            String cardNo = "";
            String cardOrg = "";
            if ("32".equals(decodeData.get("formatID"))) {
                cardNo = decodeData.get("maskedPAN");
            } else {
                List<TLV> tlvList = TLVParser.parse(tlv);
                TLV cardNoTlv = TLVParser.searchTLV(tlvList, "C4");
                cardNo = cardNoTlv == null ? "" : cardNoTlv.value;
                cardNo = cardNo.substring(0, cardNo.length() - 1);
            }
            cardOrg = AdvancedBinDetector.detectCardType(cardNo).getDisplayName();
            paymentModel.setCardNo(cardNo);
            paymentModel.setCardOrg(cardOrg);
            viewModel.requestOnlineAuth(true, paymentModel);
        }

        @Override
        public void onReturnGetPinInputResult(int num, QPOSService.PinError error, int minLen, int maxLen) {
            TRACE.i("onReturnGetPinInputResult  ===" + num);
            StringBuilder s = new StringBuilder();
            if (num == -1) {
                isPinBack = false;
                binding.pinpadEditText.setText("");
                viewModel.pincomPletedState();
                binding.d70ImageView.setVisibility(View.GONE);
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
            } else {
                for (int i = 0; i < num; i++) {
                    s.append("*");
                }
                binding.pinpadEditText.setText(s.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPinBack) {
            new Thread(() -> {
                POSManager.getInstance().cancelTransaction();
                runOnUiThread(() -> finish());
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogFileConfig.getInstance(this).readLog();
        PrinterHelper.getInstance().close();
        POSManager.getInstance().unregisterCallbacks();
    }

    private void paymentStatus(String amount, String maskedPAN, String terminalTime, String errorMsg) {
        binding.d70ImageView.setVisibility(View.GONE);
        if (isStarting.compareAndSet(false, true)) {
            try {
                Intent intent = new Intent(PaymentActivity.this, PaymentStatusActivity.class);
                if (amount != null && !"".equals(amount)) {
                    intent.putExtra("amount", amount);
                    intent.putExtra("maskedPAN", maskedPAN);
                    intent.putExtra("terminalTime", terminalTime);
                } else if (errorMsg != null && !"".equals(errorMsg)) {
                    intent.putExtra("errorMsg", errorMsg);
                }
                startActivity(intent);
                finish();
            } finally {
                new Handler().postDelayed(() -> isStarting.set(false), 500);
            }
        }
    }

}