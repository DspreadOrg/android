package com.dspread.pos.ui.printer.activities;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.RemoteException;

import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingCommand;


public class PrintTicketViewModel extends BasePrinterViewModel {

    private MutableLiveData<Bitmap> receiptBitmap = new MutableLiveData<>();


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


    public void printTicket(Bitmap mBitmap) {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().printBitmap(getApplication(),mBitmap);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* public BindingCommand printTicket = new BindingCommand(() -> {
         doPrint();
     });
 */
    public MutableLiveData<Bitmap> getReceiptBitmap() {
        return receiptBitmap;
    }
    public void generateReceiptBitmap(Map<String,String> map) {
        try {
            Bitmap ticketBitmap = PrinterHelper.getInstance().getTicketBitmap(getApplication(),map);
            receiptBitmap.postValue(ticketBitmap);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onPrintComplete(boolean isSuccess, String status) {
        super.onPrintComplete(isSuccess, status);
        PrinterHelper.getInstance().close();
    }
}