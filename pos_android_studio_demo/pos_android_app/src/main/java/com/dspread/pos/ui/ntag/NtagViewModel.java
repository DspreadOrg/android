package com.dspread.pos.ui.ntag;

import android.app.Application;

import androidx.annotation.NonNull;

//import com.dspread.pos.log.TRACE;
import androidx.databinding.ObservableField;

import com.dspread.pos.common.base.BaseAppViewModel;

public class NtagViewModel extends BaseAppViewModel {
    // 双向绑定字段
    public final ObservableField<String> blockAddress = new ObservableField<>("0A");
    public final ObservableField<String> keyValue = new ObservableField<>("ffffffffffff");
    

    
    public NtagViewModel(@NonNull Application application) {
        super(application);
    }
    

}
