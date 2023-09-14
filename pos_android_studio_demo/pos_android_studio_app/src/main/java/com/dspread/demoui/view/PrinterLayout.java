package com.dspread.demoui.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

public class PrinterLayout extends LinearLayout {
    private int bottomSpace;
    private int lineSpace;
    private Context context;
    private LayoutParams layoutParams;
    private Typeface font;

    public PrinterLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public PrinterLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrinterLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.bottomSpace = 0;
        this.lineSpace = 0;
        this.font = Typeface.DEFAULT;
        this.context = context;
        this.setOrientation(LinearLayout.VERTICAL);
        this.layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom + this.bottomSpace);
    }

    public Typeface getFont() {
        return this.font;
    }

    public void setAssetFont(String fontName) {
        AssetManager mgr = this.context.getAssets();
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/" + fontName);
        this.font = tf;
    }

    public void setFontPath(String path) {
        AssetManager mgr = this.context.getAssets();
        Typeface tf = Typeface.createFromFile(path);
        this.font = tf;
    }

    public int getBottomSpace() {
        return this.bottomSpace;
    }

    public void setBottomSpace(int bottomSpace) {
        this.bottomSpace = bottomSpace;
        int paddingBottom = this.getPaddingBottom() + bottomSpace;
        this.setPadding(this.getPaddingLeft(), this.getPaddingTop(), this.getPaddingRight(), paddingBottom);
    }

    public int getLineSpace() {
        return this.lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        this.lineSpace = lineSpace;
    }

    public void addText(TextPrintLine printLine) {
        this.addView(this.getTextPrintLine(printLine), this.layoutParams);
    }

    public void addText(List<TextPrintLine> printLine) {
        RelativeLayout lineLinearLayout = new RelativeLayout(this.context);
        Iterator var3 = printLine.iterator();

        while (var3.hasNext()) {
            TextPrintLine mLine = (TextPrintLine) var3.next();
            lineLinearLayout.addView(this.getTextPrintLine(mLine), this.layoutParams);
        }

        this.addView(lineLinearLayout, this.layoutParams);
    }

    public void addTextView(TextView textView) {
        this.addView(textView, this.layoutParams);
    }

    public void addBitmap(BitmapPrintLine printLine) {
        if (printLine.getBitmap() != null) {
            ImageView imageView = new ImageView(this.context);
            imageView.setPadding(imageView.getPaddingLeft(), imageView.getPaddingTop(), imageView.getPaddingRight(), imageView.getPaddingBottom() + this.lineSpace);
            switch (printLine.getPosition()) {
                case 0:
                    imageView.setScaleType(ImageView.ScaleType.FIT_START);
                    break;
                case 1:
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    break;
                case 2:
                    imageView.setScaleType(ImageView.ScaleType.FIT_END);
            }

            if (this.isNeedScale(printLine.getBitmap(), 384, 100000)) {
                Bitmap scaleBitmap = this.scaleBitmap(printLine.getBitmap(), 384, 0);
                imageView.setImageBitmap(scaleBitmap);
            } else {
                imageView.setImageBitmap(printLine.getBitmap());
            }

            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.0F);
            ColorMatrixColorFilter grayColorFilter = new ColorMatrixColorFilter(cm);
            imageView.setColorFilter(grayColorFilter);
            this.addView(imageView, this.layoutParams);
        }

    }

    public void addImageView(ImageView imageView) {
        this.addView(imageView, this.layoutParams);
    }

    public void addView(View view) {
        this.addView(view, this.layoutParams);
    }

    private View getTextPrintLine(TextPrintLine printLine) {
        TextView textView = new TextView(this.context);
        textView.setPadding(textView.getPaddingLeft(), textView.getPaddingTop() + this.lineSpace, textView.getPaddingRight(), textView.getPaddingBottom() + this.lineSpace);
        switch (printLine.getPosition()) {
            case 0:
                textView.setGravity(Gravity.LEFT);
                break;
            case 1:
                textView.setGravity(Gravity.CENTER);
                break;
            case 2:
                textView.setGravity(Gravity.RIGHT);
        }

        if (printLine.isInvert()) {
            textView.setTextColor(-1); //white  0xFF000000恰好是-16777216,0xFFFFFFFF是-1.
            textView.setBackgroundColor(-16777216);
        } else {
            textView.setTextColor(-16777216); // black
            textView.setBackgroundColor(0);
        }

        textView.setTextSize(2, (float) printLine.getSize());
        textView.setLineSpacing((float) this.lineSpace, 1.0F);
        textView.getPaint().setAntiAlias(false);
        textView.setPaintFlags(256);
        if (printLine.isBold() && printLine.isItalic()) {
            textView.setTypeface(this.font, Typeface.BOLD_ITALIC);
        } else if (printLine.isBold()) {
            textView.setTypeface(this.font, Typeface.BOLD);
        } else if (printLine.isItalic()) {
            textView.setTypeface(this.font, Typeface.ITALIC);
        } else {
            textView.setTypeface(this.font, Typeface.NORMAL);
        }

        textView.setText(printLine.getContent());
        return textView;
    }

    public Bitmap viewToBitmap() {
        int measuredWidth = MeasureSpec.makeMeasureSpec(384, MeasureSpec.EXACTLY);
        int measuredHeight = MeasureSpec.makeMeasureSpec(100000, MeasureSpec.UNSPECIFIED);
        this.measure(measuredWidth, measuredHeight);
        this.layout(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
        int w = this.getWidth();
        int h = this.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(-1);
        this.invalidate();
        this.draw(c);
        return bmp;
    }

    private boolean isNeedScale(Bitmap bitmap, int srcWidth, int srcHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        return width > srcWidth || height > srcHeight;
    }

    private Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        } else if (origin.isRecycled()) {
            return null;
        } else {
            int width = origin.getWidth();
            int height = origin.getHeight();
            float scaleWidth = (float) newWidth / (float) width;
            float scaleHeight = (float) newHeight / (float) height;
            float scale = Math.max(scaleWidth, scaleHeight);
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
            if (!origin.isRecycled()) {
                origin.recycle();
            }

            return newBM;
        }
    }
}
