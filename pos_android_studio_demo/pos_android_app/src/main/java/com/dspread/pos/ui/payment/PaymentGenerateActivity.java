package com.dspread.pos.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dspread.pos.ui.scan.ScanCodeActivity;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentGenerateBinding;
import com.dspread.pos_android_app.generated.callback.OnClickListener;

import me.goldze.mvvmhabit.base.BaseActivity;

public class PaymentGenerateActivity extends BaseActivity<ActivityPaymentGenerateBinding, PaymentGenerateViewModel> {
    private String amount;

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_payment_generate;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        binding.setVariable(BR.viewModel, viewModel);
        amount = getIntent().getStringExtra("amount");
        amount = "$" + DeviceUtils.convertAmountToCents(amount);
        viewModel.setPaymentAmount(amount);
        setupObservers();
        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setupObservers() {
        viewModel.getQrCodeBitmap().observe(this, bitmap -> {
            if (bitmap != null) {
                binding.qrCodeImageView.setImageBitmap(bitmap);
            } else {
                binding.qrCodeImageView.setImageResource(R.drawable.ic_printer);
            }
        });
    }

}
