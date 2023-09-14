package com.dspread.demoui.utils;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to get the current time
 */

public class DateUtils {

    public static String getNowTime() {
        SimpleDateFormat formatter;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            formatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss:SSS"); //set time format
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS"); //set time format
        }

        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08")); //set time zone

        Date curDate = new Date(System.currentTimeMillis()); //get current time

        String createDate = formatter.format(curDate);//format conversion
        return createDate;
    }
}
