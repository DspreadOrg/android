package com.dspread.pos.ui.transaction.reissue;

import android.os.Bundle;
import android.view.View;

import com.dspread.pos.ui.transaction.reissue.TransactionReissueReceipViewModel;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityReissueReceiptBinding;

import me.goldze.mvvmhabit.base.BaseActivity;

public class ReissueReceiptActivity extends BaseActivity<ActivityReissueReceiptBinding, TransactionReissueReceipViewModel> {
    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_reissue_receipt;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        binding.setVariable(BR.viewModel, viewModel);

        String amount = getIntent().getStringExtra("amount");
        TRACE.d("amount:" + amount);
        binding.amountText.setText("$" + amount);
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
