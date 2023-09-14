package com.dspread.demoui.utils;

public class ClickUtil {
    // The click interval between two clicks of a button cannot be less than 1000 milliseconds
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
