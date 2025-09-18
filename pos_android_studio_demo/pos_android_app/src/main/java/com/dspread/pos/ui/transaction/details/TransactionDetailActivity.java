package com.dspread.pos.ui.transaction.details;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dspread.pos.ui.transaction.Transaction;
import com.dspread.pos.ui.transaction.reissue.ReissueReceiptActivity;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityTransactionDetailsBinding;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

        initCardImage();
        binding.setVariable(BR.viewModel, viewModel);
        transaction = (Transaction) getIntent().getSerializableExtra("transaction");
        binding.transactionDate.setText(transaction.getTransactionDate());
        String amount = DeviceUtils.convertAmountToCents(new BigDecimal(transaction.getAmount()).toPlainString());
        binding.tvAmount.setText("$" + amount);
        binding.tvPayType.setText(transaction.getPayType());
        binding.tvDeviceId.setText(SPUtils.getInstance().getString("posID", ""));
        binding.tvCardNumber.setText(transaction.getMaskPan().replaceAll("[fFXx]", "*"));

        if (transaction.getCardOrg().equalsIgnoreCase("visa")) {
            binding.ivCardOrganization.setImageResource(mipmapImageIds.get(0));
        }
        if (transaction.getCardOrg().equalsIgnoreCase("masterCard")) {
            binding.ivCardOrganization.setImageResource(mipmapImageIds.get(1));
        }
        if (transaction.getCardOrg().equalsIgnoreCase("amex")) {
            binding.ivCardOrganization.setImageResource(mipmapImageIds.get(2));
        }
        if (transaction.getCardOrg().equalsIgnoreCase("discover")) {
            binding.ivCardOrganization.setImageResource(mipmapImageIds.get(3));
        }
        if (transaction.getCardOrg().equalsIgnoreCase("jcb")) {
            binding.ivCardOrganization.setImageResource(mipmapImageIds.get(4));
        }
        if (transaction.getCardOrg().equalsIgnoreCase("UnionPay")) {
            binding.ivCardOrganization.setImageResource(mipmapImageIds.get(5));
        }


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
                intent.putExtra("transaction", (Serializable) transaction);
                startActivity(intent);
            }
        });
    }

    private List<Integer> mipmapImageIds = new ArrayList<>();

    private void initCardImage() {
        mipmapImageIds.add(R.mipmap.ic_visa);
        mipmapImageIds.add(R.mipmap.ic_master);
        mipmapImageIds.add(R.mipmap.ic_amex);
        mipmapImageIds.add(R.mipmap.ic_discover);
        mipmapImageIds.add(R.mipmap.ic_jcb);
        mipmapImageIds.add(R.mipmap.ic_unionpay);
    }
}
