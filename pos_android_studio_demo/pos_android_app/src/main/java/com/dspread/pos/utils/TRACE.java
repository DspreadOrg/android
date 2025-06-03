package com.dspread.pos.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.upgrade.callback.Logger;


public class TRACE implements Logger {
    public static String NEW_LINE = System.getProperty("line.separator");

    private static String AppName = "POS_LOG";
    private static Boolean isTesting = true;
    private static Context mContext;
    private static LogFileConfig logFileConfig;

    public static void setContext(Context context){
        mContext = context;
        logFileConfig = LogFileConfig.getInstance(context);
    }

    public static void v(String string) {
        if (isTesting) {
            Log.v(AppName, string);
//            Sentry.captureMessage(string);
            if(logFileConfig != null){
                logFileConfig.writeLog(string);
            }

        }
    }

    public static void i(String string) {
        if (isTesting) {
            Log.i(AppName, string);
//            Sentry.captureMessage(string);
            if(logFileConfig != null){
                logFileConfig.writeLog(string);
            }

        }
    }

    public static void w(String string) {
        if (isTesting) {
            Log.w(AppName, string);
//            Sentry.captureMessage(string);
            if(logFileConfig != null){
                logFileConfig.writeLog(string);
            }

        }
    }

    public static void e(Exception exception) {
        if (isTesting) {
            Log.e(AppName, exception.toString());
            if(logFileConfig != null){
                logFileConfig.writeLog(exception.toString());
            }
        }
    }

    public static void e(String exception) {
        if (isTesting) {
            Log.e(AppName, exception);
            if(logFileConfig != null){
                logFileConfig.writeLog(exception);
            }
        }
    }

    public static void d(String string) {
        if (isTesting) {
            Log.d(AppName, string);
//            String posID = BaseApplication.getmPosID();
//            User user = new User();
//            user.setId(posID);
//            Sentry.setUser(user);
//            Sentry.captureMessage(string);
            if(logFileConfig != null){
                logFileConfig.writeLog(string);
            }
        }
    }

    public static void a(int num) {
        if (isTesting) {
            Log.d(AppName, Integer.toString(num));
            if(logFileConfig != null){
                logFileConfig.writeLog(String.valueOf(num));
            }
        }
    }

    @Override
    public void v(String tag, String msg) {
        Log.v(AppName + tag, msg);
    }

    @Override
    public void v(String tag, String msg, Throwable throwable) {
        Log.v(AppName + tag, msg, throwable);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(AppName + tag, msg);
    }

    @Override
    public void d(String tag, String msg, Throwable throwable) {
        Log.d(AppName + tag, msg, throwable);
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(AppName + tag, msg);
    }

    @Override
    public void i(String tag, String msg, Throwable throwable) {
        Log.i(AppName + tag, msg, throwable);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(AppName + tag, msg);
    }

    @Override
    public void w(String tag, String msg, Throwable throwable) {
        Log.w(AppName + tag, msg, throwable);
    }

    @Override
    public void e(String tag, String msg) {
        Log.e(AppName + tag, msg);
    }

    @Override
    public void e(String tag, String msg, Throwable throwable) {
        Log.e(AppName + tag, msg, throwable);
    }
}