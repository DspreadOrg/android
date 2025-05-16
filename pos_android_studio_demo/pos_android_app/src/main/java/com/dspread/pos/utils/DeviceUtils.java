package com.dspread.pos.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.navigation.PopUpToBuilder;

import com.dspread.pos.common.enums.POS_TYPE;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * [一句话描述该类的功能]
 *
 * @author : [DH]
 * @createTime : [2024/9/3 10:43]
 * @updateRemark : [说明本次修改内容]
 */
public class DeviceUtils {

        /**
         * 获取当前手机系统语言。
         *
         * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
         */
        public static String getSystemLanguage() {
            return Locale.getDefault().getLanguage();
        }

        /**
         * 获取当前系统上的语言列表(Locale列表)
         *
         * @return  语言列表
         */
        public static Locale[] getSystemLanguageList() {
            return Locale.getAvailableLocales();
        }

        /**
         * 获取androidId
         * @return
         */
        public static String getAndroidId(Context context) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        /**
         * 相机是否可用
         *
         * @return
         */
        public static boolean isSupportCamera(Context context) {
            PackageManager packageManager = context.getPackageManager();
            return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        }

        /**
         * 获取手机厂商
         *  HuaWei
         * @return 手机厂商
         */
        public static String getPhoneBrand() {
            return Build.BRAND;
        }

        /**
         * 获取手机型号
         * @return 手机型号
         */
        public static String getPhoneModel() {
            return Build.MODEL;
        }

        /**
         * 获取当前手机系统版本号
         * Android     10
         * @return 系统版本号
         */
        public static String getVersionRelease() {
            return Build.VERSION.RELEASE;
        }

        /**
         * 获取当前手机设备名
         * 设备统一型号,不是"关于手机"的中设备名
         * @return 设备名
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
         * 获取手机主板名
         *
         * @return  主板名
         */
        public static String getDeviceBoard() {
            return Build.BOARD;
        }


        public static boolean isSmartDevices(){
            if("D20".equals(Build.MODEL)||"D30".equals(Build.MODEL)||"D50".equals(Build.MODEL) ||"D60".equals(Build.MODEL)
                    ||"D70".equals(Build.MODEL)||"D30M".equals(Build.MODEL)||"S10".equals(Build.MODEL)){
                return true;
            }
            return false;
        }

    public static boolean isPrinterDevices(){
        if("D30".equals(Build.MODEL)||"D60".equals(Build.MODEL)
                ||"D70".equals(Build.MODEL)||"D30M".equals(Build.MODEL)){
            return true;
        }
        return false;
    }
        /**
         * 获取手机厂商名
         * HuaWei
         * @return  手机厂商名
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
