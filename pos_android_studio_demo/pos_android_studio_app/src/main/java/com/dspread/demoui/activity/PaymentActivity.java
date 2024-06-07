package com.dspread.demoui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.DUKPK2009_CBC;
import com.dspread.demoui.utils.FileUtils;
import com.dspread.demoui.utils.QPOSUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.USBClass;
import com.dspread.demoui.widget.pinpad.PinPadDialog;
import com.dspread.demoui.widget.pinpad.PinPadView;
import com.dspread.demoui.widget.pinpad.keyboard.KeyBoardNumInterface;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.Util;
import com.dspread.xpos.utils.AESUtil;
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
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.droidsonroids.gif.GifImageView;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.dspread.demoui.ui.dialog.Mydialog.UART;
import static com.dspread.demoui.ui.dialog.Mydialog.USB_OTG_CDC_ACM;
import static com.dspread.demoui.utils.QPOSUtil.HexStringToByteArray;
import static com.dspread.demoui.utils.Utils.getKeyIndex;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    private String blueTootchAddress = "";

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private String transactionTypeString = "";

    private POS_TYPE posType = POS_TYPE.UART;
    private UsbDevice usbDevice;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static Dialog dialog;
    private String nfcLog = "";
    private String cashbackAmounts = "";
    private String amounts = "";
    private String amount = "";
    private boolean isVisiblePosID;
    private ListView appListView;
    private String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    private QPOSService.TransactionType transactionType = QPOSService.TransactionType.GOODS;
    private boolean isPinCanceled = false;
    private String title;
    private UpdateThread updateThread;
    private String deviceSignCert;
    private String verifySignatureCommand, pedvVerifySignatureCommand;
    private String pubModel = "";
    private String KB;
    private boolean isInitKey;
    private long start_time = 0L;
    private String blueTitle;
    public boolean isConnStatus = false;
    private TextView mtvinfo;
    private RelativeLayout mllinfo;
    private Button mbtnNewpay;
    private GifImageView gifImageView;
    private LinearLayout mllgif;
    private LinearLayout mllchrccard;
    private ImageView ivBackTitle, ivBlue;
    private TextView tvTitle;
    private TextView tvAmount;
    private EditText mKeyIndex;
    private TextView tradeSuccess;
    private EditText statusEditText, pinpadEditText;
    private RecyclerView lvIndicatorBTPOS;
    private RelativeLayout mrllayout;
    private View line1;
    private ImageView imgLine;
    private ScrollView scvText;
    private String disblue = "";
    private String disbuart = "";
    private String posinfo = "";
    private String posUpdate = "";
    public static PinPadDialog pinPadDialog;
    private int type;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private String MifareCards = "";
    private LinearLayout llayoutMifare;
    private Button btnPollCard, btnVerifyCard, btnOperateCard, btnWriteCard, btnReadCard, btnFinishCard;
    private EditText etKeyValue, etBlock, etCardData, etWriteCard, etCardstate;
    private RadioGroup rgkeyClass, rgAddrr;
    private RadioButton rbtnKeyA, rbtnKeyB, rbtnAdd, rbtnReduce, rbtnRestore;
    private String keyclass = "Key A";
    private String blockaddr = "";
    private String mifareCardOperationType = "add";
    private LinearLayout llayoutMifareDesfire;
    private EditText etDesfireState, etSendApdu;
    private Button btnPowerOnNfc, btnSendApdu, btnPowerOffNfc;
    private boolean dealDoneflag = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_payment);

        disblue = getIntent().getStringExtra("disblue");
        disbuart = getIntent().getStringExtra("disbuart");
        posinfo = getIntent().getStringExtra("posinfo");
        MifareCards = getIntent().getStringExtra("MifareCards");
        posUpdate = getIntent().getStringExtra("deviceUpdate");
        transactionTypeString = getIntent().getStringExtra("paytype");
        amounts = getIntent().getStringExtra("inputMoney");
        cashbackAmounts = getIntent().getStringExtra("cashbackAmounts");
        initView();
        initIntent();
        TRACE.setContext(this);

    }

    public void sendInfo(String receipt) {
        Intent intent = new Intent();
        intent.putExtra("info", receipt);
        setResult(2, intent);
        finish();
    }

    private void operateMifareCards() {
        if ("MifareClassic".equals(MifareCards)) {
            if (type != UART) {
                tvTitle.setText(getString(R.string.operate_mifareCards));
            }
            llayoutMifare.setVisibility(View.VISIBLE);
        } else if ("MifareDesfire".equals(MifareCards)) {
            if (type != UART) {
                tvTitle.setText(getString(R.string.operate_mifarDesfire));
            }
            llayoutMifareDesfire.setVisibility(View.VISIBLE);

        }
    }

    private void getPosInfo(String info) {
        dealDoneflag = true;
        if ("posid".equals(info)) {
            TRACE.d("get pos id id");
            tvTitle.setText(getString(R.string.get_pos_id));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_pos_id));
            pos.getQposId();
        } else if ("posinfo".equals(info)) {
            tvTitle.setText(getString(R.string.get_info));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_info));
            pos.getQposInfo();
        } else if ("updatekey".equals(info)) {
            tvTitle.setText(getString(R.string.get_update_key));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_update_key));
            pos.getUpdateCheckValue();
        } else if ("keycheckvalue".equals(info)) {
            tvTitle.setText(getString(R.string.get_key_checkvalue));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_key_checkvalue));
            int keyIdex = getKeyIndex();
            pos.getKeyCheckValue(keyIdex, QPOSService.CHECKVALUE_KEYTYPE.DUKPT_MKSK_ALLTYPE);
        }
    }

    private void updatePosInfo(String updatePosInfo) {
        if ("updateIpeK".equals(updatePosInfo)) {
            tvTitle.setText(getString(R.string.updateIPEK));
            Mydialog.loading(PaymentActivity.this, getString(R.string.updateIPEK));
            int keyIndex = getKeyIndex();
            String ipekGrop = "0" + keyIndex;
            pos.doUpdateIPEKOperation(ipekGrop, "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944");
        } else if ("setMasterkey".equals(updatePosInfo)) {
            tvTitle.setText(getString(R.string.set_Masterkey));
            Mydialog.loading(PaymentActivity.this, getString(R.string.set_Masterkey));
            int keyIndex = getKeyIndex();
            pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", keyIndex);
        } else if ("updateWorkkey".equals(updatePosInfo)) {
            tvTitle.setText(getString(R.string.update_WorkKey));
            Mydialog.loading(PaymentActivity.this, getString(R.string.update_WorkKey));
            int keyIndex = getKeyIndex();
            pos.updateWorkKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",//PIN KEY
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",  //TRACK KEY
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", //MAC KEY
                    keyIndex, 5);
        } else if ("updateFirmware".equals(updatePosInfo)) {
            tvTitle.setText(getString(R.string.updateFirmware));
            updateFirmware();
        } else if ("updateEmvByXml".equals(updatePosInfo)) {
            tvTitle.setText(getString(R.string.updateEMVByXml));
            Mydialog.loading(PaymentActivity.this, getString(R.string.updateEMVByXml));
            pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("emv_profile_tlv_cit.xml", PaymentActivity.this)));
        }
    }

    public void updateFirmware() {
        if (ActivityCompat.checkSelfPermission(PaymentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(PaymentActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
//            updateThread = new UpdateThread();
//            updateThread.start();
            byte[] data = null;
            data = FileUtils.readAssetsLine("CR100D(样机-XFLASH)_master.asc", PaymentActivity.this);
            if (data != null) {
                int a = pos.updatePosFirmware(data, blueTootchAddress);
//                Mydialog.loading(PaymentActivity.this, progres + "%");
                if (a == -1) {
                    Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.charging_warning), new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onConfirm() {
                            finish();
                        }
                    });
                    return;
                }
                updateThread = new UpdateThread();
                updateThread.start();
            } else {
                Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.does_the_file_exist), new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm() {
                        finish();
                    }
                });
                return;

            }
        }
    }


    private void deviceType(int type) {
        dismissDialog();
    }

    private void initView() {
        ivBackTitle = findViewById(R.id.iv_back_title);
        ivBlue = findViewById(R.id.iv_blue);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.device_connect));
        tvAmount = findViewById(R.id.tv_amount);
        tvAmount.setText(amount);
        tradeSuccess = findViewById(R.id.trade_success_flag);
        line1 = findViewById(R.id.line1);
        lvIndicatorBTPOS = findViewById(R.id.lv_indicator_BTPOS);
        imgLine = findViewById(R.id.img_line);
        scvText = findViewById(R.id.scv_text);
        mrllayout = findViewById(R.id.rl_layout);
        mbtnNewpay = findViewById(R.id.btn_newpay);
        mtvinfo = findViewById(R.id.tv_info);
        mllinfo = findViewById(R.id.ll_info);
        gifImageView = findViewById(R.id.giv_gif);
        mllgif = findViewById(R.id.ll_gif);
        mllchrccard = findViewById(R.id.ll_chrccard);
        mllchrccard.setVisibility(View.GONE);
        statusEditText = findViewById(R.id.statusEditText);
        pinpadEditText = findViewById(R.id.pinpadEditText);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tv_progress);
        mllgif.setOnClickListener(this);
        ivBackTitle.setOnClickListener(this);
        ivBlue.setOnClickListener(this);
        mbtnNewpay.setOnClickListener(this);
        tvTitle.setOnClickListener(this);
        llayoutMifare = findViewById(R.id.llayout_mifare);
        btnPollCard = findViewById(R.id.btn_pollCard);
        btnVerifyCard = findViewById(R.id.btn_verifyCard);
        btnOperateCard = findViewById(R.id.btn_operateCard);
        btnWriteCard = findViewById(R.id.btn_writeCard);
        btnReadCard = findViewById(R.id.btn_readCard);
        btnFinishCard = findViewById(R.id.btn_finishCard);
        etKeyValue = findViewById(R.id.et_keyValue);
        etBlock = findViewById(R.id.et_block);
        rgkeyClass = findViewById(R.id.rg_keyClass);
        rbtnKeyA = findViewById(R.id.rbtn_KeyA);
        rbtnKeyB = findViewById(R.id.rbtn_KeyB);
        etCardData = findViewById(R.id.et_cardData);
        etWriteCard = findViewById(R.id.et_writeCard);
        etCardstate = findViewById(R.id.et_cardstate);
        rgAddrr = findViewById(R.id.rg_addrr);
        rbtnAdd = findViewById(R.id.rbtn_Add);
        rbtnReduce = findViewById(R.id.rbtn_Reduce);
        rbtnRestore = findViewById(R.id.rbtn_Restore);

        llayoutMifareDesfire = findViewById(R.id.llayout_mifareDesfire);
        etDesfireState = findViewById(R.id.et_desfireState);
        etSendApdu = findViewById(R.id.et_sendApdu);
        btnPowerOnNfc = findViewById(R.id.btn_powerOnNfc);
        btnSendApdu = findViewById(R.id.btn_sendApdu);
        btnPowerOffNfc = findViewById(R.id.btn_powerOffNfc);
        btnPowerOnNfc.setOnClickListener(this);
        btnSendApdu.setOnClickListener(this);
        btnPowerOffNfc.setOnClickListener(this);

        btnPollCard.setOnClickListener(this);
        btnVerifyCard.setOnClickListener(this);
        btnOperateCard.setOnClickListener(this);
        btnWriteCard.setOnClickListener(this);
        btnReadCard.setOnClickListener(this);
        btnFinishCard.setOnClickListener(this);
        rgkeyClass.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_KeyA:
                        keyclass = "Key A";
                        break;
                    case R.id.rbtn_KeyB:
                        keyclass = "Key B";
                        break;
                    default:
                        break;


                }
            }
        });
        rgAddrr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_Add:
                        mifareCardOperationType = "add";
                        break;
                    case R.id.rbtn_Reduce:
                        mifareCardOperationType = "reduce";
                        break;
                    case R.id.rbtn_Restore:
                        mifareCardOperationType = "restore";
                        break;
                    default:
                        break;
                }
            }
        });

    }

    private void initIntent() {

        type = getIntent().getIntExtra("connect_type", 0);
        switch (type) {
            case UART:
                if (MifareCards == null) {
                    tvTitle.setText(getText(R.string.device_connect));
                } else {
                    dismissDialog();
                    Mydialog.loading(this, "");
                    if ("MifareClassic".equals(MifareCards)) {
                        tvTitle.setText(getString(R.string.operate_mifareCards));
                    } else if ("MifareDesfire".equals(MifareCards)) {
                        tvTitle.setText(getString(R.string.operate_mifarDesfire));
                    }
                }
                mrllayout.setVisibility(View.GONE);
                open(QPOSService.CommunicationMode.UART);
                pos.setDeviceAddress("/dev/ttyS1");
                pos.openUart();
                break;
            default:
                break;

        }
    }

    public boolean flag = false;
    public AlertDialog alert;

    @Override
    public void onResume() {
        super.onResume();

    }

    private void open(QPOSService.CommunicationMode mode) {
        TRACE.d("open");
        MyQposClass listener = new MyQposClass();
        pos = QPOSService.getInstance(this, mode);
        if (pos == null) {
            statusEditText.setText("CommunicationMode unknow");
            Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.communicationMode_unknow), null);
            return;
        }

        if (type == UART) {
            pos.setD20Trade(true);
        } else {
            pos.setD20Trade(false);
        }

        pos.setConext(this);
        //init handler
        handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
