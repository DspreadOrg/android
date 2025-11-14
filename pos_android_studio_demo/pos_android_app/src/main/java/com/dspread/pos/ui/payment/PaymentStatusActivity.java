package com.dspread.pos.ui.payment;


import android.os.Build;
import android.os.Bundle;

import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentBinding;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.base.BaseActivity;

public class PaymentStatusActivity extends BaseActivity<ActivityPaymentBinding, PaymentStatusViewModel> {
    private String amount;
    private String maskedPAN;
    private String terminalTime;
    private String errorMsg;

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
        maskedPAN = getIntent().getStringExtra("maskedPAN");
        terminalTime = getIntent().getStringExtra("terminalTime");
        errorMsg = getIntent().getStringExtra("errorMsg");
        if ("D70".equals(Build.MODEL)) {
            viewModel.isD70DisplayScreen.set(true);
        } else {
            viewModel.isD70DisplayScreen.set(false);
        }
        if (amount != null && !"".equalsIgnoreCase(amount)) {
            viewModel.displayAmount(DeviceUtils.convertAmountToCents(amount));
            viewModel.setTransactionSuccess();
            Map<String, String> map = new HashMap();
            map.put("terAmount", DeviceUtils.convertAmountToCents(amount));
            map.put("maskedPAN", maskedPAN);
            map.put("terminalTime", terminalTime);
            viewModel.sendTranReceipt(map);
        } else {
            String errorMsgs = "";
            if (errorMsg != null && !"".equalsIgnoreCase(errorMsg)) {
                errorMsgs = errorMsg;
            }
            viewModel.setTransactionFailed(errorMsgs);
        }
        if (DeviceUtils.isPrinterDevices()) {
            viewModel.isShouwPrinting.set(true);
        } else {
            viewModel.isShouwPrinting.set(false);
        }
    }
}