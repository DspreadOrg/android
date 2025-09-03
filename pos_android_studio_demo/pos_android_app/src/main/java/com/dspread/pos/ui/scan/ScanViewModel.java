package com.dspread.pos.ui.scan;

import android.app.Application;
import android.util.Log;

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
    // Remove launcher related code
    public SingleLiveEvent<Void> startScanEvent = new SingleLiveEvent<>();

    public ScanViewModel(@NonNull Application application) {
        super(application);
    }

    public void onScanResult(String result) {
        isScanning.set(false);
        hasResult.set(true);
        scanResult.set(result);
    }

    public BindingCommand closeButton = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Log.d("Payment", "close button");
            finish();
        }
    });
}
