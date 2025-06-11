package com.dspread.pos.posAPI;

import com.dspread.xpos.QPOSService;

public interface BaseQPOSCallback {
    // General Error Handling
    default void onError(QPOSService.Error error) {}
    // General status update
    default void onStatusUpdate(String status) {}
}