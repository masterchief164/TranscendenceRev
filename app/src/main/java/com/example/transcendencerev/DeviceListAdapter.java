package com.example.transcendencerev;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<Object> deviceList;

    public DeviceListAdapter(Context context, List<Object> deviceList) {
        this.context = context;
        this.deviceList = deviceList;

    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_info_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        final DeviceInfoModel deviceInfoModel = (DeviceInfoModel) deviceList.get(position);
        itemHolder.textName.setText(deviceInfoModel.getDeviceName());
        itemHolder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());

        itemHolder.linearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("deviceName", deviceInfoModel.getDeviceName());
            intent.putExtra("deviceAddress", deviceInfoModel.getDeviceHardwareAddress());
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        LinearLayout linearLayout;

        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.textViewDeviceName);
            textAddress = v.findViewById(R.id.textViewDeviceAddress);
            linearLayout = v.findViewById(R.id.linearLayoutDeviceInfo);
        }
    }
}