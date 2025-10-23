package com.dspread.pos.ui.payment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;

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
        viewModel.setContext(this);
        binding.setVariable(BR.viewModel, viewModel);
        amount = getIntent().getStringExtra("amount");

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthPx = displayMetrics.widthPixels;
        int heightPx = displayMetrics.heightPixels;

        if (widthPx <= 320 && heightPx <= 240) {
            viewModel.isSmallScreen.set(true);
            viewModel.isNormalScreen.set(false);
            setConstraintHeightPercent(binding.qrCodeImageView,0.7f);
        } else {
            viewModel.isSmallScreen.set(false);
            viewModel.isNormalScreen.set(true);
            setConstraintHeightPercent(binding.qrCodeImageView,0.5f);
        }
        viewModel.setPaymentAmount("$" + DeviceUtils.convertAmountToCents(amount));
        setupObservers();
        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.paymentResultEvent.observe(this, flag -> {
            if (flag) {
                Intent intent = new Intent(PaymentGenerateActivity.this, PaymentStatusActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("amount", amount);
                startActivity(intent);
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



    private void setConstraintHeightPercent(View view, float percent) {
        if (view.getLayoutParams() instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            params.matchConstraintDefaultHeight = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
            params.matchConstraintPercentHeight = percent;
            view.setLayoutParams(params);
        }
    }

}
