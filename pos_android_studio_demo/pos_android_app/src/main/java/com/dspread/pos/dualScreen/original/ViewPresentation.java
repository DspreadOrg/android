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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.dspread.pos.dualScreen.manager.PictureData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义View显示 Presentation(原生实现)
 *
 * <p>将外部传入的 View 显示到副屏,可选叠加图片(图片轮播)。</p>
 */
public class ViewPresentation extends Presentation {

    private final FrameLayout rootLayout;
    /** 轮播图片载体,固定放在自定义 View 的下层作为背景。 */
    private final ImageView picImageView;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // 轮播相关
    private List<PictureData> picDataList;
    private int currentIndex = 0;
    private long intervalMillis = 3000L;
    private boolean isStopSlide = false;

    private final Runnable slideshowRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStopSlide || picDataList == null || picDataList.isEmpty()) return;
            showCurrentPic();
            currentIndex = (currentIndex + 1) % picDataList.size();
            handler.postDelayed(this, intervalMillis);
        }
    };

    public ViewPresentation(Context context, Display display) {
        super(context, display);
        rootLayout = new FrameLayout(getContext());
        rootLayout.setBackgroundColor(Color.BLACK);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 图片层默认铺满,轮播时按 PictureData 的坐标/尺寸摆放
        picImageView = new ImageView(getContext());
        picImageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        picImageView.setScaleType(ImageView.ScaleType.FIT_XY);

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
     * 显示自定义View
     */
    public void showView(View view) {
        stopSlideshow();
        rootLayout.removeAllViews();
        if (view == null) return;
        // 如果 View 已有 parent,先移除
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        rootLayout.addView(view);
    }

    /**
     * 显示自定义View + 图片轮播
     *
     * <p>图片层在底层轮播,自定义 View 叠加在最上层。</p>
     *
     * @param intervalSeconds 轮播间隔(秒),小于 1 时按 1 秒处理
     */
    public void showViewWithPics(View view, List<PictureData> picDataList, int intervalSeconds) {
        stopSlideshow();
        rootLayout.removeAllViews();
        picImageView.setVisibility(View.GONE);
        rootLayout.addView(picImageView);
        if (view != null) {
            if (view.getParent() instanceof ViewGroup) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            rootLayout.addView(view);
        }
        this.intervalMillis = Math.max(1, intervalSeconds) * 1000L;
        this.picDataList = (picDataList == null) ? null : new ArrayList<>(picDataList);
        this.currentIndex = 0;
        this.isStopSlide = false;
        if (this.picDataList != null && !this.picDataList.isEmpty()) {
            handler.post(slideshowRunnable);
        }
    }

    /** 显示当前索引对应的图片 */
    private void showCurrentPic() {
        if (picDataList == null || picDataList.isEmpty()) return;
        PictureData pd = picDataList.get(currentIndex);
        if (pd == null) return;

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                pd.getWidth() > 0 ? pd.getWidth() : FrameLayout.LayoutParams.WRAP_CONTENT,
                pd.getHeight() > 0 ? pd.getHeight() : FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = pd.getStartX();
        lp.topMargin = pd.getStartY();
        picImageView.setLayoutParams(lp);
        picImageView.setVisibility(View.VISIBLE);
        loadImage(pd.getPath(), picImageView);
    }

    private void stopSlideshow() {
        handler.removeCallbacks(slideshowRunnable);
        picDataList = null;
        isStopSlide = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSlideshow();
    }

    private void loadImage(String path, ImageView target) {
        if (path == null || path.isEmpty()) return;
        File file = new File(path);
        if (!file.exists()) return;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
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
