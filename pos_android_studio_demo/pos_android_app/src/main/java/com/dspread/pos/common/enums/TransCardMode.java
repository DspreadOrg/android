package com.dspread.pos.common.enums;

import com.dspread.xpos.QPOSService;

public enum TransCardMode {
    SWIPE_TAP_INSERT_CARD_NOTUP(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP),SWIPE_TAP_INSERT_CARD(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD), ONLY_INSERT_CARD(QPOSService.CardTradeMode.ONLY_INSERT_CARD), ONLY_SWIPE_CARD(QPOSService.CardTradeMode.ONLY_SWIPE_CARD), TAP_INSERT_CARD(QPOSService.CardTradeMode.TAP_INSERT_CARD),
    TAP_INSERT_CARD_NOTUP(QPOSService.CardTradeMode.TAP_INSERT_CARD_NOTUP), UNALLOWED_LOW_TRADE(QPOSService.CardTradeMode.UNALLOWED_LOW_TRADE), SWIPE_INSERT_CARD(QPOSService.CardTradeMode.SWIPE_INSERT_CARD), SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE),
    SWIPE_TAP_INSERT_CARD_NOTUP_UNALLOWED_LOW_TRADE(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP_UNALLOWED_LOW_TRADE), ONLY_TAP_CARD(QPOSService.CardTradeMode.ONLY_TAP_CARD),
    ONLY_TAP_CARD_QF(QPOSService.CardTradeMode.ONLY_TAP_CARD_QF),
    SWIPE_TAP_INSERT_CARD_DOWN(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_DOWN), SWIPE_INSERT_CARD_UNALLOWED_LOW_TRADE(QPOSService.CardTradeMode.SWIPE_INSERT_CARD_UNALLOWED_LOW_TRADE),
    SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE_NEW(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_UNALLOWED_LOW_TRADE_NEW), ONLY_INSERT_CARD_NOPIN(QPOSService.CardTradeMode.ONLY_INSERT_CARD_NOPIN),
    SWIPE_TAP_INSERT_CARD_NOTUP_DELAY(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP_DELAY);

    protected QPOSService.CardTradeMode cardTradeModeValue;

    public QPOSService.CardTradeMode getCardTradeModeValue() {
        return cardTradeModeValue;
    }

    TransCardMode(QPOSService.CardTradeMode i) {
        this.cardTradeModeValue = i;
    }

    public static String[] getCardTradeModes() {
        TransCardMode[] types = TransCardMode.values();
        String[] values = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            values[i] = types[i].getCardTradeModeValue().name();
        }
        return values;
    }
}
