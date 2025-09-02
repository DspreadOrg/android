package com.dspread.pos.ui.payment;

import android.content.Intent;
import android.os.Bundle;

import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentMetholdBinding;

import me.goldze.mvvmhabit.base.BaseActivity;

public class PaymentMethodActivity extends BaseActivity<ActivityPaymentMetholdBinding, PaymentMethodViewModel> {
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
        binding.setVariable(BR.viewModel, viewModel);
        viewModel.getSelectedPaymentMethod().observe(this, methodIndex -> {
            if (methodIndex != null) {
                handlePaymentMethodSelection(methodIndex);
            }
        });
        viewModel.setTotalAmount("$886.00");
    }

    private void handlePaymentMethodSelection(int methodIndex) {
        switch (methodIndex) {
            case 0:
                startCardPayment();
                break;
            case 1:
                startScanCodePayment();
                break;
            case 2:
                startGeneratePayment();
                break;
            case 3:
                startCashPayment();
                break;
        }
    }

    private void startCardPayment() {
        // 启动银行卡支付
       /* Intent intent = new Intent(this, CardPaymentActivity.class);
        startActivity(intent);*/
        TRACE.d("PayMethodActivity startCardPayment");
    }

    private void startScanCodePayment() {
        // 启动扫码支付
       /* Intent intent = new Intent(this, ScanCodeActivity.class);
        startActivity(intent);*/
        TRACE.d("PayMethodActivity startScanCodePayment");
    }

    private void startGeneratePayment() {
        // 启动生成支付码
        Intent intent = new Intent(this, PaymentGenerateActivity.class);
        startActivity(intent);
        finish();
        TRACE.d("PayMethodActivity startGeneratePayment");
    }

    private void startCashPayment() {
        // 处理现金支付
      /*  Intent intent = new Intent(this, CashPaymentActivity.class);
        startActivity(intent);*/
        TRACE.d("PayMethodActivity startCashPayment");
    }

}
