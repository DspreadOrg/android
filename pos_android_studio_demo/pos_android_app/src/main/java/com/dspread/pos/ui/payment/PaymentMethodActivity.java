package com.dspread.pos.ui.payment;

import android.content.Intent;
import android.os.Bundle;

import com.dspread.pos.ui.scan.ScanCodeActivity;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentMetholdBinding;

import me.goldze.mvvmhabit.base.BaseActivity;

public class PaymentMethodActivity extends BaseActivity<ActivityPaymentMetholdBinding, PaymentMethodViewModel> {
    private String amount;
    private String deviceAddress;
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
        viewModel.getSelectedPaymentMethod().observe(this, methodIndex -> {
            if (methodIndex != null) {
                handlePaymentMethodSelection(methodIndex);
            }
        });
        viewModel.setTotalAmount("$"+ DeviceUtils.convertAmountToCents(amount));
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
        Intent intent = new Intent(this, ScanCodeActivity.class);
        startActivity(intent);
        TRACE.d("PayMethodActivity startScanCodePayment");
    }

    private void startGeneratePayment() {
        // 启动生成支付码
        Intent intent = new Intent(this, PaymentGenerateActivity.class);
        startActivity(intent);
        finish();
        TRACE.d("PayMethodActivity startGeneratePayment");
    }

    private void navigateCashPayment() {
        navigateToCardPayment();
        TRACE.d("PayMethodActivity startCashPayment");
    }

}
