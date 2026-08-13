package com.dspread.pos.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class USBClass {
    private static UsbManager mManager = null;
    private static HashMap<String, UsbDevice> mdevices;
    public static HashMap<String, UsbDevice> getMdevices() {
        return mdevices;
    }
    private static PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private UsbPermissionListener usbPermissionListener;
    // Prevent re-registering the same receiver multiple times.
    // Each GetUSBDevices() call must register once and onReceive must unregister.
    private volatile boolean isReceiverRegistered = false;
    // Saved device reference for API < 31 where intent.getParcelableExtra(EXTRA_DEVICE)
    // returns null in the broadcast callback.
    private UsbDevice mPendingPermissionDevice;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device;
                    boolean granted;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // API 31+: EXTRA_PERMISSION_GRANTED and device extra are reliable
                        device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    } else {
                        // API < 31: device extra may be null, EXTRA_PERMISSION_GRANTED is buggy,
                        // hasPermission() not yet updated. Use saved device + openDevice().
                        device = mPendingPermissionDevice;
                        mPendingPermissionDevice = null;
                        if (device != null && mManager != null) {
                            UsbDeviceConnection connection = mManager.openDevice(device);
                            granted = (connection != null);
                            if (connection != null) {
                                connection.close();
                            }
                        } else {
                            granted = false;
                        }
                    }
                    if (granted) {
                        if (device != null) {
                            TRACE.i("usb permission granted for device " + device);
                            if (usbPermissionListener != null) {
                                usbPermissionListener.onPermissionGranted(device);
                            }
                        }
                    } else {
                        TRACE.i("usb permission denied for device " + device);
                        if (usbPermissionListener != null) {
                            usbPermissionListener.onPermissionDenied(device);
                        }
                    }
                    // Unregister receiver immediately to avoid accumulation
                    try {
                        context.unregisterReceiver(this);
                        isReceiverRegistered = false;
                    } catch (IllegalArgumentException e) {
                        // Already unregistered
                    }
                }
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @SuppressLint({"NewApi", "UnspecifiedRegisterReceiverFlag"})
    public ArrayList<String> GetUSBDevices(Context context) {
        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        mdevices = new HashMap<String, UsbDevice>();
        ArrayList<String> deviceList = new ArrayList<String>();

        // PendingIntent: use FLAG_CANCEL_CURRENT to ensure a fresh intent each time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent usbIntent = new Intent(ACTION_USB_PERMISSION);
            usbIntent.setPackage(context.getPackageName());
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, usbIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                            ACTION_USB_PERMISSION),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
        }
        // Register receiver only if not already registered
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(mUsbReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(mUsbReceiver, filter);
            }
            isReceiverRegistered = true;
        }
        /*
         * check for existing devices
         **/
        for (UsbDevice device : mManager.getDeviceList().values()) {
            if (!mManager.hasPermission(device) && (device.getVendorId() == 2965 || device.getVendorId() == 0x03EB || device.getVendorId() == 1027)) {
                mPendingPermissionDevice = device;
                mManager.requestPermission(device, mPermissionIntent);
                return null;
            }
            String deviceName = null;
            UsbDeviceConnection connection = null;
            if (device.getVendorId() == 2965 || device.getVendorId() == 0x03EB
                    || device.getVendorId() == 1027 || device.getVendorId() == 6790) {
                if (!mManager.hasPermission(device)) {
                    mPendingPermissionDevice = device;
                    mManager.requestPermission(device, mPermissionIntent);
                    return null;
                }
                connection = mManager.openDevice(device);
                byte rawBuf[] = new byte[255];
                int len = connection.controlTransfer(0x80, 0x06, 0x0302,
                        0x0409, rawBuf, 0x00FF, 60);
                rawBuf = Arrays.copyOfRange(rawBuf, 2, len);
                deviceName = new String(rawBuf);
                deviceList.add(deviceName);
                mdevices.put(deviceName, device);
            }
        }
        // Clean up receiver when permission was already granted (no requestPermission was called)
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(mUsbReceiver);
                isReceiverRegistered = false;
            } catch (IllegalArgumentException e) {
                // Already unregistered
            }
        }
        return deviceList;
    }

    public void setUsbPermissionListener(UsbPermissionListener listener) {
        this.usbPermissionListener = listener;
    }

    public interface UsbPermissionListener {
        void onPermissionGranted(UsbDevice device);
        void onPermissionDenied(UsbDevice device);
    }
}
