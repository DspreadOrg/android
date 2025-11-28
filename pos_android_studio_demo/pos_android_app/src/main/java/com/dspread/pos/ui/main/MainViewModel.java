package com.dspread.pos.ui.main;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.ui.ntag.NtagFragment;
import com.dspread.pos.ui.payment.PaymentMethodActivity;
import com.dspread.pos.ui.setting.connection_settings.ConnectionSettingsFragment;
import com.dspread.pos.ui.transaction.SerachKeyboardUtils;
import com.dspread.pos.ui.transaction.TransactionFragment;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import java.lang.ref.WeakReference;
import java.util.List;

import me.goldze.mvvmhabit.base.BaseViewModel;
import me.goldze.mvvmhabit.binding.command.BindingCommand;
import me.goldze.mvvmhabit.binding.command.BindingConsumer;
import me.goldze.mvvmhabit.bus.event.SingleLiveEvent;


public class MainViewModel extends BaseViewModel {
    // SingleLiveEvent
    public SingleLiveEvent<Integer> fragmentChangeEvent = new SingleLiveEvent<>();
    public SingleLiveEvent<View> changeDrawerLayout = new SingleLiveEvent<>();
    public SingleLiveEvent<Void> closeDrawerCommand = new SingleLiveEvent<>();
    public ObservableBoolean isD70DisplayScreen = new ObservableBoolean(false);
    private MainActivity activity;
    public List<Fragment> fragments;

    private WeakReference<MainActivity> activityRef;
    public Fragment currentFragment;
    public HomeFragment homeFragment;

    public MainViewModel(@NonNull Application application, MainActivity activity) {
        super(application);
        TRACE.i("main activity init");
        this.activityRef = new WeakReference<>(activity);
        this.activity = activity;
        if("D70".equals(Build.MODEL)){
            isD70DisplayScreen.set(true);
        }else{
            isD70DisplayScreen.set(false);
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
        TRACE.i("this is switch:"+integer);
        fragmentChangeEvent.setValue(integer); // Here, different fragments can be set according to logic
        handleNavigationItemClick(integer);
    });
    public void closeDrawer() {
        closeDrawerCommand.call(); // 调用无参的方法
    }

    public void handleNavigationItemClick(int itemId) {
        MainActivity activity = activityRef.get();
        SerachKeyboardUtils.hideKeyboard(activity);
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
            switchFragment(targetFragment,null);
            // set fragment title
            if (targetFragment instanceof TitleProviderListener) {
                activity.setTitle(((TitleProviderListener) targetFragment).getTitle());
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
           case R.id.nav_transaction:
               return new TransactionFragment();
            case R.id.nav_NTagcard:
                return new NtagFragment();
//            case R.id.nav_scan:
//                return new ScanFragment();
        }

        return null;
    }


  /*  private void switchFragment(Fragment targetFragment) {
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
    }*/


    private void switchFragment(Fragment targetFragment, Bundle args) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        // 避免在状态保存后提交事务
        if (fragmentManager.isStateSaved()) {
            return;
        }

        // 检查并处理已附加到其他FragmentManager的情况
        if (targetFragment.isAdded() && targetFragment.getFragmentManager() != fragmentManager) {
            Log.w("FragmentSwitch", "Fragment already attached to different manager, creating new instance");
            // 不能使用已附加的实例，需要创建新的
            targetFragment = createNewFragmentInstance(targetFragment.getClass(), args);
            if (targetFragment == null) return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        String tag = targetFragment.getClass().getName();

        // 设置或更新参数
        if (args != null) {
            if (targetFragment.getArguments() == null) {
                targetFragment.setArguments(new Bundle(args));
            } else {
                targetFragment.getArguments().putAll(args);
            }
        }

        // 隐藏当前Fragment
        if (currentFragment != null && currentFragment != targetFragment && currentFragment.isAdded()) {
            transaction.hide(currentFragment);
        }

        // 处理目标Fragment
        if (!targetFragment.isAdded()) {
            transaction.add(R.id.nav_host_fragment, targetFragment, tag);
        } else {
            transaction.show(targetFragment);
        }

        transaction.commitAllowingStateLoss();
        currentFragment = targetFragment;
    }

    private Fragment createNewFragmentInstance(Class<? extends Fragment> fragmentClass, Bundle args) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            if (args != null) {
                fragment.setArguments(new Bundle(args));
            }
            return fragment;
        } catch (Exception e) {
            Log.e("FragmentSwitch", "Create instance failed: " + fragmentClass.getSimpleName(), e);
            return null;
        }
    }
    public boolean onKeyDownInHome(int keyCode, KeyEvent event){
        TRACE.i("noe hoeme = "+homeFragment);
        if (homeFragment != null) {
            return homeFragment.onKeyDown(keyCode, event);
        }
        return false;
    }

}