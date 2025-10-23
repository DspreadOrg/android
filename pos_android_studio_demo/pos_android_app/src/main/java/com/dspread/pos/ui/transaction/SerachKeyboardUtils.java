package com.dspread.pos.ui.transaction;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SerachKeyboardUtils {
    
    /**
     * 隐藏软键盘
     */
    public static void hideKeyboard(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    /**
     * 隐藏指定 View 的软键盘
     */
    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    /**
     * 强制隐藏软键盘（不依赖焦点）
     */
    public static void hideKeyboardForce(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        
        View view = activity.getWindow().getDecorView();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}