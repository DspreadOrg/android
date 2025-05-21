package com.dspread.pos.ui.scan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos.ui.base.TitleProvider;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.FragmentScanBinding;

import me.goldze.mvvmhabit.utils.ToastUtils;
import me.tatarka.bindingcollectionadapter2.BR;

public class ScanFragment extends BaseFragment<FragmentScanBinding,ScanViewModel> implements TitleProvider {
    private ActivityResultLauncher<Intent> scanLauncher;
    private String pkg;
    private String cls;
    private boolean canshow = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册 ActivityResultLauncher
        scanLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String scanData = result.getData().getStringExtra("data");
                    viewModel.onScanResult(scanData);
                }
            }
        );
    }

    @Override
    public void initViewObservable() {
        // 观察扫描请求
        viewModel.startScanEvent.observe(this, v -> {

            if (DeviceUtils.isAppInstalled(getContext(), DeviceUtils.UART_AIDL_SERVICE_APP_PACKAGE_NAME)) {
                //D30MstartScan();
                pkg = "com.dspread.sdkservice";
                cls = "com.dspread.sdkservice.base.scan.ScanActivity";
            } else {
                if (!canshow) {
                    return;
                }
                canshow = false;
                showTimer.start();
                pkg = "com.dspread.components.scan.service";
                cls = "com.dspread.components.scan.service.ScanActivity";
            }
            Intent intent = new Intent();
            ComponentName comp = new ComponentName(pkg, cls);
            try {
                intent.putExtra("amount", "CHARGE ￥1");
                intent.setComponent(comp);
                scanLauncher.launch(intent);
            } catch (ActivityNotFoundException e) {
                Log.w("e", "e==" + e);
                viewModel.onScanResult(getString(R.string.scan_toast));
                ToastUtils.showShort(getString(R.string.scan_toast));
            }
        });
    }
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_scan;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public String getTitle() {
        return "Scan";
    }

    private CountDownTimer showTimer = new CountDownTimer(800, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            canshow = true;
        }

    };
}

