package com.dspread.pos.ui.printer.activities;

import android.app.Application;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.action.printerservice.PrintStyle;
import com.action.printerservice.barcode.Barcode1D;
import com.action.printerservice.barcode.Barcode2D;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;


public class PrintTicketViewModel extends BasePrinterViewModel {

    public ObservableField<String> info = new ObservableField<>();
    public PrintTicketViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void doPrint() {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().printTicket(getApplication());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrintComplete(boolean isSuccess, String status) {
        super.onPrintComplete(isSuccess, status);
        info.set("Print Result: " + (isSuccess ? "Success" : "Failed") + "\nStatus: " + status);
    }
}