//        String sdkVersion = pos.getSdkVersion();
//        Toast.makeText(this, "sdkVersion--" + sdkVersion, Toast.LENGTH_SHORT).show();


        if (!"".equals(disbuart) && disbuart != null) {
            mrllayout.setVisibility(View.GONE);
            try {
                pos.closeUart();
            } catch (Exception e) {

            }
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                dismissDialog();
                finish();
                break;
            case R.id.iv_blue:
                if (pos != null) {


                }
                break;
            case R.id.btn_newpay:
                dismissDialog();
                finish();
                break;
            case R.id.ll_gif:
                if (pos != null) {

                }
                break;
            case R.id.tv_title:
                break;

            case R.id.btn_pollCard:
//                pos.pollOnMifareCard(20);
                break;
            case R.id.btn_finishCard:
//                pos.finishMifareCard(20);
                break;
            case R.id.btn_verifyCard:
                String keyValue = etKeyValue.getText().toString();
                blockaddr = etBlock.getText().toString();
//                pos.setBlockaddr(blockaddr);
//                pos.setKeyValue(keyValue);
//                pos.authenticateMifareCard(QPOSService.MifareCardType.CLASSIC, keyclass, blockaddr, keyValue, 20);
                break;

            case R.id.btn_operateCard:
                blockaddr = etBlock.getText().toString();
                String cardData = etCardData.getText().toString();
//                if ("add".equals(mifareCardOperationType)) {
//                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.ADD, blockaddr, cardData, 20);
//                } else if ("reduce".equals(mifareCardOperationType)) {
//                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.REDUCE, blockaddr, cardData, 20);
//                } else if ("restore".equals(mifareCardOperationType)) {
//                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.RESTORE, blockaddr, cardData, 20);
//                }
                break;
            case R.id.btn_writeCard:
                blockaddr = etBlock.getText().toString();
                String writeCard = etWriteCard.getText().toString();
//                pos.writeMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, writeCard, 20);
                break;
            case R.id.btn_readCard:
                blockaddr = etBlock.getText().toString();
//                pos.readMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, 20);
                break;
            case R.id.btn_powerOnNfc:
                pos.powerOnNFC(false,20);
                break;
            case R.id.btn_sendApdu:
                String apduString = etSendApdu.getText().toString();
                if (apduString != null && !"".equals(apduString)) {
                    pos.sendApduByNFC(apduString, 20);
                } else {
                    Toast.makeText(this, getString(R.string.please_send_apdu_data), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_powerOffNfc:
                pos.powerOffNFC(20);
                break;
            default:
                break;

        }
    }

    private enum POS_TYPE {
         AUDIO, UART, USB, OTG
    }

    class UpdateThread extends Thread {
        private boolean concelFlag = false;
        int progress = 0;

        @Override
        public void run() {

            while (!concelFlag) {
                int i = 0;
                while (!concelFlag && i < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    i++;
                }
                if (concelFlag) {
                    break;
                }
                if (pos == null) {
                    return;
                }
                progress = pos.getUpdateProgress();
                if (progress < 100) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            statusEditText.set
//                            Text(progress + "%");
                            progress++;
                            Log.w("updatefirmware", "" + progress + "%");
                            Message msg = new Message();
                            msg.what = 1003;
                            msg.arg1 = progress;
                            mHandler.sendMessage(msg);

                        }
                    });
                    continue;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        mtvinfo.setText(getString(R.string.update_finished));
                        mllinfo.setVisibility(View.VISIBLE);
                        mbtnNewpay.setVisibility(View.GONE);
                        tradeSuccess.setVisibility(View.GONE);
                        mllchrccard.setVisibility(View.GONE);
                    }
                });

                break;
            }

        }

        public void concelSelf() {
            concelFlag = true;
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
                case 1001:
                    statusEditText.setText(R.string.connecting_bt_pos);
                    sendMsg(1002);
                    break;
                case 1002:
                    break;
                case 1003:
                    int progress = msg.arg1;
                    tvProgress.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    tvProgress.setText(progress + " %");
                    progressBar.setProgress(progress);
                    dealDoneflag = true;
                    Log.w("handlermessage", "progress---" + progress);
                    break;
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
                        TRACE.i("nfc batchdata1: " + tlv);
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + h.get("tlv");
                    } else {
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + nfcLog;
                    }
                    Log.w("nfc", "nfc-------------------");
                    sendRequestToBackend(content);
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

    private void sendRequestToBackend(String data) {
        OkGo.<String>post(Constants.backendUploadUrl).tag(this).headers("X-RapidAPI-Key", Constants.rapidAPIKey).headers("X-RapidAPI-Host", Constants.rapidAPIHost).params("data", data).execute(new AbsCallback<String>() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                TRACE.i("onStart==");
                Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
            }

            @Override
            public void onSuccess(Response<String> response) {
                dismissDialog();
                pinpadEditText.setVisibility(View.GONE);
                tvTitle.setText(getText(R.string.transaction_result));
                mllinfo.setVisibility(View.VISIBLE);
                mtvinfo.setText(data);
                mllchrccard.setVisibility(View.GONE);
            }

            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                return null;
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                dismissDialog();
                TRACE.i("onError==");
                Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.network_failed), null);
            }
        });
    }

    private List<String> keyBoardList = new ArrayList<>();
    private KeyboardUtil keyboardUtil;

    class MyQposClass extends CQPOSService {

        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            dismissDialog();
            String cardNo = "";
            String msg = "";
            if (result == QPOSService.DoTradeResult.NONE) {
//                statusEditText.setText(getString(R.string.no_card_detected));
                msg = getString(R.string.no_card_detected);
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
                statusEditText.setText(getString(R.string.try_another_interface));
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.ICC) {
                statusEditText.setText(getString(R.string.icc_card_inserted));
                TRACE.d("EMV ICC Start");
                pos.doEmvApp(QPOSService.EmvOption.START);
            } else if (result == QPOSService.DoTradeResult.NOT_ICC) {
//                statusEditText.setText(getString(R.string.card_inserted));
                msg = getString(R.string.card_inserted);
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.BAD_SWIPE) {
                statusEditText.setText(getString(R.string.bad_swipe));
                msg = getString(R.string.bad_swipe);
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.CARD_NOT_SUPPORT) {
                statusEditText.setText("GPO NOT SUPPORT");
                msg = "GPO NOT SUPPORT";
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.PLS_SEE_PHONE) {
                statusEditText.setText("PLS SEE PHONE");
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
//                pinpadEditText.setVisibility(View.GONE);
//                tvTitle.setText(getText(R.string.transaction_result));
//                mllinfo.setVisibility(View.VISIBLE);
//                mtvinfo.setText(content);
//                mllchrccard.setVisibility(View.GONE);
                sendRequestToBackend(content);
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
//                    statusEditText.setText(content);
//                pinpadEditText.setVisibility(View.GONE);
//                tvTitle.setText(getText(R.string.transaction_result));
//                mrllayout.setVisibility(View.GONE);
//                mllinfo.setVisibility(View.VISIBLE);
//                mtvinfo.setText(content);
//                mllchrccard.setVisibility(View.GONE);
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

        @Override
        public void onQposInfoResult(Hashtable<String, String> posInfoData) {
            tvTitle.setText(getString(R.string.get_info));
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
            content += getString(R.string.usb) + isUsbConnected + "\n";
            content += getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
            content += getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
            content += getString(R.string.battery_percentage) + batteryPercentage + "\n";
//			}
            content += getString(R.string.hardware_version) + hardwareVersion + "\n";
            content += "SUB : " + SUB + "\n";
            content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
            content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
            content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
            content += "PCI FirmwareVresion:" + pciFirmwareVersion + "\n";
            content += "PCI HardwareVersion:" + pciHardwareVersion + "\n";
            content += "compileTime:" + compileTime + "\n";
            mllinfo.setVisibility(View.VISIBLE);
            tradeSuccess.setVisibility(View.GONE);
            mbtnNewpay.setVisibility(View.GONE);
            mtvinfo.setText(content);
            mllchrccard.setVisibility(View.GONE);
        }

        /**
         * @see QPOSService.QPOSServiceListener#onRequestTransactionResult(QPOSService.TransactionResult)
         */
        @Override
        public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
            TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
            if (transactionResult == QPOSService.TransactionResult.CARD_REMOVED) {
            }
            dealDoneflag = true;
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
            }else {
                msg = transactionResult.name();
            }
            Log.w("TAG", "transactionResult==" + msg);
            Log.w("transactionResult", "transactionResult==" + transactionResult);
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

        @Override
        public void onRequestBatchData(String tlv) {
            dismissDialog();
            dealDoneflag = true;
            pinpadEditText.setVisibility(View.GONE);
            tvTitle.setText(getText(R.string.transaction_result));
            TRACE.d("ICC trade finished");
            String content = getString(R.string.batch_data);
            content += tlv;
            mllinfo.setVisibility(View.VISIBLE);
            mtvinfo.setText(content);
            mllchrccard.setVisibility(View.GONE);
        }

        @Override
        public void onQposIdResult(Hashtable<String, String> posIdTable) {
            dismissDialog();
            dealDoneflag = true;
            TRACE.w("onQposIdResult():" + posIdTable.toString());
            String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
            String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
            String psamId = posIdTable.get("psamId") == null ? "" : posIdTable.get("psamId");
            String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable.get("nfcID");
            String content = "";
            content += getString(R.string.posId) + posId + "\n";
            content += "csn: " + csn + "\n";
            content += "psamId: " + psamId + "\n";
            content += "NFCId: " + NFCId + "\n";
            if (!isVisiblePosID) {
                if (posinfo != null) {
                    tvTitle.setText(getString(R.string.get_pos_id));
                    tradeSuccess.setVisibility(View.GONE);
                    mbtnNewpay.setVisibility(View.GONE);
                    mllinfo.setVisibility(View.VISIBLE);
                    mtvinfo.setText(content);
                    mllchrccard.setVisibility(View.GONE);
                } else {
                    if (type == UART) {
                        if (!"".equals(posId)) {
                            tvTitle.setText("SN:" + posId);
                        } else {
                            tvTitle.setText(getString(R.string.waiting_for_card));
                        }
                        isVisiblePosID = true;
                        dealDoneflag = false;
                        pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                        pos.doTrade(20);

                    }
                }

            } else {
                isVisiblePosID = false;
            }
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            TRACE.d("Please select App -- S，emv card config");
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

        }

        @Override
        public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
            TRACE.d("onRequestWaitingUser()");
            dismissDialog();
            mllchrccard.setVisibility(View.VISIBLE);
        }

        @Override
        public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
            super.onQposRequestPinResult(dataList, offlineTime);
            boolean onlinePin = pos.isOnlinePin();
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
            dismissDialog();
            mllchrccard.setVisibility(View.GONE);
            keyBoardList = dataList;
            MyKeyboardView.setKeyBoardListener(new KeyBoardNumInterface() {
                @Override
                public void getNumberValue(String value) {
//                    statusEditText.setText("Pls click "+dataList.get(0));
                    pos.pinMapSync(value, 20);
                }
            });
            pinpadEditText.setVisibility(View.VISIBLE);
            keyboardUtil = new KeyboardUtil(PaymentActivity.this, scvText, dataList);
            keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, pinpadEditText);//Random keyboard
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

        @Override
        public void onRequestSetAmount() {
            TRACE.d("input amount -- S");
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
            tvTitle.setText(getString(R.string.online_process_requested));
            dismissDialog();


            Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
            TRACE.d("anlysEmvIccData(tlv):" + decodeData.toString());
            if (isPinCanceled) {
                mllchrccard.setVisibility(View.GONE);
            } else {
                mllchrccard.setVisibility(View.GONE);
            }

            OkGo.<String>post(Constants.backendUploadUrl).tag(this).headers("X-RapidAPI-Key", Constants.rapidAPIKey).headers("X-RapidAPI-Host", Constants.rapidAPIHost).params("tlv", tlv).execute(new AbsCallback<String>() {
                @Override
                public void onStart(Request<String, ? extends Request> request) {
                    super.onStart(request);
                    TRACE.i("onStart==");
                    Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
                }


                @Override
                public void onSuccess(Response<String> response) {
                    dismissDialog();
                    String onlineApproveCode = "8A023030";//Currently the default value,
                    // 8A023035 //online decline,This is a generic refusal that has several possible causes. The shopper should contact their issuing bank for clarification.

                    // should be assigned to the server to return data,
                    // the data format is TLV
                    pos.sendOnlineProcessResult(onlineApproveCode);//Script notification/55domain/ICCDATA
                }

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return null;
                }

                @Override
                public void onError(Response<String> response) {
                    super.onError(response);
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
                }
            });

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
        public void onRequestDisplay(QPOSService.Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
            dismissDialog();
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
                mrllayout.setVisibility(View.GONE);
            } else if (displayMsg == QPOSService.Display.PlEASE_TAP_CARD_AGAIN) {
                msg = getString(R.string.please_tap_card_again);
            }
