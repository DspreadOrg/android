package com.dspread.demoui.view;

import android.graphics.Bitmap;

public class BitmapPrintLine extends PrintLine {
    private Bitmap bitmap;

    private void BitmapPrintLine(Bitmap bitmap, int position) {
        this.type = 1;
        this.bitmap = bitmap;
        this.position = position;
    }

    public BitmapPrintLine() {
        this.BitmapPrintLine((Bitmap) null, 1);
    }

    public BitmapPrintLine(Bitmap bitmap, int position) {
        this.BitmapPrintLine(bitmap, position);
    }

    public BitmapPrintLine(Bitmap bitmap) {
        this.BitmapPrintLine(bitmap, 1);
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
