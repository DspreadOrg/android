package com.dspread.demoui.widget.pinpad.keyboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.dspread.demoui.R;
import com.dspread.demoui.utils.QPOSUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                if (keyboardABC == null) {
                    keyboardABC = new Keyboard(getContext(), R.xml.keyboard_abc);
                }
                setKeyboard(keyboardABC);
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
                    keyboardOnlyNumPwd = new Keyboard(getContext(), R.xml.keyboard_only_number);
                }
                randomKey(keyboardOnlyNumPwd);
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
        int[] ayRandomKey = new int[13];
        if(dataList.size() == 0){
            ayRandomKey = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, -3, 0, -4, -5};
//            Random random = new Random();
//            for (int i = 0; i < ayRandomKey.length; i++) {
//                int a = random.nextInt(ayRandomKey.length);
//                int temp = ayRandomKey[i];
//                ayRandomKey[i] = ayRandomKey[a];
//                ayRandomKey[a] = temp;
//            }
        }else {
            for (int i = 0; i < dataList.size(); i++) {
                ayRandomKey[i] = Integer.valueOf(dataList.get(i), 16);
            }
        }

        List<Keyboard.Key> pKeyLis = pLatinKeyboard.getKeys();
        int index = 0;
        int sy = mHeightPixels - pLatinKeyboard.getHeight();
//        int sy = mHeightPixels-80*5-8*4;//D20 is 60 and 6，D1000 is 80 and 8
//        Tip.i("sy = "+sy);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < pKeyLis.size(); i++) {
//            if(i == 0){
//                sy = mHeightPixels-pKeyLis.get(i).height*5-pKeyLis.get(i).x*6;//calculate interval value
//            }
            int code = pKeyLis.get(i).codes[0];
            int y = sy + pKeyLis.get(i).y;
            int x = pKeyLis.get(i).x;
            int rit = x + pKeyLis.get(i).width;
            int riby = y + pKeyLis.get(i).height;
            String label;
            if (code >= 0) {//number value
                pKeyLis.get(i).label = ayRandomKey[index] + "";
                pKeyLis.get(i).codes[0] = 48 + ayRandomKey[index];
                String locationStr = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(ayRandomKey[index])) + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(x)) + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(y))
                        + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(rit)) + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(riby));
                s.append(locationStr);
                index++;
            } else {
                if (code == -3) {
                    label = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(13));
                } else if (code == -4) {
                    label = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(15));
                } else {
                    label = QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(14));
                }
                String locationStr = label + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(x)) + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(y))
                        + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(rit)) + QPOSUtil.byteArray2Hex(QPOSUtil.intToByteArray(riby));
                s.append(locationStr);
            }
        }
        keyBoardNumInterface.getNumberValue(s.toString());
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
                            .keyboard_white);
                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    dr.draw(canvas);
                    int drawableX = key.x + (key.width - key.icon.getIntrinsicWidth()) / 2;
                    int drawableY = key.y + (key.height - key.icon.getIntrinsicHeight()) / 2;
                    key.icon.setBounds(drawableX, drawableY, drawableX + key.icon
                            .getIntrinsicWidth(), drawableY + key.icon.getIntrinsicHeight());
                    key.icon.draw(canvas);
                    Log.i("test", "drawableX: " + drawableX + " drawableY: " + drawableY);
                }
//                Log.i("test","x: " +key.x+" y: "+key.y+" wi:"+key.width+" he:"+key.height);

            }
        }
    }
}
