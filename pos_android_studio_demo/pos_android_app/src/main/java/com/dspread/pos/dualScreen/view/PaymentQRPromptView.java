package com.dspread.pos.dualScreen.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.pos_android_app.R;

/**
 * 副屏扫码支付提示自定义 View
 *
 * <p>两列布局:左侧红色圆角卡片显示"Trans Amount"+金额,
 * 右侧白色描边卡片显示红色圆形扫码图标+提示文字,
 * 通过 {@link com.dspread.pos.dualScreen.manager.ViceScreenManager#show(android.view.View)} 显示到副屏。</p>
 */
public class PaymentQRPromptView extends LinearLayout {

    private final TextView tvAmount;

    public PaymentQRPromptView(Context context) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.WHITE);
        setPadding(dp(4), dp(4), dp(4), dp(4));
        // 作为副屏 Presentation 根容器的子 View,铺满整个副屏
        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 左列:红色圆角卡片,显示 Trans Amount + 金额
        LinearLayout amountCard = new LinearLayout(context);
        amountCard.setOrientation(LinearLayout.VERTICAL);
        amountCard.setGravity(Gravity.CENTER);
        amountCard.setPadding(dp(4), dp(4), dp(4), dp(4));
        amountCard.setBackground(roundedDrawable(Color.parseColor("#FFD32F2F"), dp(8), 0, Color.TRANSPARENT));
        LayoutParams amountCardLp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        amountCardLp.setMargins(0, 0, dp(4), 0);
        addView(amountCard, amountCardLp);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Trans Amount");
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tvTitle.setGravity(Gravity.CENTER);
        amountCard.addView(tvTitle);

        tvAmount = new TextView(context);
        tvAmount.setTextColor(Color.WHITE);
        tvAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvAmount.setTypeface(Typeface.DEFAULT_BOLD);
        tvAmount.setGravity(Gravity.CENTER);
        LayoutParams amountTextLp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        amountTextLp.topMargin = dp(4);
        amountCard.addView(tvAmount, amountTextLp);

        // 右列:白色圆角卡片(红色描边),显示扫码图标 + 提示文字
        LinearLayout scanCard = new LinearLayout(context);
        scanCard.setOrientation(LinearLayout.VERTICAL);
        scanCard.setGravity(Gravity.CENTER);
        scanCard.setPadding(dp(4), dp(4), dp(4), dp(4));
        scanCard.setBackground(roundedDrawable(Color.WHITE, dp(8), dp(1), Color.parseColor("#FFD32F2F")));
        LayoutParams scanCardLp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        scanCardLp.setMargins(dp(4), 0, 0, 0);
        addView(scanCard, scanCardLp);

        // 红色圆形背景 + 白色扫码图标
        FrameLayout iconWrap = new FrameLayout(context);
        GradientDrawable circleBg = new GradientDrawable();
        circleBg.setShape(GradientDrawable.OVAL);
        circleBg.setColor(Color.parseColor("#FFD32F2F"));
        iconWrap.setBackground(circleBg);
        scanCard.addView(iconWrap, new LayoutParams(dp(32), dp(32)));

        ImageView ivScan = new ImageView(context);
        ivScan.setImageResource(R.mipmap.ic_salemethod_scan);
        ivScan.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        ivScan.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iconWrap.addView(ivScan, new FrameLayout.LayoutParams(
                dp(18), dp(18), Gravity.CENTER));

        // 提示文字
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText("Please place your payment code above the camera.");
        tvPrompt.setTextColor(Color.BLACK);
        tvPrompt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tvPrompt.setGravity(Gravity.CENTER);
        LayoutParams promptLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        promptLp.topMargin = dp(4);
        scanCard.addView(tvPrompt, promptLp);
    }

    /**
     * 更新副屏显示的金额
     */
    public void setAmount(String amountText) {
        if (amountText != null) {
            tvAmount.setText(amountText);
        }
    }

    private GradientDrawable roundedDrawable(int fillColor, int radius, int strokeWidth, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radius);
        drawable.setColor(fillColor);
        if (strokeWidth > 0) {
            drawable.setStroke(strokeWidth, strokeColor);
        }
        return drawable;
    }

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }
}
