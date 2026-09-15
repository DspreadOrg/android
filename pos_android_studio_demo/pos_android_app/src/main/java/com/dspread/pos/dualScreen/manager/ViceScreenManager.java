package com.dspread.pos.dualScreen.manager;

import android.content.Context;
import android.view.View;


import com.dspread.pos.dualScreen.original.ViceScreenOriginal;

import java.util.ArrayList;

public class ViceScreenManager {
    private static volatile ViceScreenManager instance;

    private Context context;
    private int deviceType;
    private ViceScreenOriginal original;

    private ViceScreenManager(Context context, int deviceType) {
        this.context = context.getApplicationContext();
        this.deviceType = deviceType;
        this.original = new ViceScreenOriginal(context);
    }

    /**
     * 获取全局单例(默认设备类型)
     * 可在任意 Activity 或其他页面直接调用
     */
    public static ViceScreenManager getInstance(Context context) {
        return getInstance(context, 0);
    }

    /**
     * 获取全局单例(指定设备类型)
     */
    public static ViceScreenManager getInstance(Context context, int deviceType) {
        if (instance == null) {
            synchronized (ViceScreenManager.class) {
                if (instance == null) {
                    instance = new ViceScreenManager(context, deviceType);
                }
            }
        }
        return instance;
    }

    public int showPic(String picPath) {
        return original.showPic(picPath);
    }

    public int showPic(ArrayList<String> picPathList, int intervalSeconds) {
        return original.showPic(picPathList, intervalSeconds);
    }

    public int showVideo(String videoPath) {
        return original.showVideo(videoPath);
    }

    public void show(View view) {
        show(view, null);
    }

    public void show(View view, IResultCallback callback) {
        original.show(view, callback);
    }

    public void show(View view, ArrayList<PictureData> picDataList, IResultCallback callback) {
        original.show(view, picDataList, callback);
    }

    public int showWallpaper() {
        return original.showWallpaper();
    }

    public int power(boolean on) {
        return original.power(on);
    }

    public int getPowerOnStatus() {
        return original.getPowerOnStatus();
    }

    public int setBrightness(int value) {
        return original.setBrightness(value);
    }

    public int getBrightness() {
        return original.getBrightness();
    }

    public int setBootLogo(String filePath) {
        return original.setBootLogo(filePath);
    }

    public int[] getScreenResolution() {
        return original.getScreenResolution();
    }

    public String getScreenDescription() {
        return original.getScreenDescription();
    }
}
