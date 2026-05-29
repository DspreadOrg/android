package com.dspread.pos.ui.mifare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dspread.pos.posAPI.ConnectionServiceCallback;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;

import me.goldze.mvvmhabit.utils.ToastUtils;

public class UltralightFragment extends BaseCardFragment {

    private View rootView;
    private Button deactivateBtn, activateBtn;
    private Button fastReadBtn;
    private Button readBlockBtn, writeBlockBtn, operateValueBtn;
    private Button readValueBtn, writeValueBtn;
    private TextView powerStatusText;
    
    private int currentOperationType = 0; // 0: Increase, 1: Decrease
    
    // Connection callback
    private ConnectionServiceCallback connectionCallback;

    public static UltralightFragment newInstance() {
        return new UltralightFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initConnectionCallback();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mifare_ultralight, container, false);
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
        fastReadBtn = rootView.findViewById(R.id.fastReadBtn);
        readBlockBtn = rootView.findViewById(R.id.readBlockBtn);
        writeBlockBtn = rootView.findViewById(R.id.writeBlockBtn);
        operateValueBtn = rootView.findViewById(R.id.operateValueBtn);
        readValueBtn = rootView.findViewById(R.id.readValueBtn);
        writeValueBtn = rootView.findViewById(R.id.writeValueBtn);
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
        // Setup operation type radio group
        RadioGroup operationTypeRadioGroup = rootView.findViewById(R.id.operationTypeRadioGroup);
        operationTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.blockValueRadio) {
                rootView.findViewById(R.id.blockValueLayout).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.financialValueLayout).setVisibility(View.GONE);
            } else if (checkedId == R.id.financialValueRadio) {
                rootView.findViewById(R.id.blockValueLayout).setVisibility(View.GONE);
                rootView.findViewById(R.id.financialValueLayout).setVisibility(View.VISIBLE);
            }
        });
        
        // Setup financial operation spinner (Increase/Decrease)
        ArrayAdapter<String> financialOpAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Increase", "Decrease"});
        financialOpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner financialOperationSpinner = rootView.findViewById(R.id.financialOperationSpinner);
        financialOperationSpinner.setAdapter(financialOpAdapter);
        financialOperationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentOperationType = position; // 0: Increase, 1: Decrease
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        activateBtn.setOnClickListener(v -> activateCard());
        deactivateBtn.setOnClickListener(v -> deactivateCard());
        fastReadBtn.setOnClickListener(v -> fastReadPages());
        readBlockBtn.setOnClickListener(v -> readBlock());
        writeBlockBtn.setOnClickListener(v -> writeBlock());
        operateValueBtn.setOnClickListener(v -> operateFinancialValue());
        readValueBtn.setOnClickListener(v -> readFinancialValue());
        writeValueBtn.setOnClickListener(v -> writeFinancialValue());
        
        // Initialize button state
        updatePowerStatus(false);
    }

    @Override
    protected void activateCard() {
        TRACE.i("Ultralight: Activate Card clicked");
        new Thread(() -> {
            // Check if device is ready before activating card
            if (!POSManager.getInstance().isDeviceConnected()) {
                TRACE.i("Ultralight: Device not ready, connecting first...");
                POSManager.getInstance().connect("", connectionCallback);
            } else {
                TRACE.i("Ultralight: Device already ready");
                POSManager.getInstance().registerConnectionCallback(connectionCallback);
            }
            // Now activate the card
            POSManager.getInstance().activateMifareCard(30);
        }).start();
    }

    @Override
    protected void deactivateCard() {
        TRACE.i("Ultralight: Deactivate Card clicked");
        POSManager.getInstance().deactivateMifareCard(5);
    }

    private void fastReadPages() {
        EditText startPageText = rootView.findViewById(R.id.startPageText);
        EditText endPageText = rootView.findViewById(R.id.endPageText);
        String startPage = startPageText.getText().toString();
        String endPage = endPageText.getText().toString();
        TRACE.i("Ultralight: Fast Read, startPage: " + startPage + ", endPage: " + endPage);
        
        if (startPage.isEmpty() || endPage.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill start and end page", Toast.LENGTH_SHORT).show();
            return;
        }
        
        POSManager.getInstance().fastReadMifareCardData(startPage, endPage, 10);
    }

    private void readBlock() {
        EditText blockValueAddressText = rootView.findViewById(R.id.blockValueAddressText);
        String blockAddress = blockValueAddressText.getText().toString();
        TRACE.i("Ultralight: Read Block, blockAddress: " + blockAddress);
        
        if (blockAddress.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill block address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        POSManager.getInstance().readMifareBlock(QPOSService.MifareCardType.UlTRALIGHT, blockAddress, 10);
    }

    private void writeBlock() {
        EditText blockValueAddressText = rootView.findViewById(R.id.blockValueAddressText);
        EditText blockWriteDataText = rootView.findViewById(R.id.blockWriteDataText);
        String blockAddress = blockValueAddressText.getText().toString();
        String writeData = blockWriteDataText.getText().toString();
        TRACE.i("Ultralight: Write Block, blockAddress: " + blockAddress + ", writeData: " + writeData);
        
        if (blockAddress.isEmpty() || writeData.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill block address and write data", Toast.LENGTH_SHORT).show();
            return;
        }
        
        POSManager.getInstance().writeMifareBlock(QPOSService.MifareCardType.UlTRALIGHT, blockAddress, writeData, 10);
    }

    private void operateFinancialValue() {
        EditText financialBlockAddressText = rootView.findViewById(R.id.financialBlockAddressText);
        EditText financialValueText = rootView.findViewById(R.id.financialValueText);
        String blockAddress = financialBlockAddressText.getText().toString();
        String valueData = financialValueText.getText().toString();
        TRACE.i("Ultralight: Operate Value, blockAddress: " + blockAddress + ", valueData: " + valueData + ", operation: " + (currentOperationType == 0 ? "Increase" : "Decrease"));
        
        if (blockAddress.isEmpty() || valueData.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill block address and value", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int value = Integer.parseInt(valueData);
            lastBlockAddress = blockAddress;
            
            if (currentOperationType == 0) {
                POSManager.getInstance().increaseValue(blockAddress, value, 10);
            } else {
                POSManager.getInstance().decreaseValue(blockAddress, value, 10);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid number for value", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFinancialValue() {
        EditText financialBlockAddressText = rootView.findViewById(R.id.financialBlockAddressText);
        String blockAddress = financialBlockAddressText.getText().toString();
        TRACE.i("Ultralight: Read Financial Value, blockAddress: " + blockAddress);
        
        if (blockAddress.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill block address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        POSManager.getInstance().readMifareValue(blockAddress, 10);
    }

    private void writeFinancialValue() {
        EditText financialBlockAddressText = rootView.findViewById(R.id.financialBlockAddressText);
        EditText financialValueText = rootView.findViewById(R.id.financialValueText);
        String blockAddress = financialBlockAddressText.getText().toString();
        String valueData = financialValueText.getText().toString();
        TRACE.i("Ultralight: Write Financial Value, blockAddress: " + blockAddress + ", valueData: " + valueData);
        
        if (blockAddress.isEmpty() || valueData.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill block address and value", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int value = Integer.parseInt(valueData);
            POSManager.getInstance().writeMifareValue(blockAddress, value, 10);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid number for value", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void updatePowerStatus(boolean isOn) {
        if (rootView != null) {
            isPowerOn = isOn;
            powerStatusText.setText(isOn ? "Status: Activated" : "Status: Not Activated");
            powerStatusText.setTextColor(getResources().getColor(isOn ? R.color.green : R.color.gray));
            
            activateBtn.setEnabled(!isOn);
            deactivateBtn.setEnabled(isOn);
            
            fastReadBtn.setEnabled(isOn);
            readBlockBtn.setEnabled(isOn);
            writeBlockBtn.setEnabled(isOn);
            operateValueBtn.setEnabled(isOn);
            readValueBtn.setEnabled(isOn);
            writeValueBtn.setEnabled(isOn);
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
