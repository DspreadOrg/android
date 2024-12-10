package com.dspread.demoui.activity.printer;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.action.printerservice.barcode.Barcode1D;
import com.dspread.demoui.R;
import com.dspread.demoui.ui.dialog.PrintDialog;
import com.dspread.demoui.utils.QRCodeUtil;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

import androidx.appcompat.app.AppCompatActivity;

public class BarCodeActivity extends BaseActivity implements View.OnClickListener {

    private ImageView ivBackTitle;
    private TextView tvTitle;
    private TextView brcodeTextContent;
    private LinearLayout brcodeContent;
    private TextView brcodeTextHeight;
    private LinearLayout brcodeHeight;
    private TextView brcodeTextWidth;
    private LinearLayout brcodeWidth;
    private TextView bcTextAlign;
    private LinearLayout brcodeAlign;
    private TextView bcTextGraylevel;
    private LinearLayout brcodeGraylevel;
    private ImageView brcodeImage;
    private TextView bcTextSpeedlevels;
    private LinearLayout brcodeSpeedlevels;
    private Button btnBrcodePrint;
    private LinearLayout brcodeSymbology;
    private TextView brcodeTextSymbology;
    private TextView brTextDensitylevel;
    private LinearLayout brcodeDensitylevel;
    private PrintLineStyle printLineStyle;
    private String alignText = "CENTER";
    private String brContent = "";
    private String brHeight = "";
    private String brWidth = "";
    private String brGraylevel = "";
    private String brSpeedlevel = "";
    private String brDensitylevel = "";
    private String brSymbology = "";
    private int printLineAlign;
    private int height;
    private int width;
    private int grayLevel;
    private int speedLevel;
    private int densityLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bar_code;
    }


    @Override
    protected void initView() {
        super.initView();
        ivBackTitle = findViewById(R.id.iv_back_title);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getText(R.string.print_brcode));
        brcodeTextContent = findViewById(R.id.brcode_text_content);
        brcodeContent = findViewById(R.id.brcode_content);
        brcodeTextHeight = findViewById(R.id.brcode_text_height);
        brcodeHeight = findViewById(R.id.brcode_height);
        brcodeTextWidth = findViewById(R.id.brcode_text_width);
        brcodeWidth = findViewById(R.id.brcode_width);
        bcTextAlign = findViewById(R.id.bc_text_align);
        brcodeAlign = findViewById(R.id.brcode_align);
        bcTextGraylevel = findViewById(R.id.bc_text_graylevel);
        brcodeGraylevel = findViewById(R.id.brcode_graylevel);
        brcodeImage = findViewById(R.id.brcode_image);
        btnBrcodePrint = findViewById(R.id.btn_brcode_print);
        bcTextSpeedlevels = findViewById(R.id.bc_text_speedlevels);
        brcodeSpeedlevels = findViewById(R.id.brcode_speedlevels);
        brTextDensitylevel = findViewById(R.id.br_text_densitylevel);
        brcodeDensitylevel = findViewById(R.id.brcode_densitylevel);
        brcodeSymbology = findViewById(R.id.brcode_symbology);
        brcodeSymbology.setVisibility(View.GONE);
        brcodeTextSymbology = findViewById(R.id.brcode_text_symbology);
        if ("mp600".equals(Build.MODEL)) {
            brcodeSpeedlevels.setVisibility(View.VISIBLE);
            brcodeDensitylevel.setVisibility(View.VISIBLE);
        } else {
            brcodeSpeedlevels.setVisibility(View.GONE);
            brcodeDensitylevel.setVisibility(View.GONE);
        }
        ivBackTitle.setOnClickListener(this);
        brcodeContent.setOnClickListener(this);
        brcodeHeight.setOnClickListener(this);
        brcodeWidth.setOnClickListener(this);
        brcodeAlign.setOnClickListener(this);
        brcodeGraylevel.setOnClickListener(this);
        btnBrcodePrint.setOnClickListener(this);
        brcodeSpeedlevels.setOnClickListener(this);
        brcodeDensitylevel.setOnClickListener(this);
        brcodeSymbology.setOnClickListener(this);
    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        btnBrcodePrint.setEnabled(true);
        Log.w("printResult", "boolean b==" + isSuccess);
        Log.w("printResult", "String s==" + status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.brcode_content:
                PrintDialog.printInputDialog(BarCodeActivity.this, getString(R.string.input_barcode), new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {
                        PrintDialog.printInputDialog.dismiss();
                    }

                    @Override
                    public void onConfirm(String str) {
                        brcodeTextContent.setText(str);
                        brContent = str;
                    }


                });
                break;
            case R.id.brcode_symbology:
