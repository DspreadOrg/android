package com.dspread.pos.ui.payment;

import android.app.Application;
import android.content.Intent;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.utils.TRACE;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PaymentStatusViewModel extends BaseAppViewModel {
    private String terAmount;
    private String maskedPAN;
    private String terminalTime;

    private static final long CLICK_INTERVAL = 500;
    private volatile long lastClickTime = 0;
    private volatile boolean isNavigating = false;

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
        if (isClickValid() && !isNavigating) {
            isNavigating = true;
            try {
                finish();
            } catch (Exception e) {
                isNavigating = false;
            }
        }
    });
    public BindingCommand sendReceiptCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            if (!isClickValid() || isNavigating) {
                return;
            }
            isNavigating = true;
            try {
                Intent intent = new Intent(context, PrintTicketActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_TER_AMOUNT, terAmount != null ? terAmount : "");
                intent.putExtra(EXTRA_MASKED_PAN, maskedPAN != null ? maskedPAN : "");
                intent.putExtra(EXTRA_TERMINAL_TIME, terminalTime != null ? terminalTime : "");
                context.startActivity(intent);
                finish();
            } catch (Exception e) {
                try {
                    finish();
                } catch (Exception ex) {
                    TRACE.e("Failed to finish activity: " + ex.getMessage());
                } finally {
                    isNavigating = false;
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
