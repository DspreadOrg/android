package com.dspread.pos.posAPI;

import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

public interface NtagCardServiceCallback {
    /**
     * Poll on Ntag card result
     */
    default void onSearchMifareCardResult(boolean result, QPOSService.CardsType cardType, String atr, int atrLen) {}

    /**
     * Finish Ntag card result
     */
    default void onFinishMifareCardResult(boolean arg0) {}

    /**
     * Write Ntag card result
     */
    default void writeMifareULData(boolean arg0){};

    /**
     * Read Ntag card result
     */
    default void getMifareReadData(boolean flag, Hashtable<String, String> result) {}

}
