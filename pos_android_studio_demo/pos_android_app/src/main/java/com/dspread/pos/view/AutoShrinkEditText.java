package com.dspread.pos.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

public class AutoShrinkEditText extends AppCompatEditText {
    private float minTextSize = 12;
    // 最大字体大小（sp），默认为当前文本大小
    private float maxTextSize;

    public AutoShrinkEditText(Context context) {
        super(context);
        init();
    }

    public AutoShrinkEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoShrinkEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置为单行显示
        setSingleLine(true);
        // 获取当前文本大小作为最大字体大小
        maxTextSize = getTextSize();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        adjustTextSize(text.toString());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 当控件大小改变时重新调整文本大小
        adjustTextSize(getText().toString());
    }

    private void adjustTextSize(String text) {
        if (text.isEmpty()) return;

        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (availableWidth <= 0) return;

        Paint paint = getPaint();
        float currentTextSize = maxTextSize;

        // 从最大字体大小开始尝试，直到找到合适的大小
        while (currentTextSize > minTextSize) {
            paint.setTextSize(currentTextSize);
            float textWidth = paint.measureText(text);

            if (textWidth <= availableWidth) {
                break;
            }

            currentTextSize -= 1;
        }

        // 确保不会小于最小字体大小
        if (currentTextSize < minTextSize) {
            currentTextSize = minTextSize;
        }

        setTextSize(currentTextSize / getResources().getDisplayMetrics().scaledDensity);
    }

    // 设置最小字体大小（sp）
    public void setMinTextSize(float minTextSize) {
        this.minTextSize = minTextSize;
        adjustTextSize(getText().toString());
    }

    // 设置最大字体大小（sp）
    public void setMaxTextSize(float maxTextSize) {
        this.maxTextSize = maxTextSize * getResources().getDisplayMetrics().scaledDensity;
        adjustTextSize(getText().toString());
    }
}
