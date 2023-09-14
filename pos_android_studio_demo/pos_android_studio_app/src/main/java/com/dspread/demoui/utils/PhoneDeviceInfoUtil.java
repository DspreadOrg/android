package com.dspread.demoui.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

/**
 * Class to get terminal device information
 */
public class PhoneDeviceInfoUtil {
    //get Android version
    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    //get Device model
    public static String getModel() {
        return Build.MODEL;
    }

    //get Device name
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    //get AndroidID
    public static String getDeviceID(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

}
