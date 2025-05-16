package com.dspread.pos.ui.printer.activities;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.action.printerservice.PrintStyle;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;

public class PrintFunctionMultiViewModel extends BasePrinterViewModel {
    public ObservableField<String> info = new ObservableField<>();

    public PrintFunctionMultiViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void doPrint() {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().printMultipleColumns(getApplication());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrintComplete(boolean isSuccess, String status) {
        super.onPrintComplete(isSuccess, status);
        info.set("Print Result: " + (isSuccess ? "Success" : "Failed") + "\nStatus: " + status);
    }
}