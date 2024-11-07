package com.dspread.demoui.interfaces;

import java.util.Hashtable;

/**
 * [一句话描述该类的功能]
 *
 * @author : [Qianmeng Chen]
 * @createTime : [2024/11/7 11:01]
 * @updateRemark : [These method is used to operation Mifare cards]
 */
public interface MifareCardOperationCallback {
    void onSearchMifareCardResult(Hashtable<String, String> arg0);
    void onFinishMifareCardResult(boolean arg0);
    void onVerifyMifareCardResult(boolean arg0);
    void onReadMifareCardResult(Hashtable<String, String> arg0);
    void onWriteMifareCardResult(boolean arg0);
    void onOperateMifareCardResult(Hashtable<String, String> arg0);
    void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3);
    void onReturnNFCApduResult(boolean arg0, String arg1, int arg2);
    void onReturnPowerOffNFCResult(boolean arg0);
}
