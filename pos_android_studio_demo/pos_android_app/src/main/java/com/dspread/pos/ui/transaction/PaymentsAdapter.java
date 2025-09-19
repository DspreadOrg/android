package com.dspread.pos.ui.transaction;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.pos.ui.setting.device_config.DeviceConfigItem;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos_android_app.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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
        mipmapImageIds.add(R.mipmap.ic_unionpay);
    }

    public void refreshAdapter(boolean showCategorized, List<Transaction> payments) {
        setShowCategorized(showCategorized, payments);
    }

    public void setShowCategorized(boolean showCategorized, List<Transaction> payments) {
        buildListItems(payments, showCategorized);
        notifyDataSetChanged();
    }

    private void buildListItems(List<Transaction> payments, boolean categorized) {
        items.clear();
        if (categorized) {
            // 按月份分类显示，同时计算每个月的总金额
            Map<String, MonthSummary> paymentsByMonth = new LinkedHashMap<>();

            // 分组支付记录按月份并计算总金额
            for (Transaction payment : payments) {
                String date = payment.getTransactionDate();
                String month = extractMonthFromDate(date);

                if (!paymentsByMonth.containsKey(month)) {
                    paymentsByMonth.put(month, new MonthSummary(month, new ArrayList<>(), BigDecimal.ZERO));
                }

                MonthSummary monthSummary = paymentsByMonth.get(month);
                monthSummary.getTransactions().add(payment);

                // 累加金额（假设金额为正数，如果是负数需要特殊处理）
                try {
                    BigDecimal amount = new BigDecimal(payment.getAmount());
                    monthSummary.setTotalAmount(monthSummary.getTotalAmount().add(amount));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 构建带标题的列表
            for (Map.Entry<String, MonthSummary> entry : paymentsByMonth.entrySet()) {
                MonthSummary monthSummary = entry.getValue();
                items.add(new ListItem(monthSummary.getMonth(), monthSummary.getTotalAmount())); // 添加月份标题和总金额

                for (Transaction payment : monthSummary.getTransactions()) {
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


    private static class MonthSummary {
        private String month;
        private List<Transaction> transactions;
        private BigDecimal totalAmount;

        public MonthSummary(String month, List<Transaction> transactions, BigDecimal totalAmount) {
            this.month = month;
            this.transactions = transactions;
            this.totalAmount = totalAmount;
        }

        public String getMonth() {
            return month;
        }

        public List<Transaction> getTransactions() {
            return transactions;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
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

    private DecimalFormat amountFormat = new DecimalFormat("#,##0.00");

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ListItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.headerText.setText(item.getHeaderText());
            headerHolder.headerAmount.setText("$" + amountFormat.format(item.getMonthTotalAmount()));
        } else if (holder instanceof PaymentViewHolder) {
            PaymentViewHolder paymentHolder = (PaymentViewHolder) holder;
            Transaction payment = item.getPayment();
            paymentHolder.dateText.setText(payment.getTransactionDate() != null ? payment.getTransactionDate().replace("-", "/") : payment.getTransactionDate());
            String amount = DeviceUtils.convertAmountToCents(new BigDecimal(payment.getAmount()).toPlainString());
            paymentHolder.amountText.setText("$" + amount);
            paymentHolder.cardInfoText.setText(payment.getMaskPan().replaceAll("[fFXx]", "*") + " - " + payment.getTransactionTime());
            paymentHolder.statusText.setText(payment.getTransResult());
            if (payment.getCardOrg().equalsIgnoreCase("visa")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(0));
            }
            if (payment.getCardOrg().equalsIgnoreCase("masterCard")) {
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
            if (payment.getCardOrg().equalsIgnoreCase("UnionPay")) {
                paymentHolder.cardIcon.setImageResource(mipmapImageIds.get(5));
            }

            // Set amount color based on value
            /*if (payment.getAmount().startsWith("-")) {
                paymentHolder.amountText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.red));
            } else {*/

            paymentHolder.amountText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.transaction_amount_text_color));            // }

            // Set status color
            if ("Voided".equalsIgnoreCase(payment.getTransResult())) {
                paymentHolder.statusText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.gray));
            } else {
                paymentHolder.statusText.setTextColor(ContextCompat.getColor(paymentHolder.itemView.getContext(), R.color.white));
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
        TextView headerText, headerAmount;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.header_text);
            headerAmount = itemView.findViewById(R.id.header_amount);
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
            if (date != null) {
                String[] parts = date.split("-");
                if (parts.length >= 2) {
                    int monthNum = Integer.parseInt(parts[1]);
                    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                    if (monthNum >= 1 && monthNum <= 12) {
                        return months[monthNum - 1];
                    }
                }
            } else {
                return "Unknown";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}