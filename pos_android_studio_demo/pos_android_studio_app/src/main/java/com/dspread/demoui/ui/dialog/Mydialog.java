package com.dspread.demoui.ui.dialog;

import static com.dspread.demoui.activity.BaseApplication.getApplicationInstance;
import static com.dspread.demoui.activity.BaseApplication.pos;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.PaymentActivity;
import com.dspread.demoui.activity.PaymentUartActivity;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.utils.MoneyUtil;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.Utils;
import com.dspread.demoui.widget.MyAdapter;
import com.dspread.demoui.widget.pinpad.keyboard.KeyBoardNumInterface;
import com.dspread.demoui.widget.pinpad.keyboard.KeyboardUtil;
import com.dspread.demoui.widget.pinpad.keyboard.MyKeyboardView;
import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.List;

public class Mydialog {
    public interface OnMyClickListener {
        void onCancel();

        void onConfirm();
    }

    /**
     * loading
     */
    public static Dialog Ldialog;

    public static void loading(Activity mContext, String msg) {
        if(mContext==null){
            return;
        }
        Ldialog = new Dialog(mContext);
        Ldialog.setContentView(R.layout.processing_dialog);
        Button confirmButton = Ldialog.findViewById(R.id.confirmButton);
        TextView messageTextView = Ldialog.findViewById(R.id.msgTextView);
        messageTextView.setText(msg);
        messageTextView.setTextSize(20);
        Window dialogWindow = Ldialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        Ldialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        lp.dimAmount = 0.4f;
        dialogWindow.setAttributes(lp);
        Ldialog.setCanceledOnTouchOutside(false);
//        Ldialog.setCancelable(false);
        if (!mContext.isFinishing()) {
            Ldialog.show();
        }
    }

    /*
     *Error message prompt box
     */
    public static AlertDialog ErrorDialog;

    public static void ErrorDialog(Activity mContext, String msg, OnMyClickListener listener) {
        if (mContext==null){
            return;
        }
        View view = View.inflate(mContext, R.layout.alert_dialog, null);
        Button mbtnConfirm = view.findViewById(R.id.btnConfirm);
        TextView mtvInfo = view.findViewById(R.id.tvInfo);
        mtvInfo.setText(msg);
        Button mbtnCancel = view.findViewById(R.id.btnCancel);
        mbtnCancel.setVisibility(View.GONE);

        mbtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirm();
                } else {
                    ErrorDialog.dismiss();
                    if (!msg.equals(mContext.getString(R.string.bad_swipe))) {
                        mContext.finish();
                    }
                }
            }
        });

        ErrorDialog = new AlertDialog.Builder(mContext).create();
        ErrorDialog.setCanceledOnTouchOutside(false);
        ErrorDialog.setCancelable(false);
//        if (msg.equals(mContext.getString(R.string.bad_swipe))) {
//            mbtnConfirm.setVisibility(View.GONE);
//            ErrorDialog.setCancelable(true);
//        }else{
//            ErrorDialog.setCancelable(false);
//            mbtnConfirm.setVisibility(View.VISIBLE);
//        }
        if (!mContext.isFinishing()) {
            ErrorDialog.show();
        }
        //显示对话框
        Window window = ErrorDialog.getWindow();
