package com.dspread.pos.ui.transaction.filter;

import android.app.Application;

import com.dspread.pos.common.base.BaseAppViewModel;

import androidx.annotation.NonNull;

import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class TransactionFilterViewModel extends BaseAppViewModel {
    public TransactionFilterViewModel(@NonNull Application application) {
        super(application);
    }

    public BindingCommand doneCommand = new BindingCommand(() -> finish());
}
