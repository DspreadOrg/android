package com.dspread.pos.ui.mifare;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dspread.pos.posAPI.MifareServiceCallback;
import com.dspread.pos.posAPI.POSManager;

import java.util.Hashtable;

public abstract class BaseCardFragment extends Fragment implements MifareServiceCallback {

    protected boolean isPowerOn = false;
    protected String lastBlockAddress = "";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Register callback
        POSManager.getInstance().registerMifareCallback(this);
    }

    protected abstract void activateCard();
    protected abstract void deactivateCard();
    protected abstract void updatePowerStatus(boolean isOn);
    protected abstract void displayResult(String result);

    // MifareServiceCallback implementations
    @Override
    public void onActivateMifareCardResult(Hashtable<String, String> cardData) {
        requireActivity().runOnUiThread(() -> {
            StringBuilder result = new StringBuilder("Activate result:\n");
            if (cardData != null) {
                for (String key : cardData.keySet()) {
                    result.append(key).append(": ").append(cardData.get(key)).append("\n");
                }
                isPowerOn = true;
                updatePowerStatus(true);
            } else {
                result.append("Failed to activate card");
            }
            displayResult(result.toString());
        });
    }

    @Override
    public void onDeactivateMifareCardResult(boolean result) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Deactivate result: " + (result ? "Success" : "Failed"));
            if (result) {
                isPowerOn = false;
                updatePowerStatus(false);
            }
        });
    }

    @Override
    public void onReturnPowerOnNFCResult(boolean result, com.dspread.xpos.QPOSService.CardsType cardType, String atr, int atrLen) {
        requireActivity().runOnUiThread(() -> {
            String resultTxt = "Power On NFC result: " + result + "\n" +
                    "Card Type: " + cardType + "\n" +
                    "ATR: " + atr + "\n" +
                    "ATR Length: " + atrLen;
            displayResult(resultTxt);
            if (result) {
                isPowerOn = true;
                updatePowerStatus(true);
            }
        });
    }

    @Override
    public void onReturnPowerOffNFCResult(boolean result) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Power Off NFC result: " + (result ? "Success" : "Failed"));
            if (result) {
                isPowerOn = false;
                updatePowerStatus(false);
            }
        });
    }

    @Override
    public void onReturnNFCApduResult(boolean result, String apdu, int apduLen) {
        requireActivity().runOnUiThread(() -> {
            String resultTxt = "NFC APDU result: " + result + "\n" +
                    "APDU: " + apdu + "\n" +
                    "APDU Length: " + apduLen;
            displayResult(resultTxt);
        });
    }

    @Override
    public void onAuthenticateMifareCardResult(boolean flag) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Authenticate result: " + (flag ? "Success" : "Failed"));
        });
    }

    @Override
    public void onReadMifareBlockResult(String data) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Read Block result:\n" + data);
        });
    }

    @Override
    public void onWriteMifareBlockResult(boolean flag) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Write Block result: " + (flag ? "Success" : "Failed"));
        });
    }

    @Override
    public void getMifareFastReadData(Hashtable<String, String> data) {
        requireActivity().runOnUiThread(() -> {
            StringBuilder result = new StringBuilder("Fast Read result:\n");
            if (data != null) {
                for (String key : data.keySet()) {
                    result.append(key).append(": ").append(data.get(key)).append("\n");
                }
            }
            displayResult(result.toString());
        });
    }

    @Override
    public void getMifareReadData(Hashtable<String, String> data) {
        requireActivity().runOnUiThread(() -> {
            StringBuilder result = new StringBuilder("Ultralight Read result:\n");
            if (data != null) {
                for (String key : data.keySet()) {
                    result.append(key).append(": ").append(data.get(key)).append("\n");
                }
            }
            displayResult(result.toString());
        });
    }

    @Override
    public void writeMifareULData(String data) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Write Ultralight result:\n" + data);
        });
    }

    @Override
    public void onIncreaseValueResult(boolean result) {
        requireActivity().runOnUiThread(() -> {
            if (!result) {
                displayResult("Increment result: Failed");
            } else {
                displayResult("Increment result: Success, transferring...");
                // Automatically call transfer after successful increase
                POSManager.getInstance().transferBlock(lastBlockAddress);
            }
        });
    }

    @Override
    public void onDecreaseValueResult(boolean result) {
        requireActivity().runOnUiThread(() -> {
            if (!result) {
                displayResult("Decrement result: Failed");
            } else {
                displayResult("Decrement result: Success, transferring...");
                // Automatically call transfer after successful decrease
                POSManager.getInstance().transferBlock(lastBlockAddress);
            }
        });
    }

    @Override
    public void onTransferValueResult(boolean flag) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Transfer result: " + (flag ? "Success" : "Failed"));
        });
    }

    @Override
    public void onReadMifareValueResult(int value) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Read Value result: " + value);
        });
    }

    @Override
    public void onWriteMifareValueResult(boolean flag) {
        requireActivity().runOnUiThread(() -> {
            displayResult("Write Value result: " + (flag ? "Success" : "Failed"));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        POSManager.getInstance().unregisterCallbacks();
    }
}
