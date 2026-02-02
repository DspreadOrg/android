package com.dspread.pos.ui.payment.pinkeyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.security.keystore.KeyInfo;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.PopupWindow;


import com.dspread.pos.utils.QPOSUtil;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ****************************************************************
 * File Name: MyKeyboardView
 * File Description: Keyboard View
 * ****************************************************************
 */
public class MyKeyboardView extends KeyboardView {
    public static final int KEYBOARDTYPE_Num = 0;//Number  keyboard
    public static final int KEYBOARDTYPE_Num_Pwd = 1;//Number type keyboard(password)
    public static final int KEYBOARDTYPE_ABC = 2;//letter keyboard
    public static final int KEYBOARDTYPE_Symbol = 4;//symbol keyboard
    public static final int KEYBOARDTYPE_Only_Num_Pwd = 5;//only number keyboard

    private final String strLetter = "abcdefghijklmnopqrstuvwxyz";//letter

    private EditText mEditText;
    private PopupWindow mWindow;
    private Activity mActivity;

    private Keyboard keyboardNum;
    private Keyboard keyboardNumPwd;
    private Keyboard keyboardOnlyNumPwd;
    private Keyboard keyboardABC;
    private Keyboard keyboardSymbol;
    private int mHeightPixels;//screen height

    public boolean isSupper = false;//whether the letter keyboard is capitalized
    public boolean isPwd = false;//whether the numbers on the number keyboard are random
    private int keyBoardType;//keyboard type
    private List<String> dataList = new ArrayList<>();

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setHeight(int mHeightPixels) {
        this.mHeightPixels = mHeightPixels;
    }

