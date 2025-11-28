package com.dspread.pos.ui.ntag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.dspread.pos.TitleProviderListener;

import com.dspread.pos.posAPI.NtagCardServiceCallback;
import com.dspread.pos.ui.payment.PaymentViewModel;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos_android_app.R;
import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos_android_app.databinding.FragmentNtagBinding;

import java.util.Hashtable;


public class NtagFragment extends BaseFragment<FragmentNtagBinding, NtagViewModel> implements TitleProviderListener {
    private NtagCardServiceCallback ntagCardServiceCallback;

    @Override
    public String getTitle() {
        return "NTAG Card";
    }

    @Override
    public int initContentView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        return R.layout.fragment_ntag;
    }

    @Override
    public void initData(){
        ntagCardServiceCallback = new NtagCardCallback();
    }

    @Override
    public int initVariableId() {
        return 0; // 不再使用ViewModel
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册Ntag回调
        POSManager.getInstance().registerNtagCardCallback(ntagCardServiceCallback);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置按钮点击监听
        binding.pollOnNtagCard.setOnClickListener(v -> {
            TRACE.i("Poll NTag card clicked");
            POSManager.getInstance().pollOnNtagCard(5); // timeout=5
        });
        
        binding.finishNtagCard.setOnClickListener(v -> {
            TRACE.i("Finish NTag card clicked");
            POSManager.getInstance().finishNtagCard(5); // timeout=5
        });
        
        binding.writeNtagCard.setOnClickListener(v -> {
            String block = binding.blockAddressText.getText().toString();
            String data = binding.ketValueText.getText().toString();
            TRACE.i("Write NTag card clicked, block: " + block + ", data: " + data);
            POSManager.getInstance().writeNtagCard(block, data, 5); // timeout=5
        });
        
        binding.readNtagCard.setOnClickListener(v -> {
            String block = binding.blockAddressText.getText().toString();
            TRACE.i("Read NTag card clicked, block: " + block);
            POSManager.getInstance().readNtagCard(block, 5); // timeout=5
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销回调
        POSManager.getInstance().unregisterCallbacks();
    }

    // NTagCardCallbacks 实现

    private class NtagCardCallback implements NtagCardServiceCallback {
        @Override
        public void onSearchMifareCardResult(Hashtable<String, String> cardConfig) {
            requireActivity().runOnUiThread(() -> {
                String statuString = cardConfig.get("status");
                String cardTypeString = cardConfig.get("cardType");
                String cardUidLen = cardConfig.get("cardUidLen");
                String cardUid = cardConfig.get("cardUid");
                String cardAtsLen = cardConfig.get("cardAtsLen");
                String cardAts = cardConfig.get("cardAts");
                String ATQA = cardConfig.get("ATQA");
                String SAK = cardConfig.get("SAK");

                String result = "Status: " + statuString + "\n" +
                        "Card Type: " + cardTypeString + "\n" +
                        "UID Len: " + cardUidLen + "\n" +
                        "UID: " + cardUid + "\n" +
                        "ATS Len: " + cardAtsLen + "\n" +
                        "ATS: " + cardAts + "\n" +
                        "ATQA: " + ATQA + "\n" +
                        "SAK: " + SAK;

                binding.resultText.setText(result);
            });
        }

        @Override
        public void onFinishMifareCardResult(boolean result) {
            requireActivity().runOnUiThread(() -> {
                binding.resultText.setText("Finish result: " + (result ? "Success" : "Failed"));
            });
        }

        @Override
        public void writeMifareULData(String arg0) {
            requireActivity().runOnUiThread(() -> {
                String cardData = arg0;
                if(arg0 != null) {
                    binding.resultText.setText("Write result:\n"+arg0);

                }

            });
        }

        @Override
        public void getMifareReadData(Hashtable<String, String> arg0) {
            requireActivity().runOnUiThread(() -> {
                StringBuilder result = new StringBuilder("Read result:\n");
                for (String key : arg0.keySet()) {
                    result.append(key).append(": ").append(arg0.get(key)).append("\n");
                }
                binding.resultText.setText(result.toString());
            });
        }
    }
}
