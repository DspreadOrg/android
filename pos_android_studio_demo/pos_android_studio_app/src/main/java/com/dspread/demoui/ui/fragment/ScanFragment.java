package com.dspread.demoui.ui.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;


public class ScanFragment extends Fragment {

    private TextView tvScanInfo;
    private ImageButton btnScan;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvScanInfo = view.findViewById(R.id.tv_scan_info);
        btnScan = view.findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
            return;
        }
        String str = result.getData().getStringExtra("data");
        tvScanInfo.setText(str);
        Log.w("scan", "strcode==" + str);
    });

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
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
}