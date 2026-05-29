package com.dspread.pos.posAPI;

import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

public interface MifareServiceCallback {
    default void onActivateMifareCardResult(Hashtable<String, String> cardData){};
    default void onAuthenticateMifareCardResult(boolean success){};
    default void onReadMifareBlockResult(String data){};
    default void onWriteMifareBlockResult(boolean success){};
    default void onReadMifareValueResult(int value){};
    default void onWriteMifareValueResult(boolean success){};
    default void onIncreaseValueResult(boolean success){};
    default void onDecreaseValueResult(boolean success){};
    default void onTransferValueResult(boolean flag){};
    default void onDeactivateMifareCardResult(boolean success){};
    default void getMifareFastReadData(Hashtable<String, String> flag){};

    default void getMifareReadData(Hashtable<String, String> flag){};
    default void writeMifareULData(String flag){};
    default void onReturnPowerOnNFCResult(boolean result, QPOSService.CardsType cardType, String atr, int atrLen){};
    default void onReturnNFCApduResult(boolean result, String apdu, int apduLen){};
    default void onReturnPowerOffNFCResult(boolean result){};

}
