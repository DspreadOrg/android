package com.dspread.demoui.scan;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

import com.dspread.demoui.utils.TRACE;
import com.dspread.sdkdevservice.aidl.constant.SDKDevConstant;
import com.dspread.sdkdevservice.aidl.deviceService.SDKDeviceService;
import com.dspread.sdkdevservice.aidl.scanner.SDKScanner;

public class D30MScanner extends ScannerDevice {
    private static final String TAG = D30MScanner.class.getName().toString();
    private ServiceConnection devServiceConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
            setDeviceService(null);
        }

        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            if (serviceBinder != null) {
                TRACE.w(TAG + ":onServiceConnected");
                setDeviceService(SDKDeviceService.Stub.asInterface(serviceBinder));
                linkToDeathSDKDevService(serviceBinder);
            }
        }
    };
    private SDKDeviceService deviceService;

    public void setDeviceService(SDKDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void initScannerDevice() {
        TRACE.d(TAG + ":start bind ScannerDeviceService");
        bindSDKDevService();
    }

    @Override
    public SDKScanner getScannerDevice() {
        if (deviceService != null) {
            try {
                TRACE.d(TAG + ":getScannerDevice ScannerDevice");
                return SDKScanner.Stub.asInterface(deviceService.getScanner());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    private void linkToDeathSDKDevService(IBinder service) {
        try {
            service.linkToDeath(new IBinder.DeathRecipient() {
                public void binderDied() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            unbindSDKDevService();
                            SystemClock.sleep(2000);
                            bindSDKDevService();
                        }
                    }).start();
                }
            }, 0);
        } catch (Exception e) {
        }
    }

    private void unbindSDKDevService() {
        if (devServiceConnection != null && deviceService != null) {
            Intent intent = new Intent();
            intent.setPackage(SDKDevConstant.SERVICE_PACKAGE_NAME);
            intent.setAction(SDKDevConstant.SERVICE_ACTION_NAME);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ContextUtil.getGlobalApplicationContext().unbindService(devServiceConnection);
            }
            setDeviceService(null);
        }
    }

    private void bindSDKDevService() {
        Intent intent = new Intent();
        intent.setPackage(SDKDevConstant.SERVICE_PACKAGE_NAME);
        intent.setAction(SDKDevConstant.SERVICE_ACTION_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ContextUtil.getGlobalApplicationContext().bindService(intent, devServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }
}
