package com.dspread.pos.ui.setting.device_selection;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.pos_android_app.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private OnItemClickListener listener;
    private Activity context;

    public BluetoothDeviceAdapter(Activity activity,OnItemClickListener listener) {
        this.context = activity;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.tvDeviceAddress.setText(device.getName()+"("+device.getAddress()+")");
        Drawable imageBmp = device.getBondState() == BluetoothDevice.BOND_BONDED ? context.getDrawable(R.mipmap.bluetooth_blue) : context.getDrawable(R.mipmap.bluetooth_blue_unbond);
        holder.imgBluetooth.setImageDrawable(imageBmp);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void addDevice(BluetoothDevice device) {
        // Check if the device already exists
        for (BluetoothDevice existingDevice : devices) {
            if (existingDevice.getAddress().equals(device.getAddress())) {
                return;
            }
        }
        // Add a new device and refresh the list
        devices.add(device);
        notifyDataSetChanged();
    }

    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceAddress;
        ImageView imgBluetooth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceAddress = itemView.findViewById(R.id.tv_device_address);
            imgBluetooth = itemView.findViewById(R.id.img_device);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(BluetoothDevice device);
    }
}
