package com.dspread.pos.ui.printer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.ui.main.MainActivity;
import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos.utils.PrintDialogUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPrintTicketBinding;
import com.dspread.pos_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.pos_android_app.generated.callback.OnClickListener;
import com.dspread.print.device.PrinterDevice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PrintTicketActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, PrintTicketViewModel> {
    private ActivityPrintTicketBinding contentBinding;
    private Bitmap mBitmap;
    private String amount="";
    private String maskedPAN="";
    private String terminalTime="";
    @Override
    public void initData() {
        super.initData();
        amount = getIntent().getStringExtra("terAmount");
        maskedPAN = getIntent().getStringExtra("maskedPAN");
        terminalTime = getIntent().getStringExtra("terminalTime");
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
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        Map<String,String> map = new HashMap<>();
        if(amount!=null&&!"".equals(amount)){
        }else{
            amount ="";
        }
        map.put("terAmount", amount);
        if(maskedPAN!=null&&!"".equals(maskedPAN)){
        }else{
            maskedPAN="";
        }
        map.put("maskedPAN",maskedPAN);
        if(terminalTime!=null && !"".equals(terminalTime)){
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            try {
                // 解析输入的日期字符串
                Date date = inputFormat.parse(terminalTime);
                // 格式化日期
                terminalTime = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            terminalTime="";
        }
        map.put("terminalTime",terminalTime);
        viewModel.generateReceiptBitmap(map);
        viewModel.getReceiptBitmap().observe(this, bitmap -> {
            if (bitmap != null) {
                this.mBitmap = bitmap;
                contentBinding.receiptImage.setImageBitmap(bitmap);
            }
        });

        binding.btnPrintTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBitmap != null && !mBitmap.isRecycled()) {
                    viewModel.printTicket(mBitmap);
                    startPrintAnimation();
                } else {
                    Toast.makeText(PrintTicketActivity.this, "Receipt not ready", Toast.LENGTH_SHORT).show();
                    // 重新生成bitmap
                    viewModel.generateReceiptBitmap(map);
                }
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

                if (mBitmap == null || mBitmap.isRecycled()) {
                    return;
                }
                int imageHeight = contentBinding.receiptImage.getHeight();
                int screenHeight = getResources().getDisplayMetrics().heightPixels;

                if (imageHeight == 0) {
                    imageHeight = screenHeight / 3;
                }

                // 计算需要移动的总距离（从当前位置移动到完全离开屏幕）
                float currentY = contentBinding.receiptImage.getY();
                float moveDistance = -(currentY + imageHeight);

                // 设置移动速度（像素/毫秒）
                float moveSpeed = 0.5f; // 0.8像素/毫秒

                // 根据距离和速度计算持续时间
                long duration = (long) (Math.abs(moveDistance) / moveSpeed);

                // 限制动画时间范围
                duration = Math.max(1000, Math.min(duration, 3000));

                ObjectAnimator translateAnim = ObjectAnimator.ofFloat(contentBinding.receiptImage, "translationY", 0f, moveDistance);

                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(contentBinding.receiptImage, "alpha", 1f, 0f);

                // 透明度动画应该比移动动画稍快
                alphaAnim.setDuration((long) (duration * 0.8));
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(translateAnim, alphaAnim);
                animatorSet.setDuration(duration);
                animatorSet.setInterpolator(new AccelerateInterpolator());

                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetImagePosition();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        resetImagePosition();
                    }
                });

                animatorSet.start();
            }
        });
    }


    private void resetImagePosition() {
        contentBinding.receiptImage.setTranslationY(0);
        contentBinding.receiptImage.setAlpha(1);
    }

    @Override
    protected void onReturnPrintResult(boolean isSuccess, String status, PrinterDevice.ResultType resultType) {
        viewModel.onPrintComplete(isSuccess, status);
        if (isSuccess) {
            dialog(PrintTicketActivity.this, "Print Successful", 3000L, true, false);
        } else {
            dialog(PrintTicketActivity.this, "Print Fail", 3000L, false, true);

        }
    }

    private void dialog(Context mContext, String message, Long duration, boolean isShowCountdown, boolean isShowCloseButton) {
        PrintDialogUtils.showCustomDialog(mContext, android.R.drawable.ic_dialog_info, message, duration, isShowCountdown, isShowCloseButton, false, new PrintDialogUtils.DialogDismissListener() {
            @Override
            public void onDismiss() {
                if (isShowCloseButton) {
                    regenerateReceipt();
                } else {
                    Intent intent = new Intent(PrintTicketActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    private void regenerateReceipt() {
        // 确保在主线程执行
        runOnUiThread(() -> {
            // 显示加载状态
            // 延迟一下再重新生成，避免立即重试可能的问题
            contentBinding.receiptImage.postDelayed(() -> {
                finish();
            }, 100);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理bitmap资源
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}