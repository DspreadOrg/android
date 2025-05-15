package com.dspread.demoui.fragment;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.DeviceUtils;
import com.dspread.demoui.utils.TRACE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;


public class ScanFragment extends Fragment {

    private TextView tvScanInfo;
    private ImageButton btnScan;

    private String pkg;
    private String cls;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvScanInfo = view.findViewById(R.id.tv_scan_info);
        btnScan = view.findViewById(R.id.btn_scan);
        //initDevice(Build.MODEL);
        startScan();
    }


    @Override
    public void onStart() {
        super.onStart();
        TRACE.d("initDevice:----onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        TRACE.d("initDevice:----onResume");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }

    private boolean canshow = true;
    private CountDownTimer showTimer = new CountDownTimer(800, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            canshow = true;
        }

    };

    private void startScan() {
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DeviceUtils.isAppInstalled(getContext(), DeviceUtils.UART_AIDL_SERVICE_APP_PACKAGE_NAME)) {
                    //D30MstartScan();
                    pkg = "com.dspread.sdkservice";
                    cls = "com.dspread.sdkservice.base.scan.ScanActivity";
                } else {
                    if (!canshow) {
                        return;
                    }
                    canshow = false;
                    showTimer.start();
                    pkg = "com.dspread.components.scan.service";
                    cls = "com.dspread.components.scan.service.ScanActivity";
                }
                Intent intent = new Intent();
                ComponentName comp = new ComponentName(pkg, cls);
                try {
                    intent.putExtra("amount", "CHARGE ￥1");
                    intent.setComponent(comp);
                    launcher.launch(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w("e", "e==" + e);
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.scan_toast), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }
        String str = result.getData().getStringExtra("data");
        Log.w("scan", "strcode==" + str);
        tvScanInfo.setText(str);
    });
}