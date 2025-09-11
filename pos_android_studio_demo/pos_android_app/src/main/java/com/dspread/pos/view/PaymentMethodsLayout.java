package com.dspread.pos.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.pos.ui.payment.PaymentMethodViewModel;
import com.dspread.pos_android_app.R;

import java.util.Arrays;
import java.util.List;


public class PaymentMethodsLayout extends LinearLayout {
    private PaymentMethodViewModel viewModel;
    private List<PaymentOption> paymentOptions;
    private boolean isLayoutInitialized = false;
    private int cellSize = 0;
    private int lastWidth = 0;
    private int lastHeight = 0;
    // 减少间距以适应小屏幕
    private final int spacing = dpToPx(10);
    private final int innerPadding = dpToPx(8);
    private GridLayout gridLayout;
    // 屏幕尺寸相关常量
    private final int MIN_SCREEN_WIDTH = 320;
    private final int MIN_SCREEN_HEIGHT = 480;

    public PaymentMethodsLayout(Context context) {
        super(context);
        init();
    }

    public PaymentMethodsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PaymentMethodsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(2);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_MARGINS);
        gridLayout.setColumnOrderPreserved(false);

        LayoutParams gridParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        gridParams.gravity = Gravity.CENTER;
        gridLayout.setLayoutParams(gridParams);

        addView(gridLayout);

        paymentOptions = Arrays.asList(
                new PaymentOption(R.mipmap.ic_salemethod_card, "Card", 0),
                new PaymentOption(R.mipmap.ic_salemethod_scan, "Scan Code", 1),
                new PaymentOption(R.mipmap.ic_salemethod_generate, "Generate", 2),
                new PaymentOption(R.mipmap.ic_salemethod_cash, "Cash", 3)
        );
    }

    public void setViewModel(PaymentMethodViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isLayoutInitialized) {
            getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                if (getWidth() > 0 && getHeight() > 0) {
                    initializeLayout();
                    getViewTreeObserver().removeOnGlobalLayoutListener(this::initializeLayout);
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算单元格大小，同时考虑宽度和高度限制
        calculateCellSize(parentWidth, parentHeight);

        // 计算网格所需宽度和高度
        int gridWidth = cellSize * 2 + spacing;
        int gridHeight = cellSize * 2 + spacing;

        // 特别针对小屏幕设备的额外调整
        if (parentWidth <= MIN_SCREEN_WIDTH && parentHeight <= MIN_SCREEN_HEIGHT) {
            // 小屏幕再减少一些高度
            gridHeight = (int)(gridHeight * 0.95);
        }

        int gridWidthSpec = MeasureSpec.makeMeasureSpec(gridWidth, MeasureSpec.EXACTLY);
        int gridHeightSpec = MeasureSpec.makeMeasureSpec(gridHeight, MeasureSpec.EXACTLY);
        gridLayout.measure(gridWidthSpec, gridHeightSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(parentWidth, Math.min(gridHeight, parentHeight));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 当宽高变化明显时才更新布局
        if (isLayoutInitialized &&
                (Math.abs(w - lastWidth) > dpToPx(10) || Math.abs(h - lastHeight) > dpToPx(10))) {
            lastWidth = w;
            lastHeight = h;
            calculateCellSize(w, h);
            updateChildSizes();
        }
    }

    private void initializeLayout() {
        if (isLayoutInitialized || getWidth() <= 0 || cellSize <= 0) return;

        addAllPaymentOptions();
        isLayoutInitialized = true;
    }

    // 同时考虑宽度和高度来计算单元格大小
    private void calculateCellSize(int parentWidth, int parentHeight) {
        if (parentWidth <= 0 || parentHeight <= 0) return;

        // 留出边距
        int maxAvailableWidth = parentWidth - dpToPx(20);
        int maxAvailableHeight = parentHeight - dpToPx(40); // 底部多留一些空间

        // 分别根据宽度和高度计算可能的单元格大小
        int widthBasedCellSize = (maxAvailableWidth - spacing) / 2;
        int heightBasedCellSize = (maxAvailableHeight - spacing) / 2;

        // 取较小的值，确保不会超出屏幕
        cellSize = Math.min(widthBasedCellSize, heightBasedCellSize);

        // 根据屏幕大小调整尺寸限制
        int minCellSize, maxCellSize;
        if (parentWidth <= MIN_SCREEN_WIDTH && parentHeight <= MIN_SCREEN_HEIGHT) {
            // 小屏幕使用更小的尺寸范围
            minCellSize = dpToPx(80);
            maxCellSize = dpToPx(140);
        } else {
            minCellSize = dpToPx(120);
            maxCellSize = dpToPx(180);
        }

        cellSize = Math.max(minCellSize, Math.min(cellSize, maxCellSize));
    }

    private void addAllPaymentOptions() {
        gridLayout.removeAllViews();

        for (int i = 0; i < paymentOptions.size(); i++) {
            PaymentOption option = paymentOptions.get(i);
            addPaymentOption(option, i);
        }
    }

    private void addPaymentOption(PaymentOption option, int position) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setBackground(createBackgroundSelector());
        container.setClickable(true);
        container.setFocusable(true);
        container.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = cellSize;
        params.height = cellSize;

        int horizontalMargin = spacing / 2;
        int verticalMargin = spacing / 2;

        params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);

        params.rowSpec = GridLayout.spec(position / 2);
        params.columnSpec = GridLayout.spec(position % 2);

        container.setLayoutParams(params);

        // 图标
        ImageView imageView = new ImageView(getContext());
        // 根据屏幕大小调整图标比例
        float iconRatio = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 0.35f : 0.4f;
        int iconSize = (int) (cellSize * iconRatio);
        LayoutParams ivParams = new LayoutParams(iconSize, iconSize);
        ivParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(ivParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(option.iconRes);

        // 文本
        TextView textView = new TextView(getContext());
        LayoutParams tvParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.gravity = Gravity.CENTER;
        tvParams.topMargin = dpToPx(5);

        // 小屏幕使用更小的字体
        float textSize = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 14 : 16;
        textView.setLayoutParams(tvParams);
        textView.setText(option.text);
        textView.setTextColor(Color.parseColor("#ff030303"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setSingleLine(true);
        textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textView.setMaxWidth((int) (cellSize * 0.8));

        container.addView(imageView);
        container.addView(textView);

        container.setOnClickListener(v -> {
            if (viewModel != null) {
                viewModel.onPaymentMethodSelected(option.id);
            }
        });

        gridLayout.addView(container);
    }

    private void updateChildSizes() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) child.getLayoutParams();
            if (params != null) {
                params.width = cellSize;
                params.height = cellSize;

                int horizontalMargin = spacing / 2;
                int verticalMargin = spacing / 2;
                params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);

                child.setLayoutParams(params);

                if (child instanceof LinearLayout) {
                    LinearLayout container = (LinearLayout) child;
                    // 更新图标大小
                    if (container.getChildAt(0) instanceof ImageView) {
                        ImageView imageView = (ImageView) container.getChildAt(0);
                        LayoutParams ivParams = (LayoutParams) imageView.getLayoutParams();
                        float iconRatio = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 0.35f : 0.4f;
                        int iconSize = (int) (cellSize * iconRatio);
                        ivParams.width = iconSize;
                        ivParams.height = iconSize;
                        imageView.setLayoutParams(ivParams);
                    }
                    // 更新文字大小
                    if (container.getChildAt(1) instanceof TextView) {
                        TextView textView = (TextView) container.getChildAt(1);
                        float textSize = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 14 : 16;
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                    }
                }
            }
        }
    }

    private StateListDrawable createBackgroundSelector() {
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setShape(GradientDrawable.RECTANGLE);
        normalDrawable.setCornerRadius(dpToPx(26));
        normalDrawable.setStroke(dpToPx(2), Color.parseColor("#ffe47579"));
        normalDrawable.setColor(Color.WHITE);

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setShape(GradientDrawable.RECTANGLE);
        pressedDrawable.setCornerRadius(dpToPx(26));
        pressedDrawable.setStroke(dpToPx(2), Color.parseColor("#ffe47579"));
        pressedDrawable.setColor(Color.parseColor("#ffffe9e9"));

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        states.addState(new int[]{}, normalDrawable);

        return states;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class PaymentOption {
        int iconRes;
        String text;
        int id;

        PaymentOption(int iconRes, String text, int id) {
            this.iconRes = iconRes;
            this.text = text;
            this.id = id;
        }
    }
}
