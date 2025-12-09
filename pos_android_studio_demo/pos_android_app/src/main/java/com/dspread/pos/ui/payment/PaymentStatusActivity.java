package com.dspread.pos.ui.payment;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentstatusBinding;


import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.base.BaseActivity;

public class PaymentStatusActivity extends BaseActivity<ActivityPaymentstatusBinding, PaymentStatusViewModel> {
    private String amount;
    private String maskedPAN;
    private String terminalTime;
    private String errorMsg;
    // 建议在类顶部定义常量
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_MASKED_PAN = "maskedPAN";
    private static final String KEY_TERMINAL_TIME = "terminalTime";
    private static final String KEY_ERROR_MSG = "errorMsg";
    private static final String MODEL_D70 = "D70";

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
        Intent intent = getIntent();
        amount = getStringExtraSafely(intent, KEY_AMOUNT);
        maskedPAN = getStringExtraSafely(intent, KEY_MASKED_PAN);
        terminalTime = getStringExtraSafely(intent, KEY_TERMINAL_TIME);
        errorMsg = getStringExtraSafely(intent, KEY_ERROR_MSG);


        viewModel.isD70DisplayScreen.set(MODEL_D70.equals(Build.MODEL));


        if (isValidAmount(amount)) {
            handleTransactionSuccess();
        } else {
            handleTransactionFailure();
        }


        viewModel.isShouwPrinting.set(DeviceUtils.isPrinterDevices());
    }

    /**
     * 安全获取String类型的Extra
     */
    private String getStringExtraSafely(Intent intent, String key) {
        if (intent != null && intent.hasExtra(key)) {
            return intent.getStringExtra(key);
        }
        return "";
    }

    /**
     * 检查金额是否有效
     */
    private boolean isValidAmount(String amount) {
        return !TextUtils.isEmpty(amount);
    }

    /**
     * 处理交易成功逻辑
     */
    private void handleTransactionSuccess() {
        String amountInCents = DeviceUtils.convertAmountToCents(amount);

        viewModel.displayAmount(amountInCents);
        viewModel.setTransactionSuccess();

        Map<String, String> receiptData = createReceiptData(amountInCents, maskedPAN, terminalTime);
        viewModel.sendTranReceipt(receiptData);
    }

    /**
     * 创建收据数据
     */
    private Map<String, String> createReceiptData(String amountInCents, String maskedPAN, String terminalTime) {
        Map<String, String> map = new HashMap<>();
        map.put("terAmount", amountInCents);
        map.put("maskedPAN", maskedPAN != null ? maskedPAN : "");
        map.put("terminalTime", terminalTime != null ? terminalTime : "");
        return map;
    }

    /**
     * 处理交易失败逻辑
     */
    private void handleTransactionFailure() {
        String errorMessage = !TextUtils.isEmpty(errorMsg) ? errorMsg : "";
        viewModel.setTransactionFailed(errorMessage);
    }
}