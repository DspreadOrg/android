package com.dspread.pos.ui.payment;

import android.app.Application;
import android.util.Log;

import com.dspread.pos.common.base.BaseAppViewModel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PaymentMethodViewModel extends BaseAppViewModel {

    public final ObservableField<String> totalAmount = new ObservableField<>("$88.00");
    // 选中的支付方式
    private final MutableLiveData<Integer> selectedPaymentMethod = new MutableLiveData<>();

    public ObservableField<Boolean> isNormalScreen = new ObservableField<>(true);
    public ObservableField<Boolean> isSmallScreen = new ObservableField<>(false);

    public PaymentMethodViewModel(@NonNull Application application) {
        super(application);
    }

    public BindingCommand closeButton = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Log.d("Payment", "close button");
            finish();
        }
    });


    // 支付方式点击事件
    public void onPaymentMethodSelected(int methodIndex) {
        selectedPaymentMethod.setValue(methodIndex);
        // 根据选择的支付方式执行相应操作
        switch (methodIndex) {
            case 0: // Card
                processCardPayment();
                break;
            case 1: // Scan Code
                processScanCodePayment();
                break;
            case 2: // Generate
                processGeneratePayment();
                break;
            case 3: // Cash
                processCashPayment();
                break;
        }
    }

    private void processCardPayment() {
        // 处理银行卡支付逻辑
        Log.d("Payment", "Card payment selected");
    }

    private void processScanCodePayment() {
        // 处理扫码支付逻辑
        Log.d("Payment", "Scan code payment selected");
    }

    private void processGeneratePayment() {
        // 处理生成支付码逻辑
        Log.d("Payment", "Generate payment code selected");
    }

    private void processCashPayment() {
        // 处理现金支付逻辑
        Log.d("Payment", "Cash payment selected");
    }

    // 获取选中的支付方式
    public LiveData<Integer> getSelectedPaymentMethod() {
        return selectedPaymentMethod;
    }

    // 设置总金额
    public void setTotalAmount(String amount) {
        totalAmount.set(amount);
    }
}
