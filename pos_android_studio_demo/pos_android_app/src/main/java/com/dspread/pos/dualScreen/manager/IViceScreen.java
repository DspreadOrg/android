package com.dspread.pos.dualScreen.manager;

import android.view.View;

import java.util.ArrayList;

/**
 * 副屏控制接口
 *
 * <p>定义了副屏图片显示、视频播放、自定义View显示、
 * 电源控制、亮度调节、开机Logo、分辨率查询等能力。</p>
 */
public interface IViceScreen {

    /** 显示单张图片 */
    int showPic(String picPath);

    /** 显示图片轮播 */
    int showPic(ArrayList<String> picPathList, int intervalSeconds);

    /** 播放视频 */
    int showVideo(String videoPath);

    /** 显示自定义View */
    void show(View view, IResultCallback callback);

    /** 显示自定义View + 图片轮播 */
    void show(View view, ArrayList<PictureData> picDataList, IResultCallback callback);

    /** 显示壁纸 */
    int showWallpaper();

    /** 电源开关 */
    int power(boolean on);

    /** 获取电源状态 */
    int getPowerOnStatus();

    /** 设置亮度 */
    int setBrightness(int value);

    /** 获取亮度 */
    int getBrightness();

    /** 设置开机Logo */
    int setBootLogo(String filePath);

    /** 获取副屏分辨率 [width, height] */
    int[] getScreenResolution();

    /** 获取副屏描述信息*/
    String getScreenDescription();
}
