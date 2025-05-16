package com.dspread.pos;

import android.content.Context;
import android.os.Build;
import android.os.Handler;


import com.dspread.pos.posAPI.MyQposClass;
import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.posAPI.POSCommand;
import com.dspread.pos.ui.main.MainActivity;
import com.dspread.pos.utils.DevUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_new_android_app.BuildConfig;
import com.dspread.pos_new_android_app.R;
import com.dspread.xpos.QPOSService;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.raft.standard.storage.IRStorage;
import com.tencent.rdelivery.DependencyInjector;
import com.tencent.rdelivery.RDelivery;
import com.tencent.rdelivery.RDeliverySetting;
import com.tencent.rdelivery.dependencyimpl.HandlerTask;
import com.tencent.rdelivery.dependencyimpl.HttpsURLConnectionNetwork;
import com.tencent.rdelivery.dependencyimpl.MmkvStorage;
import com.tencent.rdelivery.dependencyimpl.SystemLog;
import com.tencent.upgrade.bean.UpgradeConfig;
import com.tencent.upgrade.core.UpgradeManager;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.crash.CaocConfig;


/**
 * @author user
 */
public class MyBaseApplication extends BaseApplication {
    public static Context getApplicationInstance;
    private static QPOSService pos;

    @Override
    public void onCreate() {
        super.onCreate();
        getApplicationInstance = this;
        initCrash();
        initBugly();
        initShiply();
        // 初始化Fragment缓存
        FragmentCacheManager.getInstance();
        TRACE.setContext(this);
    }

    // 优化 QPOSService 获取方法
    public static QPOSService getQposService() {
        return pos;
    }

    public void open(QPOSService.CommunicationMode mode, Context context) {
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
        pos.initListener(listener);
        POSCommand.getInstance().setQPOSService(pos);
    }

    private void initCrash() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //背景模式,开启沉浸式
                .enabled(true) //是否启动全局异常捕获
                .showErrorDetails(true) //是否显示错误详细信息
                .showRestartButton(true) //是否显示重启按钮
                .trackActivities(true) //是否跟踪Activity
                .minTimeBetweenCrashesMs(2000) //崩溃的间隔时间(毫秒)
                .errorDrawable(R.mipmap.ic_dspread_logo) //错误图标
                .restartActivity(MainActivity.class) //重新启动后的activity
//                .errorActivity(YourCustomErrorActivity.class) //崩溃后的错误activity
//                .eventListener(new YourCustomEventListener()) //崩溃后的错误监听
                .apply();
    }

    private void initBugly() {
        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = DevUtils.getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        strategy.setAppVersion(DevUtils.getPackageVersionName(this, packageName));
        strategy.setAppPackageName(packageName);

        // 初始化Bugly
        CrashReport.initCrashReport(context, "b2d80aa171", BuildConfig.DEBUG, strategy);

        // 设置用户数据
        CrashReport.setUserId(DevUtils.getDeviceId(this));

        // 添加自定义日志
        CrashReport.setUserSceneTag(context, 9527); // 设置标签
        CrashReport.putUserData(context, "deviceModel", Build.MODEL);
        CrashReport.putUserData(context, "deviceManufacturer", Build.MANUFACTURER);
    }

    private void initShiply(){
        UpgradeConfig.Builder builder = new UpgradeConfig.Builder();
        UpgradeConfig config = builder.appId("592313ecc0").appKey("bd73c4b1-4d6f-4739-bf16-dd7acea9c3ce")
                .cacheExpireTime(1000 * 60 * 60 * 6) // 灰度策略的缓存时长（ms），如果不设置，默认缓存时长为1天
                .build();
        UpgradeManager.getInstance().init(this, config);

    }
}
