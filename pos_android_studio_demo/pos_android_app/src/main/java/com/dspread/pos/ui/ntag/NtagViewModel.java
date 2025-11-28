package com.dspread.pos.ui.ntag;

import android.app.Application;

import androidx.annotation.NonNull;

import com.dspread.pos.log.TRACE;
import androidx.databinding.ObservableField;

import com.dspread.pos.common.base.BaseAppViewModel;

public class NtagViewModel extends BaseAppViewModel {
    // 双向绑定字段
    public final ObservableField<String> blockAddress = new ObservableField<>("0A");
    public final ObservableField<String> keyValue = new ObservableField<>("ffffffffffff");
    

    
    public NtagViewModel(@NonNull Application application) {
        super(application);
    }
    
    // 按钮点击事件对应的业务逻辑方法
    public void pollNtag() {
        TRACE.i("Poll NTag clicked");
        // TODO: 实现扫描NTag卡片的逻辑
    }
    
    public void finishNtag() {
        TRACE.i("Finish NTag clicked");
        // TODO: 实现结束NTag操作的逻辑
    }
    
    public void writeNtag() {
        TRACE.i("Write NTag clicked, blockAddress: " + blockAddress.get() + ", keyValue: " + keyValue.get());
        // TODO: 实现写入NTag卡片的逻辑
    }
    
    public void readNtag() {
        TRACE.i("Read NTag clicked, blockAddress: " + blockAddress.get());
        // TODO: 实现读取NTag卡片的逻辑
    }
}
