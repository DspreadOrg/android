package com.dspread.pos.ui.printer.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.action.printerservice.barcode.Barcode2D;
import com.dspread.pos.ui.printer.activities.base.PrintDialog;
import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.pos_new_android_app.databinding.ActivityQrcodeBinding;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

public class QRCodeActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, QRCodeViewModel> {
    private ActivityQrcodeBinding contentBinding;

    @Override
    public void initData() {
        super.initData();
        contentBinding = ActivityQrcodeBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set("Print QRCode");
        contentBinding.qrcodeErrorLevel.setVisibility(View.GONE);
        contentBinding.linSpeedLevel.setVisibility(View.GONE);
        contentBinding.linDensityLevel.setVisibility(View.GONE);
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
        viewModel.showInputDialog.observe(this, s -> {
            PrintDialog.printInputDialog(this, getString(R.string.input_qrcode), new PrintDialog.PrintClickListener() {
                @Override
                public void onCancel() {}

                @Override
                public void onConfirm(String str) {
                    viewModel.content.set(str);
                }
            });
        });

        viewModel.showSizeDialog.observe(this, show -> {
            if (show) {
                PrintDialog.showSeekBarDialog(this, getString(R.string.size_qrcode), 
                    1, 600, contentBinding.qrcodeTextSize, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {}

                    @Override
                    public void onConfirm(String str) {
                        viewModel.size.set(str);
                    }
                });
            }
        });
        viewModel.showErrorLevelDialog.observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] errorLevel) {
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.QR_code_errorLevel), errorLevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        viewModel.errorLevel.set(str);
                    }
                });
            }
        });
        viewModel.showSizeDialog.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                PrintDialog.showSeekBarDialog(QRCodeActivity.this, getResources().getString(R.string.size_qrcode), 1, 600, contentBinding.qrcodeTextSize, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                       viewModel.size.set(str);
                    }
                });
            }
        });
        viewModel.showAlignDialog.observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] alignStrings) {
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.set_align), alignStrings, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String content) {
                        viewModel.align.set(content);
                    }
                });
            }
        });
        viewModel.showGrayLevelDialog.observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] graylevel) {
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.grayLevel), graylevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        viewModel.grayLevel.set(str);
                    }
                });
            }
        });
        viewModel.showSpeedLevelDialog.observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] speedlevel) {
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.speedlevel), speedlevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        viewModel.speedLevel.set(str);
                    }
                });
            }
        });
        viewModel.showDensityLevelDialog.observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] densitylevel) {
                PrintDialog.setDialog(QRCodeActivity.this, getString(R.string.density_level), densitylevel, new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm(String str) {
                        viewModel.densityLevel.set(str);
                    }
                });
            }
        });

    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        viewModel.onPrintComplete(isSuccess, status);
    }

}