package com.dspread.pos.ui.printer.activities.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.dspread.pos_new_android_app.R;

import java.util.List;

public class PrinterAdapter extends RecyclerView.Adapter<PrinterAdapter.ViewHolder> {
    private List<String> dataList;

    public PrinterAdapter(List<String> dataList) {
        this.dataList = dataList;
    }

    private OnItemClickListener onItemClickListener;

    /**
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener
                                               onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.print_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String content = dataList.get(position);
        holder.tvpaytype.setText(content);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvpaytype;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvpaytype = itemView.findViewById(R.id.tv_paytype);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {

                        onItemClickListener.onItemClick(v, getLayoutPosition(), dataList.get(getLayoutPosition()));
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String content);
    }
}


