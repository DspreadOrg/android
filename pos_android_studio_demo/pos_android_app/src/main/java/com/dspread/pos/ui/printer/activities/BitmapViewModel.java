package com.dspread.pos.ui.printer.activities;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;
import com.dspread.pos_new_android_app.R;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

public class BitmapViewModel extends BasePrinterViewModel {
    public ObservableField<Bitmap> bitmapImage = new ObservableField<>();


    public BitmapViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void doPrint() {
        try {
            if (getPrinter() != null) {
                Bitmap bitmap = PrinterHelper.getInstance().printPicture(getApplication());
                bitmapImage.set(bitmap);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}