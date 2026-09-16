package com.dspread.pos.ui.payment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 副屏"执行中"提示自定义 View
 *
 * <p>白底布局:居中显示循环转动的加载动画,下方居中显示处理提示文本。
 * 文本变化时调用 {@link #setMessage(String)} 更新,
 * 再通过 {@link com.dspread.pos.dualScreen.manager.ViceScreenManager#show(View)} 刷新到副屏。</p>
 */
public class ProcessingDisplayView extends FrameLayout {

    private final TextView tvAmount;
    private final TextView tvMessage;

    public ProcessingDisplayView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);
        // 作为副屏 Presentation 根容器的子 View,铺满整个副屏
        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 内容容器:垂直排列,整体居中
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER);
        addView(contentLayout, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        // 顶部加载动画:无限循环的圆形进度动画
     /*   ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(dp(24), dp(24));
        pbLp.gravity = Gravity.CENTER_HORIZONTAL;
        contentLayout.addView(progressBar, pbLp);*/

        // 金额文本:位于提示文本上方,大号加粗显示
        tvAmount = new TextView(context);
        tvAmount.setTextColor(Color.BLACK);
        tvAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        tvAmount.setTypeface(Typeface.DEFAULT_BOLD);
        tvAmount.setGravity(Gravity.CENTER);
        tvAmount.setVisibility(View.GONE);
        LinearLayout.LayoutParams amountLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        amountLp.topMargin = dp(12);
        contentLayout.addView(tvAmount, amountLp);

        // 底部处理提示文本
        tvMessage = new TextView(context);
        tvMessage.setTextColor(Color.BLACK);
        tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvMessage.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        msgLp.topMargin = dp(16);
        contentLayout.addView(tvMessage, msgLp);
    }

    /**
     * 更新副屏显示的处理提示文本
     */
    public void setMessage(String msg) {
        if (msg != null) {
            tvMessage.setText(msg);
        }
    }

    /**
     * 更新副屏显示的金额文本,金额为空时隐藏
     */
    public void setAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            tvAmount.setVisibility(View.GONE);
        } else {
            tvAmount.setText(amount);
            tvAmount.setVisibility(View.VISIBLE);
        }
    }

    private int dp(float value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }
}
