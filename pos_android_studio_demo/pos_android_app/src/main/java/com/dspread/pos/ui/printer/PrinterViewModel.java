package com.dspread.pos.ui.printer;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.dspread.pos.common.base.BaseAppViewModel;

import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.tatarka.bindingcollectionadapter2.ItemBinding;

public class PrinterViewModel extends BaseAppViewModel {
    public SingleLiveEvent<Intent> startActivityEvent = new SingleLiveEvent<>();
    public List<PrinterItemViewModel> items = new ArrayList<>();

    public ItemBinding<Object> itemBinding = ItemBinding.of(BR.item, R.layout.printer_work_item)
            .bindExtra(BR.viewModel, this);

    public void onItemClick(PrinterItemViewModel item) {
        // Jump to the corresponding interface based on different printing projects
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
        items.add(new PrinterItemViewModel(this, R.string.print_ticket, R.mipmap.function_all, PrintTicketActivity.class));
    }

}