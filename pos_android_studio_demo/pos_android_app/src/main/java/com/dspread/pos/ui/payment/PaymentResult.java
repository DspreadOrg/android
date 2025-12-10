package com.dspread.pos.ui.payment;

import java.io.Serializable;

public class PaymentResult implements Serializable {
    private boolean isConnected;
    private String status;
    private String amount;
    private String formatID;
    private String maskedPAN;
    private String expiryDate;
    private String cardHolderName;
    private String serviceCode;
    private String track1Length;
    private String track2Length;
    private String track3Length;
    private String encTracks;
    private String encTrack1;
    private String encTrack2;
    private String encTrack3;
    private String partialTrack;
    private String pinKsn;
    private String trackksn;
    private String pinBlock;
    private String encPAN;
    private String trackRandomNumber;
    private String pinRandomNumber;
    private String tlv;
    private String transactionType;
    private String transResult;

    public String getTransResult() {
        return transResult;
    }

    public void setTransResult(String transResult) {
        this.transResult = transResult;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getTlv() {
        return tlv;
    }

    public void setTlv(String tlv) {
        this.tlv = tlv;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getEncPAN() {
        return encPAN;
    }

    public void setEncPAN(String encPAN) {
        this.encPAN = encPAN;
    }

    public String getEncTrack1() {
        return encTrack1;
    }

    public void setEncTrack1(String encTrack1) {
        this.encTrack1 = encTrack1;
    }

    public String getEncTrack2() {
        return encTrack2;
    }

    public void setEncTrack2(String encTrack2) {
        this.encTrack2 = encTrack2;
    }

    public String getEncTrack3() {
        return encTrack3;
    }

    public void setEncTrack3(String encTrack3) {
        this.encTrack3 = encTrack3;
    }

    public String getEncTracks() {
        return encTracks;
    }

    public void setEncTracks(String encTracks) {
        this.encTracks = encTracks;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getFormatID() {
        return formatID;
    }

    public void setFormatID(String formatID) {
        this.formatID = formatID;
    }

    public String getMaskedPAN() {
        return maskedPAN;
    }

    public void setMaskedPAN(String maskedPAN) {
        this.maskedPAN = maskedPAN;
    }

    public String getPartialTrack() {
        return partialTrack;
    }

    public void setPartialTrack(String partialTrack) {
        this.partialTrack = partialTrack;
    }

    public String getPinBlock() {
        return pinBlock;
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }

    public String getPinKsn() {
        return pinKsn;
    }

    public void setPinKsn(String pinKsn) {
        this.pinKsn = pinKsn;
    }

    public String getPinRandomNumber() {
        return pinRandomNumber;
    }

    public void setPinRandomNumber(String pinRandomNumber) {
        this.pinRandomNumber = pinRandomNumber;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTrack1Length() {
        return track1Length;
    }

    public void setTrack1Length(String track1Length) {
        this.track1Length = track1Length;
    }

    public String getTrack2Length() {
        return track2Length;
    }

    public void setTrack2Length(String track2Length) {
        this.track2Length = track2Length;
    }

    public String getTrack3Length() {
        return track3Length;
    }

    public void setTrack3Length(String track3Length) {
        this.track3Length = track3Length;
    }

    public String getTrackksn() {
        return trackksn;
    }

    public void setTrackksn(String trackksn) {
        this.trackksn = trackksn;
    }

    public String getTrackRandomNumber() {
        return trackRandomNumber;
    }

    public void setTrackRandomNumber(String trackRandomNumber) {
        this.trackRandomNumber = trackRandomNumber;
    }
}
