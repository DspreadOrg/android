package com.dspread.demoui.fragment;

import static android.content.Context.LOCATION_SERVICE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.dspread.demoui.activity.MyQposClass;
import com.dspread.demoui.activity.ScanBluetoothActivity;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.interfaces.ConnectStateCallback;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.TitleUpdateListener;

import java.util.ArrayList;
import java.util.List;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;
import com.dspread.demoui.enums.POS_TYPE;
import com.dspread.demoui.utils.USBClass;
import com.dspread.xpos.QPOSService;


/**
 * @author Qianmeng Chen
 * @date 2024-10-31
 * @description Used to connect or disconnect devices. Now support UART,BLUETOOTH,USB connection type
 */
public class SettingFragment extends Fragment {
    TitleUpdateListener titleListener;
    private RadioButton rBtnBlue, rBtnSerialPort, rBtnUsb, tBtnClicked;
    private boolean isChecked;
    private RadioGroup rgType;
    private TextView tvConnectType;
    private POS_TYPE posType = POS_TYPE.BLUETOOTH;
    private QPOSService pos;
    private static final int BLUETOOTH_CODE = 100;
    private ConnectStateCallback connectStateCallback;
    private BaseApplication application;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private SharedPreferencesUtil preferencesUtil;
    private String connType;
    private UsbDevice usbDevice;
    public AlertDialog alertDialog;
    private ProgressBar progressBar;
    private boolean closeConnection =false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        titleListener = (TitleUpdateListener) getActivity();
        titleListener.setFragmentTitle(getString(R.string.menu_setting));
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, null);
        initView(view);
        preferencesUtil = SharedPreferencesUtil.getInstance(getActivity());
        connType = (String) preferencesUtil.get(Constants.connType, "");
        TRACE.i("setting contype == " + connType);
        if (!"".equals(connType)) {
            return view;
        }
        if (Constants.BLUETOOTH.equals(connType)) {
            rBtnBlue.setEnabled(true);
            rgType.check(R.id.rbtn_blue);
        } else if (Constants.UART.equals(connType)) {
            rBtnSerialPort.setEnabled(true);
            rgType.check(R.id.rbtn_serialport);
        } else if (Constants.USB.equals(connType)) {
            rBtnUsb.setEnabled(true);
            rgType.check(R.id.rbtn_usb);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        connectStateCallback = new ConnectStatusCls();
        application = (BaseApplication) getActivity().getApplication();
        MyQposClass.setStateCallback(connectStateCallback);
    }

    private void initView(View view) {
        pos = application.getQposService();
        progressBar = view.findViewById(R.id.progressBar);
        rBtnBlue = view.findViewById(R.id.rbtn_blue);
        rBtnSerialPort = view.findViewById(R.id.rbtn_serialport);
        rBtnUsb = view.findViewById(R.id.rbtn_usb);
        rgType = view.findViewById(R.id.rg_type);
        tvConnectType = view.findViewById(R.id.tv_connect_type);
        rBtnBlue.setOnClickListener(v -> {
            if (tBtnClicked != null && tBtnClicked != rBtnBlue) {
                isChecked = false;
            }
            rBtnBlue.setChecked(!isChecked);
            isChecked = rBtnBlue.isChecked();
            tBtnClicked = rBtnBlue;
            if (rBtnBlue.isChecked()) {
                close();
                bluetoothRelaPer();
                posType = POS_TYPE.BLUETOOTH;
            } else {
                if (tBtnClicked != null && tBtnClicked == rBtnBlue) {
                    close();
                    closeConnection = true;
                    rgType.clearCheck();
                }
            }
        });
        rBtnSerialPort.setOnClickListener(v -> {
            if (tBtnClicked != null && tBtnClicked != rBtnSerialPort) {
                close();
                isChecked = false;
            }
            rBtnSerialPort.setChecked(!isChecked);
            isChecked = rBtnSerialPort.isChecked();
            tBtnClicked = rBtnSerialPort;
            if (rBtnSerialPort.isChecked()) {
                posType = POS_TYPE.UART;
                progressBar.setVisibility(View.VISIBLE);
                application.open(QPOSService.CommunicationMode.UART, getContext());
                pos = application.getQposService();
                preferencesUtil.put(Constants.BluetoothAddress, "/dev/ttyS1");
                pos.setDeviceAddress("/dev/ttyS1");
                pos.openUart();
            } else {
                if (tBtnClicked != null && tBtnClicked == rBtnSerialPort) {
                    closeConnection = true;
                    close();
                    rgType.clearCheck();
                }
            }
        });

        rBtnUsb.setOnClickListener(v -> {
            if (tBtnClicked != null && tBtnClicked != rBtnUsb) {
                isChecked = false;
            }
            rBtnUsb.setChecked(!isChecked);
            isChecked = rBtnUsb.isChecked();
            tBtnClicked = rBtnUsb;
            if (rBtnUsb.isChecked()) {
                close();
                posType = POS_TYPE.USB;
                openUSBDevice();
            } else {
                if (tBtnClicked != null && tBtnClicked == rBtnUsb) {
                    closeConnection = true;
                    close();
                    rgType.clearCheck();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, Menu.FIRST, Menu.NONE, getString(R.string.disconnect)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); // show the text menu
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            close();
            rgType.clearCheck();
            isChecked = false;
            tBtnClicked = null;
            tvConnectType.setText(getString(R.string.setting_connectiontype));
            posType = null;
            clearConnectStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void close() {
        pos = application.getQposService();
        if (pos == null || posType == null) {
            TRACE.d("return close");
        } else if (posType == POS_TYPE.BLUETOOTH) {
            pos.disconnectBT();
        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            pos.disconnectBLE();
        } else if (posType == POS_TYPE.UART) {
            pos.closeUart();
        } else if (posType == POS_TYPE.USB) {
            pos.closeUsb();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        TRACE.i("ishide === " + hidden);
        if (!hidden) {
            titleListener.setFragmentTitle(getString(R.string.menu_setting));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearConnectStatus();
    }

    private void openUSBDevice() {
        USBClass usb = new USBClass();
        ArrayList<String> deviceList = usb.GetUSBDevices(getContext());

        if (deviceList == null) {
            Toast.makeText(getContext(), "No Permission", Toast.LENGTH_SHORT).show();
            rgType.clearCheck();
            clearConnectStatus();
            return;
        }
        final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);
        if (items.length == 1) {
            String selectedDevice = (String) items[0];
            usbDevice = USBClass.getMdevices().get(selectedDevice);
            application.open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM, getContext());
            pos = application.getQposService();
            pos.openUsb(usbDevice);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select a Reader");
            if (items.length == 0) {
                rgType.clearCheck();
                builder.setMessage(getString(R.string.setting_disusb));
                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if (items.length > item) {
                        String selectedDevice = items[item].toString();
                        dialog.dismiss();
                        usbDevice = USBClass.getMdevices().get(selectedDevice);
                        application.open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM, getContext());
                        pos = application.getQposService();
                        pos.openUsb(usbDevice);
                    }
                }
            });
            alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }

    private void clearConnectStatus() {
        application.setQposService(null);
        preferencesUtil.put(Constants.connType, "");
//        posType = null;
    }

    public void bluetoothRelaPer() {
        android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//if bluetooth is disabled, add one fix
            Intent enabler = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                startActivity(enabler);
            }catch (SecurityException e){
                Toast.makeText(getActivity(),"Pls open the bluetooth in device Setting",Toast.LENGTH_LONG).show();
            }
        }
        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        List<String> listProvider = lm.getAllProviders();
        for (String str : listProvider) {
            TRACE.i("provider : " + str);
        }
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//Location service is on
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
                // Request authorization
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        String[] list = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADVERTISE};
                        requestPermissions(list, BLUETOOTH_CODE);
                        Log.w("bluetoothRelaPer", "bluetoothRelaPer--1");
                    } else {
                        navigateToBluActivity();
                    }
                } else {
                    Log.w("bluetoothRelaPer", "bluetoothRelaPer--2");
                    String[] permissions = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
                    requestPermissions(permissions, BLUETOOTH_CODE);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        String[] list = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADVERTISE};
                        requestPermissions(list, BLUETOOTH_CODE);
                        Log.w("bluetoothRelaPer", "bluetoothRelaPer--3");
                    } else {
                        navigateToBluActivity();
                    }
                } else {
                    navigateToBluActivity();
                }
                Log.w("ermission Granted", "ermission Granted");
            }
        } else {
            clearConnectStatus();
            Toast.makeText(getActivity(), "System detects that the GPS location service is not turned on", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "Pls open the LOCATION in your device settings! ", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_CODE) {
            TRACE.d("permission grant ---!");
            for (int i = 0; i < permissions.length; i++) {
                TRACE.d("permission grant ---!" + i);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    clearConnectStatus();
                    TRACE.d("permission deined!");
                    return;
                }
                if (i == permissions.length - 1) {
                    navigateToBluActivity();
                }
            }

        }
    }

    private void navigateToBluActivity() {
        Intent intent = new Intent(getContext(), ScanBluetoothActivity.class);
        startActivity(intent);
    }

    private class ConnectStatusCls implements ConnectStateCallback {

        @Override
        public void onRequestQposConnected() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    preferencesUtil.put(Constants.updateFirmwareStatus, false);
                    if (posType != null) {
                        TRACE.d("setting onRequestQposConnected()");
                        TRACE.d("connected " + posType.name());
                        preferencesUtil.put(Constants.connType, posType.name());
                        if (posType == POS_TYPE.BLUETOOTH) {
                            tvConnectType.setText(getString(R.string.setting_blu));
                        } else if (posType == POS_TYPE.UART) {
                            tvConnectType.setText(getString(R.string.setting_uart));
                        } else if (posType == POS_TYPE.USB) {
                            tvConnectType.setText(getString(R.string.setting_usb));
                        }
                    } else {
                        tvConnectType.setText(getString(R.string.setting_blu));
                    }
                    if (tBtnClicked != null) {
                        tBtnClicked.setChecked(true);
                    }
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Device connected succeed!", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onRequestQposDisconnected() {
            TRACE.i("onRequestQposDisconnected ==");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    if(posType != null && posType == POS_TYPE.BLUETOOTH){
//                        rBtnBlue.setChecked(false);
//                        posType = null;
//                        tvConnectType.setText(getString(R.string.setting_connectiontype));
//                    }
                    boolean status = (boolean) preferencesUtil.get(Constants.updateFirmwareStatus, false);

                    TRACE.i("statuas ==" + status);
                    if (status) {
                        isChecked = false;
                        tBtnClicked = null;
                    }
                    TRACE.i("statuas disconnect ==" + status + " ischeck = " + isChecked);
                    String updateFirmware= (String)preferencesUtil.get("operationType","");
                    if (closeConnection || "updateFirmware".equals(updateFirmware)) {
                        rgType.clearCheck();
                        clearConnectStatus();
                        closeConnection = false;
                        preferencesUtil.put("operationType","");
                    }
                    if(BaseApplication.getApplicationInstance!=null&&"PaymentActivity".equals(BaseApplication.getApplicationInstance.getClass().getSimpleName())){
                        Mydialog.ErrorDialog((Activity) BaseApplication.getApplicationInstance,"Device disconnect!",null);
                        rgType.clearCheck();
                        clearConnectStatus();
                    }else{
                        Toast.makeText(getContext(), "Device disconnect!", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }

        @Override
        public void onRequestNoQposDetected() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearConnectStatus();
                    Toast.makeText(getContext(), "Device connect failed! Pls try again", Toast.LENGTH_LONG).show();
                }
            });

        }
    }

}