package com.dspread.pos.ui.mifare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dspread.pos.posAPI.ConnectionServiceCallback;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import me.goldze.mvvmhabit.utils.ToastUtils;

public class DesfireFragment extends BaseCardFragment {

    private View rootView;
    private Button deactivateBtn, activateBtn;
    private Button sendApduBtn;
    private TextView powerStatusText;
    
    // Connection callback
    private ConnectionServiceCallback connectionCallback;

    public static DesfireFragment newInstance() {
        return new DesfireFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConnectionCallback();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mifare_desfire, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
        setupUI();
    }

    private void initializeViews() {
        deactivateBtn = rootView.findViewById(R.id.deactivateBtn);
        activateBtn = rootView.findViewById(R.id.activateBtn);
        powerStatusText = rootView.findViewById(R.id.powerStatusText);
        sendApduBtn = rootView.findViewById(R.id.sendApduBtn);
    }

    private void initConnectionCallback() {
        connectionCallback = new ConnectionServiceCallback() {
            @Override
            public void onRequestNoQposDetected() {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showLong("Device connected fail");
                });
            }

            @Override
            public void onRequestQposConnected() {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showLong("Device connected");
                });
            }

            @Override
            public void onRequestQposDisconnected() {
                requireActivity().runOnUiThread(() -> {
                    ToastUtils.showLong("Device disconnected");
                });
            }
        };
    }

    private void setupUI() {
        activateBtn.setOnClickListener(v -> activateCard());
        deactivateBtn.setOnClickListener(v -> deactivateCard());
        sendApduBtn.setOnClickListener(v -> sendApdu());
        
        // Initialize button state
        updatePowerStatus(false);
    }

    @Override
    protected void activateCard() {
        TRACE.i("Desfire: Power On NFC clicked");
        new Thread(() -> {
            // Check if device is ready before powering on NFC
            if (!POSManager.getInstance().isDeviceConnected()) {
                TRACE.i("Desfire: Device not ready, connecting first...");
                POSManager.getInstance().connect("", connectionCallback);
            } else {
                TRACE.i("Desfire: Device already ready");
                POSManager.getInstance().registerConnectionCallback(connectionCallback);
            }
            // Now power on NFC
            POSManager.getInstance().powerOnNFC(false, 30);
        }).start();
    }

    @Override
    protected void deactivateCard() {
        TRACE.i("Desfire: Power Off NFC clicked");
        POSManager.getInstance().powerOffNFC(5);
    }

    private void sendApdu() {
        EditText apduText = rootView.findViewById(R.id.apduText);
        String apdu = apduText.getText().toString();
        TRACE.i("Desfire: Send APDU, apdu: " + apdu);
        
        if (apdu.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill APDU command", Toast.LENGTH_SHORT).show();
            return;
        }
        
        POSManager.getInstance().sendApduByNFC(apdu, 10);
    }

    @Override
    protected void updatePowerStatus(boolean isOn) {
        if (rootView != null) {
            isPowerOn = isOn;
            powerStatusText.setText(isOn ? "Status: NFC Powered On" : "Status: Not Activated");
            powerStatusText.setTextColor(getResources().getColor(isOn ? R.color.green : R.color.gray));
            
            activateBtn.setEnabled(!isOn);
            deactivateBtn.setEnabled(isOn);
            
            sendApduBtn.setEnabled(isOn);
        }
    }

    @Override
    protected void displayResult(String result) {
        if (rootView != null) {
            EditText resultText = rootView.findViewById(R.id.resultText);
            resultText.setText(result);
        }
    }
}
