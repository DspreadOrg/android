package com.dspread.pos.ui.printer;


import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos.ui.base.TitleProvider;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.FragmentPrinterHelperBinding;

public class PrinterHelperFragment extends BaseFragment<FragmentPrinterHelperBinding, PrinterViewModel> implements TitleProvider {
    @Override
    public int initContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return R.layout.fragment_printer_helper;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public String getTitle() {
        return "Print";
    }

    @Override
    public void initData() {
        int spanCount = Build.MODEL.equalsIgnoreCase("D70") ? 4 : 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        binding.printerWorkList.setLayoutManager(layoutManager);
    }

    @Override
    public void initViewObservable() {
        viewModel.startActivityEvent.observe(this, intent -> {
            startActivity(intent);
        });
    }
}