//            Log.w("displayMsg==", "displayMsg==" + msg);
//            Toast.makeText(CheckActivity.this, msg, Toast.LENGTH_SHORT).show();
            Mydialog.loading(PaymentActivity.this, msg);
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.d("onRequestQposConnected()" + "type==" + type);
            dismissDialog();
           if (type == UART) {
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else if (MifareCards != null) {
                    operateMifareCards();
                } else {
                    Log.w("type", "type==" + type);
                    pos.getQposId();
                }

            } else if (type == USB_OTG_CDC_ACM) {
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else if (MifareCards != null) {
                    operateMifareCards();
                } else {
                    pos.getQposId();
                }
            } else {
                tvTitle.setText(getString(R.string.device_connect));
            }
            if (ActivityCompat.checkSelfPermission(PaymentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                //申请权限
                ActivityCompat.requestPermissions(PaymentActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }

        }

        @Override
        public void onRequestQposDisconnected() {
            dismissDialog();
            TRACE.d("onRequestQposDisconnected()");
            statusEditText.setText(getString(R.string.device_unplugged));
        }

        @Override
        public void onError(QPOSService.Error errorState) {
            if (updateThread != null) {
                updateThread.concelSelf();
            }
            TRACE.d("onError" + errorState.toString());
            dismissDialog();
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
            }else {
                msg = errorState.name();
            }
            Mydialog.ErrorDialog(PaymentActivity.this, msg, new Mydialog.OnMyClickListener() {
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

        @Override
        public void onReturnReversalData(String tlv) {
            dealDoneflag = true;
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
            statusEditText.setText(content);
        }


        @Override
        public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
            TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
            etDesfireState.setText("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffIccResult(boolean arg0) {
            TRACE.d("onReturnPowerOffIccResult(boolean arg0):" + arg0);
        }

        @Override
        public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
            TRACE.d("onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            if (arg0) {
                pos.sendApdu("123456");
            }
        }


        @Override
        public void onGetCardNoResult(String cardNo) {
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
            statusEditText.setText("cardNo: " + cardNo);
        }

        @Override
        public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
            tvTitle.setText(getString(R.string.update_WorkKey));
            dismissDialog();
            dealDoneflag = true;
            TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
            if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                mtvinfo.setText(getString(R.string.updateworkkey_success));
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
                mtvinfo.setText(getString(R.string.updateworkkey_fail));
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                mtvinfo.setText(getString(R.string.workkey_vefiry_error));
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                mtvinfo.setText(getString(R.string.workkey_packet_Len_error));
            }

            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);

        }

        @Override
        public void onReturnCustomConfigResult(boolean isSuccess, String result) {
            tvTitle.setText(getString(R.string.updateEMVByXml));
            dismissDialog();
            dealDoneflag = true;
//            TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + TRACE.NEW_LINE + result);
//            statusEditText.setText("result: " + isSuccess + "\ndata: " + result);
            mllinfo.setVisibility(View.VISIBLE);
            mtvinfo.setText("result: " + isSuccess + "\ndata: " + result);
            mbtnNewpay.setVisibility(View.GONE);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
        }

        @Override
        public void onRequestSetPin() {
            TRACE.i("onRequestSetPin()");
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
//                pos.bypassPin();
                    pos.sendPin("".getBytes());
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

        @Override
        public void onReturnSetMasterKeyResult(boolean isSuccess) {
            tvTitle.setText(getString(R.string.set_Masterkey));
            dismissDialog();
            dealDoneflag = true;
//            TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);
//            statusEditText.setText("result: " + isSuccess);
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            mtvinfo.setText("SetMasterkeyResult: " + isSuccess);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
        }


        @Override
        public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
            tvTitle.setText(getString(R.string.updateFirmware));
            dismissDialog();
            String msg = "";
            TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
//            isUpdateFw = false;
            if (arg0 != QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                updateThread.concelSelf();
                msg = "update firmware fail";
            } else {
//                    mhipStatus.setText("");
                msg = "update firmware success";
            }
            mtvinfo.setText("onUpdatePosFirmwareResult" + msg);
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
        }


        @Override
        public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
            TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        }

        @Override
        public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
            TRACE.d("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
            statusEditText.setText("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffNFCResult(boolean arg0) {
            TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
            etDesfireState.setText(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        }

        @Override
        public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
            TRACE.d("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            etDesfireState.setText("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
        }

        @Override
        public void onReturnUpdateIPEKResult(boolean arg0) {
            dealDoneflag = true;
            tvTitle.setText(getString(R.string.updateIPEK));
            dismissDialog();
            TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);
            if (arg0) {
//                statusEditText.setText("update IPEK success");
                mtvinfo.setText(getString(R.string.updateIPEK_success));
            } else {
//                statusEditText.setText("update IPEK fail");
                mtvinfo.setText(getString(R.string.updateIPEK_fail));
            }
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
            mllchrccard.setVisibility(View.GONE);
        }

        @Override
        public void onReturnUpdateEMVResult(boolean arg0) {
            dismissDialog();
//            TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            mtvinfo.setText("updateEmvAppResult: " + arg0);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);

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
    }


    private static final String FILENAME = "dsp_axdd";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
        if (updateThread != null) {
            updateThread.concelSelf();
        }


        if (type == UART) {
            if (pos != null) {
                pos.closeUart();
            }
        }
        finish();
    }
}