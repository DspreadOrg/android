package com.dspread.demoui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.dspread.demoui.activity.MyQposClass;
import com.dspread.demoui.http.OKHttpUpdateHttpService;
import com.dspread.demoui.interfaces.BluetoothConnectCallback;
import com.dspread.demoui.interfaces.ConnectStateCallback;
import com.dspread.demoui.interfaces.PosInfoCallback;
import com.dspread.demoui.interfaces.TransactionCallback;
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

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.xuexiang.xupdate.entity.UpdateError.ERROR.CHECK_NO_NEW_VERSION;

//import org.bouncycastle.jcajce.provider.symmetric.ARC4;

/**
 * @author user
 */
public class BaseApplication extends Application {
    public static Context getApplicationInstance;
    private BaseApplication baseApplication;
    private QPOSService pos;
    public static Handler handler;
    private static Stack<Activity> activityStack = new Stack<>();
    public static void addActivity(Activity activity) {
          activityStack.push(activity);
    }
    public static Activity popActivity() {
            return activityStack.pop();
    }
    public static void finishAllActivities() {
            while (!activityStack.isEmpty()) {
                Activity activity = popActivity();
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
    }

    public QPOSService getQposService(){
        if(pos != null){
            return pos;
        }
        return null;
    }

    public void setQposService(QPOSService pos){
      this.pos = pos;
    }

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
       MyQposClass listener = new MyQposClass();
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
        pos.initListener(listener);

    }
}
