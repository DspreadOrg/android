package com.dspread.pos.dualScreen.original;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.dspread.pos.dualScreen.manager.PictureData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片显示 Presentation(原生实现)
 *
 * <p>支持三种模式:
 * <ul>
 *   <li>单张图片全屏显示</li>
 *   <li>多张图片轮播(可设定间隔秒数)</li>
 *   <li>多张图片按指定位置和尺寸叠加显示</li>
 * </ul>
 * </p>
 */
public class PicturePresentation extends Presentation {

    private final FrameLayout rootLayout;
    private final ImageView fullImageView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // 轮播相关
    private List<String> picPathList;
    private int intervalMillis;
    private int currentIndex = 0;
    private boolean isStopSlide = false;

    private final Runnable slideshowRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStopSlide || picPathList == null || picPathList.isEmpty()) return;
            showImage(picPathList.get(currentIndex), 0, 0, 0, 0, true);
            currentIndex = (currentIndex + 1) % picPathList.size();
            handler.postDelayed(this, intervalMillis);
        }
    };

    public PicturePresentation(Context context, Display display) {
        super(context, display);
        rootLayout = new FrameLayout(getContext());
        rootLayout.setBackgroundColor(Color.BLACK);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        fullImageView = new ImageView(getContext());
        fullImageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        fullImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        rootLayout.addView(fullImageView);
        setContentView(rootLayout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCutoutMode();
    }

    @Override
    public void hide() {
        super.hide();
        isStopSlide = true;
    }

    /** 让窗口延伸到挖孔(cutout)区域,占满整个副屏 */
    private void setupCutoutMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Window window = getWindow();
            if (window != null) {
                WindowManager.LayoutParams attrs = window.getAttributes();
                attrs.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
                window.setAttributes(attrs);
            }
        }
    }

    /**
     * 显示单张图片(全屏)
     */
    public void showSinglePic(String picPath) {
        stopSlideshow();
        showImage(picPath, 0, 0, 0, 0, true);
    }

    /**
     * 显示图片轮播
     * @param paths 图片路径列表
     * @param intervalSeconds 轮播间隔(秒)
     */
    public void showSlideshow(List<String> paths, int intervalSeconds) {
        stopSlideshow();
        if (paths == null || paths.isEmpty()) return;
        this.picPathList = new ArrayList<>(paths);
        this.intervalMillis = Math.max(1, intervalSeconds) * 1000;
        this.currentIndex = 0;
        this.isStopSlide = false;
        handler.post(slideshowRunnable);
    }

    /**
     * 显示多张图片按指定位置和尺寸叠加
     */
    public void showPicDataList(List<PictureData> picDataList) {
        stopSlideshow();
        // 清除全屏 ImageView,改为按位置叠加
        fullImageView.setVisibility(android.view.View.GONE);
        rootLayout.setBackgroundColor(Color.BLACK);
        // 清除之前按位置添加的 ImageView(保留 fullImageView)
        while (rootLayout.getChildCount() > 1) {
            rootLayout.removeViewAt(rootLayout.getChildCount() - 1);
        }
        if (picDataList == null) return;
        for (PictureData pd : picDataList) {
            ImageView iv = new ImageView(getContext());
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    pd.getWidth() > 0 ? pd.getWidth() : FrameLayout.LayoutParams.WRAP_CONTENT,
                    pd.getHeight() > 0 ? pd.getHeight() : FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = pd.getStartX();
            lp.topMargin = pd.getStartY();
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            loadImage(pd.getPath(), iv);
            rootLayout.addView(iv);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSlideshow();
    }

    private void stopSlideshow() {
        handler.removeCallbacks(slideshowRunnable);
        picPathList = null;
        isStopSlide = true;
    }

    /**
     * 加载并显示图片
     * @param path 图片文件路径
     * @param x 起始X(0=居中全屏)
     * @param y 起始Y
     * @param w 宽度(0=match_parent)
     * @param h 高度(0=match_parent)
     * @param fullscreen 是否全屏模式
     */
    private void showImage(String path, int x, int y, int w, int h, boolean fullscreen) {
        if (fullscreen) {
            fullImageView.setVisibility(android.view.View.VISIBLE);
            loadImage(path, fullImageView);
        }
    }

    /**
     * 加载图片到指定 ImageView
     */
    private void loadImage(String path, ImageView target) {
        if (path == null || path.isEmpty()) return;
        File file = new File(path);
        if (!file.exists()) return;
        try {
            // 使用 inSampleSize 控制内存,避免大图 OOM
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            int sampleSize = Math.max(1, opts.outWidth / 960);
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = sampleSize;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bmp = BitmapFactory.decodeFile(path, opts);
            if (bmp != null) {
                target.setImageBitmap(bmp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
