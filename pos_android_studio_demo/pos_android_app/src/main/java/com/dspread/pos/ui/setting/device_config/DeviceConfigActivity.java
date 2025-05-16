package com.dspread.pos.ui.setting.device_config;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.dspread.pos_new_android_app.BR;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ActivityCurrencySelectionBinding;

import java.util.ArrayList;
import java.util.List;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;

public class DeviceConfigActivity extends BaseActivity<ActivityCurrencySelectionBinding, DeviceConfigSelectionViewModel> {
    public static final String EXTRA_LIST_TYPE = "list_type";
    public static final int TYPE_CURRENCY = 1;
    public static final int TYPE_TRANSACTION = 2;
    public static final int TYPE_CARD_MODE = 3;
    private int currentType;
    
    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_currency_selection;
    }


    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();

        currentType = getIntent().getIntExtra(EXTRA_LIST_TYPE, TYPE_CURRENCY);
        // 设置返回按钮点击事件
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        
        // 设置搜索监听
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.filterCurrencies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 设置RecyclerView适配器
        DeviceConfigAdapter adapter = new DeviceConfigAdapter(item -> {
            Intent resultIntent = new Intent();
            if (currentType == TYPE_CURRENCY) {
                SPUtils.getInstance().put("currencyCode",item.getNumericCode());
                SPUtils.getInstance().put("currencyName",item.getCode());
                resultIntent.putExtra("currency_name", item.getCode());
            } else if (currentType == TYPE_TRANSACTION){
                SPUtils.getInstance().put("transactionType", item.getCode());
                resultIntent.putExtra("transaction_type", item.getCode());
            }else {
                SPUtils.getInstance().put("cardMode", item.getName());
                resultIntent.putExtra("card_mode", item.getName());
            }
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
        binding.currencyRecyclerView.setAdapter(adapter);

        viewModel.init(currentType);
        // 观察 ViewModel 中的货币列表数据变化
        viewModel.currencyList.observe(this, items -> {
            if (currentType == TYPE_CURRENCY) {
                handleCurrencyList(items, adapter);
            } else if (currentType == TYPE_TRANSACTION){
                handleTransactionList(items, adapter);
            }else {
                handleCardModeList(items,adapter);
            }
        });
    }

    private void handleCardModeList(List<DeviceConfigItem> items, DeviceConfigAdapter adapter) {
        String savedCardMode = SPUtils.getInstance().getString("cardMode", "");
        sortAndSetItems(items, adapter, savedCardMode);
    }
    private void handleCurrencyList(List<DeviceConfigItem> items, DeviceConfigAdapter adapter) {
        int savedCode = SPUtils.getInstance().getInt("currencyCode", 0);
        sortAndSetItems(items, adapter, savedCode);
    }

    private void handleTransactionList(List<DeviceConfigItem> items, DeviceConfigAdapter adapter) {
        String savedType = SPUtils.getInstance().getString("transactionType", "");
        sortAndSetItems(items, adapter, savedType);
    }

    private void sortAndSetItems(List<DeviceConfigItem> items, DeviceConfigAdapter adapter, Object savedValue) {
        if (savedValue != null && !savedValue.toString().isEmpty() && !"0".equals(savedValue.toString())) {
            DeviceConfigItem selectedItem = null;
            for (DeviceConfigItem item : items) {
                if ((currentType == TYPE_CURRENCY && item.getNumericCode() == (int)savedValue)
                        || (currentType == TYPE_TRANSACTION && item.getCode().equals(savedValue))
                        || (currentType == TYPE_CARD_MODE && item.getName().equals(savedValue))) {
                    selectedItem = item;
                    item.setSelected(true);
                    break;
                }
            }

            if (selectedItem != null) {
                List<DeviceConfigItem> sortedList = new ArrayList<>();
                sortedList.add(selectedItem);
                // 使用传统的循环替代 stream
                for (DeviceConfigItem item : items) {
                    if (item != selectedItem) {
                        sortedList.add(item);
                    }
                }
                adapter.setItems(sortedList);
            } else {
                adapter.setItems(items);
            }
        } else {
            adapter.setItems(items);
        }
    }
}