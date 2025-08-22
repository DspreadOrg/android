package com.dspread.pos.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class DialogUtils {

    public static Dialog showLowBatteryDialog(Context context, int layoutResId, int buttonResId, boolean cancelable) {
        return showLowBatteryDialog(context, layoutResId, buttonResId, cancelable, null);
    }


    public static Dialog showLowBatteryDialog(Context context, int layoutResId,
                                              int buttonResId, boolean cancelable,
                                              View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);
        dialog.setCancelable(cancelable);

        setupDialogWindow(dialog, context);

        Button okButton = dialog.findViewById(buttonResId);
        if (okButton != null) {
            if (listener != null) {
                okButton.setOnClickListener(listener);
            } else {
                okButton.setOnClickListener(v -> dialog.dismiss());
            }
        }

        dialog.show();
        return dialog;
    }


    private static void setupDialogWindow(Dialog dialog, Context context) {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());


            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (context instanceof Activity) {
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            } else {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(displayMetrics);
            }


            int dialogWidth = (int) (displayMetrics.widthPixels * 0.8);
            layoutParams.width = dialogWidth;


            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;


            window.setBackgroundDrawableResource(android.R.color.transparent);


            window.setAttributes(layoutParams);
        }
    }

    public static Dialog showCustomWidthLowBatteryDialog(Context context, int layoutResId,
                                                         int buttonResId, float widthRatio) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutResId);
        dialog.setCancelable(false);

        setupCustomDialogWindow(dialog, context, widthRatio);

        Button okButton = dialog.findViewById(buttonResId);
        if (okButton != null) {
            okButton.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
        return dialog;
    }



    private static void setupCustomDialogWindow(Dialog dialog, Context context, float widthRatio) {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());


            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (context instanceof Activity) {
                ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            } else {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(displayMetrics);
            }


            int dialogWidth = (int) (displayMetrics.widthPixels * Math.min(Math.max(widthRatio, 0.1f), 1.0f));
            layoutParams.width = dialogWidth;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setAttributes(layoutParams);
        }
    }
}