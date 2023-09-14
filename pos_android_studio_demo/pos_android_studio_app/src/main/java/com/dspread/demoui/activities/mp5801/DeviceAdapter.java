package com.dspread.demoui.activities.mp5801;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.helper.printer.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<Device> devices = new ArrayList<>();

    public void setInfo(List<Device> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
    }

    public DeviceAdapter(Context context, List<Device> devices) {
        this.context = context;
//        setInfo(devices);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.simple_list_item_2, null);
            holder = new ViewHolder();
            holder.deviceName = (TextView) convertView.findViewById(R.id.title);
            holder.deviceDescription = (TextView) convertView.findViewById(R.id.des);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Device device = devices.get(position);
        holder.deviceName.setText(device.deviceName);
        holder.deviceDescription.setText(device.deviceAddress);
        return convertView;
    }

    private class ViewHolder {

        private TextView deviceName;
        private TextView deviceDescription;
    }
}
