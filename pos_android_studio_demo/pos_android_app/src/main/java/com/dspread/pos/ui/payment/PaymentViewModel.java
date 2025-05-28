package com.dspread.pos.ui.payment;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.dspread.pos.common.http.api.DingTalkApiService;
import com.dspread.pos.printerAPI.PrinterHelper;
import com.dspread.pos.utils.TLV;
import com.dspread.pos.utils.TLVParser;
import com.dspread.pos.utils.TRACE;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.util.ImageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.http.BaseResponse;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;


public class PaymentViewModel extends BaseAppViewModel {
    private static final String DINGTALK_URL = "https://oapi.dingtalk.com/robot/send?access_token=83e8afc691a1199c70bb471ec46d50099e6dd078ce10223bbcc56c0485cb5cc3";
    private DingTalkApiService apiService;
    
    public PaymentViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getInstance().create(DingTalkApiService.class);
    }
    
    public ObservableField<String> loadingText = new ObservableField<>("");
    public ObservableBoolean isLoading = new ObservableBoolean(false);
    public ObservableField<String> transactionResult = new ObservableField<>("");
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableField<String> titleText = new ObservableField<>("Payment");
    public ObservableBoolean isWaiting = new ObservableBoolean(true);
    public ObservableBoolean isSuccess = new ObservableBoolean(false);
    public ObservableBoolean isPrinting = new ObservableBoolean(false);
    public SingleLiveEvent<Boolean> isOnlineSuccess = new SingleLiveEvent();
    public SingleLiveEvent<Boolean> isContinueTrx = new SingleLiveEvent();
    public ObservableBoolean showPinpad = new ObservableBoolean(false);
    public ObservableBoolean showResultStatus = new ObservableBoolean(false);
    public ObservableField<String> receiptContent = new ObservableField<>();
    private Bitmap receiptBitmap;
    
    public PaymentModel setTransactionSuccess(String message) {
        setTransactionSuccess();
        TRACE.i("data = "+message);
        message = message.substring(message.indexOf(":")+2);
        TRACE.i("data 2 = "+message);
        List<TLV> tlvList = TLVParser.parse(message);
        TLV dateTlv = TLVParser.searchTLV(tlvList,"9A");
//        TLV transTypeTlv = TLVParser.searchTLV(tlvList,"9C");
        TLV transCurrencyCodeTlv = TLVParser.searchTLV(tlvList,"5F2A");
        TLV transAmountTlv = TLVParser.searchTLV(tlvList,"9F02");
        TLV tvrTlv = TLVParser.searchTLV(tlvList,"95");
        TLV cvmReusltTlv = TLVParser.searchTLV(tlvList,"9F34");
        TLV cidTlv = TLVParser.searchTLV(tlvList,"9F27");
        PaymentModel paymentModel = new PaymentModel();
        paymentModel.setDate(dateTlv.value);
        String transType = SPUtils.getInstance().getString("transactionType");
        paymentModel.setTransType(transType);
        paymentModel.setTransCurrencyCode(transCurrencyCodeTlv == null? "":transCurrencyCodeTlv.value);
        paymentModel.setAmount(transAmountTlv == null? "":transAmountTlv.value);
        paymentModel.setTvr(tvrTlv == null? "":tvrTlv.value);
        paymentModel.setCvmResults(cvmReusltTlv == null? "":cvmReusltTlv.value);
        paymentModel.setCidData(cidTlv == null? "":cidTlv.value);
        return paymentModel;
    }
    
    public void setTransactionFailed(String message) {
        titleText.set("Payment finished");
        stopLoading();
        showPinpad.set(false);
        isSuccess.set(false);
        showResultStatus.set(true);
        isWaiting.set(false);
        transactionResult.set(message);
    }
    
    public void setAmount(String newAmount) {
        amount.set("¥" + newAmount);
    }

    public void setWaitingStatus(boolean isWaitings){
        isWaiting.set(isWaitings);
    }
    
    public void setTransactionSuccess() {
        titleText.set("Payment finished");
        stopLoading();
        showPinpad.set(false);
        isSuccess.set(true);
        isWaiting.set(false);
        showResultStatus.set(true);
    }
    
    public void startLoading(String text) {
        isWaiting.set(false);
        isLoading.set(true);
        loadingText.set(text);
    }
    
    public void stopLoading() {
        isLoading.set(false);
        isWaiting.set(false);
        loadingText.set("");
    }

    public BindingCommand continueTxnsCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            titleText.set("Payment");
            stopLoading();
            showPinpad.set(false);
            isSuccess.set(false);
            isWaiting.set(false);
            showResultStatus.set(false);
            isContinueTrx.setValue(true);
        }
    });

    public BindingCommand sendReceiptCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            isPrinting.set(true);
            PrinterManager instance = PrinterManager.getInstance();
            PrinterDevice mPrinter = instance.getPrinter();
            PrinterHelper.getInstance().setPrinter(mPrinter);
            PrinterHelper.getInstance().initPrinter(context);
            TRACE.i("bitmap = "+receiptBitmap);
            new Handler().postDelayed(() -> {
                try {
                    PrinterHelper.getInstance().printBitmap(getApplication(),receiptBitmap);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                PrinterHelper.getInstance().getmPrinter().setPrintListener(new PrintListener() {
                    @Override
                    public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
                        ToastUtils.showShort("Print Finished!");
                        isPrinting.set(false);
                        finish();
                    }
                });
            },100);
        }
    });

    public Bitmap convertReceiptToBitmap(TextView receiptView) {
        float originalTextSize = receiptView.getTextSize();
        int originalWidth = receiptView.getWidth();
        int originalHeight = receiptView.getHeight();
        receiptView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize * 1.5f);
        receiptView.measure(
                View.MeasureSpec.makeMeasureSpec(receiptView.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        Bitmap bitmap = Bitmap.createBitmap(
                receiptView.getWidth(),
                receiptView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        receiptView.layout(0, 0, receiptView.getWidth(), receiptView.getMeasuredHeight());
        receiptView.draw(canvas);
        receiptView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        receiptView.layout(0, 0, originalWidth, originalHeight);
        receiptBitmap = bitmap;
        return bitmap;
    }
    // send msg to dingding
    public void sendDingTalkMessage(boolean isICC, String tlvData, String message) {
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("text", message);
        textContent.put("title","issues");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("msgtype", "markdown");
        requestBody.put("markdown", textContent);

        addSubscribe(apiService.sendMessage(DINGTALK_URL, requestBody)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseResponse>() {
                    @Override
                    public void accept(BaseResponse response) throws Exception {
                        if (response.isOk()) {
                            ToastUtils.showShort("Send online success");
                            isOnlineSuccess.setValue(true);
                            if(!isICC){
//                                transactionResult.set(tlvData);
                            }
                        } else {
                            isOnlineSuccess.setValue(false);
                            transactionResult.set("Send online failed：" + response.getMessage());
                            ToastUtils.showShort("Send online failed：" + response.getMessage());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        isOnlineSuccess.setValue(false);
                        ToastUtils.showShort("The network is failed：" + throwable.getMessage());
                        transactionResult.set("The network is failed：" + throwable.getMessage());
                    }
                }));
        }


}