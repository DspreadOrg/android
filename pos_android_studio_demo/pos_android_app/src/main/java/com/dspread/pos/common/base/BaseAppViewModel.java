package com.dspread.pos.common.base;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import me.goldze.mvvmhabit.base.BaseViewModel;

public class BaseAppViewModel extends BaseViewModel {
    protected Application application;
    protected Context context;

    public BaseAppViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        this.context = application.getApplicationContext();
    }

    // 添加公共的 ViewModel 方法
    protected void showLoading() {
        showDialog();
    }

    protected void hideLoading() {
        dismissDialog();
    }
}