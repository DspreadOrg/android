package com.dspread.pos.utils;

import android.content.Context;

import com.dspread.pos.ui.payment.PaymentResult;
import com.dspread.pos.ui.payment.PaymentModel;
import com.dspread.pos.ui.payment.PaymentViewModel;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

public class HandleTxnsResultUtils {
    public static void handleDoTradeResult(PaymentResult paymentResult, Hashtable<String, String> decodeData, PaymentViewModel viewModel){
        if(decodeData != null) {
            paymentResult.setFormatID(decodeData.get("formatID") == null ? "" : decodeData.get("formatID"));
            paymentResult.setMaskedPAN(decodeData.get("maskedPAN") == null ? "" : decodeData.get("maskedPAN"));
            paymentResult.setExpiryDate(decodeData.get("expiryDate") == null ? "" : decodeData.get("expiryDate"));
            paymentResult.setCardHolderName(decodeData.get("cardholderName") == null ? "" : decodeData.get("cardholderName"));
            paymentResult.setServiceCode(decodeData.get("serviceCode") == null ? "" : decodeData.get("serviceCode"));
            paymentResult.setTrack1Length(decodeData.get("track1Length") == null ? "" : decodeData.get("track1Length"));
            paymentResult.setTrack2Length(decodeData.get("track2Length") == null ? "" : decodeData.get("track2Length"));
            paymentResult.setTrack3Length(decodeData.get("track3Length") == null ? "" : decodeData.get("track3Length"));
            paymentResult.setEncTracks(decodeData.get("encTracks") == null ? "" : decodeData.get("encTracks"));
            paymentResult.setEncTrack1(decodeData.get("encTrack1") == null ? "" : decodeData.get("encTrack1"));
            paymentResult.setEncTrack2(decodeData.get("encTrack2") == null ? "" : decodeData.get("encTrack2"));
            paymentResult.setEncTrack3(decodeData.get("encTrack3") == null ? "" : decodeData.get("encTrack3"));
            paymentResult.setPartialTrack(decodeData.get("partialTrack") == null ? "" : decodeData.get("partialTrack"));
            paymentResult.setPinKsn(decodeData.get("pinKsn") == null ? "" : decodeData.get("pinKsn"));
            paymentResult.setTrackksn(decodeData.get("trackksn") == null ? "" : decodeData.get("trackksn"));
            paymentResult.setPinBlock(decodeData.get("pinBlock") == null ? "" : decodeData.get("pinBlock"));
            paymentResult.setEncPAN(decodeData.get("encPAN") == null ? "" : decodeData.get("encPAN"));
            paymentResult.setTrackRandomNumber(decodeData.get("trackRandomNumber") == null ? "" : decodeData.get("trackRandomNumber"));
            paymentResult.setPinRandomNumber(decodeData.get("pinRandomNumber") == null ? "" : decodeData.get("pinRandomNumber"));
        }

        PaymentModel model = new PaymentModel();
        model.setAmount(paymentResult.getAmount());
        if(paymentResult.getMaskedPAN() != null){
            model.setCardNo(paymentResult.getMaskedPAN());
            model.setCardOrg(AdvancedBinDetector.detectCardType(paymentResult.getMaskedPAN()).getDisplayName());
        }
        viewModel.startLoading("processing...");
        viewModel.requestOnlineAuth(false, model);
    }

    // get the TransactionType value
    public static QPOSService.TransactionType getTransactionType(String type) {
        if (type == null) return QPOSService.TransactionType.GOODS;
        switch (type) {
            case "GOODS":
                return QPOSService.TransactionType.GOODS;
            case "SERVICES":
                return QPOSService.TransactionType.SERVICES;
            case "CASH":
                return QPOSService.TransactionType.CASH;
            case "CASHBACK":
                return QPOSService.TransactionType.CASHBACK;
            case "PURCHASE_REFUND":
            case "REFUND":
                return QPOSService.TransactionType.REFUND;
            case "INQUIRY":
                return QPOSService.TransactionType.INQUIRY;
            case "TRANSFER":
                return QPOSService.TransactionType.TRANSFER;
            case "ADMIN":
                return QPOSService.TransactionType.ADMIN;
            case "CASHDEPOSIT":
                return QPOSService.TransactionType.CASHDEPOSIT;
            case "PAYMENT":
                return QPOSService.TransactionType.PAYMENT;
            case "PBOCLOG||ECQ_INQUIRE_LOG":
                return QPOSService.TransactionType.PBOCLOG;
            case "SALE":
                return QPOSService.TransactionType.SALE;
            case "PREAUTH":
                return QPOSService.TransactionType.PREAUTH;
            case "ECQ_DESIGNATED_LOAD":
                return QPOSService.TransactionType.ECQ_DESIGNATED_LOAD;
            case "ECQ_UNDESIGNATED_LOAD":
                return QPOSService.TransactionType.ECQ_UNDESIGNATED_LOAD;
            case "ECQ_CASH_LOAD":
                return QPOSService.TransactionType.ECQ_CASH_LOAD;
            case "ECQ_CASH_LOAD_VOID":
                return QPOSService.TransactionType.ECQ_CASH_LOAD_VOID;
            case "CHANGE_PIN":
                return QPOSService.TransactionType.UPDATE_PIN;
            case "SALES_NEW":
                return QPOSService.TransactionType.SALES_NEW;
            case "BALANCE_UPDATE":
                return QPOSService.TransactionType.BALANCE_UPDATE;
            case "BALANCE":
                return QPOSService.TransactionType.BALANCE;
            default:
                return QPOSService.TransactionType.GOODS;
        }
    }

    public static String getDisplayMessage(QPOSService.Display displayMsg, Context context) {
        switch (displayMsg) {
            case PLEASE_WAIT:
                return context.getString(R.string.wait);
            case REMOVE_CARD:
                return context.getString(R.string.remove_card);
            case PROCESSING:
                return context.getString(R.string.processing);
            case INPUT_ONLINE_PIN:
            case INPUT_OFFLINE_PIN:
            case INPUT_PIN_ING:
                return "please input pin on pos";
            case INPUT_OFFLINE_PIN_ONLY:
            case INPUT_LAST_OFFLINE_PIN:
                return "please input offline pin on pos";
            case MAG_TO_ICC_TRADE:
                return "please insert chip card on pos";
            case TRANSACTION_TERMINATED:
                return "transaction terminated";
            case PlEASE_TAP_CARD_AGAIN:
                return context.getString(R.string.please_tap_card_again);
            default:
                return "";
        }
    }

}
