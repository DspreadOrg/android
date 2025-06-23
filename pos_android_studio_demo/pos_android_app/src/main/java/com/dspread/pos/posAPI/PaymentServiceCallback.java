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
     * Transaction result callback
     */
    default void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {}
    
    /**
     * Transaction request result callback
     */
    default void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {}
    
    /**
     * Transaction cancelled callback
     */
    default void onTradeCancelled() {}
    
    // ==================== Transaction Process Callbacks ====================
    
    /**
     * Request to set amount
     */
    default void onRequestSetAmount() {}
    
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
     * Request batch data
     */
    default void onRequestBatchData(String tlv) {}
    
    /**
     * Request to display message
     */
    default void onRequestDisplay(QPOSService.Display displayMsg) {}
    
    /**
     * Request final confirmation
     */
    default void onRequestFinalConfirm() {}
    
    /**
     * Request server connection status
     */
    default void onRequestIsServerConnected() {}
    
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
     * Return get PIN result
     */
    default void onReturnGetPinResult(Hashtable<String, String> result) {}
    
    /**
     * Return PIN input result
     */
    default void onReturnGetPinInputResult(int num) {}
    
    /**
     * Return keyboard input result
     */
    default void onReturnGetKeyBoardInputResult(String result) {}
    
    /**
     * Get card number result
     */
    default void onGetCardNoResult(String cardNo) {}
    
    /**
     * Get card information result
     */
    default void onGetCardInfoResult(Hashtable<String, String> cardInfo) {}
    
    /**
     * EMV ICC exception data
     */
    default void onEmvICCExceptionData(String tlv) {}
    
    // ==================== Transaction Data Callbacks ====================
    
    /**
     * Return reversal data
     */
    default void onReturnReversalData(String tlv) {}

    /**
     * Error callback
     */
    default void onError(QPOSService.Error errorState){}
}
