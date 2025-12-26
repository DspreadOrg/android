package com.dspread.pos.ui.printer.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.dspread.pos.ui.main.MainActivity;
import com.dspread.pos.ui.printer.activities.base.PrinterBaseActivity;
import com.dspread.pos.utils.PrintDialogUtils;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPrintTicketBinding;
import com.dspread.pos_android_app.databinding.ActivityPrinterBaseBinding;
import com.dspread.print.device.PrinterDevice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PrintTicketActivity extends PrinterBaseActivity<ActivityPrinterBaseBinding, PrintTicketViewModel> {
    private ActivityPrintTicketBinding contentBinding;
    private Bitmap mBitmap;
    private String amount = "";
    private String maskedPAN = "";
    private String terminalTime = "";
    private String transactionTime;
    private boolean isSmallDevices = false;

    @Override
    public void initData() {
        super.initData();
        amount = getIntent().getStringExtra("terAmount");
        maskedPAN = getIntent().getStringExtra("maskedPAN");
        terminalTime = getIntent().getStringExtra("terminalTime");
        transactionTime = getIntent().getStringExtra("transactionTime");

        contentBinding = ActivityPrintTicketBinding.inflate(getLayoutInflater());
        contentBinding.setViewModel(viewModel);
        binding.contentContainer.addView(contentBinding.getRoot());
        viewModel.title.set(getString(R.string.print_ticket));
        binding.btnPrint.setVisibility(View.GONE);
        setupViews();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthPx = displayMetrics.widthPixels;
        int heightPx = displayMetrics.heightPixels;

        if (widthPx <= 320 && heightPx <= 240) {
            isSmallDevices = true;
            viewModel.isSmallScreen.set(true);
            viewModel.isNormalScreen.set(false);
            viewModel.isSmallScreenButton.set(false);
            binding.tvTitle.setText("Please Wait...");
        } else {
            isSmallDevices = false;
            viewModel.isSmallScreen.set(false);
            viewModel.isNormalScreen.set(true);
        }
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
        // Clean up old bitmap resources
        cleanupBitmap();
        Map<String, String> map = buildParameterMap();

        //Generate receipt bitmap and process the result
        // viewModel.generateReceiptBitmap(map);
        
        generateReceiptInBackground(map);

        // observeReceiptBitmap();
        initViewOnlick(map);
    }

    /**
     *Clean up old bitmap resources
     */
    private void cleanupBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    /**
     *Build parameter mapping
     */
    private Map<String, String> buildParameterMap() {
        Map<String, String> map = new HashMap<>();

        map.put("terAmount", getSafeString(amount));
        map.put("maskedPAN", getSafeString(maskedPAN));
        map.put("terminalTime", buildTerminalTimeString());

        return map;
    }

    private String getSafeString(String value) {
        return !TextUtils.isEmpty(value) ? value : "";
    }

    /**
     *Build terminal time string
     */
    private String buildTerminalTimeString() {
        if (TextUtils.isEmpty(terminalTime)) {
            return getSafeString(transactionTime);
        }

        String formattedTime = formatDateTime(terminalTime);

        if (!TextUtils.isEmpty(transactionTime)) {
            return formattedTime + " " + transactionTime;
        }

        return formattedTime;
    }

    /**
     *Format date and time
     */
    private String formatDateTime(String dateTimeStr) {
        if (TextUtils.isEmpty(dateTimeStr)) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTimeStr;
        }
    }

    /**
     * 观察收据位图变化
     */
    private void observeReceiptBitmap() {
        viewModel.getReceiptBitmap().observe(this, bitmap -> {
            if (bitmap == null) return;

            this.mBitmap = bitmap;

            if (isSmallDevices) {
                viewModel.printTicket(bitmap);
            } else {
                contentBinding.receiptImage.setImageBitmap(bitmap);
            }
        });
    }

    private void initViewOnlick(Map<String, String> map) {
        binding.btnPrintTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mBitmap != null && !mBitmap.isRecycled()) {
                    binding.btnPrintTicket.setEnabled(false);
                    viewModel.printTicket(mBitmap);
                    startPrintAnimation();
                } else {
                    Toast.makeText(PrintTicketActivity.this, "Receipt not ready", Toast.LENGTH_SHORT).show();
                    //Regenerate bitmap
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


        binding.btnSmallPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBitmap != null && !mBitmap.isRecycled()) {
                    viewModel.isSmallScreenButton.set(false);
                    binding.tvTitle.setText("Please Wait...");
                    binding.btnSmallPrint.setEnabled(false);
                    viewModel.printTicket(mBitmap);
                } else {
                    Toast.makeText(PrintTicketActivity.this, "Receipt not ready", Toast.LENGTH_SHORT).show();
                    //Regenerate bitmap
                    viewModel.generateReceiptBitmap(map);
                }
            }
        });

        binding.btnSmallCancel.setOnClickListener(new View.OnClickListener() {
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

                // Calculate the total distance needed to move (from the current position to completely exit the screen)
                float currentY = contentBinding.receiptImage.getY();
                float moveDistance = -(currentY + imageHeight);

                //Set movement speed (pixels/milliseconds)
                float moveSpeed = 0.2f;

                //Calculate duration based on distance and speed
                long duration = (long) (Math.abs(moveDistance) / moveSpeed);

                //Limit animation time range
                duration = Math.max(1000, Math.min(duration, 3000));

                ObjectAnimator translateAnim = ObjectAnimator.ofFloat(contentBinding.receiptImage, "translationY", 0f, moveDistance);

                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(contentBinding.receiptImage, "alpha", 1f, 0f);

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
        binding.btnPrintTicket.setEnabled(true);
        binding.btnSmallPrint.setEnabled(true);
        if (isSuccess) {
            viewModel.onPrintComplete(isSuccess, status);
            dialog(PrintTicketActivity.this, R.mipmap.ic_print_success, "Print Successful", status, 3000L, true, false);
        } else {
            if (isSmallDevices) {
                binding.tvTitle.setText("Print Fail");
                binding.ivIcon.setImageResource(R.mipmap.ic_printer_small_fail);
                if (resultType == PrinterDevice.ResultType.NOPAPER) {
                    binding.tvFail.setText("No Paper");
                } else if (resultType == PrinterDevice.ResultType.LOWERBATTERY) {
                    binding.tvFail.setText("Lower Battery");
                } else if (resultType == PrinterDevice.ResultType.OVERHEATING) {
                    binding.tvFail.setText("Over Heating");
                } else {
                    binding.tvFail.setText("Unknown error");
                }
                viewModel.isSmallScreenButton.set(true);
            } else {
                dialog(PrintTicketActivity.this, R.mipmap.ic_print_fail, "Print Fail", status, 3000L, false, true);
            }
        }
    }

    private void dialog(Context mContext, int icon, String message, String failMessage, Long duration, boolean isShowCountdown, boolean isShowCloseButton) {
        PrintDialogUtils.showCustomDialog(mContext, icon, message, failMessage, duration, isShowCountdown, isShowCloseButton, false, new PrintDialogUtils.DialogDismissListener() {
            @Override
            public void onDismiss() {
                if (isShowCloseButton) {
                    regenerateReceipt();
                } else {
                    Intent intent = new Intent(PrintTicketActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    PrintTicketActivity.this.finish();
                }
            }
        });
    }
    private void regenerateReceipt() {
        runOnUiThread(() -> {

            contentBinding.receiptImage.postDelayed(() -> {
                PrintTicketActivity.this.finish();
            }, 100);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    private static final ExecutorService BITMAP_EXECUTOR = Executors.newFixedThreadPool(2);

    private void generateReceiptInBackground(Map<String, String> map) {
        contentBinding.receiptImage.setVisibility(View.INVISIBLE);
        viewModel.setShowLoading(true);
        CompletableFuture
                .runAsync(() -> viewModel.generateReceiptBitmap(map), BITMAP_EXECUTOR)
                .thenRunAsync(() -> {
                    viewModel.setShowLoading(false);
                    contentBinding.receiptImage.setVisibility(View.VISIBLE);
                    observeReceiptBitmap();
                }, this::runOnUiThread);
    }
}