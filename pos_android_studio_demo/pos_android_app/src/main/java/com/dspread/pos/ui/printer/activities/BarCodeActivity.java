package com.dspread.pos.ui.printer.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.dspread.pos.ui.printer.activities.base.PrintDialog;
import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityBarCodeBinding;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrinterDevice;

import me.tatarka.bindingcollectionadapter2.BR;

public class BarCodeActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, BarCodeViewModel> {
    private ActivityBarCodeBinding contentBinding;

    @Override
    public void initData() {
        super.initData();
        contentBinding = ActivityBarCodeBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set("Print BarCode");
        if ("mp600".equals(Build.MODEL)) {
            contentBinding.linSleedLevel.setVisibility(View.VISIBLE);
            contentBinding.linDensityLevel.setVisibility(View.VISIBLE);
        } else {
            contentBinding.linSleedLevel.setVisibility(View.GONE);
            contentBinding.linDensityLevel.setVisibility(View.GONE);
        }
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
        viewModel.showInputDialog.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                PrintDialog.printInputDialog(BarCodeActivity.this, getString(R.string.input_barcode), new PrintDialog.PrintClickListener() {
                    @Override
                    public void onCancel() {
                        PrintDialog.printInputDialog.dismiss();
                    }

                    @Override
                    public void onConfirm(String str) {
                        viewModel.content.set(str);
                    }
                });
            }
        });

        viewModel.showOptionsDialog.observe(this, symbology ->
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.symbology_barcode), symbology, new PrintDialog.PrintClickListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onConfirm(String str) {
                viewModel.symbology.set(str);
            }
        }));
        viewModel.showHeightDialog.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    PrintDialog.showSeekBarDialog(BarCodeActivity.this, getResources().getString(R.string.Barcode_height), 1, 200, contentBinding.txtHeight, new PrintDialog.PrintClickListener() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onConfirm(String str) {
                            Log.w("height", "heigt=" + str);
                            viewModel.height.set(str);
                        }
                    });

                }
            }
        });
        viewModel.showWidthDialog.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    PrintDialog.showSeekBarDialog(BarCodeActivity.this, getApplication().getResources().getString(R.string.Barcode_width), 1, 600, contentBinding.txtWidth, new PrintDialog.PrintClickListener() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onConfirm(String str) {
                            Log.w("width", "width=" + str);
                            viewModel.width.set(str);
                        }
                    });
                }
            }
        });

        viewModel.showAlignDialog.observe(this, new Observer<String[]>() {
            @Override
            public void onChanged(String[] alignStrings) {
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.set_align), alignStrings, new PrintDialog.PrintClickListener() {
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
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.grayLevel), graylevel, new PrintDialog.PrintClickListener() {
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
            public void onChanged(String[] speedlevels) {
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.speedlevel), speedlevels, new PrintDialog.PrintClickListener() {
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
            public void onChanged(String[] densitylevels) {
                PrintDialog.setDialog(BarCodeActivity.this, getString(R.string.density_level), densitylevels, new PrintDialog.PrintClickListener() {
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