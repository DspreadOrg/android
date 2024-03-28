package com.dspread.demoui.ui.fragment;

import static com.dspread.demoui.activity.BaseApplication.getApplicationInstance;
import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.dspread.demoui.utils.Utils.open;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.BaseApplication;
import com.dspread.demoui.activity.PaymentUartActivity;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.utils.ConstantUtil;
import com.dspread.demoui.utils.MoneyUtil;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.SpUtils;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.xpos.QPOSService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AutoFragment extends Fragment {

    private TitleUpdateListener myListener;
    private TextView sub;
    private TextView sucesssub;
    private TextView fialsub;
    private EditText etSub;
    private Button btnTrade, btnClearData;
    private String etSubstr;
    private int totalRecord;
    private int record;
    String transactionTypeString = "GOODS";
    private View view;
    SharedPreferencesUtil connectType;
    String conType;
    private TextView tvStartTradeTime;
    private TextView tvEndTradeTime;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.sendValue(getString(R.string.device_info));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_auto, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initData() {

        int successKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.SuccessKey, 0);
        sucesssub.setText(successKey + "");

        int subKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.SubKey, 0);
        sub.setText(subKey + "");

        int failKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.FailKey, 0);
        fialsub.setText(failKey + "");


        String startTradeTimeKey = SpUtils.getString(getApplicationInstance, ConstantUtil.StartTradeTimeKey, "");

        tvStartTradeTime.setText(startTradeTimeKey);


        String endTradeTimeKey = SpUtils.getString(getApplicationInstance, ConstantUtil.EndTradeTimeKey, "");
        tvEndTradeTime.setText(endTradeTimeKey);


        int tradeCountKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.TradeCountKey, 0);
        etSub.setText(tradeCountKey + "");

    }

    private void initView(View view) {
        sub = view.findViewById(R.id.sub);
        sucesssub = view.findViewById(R.id.sucesssub);
        fialsub = view.findViewById(R.id.fialsub);
        etSub = view.findViewById(R.id.et_sub);
        tvStartTradeTime = view.findViewById(R.id.tvStartTradeTime);
        tvEndTradeTime = view.findViewById(R.id.tvEndTradeTime);
        btnTrade = view.findViewById(R.id.btn_trade);
        btnClearData = view.findViewById(R.id.btn_clearData);
        btnTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前时间，
                String startTradeTime = getDateTime();
                SpUtils.putString(getApplicationInstance, ConstantUtil.StartTradeTimeKey, startTradeTime);
                String startTradeTimeKey = SpUtils.getString(getApplicationInstance, ConstantUtil.StartTradeTimeKey, "");
                tvStartTradeTime.setText(startTradeTimeKey);
                BaseApplication.getApplicationInstance = getActivity();
                etSubstr = etSub.getText().toString();
                if (etSubstr != null && !"".equals(etSubstr) && !"0".equals(etSubstr)) {
                   /* clearSPData();
                    sub.setText("0");
                    sucesssub.setText("0");
                    fialsub.setText("0");*/
                    totalRecord = Integer.parseInt(etSubstr);
                    SpUtils.putInt(getApplicationInstance, ConstantUtil.TradeCountKey, totalRecord);
                    Constants.transData.setInputMoney("1000");
                    Constants.transData.setPayType(transactionTypeString);
                    Constants.transData.setPayment("payment");
                    Constants.transData.setAutoTrade("autoTrade");
                    Intent intent = new Intent(getApplicationInstance, PaymentUartActivity.class);
                    getApplicationInstance.startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "请输入测试次数", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSPData();
            }
        });
    }

    private void clearSPData() {
        SpUtils.putInt(getApplicationInstance, ConstantUtil.SuccessKey, 0);
        SpUtils.putInt(getApplicationInstance, ConstantUtil.SubKey, 0);
        SpUtils.putInt(getApplicationInstance, ConstantUtil.FailKey, 0);
        SpUtils.putString(getApplicationInstance, ConstantUtil.EndTradeTimeKey, "");
        SpUtils.putString(getApplicationInstance, ConstantUtil.StartTradeTimeKey, "");
        SpUtils.putInt(getApplicationInstance, ConstantUtil.TradeCountKey, 0);
        initData();
    }

    @Override
    public void onResume() {
        Log.w("onResume", " AutoFragment onResume....");
        super.onResume();
        if (Constants.transData.getAutoTrade() != null && "StopTrade".equals(Constants.transData.getAutoTrade())) {
            Constants.transData.setAutoTrade("");
            // initInfo();
            initData();
            Toast.makeText(getActivity(), "停止交易", Toast.LENGTH_SHORT).show();
            Log.w("onResume", " AutoFragment onResume  停止交易");
        } else {
            BaseApplication.getApplicationInstance = getActivity();
            record = Constants.transData.getSub();
            record = SpUtils.getInt(getApplicationInstance, ConstantUtil.SubKey, 0);
            Log.w("onResume", " AutoFragment onResume  record111:" + record);

            /*if (Constants.transData.getSub() != 0 && !"".equals(Constants.transData.getSub())) {
                sub.setText(Constants.transData.getSub() + "");
            }
            if (Constants.transData.getSuccessSub() != 0 && !"".equals(Constants.transData.getSuccessSub())) {
                sucesssub.setText(Constants.transData.getSuccessSub() + "");
            }
            if (Constants.transData.getFialSub() != 0 && !"".equals(Constants.transData.getFialSub())) {
                fialsub.setText(Constants.transData.getFialSub() + "");
            }*/

            int successKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.SuccessKey, 0);
            sucesssub.setText(successKey + "");

            int subKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.SubKey, 0);
            sub.setText(subKey + "");

            int failKey = SpUtils.getInt(getApplicationInstance, ConstantUtil.FailKey, 0);
            fialsub.setText(failKey + "");

            if (record != 0 && !"".equals(record)) {
                totalRecord = SpUtils.getInt(getApplicationInstance, ConstantUtil.TradeCountKey, 0);
                if (totalRecord == record || record > totalRecord) {
                    Log.w("onResume", " AutoFragment onResume  totalRecord:" + totalRecord);
                    Toast.makeText(getActivity(), "交易完成", Toast.LENGTH_SHORT).show();
                    //initInfo();
                    String endTradeTime = getDateTime();
                    SpUtils.putString(getApplicationInstance, ConstantUtil.EndTradeTimeKey, endTradeTime);
                    String endTradeTimeKey = SpUtils.getString(getApplicationInstance, ConstantUtil.EndTradeTimeKey, "");
                    tvEndTradeTime.setText(endTradeTimeKey);
                } else {
                    Log.w("onResume", "AutoFragment onResume transData");

                   // if(record==3){
                       // int i = 1/0;
                   // }

                    transactionTypeString = "GOODS";
                    Constants.transData.setInputMoney("1000");
                    Constants.transData.setPayType(transactionTypeString);
                    Constants.transData.setPayment("payment");
                    Constants.transData.setAutoTrade("autoTrade");
                    PaymentUartActivity.flag = false;
                    Intent intent = new Intent(getApplicationInstance, PaymentUartActivity.class);
                    getApplicationInstance.startActivity(intent);
                }
            }
        }
    }

    @NonNull
    private static String getDateTime() {
        Date now2 = Calendar.getInstance().getTime();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String endTradeTime = format1.format(now2);
        return endTradeTime;
    }


    public void initInfo() {
        Constants.transData.setInputMoney("");
        Constants.transData.setPayType("");
        Constants.transData.setCashbackAmounts("");
        Constants.transData.setPayment("");
        Constants.transData.setAutoTrade("");
        Constants.transData.setSuccessSub(0);
        Constants.transData.setFialSub(0);
        Constants.transData.setSub(0);
    }

}