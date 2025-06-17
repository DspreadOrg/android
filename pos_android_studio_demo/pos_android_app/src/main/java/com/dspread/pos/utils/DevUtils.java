package com.dspread.pos.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.dspread.pos.ui.payment.PaymentActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.UUID;

public class DevUtils {
    /**
     * Gets the version number
     *
     * @return The version number of the current app
     */
    public static String getPackageVersionName(Context context, String pkgName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            //PackageManager.GET_CONFIGURATIONS
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get process name
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Obtain the unique identifier of the device
     */
    public static String getDeviceId(Context context) {
        String deviceId = "";
        try {
            // Obtain the device serial number
            String serial = Build.SERIAL;
            // Obtain ANDROID_ID
            String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            // Obtain device hardware information
            String hardware = Build.HARDWARE;
            // Obtain device fingerprint
            String fingerprint = Build.FINGERPRINT;
            String country = DeviceUtils.getDevieCountry(context);

            // Generate a unique ID by combining device information
            deviceId = androidId + "-"+hardware + "-"+ country + "-"+ fingerprint;
        } catch (Exception e) {
            e.printStackTrace();
            // If the acquisition fails, use UUID as an alternative solution
            deviceId = UUID.randomUUID().toString();
        }
        return deviceId;
    }

    /**
     * SHA256 encryption
     */
    private static String SHA256(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * Convert bytes to hex
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            String temp = Integer.toHexString(b & 0xFF);
            if (temp.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }
}
