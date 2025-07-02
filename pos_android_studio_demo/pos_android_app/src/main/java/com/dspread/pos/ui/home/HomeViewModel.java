package com.dspread.pos.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class HomeViewModel extends BaseAppViewModel {
    public ObservableField<String> amount = new ObservableField<>("¥0.00");
    public SingleLiveEvent<Long> paymentStartEvent = new SingleLiveEvent<>();
    
    public StringBuilder amountBuilder = new StringBuilder();
    private static final int MAX_DIGITS = 12; // Maximum amount digits
    
    public HomeViewModel(@NonNull Application application) {
        super(application);
    }
    
    // Update amount display
    private void updateAmountDisplay() {
        if (amountBuilder.length() == 0) {
            amount.set("¥0.00");
            return;
        }
        
        String amountStr = amountBuilder.toString();
        // Convert to display an amount with two decimal places
        if (amountStr.length() == 1) {
            amount.set(String.format("¥0.0%s", amountStr));
        } else if (amountStr.length() == 2) {
            amount.set(String.format("¥0.%s", amountStr));
        } else {
            String intPart = amountStr.substring(0, amountStr.length() - 2);
            String decimalPart = amountStr.substring(amountStr.length() - 2);
            amount.set(String.format("¥%s.%s", intPart, decimalPart));
        }
    }

    public void clearAmount() {
        amountBuilder.setLength(0);
        amount.set("¥0.00");
    }

    public void onNumberClick(String number){
        if (amountBuilder.length() >= MAX_DIGITS) {
            return;
        }

        if (amountBuilder.length() == 0 && number.equals("0")) {
            return;
        }

        amountBuilder.append(number);
        updateAmountDisplay();
    }

    public void onClearClickCommand(){
        TRACE.i("delete the amount");
        if(amountBuilder.length() > 0){
            amountBuilder.delete(amountBuilder.length() - 1, amountBuilder.length());
        }
        updateAmountDisplay();
    }

    // Confirm button command
    public BindingCommand onConfirmClickCommand = new BindingCommand(() -> {
        if(amountBuilder.length() == 0){
            if(SPUtils.getInstance().getString("transactionType") != null && !"".equals(SPUtils.getInstance().getString("transactionType"))){
                String transactionTypeString = SPUtils.getInstance().getString("transactionType");
                if(transactionTypeString.equals("CHANGE_PIN") || transactionTypeString.equals("BALANCE") ||transactionTypeString.equals("BALANCE_UPDATE") ){
                    paymentStartEvent.postValue(0l);
                }else {
                    ToastUtils.showShort(R.string.set_amount);
                }
            }else {
                ToastUtils.showShort(R.string.set_amount);
            }
        }else {
            try {
                long amountInCents = Long.parseLong(amountBuilder.toString());
                paymentStartEvent.postValue(amountInCents);
            } catch (NumberFormatException e) {
                ToastUtils.showShort(R.string.set_amount);
            }
        }
    });
}