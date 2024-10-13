package com.dspread.demoui.ui.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.BaseApplication;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.MoneyUtil;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TitleUpdateListener;


public class HomeFragment extends Fragment implements View.OnClickListener {
    private TextView inputMoneyYuanText, inputMoneyFenText;
    private long inputMoney = 0;
    private Bundle bundle;
    private Button mConfirm;
    private TextView mAmount;
    private String amount = "";
    SharedPreferencesUtil connectType;
    String conType;
    View view;
    TitleUpdateListener myListener;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_input_money, null);
        getActivity().setTitle(getString(R.string.menu_payment));
        initView(view);
        return view;
    }
    protected void initView(View view) {
        inputMoneyYuanText = view.findViewById(R.id.inputMoneyYuanText);
        inputMoneyFenText = view.findViewById(R.id.inputMoneyFenText);
        Button btn_num00 = view.findViewById(R.id.btn_num00);
        btn_num00.setOnClickListener(this);
        Button btn_num0 = view.findViewById(R.id.btn_num0);
        btn_num0.setOnClickListener(this);
        Button btn_num1 = view.findViewById(R.id.btn_num1);
        btn_num1.setOnClickListener(this);
        Button btn_num2 = view.findViewById(R.id.btn_num2);
        btn_num2.setOnClickListener(this);
        Button btn_num3 = view.findViewById(R.id.btn_num3);
        btn_num3.setOnClickListener(this);
        Button btn_num4 = view.findViewById(R.id.btn_num4);
        btn_num4.setOnClickListener(this);
        Button btn_num5 = view.findViewById(R.id.btn_num5);
        btn_num5.setOnClickListener(this);
        Button btn_num6 = view.findViewById(R.id.btn_num6);
        btn_num6.setOnClickListener(this);
        Button btn_num7 = view.findViewById(R.id.btn_num7);
        btn_num7.setOnClickListener(this);
        Button btn_num8 = view.findViewById(R.id.btn_num8);
        btn_num8.setOnClickListener(this);
        Button btn_num9 = view.findViewById(R.id.btn_num9);

        btn_num9.setOnClickListener(this);
        ImageView btn_num_clear = view.findViewById(R.id.btn_num_clear);
        btn_num_clear.setOnClickListener(this);
        mConfirm = view.findViewById(R.id.btn_confirm);
        mConfirm.setOnClickListener(this);
        mAmount = view.findViewById(R.id.tv_amount);
        connectType = SharedPreferencesUtil.getmInstance(getActivity());
        conType = (String) connectType.get("conType", "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_num0:
            case R.id.btn_num1:
            case R.id.btn_num2:
            case R.id.btn_num3:
            case R.id.btn_num4:
            case R.id.btn_num5:
            case R.id.btn_num6:
            case R.id.btn_num7:
            case R.id.btn_num8:
            case R.id.btn_num9:

                if ((inputMoney + "").length() < 12) {
                    inputMoney = Long.parseLong(inputMoney + ((Button) v).getText().toString());

                    inputMoneySetText();
                }
                break;

            case R.id.btn_num00:

                if ((inputMoney + "").length() < 11) {
                    inputMoney = Long.parseLong(inputMoney + ((Button) v).getText().toString());

                    inputMoneySetText();
                }

                break;

            case R.id.btn_num_clear:
                if (inputMoney > 0) {
                    inputMoney = inputMoney / 10;
                    inputMoneySetText();
                }
                break;
            case R.id.btn_confirm:
                BaseApplication.getApplicationInstance=getActivity();
                if (inputMoney > 0) {
                    if (!canshow) {
                        return;
                    }
                    canshow = false;
                    showTimer.start();
                    Mydialog.payTypeDialog(getActivity(), amount, inputMoney, data);

                } else {
                    Toast.makeText(getActivity(), getString(R.string.set_amount), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
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
        inputMoneySetText();
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
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
}

