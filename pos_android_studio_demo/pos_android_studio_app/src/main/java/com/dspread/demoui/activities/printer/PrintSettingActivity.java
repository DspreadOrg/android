package com.dspread.demoui.activities.printer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.DialogFactory;
import com.dspread.demoui.R;
import com.dspread.demoui.activities.mp5801.DeviceAdapter;
import com.dspread.demoui.activities.mp5801.PrintActivity;
import com.dspread.helper.printer.BtService;
import com.dspread.helper.printer.Device;
import com.dspread.helper.printer.PrintService;
import com.dspread.helper.printer.PrinterClass;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

//zkc.bluetooth.api扫描蓝牙并连接

public class PrintSettingActivity extends Activity implements AdapterView.OnItemClickListener {
    private String TAG = "BtSetting";
    private ArrayAdapter<String> mPairedDevicesArrayAdapter = null;
    public static ArrayAdapter<String> mNewDevicesArrayAdapter = null;
    public static List<Device> deviceList = new ArrayList<Device>();
    private Button bt_scan;
    private Button btn_serialPrint;
    public Handler mhandler;
    private LinearLayout layoutscan;
    private ListView deviceListView;
    private TextView tv_status;
    private Thread tv_update;
    private boolean tvFlag = true;
    Context _context;
    private Handler conHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                DialogFactory.dismissLoadingDialog();
                Intent intent = new Intent(PrintSettingActivity.this, MPPrintActivity.class);
                startActivity(intent);
            } else {
                DialogFactory.dismissLoadingDialog();
                Toast.makeText(PrintSettingActivity.this, "connect failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static PrinterClass pl = null;// 打印机操作类

    public static boolean checkState = true;
    private Thread pre_tv_update;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    Handler pre_mhandler = null;
    Handler pre_handler = null;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printsetting);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        _context = this;
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.drawable.device_name);

