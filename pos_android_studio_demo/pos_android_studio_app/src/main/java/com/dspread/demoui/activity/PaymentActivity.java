package com.dspread.demoui.activity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.dspread.demoui.ui.dialog.Mydialog.BLUETOOTH;
import static com.dspread.demoui.ui.dialog.Mydialog.UART;
import static com.dspread.demoui.ui.dialog.Mydialog.USB_OTG_CDC_ACM;
import static com.dspread.demoui.utils.QPOSUtil.HexStringToByteArray;
import static com.dspread.demoui.utils.Utils.getKeyIndex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.demoui.R;
import com.dspread.demoui.beans.BluetoothToolsBean;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.utils.Utils;
import com.dspread.demoui.widget.pinpad.keyboard.KeyBoardNumInterface;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.DUKPK2009_CBC;
import com.dspread.demoui.utils.DeviceUtils;
import com.dspread.demoui.utils.DingTalkTest;
import com.dspread.demoui.utils.FileUtils;
import com.dspread.demoui.utils.ParseASN1Util;
import com.dspread.demoui.utils.QPOSUtil;
import com.dspread.demoui.utils.SystemKeyListener;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.USBClass;
import com.dspread.demoui.utils.Utils;
import com.dspread.demoui.widget.BluetoothAdapter;
import com.dspread.demoui.widget.pinpad.PinPadDialog;
import com.dspread.demoui.widget.pinpad.PinPadView;
import com.dspread.demoui.widget.pinpad.keyboard.KeyBoardNumInterface;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.Util;
import com.dspread.xpos.utils.AESUtil;
import com.dspread.xpos.utils.BASE64Decoder;
import com.dspread.xpos.utils.BASE64Encoder;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pl.droidsonroids.gif.GifImageView;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.dspread.demoui.activity.BaseApplication.getApplicationInstance;
import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.dspread.demoui.ui.dialog.Mydialog.BLUETOOTH;
import static com.dspread.demoui.ui.dialog.Mydialog.UART;
import static com.dspread.demoui.ui.dialog.Mydialog.USB_OTG_CDC_ACM;
import static com.dspread.demoui.utils.QPOSUtil.HexStringToByteArray;
import static com.dspread.demoui.utils.Utils.getKeyIndex;

import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    //    private QPOSService pos;
    private String blueTootchAddress = "";
    private boolean isNormalBlu = false;//to judge if is normal bluetooth
    private BluetoothAdapter m_Adapter = null;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private String transactionTypeString = "";

    private POS_TYPE posType = POS_TYPE.BLUETOOTH;
    private UsbDevice usbDevice;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static Dialog dialog;
    private String nfcLog = "";
    private String nfcData;
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
    private SystemKeyListener systemKeyListener;
    private boolean isNormal = false;
    private boolean isICC;

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
        systemKeyListener = new SystemKeyListener(this);
        systemKeyStart();
        systemKeyListener.startSystemKeyListener();
        isNormal = false;
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

        if (type == BLUETOOTH) {
            if (pos == null) {
                open(QPOSService.CommunicationMode.BLUETOOTH);
            }
            posType = POS_TYPE.BLUETOOTH;
            pos.scanQPos2Mode(this, 20);
        } else {
            if (pos == null) {
                open(QPOSService.CommunicationMode.BLUETOOTH_BLE);
            }
            posType = POS_TYPE.BLUETOOTH_BLE;

            pos.startScanQposBLE(20);
        }
//        pos.clearBluetoothBuffer();
        refreshAdapter();
        if (m_Adapter != null) {
            m_Adapter.notifyDataSetChanged();
        }
    }


    private void refreshAdapter() {
        if (m_Adapter != null) {
            m_Adapter.clearData();
        }
        ArrayList<Map<String, ?>> data = new ArrayList<>();
        Log.w("data", "data=" + data);
        m_Adapter.setListData(data);
    }

    private void scanBlue() {
        lvIndicatorBTPOS.setLayoutManager(new LinearLayoutManager(PaymentActivity.this, LinearLayoutManager.VERTICAL, false));
        if (m_Adapter == null) {
            m_Adapter = new BluetoothAdapter(PaymentActivity.this, null);
        }
        m_Adapter.setOnBluetoothItemClickListener(new BluetoothAdapter.OnBluetoothItemClickListener() {
            @Override
            public void onItemClick(int position, Map<String, ?> itemdata) {

                onBTPosSelected(itemdata);
                Mydialog.loading(PaymentActivity.this, getString(R.string.str_connecting));
                BluetoothToolsBean.setConectedState("CONNECTED");
            }
        });

    }


    private void onBTPosSelected(Map<String, ?> itemdata) {
        pos.stopScanQPos2Mode();
        start_time = System.currentTimeMillis();
        Map<String, ?> dev = (Map<String, ?>) itemdata;
        blueTootchAddress = (String) dev.get("ADDRESS");
        blueTitle = (String) dev.get("TITLE");
        blueTitle = blueTitle.split("\\(")[0];
        sendMsg(1001);
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
            case BLUETOOTH:
                title = getString(R.string.title_blu);
                scanBlue();
                open(QPOSService.CommunicationMode.BLUETOOTH);
                if (pos.getBluetoothState()) {
                    Mydialog.manualExitDialog(PaymentActivity.this, getString(R.string.msg_continue_connect), new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCancel() {
                            pos.disconnectBT();
                            deviceType(BLUETOOTH);
                            refreshAdapter();
                            if (m_Adapter != null) {
                                m_Adapter.notifyDataSetChanged();
                            }
                            lvIndicatorBTPOS.setAdapter(m_Adapter);
                            statusEditText.setVisibility(View.GONE);
                            mrllayout.setVisibility(View.VISIBLE);
                            ivBlue.setVisibility(View.VISIBLE);
                            mllinfo.setVisibility(View.GONE);
                            statusEditText.setText("");
                            Mydialog.manualExitDialog.dismiss();
                        }

                        @Override
                        public void onConfirm() {
                            if (BluetoothToolsBean.getBulueToothName() != null) {
                                tvTitle.setText(BluetoothToolsBean.getBulueToothName());
                            }
                            if (posinfo != null) {
                                getPosInfo(posinfo);
                            } else if (posUpdate != null) {
                                updatePosInfo(posUpdate);
                            } else if (MifareCards != null) {
                                operateMifareCards();
                            } else {

                                int keyIdex = getKeyIndex();
                                pos.doTrade(keyIdex, 30);//start do trade
                                statusEditText.setText("");
                                mllchrccard.setVisibility(View.VISIBLE);

                            }
                            mrllayout.setVisibility(View.GONE);
                            mllinfo.setVisibility(View.GONE);
                            Mydialog.manualExitDialog.dismiss();
                        }
                    });


                } else {
                    if (!"".equals(disblue) && disblue != null) {
                        mrllayout.setVisibility(View.GONE);
                        try {
                            pos.disconnectBT();
                        } catch (Exception e) {

                        }
                        setResult(2);
                        finish();
                        return;
                    } else {
                        deviceType(BLUETOOTH);
                        refreshAdapter();
                        if (m_Adapter != null) {
                            m_Adapter.notifyDataSetChanged();
                        }
                        lvIndicatorBTPOS.setAdapter(m_Adapter);
                        statusEditText.setVisibility(View.GONE);
                        mrllayout.setVisibility(View.VISIBLE);
                        ivBlue.setVisibility(View.VISIBLE);
                        mllinfo.setVisibility(View.GONE);
                        statusEditText.setText("");
                    }
                }

                break;
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
                open(QPOSService.CommunicationMode.UART_SERVICE);
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
        if (type == USB_OTG_CDC_ACM) {
            mrllayout.setVisibility(View.GONE);
            tvTitle.setText(getString(R.string.device_connect));
//            open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
            if (!flag) {
                USBClass usb = new USBClass();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<String> deviceList = usb.GetUSBDevices(getBaseContext());

                if (deviceList == null) {
//                    Toast.makeText(PaymentActivity.this, "No Permission", Toast.LENGTH_SHORT).show();
                    return;
                }

                final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);

                Log.w("items", "tiems.length===" + items.length);
                if (items.length == 1) {
                    String selectedDevice = (String) items[0];
                    Log.w("usbmpos", "selectedDevice==" + selectedDevice);
                    flag = true;
                    usbDevice = USBClass.getMdevices().get(selectedDevice);
                    open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
                    pos.openUsb(usbDevice);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);

                    builder.setTitle("Select a Reader");
                    if (items.length == 0) {
                        builder.setMessage(getString(R.string.setting_disusb));
                        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
                    }
                    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (items.length > item) {
                                String selectedDevice = items[item].toString();
                                dialog.dismiss();
                                flag = true;
                                usbDevice = USBClass.getMdevices().get(selectedDevice);
                                open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
                                pos.openUsb(usbDevice);
                            }
                        }
                    });
                    alert = builder.create();
                    alert.setCanceledOnTouchOutside(false);
                    alert.setCancelable(false);
                    if (!this.isFinishing()) {
                        alert.show();
                    }
                }
            }
        }


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
        if (!"".equals(disblue) && disblue != null) {
            mrllayout.setVisibility(View.GONE);
            try {
                pos.disconnectBT();
            } catch (Exception e) {

            }
//            Intent intent = new Intent();
//            intent.putExtra("info", getString(R.string.blue_disconect));
//            setResult(2, intent);
            setResult(2);
            finish();
            return;
        }
        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
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
//            Intent intent = new Intent();
//            intent.putExtra("info", getString(R.string.blue_disconect));
//            setResult(2, intent);
            finish();
        }
    }

