package com.dspread.pos.dualScreen.original;

import android.app.Presentation;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;


import com.dspread.pos.dualScreen.manager.Constant;
import com.dspread.pos.dualScreen.manager.IResultCallback;
import com.dspread.pos.dualScreen.manager.IViceScreen;
import com.dspread.pos.dualScreen.manager.PictureData;
import com.dspread.pos_android_app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViceScreenOriginal implements IViceScreen {
    /**
     * 副屏连接状态变化监听器
     */
    public interface OnDisplayChangedListener {
        void onDisplayChanged(boolean connected, Display display);
    }

    private final Context context;
    private final DisplayManager displayManager;
    private PicturePresentation picturePresentation;
    private VideoPresentation videoPresentation;
    private ViewPresentation viewPresentation;
    private int intervalSeconds;

    public ViceScreenOriginal(Context context) {
        this.context = context.getApplicationContext();
        this.displayManager = (DisplayManager) this.context.getSystemService(Context.DISPLAY_SERVICE);
        this.intervalSeconds = 3;
        createPresentation();
    }

    /**
     * 检测副屏是否已连接
     */
    public boolean hasSecondaryDisplay() {
        return getSecondaryDisplay() != null;
    }

    /**
     * 获取副屏 Display 对象
     */
    private Display getSecondaryDisplay() {
        if (displayManager == null) return null;
        Display[] displays = displayManager.getDisplays();
        if (displays == null) return null;
        // 跳过主屏(displayId=0),取第一个非主屏
        for (Display d : displays) {
            if (d.getDisplayId() != Display.DEFAULT_DISPLAY) {
                return d;
            }
        }
        return null;
    }


    // ===== 副屏参数描述 =====

    /**
     * 生成副屏的多行参数描述(分辨率/刷新率/DPI/方向/类型等)
     */
    public String describeSecondaryDisplay() {
        Display display = getSecondaryDisplay();
        if (display == null) return context.getString(R.string.secondary_display_not_connected);

        StringBuilder sb = new StringBuilder();
        DisplayMetrics real = new DisplayMetrics();
        display.getRealMetrics(real);
        sb.append(context.getString(R.string.display_desc_title,
                display.getDisplayId(), real.widthPixels, real.heightPixels));

        Display.Mode mode = display.getMode();
        if (mode != null) {
            sb.append("\n").append(context.getString(R.string.display_refresh_rate, mode.getRefreshRate()));
        }
        sb.append(String.format(Locale.getDefault(), "  ·  DPI: %.0f", real.xdpi));
        sb.append("  ·  ").append(real.widthPixels >= real.heightPixels
                ? context.getString(R.string.orientation_landscape)
                : context.getString(R.string.orientation_portrait));

        sb.append("\n").append(context.getString(R.string.display_type, flagsToString(display.getFlags())));
        sb.append("\n").append(context.getString(R.string.display_touch,
                (display.getFlags() & F_SUPPORTS_TOUCH) != 0
                        ? context.getString(R.string.touch_supported)
                        : context.getString(R.string.touch_not_reported)));

        Display.Mode[] modes = display.getSupportedModes();
        if (modes != null && modes.length > 0) {
            sb.append("\n").append(context.getString(R.string.display_supported_modes,
                    modes.length, real.widthPixels, real.heightPixels,
                    mode != null ? mode.getRefreshRate() : 0));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String name = display.getName();
            if (name != null && !name.isEmpty()) {
                sb.append("\n").append(context.getString(R.string.display_name, name));
            }
        }
        return sb.toString();
    }

    // Display flags 位掩码(硬编码数值避免 compileSdk 版本差异)
    private static final int F_SECURE = 0x00000002;             // Display.FLAG_SECURE (1<<1)
    private static final int F_PRESENTATION = 0x00000008;        // Display.FLAG_PRESENTATION (1<<3)
    private static final int F_ROUND = 0x00000010;               // Display.FLAG_ROUND (1<<4)
    private static final int F_SUPPORTS_TOUCH = 0x00000020;      // Display.FLAG_SUPPORTS_TOUCH (1<<5, @hide 需硬编码)
    private static final int F_NOT_FOCUSABLE = 0x00000200;       // 1<<9

    /**
     * 把 Display flags 转换成可读字符串。
     */
    private String flagsToString(int flags) {
        List<String> parts = new ArrayList<>();
        if ((flags & F_SECURE) != 0) parts.add(context.getString(R.string.flag_secure));
        if ((flags & F_PRESENTATION) != 0) parts.add(context.getString(R.string.flag_presentation));
        if ((flags & F_ROUND) != 0) parts.add(context.getString(R.string.flag_round));
        if ((flags & F_SUPPORTS_TOUCH) != 0) parts.add(context.getString(R.string.flag_touch));
        if ((flags & F_NOT_FOCUSABLE) != 0)
            parts.add(context.getString(R.string.flag_not_focusable));
        if (parts.isEmpty()) parts.add(context.getString(R.string.flag_default));
        return String.join(" / ", parts);
    }

    @Override
    public int showPic(String picPath) {
        if (picPath == null || picPath.isEmpty()) {
            return Constant.ResultConstant.RESULT_ERROR_INVALID_PARAM;
        }
        if (!new File(picPath).exists()) {
            return Constant.ResultConstant.RESULT_ERROR_FILE_NOT_FOUND;
        }
        if (picturePresentation == null) {
            Display display = getSecondaryDisplay();
            if (display == null) return Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY;
            picturePresentation = new PicturePresentation(context, display);
        }
        if (picturePresentation == null) {
            return Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY;
        }

        try {
            showPresentation(picturePresentation);
            picturePresentation.showSinglePic(picPath);
            return Constant.ResultConstant.RESULT_SUCCESS;
        } catch (Exception e) {
            return Constant.ResultConstant.RESULT_ERROR_SHOW_FAILED;
        }
    }

    @Override
    public int showPic(ArrayList<String> picPathList, int intervalSeconds) {
        if (picPathList == null || picPathList.isEmpty()) {
            return Constant.ResultConstant.RESULT_ERROR_INVALID_PARAM;
        }
        // 验证文件存在性
        for (String path : picPathList) {
            if (path == null || !new File(path).exists()) {
                return Constant.ResultConstant.RESULT_ERROR_FILE_NOT_FOUND;
            }
        }
        if (picturePresentation == null) {
            Display display = getSecondaryDisplay();
            if (display == null) return Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY;
            picturePresentation = new PicturePresentation(context, display);
        }
        if (picturePresentation == null) {
            return Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY;
        }

        try {
            showPresentation(picturePresentation);
            picturePresentation.showSlideshow(picPathList, intervalSeconds);
            this.intervalSeconds = intervalSeconds;
            return Constant.ResultConstant.RESULT_SUCCESS;
        } catch (Exception e) {
            return Constant.ResultConstant.RESULT_ERROR_SHOW_FAILED;
        }
    }

    @Override
    public int showVideo(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) {
            return Constant.ResultConstant.RESULT_ERROR_INVALID_PARAM;
        }
        if (!new File(videoPath).exists()) {
            return Constant.ResultConstant.RESULT_ERROR_FILE_NOT_FOUND;
        }
        if (videoPresentation == null) {
            Display display = getSecondaryDisplay();
            if (display == null) return Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY;
            videoPresentation = new VideoPresentation(context, display);
        }
        if (videoPresentation == null) {
            return Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY;
        }

        try {
            showPresentation(videoPresentation);
            videoPresentation.playVideo(videoPath);
            return Constant.ResultConstant.RESULT_SUCCESS;
        } catch (Exception e) {
            return Constant.ResultConstant.RESULT_ERROR_SHOW_FAILED;
        }
    }

    @Override
    public void show(View view, IResultCallback callback) {
        if (viewPresentation == null) {
            Display display = getSecondaryDisplay();
            if (display == null) {
                if (callback != null)
                    callback.onFailure(Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY, context.getString(R.string.secondary_display_not_connected));
                return;
            }
            viewPresentation = new ViewPresentation(context, display);
        }
        if (viewPresentation == null) {
            if (callback != null)
                callback.onFailure(Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY, context.getString(R.string.secondary_display_not_connected));
            return;
        }

        try {
            showPresentation(viewPresentation);
            viewPresentation.showView(view);
            if (callback != null) callback.onSuccess();
        } catch (Exception e) {
            if (callback != null)
                callback.onFailure(Constant.ResultConstant.RESULT_ERROR_SHOW_FAILED, e.getMessage());
        }
    }

    @Override
    public void show(View view, ArrayList<PictureData> picDataList, IResultCallback callback) {
        if (viewPresentation == null) {
            Display display = getSecondaryDisplay();
            if (display == null) {
                if (callback != null)
                    callback.onFailure(Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY, context.getString(R.string.secondary_display_not_connected));
                return;
            }
            viewPresentation = new ViewPresentation(context, display);
        }
        if (viewPresentation == null) {
            if (callback != null)
                callback.onFailure(Constant.ResultConstant.RESULT_ERROR_NO_DISPLAY, context.getString(R.string.secondary_display_not_connected));
            return;
        }

        try {
            showPresentation(viewPresentation);
            viewPresentation.showViewWithPics(view, picDataList, intervalSeconds);
            if (callback != null) callback.onSuccess();
        } catch (Exception e) {
            if (callback != null)
                callback.onFailure(Constant.ResultConstant.RESULT_ERROR_SHOW_FAILED, e.getMessage());
        }
    }

    @Override
    public int showWallpaper() {
        return Constant.ResultConstant.RESULT_ERROR_NOT_SUPPORTED;
        /*
        Display display = getSecondaryDisplay();
        if (display == null) return ResultConstant.RESULT_ERROR_NO_DISPLAY;

        dismissCurrent();
        try {
            // 显示纯黑壁纸背景
            Presentation p = new Presentation(context, display) {
                @Override
                protected void onCreate(android.os.Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    FrameLayout root = new FrameLayout(getContext());
                    root.setBackgroundColor(Color.BLACK);
                    root.setLayoutParams(new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));
                    setContentView(root);
                }
            };
            p.show();
            currentPresentation = p;
            return ResultConstant.RESULT_SUCCESS;
        } catch (Exception e) {
            return ResultConstant.RESULT_ERROR_SHOW_FAILED;
        }*/
    }

    @Override
    public int power(boolean on) {
        if (on) {
            createPresentation();
        } else {
            dismissPresentation();
        }
        return hasSecondaryDisplay() ? Constant.ResultConstant.RESULT_SUCCESS : Constant.ResultConstant.RESULT_ERROR_NOT_SUPPORTED;
    }

    @Override
    public int getPowerOnStatus() {
        return hasSecondaryDisplay() ? 1 : 0;
    }

    @Override
    public int setBrightness(int value) {
        // Android Presentation API 无法控制副屏亮度,需硬件厂商SDK支持
        return Constant.ResultConstant.RESULT_ERROR_NOT_SUPPORTED;
    }

    @Override
    public int getBrightness() {
        // Android Presentation API 无法控制副屏亮度,需硬件厂商SDK支持
        return Constant.ResultConstant.RESULT_ERROR_NOT_SUPPORTED;
    }

    @Override
    public int setBootLogo(String filePath) {
        // Android Presentation API 无法设置开机Logo,需硬件厂商SDK支持
        return Constant.ResultConstant.RESULT_ERROR_NOT_SUPPORTED;
    }

    @Override
    public int[] getScreenResolution() {
        Display display = getSecondaryDisplay();
        if (display == null) {
            return new int[]{0, 0};
        }
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    @Override
    public String getScreenDescription() {
        return describeSecondaryDisplay();
    }

    /**
     * 切换 Presentation
     */
    private void showPresentation(Presentation presentation) {
        if (presentation instanceof PicturePresentation) {
            picturePresentation.show();
        } else {
            picturePresentation.hide();
        }
        if (presentation instanceof VideoPresentation) {
            videoPresentation.show();
        } else {
            videoPresentation.hide();
        }
        if (presentation instanceof ViewPresentation) {
            viewPresentation.show();
        } else {
            viewPresentation.hide();
        }
    }

    /**
     * 新建 Presentation
     */
    private void createPresentation() {
        Display display = getSecondaryDisplay();
        if (display == null) return;
        if (picturePresentation == null) {
            picturePresentation = new PicturePresentation(context, display);
        }
        if (videoPresentation == null) {
            videoPresentation = new VideoPresentation(context, display);
        }
        if (viewPresentation == null) {
            viewPresentation = new ViewPresentation(context, display);
        }
    }

    /**
     * 关闭 Presentation
     */
    private void dismissPresentation() {
        if (picturePresentation != null) {
            try {
                picturePresentation.hide();
                picturePresentation.dismiss();
            } catch (Exception ignored) {
            }
            picturePresentation = null;
        }
        if (videoPresentation != null) {
            try {
                videoPresentation.hide();
                videoPresentation.dismiss();
            } catch (Exception ignored) {
            }
            videoPresentation = null;
        }
        if (viewPresentation != null) {
            try {
                viewPresentation.hide();
                viewPresentation.dismiss();
            } catch (Exception ignored) {
            }
            viewPresentation = null;
        }
    }
}
