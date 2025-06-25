package com.dspread.pos.ui.main;

import android.content.pm.ActivityInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.dspread.pos.common.enums.POS_TYPE;
import com.dspread.pos.posAPI.CustomQPOSCallback;
import com.dspread.pos.common.manager.QPOSCallbackManager;
import com.dspread.pos.posAPI.POS;
import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.utils.DevUtils;
import com.dspread.pos.utils.Mydialog;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.tencent.upgrade.core.DefaultUpgradeStrategyRequestCallback;
import com.tencent.upgrade.core.UpgradeManager;

import java.util.Hashtable;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;
import me.goldze.mvvmhabit.utils.ToastUtils;

public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements CustomQPOSCallback {
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private TextView tvAppVersion;
    ActionBarDrawerToggle toggle;
    private HomeFragment homeFragment;
    @Override
    public void initParam() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public int initContentView(Bundle bundle) {
        return R.layout.activity_main;
    }

    @Override
    public int initVariableId() {
        return BR.viewModel;
    }

    @Override
    public void initData() {
        super.initData();
        viewModel.handleNavigationItemClick(R.id.nav_home);
        QPOSCallbackManager.getInstance().registerCallback(CustomQPOSCallback.class, this);
//        viewModel = new MainViewModel(getApplication(), this);
//        binding.setVariable(BR.viewModel, viewModel);
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navView;
        toolbar = binding.toolbar;
        View headerView = navigationView.getHeaderView(0);
        tvAppVersion = headerView.findViewById(R.id.tv_appversion);
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        viewModel.openDevice();

        //shiply update app
        UpgradeManager.getInstance().checkUpgrade(false, null, new DefaultUpgradeStrategyRequestCallback());
    }

    @Override
    public MainViewModel initViewModel() {
        MainViewModelFactory factory = new MainViewModelFactory(getApplication(), this);
        return new ViewModelProvider(this, factory).get(MainViewModel.class);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        // observe Fragment have been changed
        viewModel.fragmentChangeEvent.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer fragmentIndex) {
                drawerLayout.close();
            }
        });
        viewModel.changeDrawerLayout.observe(this, new Observer<View>() {
            @Override
            public void onChanged(View drawerView) {
                String packageVersionName = DevUtils.getPackageVersionName(MainActivity.this, "com.dspread.pos_android_app");
                tvAppVersion.setText(getString(R.string.app_version) + packageVersionName);
                checkUpdate();
            }
        });
    }

    private void checkUpdate(){
        UpgradeManager.getInstance().checkUpgrade(true, null, new DefaultUpgradeStrategyRequestCallback());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QPOSCallbackManager.getInstance().unregisterCallback(CustomQPOSCallback.class);
        TRACE.i("main is onDestroy");
        SPUtils.getInstance().put("isConnected",false);
        SPUtils.getInstance().put("device_type", "");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_UP) {
                toolbar.setTitle(getString(R.string.menu_payment));
                drawerLayout.close();
                viewModel.handleNavigationItemClick(R.id.nav_home);
                exit();
            }
            return true;
        }else {
            if (action == KeyEvent.ACTION_UP) {
                TRACE.i("---- = "+viewModel.homeFragment);
                return viewModel.onKeyDownInHome(keyCode,event);
            }

            return super.dispatchKeyEvent(event);
        }
    }


    private static boolean isExit = false;
    Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    private void exit() {
        if (!isExit) {
            isExit = true;
            mHandler.sendEmptyMessageDelayed(0, 1500);
        } else {
            isExit = false;
            Mydialog.manualExitDialog(MainActivity.this, getString(R.string.msg_exit), new Mydialog.OnMyClickListener() {
                @Override
                public void onCancel() {
                    Mydialog.manualExitDialog.dismiss();
                }

                @Override
                public void onConfirm() {
                    finish();
                    Mydialog.manualExitDialog.dismiss();
                }
            });
        }
    }

    @Override
    public void onRequestQposConnected() {
        CustomQPOSCallback.super.onRequestQposConnected();
        SPUtils.getInstance().put("isConnected",true);

        SPUtils.getInstance().put("device_type", POS_TYPE.UART.name());
        if(viewModel.pos != null){
           Hashtable<String, Object> posIdTable = viewModel.pos.syncGetQposId(5);
            String posId = posIdTable.get("posId") == null ? "" : (String) posIdTable.get("posId");
            SPUtils.getInstance().put("posID",posId);
            TRACE.i("posid :" + SPUtils.getInstance().getString("posID"));
//            if(!SPUtils.getInstance().getBoolean("updateKEY",false)){
//                viewModel.updateDukpt();
//                viewModel.updateMKSK();
//                SPUtils.getInstance().put("updateKEY",true);
//            }
//            if(!SPUtils.getInstance().getBoolean("updateEMV",false)){
//                ToastUtils.showShort("is updating emv, pls wait");
//                viewModel.updateDukpt();
//                viewModel.updateMKSK();
//                viewModel.updateEMVConfig(MainActivity.this);
//                SPUtils.getInstance().put("updateEMV",true);
//            }
        }
    }

    @Override
    public void onRequestNoQposDetected() {
        CustomQPOSCallback.super.onRequestNoQposDetected();
        SPUtils.getInstance().put("isConnected",false);
        POS.getInstance().setQPOSService(null);
    }

    @Override
    public void onRequestQposDisconnected() {
        CustomQPOSCallback.super.onRequestQposDisconnected();
        SPUtils.getInstance().put("isConnected",false);
        POS.getInstance().setQPOSService(null);
    }

    @Override
    public void onReturnCustomConfigResult(boolean isSuccess, String result) {
        CustomQPOSCallback.super.onReturnCustomConfigResult(isSuccess, result);
        runOnUiThread(() -> ToastUtils.showShort("update emv is success: "+isSuccess));
    }
}



