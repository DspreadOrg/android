package com.dspread.pos.ui.printer.activities.base;

import android.os.Bundle;

import androidx.databinding.ViewDataBinding;

import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;

import me.goldze.mvvmhabit.base.BaseActivity;

public abstract class PrinterBaseActivity<V extends ViewDataBinding, VM extends BasePrinterViewModel> extends BaseActivity<ActivityPrinterBaseBinding, VM> {
    protected PrinterDevice mPrinter;

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_printer_base;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        if (mPrinter == null) {
            PrinterAlertDialog.showAlertDialog(this);
            return;
        }
        
        // 设置打印机实例到 ViewModel
        viewModel.setPrinter(mPrinter,this);
        
        MyPrinterListener myPrinterListener = new MyPrinterListener();
        mPrinter.setPrintListener(myPrinterListener);

        // 设置返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }



    protected abstract void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType);

    class MyPrinterListener implements PrintListener {
        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
            onReturnPrintResult(b, s, resultType);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPrinter != null) {
            mPrinter.close();
        }
    }
}
