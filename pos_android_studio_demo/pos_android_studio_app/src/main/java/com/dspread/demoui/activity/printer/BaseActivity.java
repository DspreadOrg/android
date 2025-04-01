package com.dspread.demoui.activity.printer;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.dspread.demoui.utils.DeviceUtils;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected PrinterDevice mPrinter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(getLayoutId());
        initView();
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        if (mPrinter == null) {
            PrinterAlertDialog.showAlertDialog(this);
            return;
        }
        if ("D30".equalsIgnoreCase(Build.MODEL) && !DeviceUtils.isAppInstalled(getApplicationContext(), DeviceUtils.UART_AIDL_SERVICE_APP_PACKAGE_NAME)) {
            mPrinter.initPrinter(BaseActivity.this, new PrinterInitListener() {
                @Override
                public void connected() {
                    mPrinter.setPrinterTerminatedState(PrinterDevice.PrintTerminationState.PRINT_STOP);
                }
                @Override
                public void disconnected() {
                }
            });

        } else {
            mPrinter.initPrinter(this);
        }
        MyPrinterListener myPrinterListener = new MyPrinterListener();
        mPrinter.setPrintListener(myPrinterListener);
    }

    protected abstract int getLayoutId();

    protected void initView() {
    }

    protected abstract void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType);

    class MyPrinterListener implements PrintListener {

        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
            Log.w("printResult", "boolean b==" + b);
            Log.w("printResult", "String s==" + s);
            Log.w("printResult", "resultType==" + resultType.toString());
            onReturnPrintResult(b, s, resultType);
        }
    }

}
