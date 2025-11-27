package com.dspread.pos.ui.setting.device_selection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityDeviceSelectionBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class DeviceSelectionActivity extends BaseActivity<ActivityDeviceSelectionBinding, DeviceSelectionViewModel> {

    // Result constant
    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static final String EXTRA_CONNECTION_TYPE = "connection_type";
    public static final int REQUEST_CODE_SELECT_DEVICE = 10001;
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    // private AlertDialog bluetoothDevicesDialog;
    private RecyclerView recyclerView;

    // private POS_TYPE currentPOSType;
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;
    private BluetoothAdapter bluetoothAdapter;

    // 添加扫描状态跟踪和设备列表
    private boolean isScanning = false;
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private Handler handler = new Handler();
    private boolean isSelectBuletooth = false;

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
        TRACE.d("DeviceSelectionActivity initViewModel");
        return new ViewModelProvider(this).get(DeviceSelectionViewModel.class);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void initData() {
        super.initData();
        // Set return button click event
        // binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBackActivity();
            }
        });
        String bluetoothName = SPUtils.getInstance().getString("bluetoothName");
        //bluetoothAddress
        String bluetoothAddress = SPUtils.getInstance().getString("bluetoothAddress");

        if(!TextUtils.isEmpty(bluetoothAddress)&& !TextUtils.isEmpty(bluetoothName)){
            viewModel.isShowDeviceSelectedView.set(true);
            binding.tvBluetoothAddress.setText(bluetoothAddress);
            binding.tvBluetoothSelectedName.setText(bluetoothName);
        }
        initBluetoothDevicesDialog();
        // Set up event monitoring
        // setupEventListeners();

        if (!initBluetooth()) {
            checkLocationAndRequestPermissions(POS_TYPE.BLUETOOTH);
        }

        bluetoothEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Bluetooth is enabled, now check location and request permissions
                        TRACE.d("startBluetoothDiscovery initData");
                        startBluetoothDiscovery();
                    } else {
                        Toast.makeText(this, "Please enable Bluetooth to continue", Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    public void initViewObservable() {
        super.initViewObservable();
    }

    // init bluetooth adapter
    private boolean initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 设备不支持蓝牙
            ToastUtils.showLong("The device doesn't support the bluetooth!");
            return false;
        }
        return true;
    }

    /**
     * Set up event monitoring
     */
    private void initBluetoothDevicesDialog() {
        recyclerView = findViewById(R.id.recycler_bluetooth_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, device -> {
            stopBluetoothDiscovery();
            viewModel.isShowDeviceSelectedView.set(true);
            binding.tvBluetoothAddress.setText(device.getAddress());
            binding.tvBluetoothSelectedName.setText(device.getName());
            isSelectBuletooth = true;

            //保存选中的蓝牙名称与MAC地址

            SPUtils.getInstance().put("bluetoothName", device.getName());
            SPUtils.getInstance().put("bluetoothAddress", device.getAddress());

            SPUtils.getInstance().put("deviceAddress", device.getAddress());
        });
        recyclerView.setAdapter(bluetoothDeviceAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("CheckResult")
    private void requestBluetoothPermissions(POS_TYPE posType) {
        // Request Bluetooth permission
        RxPermissions rxPermissions = new RxPermissions(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 Version and above
            rxPermissions.request(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ).subscribe(granted -> {
                if (granted) {
                    TRACE.i("permission grant above---");
                    viewModel.setShowDeviceSelectionList(true);
                    bluetoothRelaPer(posType);
                } else {
                    Toast.makeText(this, "Pls grant the bluetooth permission first!", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Android versions below 12
            rxPermissions.request(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ).subscribe(granted -> {
                if (granted) {
                    TRACE.i("permission grant below---");
                    viewModel.setShowDeviceSelectionList(true);
                    bluetoothRelaPer(posType);
                } else {
                    Toast.makeText(this, "Pls grant the bluetooth permission first!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void checkLocationAndRequestPermissions(POS_TYPE posType) {
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
                        TRACE.i("open setting---");
                        // 用户从设置返回后重新检查位置服务
                        checkLocationAndRequestPermissions(posType);
                    }
                });
                launcher.launch(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Pls open the LOCATION in your device settings! ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void bluetoothRelaPer(POS_TYPE posType) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                TRACE.i("blu is need to open");
                bluetoothEnableLauncher.launch(enabler);
            } catch (SecurityException e) {
                Toast.makeText(this, "Please open the bluetooth in device Setting", Toast.LENGTH_LONG).show();
            }
        } else {
            TRACE.i("blu is need to start discovery");
            startBluetoothDiscovery();
        }
    }

    /**
     * 优化蓝牙扫描方法
     */
    @SuppressLint("MissingPermission")
    private void startBluetoothDiscovery() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // 停止之前的扫描
        if (isScanning) {
            bluetoothAdapter.cancelDiscovery();
            isScanning = false;
        }

        // 清空之前的结果
        discoveredDevices.clear();
        if (bluetoothDeviceAdapter != null) {
            bluetoothDeviceAdapter.clearDevices();
        }

        // 注册广播接收器（确保每次扫描都重新注册）
        registerBluetoothReceiver();

        // 开始扫描
        isScanning = bluetoothAdapter.startDiscovery();
        if (isScanning) {
            TRACE.d("Bluetooth discovery started successfully");
            Toast.makeText(this, "Scanning for Bluetooth devices...", Toast.LENGTH_SHORT).show();

            // 设置扫描超时（30秒）
            handler.postDelayed(scanTimeoutRunnable, 30000);
        } else {
            TRACE.e("Failed to start Bluetooth discovery");
            Toast.makeText(this, "Failed to start scanning", Toast.LENGTH_SHORT).show();
            isScanning = false;
        }
    }

    private Runnable scanTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (isScanning) {
                TRACE.d("Bluetooth scan timeout");
                stopBluetoothDiscovery();
                Toast.makeText(DeviceSelectionActivity.this,
                        "Scan completed, found " + discoveredDevices.size() + " devices",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 注册蓝牙广播接收器
     */
    private void registerBluetoothReceiver() {
        try {
            // 先取消注册，避免重复注册
            unregisterReceiver(receiver);
        } catch (Exception e) {
            TRACE.d("Receiver was not registered or already unregistered");
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        TRACE.d("Bluetooth broadcast receiver registered");
    }

    /**
     * 停止蓝牙扫描
     */
    @SuppressLint("MissingPermission")
    private void stopBluetoothDiscovery() {
        if (bluetoothAdapter != null && isScanning) {
            bluetoothAdapter.cancelDiscovery();
            isScanning = false;
            TRACE.d("Bluetooth discovery stopped");
        }
        handler.removeCallbacks(scanTimeoutRunnable);
    }

    /**
     * 优化的广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            TRACE.d("Bluetooth broadcast received: " + action);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null && !device.getName().isEmpty()) {
                    // 检查是否已经发现过该设备
                    if (!isDeviceDiscovered(device)) {
                        TRACE.d("New Bluetooth device found: " + device.getName() + " (" + device.getAddress() + ")");
                        discoveredDevices.add(device);
                        bluetoothDeviceAdapter.addDevice(device);
                    } else {
                        TRACE.d("Duplicate device ignored: " + device.getName());
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                TRACE.d("Bluetooth discovery finished");
                isScanning = false;
                handler.removeCallbacks(scanTimeoutRunnable);
                Toast.makeText(DeviceSelectionActivity.this,
                        "Scan completed, found " + discoveredDevices.size() + " devices",
                        Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                TRACE.d("Bluetooth discovery started");
                isScanning = true;
            }
        }
    };

    /**
     * 检查设备是否已经发现过
     */
    private boolean isDeviceDiscovered(BluetoothDevice newDevice) {
        for (BluetoothDevice device : discoveredDevices) {
            if (device.getAddress().equals(newDevice.getAddress())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        TRACE.d("onResume - Registering broadcast receiver");

        // 注册蓝牙广播接收器
        registerBluetoothReceiver();

        if (initBluetooth()) {
            checkLocationAndRequestPermissions(POS_TYPE.BLUETOOTH);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 不在onPause中停止扫描，让扫描在后台继续
        TRACE.d("onPause - Bluetooth scanning may continue in background");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TRACE.d("onDestroy - Cleaning up Bluetooth resources");
        stopBluetoothDiscovery();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            TRACE.e("Error unregistering receiver: " + e.getMessage());
        }
        handler.removeCallbacks(scanTimeoutRunnable);
        handler.removeCallbacksAndMessages(null);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setBackActivity();
    }


    private void setBackActivity() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isBluetoothStates", isSelectBuletooth);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}