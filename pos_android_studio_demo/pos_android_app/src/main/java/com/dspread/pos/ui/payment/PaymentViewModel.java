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

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        apiService = RetrofitClient.getInstance().create(RequestOnlineAuthAPI.class);
        if("D70".equals(DeviceUtils.getPhoneModel())){
            isD70.set(true);
        }else {
            isD70.set(false);
        }
    }

    public ObservableField<String> loadingText = new ObservableField<>("");
    public ObservableField<Boolean> isLoading = new ObservableField<>(false);
    public ObservableField<String> transactionResult = new ObservableField<>("");
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableField<String> titleText = new ObservableField<>("Payment");
    public ObservableBoolean isWaiting = new ObservableBoolean(true);
//    public ObservableBoolean isSuccess = new ObservableBoolean(false);
    public ObservableBoolean isPrinting = new ObservableBoolean(false);
    public ObservableBoolean isD70 = new ObservableBoolean(false);
    public SingleLiveEvent<Boolean> isOnlineSuccess = new SingleLiveEvent();
    public ObservableBoolean showPinpad = new ObservableBoolean(false);
    public ObservableBoolean showResultStatus = new ObservableBoolean(false);
    public ObservableBoolean TransactionResultStatus = new ObservableBoolean(false);
    public ObservableBoolean cardsInsertedStatus = new ObservableBoolean(false);
    public ObservableField<String> receiptContent = new ObservableField<>();
    private Bitmap receiptBitmap;
    private Context mContext;
    private boolean isIccCard=false;

    public void setmContext(Context mContext) {
        this.mContext = mContext;
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
        stopLoading();
        showPinpad.set(false);
//        isSuccess.set(false);
        showResultStatus.set(true);
        isWaiting.set(false);
        transactionResult.set(message);
        TransactionResultStatus.set(false);
        cardsInsertedStatus.set(false);
         }
    public  void setTransactionErr(String message){
        TransactionResultStatus.set(false);
    }
    public void clearErrorState() {
        showResultStatus.set(true);
        showPinpad.set(true);
        if(cardsInsertedStatus.get()){
        cardsInsertedStatus.set(false);
        }
//        transactionResult.set("");
//        isSuccess.set(false);
    }
    public void pincomPletedState(){

        showPinpad.set(false);

        if(isIccCard&&!cardsInsertedStatus.get()){
            showResultStatus.set(true);
            cardsInsertedStatus.set(true);
        }else{
            showResultStatus.set(false);
        }

    }
    public void cardInsertedState(){
        isIccCard = true;
        showResultStatus.set(true);
        cardsInsertedStatus.set(true);

    }
    public void displayAmount(String newAmount) {
        amount.set("$" + newAmount);
    }

    public void setWaitingStatus(boolean isWaitings) {
        isWaiting.set(isWaitings);
    }

    public void setTransactionSuccess() {
        titleText.set("Payment finished");
        stopLoading();
        showPinpad.set(false);
//        isSuccess.set(true);
        isWaiting.set(false);
//        showResultStatus.set(true);
//        TransactionResultStatus.set(true);
//        cardsInsertedStatus.set(true);
        if(isIccCard){
            cardsInsertedStatus.set(true);
        }else{
            showResultStatus.set(false);
        }
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

    public BindingCommand continueTxnsCommand = new BindingCommand(() -> finish());

    public BindingCommand cancleTxnsCommand = new BindingCommand(() -> {
        new Thread(() -> {
            POSManager.getInstance().cancelTransaction();
        }).start();
//        finish();
    });
    public BindingCommand sendReceiptCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            isPrinting.set(true);
            PrinterManager instance = PrinterManager.getInstance();
            PrinterDevice mPrinter = instance.getPrinter();
            PrinterHelper.getInstance().setPrinter(mPrinter);
            PrinterHelper.getInstance().initPrinter(mContext);
            TRACE.i("bitmap = " + receiptBitmap);
            new Handler().postDelayed(() -> {
                try {
                    PrinterHelper.getInstance().printBitmap(getApplication(), receiptBitmap);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                PrinterHelper.getInstance().getmPrinter().setPrintListener(new PrintListener() {
                    @Override
                    public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
                        TRACE.i("resultType = " + resultType.getValue());
                        if (!b && resultType.getValue() == -9) {
                            if (mContext != null) {
                                DialogUtils.showLowBatteryDialog(mContext, R.layout.dialog_low_battery, R.id.okButton, false, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        isPrinting.set(false);
                                        finish();
                                    }
                                });
                                return;
                            }
                        }
                        if (b) {
                            ToastUtils.showShort("Print Finished!");
                        } else {
                            ToastUtils.showShort("Print Result: " + s);
                        }
                        isPrinting.set(false);
                        finish();
                    }
                });
            }, 100);
        }
    });

//    public Bitmap convertReceiptToBitmap(TextView receiptView) {
//        float originalTextSize = receiptView.getTextSize();
//        int originalWidth = receiptView.getWidth();
//        int originalHeight = receiptView.getHeight();
//        receiptView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize * 1.5f);
//        receiptView.measure(
//                View.MeasureSpec.makeMeasureSpec(receiptView.getWidth(), View.MeasureSpec.EXACTLY),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//        );
//
//        Bitmap bitmap = Bitmap.createBitmap(
//                receiptView.getWidth(),
//                receiptView.getMeasuredHeight(),
//                Bitmap.Config.ARGB_8888
//        );
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.WHITE);
//        receiptView.layout(0, 0, receiptView.getWidth(), receiptView.getMeasuredHeight());
//        receiptView.draw(canvas);
//        receiptView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
//        receiptView.layout(0, 0, originalWidth, originalHeight);
//        receiptBitmap = bitmap;
//        return bitmap;
//    }

    public void requestOnlineAuth(boolean isICC, PaymentModel paymentModel) {
        AuthRequest authRequest = createAuthRequest(paymentModel);
        addSubscribe(apiService.sendMessage(AUTHFROMISSUER_URL, authRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    TRACE.i("online auth rsp code= " + response.getResult());
                    String onlineRspCode = (String) response.getResult();
                    if (response.isOk()) {
                        ToastUtils.showShort("Send online success");
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
        return new AuthRequest(deviceSn, amount, maskPan, cardOrg, transactionType, payType,transResult, DeviceUtils.getDeviceDate(),DeviceUtils.getDeviceTime());
    }
}