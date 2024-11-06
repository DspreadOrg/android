package com.dspread.demoui.interfaces;

import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

public interface PosUpdateCallback {
    void onReturnUpdateIPEKResult(boolean arg0);
    void onReturnSetMasterKeyResult(boolean isSuccess, Hashtable<String, String> result);
    void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result, Hashtable<String, String> checkValue);
    void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0);
    void onReturnCustomConfigResult(boolean isSuccess, String result);
}
