package com.dspread.pos.ui.printer;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.dspread.pos.common.base.BaseAppViewModel;

import com.dspread.pos.ui.printer.activities.BarCodeActivity;
import com.dspread.pos.ui.printer.activities.BitmapActivity;
import com.dspread.pos.ui.printer.activities.PrintFunctionMultiActivity;
import com.dspread.pos.ui.printer.activities.PrintTextActivity;
import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.ui.printer.activities.PrinterStatusActivity;
import com.dspread.pos.ui.printer.activities.QRCodeActivity;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

public class PrinterViewModel extends BaseAppViewModel {
    public SingleLiveEvent<Intent> startActivityEvent = new SingleLiveEvent<>();
    public List<PrinterItemViewModel> items = new ArrayList<>();
    
    // 添加 ItemBinding 配置
    public ItemBinding<Object> itemBinding = ItemBinding.of(BR.item, R.layout.printer_work_item)
            .bindExtra(BR.viewModel, this);

    public BindingCommand<PrinterItemViewModel> itemClickCommand = new BindingCommand<>(item -> {
        if (item.activityClass != null) {
            Intent intent = new Intent(getApplication(), item.activityClass);
            startActivityEvent.setValue(intent);
        }
    });

    public void onItemClick(PrinterItemViewModel item) {
        // 根据不同的打印项目跳转到对应的界面
        int title = item.titleId;
        int img = item.iconResId;
        TRACE.i("test print = "+title);
        TRACE.i("test image = "+img);
        if (item.activityClass != null) {
            Intent intent = new Intent(getApplication(), item.activityClass);
            startActivityEvent.setValue(intent);
        }
    }

    public PrinterViewModel(@NonNull Application application) {
        super(application);
        initPrinterItems();
    }

    private void initPrinterItems() {
        items.add(new PrinterItemViewModel(this,R.string.function_text, R.mipmap.function_text, PrintTextActivity.class));
        items.add(new PrinterItemViewModel(this,R.string.function_qrcode, R.mipmap.function_qr, QRCodeActivity.class));
        items.add(new PrinterItemViewModel(this,R.string.function_barcode, R.mipmap.function_barcode, BarCodeActivity.class));
        items.add(new PrinterItemViewModel(this,R.string.function_pic, R.mipmap.function_pic, BitmapActivity.class));
        items.add(new PrinterItemViewModel(this,R.string.function_multi, R.mipmap.function_multi, PrintFunctionMultiActivity.class));
        items.add(new PrinterItemViewModel(this, R.string.print_ticket, R.mipmap.function_all, PrintTicketActivity.class));
        items.add(new PrinterItemViewModel(this, R.string.get_printer_status, R.mipmap.function_status, PrinterStatusActivity.class));
    }

}