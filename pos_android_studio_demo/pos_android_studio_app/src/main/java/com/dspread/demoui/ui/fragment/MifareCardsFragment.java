package com.dspread.demoui.ui.fragment;

import static com.dspread.demoui.ui.dialog.Mydialog.BLUETOOTH;
import static com.dspread.demoui.ui.dialog.Mydialog.UART;
import static com.dspread.demoui.ui.dialog.Mydialog.USB_OTG_CDC_ACM;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.PaymentActivity;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TitleUpdateListener;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 *
 * @author user
 */
public class MifareCardsFragment extends Fragment implements View.OnClickListener {
    private TitleUpdateListener myListener;
    private RelativeLayout operateMafireCards,operateMifareDesfire;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.sendValue(getString(R.string.menu_mifareCards));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mifarecards, container, false);
        operateMafireCards = view.findViewById(R.id.operate_mifareCards);
        operateMifareDesfire = view.findViewById(R.id.operate_mifareDesfire);
        operateMafireCards.setOnClickListener(this);
        operateMifareDesfire.setOnClickListener(this);
        return view;
    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.operate_mifareCards:
//                Mydialog.loading(getActivity(),"");
                operateMafireCards("MifareClassic");
                break;
            case R.id.operate_mifareDesfire:
//                Mydialog.loading(getActivity(),"");
                operateMafireCards("MifareDesfire");
                break;
            default:
                break;
        }
    }

    private void operateMafireCards(String MifareCards) {
        SharedPreferencesUtil connectType = SharedPreferencesUtil.getmInstance(getActivity());
        String conType = (String) connectType.get("conType", "");
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        Log.w("conType","conType=="+conType);
        if ("blue".equals(conType)) {
            Log.w("conType","conType=1="+conType);
            intent.putExtra("MifareCards", MifareCards);
            intent.putExtra("connect_type", BLUETOOTH);
            startActivity(intent);
        } else if ("uart".equals(conType)) {
            Log.w("conType","conType=2="+conType);
            intent.putExtra("MifareCards", MifareCards);
            intent.putExtra("connect_type", UART);
            startActivity(intent);
        } else if ("usb".equals(conType)) {
            Log.w("conType","conType=3="+conType);
            intent.putExtra("MifareCards", MifareCards);
            intent.putExtra("connect_type", USB_OTG_CDC_ACM);
            startActivity(intent);
        }
    }
}