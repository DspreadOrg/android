package com.dspread.pos.ui.scan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityScanBinding;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class ScanCodeActivity extends BaseActivity<ActivityScanBinding, ScanViewModel> {
    private String pkg;
    private String cls;
    private boolean canshow = true;
    private ActivityResultLauncher<Intent> scanLauncher;

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_scan;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        scanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String scanData = result.getData().getStringExtra("data");
                        viewModel.onScanResult(scanData);
                    } else {
                        finish();
                    }
                }
        );


    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        if (DeviceUtils.isAppInstalled(getApplicationContext(), DeviceUtils.UART_AIDL_SERVICE_APP_PACKAGE_NAME)) {
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
