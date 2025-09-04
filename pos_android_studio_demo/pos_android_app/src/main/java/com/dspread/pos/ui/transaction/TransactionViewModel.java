package com.dspread.pos.ui.transaction;

import android.app.Application;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.ui.setting.device_config.DeviceConfigItem;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class TransactionViewModel extends BaseAppViewModel {

    public MutableLiveData<List<Transaction>> transactionList = new MutableLiveData<>();

    private List<Transaction> paymentList;
    public TransactionViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        paymentList = new ArrayList<>();
        paymentList.add(new Transaction("12/8/24", "$500.00", "4712******8415-16:07", "Paid"));
        paymentList.add(new Transaction("15/8/24", "-$43.00", "4712******8415-14:10", "Voided"));
        paymentList.add(new Transaction("20/8/24", "$500.00", "VISA 4712******8415-11:07", "Paid"));
        paymentList.add(new Transaction("5/7/24", "$300.00", "4712******8415-10:07", "Paid"));
        paymentList.add(new Transaction("10/7/24", "-$20.00", "4712******8415-09:07", "Voided"));
        paymentList.add(new Transaction("3/9/24", "$150.00", "4712******8415-08:07", "Paid"));
        transactionList.setValue(paymentList);
    }
}
