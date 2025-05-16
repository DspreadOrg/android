package com.dspread.pos.ui.payment;

public class PaymentModel {
    private String date;
    private String amount;
    private String transType;
    private String transCurrencyCode;
    private String tvr;

    private String cardNo;
    private String cvmResults;
    private String cidData;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTransCurrencyCode() {
        return transCurrencyCode;
    }

    public void setTransCurrencyCode(String transCurrencyCode) {
        this.transCurrencyCode = transCurrencyCode;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCidData() {
        return cidData;
    }

    public void setCidData(String cidData) {
        this.cidData = cidData;
    }

    public String getCvmResults() {
        return cvmResults;
    }

    public void setCvmResults(String cvmResults) {
        this.cvmResults = cvmResults;
    }
}
