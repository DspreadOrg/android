package com.dspread.pos.ui.setting.connection_settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.ui.setting.device_config.DeviceConfigActivity;
import com.dspread.pos.ui.setting.device_config.DeviceConfigItem;
import com.dspread.pos.ui.setting.device_selection.DeviceSelectionActivity;
import com.dspread.pos.utils.DevUtils;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos.utils.USBClass;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.FragmentConnectionSettingsBinding;

import java.util.ArrayList;

import com.dspread.pos.common.base.BaseFragmentWithViewCache;

import me.goldze.mvvmhabit.utils.SPUtils;

import android.util.Log;

public class ConnectionSettingsFragment extends BaseFragmentWithViewCache<FragmentConnectionSettingsBinding, ConnectionSettingsViewModel> implements TitleProviderListener {
    private final int REQUEST_CODE_CURRENCY = 1000;
    private final int REQUEST_TRANSACTION_TYPE = 1001;
    private final int REQUEST_CARD_MODE = 1002;

    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_connection_settings;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public ConnectionSettingsViewModel initViewModel() {
        return new ViewModelProvider(this).get(ConnectionSettingsViewModel.class);
    }

    @Override
    public void initData() {
        super.initData();
        // Setup event listeners (lightweight operation, execute immediately)
        setupEventListeners();
        initAppVersion();

        // Delay execution of potentially time-consuming operations
        new Handler().postDelayed(() -> {
            if (getActivity() != null && !getActivity().isFinishing()) {
                if (DeviceUtils.isSmartDevices()) {
                    selectUart();
                }
            }
        }, 100);
    }

    private void initAppVersion() {
        String versionName = DevUtils.getVersionName(getContext());
        binding.tvAppVersion.setText("APP Version: " + versionName);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadSettings();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        // Device selection event
        viewModel.selectBluetoothEvent.observe(this, v -> {
            POSManager.getInstance().close();
            navigateToDeviceSelection();
        });

        viewModel.selectUartEvent.observe(this, v -> {
            POSManager.getInstance().close();
            selectUart();
        });


        viewModel.selectUsbEvent.observe(this, v -> {
            POSManager.getInstance().close();
            showUsbDeviceDialog();
        });

        // Transaction type click event
        viewModel.transactionTypeClickEvent.observe(this, v -> {
            Intent intent = new Intent(getActivity(), DeviceConfigActivity.class);
            intent.putExtra(DeviceConfigActivity.EXTRA_LIST_TYPE,
                    DeviceConfigActivity.TYPE_TRANSACTION);
            startActivityForResult(intent, REQUEST_TRANSACTION_TYPE);
        });

        // Card mode click event
        viewModel.cardModeClickEvent.observe(this, v -> {
            Intent intent = new Intent(getActivity(), DeviceConfigActivity.class);
            intent.putExtra(DeviceConfigActivity.EXTRA_LIST_TYPE,
                    DeviceConfigActivity.TYPE_CARD_MODE);
            startActivityForResult(intent, REQUEST_CARD_MODE);
        });

        // Currency code click event
        viewModel.currencyCodeClickEvent.observe(this, v -> {
//            showCurrencyCodeDialog();
            Intent intent = new Intent(getActivity(), DeviceConfigActivity.class);
            intent.putExtra(DeviceConfigActivity.EXTRA_LIST_TYPE,
                    DeviceConfigActivity.TYPE_CURRENCY);
            startActivityForResult(intent, REQUEST_CODE_CURRENCY);
        });
    }

    private void selectUart() {
        hideAllView();
        viewModel.isShowUartImageView.set(true);
        viewModel.isShowUartTextView.set(true);
        viewModel.deviceConnected.set(true);
        SPUtils.getInstance().put("deviceAddress", "");
        SPUtils.getInstance().put("bluetoothName", "");
        SPUtils.getInstance().put("bluetoothAddress", "");
        SPUtils.getInstance().put("isSelectUartSuccess", true);
        SPUtils.getInstance().put("isSelectUsbSuccess", false);
    }

