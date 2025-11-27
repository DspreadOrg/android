package com.dspread.pos.ui.printer.activities.base;

import android.app.Dialog;
import android.os.Bundle;

import com.dspread.pos.utils.DialogUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterManager;

import androidx.databinding.ViewDataBinding;
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
        // Set printer instance to ViewModel
        viewModel.setPrinter(mPrinter, this);
        MyPrinterListener myPrinterListener = new MyPrinterListener();
        mPrinter.setPrintListener(myPrinterListener);
        mPrinter.setFooter(50);//unit is px
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    protected abstract void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType);

    class MyPrinterListener implements PrintListener {
        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
            TRACE.d("printResult:" + b + "--status--" + s + "--resultType---" + resultType.getValue());
            if (!b && resultType.getValue() == -9) {
                showLowBatteryDialog();
            }
            onReturnPrintResult(b, s, resultType);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPrinter != null) {
            mPrinter.close();
        }
        if (lowBatteryDialog != null && lowBatteryDialog.isShowing()) {
            lowBatteryDialog.dismiss();
        }
    }

    private Dialog lowBatteryDialog;

    private void showLowBatteryDialog() {
        lowBatteryDialog = DialogUtils.showLowBatteryDialog(this, R.layout.dialog_low_battery, R.id.okButton, false);
    }
}
