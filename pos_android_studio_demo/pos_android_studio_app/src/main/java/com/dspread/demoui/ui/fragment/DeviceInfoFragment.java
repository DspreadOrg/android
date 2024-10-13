package com.dspread.demoui.ui.fragment;

import static com.dspread.demoui.ui.dialog.Mydialog.BLUETOOTH;
import static com.dspread.demoui.ui.dialog.Mydialog.UART;
import static com.dspread.demoui.ui.dialog.Mydialog.USB_OTG_CDC_ACM;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.PaymentActivity;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TitleUpdateListener;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DeviceInfoFragment extends Fragment implements View.OnClickListener {
    TitleUpdateListener myListener;
    private RelativeLayout getPosid;
    private RelativeLayout getPosinfo;
    private RelativeLayout getUpdatekey;
    private RelativeLayout getKeycheckvalue;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.sendValue(getString(R.string.device_info));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        getPosid = view.findViewById(R.id.get_posid);
        getPosinfo = view.findViewById(R.id.get_posinfo);
        getUpdatekey = view.findViewById(R.id.get_updatekey);
        getKeycheckvalue = view.findViewById(R.id.get_keycheckvalue);
        getPosid.setOnClickListener(this);
        getPosinfo.setOnClickListener(this);
        getUpdatekey.setOnClickListener(this);
        getKeycheckvalue.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.get_posid:
                getposInfo("posid");
                break;
            case R.id.get_posinfo:
                getposInfo("posinfo");
                break;
            case R.id.get_updatekey:
                getposInfo("updatekey");
                break;

            case R.id.get_keycheckvalue:
                getposInfo("keycheckvalue");
                break;
            default:
                break;
        }
    }

    public void getposInfo(String posInfo) {
        SharedPreferencesUtil connectType = SharedPreferencesUtil.getmInstance(getActivity());
        String conType = (String) connectType.get("conType", "");
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        if ("blue".equals(conType)) {
            intent.putExtra("posinfo", posInfo);
            intent.putExtra("connect_type", BLUETOOTH);
            startActivity(intent);
        } else if ("uart".equals(conType)) {
            intent.putExtra("posinfo", posInfo);
            intent.putExtra("connect_type", UART);
            startActivity(intent);
        } else if ("usb".equals(conType)) {
            intent.putExtra("posinfo", posInfo);
            intent.putExtra("connect_type", USB_OTG_CDC_ACM);
            startActivity(intent);
        }


    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }


}