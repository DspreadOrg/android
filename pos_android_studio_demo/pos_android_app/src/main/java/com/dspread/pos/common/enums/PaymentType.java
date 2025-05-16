package com.dspread.pos.common.enums;

public enum PaymentType {
    GOODS("GOODS"),
    SERVICES("SERVICES"),
    CASH("CASH"),
    CASHBACK("CASHBACK"),
    PURCHASE_REFUND("PURCHASE_REFUND"),
    INQUIRY("INQUIRY"),
    TRANSFER("TRANSFER"),
    ADMIN("ADMIN"),
    PAYMENT("PAYMENT"),
    SALE("SALE"),
    CHANGE_PIN("CHANGE_PIN"),
    REFOUND("REFOUND");

    private final String value;

    PaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String[] getValues() {
        PaymentType[] types = PaymentType.values();
        String[] values = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            values[i] = types[i].getValue();
        }
        return values;
    }
}