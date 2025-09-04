package com.dspread.pos.ui.transaction.filter;

import android.os.Bundle;
import android.view.View;

import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityTransactionFilterBinding;

import me.goldze.mvvmhabit.base.BaseActivity;


public class TransactionFilterActivity extends BaseActivity<ActivityTransactionFilterBinding, TransactionFilterViewModel> {
    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_transaction_filter;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        binding.setVariable(BR.viewModel, viewModel);
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
