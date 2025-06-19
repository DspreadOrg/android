package com.dspread.pos.ui.main;

import android.app.Application;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dspread.pos.TerminalApplication;
import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.ui.base.TitleProvider;
import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.ui.printer.PrinterHelperFragment;
import com.dspread.pos.ui.scan.ScanFragment;
import com.dspread.pos.ui.setting.connection_settings.ConnectionSettingsFragment;
import com.dspread.pos.utils.DeviceUtils;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;
import com.dspread.xpos.QPOSService;

import java.lang.ref.WeakReference;
import java.util.List;

import me.goldze.mvvmhabit.base.BaseApplication;
import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;
import me.goldze.mvvmhabit.utils.SPUtils;


public class MainViewModel extends BaseViewModel {
    // SingleLiveEvent
    public SingleLiveEvent<Integer> fragmentChangeEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<View> changeDrawerLayout = new SingleLiveEvent<>();
    private MainActivity activity;
    public List<Fragment> fragments;

    private WeakReference<MainActivity> activityRef;
    public Fragment currentFragment;
    public HomeFragment homeFragment;
    private TerminalApplication myBaseApplication;
    public QPOSService pos;

    public MainViewModel(@NonNull Application application, MainActivity activity) {
        super(application);
        TRACE.i("main activity init");
        this.activityRef = new WeakReference<>(activity);
        this.activity = activity;
//        initFragments();
        if(myBaseApplication == null){
            myBaseApplication = (TerminalApplication) BaseApplication.getInstance();
        }
    }

    public void openDevice(){
        if(DeviceUtils.isSmartDevices()){
            myBaseApplication.open(QPOSService.CommunicationMode.UART, getApplication());
            pos = TerminalApplication.getQPOSService();
            pos.openUart();
            SPUtils.getInstance().put("isConnectedAutoed",true);
        }
    }

    public BindingCommand<View> onDrawerOpenedCommand = new BindingCommand<>(new BindingConsumer<View>() {

        @Override
        public void call(View drawerLayout) {
            changeDrawerLayout.setValue(drawerLayout);
        }
    });

    public BindingCommand<View> onDrawerClosedCommand  = new BindingCommand<>(new BindingConsumer<View>() {

        @Override
        public void call(View drawerLayout) {
            changeDrawerLayout.setValue(drawerLayout);
        }
    });

    // command for switch Fragment
    public BindingCommand<Integer> switchFragmentCommand = new BindingCommand<>(integer -> {
        // switch Fragment
        TRACE.i("this is switch");
        fragmentChangeEvent.setValue(integer); // Here, different fragments can be set according to logic
        handleNavigationItemClick(integer);
    });

    public void handleNavigationItemClick(int itemId) {
        MainActivity activity = activityRef.get();
        if (activity == null) return;

        Fragment targetFragment;
        // get Fragment from cache
        if (FragmentCacheManager.getInstance().hasFragment(itemId)) {
            targetFragment = FragmentCacheManager.getInstance().getFragment(itemId);
        } else {
            //create new fragment
            targetFragment = createFragment(itemId);
            if (targetFragment != null) {
                FragmentCacheManager.getInstance().putFragment(itemId, targetFragment);
            }
        }

        if (targetFragment != null) {
            switchFragment(targetFragment);
            // set fragment title
            if (targetFragment instanceof TitleProvider) {
                activity.setTitle(((TitleProvider) targetFragment).getTitle());
            }
        }
    }

    private Fragment createFragment(int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                homeFragment = new HomeFragment();
                TRACE.i("homeFragment = "+homeFragment);
                return homeFragment;
            case R.id.nav_setting:
                return new ConnectionSettingsFragment();
            case R.id.nav_printer:
                return new PrinterHelperFragment();
            case R.id.nav_scan:
                return new ScanFragment();
        }

        return null;
    }


    private void switchFragment(Fragment targetFragment) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // hide current Fragment，show target Fragment
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        if (!targetFragment.isAdded()) {
            fragmentTransaction.add(R.id.nav_host_fragment, targetFragment);
        } else {
            fragmentTransaction.show(targetFragment);
        }

        fragmentTransaction.commitAllowingStateLoss();
        currentFragment = targetFragment;
    }

    public boolean onKeyDownInHome(int keyCode, KeyEvent event){
        TRACE.i("noe hoeme = "+homeFragment);
        if (homeFragment != null) {
            return homeFragment.onKeyDown(keyCode, event);
        }
        return false;
    }

}