//                CODE_128,
//                        CODABAR,
//                        CODE_39,
//                        EAN_8,
//                        EAN_13,
//                        UPC_A,
//                        UPC_E;
                final String[] symbology = {Barcode1D.CODE_128.name(), Barcode1D.CODABAR.name(), Barcode1D.CODE_39.name(), Barcode1D.EAN_8.name(),
                        Barcode1D.EAN_13.name(), Barcode1D.UPC_A.name(), Barcode1D.UPC_E.name()};
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.symbology_barcode), symbology, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        brcodeTextSymbology.setText(str);
                        brSymbology = str;
                    }
                });

                break;
            case R.id.brcode_height:
                PrintDialog.showSeekBarDialog(BarCodeActivity.this, getResources().getString(R.string.Barcode_height), 1, 200, brcodeTextHeight, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        Log.w("height", "heigt=" + str);
                        brcodeTextHeight.setText(str);
                        brHeight = str;

                    }
                });

                break;
            case R.id.brcode_width:
                PrintDialog.showSeekBarDialog(BarCodeActivity.this, getResources().getString(R.string.Barcode_width), 1, 600, brcodeTextWidth, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        Log.w("width", "width=" + str);
                        brcodeTextWidth.setText(str);
                        brWidth = str;

                    }
                });
                break;
            case R.id.brcode_align:
                final String[] alignStrings = new String[]{getResources().getString(R.string.at_the_left),
                        getResources().getString(R.string.at_the_right),
                        getResources().getString(R.string.at_the_center)};
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.set_align), alignStrings, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String content) {
                        bcTextAlign.setText(content);
                        alignText = content;
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
            case R.id.brcode_graylevel:
                final String[] graylevel = new String[]{"1", "2", "3", "4", "5"};
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.grayLevel), graylevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        bcTextGraylevel.setText(str);
                        brGraylevel = str;
                    }
                });
                break;
            case R.id.brcode_speedlevels:
                final String[] speedlevels = new String[]{"1", "2", "3", "4", "5"};
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.speedlevel), speedlevels, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        bcTextSpeedlevels.setText(str);
                        brSpeedlevel = str;
                    }
                });
                break;
            case R.id.brcode_densitylevel:
                final String[] densitylevels = new String[]{"1", "2", "3", "4", "5"};
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.density_level), densitylevels, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm(String str) {
                        brTextDensitylevel.setText(str);
                        brDensitylevel = str;
                    }
                });
                break;
            case R.id.btn_brcode_print:
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
                        if (!"".equals(brHeight)) {
                            height = Integer.parseInt(brHeight);
                        } else {
                            height = Integer.parseInt(brcodeTextHeight.getText().toString());
                        }
                        if (!"".equals(brWidth)) {
                            width = Integer.parseInt(brWidth);
                        } else {
                            width = Integer.parseInt(brcodeTextWidth.getText().toString());
                        }
                        if (!"".equals(brGraylevel)) {
                            grayLevel = Integer.parseInt(brGraylevel);
                        } else {
                            grayLevel = Integer.parseInt(bcTextGraylevel.getText().toString());
                        }
                        if (!"".equals(brSpeedlevel)) {
                            speedLevel = Integer.parseInt(brSpeedlevel);
                        } else {
                            speedLevel = Integer.parseInt(bcTextSpeedlevels.getText().toString());
                        }
                        if (!"".equals(brDensitylevel)) {
                            densityLevel = Integer.parseInt(brDensitylevel);
                        } else {
                            densityLevel = Integer.parseInt(brTextDensitylevel.getText().toString());
                        }
                        if ("".equals(brContent)) {
                            brContent = brcodeTextContent.getText().toString();
                        }
                        Bitmap bitmap = QRCodeUtil.getBarCodeBM(brContent, width, height);
                        brcodeImage.setImageBitmap(bitmap);
                        if ("mp600".equals(Build.MODEL)) {
                            mPrinter.setPrinterSpeed(speedLevel);
                            mPrinter.setPrinterDensity(densityLevel);
                        }
                        mPrinter.setPrintStyle(printLineStyle);
                        if ("".equals(brSymbology)) {
                            brSymbology = brcodeTextSymbology.getText().toString();
                        }
                        Log.w("brSymbology", "brSymbology==" + brSymbology);
                        mPrinter.setFooter(30);
                        mPrinter.printBarCode(this, brSymbology, width, height, brContent, printLineAlign);
                        btnBrcodePrint.setEnabled(false);
                    }
                } catch (Exception e) {
                    Log.e("Exception", "e=" + e);
                    Toast toast = Toast.makeText(BarCodeActivity.this, "Error: " + e, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
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