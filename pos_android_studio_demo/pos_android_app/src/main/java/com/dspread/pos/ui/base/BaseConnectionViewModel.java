package com.dspread.pos.ui.base;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.common.enums.PaymentType;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;

public class BaseConnectionViewModel extends BaseAppViewModel {
    private SingleLiveEvent<Boolean> connectionStatus = new SingleLiveEvent<>();
    // 当前选中的交易类型
    public final ObservableField<String> selectedTransType = new ObservableField<>();

    public BaseConnectionViewModel(@NonNull Application application) {
        super(application);
    }

    public void setConnected(boolean isConnected) {
        connectionStatus.postValue(isConnected);
    }


    
    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }

}