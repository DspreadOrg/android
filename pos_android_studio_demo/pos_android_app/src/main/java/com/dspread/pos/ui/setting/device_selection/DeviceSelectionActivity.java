package com.dspread.pos.ui.setting.device_selection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.common.manager.QPOSCallbackManager;
import com.dspread.pos.posAPI.MyCustomQPOSCallback;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityDeviceSelectionBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;
import java.util.Objects;

import io.reactivex.functions.Consumer;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class DeviceSelectionActivity extends BaseActivity<ActivityDeviceSelectionBinding, DeviceSelectionViewModel>  implements MyCustomQPOSCallback {

    // 结果常量
    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static final String EXTRA_CONNECTION_TYPE = "connection_type";
    public static final int REQUEST_CODE_SELECT_DEVICE = 10001;
    private BluetoothDeviceAdapter bluetoothAdapter;
    private AlertDialog bluetoothDevicesDialog;
    private RecyclerView recyclerView;
    private POS_TYPE currentPOSType;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_device_selection;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public DeviceSelectionViewModel initViewModel() {
        return new ViewModelProvider(this).get(DeviceSelectionViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        QPOSCallbackManager.getInstance().registerCallback(MyCustomQPOSCallback.class, this);
        // 设置返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        initBluetoothDevicesDialog();
        // 设置事件监听
        setupEventListeners();

    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
    }

    /**
     * 设置事件监听
     */
    private void setupEventListeners() {
        // 监听连接方式选择完成事件
        viewModel.connectionMethodSelectedEvent.observe(this, this::onConnectionMethodSelected);

        // 监听显示蓝牙设备列表事件
        viewModel.startScanBluetoothEvent.observe(this, new Observer<POS_TYPE>() {
            @Override
            public void onChanged(POS_TYPE posType) {
                currentPOSType = posType;
                bluetoothRelaPer(posType);
            }
        });
    }

    private void initBluetoothDevicesDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_bluetooth_devices, null);
        recyclerView = dialogView.findViewById(R.id.recycler_bluetooth_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器
        bluetoothAdapter = new BluetoothDeviceAdapter(this,device -> {
            viewModel.stopScanBluetooth();
            viewModel.connectBtnTitle.set("Connect to "+device.getAddress());
            viewModel.bluetoothAddress.set(device.getAddress());
            viewModel.bluetoothName.set(device.getName());
//            viewModel.connectBluetooth(currentPOSType,device.getAddress());
            if (bluetoothDevicesDialog != null && bluetoothDevicesDialog.isShowing()) {
                bluetoothDevicesDialog.dismiss();
            }
        });
        recyclerView.setAdapter(bluetoothAdapter);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> bluetoothDevicesDialog.dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        bluetoothDevicesDialog = builder.create();
    }

    /**
     * 处理连接方式选择完成事件
     */
    private void onConnectionMethodSelected(POS_TYPE posType) {
        // 创建返回结果
        Intent resultIntent = new Intent();
        // 如果不是蓝牙连接，直接返回结果
        if (posType == POS_TYPE.BLUETOOTH) {
            resultIntent.putExtra(EXTRA_DEVICE_NAME, viewModel.bluetoothName.get());
            SPUtils.getInstance().put("device_name",viewModel.bluetoothAddress.get());
        }
        resultIntent.putExtra(EXTRA_CONNECTION_TYPE, posType.name());
        // 设置结果并关闭Activity
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        // 如果是蓝牙连接，会在选择蓝牙设备后返回结果
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("CheckResult")
    private void requestBluetoothPermissions(POS_TYPE posType) {
        // 请求蓝牙权限
        RxPermissions rxPermissions = new RxPermissions(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 及以上版本
            rxPermissions.request(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ).subscribe(granted -> {
                if (granted) {
                    TRACE.i("permission grant ---");
                    if (!bluetoothDevicesDialog.isShowing()) {
                        bluetoothDevicesDialog.show();
                    }
                    // 开始扫描蓝牙设备
                    viewModel.startScanBluetooth();
                } else {
                    Toast.makeText(this, "需要蓝牙权限才能搜索设备", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Android 12 以下版本
            rxPermissions.request(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ).subscribe(granted -> {
                if (granted) {
                    TRACE.i("permission grant ---");
                    if (!bluetoothDevicesDialog.isShowing()) {
                        bluetoothDevicesDialog.show();
                    }
                    // 开始扫描蓝牙设备
                    viewModel.startScanBluetooth();
                } else {
                    Toast.makeText(this, "需要蓝牙权限才能搜索设备", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void bluetoothRelaPer(POS_TYPE posType) {
        android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//if bluetooth is disabled, add one fix
            Intent enabler = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivity(enabler);
            }catch (SecurityException e){
                Toast.makeText(this,"Pls open the bluetooth in device Setting",Toast.LENGTH_LONG).show();
            }
        }
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> listProvider = lm.getAllProviders();
        for (String str : listProvider) {
            TRACE.i("provider : " + str);
        }
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//Location service is on
            requestBluetoothPermissions(posType);
        } else {
            Toast.makeText(this, "System detects that the GPS location service is not turned on", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            //ACTION_LOCATION_SOURCE_SETTINGS
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            try {
                ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                    }
                });
                launcher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Pls open the LOCATION in your device settings! ", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QPOSCallbackManager.getInstance().unregisterCallback(MyCustomQPOSCallback.class);
    }

    @Override
    public void onRequestQposConnected() {
        runOnUiThread(() -> {
            viewModel.isConnecting.set(false);
            currentPOSType = viewModel.posTypes[viewModel.selectedIndex.getValue()];
            viewModel.connectionMethodSelectedEvent.setValue(currentPOSType);
            viewModel.currentPOSType = viewModel.posTypes[viewModel.selectedIndex.getValue()];
            viewModel.connectedDeviceName = viewModel.currentPOSType.name();
            SPUtils.getInstance().put("device_type",viewModel.currentPOSType.name());
            SPUtils.getInstance().put("isConnectedAutoed",true);
        });
    }

    @Override
    public void onRequestQposDisconnected() {
        SPUtils.getInstance().put("device_type","");
        SPUtils.getInstance().put("isConnected",false);
        SPUtils.getInstance().put("isConnectedAutoed",false);
        runOnUiThread(() -> {
            try {
                viewModel.isConnecting.set(false);
                SPUtils.getInstance().put("device_name","");
                if(viewModel.currentPOSType == viewModel.posTypes[viewModel.selectedIndex.getValue()]){
                    // 设置ViewAdapter的isClearing为true，防止触发onCheckedChanged事件
                    binding.radioGroupConnection.clearCheck();
                    viewModel.connectedDeviceName = null;
                    viewModel.selectedIndex.setValue(-1);
                    viewModel.connectBtnTitle.set(getString(R.string.str_connect));
                    viewModel.currentPOSType = null;
                    // 现在可以安全地调用clearCheck()
                }
            } catch (Exception e) {
                // 捕获并记录异常
                e.printStackTrace();

            }
        });
    }

    @Override
    public void onRequestNoQposDetected() {
        MyCustomQPOSCallback.super.onRequestNoQposDetected();
        runOnUiThread(() -> {
            SPUtils.getInstance().put("isConnectedAutoed",false);
            viewModel.isConnecting.set(false);
            SPUtils.getInstance().put("device_name","");
        });
    }

    @Override
    public void onRequestDeviceScanFinished() {
        runOnUiThread(() ->{
            ToastUtils.showShort("Scan finished!");
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDeviceFound(BluetoothDevice device) {
        runOnUiThread(() -> {
            if (bluetoothAdapter != null) {
                if(device.getName() != null && !"".equals(device.getName())) {
                    bluetoothAdapter.addDevice(device);
                }
            }
        });
    }
}
