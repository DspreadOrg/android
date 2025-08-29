package com.dspread.pos.posAPI;

import android.bluetooth.BluetoothDevice;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Connection Service Callback Interface
 * Handle all connection-related callback methods
 */
public interface ConnectionServiceCallback {
    
    // ==================== Device Connection Status Callbacks ====================
    
    /**
     * Request QPOS Connection
     */
    default void onRequestQposConnected() {}
    
    /**
     * Request QPOS Disconnection
     */
    default void onRequestQposDisconnected() {}
    
    /**
     * Request No QPOS Detected
     */
    default void onRequestNoQposDetected() {}

}