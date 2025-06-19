package com.dspread.pos.posAPI;

import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * 支付服务回调接口
 * 处理所有交易相关的回调方法
 */
public interface PaymentServiceCallback {
    
    // ==================== 交易核心回调 ====================
    
    /**
     * 交易结果回调
     */
    default void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {}
    
    /**
     * 交易请求结果回调
     */
    default void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {}
    
    /**
     * 交易取消回调
     */
    default void onTradeCancelled() {}
    
    // ==================== 交易流程回调 ====================
    
    /**
     * 请求设置金额
     */
    default void onRequestSetAmount() {}
    
    /**
     * 等待用户操作（插卡/刷卡/挥卡）
     */
    default void onRequestWaitingUser() {}
    
    /**
     * 请求时间
     */
    default void onRequestTime() {}
    
    /**
     * 请求选择EMV应用
     */
    default void onRequestSelectEmvApp(ArrayList<String> appList) {}
    
    /**
     * 请求在线处理
     */
    default void onRequestOnlineProcess(String tlv) {}
    
    /**
     * 请求批次数据
     */
    default void onRequestBatchData(String tlv) {}
    
    /**
     * 请求显示信息
     */
    default void onRequestDisplay(QPOSService.Display displayMsg) {}
    
    /**
     * 请求最终确认
     */
    default void onRequestFinalConfirm() {}
    
    /**
     * 请求服务器连接状态
     */
    default void onRequestIsServerConnected() {}
    
    // ==================== PIN相关回调 ====================
    
    /**
     * PIN请求结果
     */
    default void onQposRequestPinResult(List<String> dataList, int offlineTime) {}
    
    /**
     * PIN映射同步结果
     */
    default void onQposPinMapSyncResult(boolean isSuccess, boolean isNeedPin) {}
    
    /**
     * 请求设置PIN
     */
    default void onRequestSetPin(boolean isOfflinePin, int tryNum) {}
    
    /**
     * 请求设置PIN（无参数）
     */
    default void onRequestSetPin() {}
    
    /**
     * 返回获取PIN结果
     */
    default void onReturnGetPinResult(Hashtable<String, String> result) {}
    
    /**
     * 返回PIN输入结果
     */
    default void onReturnGetPinInputResult(int num) {}
    
    /**
     * 返回键盘输入结果
     */
    default void onReturnGetKeyBoardInputResult(String result) {}

    
    /**
     * 获取卡号结果
     */
    default void onGetCardNoResult(String cardNo) {}
    
    /**
     * 获取卡片信息结果
     */
    default void onGetCardInfoResult(Hashtable<String, String> cardInfo) {}
    
    /**
     * EMV ICC异常数据
     */
    default void onEmvICCExceptionData(String tlv) {}
    
    // ==================== 交易数据回调 ====================
    
    /**
     * 返回冲正数据
     */
    default void onReturnReversalData(String tlv) {}

    default void onError(QPOSService.Error errorState){}
}
