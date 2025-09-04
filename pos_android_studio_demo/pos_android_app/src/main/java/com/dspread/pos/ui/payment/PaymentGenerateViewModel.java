package com.dspread.pos.ui.payment;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.utils.TRACE;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PaymentGenerateViewModel extends BaseAppViewModel {
    private final MutableLiveData<String> amount = new MutableLiveData<>();
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

    public BindingCommand checkPayStatus = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
            Log.d("PaymentGenerate", "check pay status");
            // finish();
           /* Intent intent = new Intent(context, PrintTicketActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/

            Intent intent = new Intent(context, PaymentStatusActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            String mAmount = amount.getValue().startsWith("$") ? amount.getValue().substring(1) : amount.getValue();
            intent.putExtra("amount", mAmount);
            context.startActivity(intent);
        }
    });


    public void setPaymentAmount(String amountValue) {
        amount.setValue(amountValue);
        generateQRCode(amountValue);
    }

    private void generateQRCode(String amount) {
        new Thread(() -> {
            try {
                String paymentData = createPaymentData(amount, transactionId.getValue());

                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(
                        paymentData,
                        BarcodeFormat.QR_CODE,
                        500,
                        500
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
