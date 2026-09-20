package com.dspread.pos.ui.payment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dspread.pos.dualScreen.manager.ViceScreenManager;
import com.dspread.pos.dualScreen.view.PaymentQRPromptView;
import com.dspread.pos.utils.DeviceModelUtils;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityPaymentMetholdBinding;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class PaymentMethodActivity extends BaseActivity<ActivityPaymentMetholdBinding, PaymentMethodViewModel> {
    private String amount;
    private String deviceAddress;

    private String pkg;
    private String cls;
    private boolean canshow = true;
    private ActivityResultLauncher<Intent> scanLauncher;
    private int currentMethodIndex = 0; //
    private com.dspread.pos.view.PaymentMethodsLayout paymentMethodsLayout;
    private boolean isProcessingPayment = false; // Prevent duplicate clicks flag

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_payment_methold;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        amount = getIntent().getStringExtra("amount");
        deviceAddress = getIntent().getStringExtra("deviceAddress");
        binding.setVariable(BR.viewModel, viewModel);
        binding.paymentMethodsLayout.setViewModel(viewModel);
        paymentMethodsLayout = binding.paymentMethodsLayout;
        // Initialize with first payment method selected
        paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
        viewModel.getSelectedPaymentMethod().observe(this, methodIndex -> {
            if (methodIndex != null) {
                handlePaymentMethodSelection(methodIndex);
            }
        });

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int widthPx = displayMetrics.widthPixels;
        int heightPx = displayMetrics.heightPixels;

        if (widthPx <= 320 && heightPx <= 240) {
            viewModel.isSmallScreen.set(true);
            viewModel.isNormalScreen.set(false);
        } else {
            viewModel.isNormalScreen.set(true);
            viewModel.isSmallScreen.set(false);
        }
        viewModel.setTotalAmount("$" + DeviceUtils.convertAmountToCents(amount));
        scanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    isProcessingPayment = false;
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String scanData = result.getData().getStringExtra("data");
                        scanData = amount;
                        gotoPaymentstatusActivity(scanData);
                        finish();
                    } else {
                        showVicPaymentMothed();
                    }
                }
        );

        showVicPaymentMothed();


    }

    private void showVicPaymentMothed() {
        if(DeviceModelUtils.isD80()){
            ViceScreenManager viceScreenManager = ViceScreenManager.getInstance(this);
            if(viceScreenManager.getPowerOnStatus() == 1){
                // 创建副屏显示视图:第一行金额,第二行 Card / Scan Code
                LinearLayout viceRoot = new LinearLayout(this);
                viceRoot.setOrientation(LinearLayout.VERTICAL);
                viceRoot.setGravity(Gravity.CENTER);
                viceRoot.setBackgroundColor(Color.WHITE);
                viceRoot.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));

                // 第一行:amount 标题 + 总金额(同一行显示)
                LinearLayout amountRow = new LinearLayout(this);
                amountRow.setOrientation(LinearLayout.HORIZONTAL);
                amountRow.setGravity(Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams amountRowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                amountRowLp.bottomMargin = dp(2);
                viceRoot.addView(amountRow, amountRowLp);

                TextView tvTotalTitle = new TextView(this);
                tvTotalTitle.setText("Total: ");
                tvTotalTitle.setTextColor(Color.parseColor("#ff030303"));
                tvTotalTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                tvTotalTitle.setTypeface(Typeface.DEFAULT_BOLD);
                amountRow.addView(tvTotalTitle);

                TextView tvViceAmount = new TextView(this);
                tvViceAmount.setText(viewModel.totalAmount.get());
                tvViceAmount.setTextColor(Color.parseColor("#ff030303"));
                tvViceAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                tvViceAmount.setTypeface(Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams viceAmountLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                viceAmountLp.leftMargin = dp(2);
                amountRow.addView(tvViceAmount, viceAmountLp);

                // 第二行:Card / Scan Code(宽度均分,保证按钮文字不被裁剪)
                LinearLayout methodRow = new LinearLayout(this);
                methodRow.setOrientation(LinearLayout.HORIZONTAL);
                methodRow.setGravity(Gravity.CENTER);
                methodRow.setPadding(dp(6), 0, dp(6), 0);
                LinearLayout.LayoutParams methodRowLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                methodRowLp.topMargin = dp(2);
                viceRoot.addView(methodRow, methodRowLp);

                // Card 点击事件
                methodRow.addView(createMethodItem(R.mipmap.ic_salemethod_card, "Card", v -> {
                    currentMethodIndex = 0;
                    paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
                    handlePaymentMethodSelection(0);
                }));

                // Scan Code 点击事件
                methodRow.addView(createMethodItem(R.mipmap.ic_salemethod_scan, "Scan Code", v -> {
                    currentMethodIndex = 1;
                    paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
                    handlePaymentMethodSelection(1);
                }));

                // show
                viceScreenManager.show(viceRoot);
            }
        }
    }


    /**
     * 创建副屏支付方式选项(图标 + 文字,带点击事件)
     */
    private LinearLayout createMethodItem(int iconRes, String label, View.OnClickListener listener) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setClickable(true);
        item.setFocusable(true);
        item.setPadding(dp(6), dp(6), dp(6), dp(6));
        item.setBackground(createMethodItemBackground());
        // 宽度采用 0dp + weight=1 均分副屏可用宽度,避免固定宽度超屏导致下方文字被裁切
        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        itemLp.setMargins(dp(6), 0, dp(6), 0);
        item.setLayoutParams(itemLp);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        item.addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextColor(Color.parseColor("#ff030303"));
        labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        labelTv.setGravity(Gravity.CENTER);
        labelTv.setSingleLine(true);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        labelLp.topMargin = dp(4);
        item.addView(labelTv, labelLp);

        item.setOnClickListener(listener);
        return item;
    }

    /**
     * 创建副屏支付方式选项的圆角边框背景(按压态变色)
     */
    private StateListDrawable createMethodItemBackground() {
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setShape(GradientDrawable.RECTANGLE);
        normalDrawable.setCornerRadius(dp(2));
        normalDrawable.setStroke(dp(1), Color.parseColor("#BCBCBC"));
        normalDrawable.setColor(Color.WHITE);

        GradientDrawable pressedDrawable = new GradientDrawable();
        pressedDrawable.setShape(GradientDrawable.RECTANGLE);
        pressedDrawable.setCornerRadius(dp(2));
        pressedDrawable.setStroke(dp(1), Color.parseColor("#ffe47579"));
        pressedDrawable.setColor(Color.parseColor("#ffffe9e9"));

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        states.addState(new int[]{}, normalDrawable);
        return states;
    }

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }

    private void handlePaymentMethodSelection(int methodIndex) {
        // Prevent duplicate clicks flag
        if (isProcessingPayment) {
            return;
        }
        isProcessingPayment = true;

        switch (methodIndex) {
            case 0:
                navigateToCardPayment();
                break;
            case 1:
                startScanCodePayment();

                break;
            case 2:
                startGeneratePayment();
                break;
            case 3:
                navigateCashPayment();
                break;
        }
    }

    private void navigateToCardPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("amount", amount);
        intent.putExtra("deviceAddress", deviceAddress);
        startActivity(intent);
        finish();
    }

    private void startScanCodePayment() {
        // 启动扫码支付
       /* Intent intent = new Intent(this, ScanCodeActivity.class);
        intent.putExtra("amount", amount);
        startActivity(intent);*/
        TRACE.d("PayMethodActivity startScanCodePayment");

        if(DeviceModelUtils.isD80()){
            ViceScreenManager viceScreenManager = ViceScreenManager.getInstance(this);
            if(viceScreenManager.getPowerOnStatus() == 1){
                // 副屏显示扫码支付提示:左侧金额卡片,右侧扫码图标与提示文字
                PaymentQRPromptView promptView = new PaymentQRPromptView(this);
                promptView.setAmount(viewModel.totalAmount.get());
                viceScreenManager.show(promptView);
            }
        }

        initScanCode();
    }

    private void startGeneratePayment() {
        // 启动生成支付码
        Intent intent = new Intent(this, PaymentGenerateActivity.class);
        intent.putExtra("amount", amount);
        startActivity(intent);
        finish();
        TRACE.d("PayMethodActivity startGeneratePayment");
    }

    private void navigateCashPayment() {
        navigateToCardPayment();
        TRACE.d("PayMethodActivity startCashPayment");
    }

    private void initScanCode() {
        if (DeviceUtils.isAppInstalled(getApplicationContext(), DeviceUtils.UART_AIDL_SERVICE_APP_PACKAGE_NAME)) {
            //D30MstartScan();
            pkg = "com.dspread.sdkservice";
            cls = "com.dspread.sdkservice.base.scan.ScanActivity";
        } else {
            if (!canshow) {
                return;
            }
            canshow = false;
            showTimer.start();
            pkg = "com.dspread.components.scan.service";
            cls = "com.dspread.components.scan.service.ScanActivity";
        }
        Intent intentScanCode = new Intent();
        ComponentName comp = new ComponentName(pkg, cls);
        try {
            intentScanCode.putExtra("amount", "CHARGE ￥1");
            intentScanCode.setComponent(comp);
            scanLauncher.launch(intentScanCode);
        } catch (ActivityNotFoundException e) {
            Log.w("e", "e==" + e);
            //viewModel.onScanResult(getString(R.string.scan_toast));
            ToastUtils.showShort(getString(R.string.scan_toast));
        }
    }

    private CountDownTimer showTimer = new CountDownTimer(800, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            canshow = true;
        }

    };

    private void gotoPaymentstatusActivity(String scanData) {
        Intent intent = new Intent(PaymentMethodActivity.this, PaymentStatusActivity.class);
        intent.putExtra("amount", scanData);
        startActivity(intent);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_UP) {
                finish();
            }
            return true;
        } else {
            if (action == KeyEvent.ACTION_UP) {
                TRACE.i("payment method on keydown = " + keyCode);
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        currentMethodIndex = (currentMethodIndex - 1 + 4) % 4;
                        TRACE.i("Selected payment method: " + currentMethodIndex);
                        // Update UI to show selected payment method
                        if (paymentMethodsLayout != null) {
                            paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        currentMethodIndex = (currentMethodIndex + 1) % 4;
                        TRACE.i("Selected payment method: " + currentMethodIndex);
                        // Update UI to show selected payment method
                        if (paymentMethodsLayout != null) {
                            paymentMethodsLayout.setSelectedPaymentMethod(currentMethodIndex);
                        }
                        return true;
                    case KeyEvent.KEYCODE_ENTER:
                        handlePaymentMethodSelection(currentMethodIndex);
                        return true;
                }
                return false;
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
