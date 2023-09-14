package com.dspread.demoui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.demoui.R;

public class MidTextPrintLine extends LinearLayout {
    private TextView leftTextView;
    private TextView midTextView;
    private TextView rightTextView;

    public MidTextPrintLine(Context context) {
        this(context, (AttributeSet)null);
    }

    public MidTextPrintLine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MidTextPrintLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.custome_multi_tv_layout, this);
        this.leftTextView = (TextView)this.findViewById(R.id.left_tv);
        this.midTextView = (TextView)this.findViewById(R.id.mid_tv);
        this.rightTextView = (TextView)this.findViewById(R.id.right_tv);
    }

    public TextView getLeftTextView() {
        return this.leftTextView;
    }

    public TextView getMidTextView() {
        return this.midTextView;
    }

    public TextView getRightTextView() {
        return this.rightTextView;
    }
}
