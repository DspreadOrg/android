package com.dspread.demoui.activities.mp5801;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.dspread.demoui.DialogFactory;
import com.dspread.demoui.R;
import com.dspread.demoui.utils.AnimationUtil;

import java.util.Objects;

public class LoadingDialog extends BaseDialog{

    ImageView loadingSrc;

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected int initContentView() {
        return R.layout.dialog_loading;
    }

    @Override
    protected void init() {
        loadingSrc = (ImageView) findViewById(R.id.img_loading);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getWindow()).getAttributes().gravity = Gravity.CENTER;
        }
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    @Override
    protected void showView() {
        Animation rotateAnimation = AnimationUtil.getRotateAnimation(0, 360, 1000, true);
        loadingSrc.startAnimation(rotateAnimation);
    }

    @Override
    protected void onCancelListener() {
        loadingSrc.clearAnimation();
        DialogFactory.dismissLoadingDialog();
    }

    @Override
    protected void onDismissListener() {
        loadingSrc.clearAnimation();
        DialogFactory.dismissLoadingDialog();
    }
}
