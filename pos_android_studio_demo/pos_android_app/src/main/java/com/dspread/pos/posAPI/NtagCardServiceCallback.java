package com.dspread.pos.posAPI;

import java.util.Hashtable;

public interface NtagCardServiceCallback {
    /**
     * Poll on Ntag card result
     */
    default void onSearchMifareCardResult(Hashtable<String, String> arg0) {}

    /**
     * Finish Ntag card result
     */
    default void onFinishMifareCardResult(boolean arg0) {}

    /**
     * Write Ntag card result
     */
    default void writeMifareULData(String arg0){};

    /**
     * Read Ntag card result
     */
    default void getMifareReadData(Hashtable<String, String> arg0) {}

}
