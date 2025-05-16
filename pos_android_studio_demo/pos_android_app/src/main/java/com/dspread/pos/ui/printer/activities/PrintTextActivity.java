package com.dspread.pos.ui.printer.activities;

import android.os.Bundle;
import android.util.Log;

import com.dspread.pos.ui.printer.activities.base.PrintDialog;
import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityPrintTextBinding;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrinterDevice;

import me.tatarka.bindingcollectionadapter2.BR;

public class PrintTextActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, PrintTextViewModel> {
    private ActivityPrintTextBinding contentBinding;

    @Override
    public void initData() {
        super.initData();
//         加载内容布局并获取其binding
        contentBinding = ActivityPrintTextBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        // 将内容添加到容器中
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set("Print Text");
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_printer_base;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initViewObservable() {
        viewModel.showAlignDialog.observe(this, alignOptions -> {
            PrintDialog.setDialog(this, getString(R.string.set_align), alignOptions,
                new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {}

                    @Override
                    public void onConfirm(String content) {
                        String align = "CENTER";
                        if (getString(R.string.at_the_left).equals(content)) {
                            align = "LEFT";
                        } else if (getString(R.string.at_the_right).equals(content)) {
                            align = "RIGHT";
                        }
                        viewModel.setAlignment(align);
                    }
                });
        });

        viewModel.showFontStyleDialog.observe(this, fontStyles -> {
            PrintDialog.setDialog(this, getString(R.string.set_font_style), fontStyles,
                new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {}

                    @Override
                    public void onConfirm(String content) {
                        String style = "NORMAL";
                        if (getString(R.string.fontStyle_bold).equals(content)) {
                            style = "BOLD";
                        } else if (getString(R.string.fontStyle_italic).equals(content)) {
                            style = "ITALIC";
                        } else if (getString(R.string.fontStyle_bold_italic).equals(content)) {
                            style = "BOLD_ITALIC";
                        }
                        viewModel.setFontStyle(style);
                    }
                });
        });
        viewModel.showTextSizeDialog.observe(this, fontSize -> {
            PrintDialog.showSeekBarDialog(this, getResources().getString(R.string.set_font_size), 12, 40, contentBinding.txtSize, new PrintDialog.PrintClickListener() {
                @Override
                public void onCancel() {

                }

                @Override
                public void onConfirm(String str) {
                    Log.w("width", "width=" + str);
                    viewModel.setTextSize(str);
                }
            });
        });
        viewModel.showMaxHeightDialog.observe(this, textContentMaxHeight -> {
            PrintDialog.showSeekBarDialog(this, getResources().getString(R.string.content_maxHeight), 100, 13440, contentBinding.txtMaxheight, new PrintDialog.PrintClickListener() {
                @Override
                public void onCancel() {

                }
                @Override
                public void onConfirm(String str) {
                    viewModel.setMaxHeight(str);

                }
            });
        });
    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        viewModel.onPrintComplete(isSuccess, status);
    }
}

