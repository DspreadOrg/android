package com.dspread.pos.ui.payment;

import android.app.Application;
import android.text.TextUtils;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.common.room.TransactionRecord;
import com.dspread.pos.common.room.TransactionRecordRepository;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TLV;
import com.dspread.pos.utils.TLVParser;
import com.dspread.pos.utils.TRACE;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;


public class PaymentViewModel extends BaseAppViewModel {
    private static final String TAG = "PaymentViewModel";
    
    private static final String LOADING_TEXT_PROCESSING = "processing...";
    private static final String CURRENCY_SYMBOL = "$";
    private static final String ONLINE_AUTH_RESULT = "8A023030";
    private static final long CLICK_INTERVAL = 500;
    
    private static final String TLV_TAG_DATE = "9A";
    private static final String TLV_TAG_CURRENCY = "5F2A";
    private static final String TLV_TAG_AMOUNT = "9F02";
    private static final String TLV_TAG_TVR = "95";
    private static final String TLV_TAG_CVM = "9F34";
    private static final String TLV_TAG_CID = "9F27";
    private static final String TLV_TAG_CARD_NO = "C4";
    
    private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private TransactionRecordRepository transactionRecordRepository;
    private SPUtils spUtils;

    public ObservableField<String> loadingText = new ObservableField<>("");
    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableField<String> titleText = new ObservableField<>("Payment");
    public SingleLiveEvent<Boolean> isOnlineSuccess = new SingleLiveEvent();
    public ObservableBoolean showPinpad = new ObservableBoolean(false);
    public ObservableBoolean showResultStatus = new ObservableBoolean(false);
    public ObservableBoolean cardsInsertedStatus = new ObservableBoolean(false);
    private String mTransactionTime;

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        transactionRecordRepository = TransactionRecordRepository.getInstance(application);
        spUtils = SPUtils.getInstance();
    }

    public PaymentModel setTransactionSuccess(String message) {
        setTransactionSuccess();
        
        if (TextUtils.isEmpty(message)) {
            TRACE.e("Transaction success message is empty");
            return new PaymentModel();
        }

        int colonIndex = message.indexOf(":");
        if (colonIndex < 0 || colonIndex >= message.length() - 1) {
            TRACE.e("Invalid transaction success message format: " + message);
            return new PaymentModel();
        }
        
        String tlvData = message.substring(colonIndex + 2);
        PaymentModel paymentModel = new PaymentModel();
        String transType = spUtils.getString("transactionType");
        paymentModel.setTransType(transType);

        List<TLV> tlvList = TLVParser.parse(tlvData);
        if (tlvList == null || tlvList.isEmpty()) {
            TRACE.w("No TLV data parsed from message");
            return paymentModel;
        }

        TLV dateTlv = TLVParser.searchTLV(tlvList, TLV_TAG_DATE);
        TLV transCurrencyCodeTlv = TLVParser.searchTLV(tlvList, TLV_TAG_CURRENCY);
        TLV transAmountTlv = TLVParser.searchTLV(tlvList, TLV_TAG_AMOUNT);
        TLV tvrTlv = TLVParser.searchTLV(tlvList, TLV_TAG_TVR);
        TLV cvmReusltTlv = TLVParser.searchTLV(tlvList, TLV_TAG_CVM);
        TLV cidTlv = TLVParser.searchTLV(tlvList, TLV_TAG_CID);
        TLV cardNo = TLVParser.searchTLV(tlvList, TLV_TAG_CARD_NO);

        paymentModel.setDate(dateTlv != null ? dateTlv.value : "");
        paymentModel.setTransCurrencyCode(transCurrencyCodeTlv != null ? transCurrencyCodeTlv.value : "");
        paymentModel.setAmount(transAmountTlv != null ? transAmountTlv.value : "");
        paymentModel.setTvr(tvrTlv != null ? tvrTlv.value : "");
        paymentModel.setCvmResults(cvmReusltTlv != null ? cvmReusltTlv.value : "");
        paymentModel.setCidData(cidTlv != null ? cidTlv.value : "");
        paymentModel.setCardNo(cardNo != null ? cardNo.value : "");

        TRACE.i("Transaction success, amount: " + paymentModel.getAmount());
        return paymentModel;
    }

    public void setTransactionFailed(String message) {
        titleText.set("Payment finished");
        showPinpad.set(false);
        showResultStatus.set(true);
        cardsInsertedStatus.set(false);
        TRACE.e("Transaction failed: " + message);
    }

    public void clearErrorState() {
        showResultStatus.set(true);
        showPinpad.set(true);
        if (cardsInsertedStatus.get()) {
            cardsInsertedStatus.set(false);
        }
    }

    public void onPinInputCompleted() {
        showPinpad.set(false);
        startLoading(LOADING_TEXT_PROCESSING);
        showResultStatus.set(false);
    }

    public void cardInsertedState() {
        showResultStatus.set(true);
        cardsInsertedStatus.set(true);
    }

    public void displayAmount(String newAmount) {
        if (newAmount != null) {
            amount.set(CURRENCY_SYMBOL + newAmount);
        }
    }

    public void setTransactionSuccess() {
        titleText.set("Payment finished");
        showPinpad.set(false);
        cardsInsertedStatus.set(false);
        showResultStatus.set(false);
    }

    public void startLoading(String text) {
        isLoading.set(true);
        loadingText.set(text != null ? text : "");
    }

    public void stopLoading() {
        isLoading.set(false);
        loadingText.set("");
    }

    public void saveTransactionTime(String transactionTime) {
        this.mTransactionTime = transactionTime;
    }

    private AtomicLong lastClickTimeForCancel = new AtomicLong(0);
    
    public BindingCommand cancleTxnsCommand = new BindingCommand(() -> {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTimeForCancel.get() > CLICK_INTERVAL) {
            lastClickTimeForCancel.set(currentTime);
            startLoading(LOADING_TEXT_PROCESSING);
            if (POSManager.getInstance().isDeviceConnected()) {
                new Thread(() -> {
                    POSManager.getInstance().cancelTransaction();
                }).start();
            } else {
                finish();
            }
        }
    });

    public void requestOnlineAuth(boolean isICC, PaymentModel paymentModel) {
        sendOnlineAuthRequest(isICC);
        saveTransactionRecordToDatabase(paymentModel, new TransactionRecordRepository.InsertCallback() {
            @Override
            public void onInserted(long id) {
                TRACE.i("Transaction record saved with ID: " + id);
            }
        });
    }

    private void sendOnlineAuthRequest(boolean isICC) {
        handleAuthResponse(isICC);
    }

    private void handleAuthResponse(boolean isICC) {
        TRACE.i("Online auth response received");
        if (isICC) {
            POSManager.getInstance().sendOnlineProcessResult(ONLINE_AUTH_RESULT);
        } else {
            isOnlineSuccess.setValue(true);
        }
    }

    private void saveTransactionRecordToDatabase(PaymentModel paymentModel,
                                                 TransactionRecordRepository.InsertCallback callback) {
        if (paymentModel == null) {
            TRACE.w("PaymentModel is null, skip saving transaction record");
            if (callback != null) {
                callback.onInserted(-1);
            }
            return;
        }

        try {
            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setDeviceSn(spUtils.getString("posID", ""));
            transactionRecord.setTransactionType(spUtils.getString("transactionType", ""));
            transactionRecord.setAmount(paymentModel.getAmount());
            transactionRecord.setMaskPan(paymentModel.getCardNo());
            transactionRecord.setCardOrg(paymentModel.getCardOrg());
            transactionRecord.setPayType("Card");
            transactionRecord.setTransResult("Paid");
            transactionRecord.setDeviceDate(DeviceUtils.getDeviceDate());
            transactionRecord.setDeviceTime(formatDateTime(mTransactionTime));
            transactionRecordRepository.insertAsync(transactionRecord, callback);
        } catch (Exception e) {
            TRACE.e("Failed to save transaction record: " + e.getMessage());
            if (callback != null) {
                callback.onInserted(-1);
            }
        }
    }

    private String formatDateTime(String dateTimeStr) {
        if (TextUtils.isEmpty(dateTimeStr)) {
            return "";
        }
        
        synchronized (this) {
            try {
                Date date = INPUT_DATE_FORMAT.parse(dateTimeStr);
                return date != null ? OUTPUT_TIME_FORMAT.format(date) : dateTimeStr;
            } catch (ParseException e) {
                TRACE.w("Failed to parse date time: " + dateTimeStr + ", error: " + e.getMessage());
                return dateTimeStr;
            }
        }
    }
}