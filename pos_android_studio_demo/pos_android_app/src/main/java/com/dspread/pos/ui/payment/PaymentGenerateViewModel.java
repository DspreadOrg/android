package com.dspread.pos.ui.payment;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.utils.TRACE;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;

public class PaymentGenerateViewModel extends BaseAppViewModel {
    private final MutableLiveData<String> amount = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> paymentResultEvent = new SingleLiveEvent<>();
    private final MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> transactionId = new MutableLiveData<>();

    public PaymentGenerateViewModel(@NonNull Application application) {
        super(application);
        isLoading.setValue(false);
        // 生成初始交易ID
        transactionId.setValue(generateTransactionId());
    }


    public void setContext(Context context) {
        this.context = context;
    }


    public LiveData<String> getAmount() {
        return amount;
    }

    public LiveData<Bitmap> getQrCodeBitmap() {
        return qrCodeBitmap;
    }

    public BindingCommand checkPayStatus = new BindingCommand(() -> {
        paymentResultEvent.setValue(true);
        Log.d("PaymentGenerate", "check pay status");
    });


    public void setPaymentAmount(String amountValue) {
        amount.setValue(amountValue);
        generateQRCode(amountValue);
    }

    private void generateQRCode(String amount) {
        new Thread(() -> {
            try {
                String paymentData = createPaymentData(amount, transactionId.getValue());

                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;

                // 计算最佳尺寸 - 屏幕宽度的60-80%，但不超过特定限制
                int maxQrSize = (int) (Math.min(screenWidth, screenHeight) * 0.9f);
                int minQrSize = (int) (Math.min(screenWidth, screenHeight) * 0.7f);

                // 确保尺寸合理
                int qrSize = Math.max(minQrSize, Math.min(maxQrSize, 600)); // 最大不超过600px
                TRACE.d("generateQRCode:"+qrSize);

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(
                        paymentData,
                        BarcodeFormat.QR_CODE,
                        qrSize,
                        qrSize
                );

                qrCodeBitmap.postValue(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                qrCodeBitmap.postValue(null);
            }
        }).start();
    }

    private String createPaymentData(String amount, String transactionId) {
        return "payment://transaction?" +
                "amount=" + amount +
                "&currency=USD" +
                "&merchant=DSPread" +
                "&timestamp=" + System.currentTimeMillis() +
                "&transaction_id=" + transactionId;
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String formatAmount(String amount) {
        try {
            double value = Double.parseDouble(amount.replace("$", "").trim());
            return String.format("$%.2f", value);
        } catch (NumberFormatException e) {
            return "$0.00";
        }
    }



}
