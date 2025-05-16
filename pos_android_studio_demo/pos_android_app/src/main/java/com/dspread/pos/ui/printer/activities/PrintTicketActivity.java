package com.dspread.pos.ui.printer.activities;

import android.os.Bundle;
import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityPrintTicketBinding;
import com.dspread.pos_new_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrinterDevice;


public class PrintTicketActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, PrintTicketViewModel> {
    private ActivityPrintTicketBinding contentBinding;

    @Override
    public void initData() {
        super.initData();
        contentBinding = ActivityPrintTicketBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set(getString(R.string.print_ticket));
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