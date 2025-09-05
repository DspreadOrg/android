package com.dspread.pos.posAPI;

import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Payment Service Callback Interface
 * Handle all transaction-related callback methods
 */
public interface PaymentServiceCallback {
    
    // ==================== Core Transaction Callbacks ====================
    
    /**
     * Waiting for user operation (insert/swipe/tap card)
     */
    default void onRequestWaitingUser() {}
    
    /**
     * Request time
     */
    default void onRequestTime() {}
    
    /**
     * Request to select EMV application
     */
    default void onRequestSelectEmvApp(ArrayList<String> appList) {}
    
    /**
     * Request online processing
     */
    default void onRequestOnlineProcess(String tlv) {}
    
    /**
     * Request to display message
     */
    default void onRequestDisplay(QPOSService.Display displayMsg) {}
    
    // ==================== PIN Related Callbacks ====================
    
    /**
     * PIN request result
     */
    default void onQposRequestPinResult(List<String> dataList, int offlineTime) {}
    
    /**
     * Request to set PIN
     */
    default void onRequestSetPin(boolean isOfflinePin, int tryNum) {}
    
    /**
     * Request to set PIN (no parameters)
     */
    default void onRequestSetPin() {}
    
    /**
     * Return PIN input result
     */
    default void onReturnGetPinInputResult(int num) {}
    
    /**
     * Get card information result
     */
    default void onGetCardInfoResult(Hashtable<String, String> cardInfo) {}

    default void onTransactionCompleted(PaymentResult result) {}
    default void onTransactionFailed(String errorMessage,String data) {}
    default void onTransactionResult(PaymentResult result) {}
    default void onReturnCardInserted(){}
}
