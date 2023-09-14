package com.dspread.demoui.activities.printer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.action.printerservice.PrintStyle;
import com.dspread.demoui.R;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.view.PrintLine;
import com.dspread.demoui.view.PrinterLayout;
import com.dspread.demoui.view.TextPrintLine;
import com.dspread.print.device.bean.PrintLineStyle;

import java.text.SimpleDateFormat;

public class BatteryTestActivity extends CommonActivity {
    private static final String TAG = "BatteryTestActivity";
    private SimpleDateFormat mLogDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat mFileNameDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private int mLastBattLevel = 0;
    private int mLastDecadeLevel = 0;
    private boolean mNeedTenMinutesSleep = false;
    private boolean mTestStart = false;
    CheckBox cbContuningPrint, cbNeedInterval, cbAutoFeedLine;
    private EditText etText, etFontsize;
    private int mPrintcount;
    private boolean isContinuousPrint = false;
    private boolean IsPrintPause = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int arg1 = msg.arg1;
            if (arg1 == 1) {
                mPrintcount++;
                if (mPrintcount < 10) {
                    if (IsPrintPause) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        mPrinter.printText(getText());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }
    });


    private final BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_BATTERY_CHANGED");
                if (!mTestStart) {
                    return;
                }
                int currentBattLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int level = intent.getIntExtra("level", 0);//获取当前电量
                int scale = intent.getIntExtra("scale", 0);//获取总电量
                Log.d(TAG, "level " + level + "scale:" + scale);
                Log.d(TAG, "batt level " + currentBattLevel);
                if (currentBattLevel != mLastBattLevel) {
                    //StringBuilder batteryTestLog = new StringBuilder();
                    //batteryTestLog.append(String.format(Locale.getDefault(), "%s %d%%", mLogDateFormat.format(new Date()), currentBattLevel));
                    if ((mLastBattLevel - currentBattLevel > 1) || currentBattLevel > mLastBattLevel) {
                        String errorLog = getString(R.string.batt_level_skip_error, currentBattLevel, mLastBattLevel);
                        //batteryTestLog.append(", ").append(errorLog);
                        Log.d(TAG, errorLog);
                        setMessage(errorLog);
                    }
                    mLastBattLevel = currentBattLevel;
                    if ((mLastDecadeLevel > (currentBattLevel + 10))) {
                        mLastDecadeLevel = currentBattLevel;
                        mNeedTenMinutesSleep = true;
                        String log = getString(R.string.batt_level_test_sleep, 10 * 60 * 1000);
                        setMessage(log);
                        // batteryTestLog.append(", ").append(log);
                        Log.d(TAG, log);
                    }
                    //batteryTestLog.append("\n");
                   /* try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            Files.write(Paths.get(mLogFileName), batteryTestLog.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.mp_battery_test));
        etText = findViewById(R.id.et_text);
        etFontsize = findViewById(R.id.et_font_size);
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        mLastBattLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        Log.d(TAG, "mLastBattLevel:" + mLastBattLevel);
        mLastDecadeLevel = mLastBattLevel;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryBroadcastReceiver, intentFilter);
        findViewById(R.id.align_area).setVisibility(View.INVISIBLE);
        findViewById(R.id.gray_level_area).setVisibility(View.INVISIBLE);
        findViewById(R.id.ll_continuous_print).setVisibility(View.VISIBLE);

        cbContuningPrint = findViewById(R.id.cb_contuning_print);
        cbNeedInterval = findViewById(R.id.cb_need_interval);
        cbAutoFeedLine = findViewById(R.id.cb_line_feed);
        cbAutoFeedLine.setVisibility(View.GONE);
        cbContuningPrint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cbNeedInterval.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                PrintLineStyle printLineStyle = new PrintLineStyle();
                printLineStyle.setFontSize(getFontSize());
                mPrinter.setPrintStyle(printLineStyle);
                if (isChecked) {
                    isContinuousPrint = true;
                    mPrintcount = 0;
                    try {
                        mPrinter.printText(getText());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    isContinuousPrint = false;
                }
                if (!isChecked) {
                    cbNeedInterval.setChecked(false);
                }
            }
        });

        cbNeedInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    IsPrintPause = true;
                } else {
                    IsPrintPause = false;
                }
            }
        });
    }


    @Override
    int getLayoutId() {
        return R.layout.activity_print_battery_test;
    }

    long getDelayPrintTime() {
        if (mNeedTenMinutesSleep) {
            mNeedTenMinutesSleep = false;
            return 10 * 60 * 1000;
        }
        return 500;
    }

    @Override
    void onPrintButtonClick() {
        super.onPrintButtonClick();

    }

    @Override
    void onPrintStart() {
        super.onPrintStart();
        mTestStart = true;
        Log.d(TAG, "mTestStart:" + mTestStart);
        /*@SuppressLint("SdCardPath") String dirPath = "/sdcard/printerdemo/";
        File dir = new File(dirPath);
        dir.mkdirs();
        mLogFileName = dirPath + "batt_log_" + mFileNameDateFormat.format(new Date()) + ".txt";*/
    }

    @Override
    public void onToolbarLinstener() {
        finish();
    }

    @Override
    int printTest() throws RemoteException {
        // printMeituanTicket();
        PrintLineStyle printLineStyle = new PrintLineStyle();
        printLineStyle.setFontSize(getFontSize());
        mPrinter.setPrintStyle(printLineStyle);
        mPrinter.printText(getText());
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status,int type) {
        if (status != null) {
            TRACE.d("sssssss" + status);
        }
        setMessage(isSuccess + ";" + status);
        if (isSuccess) {
            // message.setText(code + ";" + printStateValue);
            if (isContinuousPrint) {
                Message obtain = Message.obtain();
                obtain.arg1 = 1;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.sendMessage(obtain);
                    }
                }, getDelayPrintTime());
            }
        }

    }

    @Override
    void onPrintError(boolean isSuccess, String status,int type) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBatteryBroadcastReceiver);
    }

    private void printMeituanTicket() throws RemoteException {
        PrinterLayout printerLayout = new PrinterLayout(this);
       /* TextPrintLine headTPL = new TextPrintLine();
        headTPL.setBold(true);
        headTPL.setPosition(PrintLine.CENTER);
        headTPL.setContent("Testing");
        headTPL.setSize(14);
        printerLayout.addText(headTPL);


        TextPrintLine headTwo = new TextPrintLine();
        headTwo.setBold(true);
        headTwo.setPosition(PrintLine.CENTER);
        headTwo.setContent("POS Signing of purchase orders");
        headTwo.setSize(14);
        printerLayout.addText(headTwo);

        TextPrintLine headThree = new TextPrintLine();
        headThree.setBold(true);
        headThree.setPosition(PrintLine.CENTER);
        headThree.setContent("MERCHANT COPY");
        headThree.setSize(14);
        printerLayout.addText(headThree);


        TextPrintLine toolLineTOP = new TextPrintLine();
        toolLineTOP.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTOP);


        TextPrintLine contextOne = new TextPrintLine();
        contextOne.setBold(false);
        contextOne.setPosition(PrintLine.LEFT);
        contextOne.setContent("ISSUER Agricultural Bank of China");
        contextOne.setSize(14);
        printerLayout.addText(contextOne);


        TextPrintLine contextTwo = new TextPrintLine();
        contextTwo.setBold(false);
        contextTwo.setPosition(PrintLine.LEFT);
        contextTwo.setContent("ACQ 48873110");
        contextTwo.setSize(14);
        printerLayout.addText(contextTwo);


        TextPrintLine contextThree = new TextPrintLine();
        contextThree.setBold(false);
        contextThree.setPosition(PrintLine.LEFT);
        contextThree.setContent("CARD number.");
        contextThree.setSize(14);
        printerLayout.addText(contextThree);


        TextPrintLine contextFour = new TextPrintLine();
        contextFour.setBold(true);
        contextFour.setPosition(PrintLine.LEFT);
        contextFour.setContent("6228 48******8 116 S");
        contextFour.setSize(14);
        printerLayout.addText(contextFour);


        TextPrintLine contextFive = new TextPrintLine();
        contextFive.setBold(false);
        contextFive.setPosition(PrintLine.LEFT);
        contextFive.setContent("TYPE of transaction(TXN TYPE)");
        contextFive.setSize(14);
        printerLayout.addText(contextFive);


        TextPrintLine contextSix = new TextPrintLine();
        contextSix.setBold(false);
        contextSix.setPosition(PrintLine.LEFT);
        contextSix.setContent("SALE");
        contextSix.setSize(14);
        printerLayout.addText(contextSix);

        TextPrintLine toolLineTPL2 = new TextPrintLine();
        toolLineTPL2.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTPL2);

        MidTextPrintLine titleMTPL = new MidTextPrintLine(this);
        titleMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        titleMTPL.getLeftTextView().setTextSize(10);
        titleMTPL.getLeftTextView().setText("Item/NO");
        titleMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        titleMTPL.getMidTextView().setTextSize(10);
        titleMTPL.getMidTextView().setText("Quantity");
        titleMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        titleMTPL.getRightTextView().setTextSize(10);
        titleMTPL.getRightTextView().setText("Amount");
        printerLayout.addView(titleMTPL);

        MidTextPrintLine eighthMTPL = new MidTextPrintLine(this);
        eighthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        eighthMTPL.getLeftTextView().setTextSize(10);
        eighthMTPL.getLeftTextView().setText("008 Churrasco");
        eighthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        eighthMTPL.getMidTextView().setTextSize(10);
        eighthMTPL.getMidTextView().setText("1");
        eighthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        eighthMTPL.getRightTextView().setTextSize(10);
        eighthMTPL.getRightTextView().setText("117.56");
        printerLayout.addView(eighthMTPL);

        MidTextPrintLine ninthMTPL = new MidTextPrintLine(this);
        ninthMTPL.getLeftTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ninthMTPL.getLeftTextView().setTextSize(10);
        ninthMTPL.getLeftTextView().setText("009 Rost Goose");
        ninthMTPL.getMidTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ninthMTPL.getMidTextView().setTextSize(10);
        ninthMTPL.getMidTextView().setText("2");
        ninthMTPL.getRightTextView().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ninthMTPL.getRightTextView().setTextSize(10);
        ninthMTPL.getRightTextView().setText("7.56");
        printerLayout.addView(ninthMTPL);

        TextPrintLine toolLineTPL3 = new TextPrintLine();
        toolLineTPL3.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTPL3);

        TextPrintLine toolLineTPL4 = new TextPrintLine();
        toolLineTPL4.setContent("RMB:249.00");
        toolLineTPL4.setPosition(PrintLine.CENTER);
        printerLayout.addText(toolLineTPL4);

        TextPrintLine toolLineTPL5 = new TextPrintLine();
        toolLineTPL5.setContent("- - - - - - - - - - - - - -");
        printerLayout.addText(toolLineTPL5);

        TextPrintLine toolLineTPL6 = new TextPrintLine();
        toolLineTPL6.setContent("Please scan the QRCode for getting more information: ");
        toolLineTPL6.setSize(10);
        printerLayout.addText(toolLineTPL6);

        Bitmap barCodeBM = QRCodeUtil.getBarCodeBM("123456", 300, 100);
        BitmapPrintLine bitmapPrintLine1 = new BitmapPrintLine(barCodeBM, PrintLine.CENTER);
        printerLayout.addBitmap(bitmapPrintLine1);

        TextPrintLine toolLineTPL7 = new TextPrintLine();
        toolLineTPL7.setContent("Please scan the BarCode for getting more information: ");
        toolLineTPL7.setSize(10);
        printerLayout.addText(toolLineTPL7);

        Bitmap qrcodeBM = QRCodeUtil.getQrcodeBM("123456", 200);
        BitmapPrintLine bitmapPrintLine2 = new BitmapPrintLine(qrcodeBM, PrintLine.CENTER);
        printerLayout.addBitmap(bitmapPrintLine2);*/

        TextPrintLine textPrintLine = new TextPrintLine();
        textPrintLine.setContent("");
        textPrintLine.setSize(10);
        textPrintLine.setBold(true);
        textPrintLine.setPosition(PrintLine.CENTER);
        printerLayout.addText(textPrintLine);
        Bitmap bitmap = printerLayout.viewToBitmap();
        mPrinter.printBitmap(this,bitmap);


    }


    private String getText() {
        if (etText.getText() != null) {
            return etText.getText().toString();
        } else {
            return "";
        }
    }

    private int getFontSize() {
        if (etFontsize.getText() != null) {
            return Integer.parseInt(etFontsize.getText().toString());
        } else {
            return 14;
        }
    }
}
