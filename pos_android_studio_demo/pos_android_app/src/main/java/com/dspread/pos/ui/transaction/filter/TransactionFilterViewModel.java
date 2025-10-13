package com.dspread.pos.ui.transaction.filter;

import android.app.Application;

import com.dspread.pos.common.base.BaseAppViewModel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

public class TransactionFilterViewModel extends BaseAppViewModel {

    public SingleLiveEvent<Void> doneEvent = new SingleLiveEvent<>();
    public ObservableBoolean isD70 = new ObservableBoolean(false);

    public TransactionFilterViewModel(@NonNull Application application) {
        super(application);
    }

    public BindingCommand doneCommand = new BindingCommand(() -> {
        doneEvent.call();
    });
}
