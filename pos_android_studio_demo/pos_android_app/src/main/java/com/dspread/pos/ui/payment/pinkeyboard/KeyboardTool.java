package com.dspread.pos.ui.payment.pinkeyboard;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * ****************************************************************
 * File Name: KeyboardTool
 * File Description: Keyboard Tool
 * ****************************************************************
 */
public class KeyboardTool {
    /**
     * hide keyboard
     *
     * @param v     The focus view
     * @param views Input box
     * @return true means the focus is on edit
     */
    public static boolean isFocusEditText(View v, View... views) {
        if (v instanceof EditText && views != null && views.length > 0) {
            for (View view : views) {
                if (v == view) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * whether touch specified view
     **/
    public static boolean isTouchView(View[] views, MotionEvent ev) {
        if (views == null || views.length == 0) {
            return false;
        }
        int[] location = new int[2];
        for (View view : views) {
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            if (ev.getX() > x && ev.getX() < (x + view.getWidth()) && ev.getY() > y && ev.getY() < (y + view.getHeight())) {
                return true;
            }
        }
        return false;
    }

    /**
     * hide soft keyboard
     */
    public static void hideInputForce(Activity activity, View currentFocusView) {
        if (activity == null || currentFocusView == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
        }
    }
}
