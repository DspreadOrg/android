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
    private String terAmount;
    private String maskedPAN;
    private String terminalTime;

    // Anti-shake protection for rapid clicks
    private static final long CLICK_INTERVAL = 500; // Minimum interval between clicks (milliseconds)
    private long lastClickTime = 0;
    private boolean isNavigating = false;

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

    // Intent extras keys
    private static final String EXTRA_TER_AMOUNT = "terAmount";
    private static final String EXTRA_MASKED_PAN = "maskedPAN";
    private static final String EXTRA_TERMINAL_TIME = "terminalTime";

    public BindingCommand continueTxnsCommand = new BindingCommand(() -> {
        // Anti-shake for continue button
        if (isClickValid()) {
            finish();
        }
    });

    public BindingCommand sendReceiptCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (!isClickValid()) {
                TRACE.d("Ignored rapid click on send receipt button");
                return;
            }
            try {
                Intent intent = new Intent(context, PrintTicketActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // Use default empty string if value is null to prevent NullPointerException
                intent.putExtra(EXTRA_TER_AMOUNT, terAmount != null ? terAmount : "");
                intent.putExtra(EXTRA_MASKED_PAN, maskedPAN != null ? maskedPAN : "");
                intent.putExtra(EXTRA_TERMINAL_TIME, terminalTime != null ? terminalTime : "");
                context.startActivity(intent);
                finish();
            } catch (Exception e) {
                TRACE.e("Failed to start PrintTicketActivity: " + e.getMessage());
                try {
                    finish();
                } catch (Exception ex) {
                    TRACE.e("Failed to finish activity: " + ex.getMessage());
                }
            }
        }
    });

    /**
     * Check if the click is valid (anti-shake)
     *
     * @return true if the click interval is sufficient, false otherwise
     */
    private boolean isClickValid() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_INTERVAL) {
            return false;
        }
        lastClickTime = currentTime;
        return true;
    }

}
