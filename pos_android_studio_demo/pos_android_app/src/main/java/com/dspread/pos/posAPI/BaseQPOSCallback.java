package com.dspread.pos.posAPI;

import com.dspread.xpos.QPOSService;

public interface BaseQPOSCallback {
    // 通用错误处理
    default void onError(QPOSService.Error error) {}
    // 通用状态更新
    default void onStatusUpdate(String status) {}
}