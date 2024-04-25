package com.dspread.demoui.activity.printer;

import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.action.printerservice.PrintStyle;
import com.action.printerservice.barcode.Barcode1D;
import com.dspread.demoui.R;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

import androidx.appcompat.app.AppCompatActivity;

public class PrintTicketActivity extends AppCompatActivity implements View.OnClickListener {
    private PrinterDevice mPrinter;
    private PrintLineStyle printLineStyle;
    private ImageView ivBackTitle;
    private TextView tvTitle;
    private Button btnComposite;
    private Button btnMulti;
    private Button btnStopPrint;
    private Button btnPrint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_print_ticket);
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        if ("D30".equals(Build.MODEL)) {
            mPrinter.initPrinter(PrintTicketActivity.this, new PrinterInitListener() {
                @Override
                public void connected() {
                    mPrinter.setPrinterTerminatedState(PrinterDevice.PrintTerminationState.PRINT_STOP);
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

        } else {
            mPrinter.initPrinter(this);
        }
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
        btnStopPrint = findViewById(R.id.btn_stopprint);
        String deviceModel = Build.MODEL;
        btnPrint = findViewById(R.id.btn_Print);
        if ("mp600".equalsIgnoreCase(deviceModel)) {
            btnStopPrint.setVisibility(View.VISIBLE);
        } else {
            btnStopPrint.setVisibility(View.GONE);
        }
        ivBackTitle.setOnClickListener(this);
        btnComposite.setOnClickListener(this);
        btnMulti.setOnClickListener(this);
        btnStopPrint.setOnClickListener(this);
//        printtext();
    }

    private void printtext() {
        printLineStyle.setFontStyle(PrintStyle.FontStyle.BOLD);
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
//            mPrinter.addText("Please scan the QRCode for getting more information:");
//            mPrinter.addQRCode(300, Barcode2D.QR_CODE.name(), "123456", PrintLine.CENTER);
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
//            mPrinter.addBitmap(bitmap);
            mPrinter.setPrintStyle(printLineStyle);
            mPrinter.setFooter(100);
            mPrinter.print(this);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }


    private void printTicket() {
        printLineStyle.setFontStyle(PrintStyle.FontStyle.BOLD);
        printLineStyle.setFontSize(16);
        printLineStyle.setAlign(PrintLine.CENTER);
        mPrinter.addPrintLintStyle(printLineStyle);

        try {
            mPrinter.addText("银联签购单");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.CENTER, 12));
            mPrinter.addTexts(new String[]{"商户存根", "MERCHANT COPY"}, new int[]{4, 6}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addText("- - - - - - - - - - - - - - - - - - - - - ");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.CENTER, 12));
            mPrinter.addTexts(new String[]{"商户名称(MERCHANT NAME):", "测试商户"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addTexts(new String[]{"商户号(MERCHANT NO.):", "111111111111111"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"终端号(TERMINAL ID.):", "22222222"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"操作员号(OPERATOR NO.):", "01"}, new int[]{8, 2}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addText("- - - - - - - - - - - - - - - - - - - - - ");

            mPrinter.addTexts(new String[]{"收单行(ACQUIRER):", "xxxx银行"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});


            mPrinter.addTexts(new String[]{"卡号(CARD NO.):", ""}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"", "621485******2878"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});



            mPrinter.addTexts(new String[]{"有效期(EXP.DATE):", "2030/01"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"卡顺序号(CARD SN):", "0000"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.CENTER, 12));
            mPrinter.addText("- - - - - - - - - - - - - - - - - - - - - ");

            mPrinter.addTexts(new String[]{"交易类型(TRANS TYPE):", ""}, new int[]{9, 1}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.RIGHT, 14));

            mPrinter.addText("消费");

            mPrinter.addTexts(new String[]{"批次号(BATCH NO.):", "000326"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"流水号(TRACE NO.):", "000320"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"参考号(REF，NO.):", "622228356483"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"日期/时间(DATE/TIME):", "2024-04-13 08:49:58"}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

            mPrinter.addTexts(new String[]{"金额(AMOUNT):", ""}, new int[]{7, 3}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.RIGHT, 14));
            mPrinter.addText("RMB 0.59");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 12));
            mPrinter.addText("持卡人签名CARDHOLDER SIGNATURE:");
            mPrinter.addText("");
            mPrinter.addText("");
            mPrinter.addText("");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 12));
            mPrinter.addText("本人确认以上交易,同意将其记入本卡账户");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 12));
            mPrinter.addText("I ACKNOWLEDGE SATISFACTORY RECEIPT OFRELATIVE GOODS/SERVICES");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.CENTER, 14));
            mPrinter.addText("- - - - - - - - - - - - - - - - - - - - - ");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.LEFT, 12));
            mPrinter.addText("备注(REFERENCE):");
            mPrinter.addTexts(new String[]{"AROC:", "98E9FB585E304BFD"}, new int[]{3, 7}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addTexts(new String[]{"ID:", "A0000333010101"}, new int[]{3, 7}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addTexts(new String[]{"APPLAB:", "PBOC DEBIT"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addTexts(new String[]{"ATC:", "0966"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addTexts(new String[]{"TVR:", "00000000000"}, new int[]{3, 7}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addTexts(new String[]{"TSI:", "000"}, new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.LEFT, 12));

            mPrinter.addText("DSPREAD D6001610020202304040033");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.NORMAL, PrintLine.CENTER, 12));
            mPrinter.addText("****重打印(REPRINT)****");
            mPrinter.addPrintLintStyle(new PrintLineStyle(PrintStyle.FontStyle.BOLD, PrintLine.CENTER, 12));
            mPrinter.addText("- - - - - - - - - - - - - - - - - - - - - ");
            mPrinter.setPrintStyle(printLineStyle);
            mPrinter.setFooter(100);
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
            case R.id.btn_Print:
               // printtext();
                printTicket();
                break;
            case R.id.btn_multi:
                try {
                    mPrinter.addTexts(new String[]{"TEST1"}, new int[]{1}, new int[]{PrintStyle.Alignment.NORMAL});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2"}, new int[]{1, 4}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2", "TEST3"}, new int[]{1, 2, 2}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.ALIGN_OPPOSITE});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2", "TEST3", "TEST4"}, new int[]{1, 1, 1, 2}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.ALIGN_OPPOSITE});
                    mPrinter.addTexts(new String[]{"TEST1", "TEST2", "TEST3", "TEST4", "TEST5"}, new int[]{1, 1, 1, 1, 1}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.CENTER, PrintStyle.Alignment.ALIGN_OPPOSITE});
                    mPrinter.addText(" ");
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

        //        @Override
//        public void printResult(boolean b, String s, int i) {
//            Log.w("printResult", "boolean b==" + b);
//            Log.w("printResult", "String s==" + s);
//            Log.w("printResult", "int i==" + i);
//
//        }
        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
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