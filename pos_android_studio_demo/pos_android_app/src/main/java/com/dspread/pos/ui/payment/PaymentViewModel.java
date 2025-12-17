package com.dspread.pos.ui.payment;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;


import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.common.http.RetrofitClient;
import com.dspread.pos.common.http.api.RequestOnlineAuthAPI;
import com.dspread.pos.common.http.model.AuthRequest;

import com.dspread.pos.common.room.TransactionRecord;
import com.dspread.pos.common.room.TransactionRecordRepository;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TLV;
import com.dspread.pos.utils.TLVParser;
import com.dspread.pos.utils.TRACE;

import java.util.List;

import androidx.lifecycle.LiveData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;


public class PaymentViewModel extends BaseAppViewModel {
    private static final String AUTHFROMISSUER_URL = "https://ypparbjfugzgwijijfnb.supabase.co/functions/v1/request-online-result";
    private RequestOnlineAuthAPI apiService;
    private TransactionRecordRepository transactionRecordRepository;
    private long currentTransactionId = -1;

    public ObservableField<String> loadingText = new ObservableField<>("");
    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableField<String> titleText = new ObservableField<>("Payment");
    public ObservableBoolean isWaiting = new ObservableBoolean(true);
    public ObservableBoolean isD70 = new ObservableBoolean(false);
    public ObservableBoolean isShowAnimationView = new ObservableBoolean(false);
    public ObservableBoolean isShowOtherCardTxt = new ObservableBoolean(false);
    public ObservableBoolean isPayMentGuideD35 = new ObservableBoolean(false);
    public ObservableBoolean isPayMentGuideD50 = new ObservableBoolean(false);
    public SingleLiveEvent<Boolean> isOnlineSuccess = new SingleLiveEvent();
    public ObservableBoolean showPinpad = new ObservableBoolean(false);
    public ObservableBoolean showResultStatus = new ObservableBoolean(false);
    public ObservableBoolean TransactionResultStatus = new ObservableBoolean(false);
    public ObservableBoolean cardsInsertedStatus = new ObservableBoolean(false);
    private boolean isIccCard = false;

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getInstance().create(RequestOnlineAuthAPI.class);
        transactionRecordRepository = TransactionRecordRepository.getInstance(application);
        initDeviceConfig();
    }

    private void initDeviceConfig() {
        String deviceModel = DeviceUtils.getPhoneModel();
        if ("D70".equalsIgnoreCase(deviceModel)) {
            isD70.set(true);
            isShowAnimationView.set(true);
            isPayMentGuideD35.set(true);
            isPayMentGuideD50.set(true);
            isShowOtherCardTxt.set(true);
        } else if ("D35".equalsIgnoreCase(deviceModel)) {
            isD70.set(false);
            isShowAnimationView.set(true);
            isPayMentGuideD35.set(false);
            isPayMentGuideD50.set(true);
            isShowOtherCardTxt.set(false);
        } else if ("D50".equalsIgnoreCase(deviceModel)) {
            isD70.set(false);
            isShowAnimationView.set(true);
            isPayMentGuideD35.set(true);
            isPayMentGuideD50.set(false);
            isShowOtherCardTxt.set(false);
        } else {
            isD70.set(false);
            isPayMentGuideD35.set(true);
            isPayMentGuideD50.set(true);
            isShowOtherCardTxt.set(true);
        }
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
        isWaiting.set(false);
        TransactionResultStatus.set(false);
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
        if (isD70Device()) {
            cardsInsertedStatus.set(false);
            showResultStatus.set(true);
            isD70.set(false);
            return;
        }
        startLoading("processing...");
        boolean shouldShowResult = isIccCard && !cardsInsertedStatus.get();
        showResultStatus.set(shouldShowResult);

        if (shouldShowResult) {
            cardsInsertedStatus.set(true);
        }
    }

    public void cardInsertedState() {
        isIccCard = true;
        showResultStatus.set(true);
        cardsInsertedStatus.set(true);
    }

    public void displayAmount(String newAmount) {
        amount.set("$" + newAmount);
    }

    public void setTransactionSuccess() {
        titleText.set("Payment finished");
        showPinpad.set(false);
        isWaiting.set(false);

        if (isIccCard) {
            cardsInsertedStatus.set(!isD70Device());
        } else {
            showResultStatus.set(false);
        }
    }

    public void startLoading(String text) {
        isWaiting.set(false);
        isLoading.set(true);
        loadingText.set(text);
        if (isD70Device()) {
            handleD70Loading();
        }
    }

    private void handleD70Loading() {
        isD70.set(false);
        cardsInsertedStatus.set(false);
        showResultStatus.set(true);
    }

    private boolean isD70Device() {
        return "D70".equalsIgnoreCase(DeviceUtils.getPhoneModel());
    }

    public void stopLoading() {
        isLoading.set(false);
        isWaiting.set(false);
        loadingText.set("");
    }

    public BindingCommand cancleTxnsCommand = new BindingCommand(() -> {
        startLoading("processing...");
        new Thread(() -> {
            POSManager.getInstance().cancelTransaction();
        }).start();
    });

    public void requestOnlineAuth(boolean isICC, PaymentModel paymentModel) {
        // AuthRequest authRequest = createAuthRequest(paymentModel);
        // 保存交易记录到数据库
        sendOnlineAuthRequest(isICC);
        saveTransactionRecordToDatabase(paymentModel, new TransactionRecordRepository.InsertCallback() {
            @Override
            public void onInserted(long id) {
                currentTransactionId = id;
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

    private void handleAuthError(boolean isICC, Throwable throwable) {
        TRACE.e("Online auth request failed: " + throwable.getMessage());

        if (isICC) {
            POSManager.getInstance().sendOnlineProcessResult("8A023030");
        } else {
            isOnlineSuccess.setValue(true);
        }
    }

    private AuthRequest createAuthRequest(PaymentModel paymentModel) {
        String deviceSn = SPUtils.getInstance().getString("posID", "");
        String transactionType = SPUtils.getInstance().getString("transactionType", "");
        String amount = paymentModel.getAmount();
        String maskPan = paymentModel.getCardNo();
        String cardOrg = paymentModel.getCardOrg();
        String payType = "Card";
        String transResult = "Paid";

        return new AuthRequest(
                deviceSn,
                amount,
                maskPan,
                cardOrg,
                transactionType,
                payType,
                transResult,
                DeviceUtils.getDeviceDate(),
                DeviceUtils.getDeviceTime()
        );
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
            transactionRecord.setDeviceTime(DeviceUtils.getDeviceTime());
            // 异步保存到数据库
            transactionRecordRepository.insertAsync(transactionRecord, callback);
        } catch (Exception e) {
            TRACE.e("Failed to save transaction record: " + e.getMessage());
            if (callback != null) {
                callback.onInserted(-1);
            }
        }
    }
}