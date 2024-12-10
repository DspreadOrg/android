package com.dspread.demoui.activity.printer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.action.printerservice.barcode.Barcode2D;
import com.dspread.demoui.R;
import com.dspread.demoui.ui.dialog.PrintDialog;
import com.dspread.demoui.utils.QRCodeUtil;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

public class QRCodeActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivBackTitle;
    private TextView tvTitle;
    private TextView qrcodeTextContent;
    private LinearLayout qrcodeContent;
    private TextView qrcodeTextSize;
    private LinearLayout qrcodeSize;
    private TextView qrTextAlign;
    private LinearLayout qrcodeAlign;
    private TextView qrTextGraylevel;
    private LinearLayout qrcodeGraylevel;
    private ImageView qrcodeImage;
    private Button btnQrcodePrint;
    private TextView qrTextSpeedlevel;
    private LinearLayout qrcodeSpeedlevel;
    private TextView qrTextDensitylevel;
    private LinearLayout qrcodeDensitylevel;
    private LinearLayout qrcodeErrorLevel;
    private TextView qrcodeTextErrorLevel;
    private PrintLineStyle printLineStyle;
    private String qrCodeSize = "";
    private int qrSize;
    private String alignText = "CENTER";
    private int printLineAlign;
    private String qrContent = "";
    private String qrGraylevel = "";
    private int grayLevel;
    private String qrSpeedlevel = "";
    private int speedLevel;
    private String qrDensitylevel = "";
    private int densityLevel;
    private String qrErrorLevel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qrcode;
    }

    @Override
    protected void initView() {
        super.initView();
        ivBackTitle = findViewById(R.id.iv_back_title);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.print_qrcode));
        qrcodeTextContent = findViewById(R.id.qrcode_text_content);
        qrcodeContent = findViewById(R.id.qrcode_content);
        qrcodeTextSize = findViewById(R.id.qrcode_text_size);
        qrcodeSize = findViewById(R.id.qrcode_size);
        qrTextAlign = findViewById(R.id.qr_text_align);
        qrcodeAlign = findViewById(R.id.qrcode_align);
        qrTextGraylevel = findViewById(R.id.qr_text_graylevel);
        qrcodeGraylevel = findViewById(R.id.qrcode_graylevel);
        qrcodeImage = findViewById(R.id.qrcode_image);
        btnQrcodePrint = findViewById(R.id.btn_qrcode_print);
        qrTextSpeedlevel = findViewById(R.id.qr_text_speedlevel);
        qrcodeSpeedlevel = findViewById(R.id.qrcode_speedlevel);
        qrTextDensitylevel = findViewById(R.id.qr_text_densitylevel);
        qrcodeDensitylevel = findViewById(R.id.qrcode_densitylevel);
        qrcodeErrorLevel = findViewById(R.id.qrcode_errorLevel);
        qrcodeTextErrorLevel = findViewById(R.id.qrcode_text_errorLevel);
        qrcodeSpeedlevel.setVisibility(View.GONE);
        qrcodeErrorLevel.setVisibility(View.GONE);
        qrcodeDensitylevel.setVisibility(View.GONE);
        ivBackTitle.setOnClickListener(this);
        qrcodeContent.setOnClickListener(this);
        qrcodeSize.setOnClickListener(this);
        qrcodeAlign.setOnClickListener(this);
        qrcodeGraylevel.setOnClickListener(this);
        btnQrcodePrint.setOnClickListener(this);
        qrcodeSpeedlevel.setOnClickListener(this);
        qrcodeDensitylevel.setOnClickListener(this);
        qrcodeErrorLevel.setOnClickListener(this);
    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        btnQrcodePrint.setEnabled(true);
        Log.w("printResult", "boolean b==" + isSuccess);
        Log.w("printResult", "String s==" + status);
        Log.w("printResult", "resultType==" + resultType.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.qrcode_content:
                PrintDialog.printInputDialog(QRCodeActivity.this, getString(R.string.input_qrcode), new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {
                        PrintDialog.printInputDialog.dismiss();
                    }

                    @Override
                    public void onConfirm(String str) {
                        qrContent = str;
                        qrcodeTextContent.setText(str);
                        Log.w("str", "str==" + str);
                    }


                });
                break;
            case R.id.qrcode_errorLevel:
