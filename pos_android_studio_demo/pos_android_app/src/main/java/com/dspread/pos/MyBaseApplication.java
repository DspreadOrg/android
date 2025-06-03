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
import com.dspread.pos_android_app.BuildConfig;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.upgrade.bean.UpgradeConfig;
import com.tencent.upgrade.core.UpgradeManager;

import java.util.HashMap;
import java.util.Map;

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
        String appId = "6316d5169f"; // 在shiply前端页面申请的项目Android产品的appid
        String appKey = "ffe00435-2389-4189-bd87-4b30ffcaff8e"; // 在shiply前端页面申请的项目Android产品的appkey
        UpgradeConfig.Builder builder = new UpgradeConfig.Builder();
        UpgradeConfig config = builder.appId(appId).appKey(appKey).build();
        UpgradeManager.getInstance().init(this, config);
//        Map<String, String> map = new HashMap<>();
//        map.put("UserGender", "Male");
//        builder.systemVersion(String.valueOf(Build.VERSION.SDK_INT))    // 用户手机系统版本，用于匹配shiply前端创建任务时设置的系统版本下发条件
////                .customParams(map)                                      // 自定义属性键值对，用于匹配shiply前端创建任务时设置的自定义下发条件
//                .cacheExpireTime(1000 * 60 * 60 * 6)                    // 灰度策略的缓存时长（ms），如果不设置，默认缓存时长为1天
////                .internalInitMMKVForRDelivery(true)                     // 是否由sdk内部初始化mmkv(调用MMKV.initialize()),业务方如果已经初始化过mmkv可以设置为false
////                .userId("xxx")                                          // 用户Id,用于匹配shiply前端创建的任务中的体验名单以及下发条件中的用户号码包
//                .customLogger(new TRACE());// 日志实现接口，建议对接到业务方的日志接口，方便排查问题
        builder.cacheExpireTime(1000 * 60 * 60 * 6)
                .customLogger(new TRACE());
    }
}