//        window.setWindowAnimations(R.style.popupAnimation);
        window.setBackgroundDrawable(null);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        Display d = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = ErrorDialog.getWindow().getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        ErrorDialog.getWindow().setAttributes(p);
        window.setContentView(view);

    }


    public static AlertDialog manualExitDialog;

    public static void manualExitDialog(Activity mContext, String msg, OnMyClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = View.inflate(mContext, R.layout.alert_dialog, null);
        View viewv = view.findViewById(R.id.view_v);
        viewv.setVisibility(View.VISIBLE);
        TextView mtvInfo = view.findViewById(R.id.tvInfo);
        mtvInfo.setText(msg);
        Button mbtnConfirm = view.findViewById(R.id.btnConfirm);
        Button mbtnCancel = view.findViewById(R.id.btnCancel);
        mbtnCancel.setVisibility(View.VISIBLE);
        mbtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onConfirm();
            }
        });
        mbtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
            }
        });

        manualExitDialog = builder.create();
        if (!mContext.isFinishing()) {
            manualExitDialog.show();
        }
        Window window = manualExitDialog.getWindow();
        window.setWindowAnimations(R.style.popupAnimation);
        window.setBackgroundDrawable(null);
        window.setGravity(Gravity.BOTTOM);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = manualExitDialog.getWindow().getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        manualExitDialog.getWindow().setAttributes(p);
        manualExitDialog.setCanceledOnTouchOutside(true);
        window.setContentView(view);
    }


    public static AlertDialog payTypeDialog;
    private static RecyclerView rvlist;
    private static String transactionTypeString = "GOODS";
    private static MyAdapter myAdapter;
    public static final int BLUETOOTH = 1;
    public static final int UART = 2;
    public static final int USB_OTG_CDC_ACM = 3;

    public static void payTypeDialog(Activity mContext, String amount, long inputMoney, String[] data) {
        payTypeDialog = new AlertDialog.Builder(mContext).create();
        if (!mContext.isFinishing()) {
            payTypeDialog.show();
        }
        Window window = payTypeDialog.getWindow();
//        window.setWindowAnimations(R.style.popupAnimation);
        window.setBackgroundDrawable(null);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = payTypeDialog.getWindow().getAttributes();
        p.height = (int) (d.getHeight() * 0.6);
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        payTypeDialog.getWindow().setAttributes(p);
        View view = View.inflate(mContext, R.layout.paytype_dialog_view, null);
        rvlist = view.findViewById(R.id.rv_list);
        rvlist.setLayoutManager(new LinearLayoutManager(mContext.getApplicationContext()));
        myAdapter = new MyAdapter(getArrayList(data));
        rvlist.setAdapter(myAdapter);
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String content) {
                if (Utils.islistFastClick()) {
                    SharedPreferencesUtil connectType = SharedPreferencesUtil.getmInstance(mContext);
                    String conType = (String) connectType.get("conType", "");
                    if ("CASHBACK".equals(content)) {
                        String inputMoneyString = String.valueOf(inputMoney);
                        cashBackPaymentDialog(mContext, inputMoneyString);
                    } else {
                        if (!"".equals(conType) && "uart".equals(conType)) {
                            transactionTypeString = content;
//                            Intent intent = new Intent(mContext, PaymentActivity.class);
                            String inputMoneyString = String.valueOf(inputMoney);
                            Log.w("inputMoneyString","inputMoneyString+"+inputMoneyString);
//                            intent.putExtra("inputMoney", inputMoneyString);
//                            intent.putExtra("paytype", transactionTypeString);
//                            intent.putExtra("connect_type", UART);
//                            mContext.startActivity(intent);
                            Constants.transData.setInputMoney(inputMoneyString);
                            Constants.transData.setPayType(transactionTypeString);
                            Constants.transData.setPayment("payment");
//                            pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
//                            pos.doTrade(20);
                            Intent intent = new Intent(getApplicationInstance, PaymentUartActivity.class);
                            getApplicationInstance.startActivity(intent);
//                            pos.getQposId();
                        } else if (!"".equals(conType) && "usb".equals(conType)) {
                            transactionTypeString = content;
                            Intent intent = new Intent(mContext, PaymentActivity.class);
                            intent.putExtra("amount", amount);
                            String inputMoneyString = String.valueOf(inputMoney);
                            intent.putExtra("inputMoney", inputMoneyString);
                            intent.putExtra("paytype", transactionTypeString);
                            intent.putExtra("conType", conType);
                            intent.putExtra("connect_type", USB_OTG_CDC_ACM);
                            mContext.startActivity(intent);
                        } else if (!"".equals(conType) && "blue".equals(conType)) {//blue
                            transactionTypeString = content;
                            Intent intent = new Intent(mContext, PaymentActivity.class);
                            intent.putExtra("amount", amount);
                            String inputMoneyString = String.valueOf(inputMoney);
                            intent.putExtra("inputMoney", inputMoneyString);
                            intent.putExtra("paytype", transactionTypeString);
                            intent.putExtra("connect_type", BLUETOOTH);
                            mContext.startActivity(intent);
                        }
                    }
                    payTypeDialog.dismiss();

                }
            }
        });
        window.setContentView(view);
        payTypeDialog.setCanceledOnTouchOutside(true);
        payTypeDialog.setCancelable(true);
    }

    private static List<String> getArrayList(String[] data) {
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            dataList.add(data[i]);
        }
        return dataList;
    }

    //Transaction confirmation information
    public static AlertDialog onlingDialog;

    public static void onlingDialog(Activity mContext, boolean isPinCanceled, OnMyClickListener listener) {
        View view = View.inflate(mContext, R.layout.processing_dialog, null);
        TextView textView = view.findViewById(R.id.messageTextView);
        LinearLayout btnlayout = view.findViewById(R.id.btn_layout);
        btnlayout.setVisibility(View.VISIBLE);
        textView.setTextSize(20);
        if (isPinCanceled) {
            textView.setText(R.string.replied_failed);
        } else {
            textView.setText(R.string.replied_success);
        }

        Button cancelButton = view.findViewById(R.id.cancelButton);
        view.findViewById(R.id.confirmButton).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        listener.onConfirm();
                    }
                });

        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onCancel();

                    }
                });
        onlingDialog = new AlertDialog.Builder(mContext).create();
        onlingDialog.setCanceledOnTouchOutside(false);
        onlingDialog.setCancelable(false);
        if (!mContext.isFinishing()) {
            onlingDialog.show();
        }
        Window window = onlingDialog.getWindow();
        window.setWindowAnimations(R.style.popupAnimation);
        window.setBackgroundDrawable(null);
        window.setGravity(Gravity.BOTTOM);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = onlingDialog.getWindow().getAttributes();
