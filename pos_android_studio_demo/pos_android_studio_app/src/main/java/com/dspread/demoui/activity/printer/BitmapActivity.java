package com.dspread.demoui.activity.printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;

import androidx.appcompat.app.AppCompatActivity;

public class BitmapActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBackTitle;
    private TextView tvTitle;
    private ImageView bitmapImage;
    private Button btnBitmapPrint;
    private PrinterDevice mPrinter;
    private PrintLineStyle printLineStyle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_bitmap);
        initView();
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        if ("D30".equalsIgnoreCase(Build.MODEL)) {
            mPrinter.initPrinter(BitmapActivity.this, new PrinterInitListener() {
                @Override
                public void connected() {
                    Log.w("MODEL","modeD30");
//                    mPrinter.setPrinterTerminatedState(PrinterDevice.PrintTerminationState.PRINT_NORMAL);
                /*When no paper, the
                printer terminates printing and cancels the printing task.*/
//              PrinterDevice.PrintTerminationState.PRINT_STOP
               /* When no paper, the
                printer will prompt that no paper. After loading the paper, the printer
                will continue to restart printing.*/
//              PrinterDevice.PrintTerminationState. PRINT_NORMAL
                }
                @Override
                public void disconnected() {
                }
            });

        }else{
            mPrinter.initPrinter(this);
        }
        MyPrinterListener myPrinterListener = new MyPrinterListener();
        mPrinter.setPrintListener(myPrinterListener);
        printLineStyle = new PrintLineStyle();
    }

    private void initView() {
        ivBackTitle = findViewById(R.id.iv_back_title);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.print_bitmap));
        bitmapImage = findViewById(R.id.bitmap_image);
        btnBitmapPrint = findViewById(R.id.btn_bitmap_print);
        ivBackTitle.setOnClickListener(this);
        btnBitmapPrint.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.btn_bitmap_print:
                try {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                    mPrinter.setFooter(100);
                    mPrinter.printBitmap(this,bitmap);
                    btnBitmapPrint.setEnabled(true);

                } catch (RemoteException e) {
                    throw new RuntimeException(e);

                }
                break;

        }
    }
    class MyPrinterListener implements PrintListener {

        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
            btnBitmapPrint.setEnabled(true);
            Log.w("printResult", "boolean b==" + b);
            Log.w("printResult", "String s==" + s);
            Log.w("printResult", "resultType==" + resultType.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrinter.close();
    }
}