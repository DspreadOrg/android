package com.dspread.pos.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    // 自定义绑定适配器：根据条件设置layout_below
    @BindingAdapter("layoutBelowIf")
    public static void setLayoutBelowIf(View view, int anchorId) {
        // 获取当前视图的布局参数并强制转换为RelativeLayout.LayoutParams
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if (anchorId != 0) {
            // 如果anchorId不为0，设置layout_below规则
            params.addRule(RelativeLayout.BELOW, anchorId);
        } else {
            // 否则清除layout_below规则
            params.removeRule(RelativeLayout.BELOW);
        }

        // 重新设置布局参数以应用更改
        view.setLayoutParams(params);
    }

    @BindingAdapter("marginTopConditional")
    public static void setMarginTopConditional(View view, Boolean isD70) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            int marginTop;
            if (isD70 != null && isD70) {
                // 14dp 转换为像素
                marginTop = (int) (16 * view.getResources().getDisplayMetrics().density);
            } else {
                marginTop = 0;
            }

            layoutParams.topMargin = marginTop;
            view.setLayoutParams(layoutParams);
        }
    }
}