//        p.height = (int) (d.getHeight() * 0.3);
        p.width = (int) (d.getWidth() * 1);

        onlingDialog.getWindow().setAttributes(p);
        window.setContentView(view);
    }


    public static Dialog cashBackPaymentDialog;

    public static void cashBackPaymentDialog(Activity mContext, String inputMoney) {
        View view = View.inflate(mContext, R.layout.cashback_dialog, null);
        TextView textView = view.findViewById(R.id.messageTextView);
        textView.setTextSize(20);

        EditText etInputMoney = view.findViewById(R.id.et_inputMoney);
        etInputMoney.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        etInputMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (s.length() == 1 && s.toString().equals(".")) {
                    etInputMoney.setText("");
                }
                if (str.contains(".")) {
                    String[] strArr = str.split("\\.");
                    if (strArr.length > 1 && strArr[1].length() > 2) {
                        s.delete(str.length() - 1, str.length());
                    }
                }
                if (str.length() > 10 && !str.contains(".")) {
                    s.delete(str.length() - 1, str.length());
                }

            }
        });

        view.findViewById(R.id.confirmButton).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String cashbackAmounts = etInputMoney.getText().toString().trim();
                        if (!"".equals(cashbackAmounts) && !"0".equals(cashbackAmounts)) {
                            Double inputCashbackAmount = Double.valueOf(cashbackAmounts);
                            Long inputCashbackAmounts = MoneyUtil.yuan2fen(inputCashbackAmount);
                            String inputcashbackMoney = String.valueOf(inputCashbackAmounts);
                            SharedPreferencesUtil connectType = SharedPreferencesUtil.getmInstance(mContext);
                            String conType = (String) connectType.get("conType", "");

                            if (conType != null && "uart".equals(conType)) {
//                                Intent intent = new Intent(mContext, PaymentActivity.class);
                                String inputMoneyString = String.valueOf(inputMoney);
//                                intent.putExtra("inputMoney", inputMoneyString);
//                                intent.putExtra("paytype", "CASHBACK");
//                                intent.putExtra("cashbackAmounts", inputcashbackMoney);
//                                intent.putExtra("connect_type", 2);
//                                mContext.startActivity(intent);
                                Constants.transData.setInputMoney(inputMoneyString);
                                Constants.transData.setPayType("CASHBACK");
                                Constants.transData.setCashbackAmounts(inputcashbackMoney);
                                Constants.transData.setPayment("payment");
                                Intent intent = new Intent(getApplicationInstance, PaymentUartActivity.class);
                                getApplicationInstance.startActivity(intent);
                                cashBackPaymentDialog.dismiss();
                            } else if (conType != null && "usb".equals(conType)) {
                                Intent intent = new Intent(mContext, PaymentActivity.class);
                                String inputMoneyString = String.valueOf(inputMoney);
                                intent.putExtra("inputMoney", inputMoneyString);
                                intent.putExtra("paytype", "CASHBACK");
                                intent.putExtra("cashbackAmounts", inputcashbackMoney);
                                intent.putExtra("conType", conType);
                                intent.putExtra("connect_type", 3);
                                mContext.startActivity(intent);
                                cashBackPaymentDialog.dismiss();
                            } else if (conType != null && "blue".equals(conType)) {//blue
                                Intent intent = new Intent(mContext, PaymentActivity.class);
                                String inputMoneyString = String.valueOf(inputMoney);
                                intent.putExtra("inputMoney", inputMoneyString);
                                intent.putExtra("paytype", "CASHBACK");
                                intent.putExtra("cashbackAmounts", inputcashbackMoney);
                                intent.putExtra("connect_type", 1);
                                mContext.startActivity(intent);
                                cashBackPaymentDialog.dismiss();
                            }


                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.set_amount), Toast.LENGTH_SHORT).show();
                        }


                    }
                });


        cashBackPaymentDialog = new Dialog(mContext);
        cashBackPaymentDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        cashBackPaymentDialog.setCanceledOnTouchOutside(false);
        if (!mContext.isFinishing()) {
            cashBackPaymentDialog.show();
        }

        etInputMoney.setFocusable(true);
        etInputMoney.setFocusableInTouchMode(true);
        etInputMoney.requestFocus();
        etInputMoney.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etInputMoney, 0);
            }
        }, 100);

        Window window = cashBackPaymentDialog.getWindow();
