package com.dspread.pos.ui.printer.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityBitmapBinding;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.bean.PrintLineStyle;
import com.dspread.print.widget.PrintLine;

public class BitmapActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, BitmapViewModel> {
    private ActivityBitmapBinding contentBinding;

    @Override
    public void initData() {
        super.initData();
        contentBinding = ActivityBitmapBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set("Print Bitmap");
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
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        viewModel.onPrintComplete(isSuccess, status);
    }
}