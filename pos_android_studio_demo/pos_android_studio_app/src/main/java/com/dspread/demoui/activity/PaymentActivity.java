package com.dspread.demoui.activity;

import static com.dspread.demoui.utils.QPOSUtil.HexStringToByteArray;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.beans.GlobalErrorEvent;
import com.dspread.demoui.enums.POS_TYPE;
import com.dspread.demoui.interfaces.TransactionCallback;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.xpos.QPOSService;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.DUKPK2009_CBC;
import com.dspread.demoui.utils.DeviceUtils;
import com.dspread.demoui.utils.DingTalkTest;
import com.dspread.demoui.utils.SystemKeyListener;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.widget.pinpad.PinPadDialog;
import com.dspread.demoui.widget.pinpad.PinPadView;
import com.dspread.xpos.Util;
import com.dspread.xpos.utils.AESUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    private String transactionTypeString = "";
    private static Dialog dialog;
    private String nfcLog = "";
    private String nfcData;
    private String cashbackAmounts = "";
    private String amounts = "";
    private String amount = "";
    private ListView appListView;
    private QPOSService.TransactionType transactionType = QPOSService.TransactionType.GOODS;
    private boolean isPinCanceled = false;
    private TextView mtvinfo;
    private RelativeLayout mllinfo;
    private Button mbtnNewpay;
    private LinearLayout mllchrccard;
    private ImageView ivBackTitle, ivBlue;
    private TextView tvTitle;
    private TextView tvAmount;
    private TextView tradeSuccess;
    private EditText statusEditText, pinpadEditText;
    private ScrollView scvText;
    public static PinPadDialog pinPadDialog;
    private boolean dealDoneflag = false;
    private SystemKeyListener systemKeyListener;
    private boolean isNormal = false;
    private boolean isICC;
    private QPOSService pos;
    private BaseApplication baseApplication;
    private SharedPreferencesUtil preferencesUtil;
    private String connType;
    private TransactionCallbackCls transactionCallbackCls;

    private boolean isChangePin = false;

    private int timeOfPinInput = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getApplicationInstance = this;
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_payment);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        transactionCallbackCls = new TransactionCallbackCls();
        com.dspread.demoui.activity.MyQposClass.setTransactionCallback(transactionCallbackCls);
        preferencesUtil = SharedPreferencesUtil.getInstance(this);
        connType = (String) preferencesUtil.get(Constants.connType, "");
        baseApplication = (BaseApplication) getApplication();
        pos = baseApplication.getQposService();
        transactionTypeString = getIntent().getStringExtra("paytype");
        amounts = getIntent().getStringExtra("inputMoney");
        cashbackAmounts = getIntent().getStringExtra("cashbackAmounts");
        systemKeyListener = new SystemKeyListener(this);
        systemKeyStart();
        systemKeyListener.startSystemKeyListener();
        isNormal = false;
        initView();
        if (pos != null && !"".equals(connType)) {
            if (connType.equals(POS_TYPE.UART.name())) {
                pos.setRandomKeyboardFlag(true);// for new smartpos
                pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);

            }
            pos.setDelayCheckingCardTime(500);
            pos.doTrade(20);
        } else {
            goToMainPage();
        }
        TRACE.setContext(this);
        EventBus.getDefault().register(this);
    }

    private void goToMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("showSettingFragment", true);
        startActivity(intent);
        finish();
    }

    private void initView() {
        ivBackTitle = findViewById(R.id.iv_back_title);
        ivBlue = findViewById(R.id.iv_blue);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.device_connect));
        tvAmount = findViewById(R.id.tv_amount);
        tvAmount.setText(amount);
        tradeSuccess = findViewById(R.id.trade_success_flag);
        scvText = findViewById(R.id.scv_text);
        mbtnNewpay = findViewById(R.id.btn_newpay);
        mtvinfo = findViewById(R.id.tv_info);
        mllinfo = findViewById(R.id.ll_info);
        mllchrccard = findViewById(R.id.ll_chrccard);
        statusEditText = findViewById(R.id.statusEditText);
        pinpadEditText = findViewById(R.id.pinpadEditText);
        ivBackTitle.setOnClickListener(this);
        ivBlue.setOnClickListener(this);
        mbtnNewpay.setOnClickListener(this);
        tvTitle.setOnClickListener(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGlobalErrorEvent(GlobalErrorEvent event) {
        // 处理事件
        TRACE.i("payment error == " + event.errorState);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QPOSService.Error errorState = event.errorState;
                dismissDialog();
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
                String msg = "";
                if (errorState == QPOSService.Error.CMD_NOT_AVAILABLE) {
                    msg = getString(R.string.command_not_available);
                } else if (errorState == QPOSService.Error.TIMEOUT) {
                    msg = getString(R.string.device_no_response);
                } else if (errorState == QPOSService.Error.DEVICE_RESET) {
                    msg = getString(R.string.device_reset);
                } else if (errorState == QPOSService.Error.UNKNOWN) {
                    msg = getString(R.string.unknown_error);
                } else if (errorState == QPOSService.Error.DEVICE_BUSY) {
                    msg = getString(R.string.device_busy);
                    if (pos != null) {
                        pos.resetPosStatus();
                    }
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
                } else {
                    msg = errorState.name();
                }
                String finalMsg = msg;

                Mydialog.ErrorDialog(PaymentActivity.this, finalMsg, new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm() {
                        finish();
                        Mydialog.ErrorDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                dismissDialog();
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
                if (!isNormal) {
                    if (pos != null) {
                        pos.cancelTrade();
                    }
                }
                finish();
                break;
            case R.id.btn_newpay:
                dismissDialog();
                finish();
                break;
            default:
                break;
        }
    }

    private void sendMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 8003:
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String content = "";
                    if (nfcLog == null && pos != null) {
                        Hashtable<String, String> h = pos.getNFCBatchData();
                        String tlv = h.get("tlv");
//                        TRACE.i("nfc batchdata1: " + tlv);
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + h.get("tlv");
                    } else {
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + nfcLog;
                    }
                    sendRequestToBackend(nfcData + content);
                    break;
                default:
                    break;
            }
        }
    };

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
        if (pinPadDialog != null) {
            pinPadDialog.dismiss();
        }
    }

    private final Handler dingdingHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    if (isICC) {
                        dismissDialog();
                        TRACE.i("onError==");

                        Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.network_failed), new Mydialog.OnMyClickListener() {
                            @Override
                            public void onCancel() {

                            }

                            @Override
                            public void onConfirm() {
                                //8A025A33 //Unable to go online, offline declined
                                String offlineDeclinedCode = "8A025A33";
                                pos.sendOnlineProcessResult(offlineDeclinedCode);
                            }
                        });
                    } else {
                        Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.network_failed), null);
                    }
                    break;
                case 100:
                    if (isICC) {
                        pos.sendOnlineProcessResult("8A023030");
                    } else {
                        isNormal = true;
                        pinpadEditText.setVisibility(View.GONE);
                        tvTitle.setText(getText(R.string.transaction_result));
                        mllinfo.setVisibility(View.VISIBLE);
                        mtvinfo.setText((String) msg.obj);
                        mllchrccard.setVisibility(View.GONE);
                        dismissDialog();
                    }

                default:
                    break;
            }
        }
    };

    private void putInfoToDingding(String tlv, String info) {
        new Thread(() -> {
            try {
                boolean isAtAll = false;
                String content = "issues: " + info;
                String reqStr = DingTalkTest.buildReqStr(content, isAtAll);
                String result = DingTalkTest.postJson(Constants.dingdingUrl, reqStr);
                Message msg = new Message();
                if (result != null) {
                    System.out.println("result == " + result);
                    JSONObject object = new JSONObject(result);
                    String errmsg = object.getString("errmsg");
                    int errcode = object.getInt("errcode");
                    if (errcode == 0) {
                        msg.what = 100;
                        msg.obj = tlv;
                        dingdingHandler.sendMessage(msg);
                    } else {
                        msg.what = 101;
                        dingdingHandler.sendMessage(msg);
                        Log.e("Exception", "Network fail");
                    }
                } else {
                    msg.what = 101;
                    dingdingHandler.sendMessage(msg);
                    Log.e("Exception", "Network fail");
                }

            } catch (Exception e) {
                Log.e("Exception", "e:" + e.toString());
                e.printStackTrace();

            }
        }).start();
    }

    private void sendRequestToBackend(String tlvData) {
//
//        if("D300".equals(Build.MODEL) ||"D70".equals(Build.MODEL)) {
//            isNormal = true;
//            pinpadEditText.setVisibility(View.GONE);
//            tvTitle.setText(getText(R.string.transaction_result));
//            mllinfo.setVisibility(View.VISIBLE);
//            mtvinfo.setText(tlvData);
//            mllchrccard.setVisibility(View.GONE);
//            dismissDialog();
//            return;
//        }
        pinpadEditText.setVisibility(View.GONE);
        tvTitle.setText(getText(R.string.transaction_result));
        String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                + ", \"tlv\": " + tlvData + "}";

        Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
        putInfoToDingding(tlvData, data);
    }

    private static KeyboardUtil keyboardUtil;

    class TransactionCallbackCls implements TransactionCallback {

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
                pos.setAmount(amounts, cashbackAmounts, "643", transactionType);
            }
        }

        @Override
        public void onRequestWaitingUser() {

        }

        @Override
        public void onRequestTime() {
            TRACE.d("onRequestTime");
            dismissDialog();
            String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            pos.sendTime(terminalTime);
//            statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            runOnUiThread(() -> {
                dismissDialog();
                dialog = new Dialog(PaymentActivity.this);
                dialog.setContentView(R.layout.emv_app_dialog);
                dialog.setTitle(R.string.please_select_app);
                String[] appNameList = new String[appList.size()];
                for (int i = 0; i < appNameList.length; ++i) {

                    appNameList[i] = appList.get(i);
                }
                appListView = (ListView) dialog.findViewById(R.id.appList);
                appListView.setAdapter(new ArrayAdapter<String>(PaymentActivity.this, android.R.layout.simple_list_item_1, appNameList));
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
            });
        }

        @Override
        public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pos != null) {
                        boolean onlinePin = pos.isOnlinePin();
                        pinpadEditText.setText("");
                        if (keyboardUtil != null) {
                            keyboardUtil.hide();
                        }
                        if (isChangePin) {
                            if (timeOfPinInput == 1) {
                                tvTitle.setText(getString(R.string.input_new_pin_first_time));
                            } else if (timeOfPinInput == 2) {
                                tvTitle.setText(getString(R.string.input_new_pin_confirm));
                                timeOfPinInput = 0;
                            }

                        } else {
                            if (onlinePin) {
                                tvTitle.setText(getString(R.string.input_onlinePin));
                            } else {
                                int cvmPinTryLimit = pos.getCvmPinTryLimit();
                                TRACE.d("PinTryLimit:" + cvmPinTryLimit);
                                if (cvmPinTryLimit == 1) {
                                    tvTitle.setText(getString(R.string.input_offlinePin_last));
                                } else {
                                    tvTitle.setText(getString(R.string.input_offlinePin));
                                }
                            }
                        }
                    }
                    dismissDialog();
                    mllchrccard.setVisibility(View.GONE);
                    MyKeyboardView.setKeyBoardListener(value -> {
                        if (pos != null) {
                            pos.pinMapSync(value, 20);
                        }
                    });
                    pinpadEditText.setVisibility(View.VISIBLE);
                    if (pos != null) {
                        keyboardUtil = new KeyboardUtil(PaymentActivity.this, scvText, dataList);
                        keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, pinpadEditText);//Random keyboard
                    }
                }
            });
        }

        @Override
        public void onQposPinMapSyncResult(boolean isSuccess, boolean isNeedPin) {

        }

        @Override
        public void onRequestSetPin(boolean isOfflinePin, int tryNum) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    tvTitle.setText(getString(R.string.input_pin));
                    dismissDialog();
                    pinpadEditText.setVisibility(View.VISIBLE);
                    mllchrccard.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onRequestSetPin() {
            TRACE.i("onRequestSetPin()");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTitle.setText(getString(R.string.input_pin));
                    dismissDialog();
                    mllchrccard.setVisibility(View.GONE);
                    pinPadDialog = new PinPadDialog(PaymentActivity.this);
                    pinPadDialog.getPayViewPass().setRandomNumber(true).setPayClickListener(pos, new PinPadView.OnPayClickListener() {

                        @Override
                        public void onCencel() {
                            pos.cancelPin();
                            pinPadDialog.dismiss();
                        }

                        @Override
                        public void onPaypass() {
                            pos.bypassPin();
                            //                    pos.sendPin("".getBytes());
                            pinPadDialog.dismiss();
                        }

                        @Override
                        public void onConfirm(String password) {
                            String pinBlock = buildCvmPinBlock(pos.getEncryptData(), password);// build the ISO format4 pin block
                            pos.sendCvmPin(pinBlock, true);
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
            statusEditText.setText(content);
            TRACE.i(content);
        }

        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    dismissDialog();
                    String cardNo = "";
                    String msg = "";
                    isICC = false;
                    if (pos == null) {
                        msg = "Pls open device";
                        Mydialog.ErrorDialog(PaymentActivity.this, msg, null);
                        return;
                    }
                    if (result == QPOSService.DoTradeResult.NONE) {
//                statusEditText.setText(getString(R.string.no_card_detected));
                        msg = getString(R.string.no_card_detected);
                    } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
                        statusEditText.setText(getString(R.string.try_another_interface));
                        msg = getString(R.string.try_another_interface);
                    } else if (result == QPOSService.DoTradeResult.ICC) {
                        isICC = true;
                        statusEditText.setText(getString(R.string.icc_card_inserted));
                        pos.doEmvApp(QPOSService.EmvOption.START);
                    } else if (result == QPOSService.DoTradeResult.NOT_ICC) {
//                statusEditText.setText(getString(R.string.card_inserted));
                        msg = getString(R.string.card_inserted);
                    } else if (result == QPOSService.DoTradeResult.BAD_SWIPE) {
                        statusEditText.setText(getString(R.string.bad_swipe));
                        msg = getString(R.string.bad_swipe);
                    } else if (result == QPOSService.DoTradeResult.CARD_NOT_SUPPORT) {
                        statusEditText.setText("GPO NOT SUPPORT");
                        msg = "GPO NOT SUPPORT";
                    } else if (result == QPOSService.DoTradeResult.PLS_SEE_PHONE) {
                        statusEditText.setText("PLS SEE PHONE");
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

                        }
                        if (decodeData.get("maskedPAN") != null && !"".equals(decodeData.get("maskedPAN"))) {
                            sendRequestToBackend(content);
                        } else {
                            Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.trade_returnfailed), new Mydialog.OnMyClickListener() {
                                @Override
                                public void onCancel() {

                                }

                                @Override
                                public void onConfirm() {
                                    if (pos != null) {
                                        pos.cancelTrade();
                                    }
                                    finish();
                                }
                            });
                        }

                    } else if ((result == QPOSService.DoTradeResult.NFC_ONLINE) || (result == QPOSService.DoTradeResult.NFC_OFFLINE)) {
                        nfcLog = decodeData.get("nfcLog");
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
                        nfcData = content;
                        sendMsg(8003);
                    } else if ((result == QPOSService.DoTradeResult.NFC_DECLINED)) {
                        statusEditText.setText(getString(R.string.transaction_declined));
                        msg = getString(R.string.transaction_declined);
                    } else if (result == QPOSService.DoTradeResult.NO_RESPONSE) {
                        statusEditText.setText(getString(R.string.card_no_response));
                        getString(R.string.card_no_response);
                    } else {
                        statusEditText.setText(getString(R.string.unknown_error));
                        msg = getString(R.string.unknown_error);
                    }
                    if (msg != null && !"".equals(msg)) {
                        Mydialog.ErrorDialog(PaymentActivity.this, msg, null);
                    }
                    dealDoneflag = true;
                }
            });
        }

        @Override
        public void onRequestOnlineProcess(final String tlv) {
            TRACE.d("onRequestOnlineProcess" + tlv);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    tvTitle.setText(getString(R.string.online_process_requested));
                    dismissDialog();

                    Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
//            TRACE.d("anlysEmvIccData(tlv):" + decodeData.toString());
                    if (isPinCanceled) {
                        mllchrccard.setVisibility(View.GONE);
                    } else {
                        mllchrccard.setVisibility(View.GONE);
                    }
                    String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                            + ", \"tlv\": " + tlv + "}";
                    Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
                    putInfoToDingding(tlv, data);
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
                    dealDoneflag = true;
                    dismissDialog();
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
                        Mydialog.ErrorDialog(PaymentActivity.this, msg, new Mydialog.OnMyClickListener() {
                            @Override
                            public void onCancel() {
                            }

                            @Override
                            public void onConfirm() {
//                        pos.cancelTrade();
                                finish();
                                Mydialog.ErrorDialog.dismiss();
                            }
                        });
                    }
                    amounts = "";
                    cashbackAmounts = "";
                }
            });
        }

        @Override
        public void onRequestBatchData(String tlv) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    dealDoneflag = true;
                    isNormal = true;
                    pinpadEditText.setVisibility(View.GONE);
                    tvTitle.setText(getText(R.string.transaction_result));
                    TRACE.d("ICC trade finished");
                    String content = getString(R.string.batch_data);
                    content += tlv;
                    mllinfo.setVisibility(View.VISIBLE);
                    mtvinfo.setText(content);
                    mllchrccard.setVisibility(View.GONE);

                }
            });
        }

        @Override
        public void onQposIsCardExist(boolean cardIsExist) {
            TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (cardIsExist) {
                        statusEditText.setText("cardIsExist:" + cardIsExist);
                    } else {
                        statusEditText.setText("cardIsExist:" + cardIsExist);
                    }
                }
            });

        }

        @Override
        public void onRequestDisplay(QPOSService.Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    dismissDialog();
                    String msg = "";
                    if (displayMsg == QPOSService.Display.CLEAR_DISPLAY_MSG) {
                        msg = "";
                    } else if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
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
                    Mydialog.loading(PaymentActivity.this, msg);
                }
            });
        }

        @Override
        public void onReturnReversalData(String tlv) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dealDoneflag = true;
                    String content = getString(R.string.reversal_data);
                    content += tlv;
                    TRACE.d("onReturnReversalData(): " + tlv);
                    statusEditText.setText(content);

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
                        if (keyboardUtil != null) {
                            keyboardUtil.hide();
                            pinpadEditText.setVisibility(View.GONE);
                        }
                    } else {
                        for (int i = 0; i < num; i++) {
                            s += "*";
                        }
                        pinpadEditText.setText(s);
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
                    statusEditText.setText("cardNo: " + cardNo);
                }
            });

        }

        @Override
        public void onGetCardInfoResult(Hashtable<String, String> cardInfo) {
        }

        @Override
        public void onEmvICCExceptionData(String tlv) {
        }

        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                }
            });
        }
    }

    private String buildCvmPinBlock(Hashtable<String, String> value, String pin) {
        String randomData = value.get("RandomData") == null ? "" : value.get("RandomData");
        String pan = value.get("PAN") == null ? "" : value.get("PAN");
        String AESKey = value.get("AESKey") == null ? "" : value.get("AESKey");
        String isOnline = value.get("isOnlinePin") == null ? "" : value.get("isOnlinePin");
        String pinTryLimit = value.get("pinTryLimit") == null ? "" : value.get("pinTryLimit");
        //iso-format4 pinblock
        int pinLen = pin.length();
        pin = "4" + Integer.toHexString(pinLen) + pin;
        for (int i = 0; i < 14 - pinLen; i++) {
            pin = pin + "A";
        }
        pin += randomData.substring(0, 16);
        String panBlock = "";
        int panLen = pan.length();
        int m = 0;
        if (panLen < 12) {
            panBlock = "0";
            for (int i = 0; i < 12 - panLen; i++) {
                panBlock += "0";
            }
            panBlock = panBlock + pan + "0000000000000000000";
        } else {
            m = pan.length() - 12;
            panBlock = m + pan;
            for (int i = 0; i < 31 - panLen; i++) {
                panBlock += "0";
            }
        }
        String pinBlock1 = AESUtil.encrypt(AESKey, pin);
        pin = Util.xor16(HexStringToByteArray(pinBlock1), HexStringToByteArray(panBlock));
        String pinBlock2 = AESUtil.encrypt(AESKey, pin);
        return pinBlock2;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        dismissDialog();
        if (pos != null) {
            pos = null;
        }

        if (!dealDoneflag) {
            if (pos != null) {
                pos.cancelTrade();
            }
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.w("onKeyDown", "keyCode==" + keyCode);

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismissDialog();
            if (keyboardUtil != null) {
                Log.w("keyboardUtil", "keyboardUtil.hide");
                keyboardUtil.hide();
            }

            if (!isNormal) {

                if (pos != null) {
                    pos.cancelTrade();
                }

            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void systemKeyStart() {
        systemKeyListener.setOnSystemKeyListener(new SystemKeyListener.OnSystemKeyListener() {
            @Override
            public void onHomePressed() {
                dismissDialog();
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
                if (!isNormal) {
                    if (pos != null) {
                        pos.cancelTrade();
                    }
                }
                finish();
            }

            @Override
            public void onMenuPressed() {
                dismissDialog();
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
                if (!isNormal) {
                    if (pos != null) {
                        pos.cancelTrade();
                    }
                }
                finish();
            }

            @Override
            public void onScreenOff() {
            }


            @Override
            public void onScreenOn() {
            }
        });
    }

}