//        window.setWindowAnimations(R.style.popupAnimation);
        window.setBackgroundDrawable(null);
        window.setGravity(Gravity.BOTTOM);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = cashBackPaymentDialog.getWindow().getAttributes();

        p.width = (int) (d.getWidth() * 1);

        cashBackPaymentDialog.getWindow().setAttributes(p);
        cashBackPaymentDialog.setContentView(view);
    }

    public static AlertDialog ShowInfoDialog;

    public static void ShowInfoDialog(Context mContext, String msg, OnMyClickListener listener) {
        if(mContext==null){
            return;
        }
        View view = View.inflate(mContext, R.layout.alert_dialog, null);
        Button mbtnConfirm = view.findViewById(R.id.btnConfirm);
        TextView mtvInfo = view.findViewById(R.id.tvInfo);
        mtvInfo.setText(msg);
        Button mbtnCancel = view.findViewById(R.id.btnCancel);
        mbtnCancel.setVisibility(View.GONE);
        mbtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirm();
                } else {
                    ShowInfoDialog.dismiss();
                }
            }
        });

        ShowInfoDialog = new AlertDialog.Builder(mContext).create();
        ShowInfoDialog.setCanceledOnTouchOutside(false);
//        ShowInfoDialog.setCancelable(false);
        ShowInfoDialog.show();
        //显示对话框
        Window window = ShowInfoDialog.getWindow();
        window.setWindowAnimations(R.style.popupAnimation);
        window.setBackgroundDrawable(null);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        Display d = wm.getDefaultDisplay();
        WindowManager.LayoutParams p = ShowInfoDialog.getWindow().getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.MATCH_PARENT;
        ShowInfoDialog.getWindow().setAttributes(p);
        window.setContentView(view);

    }

}