//		InitListView();
        layoutscan = (LinearLayout) findViewById(R.id.layoutscan);
        layoutscan.setVisibility(View.GONE);

        deviceListView = (ListView) findViewById(R.id.list_device);

        deviceListView.setOnItemClickListener(this);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.drawable.device_name);
        deviceList = new ArrayList<Device>();
        btn_serialPrint = findViewById(R.id.btn_serialPrint);
        btn_serialPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrintSettingActivity.this, MPPrintActivity.class);
                startActivity(intent);
            }
        });
        bt_scan = (Button) findViewById(R.id.bt_scan);
        ((Button) findViewById(R.id.bt_con)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFactory.showLoadingDialog(PrintSettingActivity.this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean b = pl.synConnect("DC:0D:30:C4:B6:29", 1);
                        Message obtain = Message.obtain();
                        if (b) {
                            obtain.what = 1;
                        } else {
                            obtain.what = 0;
                        }
                        conHandler.sendMessage(obtain);
                    }
                }).start();

            }
        });
        ((Button) findViewById(R.id.bt_dis)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = pl.synDisconnect();
            }
        });
        bt_scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceList != null) {
                    deviceList.clear();
                }
                if (!pl.IsOpen()) {
                    pl.open(_context);
                }
                layoutscan.setVisibility(View.VISIBLE);
                mNewDevicesArrayAdapter.clear();
                pl.scan();
                deviceList = pl.getDeviceList();
                InitListView();
            }
        });

        pre_mhandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        Log.e(TAG, "readBuf1:" + readBuf[0]);
                        Log.e(TAG, "readBuf2:" + readBuf[1]);
                        Log.e(TAG, "readBuf3:" + readBuf[2]);//查询指令发送1D 61 00    缺纸值为12 不缺纸为00；
                        Log.e(TAG, "readBuf4:" + readBuf[3]);
                        if (readBuf[0] == 0x13) {
                            PrintService.isFUll = true;
                            ShowMsg(getResources().getString(R.string.str_printer_state) + ":" + getResources().getString(R.string.str_printer_bufferfull));
                        } else if (readBuf[0] == 0x11) {
                            PrintService.isFUll = false;
                            ShowMsg(getResources().getString(R.string.str_printer_state) + ":" + getResources().getString(R.string.str_printer_buffernull));
                        } else if (readBuf[2] == 12) {
                            ShowMsg(getResources().getString(R.string.str_printer_state) + ":" + getResources().getString(R.string.str_printer_nopaper));
                        } else if (readBuf[2] == 00) {
                            ShowMsg("有纸");
                        } else if (readBuf[0] == 0x04) {
                            ShowMsg(getResources().getString(R.string.str_printer_state) + ":" + getResources().getString(R.string.str_printer_hightemperature));
                        } else if (readBuf[0] == 0x02) {
                            ShowMsg(getResources().getString(R.string.str_printer_state) + ":" + getResources().getString(R.string.str_printer_lowpower));
                        } else {
                            String readMessage = new String(readBuf, 0, msg.arg1);
                            Log.e("", "" + readMessage);

                            //计算公式 *3/8200
                            if (readMessage.contains("current")) {
                                String[] str_vol = readMessage.split(":");

                                int int_vol = Integer.valueOf(str_vol[1].trim());

                                // 创建一个数值格式化对象
                                NumberFormat numberFormat = NumberFormat.getInstance();
                                // 设置精确到小数点后2位
                                numberFormat.setMaximumFractionDigits(1);

                                String result = numberFormat.format((float) int_vol * 3 / (float) 8200 * 100);

                                ShowMsg("当前电压：" + int_vol + ",电量百分比：" + result + "%");

                            }

                            if (readMessage.contains("temp")) {

                                String[] str_vol = readMessage.split(":");

                                int int_temp = Integer.valueOf(str_vol[1].trim());

                                ShowMsg("当前温度：" + int_temp + "摄氏度");

                            }

                            if (readMessage.contains("800"))// 80mm paper
                            {
                                PrintService.imageWidth = 72;
                                Toast.makeText(PrintSettingActivity.this, "80mm",
                                        Toast.LENGTH_SHORT).show();
                                Log.e("", "imageWidth:" + "80mm");
                            } else if (readMessage.contains("580"))// 58mm paper
                            {
                                PrintService.imageWidth = 48;
                                Toast.makeText(PrintSettingActivity.this, "58mm",
                                        Toast.LENGTH_SHORT).show();
                                Log.e("", "imageWidth:" + "58mm");
                            }
                        }
                        break;
                    case MESSAGE_STATE_CHANGE:// 蓝牙连接状
                        switch (msg.arg1) {
                            case PrinterClass.STATE_CONNECTED:// 已经连接
                                Log.i("--TAG--", "PrePrint-131: 已经连接");
                                DialogFactory.dismissLoadingDialog();
                                break;
                            case PrinterClass.STATE_CONNECTING:// 正在连接
                                Log.i("--TAG--", "PrePrint-135: 正在连接");
                                Toast.makeText(getApplicationContext(),
                                        "STATE_CONNECTING", Toast.LENGTH_SHORT).show();
                                break;
                            case PrinterClass.STATE_LISTEN:
                            case PrinterClass.STATE_NONE:
                                DialogFactory.dismissLoadingDialog();
                                break;
                            case PrinterClass.SUCCESS_CONNECT:
                                Log.i("--TAG--", "PrePrint-138: SUCCESS_CONNECT");
                                pl.write(new byte[]{0x1b, 0x2b});// 检测打印机型号
                                Toast.makeText(getApplicationContext(),
                                        "SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
                                DialogFactory.dismissLoadingDialog();
                                Intent intent = new Intent(PrintSettingActivity.this, PrintActivity.class);
                                startActivity(intent);
                                break;
                            case PrinterClass.FAILED_CONNECT:
                                Toast.makeText(getApplicationContext(),
                                        "FAILED_CONNECT", Toast.LENGTH_SHORT).show();
                                DialogFactory.dismissLoadingDialog();
                                break;
                            case PrinterClass.LOSE_CONNECT:
                                Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
                                        Toast.LENGTH_SHORT).show();
                                DialogFactory.dismissLoadingDialog();
                        }
                        break;
                    case MESSAGE_WRITE:
                        Log.i("--TAG--", "PrePrint-153: MESSAGE_WRITE");
                        break;
                }
                super.handleMessage(msg);
            }
        };

        pre_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        break;
                    case 1:
                        Device d = (Device) msg.obj;
                        if (d != null) {
                            if (deviceList == null) {
                                deviceList = new ArrayList<Device>();
                            }
                            if (!checkData(deviceList, d)) {
                                deviceList.add(d);
                            }
                        }
                        break;
                    case 2:
                        break;
                }
            }
        };

        tv_status = findViewById(R.id.tv_status);
        tv_update = new Thread() {
            public void run() {
                while (tvFlag) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tv_status.post(new Runnable() {
                        @Override
                        public void run() {

                            if (pl != null) {
                                if (pl.getState() == PrinterClass.STATE_CONNECTED) {

                                    tv_status.setText(PrintSettingActivity.this
                                            .getResources().getString(
                                                    R.string.str_connected));

                                    checkState = true;
                                    tvFlag = false;
                                    pl.stopScan();

                                } else if (pl.getState() == PrinterClass.STATE_CONNECTING) {
                                    tv_status.setText(PrintSettingActivity.this
                                            .getResources().getString(
                                                    R.string.str_connecting));
                                } else if (pl.getState() == PrinterClass.LOSE_CONNECT
                                        || pl.getState() == PrinterClass.FAILED_CONNECT) {
                                    checkState = false;
                                    tv_status.setText(getResources().getString(R.string.str_disconnected));
                                } else if (pl.getState() == PrinterClass.STATE_SCAN_STOP) {
                                    tv_status.setText(PrintSettingActivity.this
                                            .getResources().getString(
                                                    R.string.str_scanover));
                                    layoutscan.setVisibility(View.GONE);

                                    InitListView();
                                } else if (pl.getState() == PrinterClass.STATE_SCANING) {
                                    tv_status.setText(PrintSettingActivity.this
                                            .getResources().getString(
                                                    R.string.str_scaning));

                                    InitListView();
                                } else {
                                    int ss = pl.getState();
                                    tv_status.setText(PrintSettingActivity.this
                                            .getResources().getString(
                                                    R.string.str_disconnected));
                                }
                            }
                        }
                    });
                }
            }
        };
        tv_update.start();
        pl = new BtService(this, pre_mhandler, pre_handler);
    }

    private void ShowMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_SHORT).show();
    }

    private boolean checkData(List<Device> list, Device d) {
        for (Device device : list) {
            if (device.deviceAddress.equals(d.deviceAddress)) {
                return true;
            }
        }
        return false;
    }

    //	private MyAdapter adapter=null;
    private DeviceAdapter deviceAdapter = null;

    private void InitListView() {
        if (null == deviceAdapter) {
            deviceAdapter = new DeviceAdapter(this, deviceList);
            deviceListView.setAdapter(deviceAdapter);
        } else {
            deviceAdapter.setInfo(deviceList);
            deviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.str_exit))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkState = false;
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        pl.stopScan();
        DialogFactory.showLoadingDialog(this);

        Device device = (Device) parent.getItemAtPosition(position);
        //connect the blu
        String cmd = device.deviceAddress;
        pl.connect(cmd);
    }
}