    /**
     * Navigate to device selection screen
     */
    private void navigateToDeviceSelection() {
        Intent intent = new Intent(getActivity(), DeviceSelectionActivity.class);
        startActivityForResult(intent, DeviceSelectionActivity.REQUEST_CODE_SELECT_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            TRACE.d("onActivityResult  above");
            if (requestCode == DeviceSelectionActivity.REQUEST_CODE_SELECT_DEVICE) {
                // Get device name
                String bluetoothName = SPUtils.getInstance().getString("bluetoothName");
                String bluetoothAddress = SPUtils.getInstance().getString("bluetoothAddress");
                boolean isBluetoothStates = data.getBooleanExtra("isBluetoothStates", false);
                // Update UI
                if (isBluetoothStates || (!TextUtils.isEmpty(bluetoothName)) && !TextUtils.isEmpty(bluetoothAddress)) {
                    hideAllView();
                    showBlutetoothSelectView(bluetoothAddress);
                } else {
                    hideAllView();
                }
            } else if (requestCode == REQUEST_CODE_CURRENCY) {
                String currencyName = data.getStringExtra("currency_name");
                viewModel.currencyCode.set(currencyName);
                TRACE.i("currency code = " + currencyName);
            } else if (requestCode == REQUEST_TRANSACTION_TYPE) {
                String transactionType = data.getStringExtra("transaction_type");
                viewModel.transactionType.set(transactionType);
                TRACE.i("transactionType = " + transactionType);
            } else if (requestCode == REQUEST_CARD_MODE) {
                String cardMode = data.getStringExtra("card_mode");
                viewModel.cardMode.set(cardMode);
                TRACE.i("cardMode = " + cardMode);
            } else {
                viewModel.loadSettings();
            }
        }
    }

    private void showBlutetoothSelectView(String bluetoothAddress) {
        viewModel.isShowBluetoothImageView.set(true);
        viewModel.isShowBluetoothTextView.set(true);
        viewModel.deviceConnected.set(true);
        SPUtils.getInstance().put("deviceAddress", bluetoothAddress);
    }

    private void hideAllView() {
        viewModel.isShowBluetoothImageView.set(false);
        viewModel.isShowBluetoothTextView.set(false);
        viewModel.isShowUartImageView.set(false);
        viewModel.isShowUartTextView.set(false);
        viewModel.isShowUSBImageView.set(false);
        viewModel.isShowUsbTextView.set(false);
    }

    @Override
    public String getTitle() {
        return "Settings";
    }


    // Thread pool for executing asynchronous operations
    private static final java.util.concurrent.ExecutorService asyncExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();

    private void showUsbDeviceDialog() {
        USBClass usb = new USBClass();
        usb.setUsbPermissionListener(new USBClass.UsbPermissionListener() {
            @Override
            public void onPermissionGranted(UsbDevice device) {
                // Get USB device list in sub-thread
                asyncExecutor.execute(() -> {
                    ArrayList<String> deviceList = usb.GetUSBDevices(getActivity());
                    // Update UI in main thread, using main thread's Looper
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            openUsbDeviceDialog(deviceList);
                        }
                    });
                });
            }

            @Override
            public void onPermissionDenied(UsbDevice device) {
                Toast.makeText(getActivity(), "No Permission", Toast.LENGTH_SHORT).show();
            }
        });

        // Get USB device list in sub-thread
        asyncExecutor.execute(() -> {
            ArrayList<String> deviceList = usb.GetUSBDevices(getActivity());
            // Update UI in main thread, using main thread's Looper
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() != null && !getActivity().isFinishing()) {
                    if (deviceList != null) {
                        openUsbDeviceDialog(deviceList);
                    }
                }
            });
        });
    }

    private void openUsbDeviceDialog(ArrayList<String> deviceList) {
        final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);
        if (items.length == 1) {
            String selectedDevice = (String) items[0];
            SPUtils.getInstance().put("deviceAddress", selectedDevice);
            SPUtils.getInstance().put("bluetoothName", "");
            SPUtils.getInstance().put("bluetoothAddress", "");
            SPUtils.getInstance().put("isSelectUsbSuccess", true);
            SPUtils.getInstance().put("isSelectUartSuccess", false);
            hideAllView();
            viewModel.isShowUSBImageView.set(true);
            viewModel.isShowUsbTextView.set(true);
            viewModel.deviceConnected.set(true);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Select a Reader");
            if (items.length == 0) {
                builder.setMessage(getActivity().getString(R.string.setting_disusb));
                builder.setPositiveButton(getActivity().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
            }
            builder.setSingleChoiceItems(items, -1, (dialog, item) -> {
                if (items.length > item) {
                    String selectedDevice = items[item].toString();
                    dialog.dismiss();
                    SPUtils.getInstance().put("deviceAddress", selectedDevice);
                    SPUtils.getInstance().put("isSelectUsbSuccess", true);

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);
            alertDialog.show();

            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(getResources().getColor(R.color.transaction_detail_text_red));
                positiveButton.setTextSize(16);
                positiveButton.setTypeface(null, Typeface.BOLD);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Clean up thread pool resources to avoid memory leaks
        try {
            if (asyncExecutor != null && !asyncExecutor.isShutdown()) {
                asyncExecutor.shutdownNow();
            }
        } catch (Exception e) {
            Log.e("ConnectionSettingsFragment", "关闭线程池失败: " + e.getMessage());
        }
    }

}
