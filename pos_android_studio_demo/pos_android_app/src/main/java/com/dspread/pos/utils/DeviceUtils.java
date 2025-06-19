package com.dspread.pos.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Spanned;

import androidx.annotation.RequiresApi;
import androidx.navigation.PopUpToBuilder;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Locale;

/**
 * [Describe the functionality of this class in one sentence]
 *
 * @author : [DH]
 * @createTime : [2024/9/3 10:43]
 * @updateRemark : [Explain the content of this modification]
 */
public class DeviceUtils {

        /**
         * Get the current mobile system language。
         *
         * @return Return the current system language. For example, if the current setting is "Chinese-China", return "zh-CN"
         */
        public static String getSystemLanguage() {
            return Locale.getDefault().getLanguage();
        }

        /**
         * Retrieve the list of languages (Locale list) on the current system
         *
         * @return  Lists of languages
         */
        public static Locale[] getSystemLanguageList() {
            return Locale.getAvailableLocales();
        }

        /**
         * obtain androidId
         * @return
         */
        public static String getAndroidId(Context context) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        /**
         * Is the camera available
         *
         * @return
         */
        public static boolean isSupportCamera(Context context) {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        }

        /**
         * Obtain mobile phone manufacturers
         *  HuaWei
         * @return Mobile phone manufacturers
         */
        public static String getPhoneBrand() {
            return Build.BRAND;
        }

        /**
         * Get phone model
         * @return Mobile phone model
         */
        public static String getPhoneModel() {
            return Build.MODEL;
        }

        /**
         * Get the current mobile system version number
         * Android     10
         * @return System Version Number
         */
        public static String getVersionRelease() {
            return Build.VERSION.RELEASE;
        }

        /**
         * Get the current mobile device name
         * Unified device model, not the device name in 'About Mobile'
         * @return device name
         */
        public static String getDeviceName() {
            return Build.DEVICE;
        }

        /**
         * HUAWEI HWELE ELE-AL00 10
         * @return
         */
        public static String getPhoneDetail() {
            return "Brand:"+DeviceUtils.getPhoneBrand() + " || Name:" + DeviceUtils.getDeviceName() + " || Model:" + DeviceUtils.getPhoneModel() + " || Version:" + DeviceUtils.getVersionRelease();
        }

        /**
         * Get the name of the phone motherboard
         *
         * @return  Motherboard name
         */
        public static String getDeviceBoard() {
            return Build.BOARD;
        }


        public static boolean isSmartDevices(){
            if("D20".equals(Build.MODEL)||"D30".equals(Build.MODEL)||"D50".equals(Build.MODEL) ||"D60".equals(Build.MODEL)
                    ||"D70".equals(Build.MODEL)||"D30M".equals(Build.MODEL)||"S10".equals(Build.MODEL)
            ||"D80".equals(Build.MODEL)||"D80K".equals(Build.MODEL)){
                return true;
            }
            return false;
        }

    public static boolean isPrinterDevices(){
        if("D30".equals(Build.MODEL)||"D60".equals(Build.MODEL)
                ||"D70".equals(Build.MODEL)||"D30M".equals(Build.MODEL) || "D80".equals(Build.MODEL)||"D80K".equals(Build.MODEL)){
            return true;
        }
        return false;
    }
        /**
         * Obtain the name of the mobile phone manufacturer
         * HuaWei
         * @return  Mobile phone manufacturer name
         */
        public static String getDeviceManufacturer() {
            return Build.MANUFACTURER;
        }

        public static String getDevieCountry(Context context){
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String code = telephonyManager.getNetworkCountryIso();
            return code;
        }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Context getGlobalApplicationContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);
            Field mInitialApplicationField = activityThreadClass.getDeclaredField("mInitialApplication");
            mInitialApplicationField.setAccessible(true);
            Application application = (Application) mInitialApplicationField.get(activityThread);
            return application.getApplicationContext();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            TRACE.d("[PrinterManager] isAppInstalled ");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            TRACE.d("not found pacakge == " + e.toString());
            return false;
        }
    }

    public static POS_TYPE getDevicePosType(String deviceTypeName){
        if(deviceTypeName.equals(POS_TYPE.UART.name())){
            return POS_TYPE.UART;
        } else if (deviceTypeName.equals(POS_TYPE.USB.name())) {
            return POS_TYPE.USB;
        } else if (deviceTypeName.equals(POS_TYPE.BLUETOOTH.name())) {
            return POS_TYPE.BLUETOOTH;
        }
        return POS_TYPE.BLUETOOTH;
    }

    public static final String UART_AIDL_SERVICE_APP_PACKAGE_NAME = "com.dspread.sdkservice";//新架构的service包名

}
