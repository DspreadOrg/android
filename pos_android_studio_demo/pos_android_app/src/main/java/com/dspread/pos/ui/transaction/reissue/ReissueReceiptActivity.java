package com.dspread.pos.ui.transaction.reissue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.ui.transaction.Transaction;
import com.dspread.pos.ui.transaction.reissue.TransactionReissueReceipViewModel;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityReissueReceiptBinding;
import com.dspread.pos_android_app.generated.callback.OnClickListener;

import java.math.BigDecimal;

import me.goldze.mvvmhabit.base.BaseActivity;

public class ReissueReceiptActivity extends BaseActivity<ActivityReissueReceiptBinding, TransactionReissueReceipViewModel> {
    private Transaction transaction;

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
        transaction = (Transaction) getIntent().getSerializableExtra("transaction");

        String amount = getIntent().getStringExtra("amount");

        String mAmount = DeviceUtils.convertAmountToCents(amount);
        TRACE.d("mAmount:" + mAmount);
        binding.amountText.setText("$" + mAmount);
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReissueReceiptActivity.this, PrintTicketActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("terAmount", transaction.getAmount()+"");
                intent.putExtra("maskedPAN", transaction.getMaskPan());
                intent.putExtra("terminalTime", transaction.getRequestDate());
                ReissueReceiptActivity.this.startActivity(intent);

            }
        });


        binding.doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
