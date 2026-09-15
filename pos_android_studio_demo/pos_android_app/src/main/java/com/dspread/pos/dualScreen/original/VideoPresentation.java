package com.dspread.pos.dualScreen.original;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 视频显示 Presentation(原生实现)。
 *
 * <p>方案1：用 TextureView + MediaPlayer（走 GLES 合成）替代 VideoView/SurfaceView（走 overlay
 * 合成），规避 MTK hwcomposer 长时间合成副屏视频图层时的 vendor 崩溃。</p>
 *
 * <p>方案2：定时重启播放（周期 stop+release 再重新 prepare），释放图层 buffer，
 * 避免长时间连续循环合成累积出错。</p>
 */
public class VideoPresentation extends Presentation {

    private TextureView textureView;
    private MediaPlayer mediaPlayer;
    private String videoPath;
    private Surface surface;

    /*
    private final Handler handler = new Handler(Looper.getMainLooper());
    // 定时重启间隔(毫秒)，避免长时间连续合成触发 HWC 崩溃
    private static final long RESTART_INTERVAL_MS = 5 * 60 * 1000L;

    private final Runnable restartRunnable = this::restartPlayback;*/

    public VideoPresentation(Context context, Display display) {
        super(context, display);
        FrameLayout root = new FrameLayout(getContext());
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        textureView = new TextureView(getContext());
        textureView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(textureView);
        setContentView(root);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // window 在 show() 之后才真正创建，此时设置 cutout 模式才可靠
        setupCutoutMode();
    }

    @Override
    public void hide() {
        super.hide();
        if (surface != null && mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * 让窗口延伸到挖孔(cutout)区域,占满整个副屏
     */
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
     * 播放指定路径的视频
     */
    public void playVideo(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) return;
        if (videoPath.equals(this.videoPath) && surface != null && mediaPlayer != null) {
            mediaPlayer.start();
            return;
        }
        this.videoPath = videoPath;
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        if (textureView.isAvailable()) {
            surface = new Surface(textureView.getSurfaceTexture());
            startPlayback();
        }
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
                    surface = new Surface(st);
                    startPlayback();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
                    releasePlayer();
                    releaseSurface();
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture st) {
                }
            };

    private void startPlayback() {
        if (surface == null || videoPath == null) return;
        try {
            releasePlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                //scheduleRestart();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> true);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    // 方案2：周期停止并重新播放，释放 buffer，避免长时间连续合成
    private void restartPlayback() {
        startPlayback();
    }

    private void scheduleRestart() {
        handler.removeCallbacks(restartRunnable);
        handler.postDelayed(restartRunnable, RESTART_INTERVAL_MS);
    }*/

    private void releasePlayer() {
        //handler.removeCallbacks(restartRunnable);
        final MediaPlayer mp = mediaPlayer;
        mediaPlayer = null;
        if (mp != null) {
            new Thread(() -> {
                try {
                    mp.setOnPreparedListener(null);
                    mp.setOnErrorListener(null);
                    if (mp.isPlaying()) {
                        mp.stop();
                    }
                    mp.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void releaseSurface() {
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
        releaseSurface();
    }
}
