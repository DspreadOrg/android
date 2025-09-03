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
    public AuthRequest(String deviceSn, String amount, String maskPan,
                       String cardOrg, String transactionType, String payType,String transResult) {
        this.deviceSn = deviceSn;
        this.amount = amount;
        this.maskPan = maskPan;
        this.cardOrg = cardOrg;
        this.transactionType = transactionType;
        this.payType = payType;
        this.transResult = transResult;
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
}
