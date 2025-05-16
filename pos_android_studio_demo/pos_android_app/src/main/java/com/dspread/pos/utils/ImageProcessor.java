package com.dspread.print.util;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ImageProcessor {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final int TRANSPARENT_THRESHOLD = 128;
    private static final int CONTRAST_MIN = 96;
    private static final int CONTRAST_MAX = 160;

    /**
     * 同步方法：将 Bitmap 转换为黑白图（保留透明背景）。
     *
     * @param bmp 输入 Bitmap
     * @return 转换后的黑白 Bitmap
     */
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int[] ditherWeights = {7, 3, 5, 1};
        int[] offsetX = {1, -1, 0, 1};
        int[] offsetY = {0, 1, 1, 1};

        int[] pixelArray = new int[width * height];
        bmp.getPixels(pixelArray, 0, width, 0, 0, width, height);

        int totalLuminance = 0;

        for (int i = 0; i < pixelArray.length; i++) {
            int pixel = pixelArray[i];
            int alpha = (pixel >> 24) & 0xFF;

            if (alpha < TRANSPARENT_THRESHOLD) {
                pixelArray[i] = 255; // 透明背景设为白色
            } else {
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;
                int gray = (int) (0.3 * red + 0.59 * green + 0.11 * blue);
                pixelArray[i] = gray;
                totalLuminance += gray;
            }
        }

        int averageLuminance = totalLuminance / pixelArray.length;
        int threshold = Math.max(CONTRAST_MIN, Math.min(CONTRAST_MAX, averageLuminance));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int pixel = bmp.getPixel(x, y);
                int alpha = (pixel >> 24) & 0xFF;

                if (alpha < TRANSPARENT_THRESHOLD) {
                    bwBitmap.setPixel(x, y, 0xFFFFFFFF);
                    continue;
                }

                int oldPixel = pixelArray[index];
                int newPixel = oldPixel < threshold ? 0xFF000000 : 0xFFFFFFFF;
                int error = oldPixel - (newPixel == 0xFF000000 ? 0 : 255);

                bwBitmap.setPixel(x, y, newPixel);

                for (int i = 0; i < ditherWeights.length; i++) {
                    int nx = x + offsetX[i];
                    int ny = y + offsetY[i];
                    int ni = ny * width + nx;

                    if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                        int neighborPixel = bmp.getPixel(nx, ny);
                        int neighborAlpha = (neighborPixel >> 24) & 0xFF;

                        if (neighborAlpha >= TRANSPARENT_THRESHOLD) {
                            pixelArray[ni] += error * ditherWeights[i] / 16;
                            pixelArray[ni] = Math.max(0, Math.min(255, pixelArray[ni]));
                        }
                    }
                }
            }
        }

        return bwBitmap;
    }

    /**
     * 异步方法：将 Bitmap 转换为黑白图（保留透明背景）。
     *
     * @param bmp      输入 Bitmap
     * @param listener 结果回调
     */
    public static void convertToBlackWhiteAsync(Bitmap bmp, OnBitmapProcessedListener listener) {
        executorService.execute(() -> {
            Bitmap result = convertToBlackWhite(bmp);
            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onBitmapProcessed(result);
                }
            });
        });
    }

    /**
     * 回调接口：Bitmap 处理完成后返回结果。
     */
    public interface OnBitmapProcessedListener {
        void onBitmapProcessed(Bitmap bitmap);
    }
}
