package com.dspread.pos.common.http.model;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("deviceSn")
    private String deviceSn;

    @SerializedName("amount")
    private String amount;

    @SerializedName("maskPan")
    private String maskPan;

    @SerializedName("cardOrg")
    private String cardOrg;

    @SerializedName("transactionType")
    private String transactionType;

    @SerializedName("payType")
    private String payType;

    @SerializedName("transResult")
    private String transResult;

    @SerializedName("transactionDate")
    private String transactionDate;
    @SerializedName("transactionTime")
    private String transactionTime;

    @SerializedName("pollToEntryPinTime")
    private int pollToEntryPinTime;

    @SerializedName("pinEntryToFinishTime")
    private int pinEntryToFinishTime;

    @SerializedName("txnTotalTime")
    private int txnTotalTime;

    @SerializedName("tradeMode")
    private String tradeMode;

    @SerializedName("appVersion")
    private String appVersion;

    @SerializedName("osVersion")
    private String osVersion;

    @SerializedName("model")
    private String model;
    public AuthRequest(String deviceSn, String amount, String maskPan,
                       String cardOrg, String transactionType, String payType,String transResult,String transactionDate, String transactionTime, int pollToEntryPinTime, int pinEntryToFinishTime, int txnTotalTime, String tradeMode, String appVersion, String osVersion, String model) {
        this.deviceSn = deviceSn;
        this.amount = amount;
        this.maskPan = maskPan;
        this.cardOrg = cardOrg;
        this.transactionType = transactionType;
        this.payType = payType;
        this.transResult = transResult;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
        this.pollToEntryPinTime = pollToEntryPinTime;
        this.pinEntryToFinishTime = pinEntryToFinishTime;
        this.txnTotalTime = txnTotalTime;
        this.tradeMode = tradeMode;
        this.appVersion = appVersion;
        this.osVersion = osVersion;
        this.model = model;
    }

    // Getter方法
    public String getDeviceSn() { return deviceSn; }
    public String getAmount() { return amount; }
    public String getMaskPan() { return maskPan; }
    public String getCardOrg() { return cardOrg; }
    public String getTransactionType() { return transactionType; }
    public String getPayType() { return payType; }

    public String getTransResult() {
        return transResult;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionTime() {
        return transactionTime;
    }
    // 添加getter方法
    public int getPollToEntryPinTime() { return pollToEntryPinTime; }
    public int getPinEntryToFinishTime() { return pinEntryToFinishTime; }
    public int getTxnTotalTime() { return txnTotalTime; }
    public String getTradeMode() { return tradeMode; }
    public String getAppVersion() { return appVersion; }
    public String getOsVersion() { return osVersion; }
    public String getModel() { return model; }
}
