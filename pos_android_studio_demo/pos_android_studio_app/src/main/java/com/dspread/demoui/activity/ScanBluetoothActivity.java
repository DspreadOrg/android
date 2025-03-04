package com.dspread.demoui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;
import com.dspread.demoui.beans.BluetoothToolsBean;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.enums.POS_TYPE;
import com.dspread.demoui.interfaces.BluetoothConnectCallback;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.widget.BluetoothAdapter;
import com.dspread.xpos.QPOSService;

//import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScanBluetoothActivity extends AppCompatActivity implements BluetoothConnectCallback {
    private RecyclerView rlBluListView;
    private LinearLayout llScanBlu;
    private BluetoothAdapter m_Adapter = null;
    private QPOSService pos;
    private String blueTootchAddress,bluTitle;
    private SharedPreferencesUtil preferencesUtil;
    private TextView tvTitle;
    private ImageView ivBackTitle;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_scanbluetooeh);
        preferencesUtil = SharedPreferencesUtil.getInstance(this);
        initView();
    }

    private void initView(){
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.scan_bt_device));
        ivBackTitle = findViewById(R.id.iv_back_title);
        progressBar = findViewById(R.id.progressBar);
        BaseApplication application = (BaseApplication) getApplication();
        MyQposClass.setBluetoothConnectCallback(this);
        application.open(QPOSService.CommunicationMode.BLUETOOTH, this);
        pos = application.getQposService();
        rlBluListView = findViewById(R.id.rl_indicator_BTPOS);
        llScanBlu = findViewById(R.id.ll_gif);
        initBluAdapter();
//        pos.clearBluetoothBuffer();
        pos.scanQPos2Mode(this, 20);
        refreshAdapter();
        if (m_Adapter != null) {
            m_Adapter.notifyDataSetChanged();
        }
        rlBluListView.setAdapter(m_Adapter);
        ivBackTitle.setOnClickListener(v -> finish());
        llScanBlu.setOnClickListener(v -> {
            pos.clearBluetoothBuffer();
            pos.scanQPos2Mode(this, 20);
            refreshAdapter();
            if (m_Adapter != null) {
                m_Adapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshAdapter() {
        if (m_Adapter != null) {
            m_Adapter.clearData();
        }
        ArrayList<Map<String, ?>> data = new ArrayList<>();
        m_Adapter.setListData(data);
    }

    private void onBTPosSelected(Map<String, ?> itemdata) {
        pos.stopScanQPos2Mode();
//        start_time = System.currentTimeMillis();
        Map<String, ?> dev = (Map<String, ?>) itemdata;
        blueTootchAddress = (String) dev.get("ADDRESS");
        bluTitle = (String) dev.get("TITLE");
        bluTitle = bluTitle.split("\\(")[0];
        preferencesUtil.put(Constants.BluetoothAddress,blueTootchAddress);
        pos.connectBluetoothDevice(true, 25, blueTootchAddress);
    }

    private void initBluAdapter() {
        rlBluListView.setLayoutManager(new LinearLayoutManager(ScanBluetoothActivity.this, LinearLayoutManager.VERTICAL, false));
        if (m_Adapter == null) {
            m_Adapter = new BluetoothAdapter(ScanBluetoothActivity.this, null);
        }
        m_Adapter.setOnBluetoothItemClickListener(new BluetoothAdapter.OnBluetoothItemClickListener() {
            @Override
            public void onItemClick(int position, Map<String, ?> itemdata) {
                progressBar.setVisibility(View.VISIBLE);
                onBTPosSelected(itemdata);
                BluetoothToolsBean.setConectedState("CONNECTED");
            }
        });

    }

    @Override
    public void onRequestDeviceScanFinished() {
        TRACE.d("Scan finished!");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (device != null && device.getName() != null) {
                    TRACE.d("onDeviceFound(BluetoothDevice arg0):" + device.getName() + ":" + device.toString());
                    if (m_Adapter != null) {
                        Map<String, Object> itm = new HashMap<String, Object>();
                        itm.put("ICON", device.getBondState() == BluetoothDevice.BOND_BONDED ? Integer.valueOf(R.drawable.bluetooth_blue) : Integer.valueOf(R.drawable.bluetooth_blue_unbond));
                        itm.put("TITLE", device.getName() + "(" + device.getAddress() + ")");
                        itm.put("ADDRESS", device.getAddress());
                        m_Adapter.setData(itm);
                    }
                    String address = device.getAddress();
                    String name = device.getName();
                    name += address + "\n";
                } else {
                    TRACE.d("Don't found new device");
                }
            }
        });
    }

    @Override
    public void onRequestQposConnected() {
        TRACE.d("blu onRequestQposConnected()11");
        preferencesUtil.put(Constants.connType, POS_TYPE.BLUETOOTH.name());
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra("isConnected", true);
//        setResult(Activity.RESULT_OK, resultIntent);
//        TRACE.d("blu onRequestQposConnected()22");
        finish();
    }

    @Override
    public void onRequestQposDisconnected() {
        preferencesUtil.put(Constants.BluetoothAddress,"");
    }

    @Override
    public void onRequestNoQposDetected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preferencesUtil.put(Constants.BluetoothAddress,"");
                Toast.makeText(ScanBluetoothActivity.this,"The device bluetooth connect failed! Pls try again!", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isConnected", false);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}