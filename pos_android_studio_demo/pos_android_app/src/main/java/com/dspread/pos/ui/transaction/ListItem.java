package com.dspread.pos.ui.transaction;

public class ListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_PAYMENT = 1;
    
    private int type;
    private String headerText;
    private Transaction payment;
    
    public ListItem(String headerText) {
        this.type = TYPE_HEADER;
        this.headerText = headerText;
    }
    
    public ListItem(Transaction payment) {
        this.type = TYPE_PAYMENT;
        this.payment = payment;
    }
    
    public int getType() { return type; }
    public String getHeaderText() { return headerText; }
    public Transaction getPayment() { return payment; }
}