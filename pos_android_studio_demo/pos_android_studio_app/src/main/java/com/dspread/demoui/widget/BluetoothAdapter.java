package com.dspread.demoui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.dspread.demoui.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

public class BluetoothAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private ArrayList<Map<String, ?>> datas  = new ArrayList<>();

    private OnBluetoothItemClickListener mEvent;

    public BluetoothAdapter(Context context, ArrayList<Map<String, ?>> data) {
        this.mContext = context;
        this.datas = data;
    }

    public void setData(Map<String, ?> map) {
        if (datas!=null){
//            List list= map.values().stream().distinct().collect(Collectors.toList());
//            for (int i)
//                Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (key1, key2) -> key1);
            Map<String,?> resultMap = map.entrySet().stream().collect(

                    Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (key1, key2) -> key1)

            ).entrySet().stream().collect(

                    Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey, (key1, key2) -> key1)

            );
        this.datas.add(resultMap);

        }
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
//        TRACE.d("bluetooth size" + datas.size());
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
                        Log.w("adapter","item click:" + position);
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