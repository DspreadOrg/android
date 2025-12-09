package com.dspread.pos.ui.payment;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.utils.TRACE;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PaymentStatusViewModel extends BaseAppViewModel {
    private Context mContext;
    private Bitmap receiptBitmap;
    private String terAmount;
    private String maskedPAN;
    private String terminalTime;

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public ObservableBoolean isSuccess = new ObservableBoolean(false);
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableBoolean isPrinting = new ObservableBoolean(false);
    public ObservableBoolean isShouwPrinting = new ObservableBoolean(false);
    public ObservableBoolean isD70DisplayScreen = new ObservableBoolean(false);
    public ObservableField<String> sendError = new ObservableField<>("");

    public PaymentStatusViewModel(@NonNull Application application) {
        super(application);
    }

    public void setTransactionFailed(String errorMsg) {
        isSuccess.set(false);
        if (errorMsg != null && !"".equalsIgnoreCase(errorMsg)) {
            sendError.set(errorMsg);
        }
    }

    public void setTransactionSuccess() {
        isSuccess.set(true);
    }


    public void displayAmount(String newAmount) {
        TRACE.d("displayAmount:" + newAmount);
        amount.set("$ " + newAmount);
    }

    public void sendTranReceipt(Map<String, String> map) {
        terAmount = map.get("terAmount");
        maskedPAN = map.get("maskedPAN");
        terminalTime = map.get("terminalTime");
    }

    public BindingCommand continueTxnsCommand = new BindingCommand(() -> finish());
    public BindingCommand sendReceiptCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Intent intent = new Intent(context, PrintTicketActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("terAmount", terAmount);
            intent.putExtra("maskedPAN", maskedPAN);
            intent.putExtra("terminalTime", terminalTime);
            context.startActivity(intent);
            finish();
        }
    });

}
