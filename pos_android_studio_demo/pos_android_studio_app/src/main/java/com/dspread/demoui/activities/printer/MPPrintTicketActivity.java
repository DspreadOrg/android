package com.dspread.demoui.activities.printer;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.QRCodeUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.view.BitmapPrintLine;
import com.dspread.demoui.view.MidTextPrintLine;
import com.dspread.demoui.view.PrintLine;
import com.dspread.demoui.view.PrintLayout;
import com.dspread.demoui.view.TextPrintLine;

public class MPPrintTicketActivity extends CommonActivity {
    private EditText etText, etFontsize;
    private Spinner mSpinnerSpAlignment;
    private Spinner mSpinnerSpGreyLevel;

    private TextView tvGrayLevel, tvAlign;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.mp_tickets_print));
        initView();
    }

    private void initView() {
        mSpinnerSpAlignment = findViewById(R.id.sp_alignment);
        mSpinnerSpGreyLevel = findViewById(R.id.sp_grey_level);
        tvGrayLevel = findViewById(R.id.gray_level);
        tvAlign = findViewById(R.id.align);
        tvAlign.setVisibility(View.GONE);
        mSpinnerSpAlignment.setVisibility(View.GONE);
        tvGrayLevel.setVisibility(View.GONE);
        mSpinnerSpGreyLevel.setVisibility(View.GONE);
        iv = findViewById(R.id.iv);
        Bitmap bitmap = getBitmap();
        iv.setImageBitmap(bitmap);

    }

    @Override
    int getLayoutId() {
        return R.layout.activity_print_tickets;
    }

    @Override
    public void onToolbarLinstener() {
        finish();
    }

    @Override
    int printTest() throws RemoteException {
        printMeituanTicket();
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status, int type) {
        TRACE.d("onPrintFinished:" + isSuccess + "---" + "status:" + status);
        if (status != null) {
        }
    }

    @Override
    void onPrintError(boolean isSuccess, String status, int type) {
    }

    private void printMeituanTicket() throws RemoteException {
        Bitmap bitmap = getBitmap();
        mPrinter.printBitmap(this, bitmap);
    }

    private Bitmap getBitmap() {
        PrintLayout printLayout = new PrintLayout(this);
        TextPrintLine headTPL = new TextPrintLine();
        headTPL.setBold(true);
        headTPL.setPosition(PrintLine.CENTER);
        headTPL.setContent("Testing");
        headTPL.setSize(14);
        printLayout.addText(headTPL);
        TextPrintLine headTwo = new TextPrintLine();
        headTwo.setBold(true);
        headTwo.setPosition(PrintLine.CENTER);
        headTwo.setContent("POS Signing of purchase orders");
        headTwo.setSize(14);
        printLayout.addText(headTwo);
        TextPrintLine headThree = new TextPrintLine();
        headThree.setBold(true);
        headThree.setPosition(PrintLine.CENTER);
        headThree.setContent("MERCHANT COPY");
        headThree.setSize(14);
        printLayout.addText(headThree);
        TextPrintLine toolLineTOP = new TextPrintLine();
        toolLineTOP.setContent("- - - - - - - - - - - - - -");
        toolLineTOP.setPosition(PrintLine.CENTER);
        printLayout.addText(toolLineTOP);

        TextPrintLine contextOne = new TextPrintLine();
        contextOne.setBold(false);
        contextOne.setPosition(PrintLine.LEFT);
        contextOne.setContent("ISSUER Agricultural Bank of China");
        contextOne.setSize(14);
        printLayout.addText(contextOne);


        TextPrintLine contextTwo = new TextPrintLine();
        contextTwo.setBold(false);
        contextTwo.setPosition(PrintLine.LEFT);
        contextTwo.setContent("ACQ 48873110");
        contextTwo.setSize(14);
        printLayout.addText(contextTwo);


        TextPrintLine contextThree = new TextPrintLine();
        contextThree.setBold(false);
        contextThree.setPosition(PrintLine.LEFT);
        contextThree.setContent("CARD number.");
        contextThree.setSize(14);
        printLayout.addText(contextThree);


        TextPrintLine contextFour = new TextPrintLine();
        contextFour.setBold(true);
        contextFour.setPosition(PrintLine.LEFT);
        contextFour.setContent("6228 48******8 116 S");
        contextFour.setSize(14);
        printLayout.addText(contextFour);


        TextPrintLine contextFive = new TextPrintLine();
        contextFive.setBold(false);
        contextFive.setPosition(PrintLine.LEFT);
        contextFive.setContent("TYPE of transaction(TXN TYPE)");
        contextFive.setSize(14);
        printLayout.addText(contextFive);


        TextPrintLine contextSix = new TextPrintLine();
        contextSix.setBold(false);
        contextSix.setPosition(PrintLine.LEFT);
        contextSix.setContent("SALE");
        contextSix.setSize(14);
        printLayout.addText(contextSix);


/*
        TextPrintLine toolLineTwo= new TextPrintLine();
        toolLineTOP.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTwo);


        TextPrintLine secondTPL = new TextPrintLine();
        secondTPL.setContent("Sales Order\nManual Cashier: 20 pieces in total");
        secondTPL.setSize(10);
        printerLayout.addText(secondTPL);

        TextPrintLine toolLineTPL = new TextPrintLine();
        toolLineTPL.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTPL);

        TextPrintLine thirdTPL = new TextPrintLine();
        thirdTPL.setContent("POS NO: 2110   Cashier: 0719\n"
                + "PrintTime: 2022-06-30:59\n"
                + "Order time: 2022-06-13 14:55");
        thirdTPL.setSize(10);
        printerLayout.addText(thirdTPL);*/

        TextPrintLine toolLineTPL2 = new TextPrintLine();
        toolLineTPL2.setContent("- - - - - - - - - - - - - -");
        toolLineTPL2.setPosition(PrintLine.CENTER);
        printLayout.addText(toolLineTPL2);

       /* MidTextPrintLine titleMTPL = new MidTextPrintLine(this);
        titleMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        titleMTPL.getLeftTextView().setTextSize(10);
        titleMTPL.getLeftTextView().setText("Item/NO");
        titleMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        titleMTPL.getMidTextView().setTextSize(10);
        titleMTPL.getMidTextView().setText("Quantity");
        titleMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        titleMTPL.getRightTextView().setTextSize(10);
        titleMTPL.getRightTextView().setText("Amount");
        printerLayout.addView(titleMTPL);*/

        MidTextPrintLine firstMTPL = new MidTextPrintLine(this);
        firstMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        firstMTPL.getLeftTextView().setTextSize(10);
        firstMTPL.getLeftTextView().setText("BATCH NO");
        firstMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        firstMTPL.getMidTextView().setTextSize(10);
        firstMTPL.getMidTextView().setText("");
        firstMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        firstMTPL.getRightTextView().setTextSize(10);
        firstMTPL.getRightTextView().setText("000043");
        printLayout.addView(firstMTPL);

        MidTextPrintLine secondMTPL = new MidTextPrintLine(this);
        secondMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        secondMTPL.getLeftTextView().setTextSize(10);
        secondMTPL.getLeftTextView().setText("VOUCHER NO");
        secondMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        secondMTPL.getMidTextView().setTextSize(10);
        secondMTPL.getMidTextView().setText("");
        secondMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        secondMTPL.getRightTextView().setTextSize(10);
        secondMTPL.getRightTextView().setText("000509");
        printLayout.addView(secondMTPL);

        MidTextPrintLine thirdMTPL = new MidTextPrintLine(this);
        thirdMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        thirdMTPL.getLeftTextView().setTextSize(10);
        thirdMTPL.getLeftTextView().setText("AUTH NO");
        thirdMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        thirdMTPL.getMidTextView().setTextSize(10);
        thirdMTPL.getMidTextView().setText("");
        thirdMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        thirdMTPL.getRightTextView().setTextSize(10);
        thirdMTPL.getRightTextView().setText("000786");
        printLayout.addView(thirdMTPL);

        MidTextPrintLine forthMTPL = new MidTextPrintLine(this);
        forthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        forthMTPL.getLeftTextView().setTextSize(10);
        forthMTPL.getLeftTextView().setText("DATE/TIME");
        forthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        forthMTPL.getMidTextView().setTextSize(10);
        forthMTPL.getMidTextView().setText("");
        forthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        forthMTPL.getRightTextView().setTextSize(10);
        forthMTPL.getRightTextView().setText("2014/12/07 16:15:17");
        printLayout.addView(forthMTPL);

        MidTextPrintLine fifthMTPL = new MidTextPrintLine(this);
        fifthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        fifthMTPL.getLeftTextView().setTextSize(10);
        fifthMTPL.getLeftTextView().setText("REF NO");
        fifthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        fifthMTPL.getMidTextView().setTextSize(10);
        fifthMTPL.getMidTextView().setText("");
        fifthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        fifthMTPL.getRightTextView().setTextSize(10);
        fifthMTPL.getRightTextView().setText("000001595276");
        printLayout.addView(fifthMTPL);

        MidTextPrintLine sixthMTPL = new MidTextPrintLine(this);
        sixthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        sixthMTPL.getLeftTextView().setTextSize(10);
        sixthMTPL.getLeftTextView().setText("2014/12/07 16:12:17");
        sixthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        sixthMTPL.getMidTextView().setTextSize(10);
        sixthMTPL.getMidTextView().setText("");
        sixthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        sixthMTPL.getRightTextView().setTextSize(10);
        sixthMTPL.getRightTextView().setText("");
        printLayout.addView(sixthMTPL);

        MidTextPrintLine seventhMTPL = new MidTextPrintLine(this);
        seventhMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        seventhMTPL.getLeftTextView().setTextSize(10);
        seventhMTPL.getLeftTextView().setText("AMOUNT:");
        seventhMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        seventhMTPL.getMidTextView().setTextSize(10);
        seventhMTPL.getMidTextView().setText("");
        seventhMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        seventhMTPL.getRightTextView().setTextSize(10);
        seventhMTPL.getRightTextView().setText("");
        printLayout.addView(seventhMTPL);

       /* MidTextPrintLine eighthMTPL = new MidTextPrintLine(this);
        eighthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        eighthMTPL.getLeftTextView().setTextSize(10);
        eighthMTPL.getLeftTextView().setText("008 Churrasco");
        eighthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        eighthMTPL.getMidTextView().setTextSize(10);
        eighthMTPL.getMidTextView().setText("1");
        eighthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        eighthMTPL.getRightTextView().setTextSize(10);
        eighthMTPL.getRightTextView().setText("117.56");
        printerLayout.addView(eighthMTPL);

        MidTextPrintLine ninthMTPL = new MidTextPrintLine(this);
        ninthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ninthMTPL.getLeftTextView().setTextSize(10);
        ninthMTPL.getLeftTextView().setText("009 Rost Goose");
        ninthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ninthMTPL.getMidTextView().setTextSize(10);
        ninthMTPL.getMidTextView().setText("2");
        ninthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ninthMTPL.getRightTextView().setTextSize(10);
        ninthMTPL.getRightTextView().setText("7.56");
        printerLayout.addView(ninthMTPL);

        TextPrintLine toolLineTPL3 = new TextPrintLine();
        toolLineTPL3.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTPL3);*/

        TextPrintLine toolLineTPL4 = new TextPrintLine();
        toolLineTPL4.setContent("RMB:249.00");
        toolLineTPL4.setPosition(PrintLine.CENTER);
        printLayout.addText(toolLineTPL4);

        TextPrintLine toolLineTPL5 = new TextPrintLine();
        toolLineTPL5.setContent("- - - - - - - - - - - - - -");
        toolLineTPL5.setPosition(PrintLine.CENTER);
        printLayout.addText(toolLineTPL5);

        TextPrintLine toolLineTPL6 = new TextPrintLine();
        toolLineTPL6.setContent("Please scan the QRCode for getting more information: ");
        toolLineTPL6.setSize(10);
        printLayout.addText(toolLineTPL6);

        Bitmap barCodeBM = QRCodeUtil.getBarCodeBM("123456", 300, 100);
        BitmapPrintLine bitmapPrintLine1 = new BitmapPrintLine(barCodeBM, PrintLine.CENTER);
        printLayout.addBitmap(bitmapPrintLine1);

        TextPrintLine toolLineTPL7 = new TextPrintLine();
        toolLineTPL7.setContent("Please scan the BarCode for getting more information: ");
        toolLineTPL7.setSize(10);
        printLayout.addText(toolLineTPL7);

        Bitmap qrcodeBM = QRCodeUtil.getQrcodeBM("123456", 200);
        BitmapPrintLine bitmapPrintLine2 = new BitmapPrintLine(qrcodeBM, PrintLine.CENTER);
        printLayout.addBitmap(bitmapPrintLine2);

        TextPrintLine textPrintLine = new TextPrintLine();
        textPrintLine.setContent("\n");
        textPrintLine.setSize(10);
        textPrintLine.setBold(true);
        textPrintLine.setPosition(PrintLine.CENTER);
        printLayout.addText(textPrintLine);
        Bitmap bitmap = printLayout.viewToBitmap();
        return bitmap;
    }

}
