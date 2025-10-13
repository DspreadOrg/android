package com.dspread.pos.ui.payment;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.dspread.pos.common.base.BaseAppViewModel;
import com.dspread.pos.ui.printer.activities.PrintTicketActivity;
import com.dspread.pos.utils.TRACE;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import java.util.Map;

import me.goldze.mvvmhabit.binding.command.BindingAction;
import me.goldze.mvvmhabit.binding.command.BindingCommand;

public class PaymentStatusViewModel extends BaseAppViewModel {
    private Context mContext;
    private Bitmap receiptBitmap;
    private String terAmount;
    private String maskedPAN;
    private String terminalTime;
    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public ObservableBoolean isSuccess = new ObservableBoolean(false);
    public ObservableField<String> amount = new ObservableField<>("");
    public ObservableBoolean isPrinting = new ObservableBoolean(false);
    public ObservableBoolean isShouwPrinting = new ObservableBoolean(false);
    public ObservableBoolean isD70DisplayScreen = new ObservableBoolean(false);

    public PaymentStatusViewModel(@NonNull Application application) {
        super(application);
    }
    public void setD70DisplayScreen(Boolean flag){
        isD70DisplayScreen.set(flag);
    }
    public void setTransactionFailed() {
        isSuccess.set(false);
    }

    public void setTransactionSuccess() {
        isSuccess.set(true);
    }


    public void displayAmount(String newAmount) {
        TRACE.d("displayAmount:"+newAmount);
        amount.set("$ " + newAmount);
    }
    public void sendTranReceipt(Map<String,String> map){
        terAmount=map.get("terAmount");
        maskedPAN=map.get("maskedPAN");
        terminalTime=map.get("terminalTime");
    }
    public BindingCommand continueTxnsCommand = new BindingCommand(() -> finish());
    public BindingCommand sendReceiptCommand = new BindingCommand(new BindingAction() {
        @Override
        public void call() {
/*            isPrinting.set(true);
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
            }, 100);*/

            Intent intent = new Intent(context, PrintTicketActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("terAmount", terAmount);
            intent.putExtra("maskedPAN",maskedPAN);
            intent.putExtra("terminalTime",terminalTime);
            context.startActivity(intent);
            finish();
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

}
