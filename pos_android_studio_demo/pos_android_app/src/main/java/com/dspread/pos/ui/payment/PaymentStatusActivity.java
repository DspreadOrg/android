package com.dspread.pos.ui.payment;


import android.os.Bundle;

import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentBinding;

import me.goldze.mvvmhabit.base.BaseActivity;

public class PaymentStatusActivity extends BaseActivity<ActivityPaymentBinding, PaymentStatusViewModel> {
    private String amount;

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_paymentstatus;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    public void initData() {
        viewModel.setmContext(this);
        amount = getIntent().getStringExtra("amount");
        if (amount != null && !"".equalsIgnoreCase(amount)) {
            viewModel.displayAmount(DeviceUtils.convertAmountToCents(amount));
            viewModel.setTransactionSuccess();
        } else {
            viewModel.setTransactionFailed();
        }
    }
}