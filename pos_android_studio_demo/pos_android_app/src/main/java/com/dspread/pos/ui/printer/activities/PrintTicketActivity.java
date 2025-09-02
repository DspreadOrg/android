package com.dspread.pos.ui.printer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPrintTicketBinding;
import com.dspread.pos_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.pos_android_app.generated.callback.OnClickListener;
import com.dspread.print.device.PrinterDevice;


public class PrintTicketActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, PrintTicketViewModel> {
    private ActivityPrintTicketBinding contentBinding;
    private Bitmap mBitmap;

    @Override
    public void initData() {
        super.initData();
        contentBinding = ActivityPrintTicketBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set(getString(R.string.print_ticket));
        binding.btnPrint.setVisibility(View.GONE);
        setupViews();
    }

    @Override
    public int initContentView(Bundle savedInstanceState) {
        return R.layout.activity_printer_base;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    private void setupViews() {
        viewModel.generateReceiptBitmap();
        viewModel.getReceiptBitmap().observe(this, bitmap -> {
            if (bitmap != null) {
                this.mBitmap = bitmap;
                contentBinding.receiptImage.setImageBitmap(bitmap);
            }
        });

        binding.btnPrintTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.printTicket(mBitmap);
                startPrintAnimation();
            }
        });

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void startPrintAnimation() {
        contentBinding.receiptImage.post(new Runnable() {
            @Override
            public void run() {
                int imageHeight = contentBinding.receiptImage.getHeight();
                int screenHeight = getResources().getDisplayMetrics().heightPixels;

                if (imageHeight == 0) {
                    imageHeight = screenHeight / 3;
                }

                // 计算需要移动的总距离（从当前位置移动到完全离开屏幕）
                float currentY = contentBinding.receiptImage.getY();
                float moveDistance = -(currentY + imageHeight);

                // 设置移动速度（像素/毫秒）
                float moveSpeed = 0.8f; // 0.8像素/毫秒

                // 根据距离和速度计算持续时间
                long duration = (long) (Math.abs(moveDistance) / moveSpeed);

                // 限制动画时间范围
                duration = Math.max(1000, Math.min(duration, 3000));

                ObjectAnimator translateAnim = ObjectAnimator.ofFloat(
                        contentBinding.receiptImage,
                        "translationY",
                        0f,
                        moveDistance
                );

                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(
                        contentBinding.receiptImage,
                        "alpha",
                        1f,
                        0f
                );

                // 透明度动画应该比移动动画稍快
                alphaAnim.setDuration((long) (duration * 0.8));
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(translateAnim, alphaAnim);
                animatorSet.setDuration(duration);
                animatorSet.setInterpolator(new AccelerateInterpolator());

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        contentBinding.receiptImage.setTranslationY(0);
                        contentBinding.receiptImage.setAlpha(1);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        contentBinding.receiptImage.setTranslationY(0);
                        contentBinding.receiptImage.setAlpha(1);
                    }
                });

                animatorSet.start();
            }
        });
    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        viewModel.onPrintComplete(isSuccess, status);
    }
}