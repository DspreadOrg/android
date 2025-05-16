package com.dspread.pos.ui.printer.activities.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.dspread.pos_new_android_app.R;

import java.util.ArrayList;
import java.util.List;

public class PrintDialog {
    public interface PrintClickListener {
        void onCancel();

        void onConfirm(String str);
    }


    public static AlertDialog setDialog;
    private static RecyclerView rvlist;
    private static TextView dialogTitle;
    private static PrinterAdapter mAdapter;

    public static void setDialog(Activity context, String title, String[] data, PrintClickListener listener) {

        setDialog = new AlertDialog.Builder(context).create();
        if (!context.isFinishing()) {
            setDialog.show();
        }

        Window window = setDialog.getWindow();
        View view = View.inflate(context, R.layout.printer_setting_dialog_view, null);
        WindowManager.LayoutParams p = setDialog.getWindow().getAttributes();
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.WRAP_CONTENT;
        setDialog.getWindow().setAttributes(p);
        rvlist = view.findViewById(R.id.rv_list);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog.dismiss();
            }
        });
        dialogTitle = view.findViewById(R.id.tv_dialogTitle);
        dialogTitle.setText(title);
        rvlist.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));
        mAdapter = new PrinterAdapter(getArrayList(data));
        rvlist.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new PrinterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String content) {
                listener.onConfirm(content);

                setDialog.dismiss();
            }
        });
        window.setContentView(view);
        setDialog.setCanceledOnTouchOutside(true);
        setDialog.setCancelable(true);
    }


    private static List<String> getArrayList(String[] data) {
        List<String> dataList = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            dataList.add(data[i]);
        }
        return dataList;
    }


    public static Dialog printInputDialog;

    public static void printInputDialog(Activity mContext, String title, PrintClickListener listener) {
        View view = View.inflate(mContext, R.layout.print_dialog_layout, null);
        TextView tvTitle = view.findViewById(R.id.tv_print_title);
        tvTitle.setText(title);
        EditText etContent = view.findViewById(R.id.et_input_content);
        view.findViewById(R.id.tv_print_confirm).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String content = etContent.getText().toString().trim();
                        listener.onConfirm(content);
                        printInputDialog.dismiss();

                    }
                });
        view.findViewById(R.id.tv_print_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                printInputDialog.dismiss();
            }
        });

        printInputDialog = new Dialog(mContext);
        printInputDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        printInputDialog.setCanceledOnTouchOutside(false);
        if (!mContext.isFinishing()) {
            printInputDialog.show();
        }

        WindowManager.LayoutParams p = printInputDialog.getWindow().getAttributes();
        printInputDialog.getWindow().setAttributes(p);
        printInputDialog.setContentView(view);
    }


    /**
     * seekbar dialog
     *
     * @param context
     * @param title
     * @param min
     * @param max
     * @param set
     */

    public static void showSeekBarDialog(Context context, String title, final int min, final int max, final TextView set, PrintClickListener listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.widget_seekbar, null);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        TextView tv_title = view.findViewById(R.id.sb_title);
        TextView tv_start = view.findViewById(R.id.sb_start);
        TextView tv_end = view.findViewById(R.id.sb_end);
        TextView tv_result = view.findViewById(R.id.sb_result);
        TextView tv_ok = view.findViewById(R.id.tv_confirm);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        SeekBar sb = view.findViewById(R.id.sb_seekbar);
        tv_title.setText(title);
        tv_start.setText(min + "");
        tv_end.setText(max + "");
        tv_result.setText(set.getText());
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set.setText(tv_result.getText());
                String textSizeStr = tv_result.getText().toString().trim();
                listener.onConfirm(textSizeStr);
                dialog.cancel();
            }
        });
        sb.setMax(max - min);
        sb.setProgress(Integer.parseInt(set.getText().toString()) - min);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rs = min + progress;
                tv_result.setText(rs + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        dialog.show();

    }


}