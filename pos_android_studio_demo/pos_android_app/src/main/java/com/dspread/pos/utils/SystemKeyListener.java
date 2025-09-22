package com.dspread.pos.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;


public class SystemKeyListener {
    private Context mContext;
    private IntentFilter mFilter;
    private OnSystemKeyListener mListener;
    private InnerRecevier mRecevier;

    /**
     * @param context
     */
    public SystemKeyListener(Context context) {
        mContext = context;
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mFilter.addAction(Intent.ACTION_SCREEN_ON);

    }

    /**
     * @author miaowei
     */
    public interface OnSystemKeyListener {

        /**
         * Home Keyboard
         */
        public void onHomePressed();

        /**
         * menu Keyboard
         */
        public void onMenuPressed();

        public void onScreenOff();

        public void onScreenOn();

    }

    /**
     * @param listener
     */
    public void setOnSystemKeyListener(OnSystemKeyListener listener) {
        mListener = listener;
        mRecevier = new InnerRecevier();
    }


    /**
     * start Recevie
     */
    public void startSystemKeyListener() {
        if (mRecevier != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(mRecevier, mFilter, Context.RECEIVER_NOT_EXPORTED);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mContext.registerReceiver(mRecevier, mFilter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                mContext. registerReceiver(mRecevier, mFilter);
            }
        }
    }

    /**
     * stop Recevie
     */
    public void stopSystemKeyListener() {
        if (mRecevier != null) {
            mContext.unregisterReceiver(mRecevier);
        }
    }

    /**
     * Broadcast Receiver
     */
    class InnerRecevier extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (mListener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            // home
                            mListener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            // menu
                            mListener.onMenuPressed();
                        }
                    }
                }
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mListener.onScreenOff();
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mListener.onScreenOn();
            }
        }
    }
}



