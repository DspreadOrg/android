package com.dspread.pos.ui.transaction.reissue;

import android.app.Application;

import com.dspread.pos.common.base.BaseAppViewModel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;

public class TransactionReissueReceipViewModel extends BaseAppViewModel {

    public ObservableBoolean isD70 = new ObservableBoolean(false);

    public TransactionReissueReceipViewModel(@NonNull Application application) {
        super(application);
    }
}
