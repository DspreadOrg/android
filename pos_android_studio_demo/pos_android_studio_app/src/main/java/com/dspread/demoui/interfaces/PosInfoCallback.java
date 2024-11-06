package com.dspread.demoui.interfaces;

import java.util.Hashtable;

public interface PosInfoCallback {
    void onQposInfoResult(Hashtable<String, String> posInfoData) ;
    void onQposIdResult(Hashtable<String, String> posIdTable);
    void onRequestUpdateKey(String arg0);
    void onGetKeyCheckValue(Hashtable<String, String> checkValue);
}