//    private int getKeyIndex() {
////        String s = mKeyIndex.getText().toString();
//        String s = "";
//        if (TextUtils.isEmpty(s)) {
//            return 0;
//        }
//        int i = 0;
//        try {
//            i = Integer.parseInt(s);
//            if (i > 9 || i < 0) {
//                i = 0;
//            }
//        } catch (Exception e) {
//            i = 0;
//            return i;
//        }
//        return i;
//    }

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
            case R.id.iv_blue:
                if (pos != null) {
                    pos.scanQPos2Mode(PaymentActivity.this, 20);

                    pos.clearBluetoothBuffer();
                    refreshAdapter();
                    if (m_Adapter != null) {
                        m_Adapter.notifyDataSetChanged();
                    }

                }
                break;
            case R.id.btn_newpay:
                dismissDialog();
                finish();
                break;
            case R.id.ll_gif:
                if (pos != null) {
                    pos.scanQPos2Mode(PaymentActivity.this, 20);
                    pos.clearBluetoothBuffer();
                    refreshAdapter();
                    if (m_Adapter != null) {
                        m_Adapter.notifyDataSetChanged();
                    }

                }
                break;
            case R.id.tv_title:
                if (pos.getBluetoothState()) {
                    Mydialog.manualExitDialog(PaymentActivity.this, getString(R.string.disconnect), new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCancel() {
                            Mydialog.manualExitDialog.dismiss();
                        }

                        @Override
                        public void onConfirm() {
                            if (pos != null) {
                                pos.disconnectBT();
                            }
                            finish();
                            Mydialog.manualExitDialog.dismiss();
                        }
                    });
                }
                break;

            case R.id.btn_pollCard:
                pos.pollOnMifareCard(20);
                break;
            case R.id.btn_finishCard:
                pos.finishMifareCard(20);
                break;
            case R.id.btn_verifyCard:
                String keyValue = etKeyValue.getText().toString();
                blockaddr = etBlock.getText().toString();
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(keyValue);
                pos.authenticateMifareCard(QPOSService.MifareCardType.CLASSIC, keyclass, blockaddr, keyValue, 20);
                break;

            case R.id.btn_operateCard:
                blockaddr = etBlock.getText().toString();
                String cardData = etCardData.getText().toString();
                if ("add".equals(mifareCardOperationType)) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.ADD, blockaddr, cardData, 20);
                } else if ("reduce".equals(mifareCardOperationType)) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.REDUCE, blockaddr, cardData, 20);
                } else if ("restore".equals(mifareCardOperationType)) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.RESTORE, blockaddr, cardData, 20);
                }
                break;
            case R.id.btn_writeCard:
                blockaddr = etBlock.getText().toString();
                String writeCard = etWriteCard.getText().toString();
                pos.writeMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, writeCard, 20);
                break;
            case R.id.btn_readCard:
                blockaddr = etBlock.getText().toString();
                pos.readMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, 20);
                break;
            case R.id.btn_powerOnNfc:
                pos.powerOnNFC(false, 20);
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
        BLUETOOTH, AUDIO, UART, USB, OTG, BLUETOOTH_BLE
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
                    pos.connectBluetoothDevice(true, 25, blueTootchAddress);
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

    private Handler dingdingHandler = new Handler() {
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
                        dismissDialog();
                        String onlineApproveCode = "8A023030";//Currently the default value,
                        // 8A023035 //online decline,This is a generic refusal that has several possible causes. The shopper should contact their issuing bank for clarification.

                        // should be assigned to the server to return data,
                        // the data format is TLV
                        pos.sendOnlineProcessResult(onlineApproveCode);//Script notification/55domain/ICCDATA
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
        String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                + ", \"tlv\": " + tlvData + "}";
        pinpadEditText.setVisibility(View.GONE);
        tvTitle.setText(getText(R.string.transaction_result));
        Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
        putInfoToDingding(tlvData, data);
//        OkGo.<String>post(Constants.backendUploadUrl).tag(this)
//                .headers("content-type", "application/json")
//                .params("deviceInfo", DeviceUtils.getPhoneDetail())
//                .params("countryCode",DeviceUtils.getDevieCountry(PaymentActivity.this))
//                .params("tlv", data)
//                .execute(new AbsCallback<String>() {
//            @Override
//            public void onStart(Request<String, ? extends Request> request) {
//                super.onStart(request);
//                TRACE.i("onStart==");
//                pinpadEditText.setVisibility(View.GONE);
//                tvTitle.setText(getText(R.string.transaction_result));
//                Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
//
//            }
//
//            @Override
//            public void onSuccess(Response<String> response) {
//                try {
//                    putInfoToDingding(new String(response.getRawResponse().body().bytes()));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                isNormal = true;
//                pinpadEditText.setVisibility(View.GONE);
//                tvTitle.setText(getText(R.string.transaction_result));
//                mllinfo.setVisibility(View.VISIBLE);
//                mtvinfo.setText(data);
//                mllchrccard.setVisibility(View.GONE);
//                dismissDialog();
//            }
//
//            @Override
//            public String convertResponse(okhttp3.Response response) throws Throwable {
//                return null;
//            }
//
//            @Override
//            public void onError(Response<String> response) {
//                super.onError(response);
//                dismissDialog();
//                TRACE.i("onError==");
//                Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.network_failed), null);
//            }
//        });
    }

    private List<String> keyBoardList = new ArrayList<>();
    private static KeyboardUtil keyboardUtil;

    class MyQposClass extends CQPOSService {

        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            dismissDialog();
            String cardNo = "";
            String msg = "";
            isICC = false;
            if (result == QPOSService.DoTradeResult.NONE) {
//                statusEditText.setText(getString(R.string.no_card_detected));
                msg = getString(R.string.no_card_detected);
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
                statusEditText.setText(getString(R.string.try_another_interface));
                Log.w("paymentActivity", "msg==" + msg);
            } else if (result == QPOSService.DoTradeResult.ICC) {
                isICC = true;
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
//                    statusEditText.setText(content);
//                pinpadEditText.setVisibility(View.GONE);
//                tvTitle.setText(getText(R.string.transaction_result));
//                mrllayout.setVisibility(View.GONE);
//                mllinfo.setVisibility(View.VISIBLE);
//                mtvinfo.setText(content);
//                mllchrccard.setVisibility(View.GONE);
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
            } else {
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

        @Override
        public void onRequestTransactionLog(String tlv) {
            TRACE.d("onRequestTransactionLog(String tlv):" + tlv);
            dismissDialog();
            String content = getString(R.string.transaction_log);
            content += tlv;
            statusEditText.setText(content);
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
            content += "conn: " + pos.getBluetoothState() + "\n";
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

                    } else if (type == USB_OTG_CDC_ACM) {
                        if (!"".equals(posId)) {
                            tvTitle.setText("SN:" + posId);
                        } else {
                            tvTitle.setText(getString(R.string.waiting_for_card));
                        }
                        dealDoneflag = false;
                        isVisiblePosID = true;
//                        pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
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
            if (pos != null) {
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
            }
            dismissDialog();
            mllchrccard.setVisibility(View.GONE);
            keyBoardList = dataList;
            MyKeyboardView.setKeyBoardListener(new KeyBoardNumInterface() {
                @Override
                public void getNumberValue(String value) {
//                    statusEditText.setText("Pls click "+dataList.get(0));
                    if (pos != null) {
                        pos.pinMapSync(value, 20);
                    }
                }
            });
            pinpadEditText.setVisibility(View.VISIBLE);
            if (pos != null) {
                keyboardUtil = new KeyboardUtil(PaymentActivity.this, scvText, dataList);
                keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, pinpadEditText);//Random keyboard
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
            String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            String data = "{\"createdAt\": " + requestTime + ", \"deviceInfo\": " + DeviceUtils.getPhoneDetail() + ", \"countryCode\": " + DeviceUtils.getDevieCountry(PaymentActivity.this)
                    + ", \"tlv\": " + tlv + "}";
            Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
            putInfoToDingding(tlv, data);
//            OkGo.<String>post(Constants.backendUploadUrl).tag(this)
//                    .headers("content-type", "application/json")
//                    .params("deviceInfo", DeviceUtils.getPhoneDetail())
//                    .params("countryCode",DeviceUtils.getDevieCountry(PaymentActivity.this))
//                    .params("tlv", tlv).execute(new AbsCallback<String>() {
//                @Override
//                public void onStart(Request<String, ? extends Request> request) {
//                    super.onStart(request);
//                    TRACE.i("onStart==");
//                    Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
//                }
//
//
//                @Override
//                public void onSuccess(Response<String> response) {
//                    dismissDialog();
//                    String onlineApproveCode = "8A023030";//Currently the default value,
//                    // 8A023035 //online decline,This is a generic refusal that has several possible causes. The shopper should contact their issuing bank for clarification.
//
//                    // should be assigned to the server to return data,
//                    // the data format is TLV
//                    pos.sendOnlineProcessResult(onlineApproveCode);//Script notification/55domain/ICCDATA
//                }
//
//                @Override
//                public String convertResponse(okhttp3.Response response) throws Throwable {
//                    return null;
//                }
//
//                @Override
//                public void onError(Response<String> response) {
//                    super.onError(response);
//                    dismissDialog();
//                    TRACE.i("onError==");
//
//                    Mydialog.ErrorDialog(PaymentActivity.this, getString(R.string.network_failed), new Mydialog.OnMyClickListener() {
//                        @Override
//                        public void onCancel() {
//
//                        }
//
//                        @Override
//                        public void onConfirm() {
//                            //8A025A33 //Unable to go online, offline declined
//                            String offlineDeclinedCode = "8A025A33";
//                            pos.sendOnlineProcessResult(offlineDeclinedCode);
//                        }
//                    });
//                }
//            });

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
        public void onRequestFinalConfirm() {
            TRACE.d("onRequestFinalConfirm() ");
            TRACE.d("onRequestFinalConfirm - S");
            dismissDialog();
            if (!isPinCanceled) {
                dialog = new Dialog(PaymentActivity.this);
                dialog.setContentView(R.layout.confirm_dialog);
                dialog.setTitle(getString(R.string.confirm_amount));

                String message = getString(R.string.amount) + ": $" + amounts;
                if (!cashbackAmounts.equals("")) {
                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmounts;
                }
                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);
                dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(true);
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(false);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                pos.finalConfirm(false);
            }
        }

        @Override
        public void onRequestNoQposDetected() {
            TRACE.d("onRequestNoQposDetected()");
            dismissDialog();
            statusEditText.setText(getString(R.string.no_device_detected));
            Log.w("onRequestNoQposDetected", "No pos detected.");
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.d("onRequestQposConnected()" + "type==" + type);
            dismissDialog();
            if (type == BLUETOOTH) {
                mrllayout.setVisibility(View.GONE);
                ivBlue.setVisibility(View.GONE);
                tvTitle.setText(title + "(" + blueTitle.substring(0, 6) + "..." + blueTitle.substring(blueTitle.length() - 3) + ")");
                BluetoothToolsBean.setBulueToothName(title + "(" + blueTitle.substring(0, 6) + "..." + blueTitle.substring(blueTitle.length() - 3) + ")");
                isConnStatus = true;
                int keyIdex = getKeyIndex();
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else if (MifareCards != null) {
                    operateMifareCards();
                } else {
                    pos.doTrade(keyIdex, 30);//start do trade
                }

            } else if (type == UART) {
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else if (MifareCards != null) {
                    operateMifareCards();
                } else {
                    if (pos != null) {
                        pos.getQposId();
                    }
                }

            } else if (type == USB_OTG_CDC_ACM) {
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else if (MifareCards != null) {
                    operateMifareCards();
                } else {
                    if (pos != null) {
                        pos.getQposId();
                    }
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
            if (type == USB_OTG_CDC_ACM) {
                Mydialog.ErrorDialog(PaymentActivity.this, "USB " + getString(R.string.disconnect), null);
            } else if (type == UART) {
                tvTitle.setText(getString(R.string.disconnect));
            }
            if (type == BLUETOOTH) {
                tvTitle.setText(title);
            }
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
        public void onReturnUpdateKeyByTR_31Result(boolean result) {
            super.onReturnUpdateKeyByTR_31Result(result);
            if (result) {
                statusEditText.setText("send TR31 key success!");
            } else {
                statusEditText.setText("send TR31 key fail");
            }
        }


        @Override
        public void onReturnServerCertResult(String serverSignCert, String serverEncryptCert) {
            super.onReturnServerCertResult(serverSignCert, serverEncryptCert);
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
        public void onReturnSetSleepTimeResult(boolean isSuccess) {
            TRACE.d("onReturnSetSleepTimeResult(boolean isSuccess):" + isSuccess);
            String content = "";
            if (isSuccess) {
                content = "set the sleep time success.";
            } else {
                content = "set the sleep time failed.";
            }
            statusEditText.setText(content);
        }

        @Override
        public void onGetCardNoResult(String cardNo) {
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
            statusEditText.setText("cardNo: " + cardNo);
        }

        @Override
        public void onRequestCalculateMac(String calMac) {
            TRACE.d("onRequestCalculateMac(String calMac):" + calMac);
            if (calMac != null && !"".equals(calMac)) {
                calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
            }
            statusEditText.setText("calMac: " + calMac);
            TRACE.d("calMac_result: calMac=> e: " + calMac);
        }

        @Override
        public void onRequestSignatureResult(byte[] arg0) {
            TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());
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
//            Mydialog.pinpadDialog(CheckActivity.this, pos);
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
        public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
            TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());
            StringBuilder sb = new StringBuilder();
            sb.append("APDU Responses: \n");
            for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
                sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
            }
            statusEditText.setText("\n" + sb.toString());
        }

        @Override
        public void onBluetoothBondFailed() {
            TRACE.d("onBluetoothBondFailed()");
            statusEditText.setText("bond failed");
        }

        @Override
        public void onBluetoothBondTimeout() {
            TRACE.d("onBluetoothBondTimeout()");
            statusEditText.setText("bond timeout");
        }

        @Override
        public void onBluetoothBonded() {
            TRACE.d("onBluetoothBonded()");
            statusEditText.setText("bond success");
        }

        @Override
        public void onBluetoothBonding() {
            TRACE.d("onBluetoothBonding()");
            statusEditText.setText("bonding .....");
        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> result) {
            TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
            String s = "serviceCode: " + result.get("serviceCode");
            s += "\n";
            s += "trackblock: " + result.get("trackblock");
            statusEditText.setText(s);
        }

        @Override
        public void onLcdShowCustomDisplay(boolean arg0) {
            TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
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
            statusEditText.setText("randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:" + randomKeyCheckValue);
        }

        @Override
        public void onGetPosComm(int mod, String amount, String posid) {
            TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
        }

        @Override
        public void onPinKey_TDES_Result(String arg0) {
            TRACE.d("onPinKey_TDES_Result(String arg0):" + arg0);
            statusEditText.setText("result:" + arg0);
        }

        @Override
        public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
            TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
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
        public void onCbcMacResult(String result) {
            TRACE.d("onCbcMacResult(String result):" + result);
            if (result == null || "".equals(result)) {
                statusEditText.setText("cbc_mac:false");
            } else {
                statusEditText.setText("cbc_mac: " + result);
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
            if (cardIsExist) {
                statusEditText.setText("cardIsExist:" + cardIsExist);
            } else {
                statusEditText.setText("cardIsExist:" + cardIsExist);
            }
        }

        @Override
        public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onSearchMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String statuString = arg0.get("status");
                String cardTypeString = arg0.get("cardType");
                String cardUidLen = arg0.get("cardUidLen");
                String cardUid = arg0.get("cardUid");
                String cardAtsLen = arg0.get("cardAtsLen");
                String cardAts = arg0.get("cardAts");
                String ATQA = arg0.get("ATQA");
                String SAK = arg0.get("SAK");
                etCardstate.setText("statuString:" + statuString + "\n" + "cardTypeString:" + cardTypeString + "\ncardUidLen:" + cardUidLen + "\ncardUid:" + cardUid + "\ncardAtsLen:" + cardAtsLen + "\ncardAts:" + cardAts + "\nATQA:" + ATQA + "\nSAK:" + SAK);
            } else {
                etCardstate.setText(getString(R.string.poll_card_failed));
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
                statusEditText.setText("Set buzzer success");
            } else {
                statusEditText.setText("Set buzzer failed");
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
                statusEditText.setText("Set master key success");
            } else {
                statusEditText.setText("Set master key failed");
            }
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
        public void onReturnUpdateEMVRIDResult(boolean arg0) {
            dismissDialog();
            TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            mtvinfo.setText("updateEmvDidResult: " + arg0);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
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

        @Override
        public void onBluetoothBoardStateResult(boolean arg0) {
            TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDeviceFound(BluetoothDevice arg0) {
            if (arg0 != null && arg0.getName() != null) {
                TRACE.d("onDeviceFound(BluetoothDevice arg0):" + arg0.getName() + ":" + arg0.toString());
                if (m_Adapter != null) {
                    Map<String, Object> itm = new HashMap<String, Object>();
                    itm.put("ICON", arg0.getBondState() == BluetoothDevice.BOND_BONDED ? Integer.valueOf(R.drawable.bluetooth_blue) : Integer.valueOf(R.drawable.bluetooth_blue_unbond));
                    itm.put("TITLE", arg0.getName() + "(" + arg0.getAddress() + ")");
                    itm.put("ADDRESS", arg0.getAddress());
                    m_Adapter.setData(itm);
                }
                String address = arg0.getAddress();
                String name = arg0.getName();
                name += address + "\n";
                statusEditText.setText(name);
                TRACE.d("found new device" + name);
            } else {
                statusEditText.setText("Don't found new device");
                TRACE.d("Don't found new device");
            }
        }

        @Override
        public void onSetSleepModeTime(boolean arg0) {
            TRACE.d("onSetSleepModeTime(boolean arg0):" + arg0);
            if (arg0) {
                statusEditText.setText("set the Sleep timee Success");
            } else {
                statusEditText.setText("set the Sleep timee unSuccess");
            }
        }

        @Override
        public void onReturnGetEMVListResult(String arg0) {
            TRACE.d("onReturnGetEMVListResult(String arg0):" + arg0);
            if (arg0 != null && arg0.length() > 0) {
                statusEditText.setText("The emv list is : " + arg0);
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
            tvTitle.setText(getString(R.string.get_update_key));
            dismissDialog();
            dealDoneflag = true;
            TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
//                mhipStatus.setText("update checkvalue : " + arg0);
//            if(isUpdateFw){
//                updateFirmware();
//            }
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
            mtvinfo.setText("update checkvalue : " + arg0);
            mllchrccard.setVisibility(View.GONE);
        }

        @Override
        public void onReturnGetQuickEmvResult(boolean arg0) {
            TRACE.d("onReturnGetQuickEmvResult(boolean arg0):" + arg0);
            if (arg0) {
                statusEditText.setText("emv configed");
                pos.setQuickEmv(true);
            } else {
                statusEditText.setText("emv don't configed");
            }
        }

        @Override
        public void onQposDoGetTradeLogNum(String arg0) {
            TRACE.d("onQposDoGetTradeLogNum(String arg0):" + arg0);
            int a = Integer.parseInt(arg0, 16);
            if (a >= 188) {
                statusEditText.setText("the trade num has become max value!!");
                return;
            }
            statusEditText.setText("get log num:" + a);
        }

        @Override
        public void onQposDoTradeLog(boolean arg0) {
            TRACE.d("onQposDoTradeLog(boolean arg0) :" + arg0);
            if (arg0) {
                statusEditText.setText("clear log success!");
            } else {
                statusEditText.setText("clear log fail!");
            }
        }

        @Override
        public void onAddKey(boolean arg0) {
            TRACE.d("onAddKey(boolean arg0) :" + arg0);
            if (arg0) {
                statusEditText.setText("ksn add 1 success");
            } else {
                statusEditText.setText("ksn add 1 failed");
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
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
            mtvinfo.setText("pinKsn: " + pinKsn + "\ntrackKsn: " + trackKsn + "\nemvKsn: " + emvKsn);
            mllchrccard.setVisibility(View.GONE);

        }

        @Override
        public void onQposDoGetTradeLog(String arg0, String arg1) {
            TRACE.d("onQposDoGetTradeLog(String arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
            arg1 = QPOSUtil.convertHexToString(arg1);
            statusEditText.setText("orderId:" + arg1 + "\ntrade log:" + arg0);
        }

        @Override
        public void onRequestDevice() {
            List<UsbDevice> deviceList = getPermissionDeviceList();
            UsbManager mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            for (int i = 0; i < deviceList.size(); i++) {
                UsbDevice usbDevice = deviceList.get(i);
                if (usbDevice.getVendorId() == 2965 || usbDevice.getVendorId() == 0x03EB) {

                    if (mManager.hasPermission(usbDevice)) {
                        pos.setPermissionDevice(usbDevice);
                    } else {
                        devicePermissionRequest(mManager, usbDevice);
                    }
                }
            }
        }

        @Override
        public void onGetKeyCheckValue(Hashtable<String, String> checkValue) {
            super.onGetKeyCheckValue(checkValue);
            tvTitle.setText(getString(R.string.get_key_checkvalue));
            dismissDialog();
            dealDoneflag = true;

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

            tradeSuccess.setVisibility(View.GONE);
            mbtnNewpay.setVisibility(View.GONE);
            mllinfo.setVisibility(View.VISIBLE);
            mtvinfo.setText("PosKeyCheckValue : \n" + stringBuffer);
            mllchrccard.setVisibility(View.GONE);

        }

        /*    @Override
            public void onGetDevicePubKey(String clearKeys) {
                dismissDialog();
                TRACE.d("onGetDevicePubKey(clearKeys):" + clearKeys);
                mllinfo.setVisibility(View.VISIBLE);
                mbtnNewpay.setVisibility(View.GONE);
                tradeSuccess.setVisibility(View.GONE);
                mtvinfo.setText("DevicePubbicKey: \n" + clearKeys);
                mllchrccard.setVisibility(View.GONE);
                String lenStr = clearKeys.substring(0, 4);
                int sum = 0;
                for (int i = 0; i < 4; i++) {
                    int bit = Integer.parseInt(lenStr.substring(i, i + 1));
                    sum += bit * Math.pow(16, (3 - i));
                }
                pubModel = clearKeys.substring(4, 4 + sum * 2);
            }*/
        @Override
        public void onGetDevicePubKey(Hashtable<String, String> hashtable) {
            super.onGetDevicePubKey(hashtable);
            TRACE.d("onGetDevicePubKey(clearKeys):" + hashtable);
            dismissDialog();
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
            mtvinfo.setText("DevicePubbicKey: \n" + pubModel);
            mllchrccard.setVisibility(View.GONE);
            pubModel = hashtable.get("modulus");
        }

        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            dismissDialog();
        }

        @Override
        public void onReturnSignature(boolean b, String signaturedData) {
            if (b) {
                BASE64Encoder base64Encoder = new BASE64Encoder();
                String encode = base64Encoder.encode(signaturedData.getBytes());
                statusEditText.setText("signature data (Base64 encoding):" + encode);
            }
        }

        @Override
        public void onReturnConverEncryptedBlockFormat(String result) {
            statusEditText.setText(result);
        }

        @Override
        public void onFinishMifareCardResult(boolean arg0) {
            TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                etCardstate.setText(getString(R.string.finish_success));
            } else {
                etCardstate.setText(getString(R.string.finish_fail));
            }
        }

        @Override
        public void onVerifyMifareCardResult(boolean arg0) {
            TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                etCardstate.setText(getString(R.string.Verify_success));
            } else {
                etCardstate.setText(getString(R.string.Verify_fail));
            }
        }

        @Override
        public void onReadMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String addr = arg0.get("addr");
                String cardDataLen = arg0.get("cardDataLen");
                String cardData = arg0.get("cardData");
                etCardstate.setText("addr:" + addr + "\ncardDataLen:" + cardDataLen + "\ncardData:" + cardData);
            } else {
                etCardstate.setText(getString(R.string.read_fail));
            }
        }

        @Override
        public void onWriteMifareCardResult(boolean arg0) {
            TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                etCardstate.setText(getString(R.string.write_success));
            } else {
                etCardstate.setText(getString(R.string.write_fail));
            }
        }

        @Override
        public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String cmd = arg0.get("Cmd");
                String blockAddr = arg0.get("blockAddr");
                etCardstate.setText("Cmd:" + cmd + "\nBlock Addr:" + blockAddr);
            } else {
                etCardstate.setText(getString(R.string.operate_failed));
            }
        }

        @Override
        public void getMifareCardVersion(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareCardVersion(Hashtable<String, String> arg0):" + arg0.toString());

                String verLen = arg0.get("versionLen");
                String ver = arg0.get("cardVersion");
                statusEditText.setText("versionLen:" + verLen + "\nverison:" + ver);
            } else {
                statusEditText.setText("get mifare UL version failed");
            }
        }

        @Override
        public void getMifareFastReadData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareFastReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String startAddr = arg0.get("startAddr");
                String endAddr = arg0.get("endAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                statusEditText.setText("startAddr:" + startAddr + "\nendAddr:" + endAddr + "\ndataLen:" + dataLen + "\ncardData:" + cardData);
            } else {
                statusEditText.setText("read fast UL failed");
            }
        }

        @Override
        public void getMifareReadData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String blockAddr = arg0.get("blockAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                statusEditText.setText("blockAddr:" + blockAddr + "\ndataLen:" + dataLen + "\ncardData:" + cardData);
            } else {
                statusEditText.setText("read mifare UL failed");
            }
        }

        @Override
        public void writeMifareULData(String arg0) {
            if (arg0 != null) {
                TRACE.d("writeMifareULData(String arg0):" + arg0);
                statusEditText.setText("addr:" + arg0);
            } else {
                statusEditText.setText("write UL failed");
            }
        }

        @Override
        public void verifyMifareULData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("verifyMifareULData(Hashtable<String, String> arg0):" + arg0.toString());
                String dataLen = arg0.get("dataLen");
                String pack = arg0.get("pack");
                statusEditText.setText("dataLen:" + dataLen + "\npack:" + pack);
            } else {
                statusEditText.setText("verify UL failed");
            }
        }

        @Override
        public void onGetSleepModeTime(String arg0) {
            if (arg0 != null) {
                TRACE.d("onGetSleepModeTime(String arg0):" + arg0.toString());

                int time = Integer.parseInt(arg0, 16);
                statusEditText.setText("time is ： " + time + " seconds");
            } else {
                statusEditText.setText("get the time is failed");
            }
        }

        @Override
        public void onGetShutDownTime(String arg0) {
            dismissDialog();
            if (arg0 != null) {
                TRACE.d("onGetShutDownTime(String arg0):" + arg0.toString());
                mtvinfo.setText("shut down time is : " + Integer.parseInt(arg0, 16) + "s");
            } else {
                mtvinfo.setText(getString(R.string.get_shutdown_time_fail));
            }
            mllinfo.setVisibility(View.VISIBLE);
            mbtnNewpay.setVisibility(View.GONE);
            mllchrccard.setVisibility(View.GONE);
            tradeSuccess.setVisibility(View.GONE);
        }

        @Override
        public void onQposDoSetRsaPublicKey(boolean arg0) {
            TRACE.d("onQposDoSetRsaPublicKey(boolean arg0):" + arg0);
            if (arg0) {
                statusEditText.setText("set rsa is successed!");
            } else {
                statusEditText.setText("set rsa is failed!");
            }
        }

        @Override
        public void onQposGenerateSessionKeysResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onQposGenerateSessionKeysResult(Hashtable<String, String> arg0):" + arg0.toString());
                String rsaFileName = arg0.get("rsaReginString");
                String enPinKeyData = arg0.get("enPinKey");
                String enKcvPinKeyData = arg0.get("enPinKcvKey");
                String enCardKeyData = arg0.get("enDataCardKey");
                String enKcvCardKeyData = arg0.get("enKcvDataCardKey");
                statusEditText.setText("rsaFileName:" + rsaFileName + "\nenPinKeyData:" + enPinKeyData + "\nenKcvPinKeyData:" + enKcvPinKeyData + "\nenCardKeyData:" + enCardKeyData + "\nenKcvCardKeyData:" + enKcvCardKeyData);
            } else {
                statusEditText.setText("get key failed,pls try again!");
            }
        }

        @Override
        public void transferMifareData(String arg0) {
            TRACE.d("transferMifareData(String arg0):" + arg0.toString());

            // TODO Auto-generated method stub
            if (arg0 != null) {
                statusEditText.setText("response data:" + arg0);
            } else {
                statusEditText.setText("transfer data failed!");
            }
        }

        @Override
        public void onReturnRSAResult(String arg0) {
            TRACE.d("onReturnRSAResult(String arg0):" + arg0.toString());

            if (arg0 != null) {
                statusEditText.setText("rsa data:\n" + arg0);
            } else {
                statusEditText.setText("get the rsa failed");
            }
        }

        @Override
        public void onRequestNoQposDetectedUnbond() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestNoQposDetectedUnbond()");
        }

        @Override
        public void onReturnDeviceCSRResult(String re) {
            TRACE.d("onReturnDeviceCSRResult:" + re);
            statusEditText.setText("onReturnDeviceCSRResult:" + re);
        }

        @Override
        public void onReturnStoreCertificatesResult(boolean re) {
            TRACE.d("onReturnStoreCertificatesResult:" + re);
            if (isInitKey) {
                statusEditText.setText("Init key result is :" + re);
                isInitKey = false;
            } else {
                statusEditText.setText("Exchange Certificates result is :" + re);
            }

        }

        @Override
        public void onReturnDeviceSigningCertResult(String certificates, String certificatesTree) {
            TRACE.d("onReturnDeviceSigningCertResult:" + certificates + "\n" + certificatesTree);
            deviceSignCert = certificates;
//            String command = getString(R.string.pedi_command, certificates, "1", "oeap");
            String command = "    <string name=\"pedi_command\">&#091;AOPEDI&#059;VS2&#059;RF0&#059;TD1&#059;CC%s&#059;RG%s&#059;CE%s&#059;&#093;</string>\n";
            command = ParseASN1Util.addTagToCommand(command, "CD", certificates);
            TRACE.i("request the RKMS command is " + command);
            String pediRespose = "[AOPEDI;ANY;CC308203B33082029BA00302010202074EB0D60000987E300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330363030303030305A170D3330303330373030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D56311D301B06035504410C14447370726561645F417573524B4D533130312D56311D301B06035504030C14447370726561645F417573524B4D533130312D5630820122300D06092A864886F70D01010105000382010F003082010A0282010100D7FD40DD513EE82491FABA3EB734C3FE69C79973797007A2183EC9C468F73D8E1CB669DDA6DC32CA125F9FAEAC0C0556893C9196FB123B06BC9B880EEF367CD17000C7E0ECF7313DD2D396F29C8D977A65946258BE5A4133462F0675161407EED3D263BC20E9271B9070DCC1A6376F89E7E9E2B304BC756E3E3B61B869A2E39F11067D00B5BA3817673A730F42DC4C037FC214207C70A1E3E43F7D7494E71EBDD5BB0E9AFAE32E422DB90B85E230DF406FB12470AD0360FD7BDFDD1A29BCE91655A835129858A0E9EB04845A80F1E9F8EAA20C67C6B8A61113D6FFDD7DF5719778A03A30F69B0DD9033D5E975F723CC18792CC6988250A7DBD20901450651A810203010001300D06092A864886F70D01010B050003820101008F002AE3AFB49C2E7D99CC7B933617D180CB4E8EA13CBCBE7469FC4E5124033F06E4C3B0DAB3C6CA4625E3CD53E7B86C247CDF100E266059366F8FEEC746507E1B0D0924029805AAB89FCE1482946B8B0C1F546DD56B399AB48891B731148C878EF4D02AE641717A3D381C7B62011B76A6FFBF20846217EB68149C96B4B134F980060A542DBE2F32BF7AD308F26A279B41C65E32D4E260AE68B3010685CE36869EFF09D211CE64401F417A72F29F49A2EE713ACC37C29AECBFEBE571EF11D883815F54FA3E52A917CC3D6B008A3E3C52164FF5591D869026D248873F15DE531104F329C279FC5B6BC28ABC833F8C31BEF47783A5D5B9C534A57530D9AE463DC3;CD308203B33082029BA00302010202074EB0D60000987C300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330373030303030305A170D3330303330383030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D45311D301B06035504410C14447370726561645F417573524B4D533130312D45311D301B06035504030C14447370726561645F417573524B4D533130312D4530820122300D06092A864886F70D01010105000382010F003082010A0282010100A62A4935B57BA478F41B6C8B3F79E84DB61E516FEC8D5BE3E86FD296C6906625E0316A77F59D6D5075811BA7BB0801366BA7E370B758E3E1DCE005008C13D368536C2216FAF8AF70EBC6B5D1D231AFD19D6270DDBEA6535B46135D1DE11F374978A655FAA8C2A0DDC933CF82E9DC69DABF8676D0E81762D9B01799C83A8DF3EE70584AA4543EBBDAB02A0EFCA6A276588893DD28BD096400E315ECF5FE91EC210EEC2BE8763FEFB57D1448CC7D0FCDC3BDCE4B7BAAD546E0E5E99281B4F1AB052E1B0361977406B6B57B32353E9F338BED29E55E2D1F65C4322B5850D45146D5A66BFE8323C0D3E78E55A8945B622E15295B9176454A868399990B31D7B104CF0203010001300D06092A864886F70D01010B05000382010100296101AC1ED80EF9DD845D03F2D1F822B4AEFD50E0A1F47FA98105155327FDA6CE52BCD650BE1EB6DCD7F3CDF73325E85EE979EF0364970ADF6ED3A247B2E3E2D83D877BEBD66B20F3983E8DF8932F82F30C3FAF980ADF72E9FEE30EBAFC42B19FB1EAEC74BAE16E2D4EF245D18B58FB560A64C9B515EA065ECA7AE81D6ED0B97A24636E1E70EE3F2F3A3364C17C6B36BE82588BBED79F23914D4E4E7E1E3FC2A5438FAB0535D37D6FA52009ACD37B6F413700BBF440B6B94E4F12C7F465B8AAC2A03776AAB9AFBAE42FE19664DC0B4E3D8A90EB185529CABE39335AEC58295E1E073A765733410FD769345E9B99C0AA0CBE3FA815661857DCF7EA3BD35EFB4C;RD04916CCC6289600A55118FC37AF0999E;]";
            String cc = ParseASN1Util.parseToken(pediRespose, "CC");
            String cd = ParseASN1Util.parseToken(pediRespose, "CD");
            BASE64Decoder base64Decoder = new BASE64Decoder();
            try {
                String caChain = QPOSUtil.byteArray2Hex(base64Decoder.decodeBuffer(QPOSUtil.readRSANStream(getAssets().open("FX-Dspread-CA-Tree.pem"))));
                //the api callback is onReturnStoreCertificatesResult
                pos.loadCertificates(cc, cd, caChain);
                statusEditText.setText("is load the server cert to device...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReturnAnalyseDigEnvelop(String result) {
            super.onReturnAnalyseDigEnvelop(result);
            verifySignatureCommand = ParseASN1Util.addTagToCommand(verifySignatureCommand, "KA", result);
            if (pedvVerifySignatureCommand != null) {
                pedvVerifySignatureCommand = ParseASN1Util.addTagToCommand(pedvVerifySignatureCommand, "KA", result);
                TRACE.i("send key encryption command to RMKS is " + pedvVerifySignatureCommand);
            }
            TRACE.i("send key encryption command to RMKS is " + verifySignatureCommand);
            String response = "[AOPEDK;ANY;KN0;KB30819D0201013181973081941370413031313242315458303045303130304B5331384646464630303030303030303031453030303030414332313038323435443442443733373445443932464142363838373438314544363034344137453635433239463132393739383931384441394434353631443235324143414641020102040AFFFF0000000001E00000020100130A4473707265616442444B04021D58;KA0D0B2F2F3178D4045C1363274890494664B23D32BABEA47E5DB42F15C06816107FD293BAFF7371119F0B11B685A29D40DE78D397F9629A56112629452A9525F5261F8BDCA168328C49ACCFF0133C90E91AFCCA1E18178EBBA5E0BFA054B09514BA87EE05F2E4837D2C74E00BFD3B14EB708598517F357F79AA34C89DFEA9F59B6D3CECABA6C211809400DE9D0B0CA09384FDD834B8BFD416C4B09D32B3F5E45001F18E5C3116A0FFD8E0C6ACE567FCCE1AC909FD038FB58F16BB32163866CD9DCB4B131A394757638111B2CF3DC968D58CBAA95279BEFF697C0D92C6A42248B53A3E56E595AD128EDB50710BDBFFCB113A7DC4ECBCE8668482CBFD22CD7B2E42;RDB077A03C07C94F161842AA0C4831E0EF;CE1;AP308205A906092A864886F70D010702A082059A30820596020149310D300B06096086480165030402013082032506092A864886F70D010703A082031604820312020100318201FB308201F70201003081A830819C310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311B3019060355040C0C12447370726561642044657669636573204341311B301906035504410C12447370726561642044657669636573204341311B301906035504030C1244737072656164204465766963657320434102074EB0D600009880304306092A864886F70D0101073036300B0609608648016503040201301806092A864886F70D010108300B0609608648016503040201300D06092A864886F70D01010904000482010092583A07F6280625EE4CA043E3245F2CD6CCA8BAE6E198F4046A5DDE055723D2591A84DDCA4D7F7BB1B179881FD9EC4E33ED22333A9008DAEB3C3B1D7143D1953F2363BEA4C0D2592667C3468F228F856A95A6DCA1FA9CA0AB05D25DC612E7E2BF2AE3012D22C78BB7224C8C8E02146929937C3DF9FA3589B2A486C132477ACFA50BE09528FCBFDA43079AF54C050843BE4BDE701D246D8D8A4C947F12AFD97A66010459BBAE4ED627F687CC3E6DC30B5B35FE3564D9FB07F501B57A73A70AB9C3398E14391B16A5FE45C374984219F0B3A3265A82D3F5A48CEEF3998DCEA59F1CC5821B51605C66C8FD2687778C84B51CCE51C1FBFA876F978E0A9546C425FF3082010C06092A864886F70D010701301406082A864886F70D03070408C8FA8F2094E103118081E85816DF38AEC7C0E569C011DB7212278A767C8934770C7E994E9508E256B693973FBB4B47A78A9F6B1AB2D326CC2A76A53E3731B8A8128B1DE4BEDCCA51E0E740C1A474C21C8CF4A4726F4FBE0DC5CE41C4DB7A2CDBB2EF7B2C0F61B50E34A1A327A5069EB23524DB0D8119C4C407B90277B806288ECAC2826AF8AF6D092B29E90C03554986F38345B6BB247BC1498C2185661BDE318ADECAF199E798D70A058305F686ECC3A267D28EED6052483401EB5B5B84F897CAEA7968B8EEAB23F465CE3F1E7F7F7E402D1AA681D76D34CF9EC0B6BBBE9A513B8C42E5EA5319E218AC996F87767966DBD8F8318202573082025302014930819C308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B444820434102074EB0D60000987E300B0609608648016503040201A0818E301806092A864886F70D010903310B06092A864886F70D0107033020060A2A864886F70D01091903311204104CDCEDD916AAACEEAE548A1C5B0A0EAA301F06092A864886F70D0107013112041041303031364B30544E30304530303030302F06092A864886F70D01090431220420A0E06A133DA8D4A5EC5A2E51E468B470B19E13834019A0C2563BA39308660A1F300D06092A864886F70D0101010500048201003BA0F51DC5B3400E5CD29429663008713C3B61DE0C053590296421635218AEB228A1802C971B18CCF0A137D66FE07B08A0B2A592F11557CC401C353C859E1B82C4BAE146F8AC2955BD1326A3482B173E5589B321FBA0517DCA071F120D0940DC7B8CD33C861E1403CCBD7C3203F1609D261D38B415A0BF234CC9370D18B1004D89BE4C7C4631C7A5D3A1010F0371E25F70B8000D5B94C946571D0F6A730DEF57950AED18839B38B0FF6497D03E960194CF3F113C57575F62E8299FCDE855A1BD36ECE5CAF3DC9F942387A76A329715EC09FDBED3C4FACA06160D538EC00D0166D46152D61F6C665F749E91A0E70E532CE726525B946ACD81510FF47146F00994;]";
            String KA = ParseASN1Util.parseToken(response, "KA");

            KB = ParseASN1Util.parseToken(response, "KB");
            String signatureData = "a57e821386de1038b1a12dc22fa59ce317625680c523bd66bf2b9f840aebe52d020e07105d4107eeb05edd560d0345cd73ce2b68dbf19c61f9d56fbd1ddf9222c47956595b773c88eb7ec4577fb17053d42acf64f3e5c38ff325cdac7b689df029299087b69211e61bdfc22e329eb287456f83ef6c25e84fe1324e36ee85ba7e3accb79eb8ab7b270916a28a42a867e0e050c6950100c90daddb1f421444d16accb6005a312c3273c2f1b28f0c77456ae875081ae594d26139efd267c8dafa15e1b6cf961f3acdb92b26777127f474d24d57611b29f01dec062c02d720c4e759e1757f85ee39e74e05e23aa0aed53d62d05a902a6539a3e986e6dd237888ff92";
            boolean verifyResult = pos.authenticServerResponse(HexStringToByteArray(KA), signatureData);
            verifyResult = true;
            if (verifyResult) {
                if (response.contains("AP")) {
                    String AP = ParseASN1Util.parseToken(response, "AP");
                    ParseASN1Util.parseASN1new(AP.replace("A081", "3081"));
                    String nonce = ParseASN1Util.getNonce();
                    String header = ParseASN1Util.getHeader();
                    String digist = ParseASN1Util.getDigest();
                    String encryptData = ParseASN1Util.getEncryptData();
                    ParseASN1Util.parseASN1new(encryptData.substring(6));
                    String signData = ParseASN1Util.getSignData();
                    String encryptDataWith3des = ParseASN1Util.getEncryptDataWith3Des();
                    String IV = ParseASN1Util.getIVStr();
                    String clearData = "A0818e301806092a864886f70d010903310b06092a864886f70d0107033020060a2a864886f70d01091903311204104cdcedd916aaaceeae548a1c5b0a0eaa301f06092a864886f70d0107013112041041303031364b30544e30304530303030302f06092a864886f70d01090431220420a0e06a133da8d4a5ec5a2e51e468b470b19e13834019a0c2563ba39308660a1f";
                    String envelop = getDigitalEnvelopStr(encryptData, encryptDataWith3des, "01", clearData, signData, IV);
                    //the api callback is onRequestUpdateWorkKeyResult
                    pos.loadSessionKeyByTR_34(envelop);
                } else {
                    statusEditText.setText("signature verification successful.");
                    ParseASN1Util.parseASN1new(KB);
                    String data = ParseASN1Util.getTr31Data();
                    //the api callback is onReturnupdateKeyByTR_31Result
                    pos.updateKeyByTR_31(1, data);
                }
            } else {
                statusEditText.setText("signature verification failed.");
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
    }

    private void devicePermissionRequest(UsbManager mManager, UsbDevice usbDevice) {
        PendingIntent mPermissionIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
        }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mManager.requestPermission(usbDevice, mPermissionIntent);
    }

    private List getPermissionDeviceList() {
        UsbManager mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List deviceList = new ArrayList<UsbDevice>();
        // check for existing devices
        for (UsbDevice device : mManager.getDeviceList().values()) {
            deviceList.add(device);
        }
        return deviceList;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            TRACE.i("usb" + "permission granted for device " + device);
                            pos.setPermissionDevice(device);
                        }
                    } else {
                        TRACE.i("usb" + "permission denied for device " + device);

                    }
                    unregisterReceiver(mUsbReceiver);
                }
            }
        }
    };

    public static String getDigitalEnvelopStr(String encryptData, String encryptDataWith3des, String keyType, String clearData, String signData, String IV) {
        int encryptDataLen = (encryptData.length() / 2);
        int encryptDataWith3desLen = (encryptDataWith3des.length() / 2);
        int clearDataLen = (clearData.length() / 2);
        int signDataLen = (signData.length() / 2);
        int ivLen = IV.length() / 2;
        int len = 2 + 1 + 2 + 2 + encryptDataLen + 2 + encryptDataWith3desLen + 1 + ivLen + 1 + 2 + clearDataLen + 2 + signDataLen;
        String len2 = QPOSUtil.byteArray2Hex(QPOSUtil.intToBytes(len));
        String result = len2 + "010000" + QPOSUtil.intToHex2(encryptDataLen) + encryptData + QPOSUtil.intToHex2(encryptDataWith3desLen) + encryptDataWith3des + "0" + Integer.toString(ivLen, 16) + IV + keyType + QPOSUtil.intToHex2(clearDataLen) + clearData + QPOSUtil.intToHex2(signDataLen) + signData;
        System.out.println("sys = " + result);
        return result;
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
        if (type == USB_OTG_CDC_ACM) {
            if (pos != null) {
                pos.closeUsb();
            }
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