package com.dspread.demoui.widget.pinpad.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.dspread.demoui.R;

import java.lang.reflect.Method;
import java.util.List;

/**
 * ****************************************************************
 * File Name: KeyboardUtil
 * File Description: Keyboard Util
 * ****************************************************************
 */
public class KeyboardUtil {
    private Activity mActivity;
    private View mParent;

    private PopupWindow mWindow;
    private MyKeyboardView mKeyboardView;
    private boolean needInit;
    private boolean mScrollTo = false;//whether the interface moves up
    //    private int mEditTextHeight;//edit text height 44dp
    private int mKeyboardHeight;//keyboard height 260dp
    private int mHeightPixels;//screen height
    private int mKeyBoardMarginEditTextTopHeight;//the minimum distance between the keyboard and the top of the edit text
    private List<String> dataList;
    public static EditText pinpadEditText;
    public KeyboardUtil(Activity context, View parent, List<String> dataList) {
        this.dataList = dataList;
        this.mActivity = context;
        this.mParent = parent;
        LinearLayout mIncludeKeyboardview = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.include_keyboardview, null);
//        RelativeLayout mKeyboardTopView = (RelativeLayout) mIncludeKeyboardview.findViewById(R.id.keyboard_top_rl);
        mKeyboardView = (MyKeyboardView) mIncludeKeyboardview.findViewById(R.id.keyboard_view);
        mWindow = new PopupWindow(mIncludeKeyboardview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mWindow.setAnimationStyle(R.style.AnimBottom);   //Animation style
        mWindow.setOnDismissListener(mOnDismissListener);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//prevent being blocked by the bottom toolbar
        int mEditTextHeight = dp2px(44);//44dp edit text height
        mKeyboardHeight = dp2px(260);//260dp
        mKeyBoardMarginEditTextTopHeight = mEditTextHeight * 2;
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;

        //need to add the Status bar height
        Rect rect = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        mHeightPixels = rect.bottom;
    }
    public KeyboardUtil(Activity context, List<String> dataList) {
        LinearLayout mIncludeKeyboardview = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.include_pinpad, null);
        this.dataList = dataList;
        this.mActivity =  context;
        this.mParent = mIncludeKeyboardview;

        pinpadEditText = mIncludeKeyboardview.findViewById(R.id.pinpadEditText);
        mKeyboardView = (MyKeyboardView) mIncludeKeyboardview.findViewById(R.id.keyboard_view);
        mWindow = new PopupWindow(mIncludeKeyboardview, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mWindow.setAnimationStyle(R.style.AnimBottom);   //Animation style
        mWindow.setOnDismissListener(mOnDismissListener);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);//prevent being blocked by the bottom toolbar
        int mEditTextHeight = dp2px(44);//44dp edit text height
        mKeyboardHeight = dp2px(260);//260dp
        mKeyBoardMarginEditTextTopHeight = mEditTextHeight * 2;
        mHeightPixels = context.getResources().getDisplayMetrics().heightPixels;
        initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, pinpadEditText);

    }

    public void initKeyboard(EditText... editTexts) {
        initKeyboard(MyKeyboardView.KEYBOARDTYPE_Num_Pwd, editTexts);
    }

    /**
     * init keyboard
     *
     * @param keyBoardType keyboard type
     * @param editTexts    edit text
     */
    @SuppressWarnings("all")
    public void initKeyboard(final int keyBoardType, EditText... editTexts) {
        for (final EditText editText : editTexts) {
            hideSystemSofeKeyboard(editText);
            show(keyBoardType, editText);

//            editText.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (event.getAction() == MotionEvent.ACTION_UP) {
//                        show(keyBoardType, editText);
//                    }
//                    return false;
//                }
//            });
        }
    }

    /**
     * Set edittext that does not need to use this keyboard
     *
     * @param edittexts
     */
    @SuppressWarnings("all")
    public void setOtherEdittext(EditText... editTexts) {
        for (EditText editText : editTexts) {
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //prevent situations where the keyboard is not hidden    new Handler().postDelayed(new Runnable())
                        hide();
                    }
                    return false;
                }
            });
        }
    }

    public void show(int keyBoardType, EditText editText) {
        //hide system
        KeyboardTool.hideInputForce(mActivity, editText);
        //init keyboard
        mKeyboardView.setHeight(mHeightPixels);
        if (mKeyboardView.getEditText() != editText || needInit) {
            mKeyboardView.init(editText, mWindow, keyBoardType, dataList);
        }
        //display custom keyboard
        if (mWindow != null && !mWindow.isShowing()) {
            mWindow.showAtLocation(mParent, Gravity.BOTTOM, 0, 0);
        } else {
//            mWindow = null;
        }
        //modify the position of the parent control
        int nKeyBoardToTopHeight = mHeightPixels - mKeyboardHeight;//screen height-keyboard height
        int[] editLocal = new int[2];
        editText.getLocationOnScreen(editLocal);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mParent.getLayoutParams();
        if (editLocal[1] + mKeyBoardMarginEditTextTopHeight > nKeyBoardToTopHeight) {
            int height = editLocal[1] - lp.topMargin - nKeyBoardToTopHeight;
            int mScrollToValue = height + mKeyBoardMarginEditTextTopHeight;
            lp.topMargin = 0 - mScrollToValue;
            mParent.setLayoutParams(lp);
            mScrollTo = true;
        }

//        getLocation(mKeyboardView);
    }

    public boolean hide() {

        if (mWindow != null && mWindow.isShowing()) {
            mWindow.dismiss();
            needInit = true;
            return true;
        }
        return false;
    }

    /**
     * hide system keyboard
     *
     * @param editText
     */
    private static void hideSystemSofeKeyboard(EditText editText) {
        //SDK_INT >= 11
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(editText, false);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dp2px(float dpValue) {
        float scale = mActivity.getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale + 0.5f);
    }

    //keyboard dismiss,recover parent control
    private PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            if (mScrollTo) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mScrollTo = false;
                        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mParent.getLayoutParams();
                        lp.topMargin = 0;
                        mParent.setLayoutParams(lp);
                    }
                });
            }
        }
    };

    /*
     * The minimum height of the keyboard from the top of the edit text
     **/
    public void setKeyBoardMarginEditTextTopHeight(int mKeyBoardMarginEditTextTopHeight) {
        this.mKeyBoardMarginEditTextTopHeight = mKeyBoardMarginEditTextTopHeight;
    }
}


