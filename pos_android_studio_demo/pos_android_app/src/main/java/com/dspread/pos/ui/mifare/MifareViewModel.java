package com.dspread.pos.ui.mifare;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import me.goldze.mvvmhabit.base.BaseViewModel;

import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.TRACE;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.utils.ToastUtils;

/**
 * Mifare卡片操作视图模型
 */
public class MifareViewModel extends BaseViewModel {


    public ObservableField<String> resultText = new ObservableField<>("");

    public MifareViewModel(@NonNull Application application) {
        super(application);
    }


}