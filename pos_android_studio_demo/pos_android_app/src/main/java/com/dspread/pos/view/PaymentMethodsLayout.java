package com.dspread.pos.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
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
    private int cellWidth = 0;
    private int cellHeight = 0;
    private int lastWidth = 0;
    private int lastHeight = 0;
    // 减少间距以适应小屏幕
    private final int spacing = dpToPx(8);
    private final int innerPadding = dpToPx(6);
    private GridLayout gridLayout;
    // 屏幕尺寸相关常量
    private final int SMALL_SCREEN_WIDTH = 320;
    private final int SMALL_SCREEN_HEIGHT = 240;
    private boolean isSmallScreen = false;

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

        // 检测是否为小屏幕
        isSmallScreen = parentWidth <= SMALL_SCREEN_WIDTH && parentHeight <= SMALL_SCREEN_HEIGHT;

        // 计算单元格大小
        calculateCellSize(parentWidth, parentHeight);

        // 计算网格所需宽度和高度
        int gridWidth, gridHeight;
        if (isSmallScreen) {
            // 小屏幕：2行2列，宽度大于高度
            gridWidth = cellWidth * 2 + spacing;
            gridHeight = cellHeight * 2 + spacing;
        } else {
            // 正常屏幕：保持正方形

            gridWidth = cellWidth * 2 + spacing;
            gridHeight = cellWidth * 2 + spacing;

            if (parentWidth <= MIN_SCREEN_WIDTH && parentHeight <= MIN_SCREEN_HEIGHT) {
                // 小屏幕再减少一些高度
                gridHeight = (int) (gridHeight * 0.95);
            }

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
        if (isLayoutInitialized || getWidth() <= 0 || cellWidth <= 0) return;

        addAllPaymentOptions();
        isLayoutInitialized = true;
    }

    // 计算单元格大小，针对小屏幕特殊处理
    private void calculateCellSize(int parentWidth, int parentHeight) {
        if (parentWidth <= 0 || parentHeight <= 0) return;

        // 留出边距
        int maxAvailableWidth = parentWidth - dpToPx(16);
        int maxAvailableHeight = parentHeight - dpToPx(20);

        if (isSmallScreen) {
            // 小屏幕特殊处理：宽度大于高度 (2:1 比例)
            cellWidth = (maxAvailableWidth - spacing) / 2;
            cellHeight = Math.min(cellWidth / 2, (maxAvailableHeight - spacing) / 2);
            // 设置最小限制
            int minCellWidth = dpToPx(140);
            int minCellHeight = dpToPx(70);
            int maxCellWidth = dpToPx(180);
            int maxCellHeight = dpToPx(90);

            cellWidth = Math.max(minCellWidth, Math.min(cellWidth, maxCellWidth)); //105
            cellHeight = Math.max(minCellHeight, Math.min(cellHeight, maxCellHeight)); //53

        } else {
            // 正常屏幕：保持正方形
            int widthBasedCellSize = (maxAvailableWidth - spacing) / 2;
            int heightBasedCellSize = (maxAvailableHeight - spacing) / 2;

            cellWidth = Math.min(widthBasedCellSize, heightBasedCellSize);
            cellHeight = cellWidth;

            // 设置正常屏幕的尺寸限制
            int minCellSize = dpToPx(120);
            int maxCellSize = dpToPx(180);

            if (parentWidth <= MIN_SCREEN_WIDTH && parentHeight <= MIN_SCREEN_HEIGHT) {
                // 小屏幕使用更小的尺寸范围
                minCellSize = dpToPx(80);
                maxCellSize = dpToPx(140);
            }

            cellWidth = Math.max(minCellSize, Math.min(cellWidth, maxCellSize));
            cellHeight = cellWidth;

        }


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
        if (isSmallScreen) {
            // 小屏幕:垂直布局
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(Gravity.CENTER);
        } else {
            // 正常屏幕：垂直布局
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(Gravity.CENTER);
        }

        container.setBackground(createBackgroundSelector());
        container.setClickable(true);
        container.setFocusable(true);
        container.setPadding(innerPadding, innerPadding, innerPadding, innerPadding);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = cellWidth;
        params.height = cellHeight;

        int horizontalMargin = spacing / 2;
        int verticalMargin = spacing / 2;

        params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
        params.rowSpec = GridLayout.spec(position / 2);
        params.columnSpec = GridLayout.spec(position % 2);
        container.setLayoutParams(params);

        // 图标
        ImageView imageView = new ImageView(getContext());
        int iconSize;
        LayoutParams ivParams;

        if (isSmallScreen) {
            // 小屏幕：图标较小
            iconSize = (int) (Math.min(cellWidth, cellHeight) * 0.3f);
            ivParams = new LayoutParams(iconSize, iconSize);
            //ivParams.rightMargin = dpToPx(4); // 图标和文字之间的间距
            ivParams.gravity = Gravity.CENTER;
        } else {
            // 正常屏幕：图标较大，放在上方
            float iconRatio = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 0.35f : 0.4f;
            iconSize = (int) (cellWidth * iconRatio);
            ivParams = new LayoutParams(iconSize, iconSize);
            ivParams.gravity = Gravity.CENTER;
        }

        imageView.setLayoutParams(ivParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(option.iconRes);

        // 文本
        TextView textView = new TextView(getContext());
        LayoutParams tvParams;

        if (isSmallScreen) {
            // 小屏幕：
            tvParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tvParams.gravity = Gravity.CENTER;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        } else {
            // 正常屏幕：文字在图标下方
            tvParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tvParams.gravity = Gravity.CENTER;
            tvParams.topMargin = dpToPx(4);
            float textSize = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 14 : 16;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }

        textView.setLayoutParams(tvParams);
        textView.setText(option.text);
        textView.setTextColor(Color.parseColor("#ff030303"));
        textView.setSingleLine(true);
        textView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        // 设置文字最大宽度
        if (isSmallScreen) {
            textView.setMaxWidth((int) (cellWidth * 0.5f));
        } else {
            textView.setMaxWidth((int) (cellWidth * 0.8f));
        }

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
        // 重新检测屏幕尺寸
        isSmallScreen = getWidth() <= SMALL_SCREEN_WIDTH && getHeight() <= SMALL_SCREEN_HEIGHT;

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            GridLayout.LayoutParams params = (GridLayout.LayoutParams) child.getLayoutParams();
            if (params != null) {
                params.width = cellWidth;
                params.height = cellHeight;

                int horizontalMargin = spacing / 2;
                int verticalMargin = spacing / 2;
                params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);

                child.setLayoutParams(params);

                if (child instanceof LinearLayout) {
                    LinearLayout container = (LinearLayout) child;

                    // 更新布局方向
                    if (isSmallScreen) {
                        container.setOrientation(LinearLayout.VERTICAL);
                        container.setGravity(Gravity.CENTER);
                    } else {
                        container.setOrientation(LinearLayout.VERTICAL);
                        container.setGravity(Gravity.CENTER);
                    }

                    // 更新图标大小和位置
                    if (container.getChildAt(0) instanceof ImageView) {
                        ImageView imageView = (ImageView) container.getChildAt(0);
                        LayoutParams ivParams = (LayoutParams) imageView.getLayoutParams();

                        int iconSize;
                        if (isSmallScreen) {
                            iconSize = (int) (Math.min(cellWidth, cellHeight) * 0.3f);
                            ivParams.width = iconSize;
                            ivParams.height = iconSize;
                            ivParams.rightMargin = dpToPx(4);
                            ivParams.gravity = Gravity.CENTER;
                        } else {
                            float iconRatio = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 0.35f : 0.4f;
                            //float iconRatio = 0.4f;
                            iconSize = (int) (cellWidth * iconRatio);
                            ivParams.width = iconSize;
                            ivParams.height = iconSize;
                            ivParams.rightMargin = 0;
                            ivParams.gravity = Gravity.CENTER;
                        }
                        imageView.setLayoutParams(ivParams);
                    }

                    // 更新文字大小和位置
                    if (container.getChildAt(1) instanceof TextView) {
                        TextView textView = (TextView) container.getChildAt(1);
                        LayoutParams tvParams = (LayoutParams) textView.getLayoutParams();

                        if (isSmallScreen) {
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                            tvParams.gravity = Gravity.CENTER;
                            textView.setMaxWidth((int) (cellWidth * 0.5f));
                        } else {
                            float textSize = getResources().getDisplayMetrics().widthPixels <= MIN_SCREEN_WIDTH ? 14 : 16;
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
                            tvParams.gravity = Gravity.CENTER;
                            tvParams.topMargin = dpToPx(4);
                            textView.setMaxWidth((int) (cellWidth * 0.8f));
                        }
                        textView.setLayoutParams(tvParams);
                    }
                }
            }
        }
    }

    private StateListDrawable createBackgroundSelector() {
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setShape(GradientDrawable.RECTANGLE);
        normalDrawable.setCornerRadius(dpToPx(isSmallScreen ? 16 : 26));
        normalDrawable.setStroke(dpToPx(1), Color.parseColor("#BCBCBC"));
        normalDrawable.setColor(Color.WHITE);

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setShape(GradientDrawable.RECTANGLE);
        pressedDrawable.setCornerRadius(dpToPx(isSmallScreen ? 16 : 26));
        pressedDrawable.setStroke(dpToPx(1), Color.parseColor("#ffe47579"));
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