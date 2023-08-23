package com.dspread.demoui.activity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

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
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.demoui.R;
import com.dspread.demoui.widget.pinpad.keyboard.KeyBoardNumInterface;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.DUKPK2009_CBC;
import com.dspread.demoui.utils.FileUtils;
import com.dspread.demoui.utils.ParseASN1Util;
import com.dspread.demoui.utils.QPOSUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.USBClass;
import com.dspread.demoui.utils.bluebean;
import com.dspread.demoui.widget.BluetoothAdapter;
import com.dspread.demoui.widget.pinpad.PayPassDialog;
import com.dspread.demoui.widget.pinpad.PayPassView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import pl.droidsonroids.gif.GifImageView;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    private QPOSService pos;
    private String blueTootchAddress = "";
    private boolean isNormalBlu = false;//to judge if is normal bluetooth
    private BluetoothAdapter m_Adapter = null;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private String transactionTypeString = "";

    private POS_TYPE posType = POS_TYPE.BLUETOOTH;
    private UsbDevice usbDevice;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private Dialog dialog;
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
    private ImageView imgAnimScanbt;
    private View line1;
    private TextView tradeSuccessFlag;
    private ImageView imgLine;
    private ScrollView scvText;
    private String disblue = "";
    private String disbuart = "";
    private String posinfo = "";
    private String posUpdate = "";
    public PayPassDialog Paydialog;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_payment);

        disblue = getIntent().getStringExtra("disblue");
        disbuart = getIntent().getStringExtra("disbuart");
        posinfo = getIntent().getStringExtra("posinfo");
        posUpdate = getIntent().getStringExtra("deviceUpdate");
        transactionTypeString = getIntent().getStringExtra("paytype");
        amounts = getIntent().getStringExtra("inputMoney");
        cashbackAmounts = getIntent().getStringExtra("cashbackAmounts");
        initView();
        initIntent();
    }

    public void sendInfo(String receipt) {
        Intent intent = new Intent();
        intent.putExtra("info", receipt);
        setResult(2, intent);
        finish();
    }

    private void getPosInfo(String info) {
        if (info.equals("posid")) {
            TRACE.d("get pos id id");
            tvTitle.setText(getString(R.string.get_pos_id));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_pos_id));
            pos.getQposId();
        } else if (info.equals("posinfo")) {
            tvTitle.setText(getString(R.string.get_info));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_info));
            pos.getQposInfo();
        } else if (info.equals("updatekey")) {
            tvTitle.setText(getString(R.string.get_update_key));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_update_key));
            pos.getUpdateCheckValue();
        } else if (info.equals("keycheckvalue")) {
            tvTitle.setText(getString(R.string.get_key_checkvalue));
            Mydialog.loading(PaymentActivity.this, getString(R.string.get_key_checkvalue));
            int keyIdex = getKeyIndex();
            pos.getKeyCheckValue(keyIdex, QPOSService.CHECKVALUE_KEYTYPE.DUKPT_MKSK_ALLTYPE);
        }
    }

    private void updatePosInfo(String updatePosInfo) {
        if (updatePosInfo.equals("updateIpeK")) {
            tvTitle.setText(getString(R.string.updateIPEK));
            Mydialog.loading(PaymentActivity.this, getString(R.string.updateIPEK));
            int keyIndex = getKeyIndex();
            String ipekGrop = "0" + keyIndex;
            pos.doUpdateIPEKOperation(ipekGrop, "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944");
        } else if (updatePosInfo.equals("setMasterkey")) {
            tvTitle.setText("set Masterkey");
            Mydialog.loading(PaymentActivity.this, "set Masterkey");
            int keyIndex = getKeyIndex();
            pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", keyIndex);
        } else if (updatePosInfo.equals("updateWorkkey")) {
            tvTitle.setText("updateWorkkey");
            Mydialog.loading(PaymentActivity.this, "update WorkKey");
            int keyIndex = getKeyIndex();
            pos.updateWorkKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",//PIN KEY
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",  //TRACK KEY
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", //MAC KEY
                    keyIndex, 5);
        } else if (updatePosInfo.equals("updateFirmware")) {
            tvTitle.setText(getString(R.string.updateFirmware));
            updateFirmware();
        } else if (updatePosInfo.equals("updateEmvByXml")) {
            tvTitle.setText(getString(R.string.updateEMVByXml));
            Mydialog.loading(PaymentActivity.this, getString(R.string.updateEMVByXml));
            pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("emv_profile_tlv.xml", PaymentActivity.this)));
        }
    }

    public void updateFirmware() {
        if (ActivityCompat.checkSelfPermission(PaymentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(PaymentActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
//            LogFileConfig.getInstance().setWriteFlag(true);
            byte[] data = null;
            List<String> allFiles = null;
//                    allFiles = FileUtils.getAllFiles(FileUtils.POS_Storage_Dir);
            if (allFiles != null) {
                for (String fileName : allFiles) {
                    if (!TextUtils.isEmpty(fileName)) {
                        if (fileName.toUpperCase().endsWith(".asc".toUpperCase())) {
                            data = FileUtils.readLine(fileName);
                            Toast.makeText(PaymentActivity.this, "Upgrade package path:" + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dspread" + File.separator + fileName, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }
            if (data == null || data.length == 0) {
                data = FileUtils.readAssetsLine("A29DW_master.asc", PaymentActivity.this);
            }

            if (data != null) {
                int a = pos.updatePosFirmware(data, blueTootchAddress);

                if (a == -1) {
//                isUpdateFw = false;
//                Toast.makeText(PaymentActivity.this, "please keep the device charging", Toast.LENGTH_LONG).show();
                    Mydialog.ErrorDialog(PaymentActivity.this, "please keep the device charging", new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCencel() {

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
                Mydialog.ErrorDialog(PaymentActivity.this, "Please add firmware version file", new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCencel() {

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


        if (type == 1) {
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
                bluebean.setConected_state("CONNECTED");
            }
        });

    }


    private void onBTPosSelected(Map<String, ?> itemdata) {
        pos.stopScanQPos2Mode();
        start_time = new Date().getTime();
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
        imgAnimScanbt = findViewById(R.id.img_anim_scanbt);
        line1 = findViewById(R.id.line1);
        lvIndicatorBTPOS = findViewById(R.id.lv_indicator_BTPOS);
        tradeSuccessFlag = findViewById(R.id.trade_success_flag);
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
        mllgif.setOnClickListener(this);
        ivBackTitle.setOnClickListener(this);
        ivBlue.setOnClickListener(this);
        mbtnNewpay.setOnClickListener(this);
        tvTitle.setOnClickListener(this);

    }

    private void initIntent() {
        type = getIntent().getIntExtra("connect_type", 0);
        switch (type) {
            case 1:
                title = getString(R.string.title_blu);
                scanBlue();
                open(QPOSService.CommunicationMode.BLUETOOTH);
                if (pos.getBluetoothState()) {
                    Mydialog.TalertDialog(PaymentActivity.this, getString(R.string.msg_continue_connect), new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCencel() {
                            pos.disconnectBT();
                            deviceType(1);
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
                            Mydialog.TalertDialog.dismiss();
                        }

                        @Override
                        public void onConfirm() {
                            if (bluebean.getBulueName() != null) {
                                tvTitle.setText(bluebean.getBulueName());
                            }
                            if (posinfo != null) {
                                getPosInfo(posinfo);
                            } else if (posUpdate != null) {
                                updatePosInfo(posUpdate);
                            } else {

                                int keyIdex = getKeyIndex();
                                pos.doTrade(keyIdex, 30);//start do trade
                                statusEditText.setText("");
                                mllchrccard.setVisibility(View.VISIBLE);

                            }
                            mrllayout.setVisibility(View.GONE);
                            mllinfo.setVisibility(View.GONE);
                            Mydialog.TalertDialog.dismiss();
                        }
                    });


                } else {
                    deviceType(1);
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

                break;
            case 2:
                tvTitle.setText(getText(R.string.device_connect));
                mrllayout.setVisibility(View.GONE);

                open(QPOSService.CommunicationMode.UART);
                Log.w("pos.getBluetoothState()","pos.getBluetoothState()=="+pos.getBluetoothState());
                if (pos.getBluetoothState()) {
                    if (posinfo != null) {
                        getPosInfo(posinfo);
                    } else if (posUpdate != null) {
                        updatePosInfo(posUpdate);
                    }else{
                        blueTootchAddress = "/dev/ttyS1";//tongfang is s1，tianbo is s3
                        pos.setDeviceAddress(blueTootchAddress);
                        pos.openUart();
                    }
                }else {
                    blueTootchAddress = "/dev/ttyS1";//tongfang is s1，tianbo is s3
                    pos.setDeviceAddress(blueTootchAddress);
                    pos.openUart();
                }
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
        if (type == 3) {

            mrllayout.setVisibility(View.GONE);
            tvTitle.setText("Device connect");
            open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
            if (!flag) {
                USBClass usb = new USBClass();
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
                            String selectedDevice = items[item].toString();
                            dialog.dismiss();
                            flag = true;
                            usbDevice = USBClass.getMdevices().get(selectedDevice);
                            open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
                            pos.openUsb(usbDevice);
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
        pos = QPOSService.getInstance(mode);
        if (pos == null) {
            statusEditText.setText("CommunicationMode unknow");
            Mydialog.ErrorDialog(PaymentActivity.this, "CommunicationMode unknow", null);
            return;
        }
        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }

        if (type == 2) {
            pos.setD20Trade(true);
        } else {
            pos.setD20Trade(false);
        }

        pos.setConext(this);
        //init handler
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
//        String sdkVersion = pos.getSdkVersion();
//        Toast.makeText(this, "sdkVersion--" + sdkVersion, Toast.LENGTH_SHORT).show();


        if (!"".equals(disblue) && disblue != null) {
            mrllayout.setVisibility(View.GONE);
            if (pos.getBluetoothState()) {
                try {
                    pos.disconnectBT();
                } catch (Exception e) {
                }
                Intent intent = new Intent();
                intent.putExtra("info", getString(R.string.blue_disconect));
                setResult(2, intent);
                finish();

            } else {
                Intent intent = new Intent();
                intent.putExtra("info", getString(R.string.blue_disconect));
                setResult(2, intent);
                finish();

            }
        }
        if (!"".equals(disbuart) && disbuart != null) {
            mrllayout.setVisibility(View.GONE);
            if (pos.getBluetoothState()) {
                try {
                    pos.closeUart();
                } catch (Exception e) {
                }
                Intent intent = new Intent();
                intent.putExtra("info", getString(R.string.blue_disconect));
                setResult(2, intent);
                finish();

            } else {
                Intent intent = new Intent();
                intent.putExtra("info", getString(R.string.blue_disconect));
                setResult(2, intent);
                finish();

            }
        }
    }

    private int getKeyIndex() {
//        String s = mKeyIndex.getText().toString();
        String s = "1";
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        int i = 0;
        try {
            i = Integer.parseInt(s);
            if (i > 9 || i < 0) {
                i = 0;
            }
        } catch (Exception e) {
            i = 0;
            return i;
        }
        return i;
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
                    pos.scanQPos2Mode(PaymentActivity.this, 20);

                    pos.clearBluetoothBuffer();
//                        refreshAdapter();
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
//                        refreshAdapter();
                    if (m_Adapter != null) {
                        m_Adapter.notifyDataSetChanged();
                    }

                }
                break;
            case R.id.tv_title:
                if (pos.getBluetoothState()) {
                    Mydialog.TalertDialog(PaymentActivity.this, getString(R.string.disconnect), new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCencel() {
                            Mydialog.TalertDialog.dismiss();
                        }

                        @Override
                        public void onConfirm() {
                            if (pos != null) {
                                pos.disconnectBT();
                            }
                            finish();
                            Mydialog.TalertDialog.dismiss();
                        }
                    });
                }
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
                final int progress = pos.getUpdateProgress();
                if (progress < 100) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            statusEditText.setText(progress + "%");
                            Mydialog.loading(PaymentActivity.this, progress + "%");
                        }
                    });
                    continue;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mtvinfo.setText("Update Finished 100%");
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

                    break;
                case 1703:
//                    int keyIndex = getKeyIndex();
//                    String digEnvelopStr = null;
//                    Poskeys posKeys = null;
//                    try {
//                        if (resetIpekFlag) {
//                            posKeys = new DukptKeys();
//                        }
//                        if (resetMasterKeyFlag) {
//                            posKeys = new TMKKey();
//                        }
//                        posKeys.setRSA_public_key(pubModel); //Model of device public key
//                        digEnvelopStr = Envelope.getDigitalEnvelopStrByKey(getAssets().open("priva.pem"),
//                                posKeys, Poskeys.RSA_KEY_LEN.RSA_KEY_1024, keyIndex);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    pos.updateWorkKey(digEnvelopStr);
//                    break;
                default:
                    break;
            }
        }
    };

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (Mydialog.ErrorDialog != null) {
            Log.w("dismissDialog", "ErrorDialog");
            Mydialog.ErrorDialog.dismiss();
        }
        if (Mydialog.TalertDialog != null) {
            Mydialog.TalertDialog.dismiss();
        }
        if (Mydialog.Ldialog != null) {
            Mydialog.Ldialog.dismiss();
        }

        if (Mydialog.onlingDialog != null) {
            Mydialog.onlingDialog.dismiss();
        }
        if (Paydialog != null) {
            Paydialog.dismiss();
        }
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
                pinpadEditText.setVisibility(View.GONE);
                tvTitle.setText(getText(R.string.transaction_result));
                mllinfo.setVisibility(View.VISIBLE);
                mtvinfo.setText(content);
                mllchrccard.setVisibility(View.GONE);
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
                pinpadEditText.setVisibility(View.GONE);
                tvTitle.setText(getText(R.string.transaction_result));
                mrllayout.setVisibility(View.GONE);
                mllinfo.setVisibility(View.VISIBLE);
                mtvinfo.setText(content);
                mllchrccard.setVisibility(View.GONE);
                sendMsg(8003);
            } else if ((result == QPOSService.DoTradeResult.NFC_DECLINED)) {
                statusEditText.setText(getString(R.string.transaction_declined));
                msg = getString(R.string.transaction_declined);
            } else if (result == QPOSService.DoTradeResult.NO_RESPONSE) {
                statusEditText.setText(getString(R.string.card_no_response));
                getString(R.string.card_no_response);
            }
            if (msg != null && !msg.equals("")) {
                Mydialog.ErrorDialog(PaymentActivity.this, msg, null);

            }
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
            }
            Log.w("TAG", "transactionResult==" + msg);
            Log.w("transactionResult", "transactionResult==" + transactionResult);
            if (!"".equals(msg)) {
                Mydialog.ErrorDialog(PaymentActivity.this, msg, new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCencel() {

                    }

                    @Override
                    public void onConfirm() {
                        pos.cancelTrade();
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
            tvTitle.setText(getString(R.string.get_pos_id));
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
            if (!isVisiblePosID) {
                tradeSuccess.setVisibility(View.GONE);
                mbtnNewpay.setVisibility(View.GONE);
                mllinfo.setVisibility(View.VISIBLE);
                mtvinfo.setText(content);
                mllchrccard.setVisibility(View.GONE);
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
            tvTitle.setText(getString(R.string.input_pin));
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
            tvTitle.setText(getString(R.string.online_process_requested));
            dismissDialog();

            Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
            TRACE.d("anlysEmvIccData(tlv):" + decodeData.toString());


            if (isPinCanceled) {
                mllchrccard.setVisibility(View.GONE);
            } else {
                mllchrccard.setVisibility(View.GONE);
            }
            Mydialog.onlingDialog(PaymentActivity.this, isPinCanceled, new Mydialog.OnMyClickListener() {
                @Override
                public void onCencel() {
                    mllinfo.setVisibility(View.VISIBLE);
                    mtvinfo.setText(decodeData.toString());
                    mllchrccard.setVisibility(View.GONE);
                    tradeSuccess.setVisibility(View.GONE);
                    pos.cancelTrade();
                    Mydialog.onlingDialog.dismiss();

                }

                @Override
                public void onConfirm() {
                    if (isPinCanceled) {
                        pos.sendOnlineProcessResult(null);
                    } else {
//									String str = "5A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F009505088004E0009A031406179C01009F02060000000000019F03060000000000009F0702AB009F080200209F0902008C9F0D05D86004A8009F0E0500109800009F0F05D86804F8009F101307010103A02000010A010000000000CE0BCE899F1A0201569F1E0838333230314943439F21031826509F2608881E2E4151E527899F2701809F3303E0F8C89F34030203009F3501229F3602008E9F37042120A7189F4104000000015A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F00";
//									str = "9F26088930C9018CAEBCD69F2701809F101307010103A02802010A0100000000007EF350299F370415B4E5829F360202179505000004E0009A031504169C01009F02060000000010005F2A02015682027C009F1A0201569F03060000000000009F330360D8C89F34030203009F3501229F1E0838333230314943438408A0000003330101019F090200209F410400000001";
                        String str = "8A023030";//Currently the default value,
                        // should be assigned to the server to return data,
                        // the data format is TLV
                        pos.sendOnlineProcessResult(str);//Script notification/55domain/ICCDATA
                    }
                    Mydialog.onlingDialog.dismiss();
                    Mydialog.loading(PaymentActivity.this, getString(R.string.processing));
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
            TRACE.d("onRequestQposConnected()");
            dismissDialog();
            if (type == 1) {
                mrllayout.setVisibility(View.GONE);
                ivBlue.setVisibility(View.GONE);
                tvTitle.setText(title + "(" + blueTitle.substring(0, 6) + "..." + blueTitle.substring(blueTitle.length() - 3) + ")");
                bluebean.setBulueName(title + "(" + blueTitle.substring(0, 6) + "..." + blueTitle.substring(blueTitle.length() - 3) + ")");
                isConnStatus = true;
                int keyIdex = getKeyIndex();
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else {
                    pos.doTrade(keyIdex, 30);//start do trade
                }

            } else if (type == 2) {
                tvTitle.setText(getString(R.string.setting_uart));
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else {
                    isVisiblePosID = true;
                    pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                    pos.doTrade(20);
                }

            } else if (type == 3) {
                tvTitle.setText(getString(R.string.setting_usb));
                if (posinfo != null) {
                    getPosInfo(posinfo);
                } else if (posUpdate != null) {
                    updatePosInfo(posUpdate);
                } else {
                    isVisiblePosID = true;
                    pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                    pos.doTrade(20);
                }
            } else {
                tvTitle.setText("Device connect");
            }
            if (ActivityCompat.checkSelfPermission(PaymentActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                //申请权限
                ActivityCompat.requestPermissions(PaymentActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
//                isVisiblePosID = true;
//                pos.getQposId();

        }

        @Override
        public void onRequestQposDisconnected() {
            dismissDialog();
            if (type == 3) {
                Mydialog.ErrorDialog(PaymentActivity.this, "USB " + getString(R.string.disconnect), null);
            }
            tvTitle.setText(title);
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
            }
            Mydialog.ErrorDialog(PaymentActivity.this, msg, new Mydialog.OnMyClickListener() {
                @Override
                public void onCencel() {

                }

                @Override
                public void onConfirm() {
                    if (pos != null) {
                        pos.cancelTrade();
                    }
                    finish();
                    Mydialog.ErrorDialog.dismiss();
                }
            });
        }

        @Override
        public void onReturnReversalData(String tlv) {
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
            statusEditText.setText(content);
        }

        @Override
        public void onReturnupdateKeyByTR_31Result(boolean result, String keyType) {
            super.onReturnupdateKeyByTR_31Result(result, keyType);
            if (result) {
                statusEditText.setText("send TR31 key success! The keyType is " + keyType);
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
            tvTitle.setText("updateWorkkey");
            dismissDialog();
            TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
            if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                mtvinfo.setText("update work key success");
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
                mtvinfo.setText("update work key fail");
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                mtvinfo.setText("update work key packet vefiry error");
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                mtvinfo.setText("update work key packet len error");
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
            Paydialog = new PayPassDialog(PaymentActivity.this);
            Paydialog.getPayViewPass().setRandomNumber(true).setPayClickListener(new PayPassView.OnPayClickListener() {

                @Override
                public void onCencel() {
                    pos.cancelPin();
                    Paydialog.dismiss();
                }

                @Override
                public void onPaypass() {
//                pos.bypassPin();
                    {
                        pos.sendPin("");
                        Paydialog.dismiss();
                    }
                }

                @Override
                public void onConfirm(String password) {
                    if (password.length() >= 4 && password.length() <= 12) {
                        pos.sendPin(password);
                        Paydialog.dismiss();
                    } else {
                        Toast.makeText(PaymentActivity.this, "The length just can input 4 - 12 digits", Toast.LENGTH_SHORT).show();
                    }
                }


            });
        }

        @Override
        public void onReturnSetMasterKeyResult(boolean isSuccess) {
            tvTitle.setText("set Masterkey");
            dismissDialog();
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
            tvTitle.setText(getString(R.string.updateEMVByXml));
            dismissDialog();
            TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
//            isUpdateFw = false;
            if (arg0 != QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                updateThread.concelSelf();
            } else {
//                    mhipStatus.setText("");
            }
            mtvinfo.setText("onUpdatePosFirmwareResult" + arg0.toString());
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
            statusEditText.setText(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        }

        @Override
        public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
            TRACE.d("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            statusEditText.setText("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
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
                statusEditText.setText("statuString:" + statuString + "\n" + "cardTypeString:" + cardTypeString + "\ncardUidLen:" + cardUidLen + "\ncardUid:" + cardUid + "\ncardAtsLen:" + cardAtsLen + "\ncardAts:" + cardAts + "\nATQA:" + ATQA + "\nSAK:" + SAK);
            } else {
                statusEditText.setText("poll card failed");
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
            tvTitle.setText(getString(R.string.updateIPEK));
            dismissDialog();
            TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);
            if (arg0) {
//                statusEditText.setText("update IPEK success");
                mtvinfo.setText("update IPEK success");
            } else {
//                statusEditText.setText("update IPEK fail");
                mtvinfo.setText("update IPEK fail");
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
        public void onGetKeyCheckValue(List<String> checkValue) {
            tvTitle.setText(getString(R.string.get_key_checkvalue));
            dismissDialog();
            if (checkValue != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{");
                for (int i = 0; i < checkValue.size(); i++) {
                    buffer.append(checkValue.get(i)).append(",");
                }
                buffer.append("}");
                tradeSuccess.setVisibility(View.GONE);
                mbtnNewpay.setVisibility(View.GONE);
                mllinfo.setVisibility(View.VISIBLE);
                mtvinfo.setText("PosKeyCheckValue: \n" + buffer.toString());
                mllchrccard.setVisibility(View.GONE);


            }
        }

        @Override
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
                statusEditText.setText("finish success");
            } else {
                statusEditText.setText("finish fail");
            }
        }

        @Override
        public void onVerifyMifareCardResult(boolean arg0) {
            TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                statusEditText.setText(" onVerifyMifareCardResult success");
            } else {
                statusEditText.setText("onVerifyMifareCardResult fail");
            }
        }

        @Override
        public void onReadMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String addr = arg0.get("addr");
                String cardDataLen = arg0.get("cardDataLen");
                String cardData = arg0.get("cardData");
                statusEditText.setText("addr:" + addr + "\ncardDataLen:" + cardDataLen + "\ncardData:" + cardData);
            } else {
                statusEditText.setText("onReadWriteMifareCardResult fail");
            }
        }

        @Override
        public void onWriteMifareCardResult(boolean arg0) {
            TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                statusEditText.setText("write data success!");
            } else {
                statusEditText.setText("write data fail!");
            }
        }

        @Override
        public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String cmd = arg0.get("Cmd");
                String blockAddr = arg0.get("blockAddr");
                statusEditText.setText("Cmd:" + cmd + "\nBlock Addr:" + blockAddr);
            } else {
                statusEditText.setText("operate failed");
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
                statusEditText.setText("get mafire UL version failed");
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
                statusEditText.setText("read mafire UL failed");
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
                mtvinfo.setText("get the shut down time is fail!");
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
            boolean verifyResult = pos.authenticServerResponse(QPOSUtil.HexStringToByteArray(KA), signatureData);
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
                    pos.updateKeyByTR_31(data, 30);
                }
            } else {
                statusEditText.setText("signature verification failed.");
            }
        }
    }

    private void devicePermissionRequest(UsbManager mManager, UsbDevice usbDevice) {
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(PaymentActivity.this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
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
//        if (pos != null) {
//            pos.cancelTrade();
//        }

        if (type == 2) {
            if (pos != null) {
//                pos.closeUart();
            }
        }
        if (type == 3) {
            if (pos != null) {
                pos.closeUsb();
            }
        }
    }


}