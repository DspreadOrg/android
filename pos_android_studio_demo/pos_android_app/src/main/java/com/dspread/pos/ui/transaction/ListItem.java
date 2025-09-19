package com.dspread.pos.ui.transaction;

import java.math.BigDecimal;

public class ListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PAYMENT = 1;
    
    private int type;
    private String headerText;
    private Transaction payment;

    public BigDecimal getMonthTotalAmount() {
        return monthTotalAmount;
    }

    private BigDecimal monthTotalAmount;
    public ListItem(String headerText) {
        this.type = TYPE_HEADER;
        this.headerText = headerText;
    }


    public ListItem(String headerText, BigDecimal monthTotalAmount) {
        this.type = TYPE_HEADER;
        this.headerText = headerText;
        this.monthTotalAmount = monthTotalAmount;
    }
    
    public ListItem(Transaction payment) {
        this.type = TYPE_PAYMENT;
        this.payment = payment;
    }
    
    public int getType() { return type; }
    public String getHeaderText() { return headerText; }
    public Transaction getPayment() { return payment; }
}