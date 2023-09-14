package com.dspread.demoui.activities.mp5801;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.dspread.demoui.R;

public abstract class BaseDialog extends Dialog implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    protected Context context;

    public BaseDialog(Context context) {
        super(context, R.style.DialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initContentView());
        context = getContext();
        init();
        initListener();
        showView();
    }

    protected abstract int initContentView();

    protected abstract void init();

    private void initListener() {
        setOnCancelListener(this);
        setOnDismissListener(this);
    }

    protected abstract void showView();

    @Override
    public void onCancel(DialogInterface dialog) {
        onCancelListener();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        onDismissListener();
    }

    protected abstract void onCancelListener();

    protected abstract void onDismissListener();
}
