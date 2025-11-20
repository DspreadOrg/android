package com.dspread.pos.utils;

import android.content.Context;

import com.dspread.pos.ui.payment.PaymentResult;
import com.dspread.pos.ui.payment.PaymentModel;
import com.dspread.pos.ui.payment.PaymentViewModel;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentBinding;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

public class HandleTxnsResultUtils {
    public static PaymentResult handleDoTradeResult(PaymentResult paymentResult, Hashtable<String, String> decodeData,ActivityPaymentBinding binding, PaymentViewModel viewModel){
        paymentResult.setFormatID(decodeData.get("formatID") == null?"":decodeData.get("formatID"));
        paymentResult.setMaskedPAN(decodeData.get("maskedPAN") == null? "":decodeData.get("maskedPAN"));
        paymentResult.setExpiryDate(decodeData.get("expiryDate") == null? "":decodeData.get("expiryDate"));
        paymentResult.setCardHolderName(decodeData.get("cardholderName") == null? "":decodeData.get("cardholderName"));
        paymentResult.setServiceCode(decodeData.get("serviceCode") == null? "":decodeData.get("serviceCode"));
        paymentResult.setTrack1Length(decodeData.get("track1Length") == null? "":decodeData.get("track1Length"));
        paymentResult.setTrack2Length(decodeData.get("track2Length") == null? "":decodeData.get("track2Length"));
        paymentResult.setTrack3Length(decodeData.get("track3Length") == null? "":decodeData.get("track3Length"));
        paymentResult.setEncTracks(decodeData.get("encTracks") == null? "":decodeData.get("encTracks"));
        paymentResult.setEncTrack1(decodeData.get("encTrack1") == null? "":decodeData.get("encTrack1"));
        paymentResult.setEncTrack2(decodeData.get("encTrack2") == null? "":decodeData.get("encTrack2"));
        paymentResult.setEncTrack3(decodeData.get("encTrack3") == null? "":decodeData.get("encTrack3"));
        paymentResult.setPartialTrack(decodeData.get("partialTrack") == null? "":decodeData.get("partialTrack"));
        paymentResult.setPinKsn(decodeData.get("pinKsn") == null? "":decodeData.get("pinKsn"));
        paymentResult.setTrackksn(decodeData.get("trackksn") == null? "":decodeData.get("trackksn"));
        paymentResult.setPinBlock(decodeData.get("pinBlock") == null? "":decodeData.get("pinBlock"));
        paymentResult.setEncPAN(decodeData.get("encPAN") == null? "":decodeData.get("encPAN"));
        paymentResult.setTrackRandomNumber(decodeData.get("trackRandomNumber") == null? "":decodeData.get("trackRandomNumber"));
        paymentResult.setPinRandomNumber(decodeData.get("pinRandomNumber") == null? "":decodeData.get("pinRandomNumber"));

//        Spanned receiptContent = ReceiptGenerator.generateReceipt(result, "000015");
//        binding.tvReceipt.setMovementMethod(LinkMovementMethod.getInstance());
//        binding.tvReceipt.setText(receiptContent);

        PaymentModel model = new PaymentModel();
        model.setAmount(paymentResult.getAmount());
        model.setCardNo(paymentResult.getMaskedPAN());
        model.setCardOrg(AdvancedBinDetector.detectCardType(paymentResult.getMaskedPAN()).getDisplayName());
        viewModel.startLoading("processing...");
        viewModel.requestOnlineAuth(false, model);
        return paymentResult;
    }

    public static String getTradeResultMessage(QPOSService.DoTradeResult result, Context context) {
        switch (result) {
            //case NONE: return context.getString(R.string.no_card_detected);
            //case TRY_ANOTHER_INTERFACE: return context.getString(R.string.try_another_interface);
            // case NOT_ICC: return context.getString(R.string.card_inserted);
            //case BAD_SWIPE: return context.getString(R.string.bad_swipe);
            //case CARD_NOT_SUPPORT: return "GPO NOT SUPPORT";
            case PLS_SEE_PHONE:
                return "PLS SEE PHONE, and pls wait for cardholder to confirm, then to try again";
            //case NFC_DECLINED: return context.getString(R.string.transaction_declined);
            //case NO_RESPONSE: return context.getString(R.string.card_no_response);
            default:
                return context.getString(R.string.unknown_error);
        }
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

    public static String getTransactionResultMessage(QPOSService.TransactionResult result, Context context) {
        switch (result) {
            case APPROVED:
                return "";
            case TERMINATED:
                return context.getString(R.string.transaction_terminated);
            case DECLINED:
                return context.getString(R.string.transaction_declined);
            case CANCEL:
                return context.getString(R.string.transaction_cancel);
           /* case CAPK_FAIL:
                return context.getString(R.string.transaction_capk_fail);
            case NOT_ICC:
                return context.getString(R.string.transaction_not_icc);
            case SELECT_APP_FAIL:
                return context.getString(R.string.transaction_app_fail);
            case DEVICE_ERROR:
                return context.getString(R.string.transaction_device_error);
            case TRADE_LOG_FULL:
                return "the trade log has fulled!pls clear the trade log!";
            case CARD_NOT_SUPPORTED:
                return context.getString(R.string.card_not_supported);
            case MISSING_MANDATORY_DATA:
                return context.getString(R.string.missing_mandatory_data);
            case CARD_BLOCKED_OR_NO_EMV_APPS:
                return context.getString(R.string.card_blocked_or_no_evm_apps);
            case INVALID_ICC_DATA:
                return context.getString(R.string.invalid_icc_data);
            case FALLBACK:
                return "trans fallback";
            case NFC_TERMINATED:
                return "NFC Terminated";
            case CARD_REMOVED:
                return "CARD REMOVED";
            case CONTACTLESS_TRANSACTION_NOT_ALLOW:
                return "TRANS NOT ALLOW";
            case CARD_BLOCKED:
                return "CARD BLOCKED";
            case TRANS_TOKEN_INVALID:
                return "TOKEN INVALID";
            case APP_BLOCKED:
                return "APP BLOCKED";*/
            default:
                return result.name();
        }
    }

    public static String getDisplayMessage(QPOSService.Display displayMsg, Context context) {
        switch (displayMsg) {
            // case CLEAR_DISPLAY_MSG: return "";
            case PLEASE_WAIT:
                return context.getString(R.string.wait);
            case REMOVE_CARD:
                return context.getString(R.string.remove_card);
            // case TRY_ANOTHER_INTERFACE: return context.getString(R.string.try_another_interface);
            case PROCESSING:
                return context.getString(R.string.processing);
            case INPUT_PIN_ING:
                return "please input pin on pos";
            case INPUT_OFFLINE_PIN_ONLY:
            case INPUT_LAST_OFFLINE_PIN:
                return "please input offline pin on pos";
            case MAG_TO_ICC_TRADE:
                return "please insert chip card on pos";
            //  case CARD_REMOVED: return "card removed";
            case TRANSACTION_TERMINATED:
                return "transaction terminated";
            case PlEASE_TAP_CARD_AGAIN:
                return context.getString(R.string.please_tap_card_again);
            default:
                return "";
        }
    }

}
