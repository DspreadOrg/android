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

import androidx.appcompat.app.AppCompatActivity;

import com.action.printerservice.PrintStyle;
import com.action.printerservice.barcode.Barcode1D;
import com.action.printerservice.barcode.Barcode2D;
import com.dspread.demoui.R;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

public class ComprehensiveActivity extends AppCompatActivity implements View.OnClickListener {
    private PrinterDevice mPrinter;
    private PrintLineStyle printLineStyle;
    private ImageView ivBackTitle;
    private TextView tvTitle;
    private Button btnComposite;
    private Button btnMulti;
    private Button btnStopPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_comprehensive);
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        mPrinter.initPrinter(this);
        MyPrinterListener myPrinterListener = new MyPrinterListener();
        mPrinter.setPrintListener(myPrinterListener);
        printLineStyle = new PrintLineStyle();
        initView();
    }

    private void initView() {
        ivBackTitle = findViewById(R.id.iv_back_title);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.print_ticket));
        btnComposite = findViewById(R.id.btn_composite);
        btnMulti = findViewById(R.id.btn_multi);
        btnStopPrint= findViewById(R.id.btn_stopprint);
        String deviceModel = Build.MODEL;
        if("mp600".equals(deviceModel)){
            btnStopPrint.setVisibility(View.VISIBLE);
        }else{
            btnStopPrint.setVisibility(View.GONE);
        }
        ivBackTitle.setOnClickListener(this);
        btnComposite.setOnClickListener(this);
        btnMulti.setOnClickListener(this);
        btnStopPrint.setOnClickListener(this);
    }

    private void printtext() {
        printLineStyle.setFontStyle(PrintStyle.FontStyle.BOLD_ITALIC);
        printLineStyle.setFontSize(10);
        printLineStyle.setAlign(PrintLine.CENTER);
        mPrinter.addPrintLintStyle(printLineStyle);

        try {
            mPrinter.addText("Testing");
            mPrinter.addText("POS Signing of purchase orders");
            mPrinter.addText("MERCHANT COPY");
            mPrinter.addText("- - - - - - - - - - - - - -");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 14));
            mPrinter.addText("ISSUER Agricultural Bank of China");
            mPrinter.addText("ACQ 48873110");
            mPrinter.addText("CARD number.");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 14));
            mPrinter.addText("6228 48******8 116 S");
            mPrinter.addText("TYPE of transaction(TXN TYPE)");
            mPrinter.addText("SALE");
            mPrinter.addText("- - - - - - - - - - - - - -");
            mPrinter.addTexts(new String[]{"BATCH NO", "000043"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addTexts(new String[]{"VOUCHER NO", "000509"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addTexts(new String[]{"AUTH NO", "000786"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addTexts(new String[]{"DATE/TIME", "2010/12/07 16:15:17"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addTexts(new String[]{"REF NO", "000001595276"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addTexts(new String[]{"2014/12/07 16:12:17", ""}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addTexts(new String[]{"AMOUNT:", ""}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
            mPrinter.addText("RMB:249.00");
            mPrinter.addText("- - - - - - - - - - - - - -");
            mPrinter.addText("Please scan the QRCode for getting more information: ");
            mPrinter.addBarCode(this, Barcode1D.CODE_128.name(), 400, 100, "123456", PrintLine.CENTER);
            mPrinter.addText("Please scan the QRCode for getting more information:");
            mPrinter.addQRCode(300, Barcode2D.QR_CODE.name(), "123456", PrintLine.CENTER);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
            mPrinter.printBitmap(this, bitmap);
            mPrinter.addBitmap(bitmap);
            mPrinter.setFooter(3);
            mPrinter.setPrintStyle(printLineStyle);
            mPrinter.print(this);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.btn_composite:
                printtext();
                break;
            case R.id.btn_multi:
                try {
                    mPrinter.addTexts(new String[]{"TEST1"}, new int[]{1}, new int[]{PrintStyle.Alignment.NORMAL});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2"}, new int[]{1, 4}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2", "TEST3"}, new int[]{1, 2, 2}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.ALIGN_OPPOSITE});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2", "TEST3", "TEST4"}, new int[]{1, 1, 1, 2}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.ALIGN_OPPOSITE});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2", "TEST3", "TEST4", "TEST5"}, new int[]{1, 1, 1, 1, 1}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.ALIGN_OPPOSITE});

                    mPrinter.print(this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            case R.id.btn_stopprint:
                try {
                    mPrinter.stopPrint();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }
    }

    class MyPrinterListener implements PrintListener {

        @Override
        public void printResult(boolean b, String s, int i) {
            Log.w("printResult", "boolean b==" + b);
            Log.w("printResult", "String s==" + s);
            Log.w("printResult", "int i==" + i);

        }
    }
}