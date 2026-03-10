package com.dspread.pos.ui.payment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;

import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentMetholdBinding;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class PaymentMethodActivity extends BaseActivity<ActivityPaymentMetholdBinding, PaymentMethodViewModel> {
    private String amount;
    private String deviceAddress;

    private String pkg;
    private String cls;
    private boolean canshow = true;
    private ActivityResultLauncher<Intent> scanLauncher;
    private int currentMethodIndex = 0; // 当前选中的支付方式索引
    private com.dspread.pos.view.PaymentMethodsLayout paymentMethodsLayout;

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_payment_methold;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        amount = getIntent().getStringExtra("amount");
        deviceAddress = getIntent().getStringExtra("deviceAddress");
        binding.setVariable(BR.viewModel, viewModel);
        binding.paymentMethodsLayout.setViewModel(viewModel);
        paymentMethodsLayout = binding.paymentMethodsLayout;
        // Initialize with first payment method selected
        paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
        viewModel.getSelectedPaymentMethod().observe(this, methodIndex -> {
            if (methodIndex != null) {
                handlePaymentMethodSelection(methodIndex);
            }
        });

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthPx = displayMetrics.widthPixels;
        int heightPx = displayMetrics.heightPixels;

        if (widthPx <= 320 && heightPx <= 240) {
            viewModel.isSmallScreen.set(true);
            viewModel.isNormalScreen.set(false);
        } else {
            viewModel.isNormalScreen.set(true);
            viewModel.isSmallScreen.set(false);
        }
        viewModel.setTotalAmount("$" + DeviceUtils.convertAmountToCents(amount));
        scanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String scanData = result.getData().getStringExtra("data");
                        scanData = amount;
                        gotoPaymentstatusActivity(scanData);
                        finish();
                    } else {
                        gotoPaymentstatusActivity("");
                        finish();
                    }
                }
        );
    }


    private void handlePaymentMethodSelection(int methodIndex) {
        switch (methodIndex) {
            case 0:
                navigateToCardPayment();
                break;
            case 1:
                startScanCodePayment();
                break;
            case 2:
                startGeneratePayment();
                break;
            case 3:
                navigateCashPayment();
                break;
        }
    }

    private void navigateToCardPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("deviceAddress", deviceAddress);
        startActivity(intent);
        finish();
    }

    private void startScanCodePayment() {
        // 启动扫码支付
       /* Intent intent = new Intent(this, ScanCodeActivity.class);
        intent.putExtra("amount", amount);
        startActivity(intent);*/
        TRACE.d("PayMethodActivity startScanCodePayment");
        initScanCode();
    }

    private void startGeneratePayment() {
        // 启动生成支付码
        Intent intent = new Intent(this, PaymentGenerateActivity.class);
        intent.putExtra("amount", amount);
        startActivity(intent);
        finish();
        TRACE.d("PayMethodActivity startGeneratePayment");
    }

    private void navigateCashPayment() {
        navigateToCardPayment();
        TRACE.d("PayMethodActivity startCashPayment");
    }

    private void initScanCode() {
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
        Intent intentScanCode = new Intent();
        ComponentName comp = new ComponentName(pkg, cls);
        try {
            intentScanCode.putExtra("amount", "CHARGE ￥1");
            intentScanCode.setComponent(comp);
            scanLauncher.launch(intentScanCode);
        } catch (ActivityNotFoundException e) {
            Log.w("e", "e==" + e);
            //viewModel.onScanResult(getString(R.string.scan_toast));
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

    private void gotoPaymentstatusActivity(String scanData) {
        Intent intent = new Intent(PaymentMethodActivity.this, PaymentStatusActivity.class);
        intent.putExtra("amount", scanData);
        startActivity(intent);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_UP) {
                finish();
            }
            return true;
        } else {
            if (action == KeyEvent.ACTION_UP) {
                TRACE.i("payment method on keydown = " + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        currentMethodIndex = (currentMethodIndex - 1 + 4) % 4;
                        TRACE.i("Selected payment method: " + currentMethodIndex);
                        // Update UI to show selected payment method
                        if (paymentMethodsLayout != null) {
                            paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        currentMethodIndex = (currentMethodIndex + 1) % 4;
                        TRACE.i("Selected payment method: " + currentMethodIndex);
                        // Update UI to show selected payment method
                        if (paymentMethodsLayout != null) {
                            paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
                        }
                        return true;
                    case KeyEvent.KEYCODE_ENTER:
                        handlePaymentMethodSelection(currentMethodIndex);
                        return true;
                }
                return false;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
