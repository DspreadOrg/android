package com.dspread.pos.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class HomeViewModel extends BaseAppViewModel {
    public ObservableField<String> amount = new ObservableField<>("¥0.00");
    public SingleLiveEvent<Long> paymentStartEvent = new SingleLiveEvent<>();
    
    public StringBuilder amountBuilder = new StringBuilder();
    private static final int MAX_DIGITS = 12; // 最大金额位数
    
    public HomeViewModel(@NonNull Application application) {
        super(application);
    }
    
    // 更新金额显示
    private void updateAmountDisplay() {
        if (amountBuilder.length() == 0) {
            amount.set("¥0.00");
            return;
        }
        
        String amountStr = amountBuilder.toString();
        // 转换为带两位小数的金额显示
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

    // 确认按钮命令
    public BindingCommand onConfirmClickCommand = new BindingCommand(() -> {
        if (amountBuilder.length() == 0) {
            ToastUtils.showShort(R.string.set_amount);
            return;
        }

        try {
            long amountInCents = Long.parseLong(amountBuilder.toString());
            if (amountInCents > 0) {
                paymentStartEvent.postValue(amountInCents);
            } else {
                ToastUtils.showShort(R.string.set_amount);
            }
        } catch (NumberFormatException e) {
            ToastUtils.showShort(R.string.set_amount);
        }
    });
}