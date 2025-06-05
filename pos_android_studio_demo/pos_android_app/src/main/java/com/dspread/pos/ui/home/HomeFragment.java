package com.dspread.pos.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.dspread.pos.common.base.BaseFragment;
import com.dspread.pos.posAPI.POSCommand;
import com.dspread.pos.ui.base.TitleProvider;
import com.dspread.pos.ui.payment.PaymentActivity;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.FragmentHomeBinding;

import me.goldze.mvvmhabit.utils.ToastUtils;


public class HomeFragment extends BaseFragment<FragmentHomeBinding, HomeViewModel> implements TitleProvider {
//    private KeyboardUtil keyboardUtil;
    private boolean canshow = true;
    private CountDownTimer showTimer;
    
    @Override
    public int initContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return R.layout.fragment_home;
    }
    
    @Override
    public int initVariableId() {
        return BR.viewModel;
    }
    
    @Override
    public void initData() {
        getActivity().setTitle(getString(R.string.menu_payment));
        initTimer();
    }
    
    private void initTimer() {
        showTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                canshow = true;
            }
        };
    }
    
    @Override
    public void initViewObservable() {
        viewModel.paymentStartEvent.observe(this, inputMoney -> {
            if (!canshow) return;
            if(POSCommand.getInstance().getQPOSService() == null){
                ToastUtils.showShort(getString(R.string.connect_warnning));
                return;
            }
            canshow = false;
            showTimer.start();
            Intent intent = new Intent(getActivity(), PaymentActivity.class);
            intent.putExtra("amount", String.valueOf(inputMoney));
            startActivity(intent);
            // 获取系统标准跳转动画时间
            int animTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            getView().postOnAnimationDelayed(() -> {
                viewModel.clearAmount();
                TRACE.i("clear the amount");
            }, animTime + 500);
        });
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        TRACE.i("home on keydown = "+keyCode);
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_0:
                    viewModel.onNumberClick("0");
                    return true;
                case KeyEvent.KEYCODE_1:
                    viewModel.onNumberClick("1");
                    return true;
                case KeyEvent.KEYCODE_2:
                    viewModel.onNumberClick("2");
                    return true;
                case KeyEvent.KEYCODE_3:
                    viewModel.onNumberClick("3");
                    return true;
                case KeyEvent.KEYCODE_4:
                    viewModel.onNumberClick("4");
                    return true;
                case KeyEvent.KEYCODE_5:
                    viewModel.onNumberClick("5");
                    return true;
                case KeyEvent.KEYCODE_6:
                    viewModel.onNumberClick("6");
                    return true;
                case KeyEvent.KEYCODE_7:
                    viewModel.onNumberClick("7");
                    return true;
                case KeyEvent.KEYCODE_8:
                    viewModel.onNumberClick("8");
                    return true;
                case KeyEvent.KEYCODE_9:
                    viewModel.onNumberClick("9");
                    return true;
                case KeyEvent.KEYCODE_DEL:
                    viewModel.onClearClickCommand();
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                    viewModel.onConfirmClickCommand.execute();
                    return true;
            }
        }
        return true;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (showTimer != null) {
            showTimer.cancel();
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public String getTitle() {
        return "Payment";
    }
}

