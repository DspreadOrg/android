package com.dspread.demoui.activity.printer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.print.device.PrintListener;
import com.dspread.print.device.PrinterDevice;
import com.dspread.print.device.PrinterManager;
import com.dspread.print.device.bean.PrintLineStyle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class PrinterStatusActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView ivBackTitle;
    private TextView tvTitle;
    private Button btnGetstatus;
    private Button btnGetDensity;
    private Button btnGetSpeed;
    private Button btnGetTemperature;
    private Button btnGetVoltage;
    private TextView tvPrintStatusInfo;
    private PrinterDevice mPrinter;
    private PrintLineStyle printLineStyle;
    private TextView tvGetDesity;
    private TextView tvGetSpeed;
    private TextView tvGetTemperature;
    private TextView tvGetVoltage;
    private final  int PRINTER_DENSITY=3;
    private final  int PRINTER_SPEED=5;
    private final  int PRINTER_TEMPERATURE=6;
    private final  int PRINTER_VOLTAGE=7;
    private final  int PRINTER_STATUS=8;

    private  final Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PRINTER_DENSITY:
                    tvGetDesity.setText(getString(R.string.get_printer_density)+": "+msg.obj);
                  break;
                case PRINTER_SPEED:
                    tvGetSpeed.setText(getString(R.string.get_printer_speed)+": "+msg.obj);
                  break;
                case PRINTER_TEMPERATURE:
                    tvGetTemperature.setText(getString(R.string.get_printer_temperature)+": "+msg.obj);
                  break;
                case PRINTER_VOLTAGE:
                    tvGetVoltage.setText(getString(R.string.get_printer_voltage)+": "+msg.obj);
                  break;
                case PRINTER_STATUS:
                    tvPrintStatusInfo.setText(getString(R.string.get_printer_status)+": "+msg.obj);
                  break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_printer_status);
        initView();
        PrinterManager instance = PrinterManager.getInstance();
        mPrinter = instance.getPrinter();
        mPrinter.initPrinter(this);
        PrinterListener myPrinterListener = new PrinterListener();
        mPrinter.setPrintListener(myPrinterListener);
        printLineStyle = new PrintLineStyle();
    }

    private void initView() {
        ivBackTitle = findViewById(R.id.iv_back_title);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getString(R.string.get_printer_status));
        btnGetstatus = findViewById(R.id.btn_getstatus);
        btnGetDensity = findViewById(R.id.btn_get_density);
        btnGetSpeed = findViewById(R.id.btn_get_speed);
        btnGetTemperature = findViewById(R.id.btn_get_temperature);
        btnGetVoltage = findViewById(R.id.btn_get_voltage);
        tvPrintStatusInfo = findViewById(R.id.tv_printStatusInfo);
        ivBackTitle.setOnClickListener(this);
        btnGetstatus.setOnClickListener(this);
        btnGetDensity.setOnClickListener(this);
        btnGetSpeed.setOnClickListener(this);
        btnGetTemperature.setOnClickListener(this);
        btnGetVoltage.setOnClickListener(this);
        tvGetDesity = findViewById(R.id.tv_get_desity);
        tvGetSpeed = findViewById(R.id.tv_get_speed);
        tvGetTemperature = findViewById(R.id.tv_get_temperature);
        tvGetVoltage = findViewById(R.id.tv_get_voltage);
        tvGetDesity.setText("");
        tvGetSpeed.setText("");
        tvGetTemperature.setText("");
        tvGetVoltage.setText("");
        tvPrintStatusInfo.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.btn_getstatus:
                try {
                    mPrinter.getPrinterStatus();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_get_density:
                try {
                    mPrinter.getPrinterDensity();

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_get_speed:
                try {
                    mPrinter.getPrinterSpeed();
                } catch (RemoteException e) {
                   e.printStackTrace();
                }
                break;
            case R.id.btn_get_temperature:
                try {
                    mPrinter.getPrinterTemperature();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_get_voltage:
                try {
                    mPrinter.getPrinterVoltage();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    class PrinterListener implements PrintListener {
        @Override
        public void printResult(boolean b, String s, PrinterDevice.ResultType resultType) {
            Log.w("printResult", "boolean b==" + b);
            Log.w("printResult", "String s==" + s);
            Log.w("printResult", "resultType==" + resultType.toString());

            Message msg = new Message();
                msg.what = resultType.getValue();
                msg.obj = s;
                handler.sendMessage(msg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrinter.close();
    }
}