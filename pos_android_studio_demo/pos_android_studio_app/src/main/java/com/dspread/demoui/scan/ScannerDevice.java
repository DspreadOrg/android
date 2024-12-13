package com.dspread.demoui.scan;

import com.dspread.sdkdevservice.aidl.scanner.SDKScanner;

public abstract class ScannerDevice {
    public abstract void initScannerDevice();
    public abstract SDKScanner getScannerDevice();
}
