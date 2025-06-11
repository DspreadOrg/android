package com.dspread.pos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.print.zxing.BarcodeFormat;
import com.dspread.print.zxing.EncodeHintType;
import com.dspread.print.zxing.MultiFormatWriter;
import com.dspread.print.zxing.WriterException;
import com.dspread.print.zxing.common.BitMatrix;
import com.dspread.print.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class QRCodeUtil {
    private static final int PAPER_WIDTH = 384;
    private static StaticLayout sl;
    private static Layout.Alignment alignNormal = Layout.Alignment.ALIGN_NORMAL;


    protected static Bitmap createCodeBitmap(String contents, Context context) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;

        TextView tv = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText(contents);
        tv.setTextSize(scale * 12);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setDrawingCacheEnabled(true);
        tv.setTextColor(Color.BLACK);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

        tv.setBackgroundColor(Color.WHITE);

        tv.buildDrawingCache();
        Bitmap bitmapCode = tv.getDrawingCache();
        return bitmapCode;
    }


    /**
     * Convert a bitmap image into a byte stream that can be printed by the printer
     *
     * @param bmp
     * @return
     */
    public static byte[] draw2PxPoint(Bitmap bmp) {
        //Used to store the converted bitmap data. Why do we need to add 1000 more? This is to deal with situations where the image height cannot be reached
        //Divide by 24 hours. For example, the bitmap resolution is 240 * 250 and occupies 7500 bytes,
        //But in reality, to store 11 rows of data, each row requires 720 bytes of space, which is 24 * 240/8. In addition to some instruction storage overhead,
        //So it is safe to apply for an additional 1000 bytes of space, otherwise the runtime will throw an array access out of bounds exception.
        int size = bmp.getWidth() * bmp.getHeight() / 8 + 1000;
        byte[] data = new byte[size];
        int k = 0;
        //Instruction to set line spacing to 0
        data[k++] = 0x1B;
        data[k++] = 0x33;
        data[k++] = 0x00;
        // Print line by line
        for (int j = 0; j < bmp.getHeight() / 24f; j++) {
            //Instructions for printing images
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33;
            data[k++] = (byte) (bmp.getWidth() % 256); //nL
            data[k++] = (byte) (bmp.getWidth() / 256); //nH
            //Print column by column for each row
            for (int i = 0; i < bmp.getWidth(); i++) {
                //24 pixels per column, divided into 3 bytes for storage
                for (int m = 0; m < 3; m++) {
                    //Each byte represents 8 pixels, 0 represents white, 1 represents black
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bmp);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
            data[k++] = 10;//newline
        }
        return data;
    }

    /**
     * Grayscale image is black and white, with black being 1 and white being 0
     *
     * @param x   abscissa
     * @param y   y-coordinate
     * @param bit BITMAP
     * @return
     */
    public static byte px2Byte(int x, int y, Bitmap bit) {
        if (x < bit.getWidth() && y < bit.getHeight()) {
            byte b;
            int pixel = bit.getPixel(x, y);
            int red = (pixel & 0x00ff0000) >> 16; // Obtain the top two bit
            int green = (pixel & 0x0000ff00) >> 8; // Get the two middle bit
            int blue = pixel & 0x000000ff; // Get the lower two bit
            int gray = RGB2Gray(red, green, blue);
            if (gray < 128) {
                b = 1;
            } else {
                b = 0;
            }
            return b;
        }
        return 0;
    }

    /**
     * Conversion of image grayscale
     */
    private static int RGB2Gray(int r, int g, int b) {
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
        return gray;
    }

    public static Bitmap getBarCodeBM(String content, int w, int h) {
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(content,
                    BarcodeFormat.CODE_128, w, h);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? 0xff000000 : 0xFFFFFFFF;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // Generate a bitmap through a pixel array,
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap getQrcodeBM(String content, int size) {

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        // Support Chinese configuration
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size
                    , hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = bitMatrix.get(x, y) ? 0xff000000 : 0xFFFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

}
