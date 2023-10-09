package com.dspread.demoui.activities.printer;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.action.printerservice.PrintStyle;
import com.action.printerservice.barcode.Barcode1D;
import com.action.printerservice.barcode.Barcode2D;
import com.dspread.demoui.R;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.view.PrintLine;
import com.dspread.print.device.bean.PrintLineStyle;

public class MPPrintTextActivity extends CommonActivity {

    private EditText etText, etFontsize;
    private Spinner mSpinnerSpAlignment;
    private Spinner mSpinnerSpGreyLevel;
    private PrintLineStyle printLineStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        etText = findViewById(R.id.et_text);
        etFontsize = findViewById(R.id.et_font_size);
        setTitle(getString(R.string.mp_text_print));
        initView();
    }

    private void initView() {
        mSpinnerSpAlignment = findViewById(R.id.sp_alignment);
        mSpinnerSpGreyLevel = findViewById(R.id.sp_grey_level);
        mSpinnerSpAlignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                printLineStyle = new PrintLineStyle();
                if (item.equals("Left")) {
                    //mPrinter.setPrintStyle(PrintStyle.Key.ALIGNMENT, PrintStyle.Alignment.LEFT);
                    printLineStyle.setAlign(PrintLine.LEFT);
                    mPrinter.setPrintStyle(printLineStyle);
                } else if (item.equals("Right")) {
                    printLineStyle.setAlign(PrintLine.RIGHT);
                    mPrinter.setPrintStyle(printLineStyle);
                } else if (item.equals("Center")) {
                    printLineStyle.setAlign(PrintLine.CENTER);
                    mPrinter.setPrintStyle(printLineStyle);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSpinnerSpGreyLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                try {
                    mPrinter.setPrinterDensity(Integer.parseInt(item));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onToolbarLinstener() {
        finish();

    }

    @Override
    int getLayoutId() {
        return R.layout.activity_mpprint_text;
    }

    @Override
    int printTest() throws RemoteException {
        /*printLineStyle.setFontSize(getFontSize());
        mPrinter.setPrintStyle(printLineStyle);

        mPrinter.printText(getText());
*/

        PrintLineStyle bean = new PrintLineStyle();
        bean.setFontStyle(PrintStyle.FontStyle.NORMAL);
        bean.setFontSize(18);
        bean.setAlign(PrintLine.CENTER);
        mPrinter.addPrintLintStyle(bean);
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

        mPrinter.addTexts(new String[]{"BATCH NO","000043"},new int[]{5,5},
                new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});

        mPrinter.addTexts(new String[]{"VOUCHER NO", "000509"}, new int[]{5, 5},
                new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
        mPrinter.addTexts(new String[]{"AUTH NO", "000786"}, new int[]{5, 5},
                new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
        mPrinter.addTexts(new String[]{"DATE/TIME", "2010/12/07 16:15:17"},
                new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
        mPrinter.addTexts(new String[]{"REF NO", "000001595276"},
                new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
        mPrinter.addTexts(new String[]{"2014/12/07 16:12:17", ""},
                new int[]{5, 5}, new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
        mPrinter.addTexts(new String[]{"AMOUNT:", ""}, new int[]{5, 5},
                new int[]{PrintStyle.Alignment.NORMAL, PrintStyle.Alignment.ALIGN_OPPOSITE});
        mPrinter.addText("RMB:249.00");
        mPrinter.addText("- - - - - - - - - - - - - -");
        mPrinter.addText("Please scan the QRCode for getting more information: ");
        mPrinter.addBarCode(this, Barcode1D.CODE_128.name(), 400, 100, "123456", PrintLine.CENTER);
        mPrinter.addText("Please scan the QRCode for getting more information:");
        mPrinter.addQRCode(300, Barcode2D.QR_CODE.name(), "123456", PrintLine.CENTER);
        mPrinter.setFooter(3);
        mPrinter.print(this);
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status, int type) {
        TRACE.d("onPrintFinished:" + isSuccess + "---" + "status:" + status + "----" + type);
    }

    @Override
    void onPrintError(boolean isSuccess, String status, int type) {
        TRACE.d("onPrintError:" + isSuccess + "---" + "status:" + status + "----" + type);
    }

    private String getText() {
        if (etText.getText() != null) {
            return etText.getText().toString();
        } else {
            return "";
        }
    }

    private int getFontSize() {
        if (etFontsize.getText() != null) {
            return Integer.parseInt(etFontsize.getText().toString());
        } else {
            return 14;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
