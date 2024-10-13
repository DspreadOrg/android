package com.dspread.demoui.widget.pinpad;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.dspread.demoui.R;


public class PinPadDialog {
    private AlertDialog mDialog;
    private Window window;
    private Context mContext;
    private int mThemeResId;
    private View mDialogLayout;


    public PinPadDialog(Context context) {

        this.mContext = context;
        this.mThemeResId = R.style.dialog_pay_theme;
        this.mDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.view_paypass_dialog, null);
        mDialog = new AlertDialog.Builder(mContext, mThemeResId).create();
        mDialog.setCancelable(true);
        mDialog.show();

        mDialog.getWindow().setDimAmount(0.4f);
        window = mDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setContentView(mDialogLayout);
        mDialog.setCanceledOnTouchOutside(false);
        window.setWindowAnimations(R.style.dialogOpenAnimation);
        window.setGravity(Gravity.BOTTOM);

    }


    public PinPadView getPayViewPass() {
        return mDialogLayout.findViewById(R.id.pay_View);

    }


    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
            window = null;
        }
    }

}
