package com.dspread.pos.ui.transaction.filter;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

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
        binding.rgDateFilter.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.rbToday:
                    // 选中了“Today”按钮，执行相关逻辑
                    break;
                case R.id.rb3days:
                    // 选中了“3days”按钮，执行相关逻辑
                    break;
                case R.id.rbAll:
                    // 选中了“All”按钮，执行相关逻辑
                    break;
                default:
                    // 其他情况，可根据需要处理
                    break;
            }
        });
    }
}
