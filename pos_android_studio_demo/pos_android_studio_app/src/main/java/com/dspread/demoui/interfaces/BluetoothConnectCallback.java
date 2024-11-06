package com.dspread.demoui.interfaces;

import android.bluetooth.BluetoothDevice;

public interface BluetoothConnectCallback {
    void onRequestDeviceScanFinished();

    void onDeviceFound(BluetoothDevice device);

    void onRequestQposConnected();

    void onRequestQposDisconnected();

    void onRequestNoQposDetected();
}
