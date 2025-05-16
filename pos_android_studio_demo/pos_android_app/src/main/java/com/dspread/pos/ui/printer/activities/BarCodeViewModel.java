package com.dspread.pos.ui.printer.activities;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.action.printerservice.barcode.Barcode1D;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;
import com.dspread.pos.ui.printer.activities.base.PrintDialog;
import com.dspread.pos.utils.QRCodeUtil;
import com.dspread.pos_new_android_app.R;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

public class BarCodeViewModel extends BasePrinterViewModel {
    public ObservableField<String> content = new ObservableField<>("1234567890");
    public ObservableField<String> symbology = new ObservableField<>("CODE_128");
    public ObservableField<String> height = new ObservableField<>("100");
    public ObservableField<String> width = new ObservableField<>("400");
    public ObservableField<String> align = new ObservableField<>("CENTER");
    public ObservableField<String> grayLevel = new ObservableField<>("5");
    public ObservableField<String> speedLevel = new ObservableField<>("5");
    public ObservableField<String> densityLevel = new ObservableField<>("5");
    public ObservableField<Bitmap> barcodeBitmap = new ObservableField<>();

    public SingleLiveEvent<String> showInputDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showOptionsDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> showHeightDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> showWidthDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showAlignDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showGrayLevelDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showSpeedLevelDialog = new SingleLiveEvent<>();
    public SingleLiveEvent<String[]> showDensityLevelDialog = new SingleLiveEvent<>();

    public BindingCommand onContentClick = new BindingCommand(() -> {
        // 显示内容输入对话框
        showInputDialog.call();
    });
    
    public BindingCommand onSymbologyClick = new BindingCommand(() -> {
        String[] options = {Barcode1D.CODE_128.name(), Barcode1D.CODABAR.name(), Barcode1D.CODE_39.name(), Barcode1D.EAN_8.name(),
                Barcode1D.EAN_13.name(), Barcode1D.UPC_A.name(), Barcode1D.UPC_E.name()};
        showOptionsDialog.setValue(options);
    });
    
    public BindingCommand onHeightClick = new BindingCommand(() -> {
        showHeightDialog.setValue(true);
    });

    public BindingCommand onWidthClick = new BindingCommand(() -> {
        showWidthDialog.setValue(true);

    });

    public BindingCommand onAlignClick = new BindingCommand(() -> {
        String[] alignOptions = new String[]{
                getApplication().getString(R.string.at_the_left),
                getApplication().getString(R.string.at_the_right),
                getApplication().getString(R.string.at_the_center)
        };
        showAlignDialog.setValue(alignOptions);
    });

    public BindingCommand onGrayLevelClick = new BindingCommand(() -> {
        String[] graylevel = new String[]{"1", "2", "3", "4", "5"};
        showGrayLevelDialog.setValue(graylevel);
    });

    public BindingCommand onSpeedLevelClick = new BindingCommand(() -> {
        String[] speedLevel = new String[]{"1", "2", "3", "4", "5"};
        showSpeedLevelDialog.setValue(speedLevel);
    });

    public BindingCommand onDensityLevelClick = new BindingCommand(() -> {
        String[] densitylevel = new String[]{"1", "2", "3", "4", "5"};
        showDensityLevelDialog.setValue(densitylevel);
    });

    public BarCodeViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void doPrint() {
        try {
            Bitmap bitmap = PrinterHelper.getInstance().printBarCode(getApplication(), align.get(), width.get(), height.get(), content.get(), speedLevel.get(), densityLevel.get(), symbology.get());
            generateBarcode(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            onPrintComplete(false, e.getMessage());
        }
    }
    
    public void generateBarcode(Bitmap bitmap) {
        // 生成条形码预览图
        // ... 生成条形码的具体实现 ...
        barcodeBitmap.set(bitmap);
    }
}