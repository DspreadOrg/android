package com.dspread.demoui.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.printer.BarCodeActivity;
import com.dspread.demoui.activity.printer.BitmapActivity;
import com.dspread.demoui.activity.printer.PrintFunctionMultiActivity;
import com.dspread.demoui.activity.printer.PrintTicketActivity;
import com.dspread.demoui.activity.printer.PrintTextActivity;
import com.dspread.demoui.activity.printer.PrinterStatusActivity;
import com.dspread.demoui.activity.printer.QRCodeActivity;

public class PrinterHelperFragment extends Fragment {
    private RecyclerView printerWorkList;

    private final DemoDetails[] demos = {

            new DemoDetails(R.string.function_text, R.drawable.function_text,
                    PrintTextActivity.class),
            new DemoDetails(R.string.function_qrcode, R.drawable.function_qr,
                    QRCodeActivity.class),
            new DemoDetails(R.string.function_barcode, R.drawable.function_barcode,
                    BarCodeActivity.class),
            new DemoDetails(R.string.function_pic, R.drawable.function_pic,
                    BitmapActivity.class),
            new DemoDetails(R.string.function_multi, R.drawable.function_multi,
                    PrintFunctionMultiActivity.class),
            new DemoDetails(R.string.print_ticket, R.drawable.function_all,
                    PrintTicketActivity.class),

            new DemoDetails(R.string.get_printer_status, R.drawable.function_status,
                    PrinterStatusActivity.class),

//            new DemoDetails(R.string.fill_name, R.drawable.fill,
//                    null),
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_printer_helper, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        printerWorkList = view.findViewById(R.id.printerWork_list);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        Log.e("device:", +width + "--" + height);
        int size = 2;
        if (Build.MODEL.equalsIgnoreCase("D70")) {
            size = 4;
        }
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), size);
        printerWorkList.setLayoutManager(layoutManager);
        printerWorkList.setAdapter(new PrinterWordListAdapter());
//        if ("D30".equals(Build.MODEL)) {
//            demos[5] = new DemoDetails(R.string.fill_name, R.drawable.fill,
//                    null);
//        }
    }

    class PrinterWordListAdapter extends RecyclerView.Adapter<PrinterWordListAdapter.MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.printer_work_item, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.demoDetails = demos[position];
            holder.workTView.setText(demos[position].titleId);
            holder.workTView.setCompoundDrawablesWithIntrinsicBounds(null, getActivity().getDrawable(demos[position].iconResID), null, null);
        }


        @Override
        public int getItemCount() {
            if (Build.MODEL.equalsIgnoreCase("D30")) {
                return demos.length - 1;
            }
            return demos.length;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView workTView;
            DemoDetails demoDetails;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                workTView = itemView.findViewById(R.id.workTView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (demoDetails == null) {
                            return;
                        }
                        if (demoDetails.activityClass != null) {
                            startActivity(new Intent(getActivity(), demoDetails.activityClass));
                        }

                    }
                });
            }
        }


    }

    private class DemoDetails {
        @StringRes
        private final int titleId;
        @DrawableRes
        private final int iconResID;
        private final Class<? extends Activity> activityClass;

        private DemoDetails(@StringRes int titleId, @DrawableRes int descriptionId,
                            Class<? extends Activity> activityClass) {
            super();
            this.titleId = titleId;
            this.iconResID = descriptionId;
            this.activityClass = activityClass;
        }
    }


}