package com.dspread.demoui.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetCheckHelper {

    //判断网络连接是否可用（返回true表示网络可用，false为不可用）
    public static  boolean checkNetworkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();
        //获取手机所有链接管理对象（包括对Wi-Fi，net等连接的管理）
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        } else {
            //获取NetworkInfo对象
            NetworkInfo[] info = manager.getAllNetworkInfo();
            if (info != null && info.length > 0) {
                for (int i = 0; i < info.length; i++) {
                    // 判断当前网络状态是否为连接状态
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
