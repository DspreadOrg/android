package com.dspread.pos.ui.scan;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dspread.pos.common.base.BaseAppViewModel;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

public class ScanViewModel extends BaseAppViewModel {
    public ObservableBoolean isScanning = new ObservableBoolean(false);
    public ObservableBoolean hasResult = new ObservableBoolean(false);
    public ObservableField<String> scanResult = new ObservableField<>("");
    // 移除 launcher 相关代码
    public SingleLiveEvent<Void> startScanEvent = new SingleLiveEvent<>();


    public BindingCommand startScanCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            startScan();
        }
    });

    public ScanViewModel(@NonNull Application application) {
        super(application);
    }
    
    private void startScan() {
        isScanning.set(true);
        hasResult.set(false);
        startScanEvent.call();
    }

    public void onScanResult(String result) {
        isScanning.set(false);
        hasResult.set(true);
        scanResult.set(result);
    }
}
