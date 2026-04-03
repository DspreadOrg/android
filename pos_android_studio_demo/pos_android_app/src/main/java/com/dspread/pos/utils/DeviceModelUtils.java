package com.dspread.pos.utils;

import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;

public class DeviceModelUtils {

    private enum Model {
        D20, D30, D60, MP600, FP9810, D50, M60F, D80, D80K,A68
    }

    private static String getDeviceModel() {
        return Build.MODEL;
    }

    public static boolean getPosModel() {
        if (isD20()
                || isD30()
                || isD60() || isD70() || isMP600() || isD35() || isD80()||isD50() || isD80K()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean getPrinterPosModel() {
        if (isD30() || isD30M()
                || isD60() || isD70() || isMP600()  || isD80() || isD80K()) {
            return true;
        } else {
            return false;
        }
    }
    public static boolean getPosOldModel() {
        if (isD60() || isMP600()  || isD80() || isD80K()) {
            return true;
        } else {
            return false;
        }
    }
    public static String getModel() {
        return Build.MODEL;
    }

    public static String getProjectName() {
        return SystemProperties.get("ro.pos.project.name", "");
    }

    public static String getDspreadName() {
        return SystemProperties.get("ro.dspread.model", "");
    }

    public static boolean isD20() {
        if ("D20".equals(getModel()) || "FP9810".equals(getModel())) {
            return true;
        }
        if (!TextUtils.isEmpty(getDspreadName()) && "D20".equals(getDspreadName())) {
            return true;
        }
        return false;
    }

    public static boolean isD60() {
        if (TextUtils.isEmpty(getProjectName())) {
            if ("D60".equals(getModel()) || "FP9900".equals(getModel()) || "M60F".equals(getModel())) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(getDspreadName()) && "D60".equals(getDspreadName())) {
            return true;
        }
        return false;
    }

    public static boolean isMP600() {
        if ("MP600".equals(getModel())) {
            return true;
        }
        return false;
    }

    public static boolean isS10() {
        if ("S10".equals(getModel())) {
            return true;
        }
        return false;
    }

    public static boolean isD70() {
        if (TextUtils.isEmpty(getProjectName())) {
            if ("D70".equals(getModel())) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(getDspreadName()) && "D70".equals(getDspreadName())) {
            return true;
        }
        return false;
    }

    public static boolean isD30() {
        if (TextUtils.isEmpty(getProjectName())) {
            if ("D30".equals(getModel())) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(getDspreadName()) && "D30".equals(getDspreadName())) {
            return true;
        }
        return false;
    }

    public static boolean isD30M() {
        if ("D30M".equals(getModel())){
            return true;
        }
        if ("D30M-MU".equals(getProjectName())){
            return true;
        }
        return false;
    }

    public static boolean isD35() {
        if ("D35".equals(getModel())){
            return true;
        }
        return "D35-MU".equals(getProjectName()) || "D35-NU".equals(getProjectName());
    }

    public static boolean isD80() {
        if ("D80".equals(getModel())) {
            return true;
        }
        if ("D80-MU".equals(getProjectName())){
            return true;
        }
        return false;
    }

    public static boolean isD80K() {
        if ("D80K".equals(getModel())){
            return true;
        }
        if ("D80K-MU".equals(getProjectName())){
            return true;
        }
        return false;
    }

    public static boolean isD50() {
        if ("D50".equals(getModel())){
            return true;
        }
        if ("D50-MU".equals(getProjectName())){
            return true;
        }
        return false;
    }
}
