package com.dspread.demoui.activity.printer;

import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.action.printerservice.PrintStyle;
import com.dspread.demoui.R;
import com.dspread.demoui.ui.dialog.PrintDialog;
import com.dspread.helper.printer.PrinterClass;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterInitListener;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

public class PrintTextActivity extends BaseActivity implements View.OnClickListener {
    private ImageView ivBackTitle;
    private TextView tvTitle;
    private TextView textTextAlign;
    private LinearLayout layoutSetAlign;
    private TextView textSetFontStyle;
    private LinearLayout layoutSetFontStyle;
    private TextView textTextSize;
    private LinearLayout layoutTextSize, layoutMaxHeight;
    private LinearLayout textSet;
    private TextView editText;
    private Button btnPrint;
    private LinearLayout textAll;
    private PrintLineStyle printLineStyle;
    private String alignText = "CENTER";
    private String fontText = "";
    private String textSizeStr = "";
    private String textMaxHeightStr = "";
    private int textSize;
    private TextView textContentMaxHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        printLineStyle = new PrintLineStyle();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_print_text;
    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        btnPrint.setEnabled(true);
        Log.w("printResult", "boolean b==" + isSuccess);
        Log.w("printResult", "String s==" + status);
        Log.w("printResult", "resultType==" + resultType.toString());
    }

    @Override
    protected void initView() {
        super.initView();
        ivBackTitle = findViewById(R.id.iv_back_title);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.print_text));
        textTextAlign = findViewById(R.id.text_text_align);
        layoutSetAlign = findViewById(R.id.layout_setAlign);
        textSetFontStyle = findViewById(R.id.text_setFontStyle);
        layoutSetFontStyle = findViewById(R.id.layout_setFontStyle);
        textTextSize = findViewById(R.id.text_text_size);
        layoutTextSize = findViewById(R.id.Layout_textSize);
        layoutMaxHeight = findViewById(R.id.Layout_maxHeight);
        if (Build.MODEL.equalsIgnoreCase("D70")) {
            layoutMaxHeight.setVisibility(View.GONE);
        }
        if (Build.MODEL.equalsIgnoreCase("D30M")) {
            layoutMaxHeight.setVisibility(View.GONE);
            layoutSetFontStyle.setVisibility(View.GONE);
        }
        textContentMaxHeight = findViewById(R.id.text_content_maxHeight);
        textSet = findViewById(R.id.text_set);
        editText = findViewById(R.id.edit_text);
        btnPrint = findViewById(R.id.btn_Print);
        textAll = findViewById(R.id.text_all);
        ivBackTitle.setOnClickListener(this);
        layoutSetAlign.setOnClickListener(this);
        layoutSetFontStyle.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        layoutTextSize.setOnClickListener(this);
        layoutMaxHeight.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.layout_setAlign:
                final String[] alignStrings = new String[]{getResources().getString(R.string.at_the_left), getResources().getString(R.string.at_the_right), getResources().getString(R.string.at_the_center)};
                PrintDialog.setDialog(this, getString(R.string.set_align), alignStrings, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String content) {
                        textTextAlign.setText(content);
                        alignText = content;
                        if (getString(R.string.at_the_left).equals(content)) {
                            alignText = "LEFT";
                        } else if (getString(R.string.at_the_right).equals(content)) {
                            alignText = "RIGHT";
                        } else if (getString(R.string.at_the_center).equals(content)) {
                            alignText = "CENTER";
                        }
                    }
                });

                break;
            case R.id.layout_setFontStyle:
                final String[] fontStrings = new String[]{getResources().getString(R.string.fontStyle_normal), getResources().getString(R.string.fontStyle_bold), getResources().getString(R.string.fontStyle_italic), getResources().getString(R.string.fontStyle_bold_italic)};
                PrintDialog.setDialog(this, getString(R.string.set_font_style), fontStrings, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onConfirm(String content) {
                        textSetFontStyle.setText(content);
                        fontText = content;
                        if (getString(R.string.fontStyle_normal).equals(content)) {
                            textSetFontStyle.setText(content);
                            fontText = "NORMAL";
                        } else if (getString(R.string.fontStyle_bold).equals(content)) {
                            textSetFontStyle.setText(content);
                            fontText = "BOLD";
                        } else if (getString(R.string.fontStyle_italic).equals(content)) {
                            textSetFontStyle.setText(content);
                            fontText = "ITALIC";
                        } else if (getString(R.string.fontStyle_bold_italic).equals(content)) {
                            textSetFontStyle.setText(content);
                            fontText = "BOLD_ITALIC";
                        }
                    }
                });

                break;
            case R.id.btn_Print:
                try {
                    if (mPrinter != null) {
                        if (!"".equals(alignText)) {
                            if ("LEFT".equals(alignText)) {
                                printLineStyle.setAlign(PrintLine.LEFT);
                            } else if ("RIGHT".equals(alignText)) {
                                printLineStyle.setAlign(PrintLine.RIGHT);
                            } else if ("CENTER".equals(alignText)) {
                                printLineStyle.setAlign(PrintLine.CENTER);
                            }
                        }
                        if (!"".equals(fontText)) {
                            if ("NORMAL".equals(fontText)) {
                                printLineStyle.setFontStyle(PrintStyle.FontStyle.NORMAL);
                                printLineStyle.setFontStyle(PrintStyle.Key.ALIGNMENT);
                            } else if ("BOLD".equals(fontText)) {
                                printLineStyle.setFontStyle(PrintStyle.FontStyle.BOLD);
                            } else if ("ITALIC".equals(fontText)) {
                                printLineStyle.setFontStyle(PrintStyle.FontStyle.ITALIC);

                            } else if ("BOLD_ITALIC".equals(fontText)) {
                                printLineStyle.setFontStyle(PrintStyle.FontStyle.BOLD_ITALIC);
                            }
                        }
                        if ("".equals(textSizeStr)) {
                            textSizeStr = textTextSize.getText().toString();
                            textSize = Integer.parseInt(textSizeStr);
                        } else {
                            textSize = Integer.parseInt(textSizeStr);
                        }
                        printLineStyle.setFontSize(textSize);
                        mPrinter.setPrintStyle(printLineStyle);
                        mPrinter.setFooter(30);
                        mPrinter.printText(getString(R.string.text_print));
                        btnPrint.setEnabled(false);
                    }
                } catch (RemoteException e) {
                    throw new RuntimeException(e);

                }
                break;
            case R.id.Layout_textSize:
                PrintDialog.showSeekBarDialog(this, getResources().getString(R.string.set_font_size), 12, 40, textTextSize, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        Log.w("width", "width=" + str);
                        textTextSize.setText(str);
                        textSizeStr = str;

                    }
                });

                break;
            case R.id.Layout_maxHeight:
                //The max height should range from 100 to 13440。
                PrintDialog.showSeekBarDialog(this, getResources().getString(R.string.content_maxHeight), 100, 13440, textContentMaxHeight, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }
                    @Override
                    public void onConfirm(String str) {
                        textContentMaxHeight.setText(str);
                        textMaxHeightStr = str;

                    }
                });

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

