package com.dspread.demoui.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.dspread.demoui.BaseApplication;

import io.sentry.Sentry;
import io.sentry.protocol.User;

/**
 * Created by ouyang on 2023/03/14.
 */
@SuppressLint("SimpleDateFormat")
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private static CrashHandler instance = new CrashHandler();
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return instance;
    }

    /**
     * init
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            TRACE.d("uncaughtException");
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            TRACE.d("else uncaughtException");
            Sentry.captureException(ex);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * Custom error handling, collecting error information and sending error reports are all completed here
     *
     * @param ex
     * @return
     */
    private boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        try {
            String posID = BaseApplication.getmPosID();
            User user = new User();
            user.setId(posID);
            Sentry.setUser(user);
            Sentry.captureException(ex);
            //Sentry.captureMessage(ex.toString());
            TRACE.d("uncaughtException handleException: sentry");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
