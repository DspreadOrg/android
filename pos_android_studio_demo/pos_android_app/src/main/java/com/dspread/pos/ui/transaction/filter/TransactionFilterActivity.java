package com.dspread.pos.ui.transaction.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.dspread.pos.utils.DeviceUtils;
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

    String filter = "1";
    private static final int FILTER_RECEIVE = 101;

    @Override
    public void initData() {
        super.initData();
        binding.setVariable(BR.viewModel, viewModel);
        String deviceModel = DeviceUtils.getPhoneModel();
        if("D70".equals(deviceModel)){
            viewModel.isD70.set(true);
        }else{
            viewModel.isD70.set(false);
        }
        // 只给返回按钮添加点击事件，而不是整个toolbar
        if (binding.toolbar.getNavigationIcon() != null) {
            binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        binding.rgDateFilter.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.rbToday:
                    filter = "1";
                    break;
                case R.id.rb3days:
                    filter = "3";
                    break;
                case R.id.rbAll:
                    filter = "all";
                    break;
                default:
                    filter = "all";
                    break;
            }
        });
    }

    @Override
    public void initViewObservable() {
        viewModel.doneEvent.observe(this, unused -> {
            Intent intent = new Intent();
            intent.putExtra("filter", filter);
            setResult(FILTER_RECEIVE, intent);
            finish();
        });
    }

}
