package com.dspread.pos.ui.setting.connection_settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.ui.base.TitleProvider;
import com.dspread.pos.ui.setting.device_config.DeviceConfigActivity;
import com.dspread.pos.ui.setting.device_selection.DeviceSelectionActivity;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.FragmentConnectionSettingsBinding;

import me.goldze.mvvmhabit.base.BaseFragment;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class ConnectionSettingsFragment extends BaseFragment<FragmentConnectionSettingsBinding, ConnectionSettingsViewModel> implements TitleProvider {
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

        // Setup event listeners
        setupEventListeners();
    }

    /**
     * Setup event listeners
     */
    private void setupEventListeners() {
        // Device selection event
        viewModel.selectDeviceEvent.observe(this, v -> {
            navigateToDeviceSelection();
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
        if(resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == DeviceSelectionActivity.REQUEST_CODE_SELECT_DEVICE) {
                // Get device name
                String deviceName = data.getStringExtra(DeviceSelectionActivity.EXTRA_DEVICE_NAME);

                // Get connection type
                String connectionType = data.getStringExtra(DeviceSelectionActivity.EXTRA_CONNECTION_TYPE);

                // pdate device name
                if (deviceName != null) {
                    viewModel.updateDeviceName(connectionType + "(" + deviceName + ")");
                } else {
                    viewModel.updateDeviceName(connectionType);
                }

                // Update device connection status
                if (connectionType != null) {
                    POS_TYPE posType = POS_TYPE.valueOf(connectionType);
                    viewModel.deviceConnected.set(posType != null);
                    viewModel.saveSettings();
                }

                ToastUtils.showShort("Selected Devices " + deviceName);
            } else if (requestCode == REQUEST_CODE_CURRENCY) {
                String currencyName = data.getStringExtra("currency_name");
                viewModel.currencyCode.set(currencyName);
                TRACE.i("currency code = " + currencyName);
            } else if (requestCode == REQUEST_TRANSACTION_TYPE) {
                String transactionType = data.getStringExtra("transaction_type");
                viewModel.transactionType.set(transactionType);
                TRACE.i("transactionType = " + transactionType);
            }else if(requestCode == REQUEST_CARD_MODE){
                String cardMode = data.getStringExtra("card_mode");
                viewModel.cardMode.set(cardMode);
                TRACE.i("cardMode = " + cardMode);
            }else {
                viewModel.loadSettings();
            }
        }
    }

    @Override
    public String getTitle() {
        return "Settings";
    }
}