//                L,
//                        M,
//                        Q,
//                        H;
                final String[] errorLevel = {Barcode2D.ErrorLevel.L.name(), Barcode2D.ErrorLevel.M.name(),
                        Barcode2D.ErrorLevel.Q.name(), Barcode2D.ErrorLevel.H.name()};
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.QR_code_errorLevel), errorLevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        qrcodeTextErrorLevel.setText(str);
                        qrErrorLevel = str;
                    }
                });
                break;
            case R.id.qrcode_size:
                PrintDialog.showSeekBarDialog(QRCodeActivity.this, getResources().getString(R.string.size_qrcode), 1, 600, qrcodeTextSize, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        qrCodeSize = str;
                        qrcodeTextSize.setText(str);
                    }
                });
                break;
            case R.id.qrcode_align:
                final String[] alignStrings = new String[]{getResources().getString(R.string.at_the_left),
                        getResources().getString(R.string.at_the_right),
                        getResources().getString(R.string.at_the_center)};
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.set_align), alignStrings, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String content) {
                        alignText = content;
                        qrTextAlign.setText(content);
                        if ("LEFT".equals(content)) {
                            alignText = "LEFT";
                        } else if ("RIGHT".equals(content)) {
                            alignText = "RIGHT";
                        } else if ("CENTER".equals(content)) {
                            alignText = "CENTER";
                        }
                    }
                });
                break;
            case R.id.qrcode_graylevel:
                final String[] graylevel = new String[]{"1", "2", "3", "4", "5"};
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.grayLevel), graylevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        qrGraylevel = str;
                        qrTextGraylevel.setText(str);
                    }
                });
                break;
            case R.id.qrcode_speedlevel:
                final String[] speedlevel = new String[]{"1", "2", "3", "4", "5"};
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.speedlevel), speedlevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        qrSpeedlevel = str;
                        qrTextSpeedlevel.setText(str);
                    }
                });
                break;
            case R.id.qrcode_densitylevel:
                final String[] densitylevel = new String[]{"1", "2", "3", "4", "5"};
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.density_level), densitylevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        qrDensitylevel = str;
                        qrTextDensitylevel.setText(str);
                    }
                });
                break;

            case R.id.btn_qrcode_print:
                try {
                    if (mPrinter != null) {
                        printLineStyle = new PrintLineStyle();
                        if (!"".equals(alignText)) {
                            if ("LEFT".equals(alignText)) {
                                printLineAlign = PrintLine.LEFT;
                            } else if ("RIGHT".equals(alignText)) {
                                printLineAlign = PrintLine.RIGHT;
                            } else if ("CENTER".equals(alignText)) {
                                printLineAlign = PrintLine.CENTER;
                            }
                        }

                        if (!"".equals(qrCodeSize)) {
                            qrSize = Integer.parseInt(qrCodeSize);
                        } else {
                            qrSize = Integer.parseInt(qrcodeTextSize.getText().toString());
                        }
                        if (!"".equals(qrGraylevel)) {
                            grayLevel = Integer.parseInt(qrGraylevel);
                        } else {
                            grayLevel = Integer.parseInt(qrTextGraylevel.getText().toString());
                        }

                        if (!"".equals(qrSpeedlevel)) {
                            speedLevel = Integer.parseInt(qrSpeedlevel);
                        } else {
                            speedLevel = Integer.parseInt(qrTextSpeedlevel.getText().toString());
                        }

                        if (!"".equals(qrDensitylevel)) {
                            densityLevel = Integer.parseInt(qrDensitylevel);
                        } else {
                            densityLevel = Integer.parseInt(qrTextDensitylevel.getText().toString());
                        }
                        if ("".equals(qrContent)) {
                            qrContent = qrcodeTextContent.getText().toString();
                        }
                        Bitmap bitmap = QRCodeUtil.getQrcodeBM(qrContent, qrSize);
                        qrcodeImage.setImageBitmap(bitmap);
                        mPrinter.setPrintStyle(printLineStyle);
                        Log.w("qrErrorLevel", "qrErrorLevel==" + qrErrorLevel);
                        if ("".equals(qrErrorLevel)) {
                            qrErrorLevel = qrcodeTextErrorLevel.getText().toString();
                        }
                        mPrinter.setFooter(30);
                        mPrinter.printQRCode(this, qrErrorLevel, qrSize, qrContent, printLineAlign);
                        btnQrcodePrint.setEnabled(false);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);

                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPrinter != null) {
            mPrinter.close();
        }
    }
}