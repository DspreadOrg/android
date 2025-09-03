package com.dspread.pos.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.pos_android_app.R;


public class PrintDialogUtils {

    public interface DialogDismissListener {
        void onDismiss();
    }

    /**
     * 显示自定义弹窗
     */
    public static Dialog showCustomDialog(Context context, int iconResId, String message,
                                        long duration, boolean showCountdown,
                                        boolean showCloseButton, boolean cancelable,
                                        DialogDismissListener dismissListener) {
        CustomDialog dialog = new CustomDialog(context, iconResId, message, duration,
                showCountdown, showCloseButton, cancelable, dismissListener);
        dialog.show();
        return dialog;
    }

    /**
     * 显示默认的成功弹窗
     */
    public static Dialog showSuccessDialog(Context context, String message, 
                                         DialogDismissListener dismissListener) {
        return showCustomDialog(context, android.R.drawable.ic_dialog_info, message,
                3000, true, true, false, dismissListener);
    }

    public static Dialog showSuccessDialog(Context context, String message) {
        return showSuccessDialog(context, message, null);
    }

    public static Dialog showSuccessDialog(Context context) {
        return showSuccessDialog(context, "Print Successful", null);
    }

    /**
     * 自定义Dialog类
     */
    private static class CustomDialog extends Dialog {
        private final int iconResId;
        private final String message;
        private final long duration;
        private final boolean showCountdown;
        private final boolean showCloseButton;
        private final DialogDismissListener dismissListener;
        
        private CountDownTimer countDownTimer;
        private TextView tvTimer;
        private Button btnClose;

        public CustomDialog(Context context, int iconResId, String message, long duration,
                          boolean showCountdown, boolean showCloseButton, boolean cancelable,
                          DialogDismissListener dismissListener) {
            super(context);
            this.iconResId = iconResId;
            this.message = message;
            this.duration = duration;
            this.showCountdown = showCountdown;
            this.showCloseButton = showCloseButton;
            this.dismissListener = dismissListener;
            
            setCancelable(cancelable);
            setCanceledOnTouchOutside(cancelable);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_custom_alert);
            
            initViews();
            setupWindow();
            
            if (showCountdown) {
                startCountdown();
            }
        }

        private void initViews() {
            ImageView ivIcon = findViewById(R.id.ivIcon);
            TextView tvMessage = findViewById(R.id.tvMessage);
            tvTimer = findViewById(R.id.tvTimer);
            btnClose = findViewById(R.id.btnClose);

            // 设置图标
            if (iconResId != 0) {
                ivIcon.setVisibility(View.VISIBLE);
                ivIcon.setImageResource(iconResId);
            }

            // 设置消息
            tvMessage.setText(message);

            // 设置倒计时
            if (showCountdown) {
                tvTimer.setVisibility(View.VISIBLE);
                long initialSeconds = duration / 1000;
                tvTimer.setText("(" + initialSeconds + "s)");
            }

            // 设置关闭按钮
            if (showCloseButton) {
                btnClose.setVisibility(View.VISIBLE);
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
        }

        private void setupWindow() {
            if (getWindow() != null) {
                getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                // 添加动画效果
                getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            }
        }

        private void startCountdown() {
            countDownTimer = new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long secondsRemaining = (millisUntilFinished + 999) / 1000; // 向上取整
                    if (tvTimer != null) {
                        tvTimer.setText("(" + secondsRemaining + "s)");
                    }
                }

                @Override
                public void onFinish() {
                    if (isShowing()) {
                        dismiss();
                    }
                }
            }.start();
        }

        @Override
        public void show() {
            try {
                super.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dismiss() {
            try {
                // 取消倒计时
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                
                super.dismiss();
                
                // 回调监听器
                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 手动关闭弹窗（外部调用）
         */
        public void closeDialog() {
            dismiss();
        }

        /**
         * 更新倒计时显示
         */
        public void updateTimerText(String text) {
            if (tvTimer != null && tvTimer.getVisibility() == View.VISIBLE) {
                tvTimer.setText(text);
            }
        }

        /**
         * 更新消息内容
         */
        public void updateMessage(String newMessage) {
            TextView tvMessage = findViewById(R.id.tvMessage);
            if (tvMessage != null) {
                tvMessage.setText(newMessage);
            }
        }
    }
}