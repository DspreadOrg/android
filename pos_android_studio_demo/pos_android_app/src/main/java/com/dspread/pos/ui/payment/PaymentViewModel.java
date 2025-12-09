package com.dspread.pos.ui.payment;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.common.http.RetrofitClient;
import com.dspread.pos.common.http.api.RequestOnlineAuthAPI;
import com.dspread.pos.common.http.model.AuthRequest;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.DialogUtils;
import com.dspread.pos.utils.TLV;
import com.dspread.pos.utils.TLVParser;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterManager;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;


public class PaymentViewModel extends BaseAppViewModel {
    private static final String AUTHFROMISSUER_URL = "https://ypparbjfugzgwijijfnb.supabase.co/functions/v1/request-online-result";
    private RequestOnlineAuthAPI apiService;
    public ObservableField<String> loadingText = new ObservableField<>("");
    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<String> transactionResult = new ObservableField<>("");
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableField<String> titleText = new ObservableField<>("Payment");
    public ObservableBoolean isWaiting = new ObservableBoolean(true);
    public ObservableBoolean isD70 = new ObservableBoolean(false);
    public ObservableBoolean isShowAnimationView = new ObservableBoolean(false);
    public ObservableBoolean isShowOtherCardTxt = new ObservableBoolean(false);
    //isPayMentGuideD35
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
        if ("D70".equalsIgnoreCase(DeviceUtils.getPhoneModel())) {
            isD70.set(true);
            isShowAnimationView.set(true);
            isPayMentGuideD35.set(true);
            isPayMentGuideD50.set(true);
            isShowOtherCardTxt.set(true);
        } else if ("D35".equalsIgnoreCase(DeviceUtils.getPhoneModel())) {
            isD70.set(false);
            isShowAnimationView.set(true);
            isPayMentGuideD35.set(false);
            isPayMentGuideD50.set(true);
            isShowOtherCardTxt.set(false);
        } else if ("D50".equalsIgnoreCase(DeviceUtils.getPhoneModel())) {
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
//        TRACE.i("data 2 = "+message);
        PaymentModel paymentModel = new PaymentModel();
        String transType = SPUtils.getInstance().getString("transactionType");
        paymentModel.setTransType(transType);
        List<TLV> tlvList = TLVParser.parse(message);
        if (tlvList == null || tlvList.size() == 0) {
            return paymentModel;
        }
        TLV dateTlv = TLVParser.searchTLV(tlvList, "9A");
//        TLV transTypeTlv = TLVParser.searchTLV(tlvList,"9C");
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
        return paymentModel;
    }

    public void setTransactionFailed(String message) {
        titleText.set("Payment finished");
//        stopLoading();
        showPinpad.set(false);
//        isSuccess.set(false);
        showResultStatus.set(true);
        isWaiting.set(false);
        transactionResult.set(message);
        TransactionResultStatus.set(false);
        cardsInsertedStatus.set(false);
    }

    public void setTransactionErr(String message) {
        TransactionResultStatus.set(false);
    }

    public void clearErrorState() {
        showResultStatus.set(true);
        showPinpad.set(true);
        if (cardsInsertedStatus.get()) {
            cardsInsertedStatus.set(false);
        }
    }

    public void pincomPletedState() {
        showPinpad.set(false);
        if (isD70Device()) {
            cardsInsertedStatus.set(false);
            showResultStatus.set(true);
            isD70.set(false);
            return;
        }

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
        stopLoading();
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
        new Thread(() -> {
            POSManager.getInstance().cancelTransaction();
        }).start();
    });

    public void requestOnlineAuth(boolean isICC, PaymentModel paymentModel) {
        AuthRequest authRequest = createAuthRequest(paymentModel);
        addSubscribe(apiService.sendMessage(AUTHFROMISSUER_URL, authRequest).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(response -> {
            TRACE.i("online auth rsp code= " + response.getResult());
            String onlineRspCode = (String) response.getResult();
            if (response.isOk()) {
//                ToastUtils.showShort("Send online success");
                if (isICC) {
                    POSManager.getInstance().sendOnlineProcessResult("8A02" + onlineRspCode);
                } else {
                    isOnlineSuccess.setValue(true);
                }
            } else {
                if (isICC) {
                    POSManager.getInstance().sendOnlineProcessResult("8A023030");
                } else {
                    isOnlineSuccess.setValue(false);
                }
                transactionResult.set("Send online failed：" + response.getMessage());
                ToastUtils.showShort("Send online failed：" + response.getMessage());
            }
        }, throwable -> {
            if (isICC) {
                POSManager.getInstance().sendOnlineProcessResult("8A023035");
            } else {
                isOnlineSuccess.setValue(false);
            }
            ToastUtils.showShort("The network is failed：" + throwable.getMessage());
            transactionResult.set("The network is failed：" + throwable.getMessage());
        }));
    }

    private AuthRequest createAuthRequest(PaymentModel paymentModel) {
        String deviceSn = SPUtils.getInstance().getString("posID", "");
        String transactionType = SPUtils.getInstance().getString("transactionType", "");
        String amount = paymentModel.getAmount();
        String maskPan = paymentModel.getCardNo();
        String cardOrg = paymentModel.getCardOrg();
        String payType = "Card";
        String transResult = "Paid";
        return new AuthRequest(deviceSn, amount, maskPan, cardOrg, transactionType, payType, transResult, DeviceUtils.getDeviceDate(), DeviceUtils.getDeviceTime());
    }
}