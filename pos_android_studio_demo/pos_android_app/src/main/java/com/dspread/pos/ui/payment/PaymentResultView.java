package com.dspread.pos.ui.payment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.pos_android_app.R;

/**
 * 副屏交易结果自定义 View
 *
 * <p>白底布局:左右排列,左侧为交易成功/失败图标,右侧为交易金额。
 * 通过 {@link #setResult(boolean)} 切换图标、{@link #setAmount(String)} 更新金额,
 * 再通过 {@link com.dspread.pos.dualScreen.manager.ViceScreenManager#show(View)} 刷新到副屏。</p>
 */
public class PaymentResultView extends FrameLayout {

    private final ImageView ivResult;
    private final TextView tvAmount;
    private final TextView tvErrorMsg;

    public PaymentResultView(Context context) {
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
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        // 上半部分:水平排列图标与金额
        LinearLayout rowLayout = new LinearLayout(context);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER);
        contentLayout.addView(rowLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // 左侧结果图标
        ivResult = new ImageView(context);
        ivResult.setImageResource(R.mipmap.icon_success);
        rowLayout.addView(ivResult, new LinearLayout.LayoutParams(dp(40), dp(40)));

        // 右侧交易金额
        tvAmount = new TextView(context);
        tvAmount.setTextColor(Color.BLACK);
        tvAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        tvAmount.setTypeface(Typeface.DEFAULT_BOLD);
        tvAmount.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams amountLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        amountLp.leftMargin = dp(16);
        rowLayout.addView(tvAmount, amountLp);

        // 下方错误信息
        tvErrorMsg = new TextView(context);
        tvErrorMsg.setTextColor(Color.BLACK);
        tvErrorMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tvErrorMsg.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams errorLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        errorLp.topMargin = dp(8);
        contentLayout.addView(tvErrorMsg, errorLp);
    }

    /**
     * 设置交易结果图标
     *
     * @param success true 显示成功图标,false 显示失败图标
     */
    public void setResult(boolean success) {
        ivResult.setImageResource(success ? R.mipmap.icon_success : R.mipmap.payment_failed);
    }

    /**
     * 更新副屏显示的交易金额
     */
    public void setAmount(String amountText) {
        if (amountText != null) {
            tvAmount.setText(amountText);
        }
    }

    /**
     * 更新副屏显示的错误信息
     */
    public void setErrorMsg(String errorMsg) {
        if (!TextUtils.isEmpty(errorMsg)) {
            tvErrorMsg.setText(errorMsg);
        }
    }

    private int dp(float value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }
}
