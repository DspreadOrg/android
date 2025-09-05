package com.dspread.pos.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.pos.ui.setting.device_config.DeviceConfigItem;
import com.dspread.pos_android_app.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class PaymentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private List<ListItem> items;

    private List<Integer> mipmapImageIds = new ArrayList<>();

    public interface OnItemClickListener {
        void OnItemClickListener(Transaction transaction);
    }

    public PaymentsAdapter(List<Transaction> payments, OnItemClickListener onItemClickListener) {
        this.items = new ArrayList<>();
        buildListItems(payments, false);
        this.onItemClickListener = onItemClickListener;
        addIconList();
    }

    private void addIconList() {
        mipmapImageIds.add(R.mipmap.ic_visa);
        mipmapImageIds.add(R.mipmap.ic_master);
        mipmapImageIds.add(R.mipmap.ic_amex);
        mipmapImageIds.add(R.mipmap.ic_discover);
        mipmapImageIds.add(R.mipmap.ic_jcb);
    }

    public void setShowCategorized(boolean showCategorized, List<Transaction> payments) {
        buildListItems(payments, showCategorized);
        notifyDataSetChanged();
    }

    private void buildListItems(List<Transaction> payments, boolean categorized) {
        items.clear();

        if (categorized) {
            // 按月份分类显示
            Map<String, List<Transaction>> paymentsByMonth = new LinkedHashMap<>();

            // 分组支付记录按月份
            for (Transaction payment : payments) {
                String date = payment.getRequestDate();
                String month = extractMonthFromDate(date);
                if (!paymentsByMonth.containsKey(month)) {
                    paymentsByMonth.put(month, new ArrayList<>());
                }
                paymentsByMonth.get(month).add(payment);
            }

            // 构建带标题的列表
            for (Map.Entry<String, List<Transaction>> entry : paymentsByMonth.entrySet()) {
                items.add(new ListItem(entry.getKey())); // 添加月份标题
                for (Transaction payment : entry.getValue()) {
                    items.add(new ListItem(payment)); // 添加支付记录
                }
            }
        } else {
            // 默认显示所有支付记录，无标题
            for (Transaction payment : payments) {
                items.add(new ListItem(payment));
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == ListItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_transaction, parent, false);
            return new PaymentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ListItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerText.setText(item.getHeaderText());
        } else if (holder instanceof PaymentViewHolder) {
            PaymentViewHolder paymentHolder = (PaymentViewHolder) holder;
            Transaction payment = item.getPayment();

            paymentHolder.dateText.setText(payment.getRequestDate());
            paymentHolder.amountText.setText(payment.getAmount() + "");
            paymentHolder.cardInfoText.setText(payment.getMaskPan());
            paymentHolder.statusText.setText(payment.getTransResult());
            if (payment.getCardOrg().equalsIgnoreCase("visa")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(0));
            }
            if (payment.getCardOrg().equalsIgnoreCase("master")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(1));
            }
            if (payment.getCardOrg().equalsIgnoreCase("amex")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(2));
            }
            if (payment.getCardOrg().equalsIgnoreCase("discover")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(3));
            }
            if (payment.getCardOrg().equalsIgnoreCase("jcb")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(4));
            }

            // Set amount color based on value
            /*if (payment.getAmount().startsWith("-")) {
                paymentHolder.amountText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.red));
            } else {*/
            paymentHolder.amountText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.green));
            // }

            // Set status color
            if ("Voided".equalsIgnoreCase(payment.getTransResult())) {
                paymentHolder.statusText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.gray));
            } else {
                paymentHolder.statusText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.green));
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.OnItemClickListener(payment);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    // ViewHolder for header items
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.header_text);
        }
    }

    // ViewHolder for payment items
    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        ImageView cardIcon;
        TextView dateText, amountText, cardInfoText, statusText;

        public PaymentViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.date_text);
            amountText = itemView.findViewById(R.id.amount_text);
            cardInfoText = itemView.findViewById(R.id.card_info_text);
            statusText = itemView.findViewById(R.id.status_text);
            cardIcon = itemView.findViewById(R.id.card_icon);
        }
    }


    private String extractMonthFromDate(String date) {
        try {
            String[] parts = date.split("-");
            if (parts.length >= 2) {
                int monthNum = Integer.parseInt(parts[1]);
                String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                if (monthNum >= 1 && monthNum <= 12) {
                    return months[monthNum - 1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}