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
     * Show custom dialog
     */
    public static Dialog showCustomDialog(Context context, int iconResId, String message, String failMeaasge,
                                          long duration, boolean showCountdown,
                                          boolean showCloseButton, boolean cancelable,
                                          DialogDismissListener dismissListener) {
        CustomDialog dialog = new CustomDialog(context, iconResId, message, failMeaasge, duration,
                showCountdown, showCloseButton, cancelable, dismissListener);
        dialog.show();
        return dialog;
    }

    /**
     * Show default success dialog
     */
    public static Dialog showSuccessDialog(Context context, String message, String failMessage,
                                           DialogDismissListener dismissListener) {
        return showCustomDialog(context, android.R.drawable.ic_dialog_info, message, failMessage,
                3000, true, true, false, dismissListener);
    }

    public static Dialog showSuccessDialog(Context context, String message, String failMessage) {
        return showSuccessDialog(context, message, failMessage, null);
    }

    public static Dialog showSuccessDialog(Context context) {
        return showSuccessDialog(context, "Print Successful", null);
    }

    /**
     * Custom Dialog class
     */
    private static class CustomDialog extends Dialog {
        private final int iconResId;
        private final String message;
        private final String failMessage;
        private final long duration;
        private final boolean showCountdown;
        private final boolean showCloseButton;
        private final DialogDismissListener dismissListener;

        private CountDownTimer countDownTimer;
        private TextView tvTimer;
        private ImageView btnClose;

        public CustomDialog(Context context, int iconResId, String message, String failMessage, long duration,
                            boolean showCountdown, boolean showCloseButton, boolean cancelable,
                            DialogDismissListener dismissListener) {
            super(context);
            this.iconResId = iconResId;
            this.message = message;
            this.failMessage = failMessage;
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
            TextView tvFailMessage = findViewById(R.id.tvFailMessage);
            tvTimer = findViewById(R.id.tvTimer);
            btnClose = findViewById(R.id.btnClose);

            // Set icon
            if (iconResId != 0) {
                ivIcon.setVisibility(View.VISIBLE);
                ivIcon.setImageResource(iconResId);
            }

            // Set message
            tvMessage.setText(message);

            // Set countdown
            if (showCountdown) {
                tvTimer.setVisibility(View.VISIBLE);
                long initialSeconds = duration / 1000;
                tvTimer.setText("(" + initialSeconds + "s)");
            }

            // Set close button
            if (showCloseButton) {
                btnClose.setVisibility(View.VISIBLE);
                tvFailMessage.setVisibility(View.VISIBLE);
                tvFailMessage.setText(failMessage);
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
                // Add animation effect
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
                // Cancel countdown
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }

                super.dismiss();

                // Callback listener
                if (dismissListener != null) {
                    dismissListener.onDismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Manually close dialog (external call)
         */
        public void closeDialog() {
            dismiss();
        }

        /**
         * Update countdown display
         */
        public void updateTimerText(String text) {
            if (tvTimer != null && tvTimer.getVisibility() == View.VISIBLE) {
                tvTimer.setText(text);
            }
        }

        /**
         * Update message content
         */
        public void updateMessage(String newMessage) {
            TextView tvMessage = findViewById(R.id.tvMessage);
            if (tvMessage != null) {
                tvMessage.setText(newMessage);
            }
        }
    }
}