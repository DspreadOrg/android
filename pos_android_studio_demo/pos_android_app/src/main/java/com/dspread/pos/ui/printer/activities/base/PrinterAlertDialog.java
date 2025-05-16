package com.dspread.pos.ui.printer.activities.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class PrinterAlertDialog {
    public static void showAlertDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error"); //
        builder.setMessage("Printer initialization failed");
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); //
        dialog.setCanceledOnTouchOutside(false); //
        dialog.show();
    }
}
