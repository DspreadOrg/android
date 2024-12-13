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
import com.dspread.demoui.scan.D30MScanner;
import com.dspread.demoui.utils.TRACE;
import com.dspread.sdkdevservice.aidl.constant.SDKDevConstant;
import com.dspread.sdkdevservice.aidl.scanner.SDKScanListener;
import com.dspread.sdkdevservice.aidl.scanner.SDKScanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;


public class ScanFragment extends Fragment {

    private TextView tvScanInfo;
    private ImageButton btnScan;
    private D30MScanner d30MScanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvScanInfo = view.findViewById(R.id.tv_scan_info);
        btnScan = view.findViewById(R.id.btn_scan);
        initDevice(Build.MODEL);
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

    private void initDevice(String model) {
        switch (model) {
            case "D30M":
            case "D50":
                TRACE.d("initDevice:----" + model);
                try {
                    d30MScanner = new D30MScanner();
                    d30MScanner.initScannerDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
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
                if (Build.MODEL.equalsIgnoreCase("D30M") || Build.MODEL.equalsIgnoreCase("D50")) {
                    D30MstartScan();
                } else {
                    if (!canshow) {
                        return;
                    }
                    canshow = false;
                    showTimer.start();
                    Intent intent = new Intent();
                    ComponentName comp = new ComponentName("com.dspread.components.scan.service", "com.dspread.components.scan.service.ScanActivity");
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
            }
        });
    }

    private void D30MstartScan() {
        try {
            SDKScanner scannerDevice = d30MScanner.getScannerDevice();
            Bundle bundle = new Bundle();
            bundle.putInt("deviceType", SDKDevConstant.DeviceType.INTERNAL);
            bundle.putString("title", "Scanning");
            bundle.putString("message", "Scanning Code");
            bundle.putString("information", "Amount 0.01");
            bundle.putInt("cameraId", SDKDevConstant.CameraId.BACK);
            bundle.putInt("timeout", 60);
            bundle.putInt("scanMode", 0);
            scannerDevice.startScan(bundle, initScannerListener());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private SDKScanListener.Stub initScannerListener() {
        return new SDKScanListener.Stub() {
            @Override
            public void onSuccess(Bundle respData) throws RemoteException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String barcode = respData.getString("barcode");
                        tvScanInfo.setText(barcode);
                    }
                });
            }

            @Override
            public void onError(int errorCode, String errorMsg) throws RemoteException {
            }

            public void onCancel() throws RemoteException {
            }

            public void onTimeout() throws RemoteException {
            }
        };
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