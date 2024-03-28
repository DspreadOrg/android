package com.dspread.demoui.activity;

import com.dspread.demoui.utils.ActivityCollector;
import com.dspread.demoui.utils.TRACE;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;

import static com.dspread.demoui.activity.BaseApplication.mPrinter;

public class PrinterListenerClass implements PrintListener {
    @Override
    public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
        TRACE.d("printResult:" + b);
        if (mPrinter != null) {
            mPrinter.close();
        }
        if (ActivityCollector.activities != null && ActivityCollector.activities.size() > 0) {
            ActivityCollector.finishOneActivity(SuccessActivity.class.getName());
        } else {
            TRACE.d("printResultxxxx:" + ActivityCollector.activities.size());
        }
    }
}
