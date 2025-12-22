package com.dspread.pos.common.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transaction_record",
        indices = {
                @Index(value = "device_sn"),
                @Index(value = "created_at"),
                @Index(value = "device_date")
        })
public class TransactionRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "device_sn")
    private String deviceSn = "";

    @NonNull
    @ColumnInfo(name = "transaction_type")
    private String transactionType = "";

    @NonNull
    @ColumnInfo(name = "amount")
    private String amount = "";

    @ColumnInfo(name = "mask_pan")
    private String maskPan = "";

    @ColumnInfo(name = "card_org")
    private String cardOrg = "";

    @ColumnInfo(name = "pay_type")
    private String payType = "";

    @ColumnInfo(name = "trans_result")
    private String transResult = "";

    @ColumnInfo(name = "device_date")
    private String deviceDate = "";

    @ColumnInfo(name = "device_time")
    private String deviceTime = "";

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();


    @ColumnInfo(name = "merchant_name")
    private String merchantName;

    public TransactionRecord() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(@NonNull String deviceSn) {
        this.deviceSn = deviceSn;
    }

    @NonNull
    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(@NonNull String transactionType) {
        this.transactionType = transactionType;
    }

    @NonNull
    public String getAmount() {
        return amount;
    }

    public void setAmount(@NonNull String amount) {
        this.amount = amount;
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

    public String getDeviceDate() {
        return deviceDate;
    }

    public void setDeviceDate(String deviceDate) {
        this.deviceDate = deviceDate;
    }

    public String getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(String deviceTime) {
        this.deviceTime = deviceTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
}