    public void setContext(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void init(EditText editText, PopupWindow window, int keyBoardType, List<String> dataList) {
        this.dataList = dataList;
        this.mEditText = editText;
        this.mWindow = window;
        this.keyBoardType = keyBoardType;
        if (keyBoardType == KEYBOARDTYPE_Num_Pwd || keyBoardType == KEYBOARDTYPE_Only_Num_Pwd) {
            isPwd = true;
        }
        setEnabled(true);
        setPreviewEnabled(false);
        setOnKeyboardActionListener(mOnKeyboardActionListener);
        setKeyBoardType(keyBoardType);
    }

    public EditText getEditText() {
        return mEditText;
    }

    /**
     * set keyboard type
     */
    public void setKeyBoardType(int keyBoardType) {
        switch (keyBoardType) {
            case KEYBOARDTYPE_Num:
                if (keyboardNum == null) {
                    keyboardNum = new Keyboard(getContext(), R.xml.keyboard_number);
                }
                setKeyboard(keyboardNum);
                break;
            case KEYBOARDTYPE_ABC:
//                if (keyboardABC == null) {
//                    keyboardABC = new Keyboard(getContext(), R.xml.keyboard_abc);
//                }
//                setKeyboard(keyboardABC);
                break;
            case KEYBOARDTYPE_Num_Pwd:
                if (keyboardNumPwd == null) {
                    keyboardNumPwd = new Keyboard(getContext(), R.xml.keyboard_number);
                }
                randomKey(keyboardNumPwd);
                setKeyboard(keyboardNumPwd);
                break;
            case KEYBOARDTYPE_Symbol:
                if (keyboardSymbol == null) {
                    keyboardSymbol = new Keyboard(getContext(), R.xml.keyboard_symbol);
                }
                setKeyboard(keyboardSymbol);
                break;
            case KEYBOARDTYPE_Only_Num_Pwd:
                if (keyboardOnlyNumPwd == null) {
                    keyboardOnlyNumPwd = new Keyboard(getContext(), R.xml.keyboard_number_ui);
                }
//                randomKey(keyboardOnlyNumPwd);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        randomKey(keyboardOnlyNumPwd);
                    }
                }, 100);
                setKeyboard(keyboardOnlyNumPwd);
                break;
        }
    }

    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

        @Override
        public void onPress(int primaryCode) {
//            List<Keyboard.Key> keys = keyboardOnlyNumPwd.getKeys();
//            for(int i = 0 ; i < keys.size(); i++){
//                Keyboard.Key key = keys.get(i);
////                key.
//                new FancyShowCaseView.Builder(mActivity)
//                        .focusOn()
//                        .title("Focus on View")
//                        .build()
//                        .show();
//            }
        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = mEditText.getText();
            int start = mEditText.getSelectionStart();
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE://go back
                    if (editable != null && editable.length() > 0) {
                        if (start > 0) {
                            editable.delete(start - 1, start);
                        }
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT://switch uppercase or lowercase
                    changeKey();
                    setKeyBoardType(KEYBOARDTYPE_ABC);
                    break;
                case Keyboard.KEYCODE_CANCEL:// hide
                case Keyboard.KEYCODE_DONE:// confirm
                    mWindow.dismiss();
                    break;
                case 123123://switch number keyboard
                    if (isPwd) {
                        setKeyBoardType(KEYBOARDTYPE_Num_Pwd);
                    } else {
                        setKeyBoardType(KEYBOARDTYPE_Num);
                    }
                    break;
                case 456456://switch letter keyboard
                    if (isSupper)//if the current keyboard is uppercase, change to lowercase
                    {
                        changeKey();
                    }
                    setKeyBoardType(KEYBOARDTYPE_ABC);
                    break;
                case 789789://switch symbol keyboard
                    setKeyBoardType(KEYBOARDTYPE_Symbol);
                    break;
                case 666666:// name Delimiter"·"
                    editable.insert(start, "·");
                    break;
                default://input symbol
                    editable.insert(start, "*");
//                    editable.insert(start, Character.toString((char) primaryCode));
            }
        }

        @Override
        public void onText(CharSequence text) {

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
     * switch keyboard uppercase or lowercase
     */
    private void changeKey() {
        List<Keyboard.Key> keylist = keyboardABC.getKeys();
        if (isSupper) {// switch uppercase to lowercase
            for (Keyboard.Key key : keylist) {
                if (key.label != null && strLetter.contains(key.label.toString().toLowerCase())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {// Switch lowercase to uppercase
            for (Keyboard.Key key : keylist) {
                if (key.label != null && strLetter.contains(key.label.toString().toLowerCase())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
        isSupper = !isSupper;
    }

    public static KeyBoardNumInterface keyBoardNumInterface;

    /**
     * random number keyboard
     * code 48-57 (0-9)
     */
    public void randomKey(Keyboard pLatinKeyboard) {
        String keyBoardValue = getKeyBoardLocation(pLatinKeyboard);
        System.out.println("pinpad keyboard location is: "+keyBoardValue);
        keyBoardNumInterface.getNumberValue(keyBoardValue);
    }

    private String getKeyBoardLocation(Keyboard pLatinKeyboard){
        List<Integer> keyboardValues;
        if(dataList.isEmpty()){
            keyboardValues = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0x0D, 0, 0x0E, 0x0F);
        }else {
            keyboardValues = new ArrayList<>();
            for (String hex : dataList) {
                keyboardValues.add(Integer.parseInt(hex, 16));
            }
        }

        List<Keyboard.Key> keyList = pLatinKeyboard.getKeys();
//        int screenBottomY = mHeightPixels - pLatinKeyboard.getHeight();

        int[] keyboardViewLocation = new int[2];
        this.getLocationOnScreen(keyboardViewLocation);
        int keyboardViewX = keyboardViewLocation[0];
        int keyboardViewY = keyboardViewLocation[1];

//        System.out.println("屏幕高: "+screenBottomY+","+mHeightPixels +" 键盘高度="+pLatinKeyboard.getHeight());
//        System.out.println("KeyboardView屏幕位置: x=" + keyboardViewX + ", y=" + keyboardViewY);
//        System.out.println("屏幕高度: " + mHeightPixels);
        Map<Integer, KeyPositionInfo> codeToPositionMap = new HashMap<>();

        for (Keyboard.Key key : keyList) {
            int originalCode = key.codes[0];
            int x = keyboardViewX + key.x;
            int y = keyboardViewY + key.y;
//            int y = screenBottomY + key.y;
//            int x = key.x;
            int right = x + key.width;
            int bottom = y + key.height;
            System.out.println("当前y高度: "+key.y+",x "+key.x +"  键值宽高度"+key.width+ ","+key.height);
            KeyPositionInfo info = new KeyPositionInfo(
                    originalCode,
                    x, y, right, bottom,
                    key
            );
            codeToPositionMap.put(originalCode, info);
        }
        List<Integer> randomNumbers = new ArrayList<>();
        if (dataList.isEmpty()) {
            randomNumbers.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0));
        } else {
            for (String hex : dataList) {
                int value = Integer.parseInt(hex, 16);
                if (value >= 0 && value <= 9) {
                    randomNumbers.add(value);
                }
            }
        }
        Map<Integer, Integer> displayMapping = new HashMap<>();
        int numberIndex = 0;

        for (Keyboard.Key key : keyList) {
            int originalCode = key.codes[0];

            if (originalCode >= 48 && originalCode <= 57) {
                // number（ASCII）
                if (numberIndex < randomNumbers.size()) {
                    displayMapping.put(originalCode, randomNumbers.get(numberIndex));
                    numberIndex++;
                }
            } else if (originalCode >= 0 && originalCode <= 9) {
                // number integer
                if (numberIndex < randomNumbers.size()) {
                    displayMapping.put(originalCode, randomNumbers.get(numberIndex));
                    numberIndex++;
                }
            }
        }

        StringBuilder result = new StringBuilder();

        for (int keyValue : keyboardValues) {
            int keyCode = findKeyCodeForValue(keyValue, displayMapping, codeToPositionMap);

            KeyPositionInfo positionInfo = codeToPositionMap.get(keyCode);
            if (positionInfo != null) {
                System.out.println("keyValue1 : "+keyValue);
                System.out.println("keyValue2: "+QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(keyValue)));
                System.out.println("左上角x y: "+positionInfo.x+","+positionInfo.y);
                System.out.println("右下角x y: "+positionInfo.right+","+positionInfo.bottom);
                String locationStr =
                        QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(keyValue)) +
                                QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(positionInfo.x)) +
                                QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(positionInfo.y)) +
                                QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(positionInfo.right)) +
                                QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(positionInfo.bottom));

                result.append(locationStr);

                Keyboard.Key key = positionInfo.key;
                if (displayMapping.containsKey(keyCode)) {
                    int displayNumber = displayMapping.get(keyCode);
                    key.label = String.valueOf(displayNumber);
                    key.codes[0] = 48 + displayNumber;
                    // Function key: Keep as is
                }
            }
        }

        return result.toString();
    }

    private int findKeyCodeForValue(int keyValue,
                                    Map<Integer, Integer> displayMapping,
                                    Map<Integer, KeyPositionInfo> codeToPositionMap) {
        if (keyValue >= 0 && keyValue <= 9) {
            for (Map.Entry<Integer, Integer> entry : displayMapping.entrySet()) {
                if (entry.getValue() == keyValue) {
                    return entry.getKey();
                }
            }
        } else {
            if (keyValue == 0x0D) { // 13
                return -3;
            } else if (keyValue == 0x0E) { // 14
                return -5;
            } else if (keyValue == 0x0F) { // 15
                return -4;
            }
        }

        return keyValue;
    }
    private static class KeyPositionInfo {
        int originalCode;
        int x;
        int y;
        int right;
        int bottom;
        Keyboard.Key key;

        KeyPositionInfo(int originalCode, int x, int y, int right, int bottom, Keyboard.Key key) {
            this.originalCode = originalCode;
            this.x = x;
            this.y = y;
            this.right = right;
            this.bottom = bottom;
            this.key = key;
        }
    }

    public static void setKeyBoardListener(KeyBoardNumInterface mkeyBoardNumInterface) {
        keyBoardNumInterface = mkeyBoardNumInterface;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (keyBoardType == KEYBOARDTYPE_Only_Num_Pwd) {//only number keyboard
            List<Keyboard.Key> keys = getKeyboard().getKeys();
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == -5) {//delete button
                    Drawable dr = getContext().getResources().getDrawable(R.drawable
                            .bg_keyboard_clear_button);
                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    dr.draw(canvas);
                    int drawableX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
                    int drawableY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
                    key.icon.setBounds(drawableX, drawableY, drawableX + key.icon
                            .getIntrinsicWidth(), drawableY + key.icon.getIntrinsicHeight());
                    key.icon.draw(canvas);
                    Log.i("test", "drawableX: " + drawableX + " drawableY: " + drawableY);
                }else if (key.codes[0] == -3) {//cancel button
                    Drawable dr = getContext().getResources().getDrawable(R.drawable
                            .bg_keyboard_cancel_btn);
                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    dr.draw(canvas);
                    int drawableX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
                    int drawableY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
                    key.icon.setBounds(drawableX, drawableY, drawableX + key.icon
                            .getIntrinsicWidth(), drawableY + key.icon.getIntrinsicHeight());
                    key.icon.draw(canvas);
                    Log.i("test", "drawableX: " + drawableX + " drawableY: " + drawableY);
                }else if (key.codes[0] == -4) {//confirm button
                    Drawable dr = getContext().getResources().getDrawable(R.drawable
                            .bg_keyboard_confirm_button);
                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    dr.draw(canvas);
                    int drawableX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
                    int drawableY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
                    key.icon.setBounds(drawableX, drawableY, drawableX + key.icon
                            .getIntrinsicWidth(), drawableY + key.icon.getIntrinsicHeight());
                    key.icon.draw(canvas);
                    Log.i("test", "drawableX: " + drawableX + " drawableY: " + drawableY);
                }else if (key.codes[0] == -1){
//                    Drawable dr = getContext().getResources().getDrawable(R.drawable
//                            .keyboard_num_key_bg);
//                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                    dr.draw(canvas);
//                    int drawableX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
//                    int drawableY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
//                    key.icon.setBounds(drawableX, drawableY, drawableX + key.icon
//                            .getIntrinsicWidth(), drawableY + key.icon.getIntrinsicHeight());
//                    key.icon.draw(canvas);
                }

            }
        }
    }
}
