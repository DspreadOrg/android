package com.dspread.pos.ui.setting.device_selection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.dspread.pos.utils.TRACE;
import com.dspread.pos.utils.USBClass;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityDeviceSelectionBinding;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class DeviceSelectionActivity extends BaseActivity<ActivityDeviceSelectionBinding, DeviceSelectionViewModel>{

    // Result constant
    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static final String EXTRA_CONNECTION_TYPE = "connection_type";
    public static final int REQUEST_CODE_SELECT_DEVICE = 10001;
    private BluetoothDeviceAdapter bluetoothDeviceAdapter;
    private AlertDialog bluetoothDevicesDialog;
    private RecyclerView recyclerView;
    private POS_TYPE currentPOSType;
    private ActivityResultLauncher<Intent> bluetoothEnableLauncher;
    private BluetoothAdapter bluetoothAdapter;

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

    @SuppressLint("MissingPermission")
    @Override
    public void initData() {
        super.initData();
        // Set return button click event
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        initBluetoothDevicesDialog();
        // Set up event monitoring
        setupEventListeners();
        bluetoothEnableLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Bluetooth is enabled, now check location and request permissions
                        bluetoothAdapter.startDiscovery();
                    } else {
                        Toast.makeText(this, "Please enable Bluetooth to continue", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.showUsbDeviceDialogEvent.observe(this, v -> {
            showUsbDeviceDialog();
        });
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
    private void setupEventListeners() {
        // Monitor connection method selection completion event
        viewModel.connectionMethodSelectedEvent.observe(this, this::onConnectionMethodSelected);

        // Monitor and display Bluetooth device list events
        viewModel.startScanBluetoothEvent.observe(this, new Observer<POS_TYPE>() {
            @Override
            public void onChanged(POS_TYPE posType) {
                if(!initBluetooth()){
                    return;
                }
                currentPOSType = posType;
                checkLocationAndRequestPermissions(posType);
            }
        });
    }

    private void initBluetoothDevicesDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_bluetooth_devices, null);
        recyclerView = dialogView.findViewById(R.id.recycler_bluetooth_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this, device -> {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }
            viewModel.bluetoothAddress.set(device.getAddress());
            viewModel.bluetoothName.set(device.getName());
//            viewModel.connectBluetooth(currentPOSType,device.getAddress());
            if (bluetoothDevicesDialog != null && bluetoothDevicesDialog.isShowing()) {
                bluetoothDevicesDialog.dismiss();
            }
            SPUtils.getInstance().put("device_type",POS_TYPE.BLUETOOTH.name());
            SPUtils.getInstance().put("deviceAddress",device.getAddress());
            finish();
        });
        recyclerView.setAdapter(bluetoothDeviceAdapter);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> bluetoothDevicesDialog.dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        bluetoothDevicesDialog = builder.create();
    }

    /**
     * Process connection method selection completion event
     */
    private void onConnectionMethodSelected(POS_TYPE posType) {
        // Create return result
        Intent resultIntent = new Intent();
        // If it's not a Bluetooth connection, return the result directly
        if (posType == POS_TYPE.BLUETOOTH) {
            resultIntent.putExtra(EXTRA_DEVICE_NAME, viewModel.bluetoothName.get());
            SPUtils.getInstance().put("device_name",viewModel.bluetoothAddress.get());
        }
        SPUtils.getInstance().put("device_type",posType.name());
        resultIntent.putExtra(EXTRA_CONNECTION_TYPE, posType.name());
        // Set the result and close it Activity
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        // If it is a Bluetooth connection, the result will be returned after selecting the Bluetooth device
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
                    if (!bluetoothDevicesDialog.isShowing()) {
                        bluetoothDevicesDialog.show();
                    }
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
                    if (!bluetoothDevicesDialog.isShowing()) {
                        bluetoothDevicesDialog.show();
                    }
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
        android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            Intent enabler = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                TRACE.i("blu is need to open");
                bluetoothEnableLauncher.launch(enabler);
            } catch (SecurityException e) {
                Toast.makeText(this, "Please open the bluetooth in device Setting", Toast.LENGTH_LONG).show();
            }
        }else {
            bluetoothAdapter.startDiscovery();
        }
    }

    private void showUsbDeviceDialog() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        USBClass usb = new USBClass();
        ArrayList<String> deviceList = usb.GetUSBDevices(getApplication());
        if(deviceList != null){
            openUsbDeviceDialog(deviceList);
        }
        usb.setUsbPermissionListener(new USBClass.UsbPermissionListener() {
            @Override
            public void onPermissionGranted(UsbDevice device) {
                // Permission obtained successfully, handle your business logic here
                USBClass usb = new USBClass();
                ArrayList<String> deviceList = usb.GetUSBDevices(getApplication());
                openUsbDeviceDialog(deviceList);
            }

            @Override
            public void onPermissionDenied(UsbDevice device) {
                Toast.makeText(getApplication(), "No Permission", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openUsbDeviceDialog(ArrayList<String> deviceList){
        final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);
        if (items.length == 1) {
            String selectedDevice = (String) items[0];
            SPUtils.getInstance().put("deviceAddress",selectedDevice);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select a Reader");
            if (items.length == 0) {
                builder.setMessage(getApplication().getString(R.string.setting_disusb));
                builder.setPositiveButton(getApplication().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
            }
            builder.setSingleChoiceItems(items, -1, (dialog, item) -> {
                if (items.length > item) {
                    String selectedDevice = items[item].toString();
                    dialog.dismiss();
                    SPUtils.getInstance().put("deviceAddress",selectedDevice);
                    finish();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        // 停止扫描
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 发现设备
                if (bluetoothDeviceAdapter != null) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getName() != null && !"".equals(device.getName())) {
                        bluetoothDeviceAdapter.addDevice(device);
                    }
                }
            }
        }
    };
}
