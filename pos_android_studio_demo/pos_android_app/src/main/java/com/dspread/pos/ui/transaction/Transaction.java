package com.dspread.pos.ui.transaction;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Transaction implements Serializable {
    @SerializedName("id")
    private double id;

    @SerializedName("device_sn")
    private String deviceSn;

    @SerializedName("amount")
    private double amount;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("mask_pan")
    private String maskPan;

    @SerializedName("card_org")
    private String cardOrg;

    @SerializedName("pay_type")
    private String payType;

    @SerializedName("trans_result")
    private String transResult;

    @SerializedName("request_date")
    private String requestDate;

    @SerializedName("request_time")
    private String requestTime;

    @SerializedName("created_at")
    private String createdAt;


    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    @SerializedName("transaction_time")
    private String transactionTime;


    @SerializedName("transaction_date")
    private String transactionDate;

    // Getter 和 Setter 方法
    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getMaskPan() {
        return maskPan;
    }

    public void setMaskPan(String maskPan) {
        this.maskPan = maskPan;
    }

    public String getCardOrg() {
        return cardOrg;
    }

    public void setCardOrg(String cardOrg) {
        this.cardOrg = cardOrg;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getTransResult() {
        return transResult;
    }

    public void setTransResult(String transResult) {
        this.transResult = transResult;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", deviceSn='" + deviceSn + '\'' +
                ", amount=" + amount +
                ", transactionType='" + transactionType + '\'' +
                ", maskPan='" + maskPan + '\'' +
                ", cardOrg='" + cardOrg + '\'' +
                ", payType='" + payType + '\'' +
                ", transResult='" + transResult + '\'' +
                ", requestDate='" + requestDate + '\'' +
                ", requestTime='" + requestTime + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
