package com.dspread.pos.utils;

import android.text.Html;
import android.text.Spanned;

import com.dspread.pos.ui.payment.PaymentResult;
import com.dspread.pos.ui.payment.PaymentModel;

import java.util.Hashtable;

import me.goldze.mvvmhabit.utils.SPUtils;

public class ReceiptGenerator {
    private static final String DOTTED_LINE = "-----------------------";
    private static final String BR = "<br/>";

    private static String center(String text) {
        return "<div style='text-align:center'>" + text + "</div>" + BR;
    }

    private static String right(String text) {
        return "<div style='text-align:right'>" + text + "</div>";
    }

    private static String labelValue(String label, String value) {
        StringBuilder result = new StringBuilder();
        // Align with equal width font spaces
        result.append("<tt>");  // Use equal width font labels
        result.append(label);

        // Calculate required number of spaces
        int spaces = 30 - label.length();  // The total width can be adjusted as needed
        for (int i = 0; i < spaces; i++) {
            result.append(" ");  // Use regular spaces
        }

        result.append(value);
        result.append("</tt>").append(BR);

        return result.toString();
    }

    private static String bold(String text) {
        return "<b>" + text + "</b>";
    }

    private static String size(String text, int size) {
        return "<font size='" + size + "'>" + text + "</font>";
    }

    private static String line() {
        return "<div style='text-align:center'>"+DOTTED_LINE +"</div>"+ BR;
    }

    public static Spanned generateICCReceipt(PaymentModel result) {
        String transType = SPUtils.getInstance().getString("transactionType");
        StringBuilder receiptBuilder = new StringBuilder()
                .append(center(bold(size("POS of purchase orders", 5))))
                .append(center(bold(size("MERCHANT COPY",5))))
                .append(line())
                .append("ISSUER Agricultural Bank of China").append(BR)
                .append(labelValue("TYPE of transaction(TXN TYPE)", size(transType,2)))
                .append(labelValue("BATCH NO", size("000012",2)))
                .append(center("******* RECEIPT *******"))
                .append(labelValue("Date", size(result.getDate() == null?"":result.getDate(),2)))
                .append(labelValue("Currency", size(result.getTransCurrencyCode()==null?"":result.getTransCurrencyCode(),2)))
                .append(labelValue("TVR", size(result.getTvr()==null?"":result.getTvr(),2)))
                .append(labelValue("Amount", size(result.getAmount()==null?"":result.getAmount(),2)))
                .append(labelValue("CVM Results", size(result.getCvmResults()==null?"":result.getCvmResults(),2)))
                .append(labelValue("CID", size(result.getCidData()==null?"":result.getCidData(),2)))
                .append(line())
                .append(center(bold(size("Thank you",3))));

        return Html.fromHtml(receiptBuilder.toString(), Html.FROM_HTML_MODE_COMPACT);
    }

    public static Spanned generateReceipt(PaymentResult paymentResult, String batchNo) {
        String transType = SPUtils.getInstance().getString("transactionType");
        StringBuilder receiptBuilder = new StringBuilder()
                .append(center(bold(size("POS of purchase orders", 5))))
                .append(center(bold(size("MERCHANT COPY",5))))
                .append(line())
                .append("ISSUER Agricultural Bank of China").append(BR)
                .append(labelValue("TYPE of transaction(TXN TYPE)", size(transType,2)))
                .append(labelValue("BATCH NO", size(batchNo,2)))
                .append(center("******* RECEIPT *******"))
                .append(labelValue("formatID", size(paymentResult.getFormatID(),2)))
                .append(labelValue("Card number", size(paymentResult.getMaskedPAN(),2)))
                .append(labelValue("Expiry Date", size(paymentResult.getExpiryDate(),2)))
                .append(labelValue("CardHolder Name", size(paymentResult.getCardHolderName(),2)))
                .append(labelValue("Service Code", size(paymentResult.getServiceCode(),2)))
                .append(labelValue("Pin Ksn", size(paymentResult.getPinKsn(),2)))
                .append(labelValue("Track Ksn", size(paymentResult.getTrackksn(),2)))
                .append(labelValue("Pin Block", size(paymentResult.getPinBlock(),2)))
                .append(labelValue("track1Length", size(paymentResult.getTrack1Length(),2)))
                .append(labelValue("track2Length", size(paymentResult.getTrack2Length(),2)))
                .append(labelValue("track3Length", size(paymentResult.getTrack3Length(),2)))
                .append(labelValue("encTracks", size(paymentResult.getEncTracks(),2)))
                .append(labelValue("encTrack1", size(paymentResult.getEncTrack1(),2)))
                .append(labelValue("encTrack2", size(paymentResult.getEncTrack2(),2)))
                .append(labelValue("encTrack3", size(paymentResult.getEncTrack3(),2)))
                .append(line())
                .append(center(bold(size("Thank you",3))));

        return Html.fromHtml(receiptBuilder.toString(), Html.FROM_HTML_MODE_COMPACT);
    }

    public static Spanned generateReceipt(Hashtable<String, String> decodeData, String batchNo) {
        String transType = SPUtils.getInstance().getString("transactionType");
        StringBuilder receiptBuilder = new StringBuilder()
                .append(center(bold(size("POS of purchase orders", 5))))
                .append(center(bold(size("MERCHANT COPY",5))))
                .append(line())
                .append("ISSUER Agricultural Bank of China").append(BR)
                .append(labelValue("TYPE of transaction(TXN TYPE)", size(transType,2)))
                .append(labelValue("BATCH NO", size(batchNo,2)))
                .append(center("******* RECEIPT *******"))
                .append(labelValue("formatID", size(decodeData.get("formatID"),2)))
                .append(labelValue("Card number", size(decodeData.get("maskedPAN"),2)))
                .append(labelValue("Expiry Date", size(decodeData.get("expiryDate"),2)))
                .append(labelValue("CardHolder Name", size(decodeData.get("cardholderName"),2)))
                .append(labelValue("Service Code", size(decodeData.get("serviceCode"),2)))
                .append(labelValue("Pin Ksn", size(decodeData.get("pinKsn"),2)))
                .append(labelValue("Track Ksn", size(decodeData.get("trackksn"),2)))
                .append(labelValue("Pin Block", size(decodeData.get("pinBlock"),2)))
                .append(labelValue("track1Length", size(decodeData.get("track1Length"),2)))
                .append(labelValue("track2Length", size(decodeData.get("track2Length"),2)))
                .append(labelValue("track3Length", size(decodeData.get("track3Length"),2)))
                .append(labelValue("encTracks", size(decodeData.get("encTracks"),2)))
                .append(labelValue("encTrack1", size(decodeData.get("encTrack1"),2)))
                .append(labelValue("encTrack2", size(decodeData.get("encTrack2"),2)))
                .append(labelValue("encTrack3", size(decodeData.get("encTrack3"),2)))
                .append(line())
                .append(center(bold(size("Thank you",3))));

        return Html.fromHtml(receiptBuilder.toString(), Html.FROM_HTML_MODE_COMPACT);
    }
}