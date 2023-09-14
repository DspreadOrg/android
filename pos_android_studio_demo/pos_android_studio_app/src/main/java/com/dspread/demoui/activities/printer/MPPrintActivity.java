package com.dspread.demoui.activities.printer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dspread.demoui.R;
import com.dspread.demoui.activities.BaseActivity;

public class MPPrintActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        setTitle(getString(R.string.title_print));

    }

    private void initView() {
        findViewById(R.id.print_text).setOnClickListener(this);
        findViewById(R.id.print_qrcode).setOnClickListener(this);
        findViewById(R.id.print_barcode).setOnClickListener(this);
        findViewById(R.id.print_receipt).setOnClickListener(this);
        findViewById(R.id.standard_test).setOnClickListener(this);
        findViewById(R.id.print_bitmap).setOnClickListener(this);
        findViewById(R.id.print_receipt).setOnClickListener(this);
        findViewById(R.id.battery_test).setOnClickListener(this);
        findViewById(R.id.font_test).setOnClickListener(this);
        if (android.os.Build.MODEL.equalsIgnoreCase("D30")) {
            findViewById(R.id.standard_test).setVisibility(View.GONE);
            findViewById(R.id.battery_test).setVisibility(View.GONE);
            findViewById(R.id.font_test).setVisibility(View.GONE);
        }
    }

    @Override
    public void onToolbarLinstener() {
        finish();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mpprint;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent;
        if (id == R.id.print_text) {
            intent = new Intent(MPPrintActivity.this, MPPrintTextActivity.class);
            startActivity(intent);
        } else if (id == R.id.standard_test) {
            intent = new Intent(MPPrintActivity.this, StandardPrintActivity.class);
            startActivity(intent);
        } else if (id == R.id.print_barcode) {
            intent = new Intent(MPPrintActivity.this, MPPrintBarcodeActivity.class);
            startActivity(intent);
        } else if (id == R.id.print_qrcode) {
            intent = new Intent(MPPrintActivity.this, MPPrintQRCodeActivity.class);
            startActivity(intent);
        } else if (id == R.id.print_bitmap) {
            intent = new Intent(MPPrintActivity.this, MPPrintBitmapActivity.class);
            startActivity(intent);
        } else if (id == R.id.print_receipt) {
            intent = new Intent(MPPrintActivity.this, MPPrintTicketActivity.class);
            startActivity(intent);
        } else if (id == R.id.battery_test) {
            intent = new Intent(MPPrintActivity.this, BatteryTestActivity.class);
            startActivity(intent);
        } else if (id == R.id.font_test) {
            intent = new Intent(MPPrintActivity.this, TestFontActivity.class);
            startActivity(intent);
        }

    }
}
