package com.example.despreaddemo.activity;

import android.app.Application;
import android.content.Context;

import com.example.despreaddemo.http.OKHttpUpdateHttpService;
import com.lzy.okgo.OkGo;
import com.xuexiang.xhttp2.XHttp;
import com.xuexiang.xhttp2.XHttpSDK;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.entity.UpdateError;
import com.xuexiang.xupdate.listener.OnUpdateFailureListener;
import com.xuexiang.xupdate.utils.UpdateUtils;
import com.xuexiang.xutil.tip.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

/**
 * @author user
 */
public class BaseApplication extends Application {
    public static Context getApplicationInstance;



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //  Default init
        OkGo.getInstance().init(this);
        initXHttp();

        initOKHttpUtils();
        initAppUpDate();
    }


    private void initOKHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20000L, TimeUnit.MILLISECONDS)
                .readTimeout(20000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    private void initXHttp() {
        //初始化网络请求框架，必须首先执行
        XHttpSDK.init(this);
        //需要调试的时候执行
        XHttpSDK.debug("XHttp");
        XHttp.getInstance().setTimeout(20000);
    }

    private void initAppUpDate() {

        XUpdate.get()
                .debug(true)
                .isWifiOnly(true)
                // By default, only version updates are checked under WiFi
                .isGet(true)
                // The default setting uses Get request to check versions
                .isAutoMode(false)
                // The default setting is non automatic mode
                .param("versionCode", UpdateUtils.getVersionCode(this))
                // Set default public request parameters
                .param("appKey", getPackageName())
                .setOnUpdateFailureListener(new OnUpdateFailureListener() {
                    // Set listening for version update errors
                    @Override
                    public void onFailure(UpdateError error) {
                        if (error.getCode() != CHECK_NO_NEW_VERSION) {
                            // Handling different errors
                            ToastUtils.toast(error.toString());
                        }
                    }
                })
                .supportSilentInstall(true)
                // Set whether silent installation is supported. The default is true
                .setIUpdateHttpService(new OKHttpUpdateHttpService())
                // This must be set! Realize the network request function.
                .init(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        getApplicationInstance = this;
    }
}
