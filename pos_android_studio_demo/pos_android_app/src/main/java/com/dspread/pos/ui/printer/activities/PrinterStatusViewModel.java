package com.dspread.pos.ui.printer.activities;

import android.app.Application;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.ui.printer.activities.base.BasePrinterViewModel;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.R;
import com.dspread.print.device.PrinterDevice;

import java.util.Hashtable;
import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PrinterStatusViewModel extends BasePrinterViewModel {
    public ObservableField<String> resultInfo = new ObservableField<>("");
    public ObservableBoolean showExtraButtons = new ObservableBoolean(true);
    public Hashtable<String,String> resultTable = new Hashtable<>();
    private final int PRINTER_DENSITY = 3;
    private final int PRINTER_SPEED = 5;
    private final int PRINTER_TEMPERATURE = 6;
    private final int PRINTER_VOLTAGE = 7;
    private final int PRINTER_STATUS = 8;
    public PrinterStatusViewModel(@NonNull Application application) {
        super(application);
        if (Build.MODEL.equalsIgnoreCase("D70") ||
            Build.MODEL.equalsIgnoreCase("D30") ||
            Build.MODEL.equalsIgnoreCase("D30M")) {
            showExtraButtons.set(false);
        }
    }

    public BindingCommand onGetStatusClick = new BindingCommand(() -> {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().getPrinterStatus();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    });

    public BindingCommand onGetDensityClick = new BindingCommand(() -> {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().getPrinterDensity();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    });

    public BindingCommand onGetSpeedClick = new BindingCommand(() -> {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().getPrinterSpeed();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    });

    public BindingCommand onGetTemperatureClick = new BindingCommand(() -> {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().getPrinterTemperature();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    });

    public BindingCommand onGetVoltageClick = new BindingCommand(() -> {
        try {
            if (getPrinter() != null) {
                PrinterHelper.getInstance().getPrinterVoltage();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    });



    public void updatePrinterInfo(PrinterDevice.ResultType resultType, String status) {
        switch (resultType.getValue()) {
            case PRINTER_DENSITY:
                resultTable.put(getApplication().getString(R.string.get_printer_density),status);
                break;
            case PRINTER_SPEED:
                resultTable.put(getApplication().getString(R.string.get_printer_speed),status);
                break;
            case PRINTER_TEMPERATURE:
                resultTable.put(getApplication().getString(R.string.get_printer_temperature),status);
                break;
            case PRINTER_VOLTAGE:
                resultTable.put(getApplication().getString(R.string.get_printer_voltage),status);
                break;
            case PRINTER_STATUS:
                resultTable.put(getApplication().getString(R.string.get_printer_status),status);
                break;
        }
        if(resultTable.size() > 0){
            StringBuffer stringBuffer = new StringBuffer();
            for (Map.Entry<String, String> entry : resultTable.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                TRACE.d( "Key: " + key + ", Value: " + value);
                stringBuffer.append(key+": "+value+"\n");
            }
            resultInfo.set(stringBuffer.toString());
        }
    }

    @Override
    protected void doPrint() {

    }
}