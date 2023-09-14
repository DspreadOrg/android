package com.dspread.demoui.activities.printer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

public class BitmapCache {
    private final Map<Integer, Bitmap> standardBitmapMap = new HashMap<>();

    private static BitmapCache instance;

    public static BitmapCache getInstance() {
        if (instance == null) {
            instance = new BitmapCache();
        }
        return instance;
    }

    private BitmapCache() {
    }

    public synchronized Bitmap get(int contentDensity, int heightFactor) {
        int key = (contentDensity + 1) * 10 + heightFactor;
        Bitmap bitmap = standardBitmapMap.get(key);
        if (bitmap == null) {
            bitmap = genBitmap(contentDensity, heightFactor);
            standardBitmapMap.put(key, bitmap);
        }
        return bitmap;
    }

    private Bitmap genBitmap(int contentDensity, int heightFactor) {
        int width = 384;
        int height = width * heightFactor;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);

        if (contentDensity > 0) {
            int totalColumn = width / 8 * contentDensity;
            int offset = width / totalColumn;
            Canvas canvas = new Canvas(bitmap);
            int startX = 0, startY = 0, endX = 0, endY = 0;
            for (int line = 0; line < height / offset; line++) {
                for (int column = 0; column < totalColumn; column++) {
                    startX = column * offset;
                    startY = line * offset;
                    endX = startX + offset;
                    endY = startY + offset;
                    canvas.drawLine(startX, startY, endX, endY, paint);
                }
            }
        }
        return bitmap;
    }
}
