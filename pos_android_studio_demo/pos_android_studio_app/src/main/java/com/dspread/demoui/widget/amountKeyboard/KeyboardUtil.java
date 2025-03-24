package com.dspread.demoui.widget.amountKeyboard;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.MoneyUtil;
import com.dspread.demoui.utils.TRACE;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 自定义键盘
 * Created by xuejinwei on 16/3/5.
 */
public class KeyboardUtil {
    private Activity mActivity;
    private boolean  mIfRandom;
    // 添加防重复点击变量
    private static final long CLICK_INTERVAL = 1000L; // 1秒内不能重复点击
    private long lastClickTime = 0;

    private MyKeyBoardView mKeyboardView;
    private Keyboard       mKeyboardNumber;//数字键盘
    private TextView       mEditText;

    public KeyboardUtil(Activity activity) {
//        this(activity, false);
    }

    public KeyboardUtil(Activity activity, View view, boolean ifRandom) {
        this.mActivity = activity;
        this.mIfRandom = ifRandom;
        mKeyboardNumber = new Keyboard(mActivity, R.xml.amount_keyboardnumber);
        mKeyboardView = (MyKeyBoardView) view.findViewById(R.id.keyboard_view2);
        TRACE.i("key number = "+mKeyboardNumber);
        TRACE.i("key view = "+mKeyboardView);
    }

    /**
     * edittext绑定自定义键盘
     *
     * @param editText 需要绑定自定义键盘的edittext
     */
    public void attachTo(TextView editText) {
        this.mEditText = editText;
        showSoftKeyboard();
    }

    public void showSoftKeyboard() {
        if (mKeyboardNumber == null) {
            mKeyboardNumber = new Keyboard(mActivity, R.xml.keyboard_only_number);
        }
        if (mKeyboardView == null) {
            mKeyboardView = (MyKeyBoardView) mActivity.findViewById(R.id.keyboard_view2);
        }
        if (mIfRandom) {
//            randomKeyboardNumber();
        } else {
            mKeyboardView.setKeyboard(mKeyboardNumber);
        }
        mKeyboardView.requestFocus();
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);

    }

    public KeyboardView.OnKeyboardActionListener getmOnKeyboardActionListener(){
        if(mOnKeyboardActionListener != null){
            return mOnKeyboardActionListener;
        }
        return null;
    }

    private StringBuffer number = new StringBuffer("");

    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Log.i("test","key boread = "+primaryCode);
            TRACE.i("number = "+number.toString());
            if (primaryCode == 67) {// 回退
                if(number.length() >=1) {
                    number.deleteCharAt(number.length() - 1);
                }
            } else if (primaryCode == 66) {// confirm
                // 添加防重复点击判断
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < CLICK_INTERVAL) {
                    return;
                }
                lastClickTime = currentTime;
                number.delete(0, number.length());
                if (mOnOkClick != null) {
                    mOnOkClick.onOkClick();
                }
                return;
            } else {
                for (Keyboard.Key key : mKeyboardNumber.getKeys()) {
                    if (key.codes[0] == primaryCode) {
                        if(number.toString().length()< 12) {
                            number.append(key.label != null ? key.label.toString() : "");
                        }
                        // 处理获取到的 label
                        break;
                    }
                }
            }
            if (number.toString().length() > 0 && number.toString().length() < 13) {
                Long amount = Long.parseLong(number.toString());
                if (amount == 0) {
                    number.delete(0, number.length());
                } else {
                    mEditText.setText("¥" + MoneyUtil.fen2yuan(amount));
                }
            }else if(number.toString().length() == 0){
                mEditText.setText("¥0.00");
            }

        }

        @Override
        public void onText(CharSequence text) {
            TRACE.i("text = "+text.toString());
        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };


    /**
     * 隐藏系统键盘
     *
     * @param editText
     */
    public static void hideSystemSofeKeyboard(Context context, EditText editText) {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= 11) {
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
        } else {
            editText.setInputType(InputType.TYPE_NULL);
        }
        // 如果软键盘已经显示，则隐藏
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public interface OnOkClick {
        void onOkClick();
    }

    public interface onCancelClick {
        void onCancellClick();
    }

    public OnOkClick mOnOkClick = null;
    public onCancelClick mOnCancelClick;

    public void setOnOkClick(OnOkClick onOkClick) {
        mOnOkClick = onOkClick;
    }

    public void setOnCancelClick(onCancelClick onCancelClick) {
        mOnCancelClick = onCancelClick;
    }


    private boolean isNumber(String str) {
        String wordstr = "0123456789";
        return wordstr.contains(str);
    }

//    private void randomKeyboardNumber() {
//        List<Keyboard.Key> keyList = mKeyboardNumber.getKeys();
//        // 查找出0-9的数字键
//        List<Keyboard.Key> newkeyList = new ArrayList<Keyboard.Key>();
//        for (int i = 0; i < keyList.size(); i++) {
//            if (keyList.get(i).label != null
//                    && isNumber(keyList.get(i).label.toString())) {
//                newkeyList.add(keyList.get(i));
//            }
//        }
//        // 数组长度
//        int count = newkeyList.size();
//        // 结果集
//        List<KeyModel> resultList = new ArrayList<KeyModel>();
//        // 用一个LinkedList作为中介
//        LinkedList<KeyModel> temp = new LinkedList<KeyModel>();
//        // 初始化temp
//        for (int i = 0; i < count; i++) {
//            temp.add(new KeyModel(48 + i, i + ""));
//        }
//        // 取数
//        Random rand = new Random();
//        for (int i = 0; i < count; i++) {
//            int num = rand.nextInt(count - i);
//            resultList.add(new KeyModel(temp.get(num).getCode(),
//                    temp.get(num).getLable()));
//            temp.remove(num);
//        }
//        for (int i = 0; i < newkeyList.size(); i++) {
//            newkeyList.get(i).label = resultList.get(i).getLable();
//            newkeyList.get(i).codes[0] = resultList.get(i)
//                    .getCode();
//        }
//
//        mKeyboardView.setKeyboard(mKeyboardNumber);
//    }

    public void showKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            mKeyboardView.setVisibility(View.VISIBLE);
        }
    }

    public void hideKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        if (visibility == View.VISIBLE) {
            mKeyboardView.setVisibility(View.GONE);
        }
    }
}