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
import android.widget.Toast;

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


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            TRACE.i("usb" + "permission granted for device "
                                    + device);
                            Toast.makeText(context.getApplicationContext(), "Usb permission granted for device", Toast.LENGTH_SHORT).show();
                        }
                    } else {
//						mMyClickListener.onCencel();
                        TRACE.i("usb" + "permission denied for device " + device);
                        Toast.makeText(context.getApplicationContext(), "Usb permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @SuppressLint("NewApi")
    public ArrayList<String> GetUSBDevices(Context context) {
        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        mdevices = new HashMap<String, UsbDevice>();
        ArrayList<String> deviceList = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent usbIntent = new Intent(ACTION_USB_PERMISSION);
            usbIntent.setPackage(context.getPackageName());
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, usbIntent, PendingIntent.FLAG_MUTABLE);
        } else {
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
        }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);
        /*
         * check for existing devices
         **/
        for (UsbDevice device : mManager.getDeviceList().values()) {
            // judge whether there is permission
            if (!mManager.hasPermission(device) && (device.getVendorId() == 2965 || device.getVendorId() == 0x03EB || device.getVendorId() == 1027)) {
                // open device，get UsbDeviceConnection object ，Connect device for subsequent communication
                mManager.requestPermission(device, mPermissionIntent);
                return null;
            }
            String deviceName = null;
            UsbDeviceConnection connection = null;
            if (device.getVendorId() == 2965 || device.getVendorId() == 0x03EB
                    || device.getVendorId() == 1027 || device.getVendorId() == 6790) {
                if (!mManager.hasPermission(device)) {
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
        context.unregisterReceiver(mUsbReceiver);
        return deviceList;
    }
}
