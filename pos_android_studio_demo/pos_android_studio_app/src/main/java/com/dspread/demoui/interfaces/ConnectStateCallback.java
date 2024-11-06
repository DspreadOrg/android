package com.dspread.demoui.interfaces;

import android.bluetooth.BluetoothDevice;

public interface ConnectStateCallback {

    void onRequestQposConnected();

    void onRequestQposDisconnected();

    void onRequestNoQposDetected();
    
}
