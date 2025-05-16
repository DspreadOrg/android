package com.dspread.pos.ui.setting.device_config;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.R;
import com.dspread.pos_new_android_app.databinding.ItemCurrencyBinding;

import java.util.ArrayList;
import java.util.List;

public class DeviceConfigAdapter extends RecyclerView.Adapter<DeviceConfigAdapter.ViewHolder> {
    private List<DeviceConfigItem> currencyList = new ArrayList<>();
    private OnCurrencyClickListener listener;

    public interface OnCurrencyClickListener {
        void onCurrencyClick(DeviceConfigItem item);
    }

    public DeviceConfigAdapter(OnCurrencyClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCurrencyBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_currency,
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceConfigItem item = currencyList.get(position);
        holder.binding.setCurrency(item);
        holder.binding.executePendingBindings();
        
        holder.itemView.setOnClickListener(v -> {
            // 更新选中状态
            for (DeviceConfigItem currency : currencyList) {
                currency.setSelected(currency == item);
            }
            notifyDataSetChanged();
            
            if (listener != null) {
                listener.onCurrencyClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencyList.size();
    }

    public void setItems(List<DeviceConfigItem> items) {
        TRACE.d("setItems size: " + (items != null ? items.size() : 0));
        this.currencyList.clear();
        this.currencyList.addAll(items);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCurrencyBinding binding;

        ViewHolder(ItemCurrencyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}