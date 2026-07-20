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
