package com.dspread.pos.dualScreen.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.pos_android_app.R;

import java.util.Locale;

/**
 * 副屏金额显示自定义 View
 *
 * <p>白底布局:顶部 "Enter amount" 提示文字、中部大号粗体金额、底部分割线,
 * 三者整体垂直居中。金额变化时调用 {@link #setAmount(String)} / {@link #setAmount(long)} 更新文本,
 * 再通过 {@link com.dspread.pos.dualScreen.manager.ViceScreenManager#show(View)} 刷新到副屏。</p>
 */
public class AmountDisplayView extends FrameLayout {

    private final TextView tvAmount;

    public AmountDisplayView(Context context) {
        super(context);
        setBackgroundColor(Color.WHITE);
        // 作为副屏 Presentation 根容器的子 View,铺满整个副屏
        setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 内容容器:垂直排列,整体垂直居中,子元素水平居中
        LinearLayout contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        LayoutParams contentLp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        addView(contentLayout, contentLp);

        // 顶部提示文字
        TextView tvHint = new TextView(context);
        tvHint.setText(R.string.vice_screen_enter_amount);
        tvHint.setTextColor(Color.BLACK);
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvHint.setGravity(Gravity.CENTER);
        contentLayout.addView(tvHint, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // 中部金额文字:大号粗体
        tvAmount = new TextView(context);
        tvAmount.setText("¥0.00");
        tvAmount.setTextColor(Color.BLACK);
        tvAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tvAmount.setTypeface(Typeface.DEFAULT_BOLD);
        tvAmount.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams amountLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        amountLp.topMargin = dp(8);
        contentLayout.addView(tvAmount, amountLp);

        // 底部分割线:浅灰细线,贯穿整个宽度
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dividerLp.topMargin = dp(24);
        contentLayout.addView(divider, dividerLp);
    }

    /**
     * 更新副屏显示的金额(原样显示)
     */
    public void setAmount(String amountText) {
        if (amountText != null) {
            tvAmount.setText(amountText);
        }
    }

    /**
     * 更新副屏显示的金额(单位:分),自动格式化为 ¥x.xx
     */
    public void setAmount(long amountInCents) {
        tvAmount.setText(String.format(Locale.getDefault(), "%s%d.%02d",
                getResources().getString(R.string.amount_identification),
                amountInCents / 100, amountInCents % 100));
    }

    private int dp(float value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }
}
