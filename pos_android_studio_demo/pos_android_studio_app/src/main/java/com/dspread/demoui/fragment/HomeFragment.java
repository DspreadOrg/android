package com.dspread.demoui.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;
import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.MoneyUtil;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.demoui.widget.amountKeyboard.KeyboardUtil;
import com.dspread.demoui.widget.amountKeyboard.KeyboardUtil.OnOkClick;

import cn.hutool.core.collection.EnumerationIter;


public class HomeFragment extends Fragment {
    private TextView inputMoneyYuanText, inputMoneyFenText;
    private long inputMoney = 0;
    private Bundle bundle;
    private Button mConfirm;
    private TextView mAmount;
    private String amount = "";
    SharedPreferencesUtil connectType;
    String conType;
    View view;
    private ImageView btn_num_clear;
    private static KeyboardUtil keyboardUtil;
    private TextView edt_amount;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_input_money1, null);
        getActivity().setTitle(getString(R.string.menu_payment));
        initView(view);
        return view;
    }
    protected void initView(View view) {
        edt_amount = view.findViewById(R.id.txt_amount);
        if(getActivity() != null && !getActivity().isFinishing()){
            keyboardUtil = new KeyboardUtil(getActivity(),view,false);
            keyboardUtil.attachTo(edt_amount);
        }
        keyboardUtil.setOnOkClick(new OnOkClick() {
            @Override
            public void onOkClick() {
                BaseApplication.getApplicationInstance=getActivity();
                Double dAmount = Double.parseDouble(edt_amount.getText().toString().substring(1));
                TRACE.i("dAmount = "+dAmount);
                if(dAmount > 0){
                    inputMoney = Math.round(dAmount * 100);
                    if (!canshow) {
                        return;
                    }
                    canshow = false;
                    showTimer.start();
                    Mydialog.payTypeDialog(getActivity(), "", inputMoney, data);

                } else {
                    Toast.makeText(getActivity(), getString(R.string.set_amount), Toast.LENGTH_SHORT).show();
                }
            }
        });
        connectType = SharedPreferencesUtil.getInstance(getActivity());
        conType = (String) connectType.get("conType", "");
    }

    private void inputMoneySetText() {
        String inputMoneyString = MoneyUtil.fen2yuan(inputMoney);
        inputMoneyYuanText.setText(inputMoneyString.substring(0, inputMoneyString.length() - 2));
        inputMoneyFenText.setText(inputMoneyString.substring(inputMoneyString.length() - 2));
        amount = "¥" + inputMoneyString.substring(0, inputMoneyString.length() - 2) + inputMoneyString.substring(inputMoneyString.length() - 2);
        mAmount.setText(amount);

    }

    @Override
    public void onResume() {
        super.onResume();
        inputMoney = 0;
        edt_amount.setText("¥0.00");
    }

    private String[] data = {"GOODS", "SERVICES", "CASH", "CASHBACK","PURCHASE_REFUND","INQUIRY",
            "TRANSFER", "ADMIN", "CASHDEPOSIT",
            "PAYMENT", "PBOCLOG||ECQ_INQUIRE_LOG", "SALE",
            "PREAUTH", "ECQ_DESIGNATED_LOAD", "ECQ_UNDESIGNATED_LOAD",
            "ECQ_CASH_LOAD", "ECQ_CASH_LOAD_VOID", "CHANGE_PIN", "REFOUND", "SALES_NEW"};


    private boolean canshow = true;
    private CountDownTimer showTimer = new CountDownTimer(500, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            canshow = true;
        }

    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TRACE.i("home on keydown = "+keyCode);
        if(keyCode == 4){

        }else {
            keyboardUtil.getmOnKeyboardActionListener().onKey(keyCode, null);
        }
        return true;
    }
}

