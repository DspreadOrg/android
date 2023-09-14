package com.dspread.demoui.activities.printer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.action.printerservice.PrintStyle;
import com.dspread.demoui.R;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.view.PrintLine;
import com.dspread.print.device.bean.PrintLineStyle;

import androidx.annotation.NonNull;

public class StandardPrintActivity extends CommonActivity {
    private Spinner mSpGreyLevel, mSpinnerSpAlignment;
    private Spinner mSpSpeedLevel;
    private TextView message, printerSpeed, printerDensity, printerTemp, printerVoltage, printerStatus;
    private EditText etText, etFontsize;
    private boolean isContinuousPrint = false;
    private boolean IsPrintPause = false;
    private boolean iSAutoPaperOut = false;
    CheckBox cbContuningPrint, cbNeedInterval, cbAutoFeedLine;
    private int mPrintcount;
    private int mAutoPaperCount;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int arg1 = msg.arg1;
            if (arg1 == 1) {
                mPrintcount++;
                if (mPrintcount < 10000) {
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
            } else if (arg1 == 2) {
                mAutoPaperCount++;
                if (mAutoPaperCount < 30) {
                    try {
                        mPrinter.printText("");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }
    });
    private PrintLineStyle printLineStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.mp_standard_print));
        isContinuousPrint = false;
        IsPrintPause = false;
        etText = findViewById(R.id.et_text);
        etFontsize = findViewById(R.id.et_font_size);
        //spContentDensity = findViewById(R.id.sp_content_density);
        //spHeightFactor = findViewById(R.id.sp_height_factor);
        //spContentDensity.setSelection(1);
        // spHeightFactor.setSelection(1);
        mSpinnerSpAlignment = findViewById(R.id.sp_alignment);
        mSpGreyLevel = findViewById(R.id.sp_grey_level);
        mSpSpeedLevel = findViewById(R.id.sp_speed_level);
        findViewById(R.id.align_area).setVisibility(View.VISIBLE);
        findViewById(R.id.gray_level).setVisibility(View.VISIBLE);
        findViewById(R.id.content_density_area).setVisibility(View.GONE);
        findViewById(R.id.height_factor_area).setVisibility(View.GONE);
        findViewById(R.id.speed_level_area).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_continuous_print).setVisibility(View.VISIBLE);
        message = findViewById(R.id.message);
        printerSpeed = findViewById(R.id.tv_get_printer_speed);
        printerDensity = findViewById(R.id.tv_get_printer_density);
        printerTemp = findViewById(R.id.tv_get_printer_temperature);
        printerVoltage = findViewById(R.id.tv_get_printer_voltage);
        printerStatus = findViewById(R.id.tv_get_printer_status);
        cbContuningPrint = findViewById(R.id.cb_contuning_print);
        cbNeedInterval = findViewById(R.id.cb_need_interval);
        cbAutoFeedLine = findViewById(R.id.cb_line_feed);
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
        mSpGreyLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                message.setText("");
                String item = (String) parent.getAdapter().getItem(position);
                try {
                    mPrinter.setPrintDensity(Integer.parseInt(item));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mSpSpeedLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                message.setText("");
                String item = (String) parent.getAdapter().getItem(position);
                try {
                    mPrinter.setPrintSpeed(Integer.parseInt(item));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cbAutoFeedLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAutoPaperCount = 0;
                    iSAutoPaperOut = true;
                    try {
                        mPrinter.printText("");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    iSAutoPaperOut = false;
                }
            }
        });


        mSpinnerSpAlignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                printLineStyle = new PrintLineStyle();
                if (item.equals("Left")) {
                    printLineStyle.setAlign(PrintLine.LEFT);
                } else if (item.equals("Right")) {
                    printLineStyle.setAlign(PrintLine.RIGHT);
                } else if (item.equals("Center")) {
                    printLineStyle.setAlign(PrintLine.CENTER);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    @Override
    int getLayoutId() {
        return R.layout.activity_print_standard_test;
    }

    @Override
    public void onToolbarLinstener() {
        finish();
    }

    @Override
    int printTest() throws RemoteException {
        printLineStyle.setFontSize(getFontSize());
        mPrinter.setPrintStyle(printLineStyle);
        mPrinter.printText(getText());
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status, int type) {
        TRACE.d("onPrintFinished:" + isSuccess + "---" + "status:" + status + "----" + "type:" + type);
        if (type == 3) { //get printer density
            printerDensity.setText(isSuccess + ":get printer density:" + status);
        }
        if (type == 5) { //get printer density
            printerSpeed.setText(isSuccess + ":get printer speed:" + status);
        }
        if (type == 6) { //get printer density
            printerTemp.setText(isSuccess + ":get printer temperature:" + status);
        }
        if (type == 7) { //get printer density
            printerVoltage.setText(isSuccess + ":get printer voltage:" + status);
        }
        if (type == 8) { //get printer density
            printerStatus.setText(isSuccess + ":get printer status:" + status);
        }
        if (isSuccess && status.equals("Normal") && type == 1) {
            if (isContinuousPrint) {
                Message obtain = Message.obtain();
                obtain.arg1 = 1;
                mHandler.sendMessage(obtain);
            }
            if (iSAutoPaperOut) {
                Message obtain = Message.obtain();
                obtain.arg1 = 2;
                mHandler.sendMessage(obtain);
            }
        }
    }

    @Override
    void onPrintError(boolean isSuccess, String status, int type) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_print, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.get_print_density) {
            try {
                mPrinter.getPrintDensity();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.get_print_speed) {
            try {
                mPrinter.getPrintSpeed();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.get_print_temperature) {
            try {
                mPrinter.getPrintTemperature();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.get_print_voltage) {
            try {
                mPrinter.getPrintVoltage();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (item.getItemId() == R.id.get_print_status) {
            try {
                mPrinter.getPrinterStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
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
