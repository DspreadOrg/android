package com.dspread.demoui.utils;

import android.os.Handler;
import android.os.Looper;

public class SingletonHandler {
    private static volatile SingletonHandler instance;
    private Handler handler;

    private SingletonHandler() {
        // 在构造函数中获取当前线程的 Looper，并创建 Handler
        handler = new Handler(Looper.getMainLooper());
    }

    public static SingletonHandler getInstance() {
        if (instance == null) {
            synchronized (SingletonHandler.class) {
                if (instance == null) {
                    instance = new SingletonHandler();
                }
            }
        }
        return instance;
    }

    public Handler getHandler() {
        return handler;
    }

}
