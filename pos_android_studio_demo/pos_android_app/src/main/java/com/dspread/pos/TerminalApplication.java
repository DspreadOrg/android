package com.dspread.pos;

import android.content.Context;
import android.os.Build;


import com.dspread.pos.posAPI.BaseQPOSClass;
import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.posAPI.POS;
import com.dspread.pos.ui.main.MainActivity;
import com.dspread.pos.utils.DevUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BuildConfig;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.upgrade.bean.UpgradeConfig;
import com.tencent.upgrade.core.UpgradeManager;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.crash.CaocConfig;


/**
 * @author user
 */
public class TerminalApplication extends BaseApplication {
    public static Context getApplicationInstance;
    private static QPOSService pos;

    @Override
    public void onCreate() {
        super.onCreate();
        getApplicationInstance = this;
        initCrash();
        initBugly();
        initShiply();
        // Initialize Fragment Cache
        FragmentCacheManager.getInstance();
        TRACE.setContext(this);
    }

    // Optimize the QPOSService retrieval method
    public static QPOSService getQposService() {
        return pos;
    }

    public void open(QPOSService.CommunicationMode mode, Context context) {
        TRACE.d("open");
       BaseQPOSClass listener = new BaseQPOSClass();
        pos = QPOSService.getInstance(context, mode);
        if (pos == null) {
            return;
        }
        
        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
        pos.setContext(this);
        pos.initListener(listener);
        POS.getInstance().setQPOSService(pos);
    }

    private void initCrash() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //Background mode, activate immersive mode
                .enabled(true) //Do you want to initiate global exception capture
                .showErrorDetails(true) //Whether to display detailed error information
                .showRestartButton(true) //Is the restart button displayed
                .trackActivities(true) //Whether to track Activity
                .minTimeBetweenCrashesMs(2000) //Interval between crashes (milliseconds)
                .errorDrawable(R.mipmap.ic_dspread_logo) //error icon
                .restartActivity(MainActivity.class) //Activity after restart
//                .errorActivity(YourCustomErrorActivity.class) //Error activity after crash
//                .eventListener(new YourCustomEventListener()) //Error listening after crash
                .apply();
    }

    private void initBugly() {
        Context context = getApplicationContext();
        // Get the current package name
        String packageName = context.getPackageName();
        // Get the current process name
        String processName = DevUtils.getProcessName(android.os.Process.myPid());
        // Set whether it is a reporting process
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        strategy.setAppVersion(DevUtils.getPackageVersionName(this, packageName));
        strategy.setAppPackageName(packageName);

        // Initialize Bugly
        CrashReport.initCrashReport(context, "b2d80aa171", BuildConfig.DEBUG, strategy);

        // Set user data
        CrashReport.setUserId(DevUtils.getDeviceId(this));

        // Add custom logs
        CrashReport.setUserSceneTag(context, 9527); // Set label
        CrashReport.putUserData(context, "deviceModel", Build.MODEL);
        CrashReport.putUserData(context, "deviceManufacturer", Build.MANUFACTURER);
    }

    private void initShiply(){
        String appId = "6316d5169f"; // The appid of the Android product applied for on the front-end page of Shiply
        String appKey = "ffe00435-2389-4189-bd87-4b30ffcaff8e"; // The appkey for Android products applied for on the front-end page of Shiply
        UpgradeConfig.Builder builder = new UpgradeConfig.Builder();
        UpgradeConfig config = builder.appId(appId).appKey(appKey).build();
        UpgradeManager.getInstance().init(this, config);
//        Map<String, String> map = new HashMap<>();
//        map.put("UserGender", "Male");
//        builder.systemVersion(String.valueOf(Build.VERSION.SDK_INT))    // The user's mobile system version is used to match the system version distribution conditions set when creating tasks in the Shiply frontend
////                .customParams(map)                                      // Custom attribute key value pairs are used to match the custom distribution conditions set when creating tasks in the shiply frontend
//                .cacheExpireTime(1000 * 60 * 60 * 6)                    // The cache duration of the grayscale strategy (ms), if not set, defaults to 1 day
////                .internalInitMMKVForRDelivery(true)                     // Is mmkv initialized internally by the SDK (calling MMKV. initializes)? If the business has already initialized mmkv, it can be set to false
////                .userId("xxx")                                          // User ID, used to match the experience list in the tasks created by Shiply frontend and the user number package in the distribution conditions
//                .customLogger(new TRACE());// Log implementation interface, it is recommended to connect to the log interface of the business side for easy troubleshooting
        builder.cacheExpireTime(1000 * 60 * 60 * 6)
                .customLogger(new TRACE());
    }
}
