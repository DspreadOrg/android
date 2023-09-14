package com.dspread.demoui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.TRACE;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private ArrayList<Map<String, ?>> datas;

    private OnBluetoothItemClickListener mEvent;

    public BluetoothAdapter(Context context, ArrayList<Map<String, ?>> data) {
        this.mContext = context;
        this.datas = data;
    }

    public void setData(Map<String, ?> map) {
        this.datas.add(map);
        notifyDataSetChanged();
    }

    public void setListData(ArrayList<Map<String, ?>> data) {
        this.datas = data;
        notifyDataSetChanged();
    }


    public void clearData() {
        if (datas != null) {
            datas.clear();
            datas = null;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = View.inflate(mContext, R.layout.bt_qpos_item, null);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TRACE.d("bluetooth size" + datas.size());
        if (datas != null && datas.size() > 0) {
            if (holder instanceof MyViewHolder) {
                Map<String, ?> itemdata = (Map<String, ?>) datas.get(position);
                int idIcon = (Integer) itemdata.get("ICON");
                String sTitleName = (String) itemdata.get("TITLE");
                ((MyViewHolder) holder).iconImage.setBackgroundResource(idIcon);
                ((MyViewHolder) holder).txt.setText(sTitleName);
                ((MyViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(mContext, "item click:" + position, Toast.LENGTH_SHORT).show();
                        mEvent.onItemClick(position, itemdata);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return datas != null ? datas.size() : 0;
    }

    public interface OnBluetoothItemClickListener {

        void onItemClick(int position, Map<String, ?> itemdata);
    }


    public void setOnBluetoothItemClickListener(OnBluetoothItemClickListener event) {
        this.mEvent = event;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImage;
        private TextView txt;

        public MyViewHolder(View v) {
            super(v);
            iconImage = v.findViewById(R.id.item_iv_icon);
            txt = v.findViewById(R.id.item_tv_lable);
        }
    }
}