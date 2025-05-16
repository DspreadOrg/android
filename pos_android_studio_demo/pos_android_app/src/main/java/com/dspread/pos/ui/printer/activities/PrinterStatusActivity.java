package com.dspread.pos.ui.printer.activities;

import android.os.Bundle;
import android.view.View;

import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterStatusBinding;
import com.dspread.print.device.PrinterDevice;


public class PrinterStatusActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, PrinterStatusViewModel> {
    private ActivityPrinterStatusBinding contentBinding;

    @Override
    public void initData() {
        super.initData();
        contentBinding = ActivityPrinterStatusBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set(getString(R.string.get_printer_status));
        binding.btnPrint.setVisibility(View.GONE);
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
        viewModel.updatePrinterInfo(resultType, status);
    }
}