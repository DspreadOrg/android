package com.dspread.pos.ui.printer;

import android.app.Activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.databinding.ObservableField;

import me.goldze.mvvmhabit.base.ItemViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PrinterItemViewModel extends ItemViewModel<PrinterViewModel> {
    @StringRes
    public final int titleId;
    @DrawableRes
    public final int iconResId;
    public final Class<? extends Activity> activityClass;

    public ObservableField<String> title = new ObservableField<>();

    // 添加点击命令
    public BindingCommand itemClick = new BindingCommand(() -> {
        // 通知 ViewModel 处理点击事件
        viewModel.onItemClick(this);
    });

    public PrinterItemViewModel(@NonNull PrinterViewModel viewModel, int titleId, int iconResId, Class<? extends Activity> activityClass) {
        super(viewModel);
        this.titleId = titleId;
        this.iconResId = iconResId;
        this.activityClass = activityClass;
    }
}