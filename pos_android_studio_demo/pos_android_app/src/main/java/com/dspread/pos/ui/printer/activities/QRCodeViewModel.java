package com.dspread.pos.ui.printer.activities;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.action.printerservice.barcode.Barcode2D;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;
import com.dspread.pos.utils.QRCodeUtil;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

public class QRCodeViewModel extends BasePrinterViewModel {
    public ObservableField<String> content = new ObservableField<>("1234567890");
    public ObservableField<String> size = new ObservableField<>("200");
    public ObservableField<String> align = new ObservableField<>("CENTER");
    public ObservableField<String> grayLevel = new ObservableField<>("1");
    public ObservableField<String> errorLevel = new ObservableField<>("L");
    public ObservableField<String> speedLevel = new ObservableField<>("5");
    public ObservableField<String> densityLevel = new ObservableField<>("5");
    public ObservableField<Bitmap> qrCodeImage = new ObservableField<>();
    
    public SingleLiveEvent<String> showInputDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showAlignDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> showSizeDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showGrayLevelDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showErrorLevelDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showSpeedLevelDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showDensityLevelDialog = new SingleLiveEvent<>();
    
    public BindingCommand onContentClick = new BindingCommand(() -> {
        showInputDialog.call();
    });
    
    public BindingCommand onSizeClick = new BindingCommand(() -> {
        showSizeDialog.setValue(true);
    });
    
    public BindingCommand onAlignClick = new BindingCommand(() -> {
        showAlignDialog.setValue(new String[]{"LEFT", "RIGHT", "CENTER"});
    });
    
    public BindingCommand onGrayLevelClick = new BindingCommand(() -> {
        showGrayLevelDialog.setValue(new String[]{"1", "2", "3", "4", "5"});
    });
    
    public BindingCommand onErrorLevelClick = new BindingCommand(() -> {
        showErrorLevelDialog.setValue(new String[]{
            Barcode2D.ErrorLevel.L.name(),
            Barcode2D.ErrorLevel.M.name(),
            Barcode2D.ErrorLevel.Q.name(),
            Barcode2D.ErrorLevel.H.name()
        });
    });

    public BindingCommand onSpeedLevelClick = new BindingCommand(() -> {
        String[] speedLevel = new String[]{"1", "2", "3", "4", "5"};
        showSpeedLevelDialog.setValue(speedLevel);
    });

    public BindingCommand onDensityLevelClick = new BindingCommand(() -> {
        String[] densitylevel = new String[]{"1", "2", "3", "4", "5"};
        showDensityLevelDialog.setValue(densitylevel);
    });
    public QRCodeViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void doPrint() {
        try {
                Bitmap bitmap = PrinterHelper.getInstance().printQRcode(getApplication(),align.get(), size.get(), content.get(), errorLevel.get());
                qrCodeImage.set(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}