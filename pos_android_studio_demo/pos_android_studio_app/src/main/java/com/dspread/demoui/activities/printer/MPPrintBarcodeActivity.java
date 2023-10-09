package com.dspread.demoui.activities.printer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.action.printerservice.barcode.Barcode1D;
import com.dspread.demoui.R;
import com.dspread.demoui.utils.QRCodeUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.view.BitmapPrintLine;
import com.dspread.demoui.view.PrintLine;
import com.dspread.demoui.view.PrintLayout;
import com.dspread.demoui.view.TextPrintLine;

public class MPPrintBarcodeActivity extends CommonActivity {
    private EditText etBarcode, etHeiht, etWidth;
    private Spinner mSpinnerSpGreyLevel, mSpinnerSpAlignment;
    private int bitMapPosition;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(getString(R.string.mp_barcode_print));
        mSpinnerSpGreyLevel = findViewById(R.id.sp_grey_level);
        etBarcode = findViewById(R.id.et_barcode_content);
        etHeiht = findViewById(R.id.et_barcode_height);
        etWidth = findViewById(R.id.et_barcode_factor);
        initView();
    }

    private void initView() {
        findViewById(R.id.align_area).setVisibility(View.VISIBLE);
        mSpinnerSpAlignment = findViewById(R.id.sp_alignment);
        mSpinnerSpAlignment.setSelection(1);
        mSpinnerSpAlignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                if (item.equals("Left")) {
                    setPosition(0);
                } else if (item.equals("Right")) {
                    setPosition(2);
                } else if (item.equals("Center")) {
                    setPosition(1);
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
        return R.layout.activity_barcode;
    }

    @Override
    int printTest() throws RemoteException {
        String data = getText(etBarcode);
        int height = Integer.parseInt(getText(etHeiht));
        int width = Integer.parseInt(getText(etWidth));
        //pos.printBarCode(width, height, data);
        PrintLayout printLayout = new PrintLayout(MPPrintBarcodeActivity.this);
        Bitmap barcodeBM = QRCodeUtil.getBarCodeBM(data, width, height);
        switch (getPosition()) {
            case 0:
                bitMapPosition = PrintLine.LEFT;
                break;
            case 1:
                bitMapPosition = PrintLine.CENTER;
                break;
            case 2:
                bitMapPosition = PrintLine.RIGHT;
                break;
        }
        BitmapPrintLine bitmapPrintLine2 = new BitmapPrintLine(barcodeBM, bitMapPosition);
        printLayout.addBitmap(bitmapPrintLine2);
        TextPrintLine textPrintLine = new TextPrintLine();
        textPrintLine.setContent("\n");
        textPrintLine.setPosition(PrintLine.CENTER);
        printLayout.addText(textPrintLine);
        Bitmap bitmap = printLayout.viewToBitmap();
        //mPrinter.printBitmap(this, bitmap);//
        mPrinter.printBarCode(this, Barcode1D.CODE_128.name(), width, height, data, bitMapPosition);
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status, int type) {
        TRACE.d("onPrintFinished:" + isSuccess + "---" + "status:" + status);
        if (status != null) {
            TRACE.d("ssss" + status);
        }
    }
    @Override
    void onPrintError(boolean isSuccess, String status, int type) {
    }

    private String getText(EditText etText) {
        if (etText.getText() != null) {
            return etText.getText().toString().trim();
        } else {
            return "";
        }
    }
}
