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

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;


public class PaymentViewModel extends BaseAppViewModel {
    private TransactionRecordRepository transactionRecordRepository;

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
    }

    public PaymentModel setTransactionSuccess(String message) {
        setTransactionSuccess();
        message = message.substring(message.indexOf(":") + 2);

        PaymentModel paymentModel = new PaymentModel();
        String transType = SPUtils.getInstance().getString("transactionType");
        paymentModel.setTransType(transType);

        List<TLV> tlvList = TLVParser.parse(message);
        if (tlvList == null || tlvList.size() == 0) {
            return paymentModel;
        }

        TLV dateTlv = TLVParser.searchTLV(tlvList, "9A");
        TLV transCurrencyCodeTlv = TLVParser.searchTLV(tlvList, "5F2A");
        TLV transAmountTlv = TLVParser.searchTLV(tlvList, "9F02");
        TLV tvrTlv = TLVParser.searchTLV(tlvList, "95");
        TLV cvmReusltTlv = TLVParser.searchTLV(tlvList, "9F34");
        TLV cidTlv = TLVParser.searchTLV(tlvList, "9F27");

        paymentModel.setDate(dateTlv.value);
        paymentModel.setTransCurrencyCode(transCurrencyCodeTlv == null ? "" : transCurrencyCodeTlv.value);
        paymentModel.setAmount(transAmountTlv == null ? "" : transAmountTlv.value);
        paymentModel.setTvr(tvrTlv == null ? "" : tvrTlv.value);
        paymentModel.setCvmResults(cvmReusltTlv == null ? "" : cvmReusltTlv.value);
        paymentModel.setCidData(cidTlv == null ? "" : cidTlv.value);

        TRACE.i("Transaction success: " + paymentModel.getAmount());
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
        startLoading("processing...");
        showResultStatus.set(false);
    }

    public void cardInsertedState() {
        showResultStatus.set(true);
        cardsInsertedStatus.set(true);
    }

    public void displayAmount(String newAmount) {
        amount.set("$" + newAmount);
    }

    public void setTransactionSuccess() {
        titleText.set("Payment finished");
        showPinpad.set(false);
        cardsInsertedStatus.set(false);
        showResultStatus.set(false);
    }

    public void startLoading(String text) {
        isLoading.set(true);
        loadingText.set(text);
    }

    public void stopLoading() {
        isLoading.set(false);
        loadingText.set("");
    }

    public void saveTransactionTime(String transactionTime) {
        this.mTransactionTime = transactionTime;
    }

    public BindingCommand cancleTxnsCommand = new BindingCommand(() -> {
        startLoading("processing...");
        if(POSManager.getInstance().isDeviceConnected()) {
            new Thread(() -> {
                POSManager.getInstance().cancelTransaction();
            }).start();
        }else {
            finish();
        }
    });

    public void requestOnlineAuth(boolean isICC, PaymentModel paymentModel) {
        // AuthRequest authRequest = createAuthRequest(paymentModel);
        // Save transaction records to the ROOM database
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
            POSManager.getInstance().sendOnlineProcessResult("8A023030");
        } else {
            isOnlineSuccess.setValue(true);
        }
    }

    private void saveTransactionRecordToDatabase(PaymentModel paymentModel,
                                                 TransactionRecordRepository.InsertCallback callback) {
        try {
            TransactionRecord transactionRecord = new TransactionRecord();
            transactionRecord.setDeviceSn(SPUtils.getInstance().getString("posID", ""));
            transactionRecord.setTransactionType(SPUtils.getInstance().getString("transactionType", ""));
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
        try {
            return dateTimeStr == null || dateTimeStr.isEmpty() ? "" :
                    new SimpleDateFormat("HH:mm:ss")
                            .format(new SimpleDateFormat("yyyyMMddHHmmss").parse(dateTimeStr));
        } catch (Exception e) {
            return dateTimeStr == null ? "" : dateTimeStr;
        }
    }
}