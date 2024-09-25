package com.dspread.demoui.activity;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.dspread.demoui.http.OKHttpUpdateHttpService;
import com.dspread.demoui.utils.TRACE;
import com.dspread.xpos.QPOSService;
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
    public static QPOSService pos;
    public static Handler handler;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        open(QPOSService.CommunicationMode.UART_SERVICE,base);
//        pos.setDeviceAddress("/dev/ttyS1");
//        pos.openUart();
//        pos.setD20Trade(true);

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
        XHttpSDK.init(this);
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
    public void open(QPOSService.CommunicationMode mode,Context context) {
        TRACE.d("open");
//       MyQposClass listener = new MyQposClass();
        pos = QPOSService.getInstance(context, mode);
        if (pos == null) {
            return;
        }
        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
        pos.setD20Trade(true);

        pos.setContext(this);

        handler = new Handler(Looper.myLooper());
//        pos.initListener(handler, listener);


    }
}
