package com.dspread.pos.ui.transaction.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dspread.pos.ui.transaction.Transaction;
import com.dspread.pos.ui.transaction.reissue.ReissueReceiptActivity;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityTransactionDetailsBinding;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;

public class TransactionDetailActivity extends BaseActivity<ActivityTransactionDetailsBinding, TransactionDetailsViewModel> {

    private Transaction transaction;

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_transaction_details;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        binding.setVariable(BR.viewModel, viewModel);
        transaction = (Transaction) getIntent().getSerializableExtra("transaction");
        binding.transactionDate.setText(transaction.getRequestDate());
        binding.tvAmount.setText(transaction.getAmount() + "");
        binding.tvPayType.setText(transaction.getPayType());
        binding.tvDeviceId.setText(SPUtils.getInstance().getString("posID", ""));
        binding.tvCardNumber.setText(transaction.getMaskPan());


        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.tvReissueReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TransactionDetailActivity.this, ReissueReceiptActivity.class);
                intent.putExtra("amount", transaction.getAmount() + "");
                startActivity(intent);
            }
        });
    }
}
