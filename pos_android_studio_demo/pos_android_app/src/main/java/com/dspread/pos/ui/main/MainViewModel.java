package com.dspread.pos.ui.main;

import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.dspread.pos.TitleProviderListener;
import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.ui.setting.connection_settings.ConnectionSettingsFragment;
import com.dspread.pos.ui.transaction.TransactionFragment;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
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

    private WeakReference<MainActivity> activityRef;
    public Fragment currentFragment;
    public HomeFragment homeFragment;
    
    // ViewPager2 and Adapter
    private ViewPager2 viewPager;
    private MainFragmentAdapter mainFragmentAdapter;

    public MainViewModel(@NonNull Application application, MainActivity activity) {
        super(application);
        TRACE.i("main activity init");
        this.activityRef = new WeakReference<>(activity);
        this.activity = activity;
        if ("D70".equals(Build.MODEL)) {
            isD70DisplayScreen.set(true);
        } else {
            isD70DisplayScreen.set(false);
        }
        // Fragment preloading is handled by ViewPager2's offscreenPageLimit
        // preloadFragments();
    }
    
    /**
     * Set ViewPager2 and Adapter
     */
    public void setViewPager(ViewPager2 viewPager, MainFragmentAdapter adapter) {
        this.viewPager = viewPager;
        this.mainFragmentAdapter = adapter;
    }

    // Thread pool for executing preloading operations
    private static final ExecutorService preloadExecutor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    //New Method: Preloading Fragment==========
    private void preloadFragments() {
        // Move preloading operations to sub-thread to avoid affecting UI thread
        preloadExecutor.execute(() -> {
            try {
                Log.d("MainViewModel", "Starting to preload fragments...");
                
                // Preloading HomeFragment
                if (!FragmentCacheManager.getInstance().hasFragment(R.id.nav_home)) {
                    Fragment homeFragment = new HomeFragment();
                    FragmentCacheManager.getInstance().putFragment(R.id.nav_home, homeFragment);
                    Log.d("MainViewModel", "Preloading HomeFragment completed");
                }

                // Preloading TransactionFragment
                if (!FragmentCacheManager.getInstance().hasFragment(R.id.nav_transaction)) {
                    Fragment transactionFragment = new TransactionFragment();
                    FragmentCacheManager.getInstance().putFragment(R.id.nav_transaction, transactionFragment);
                    Log.d("MainViewModel", "Preloading TransactionFragment completed");
                }

                // Preloading ConnectionSettingsFragment
                if (!FragmentCacheManager.getInstance().hasFragment(R.id.nav_setting)) {
                    Fragment settingFragment = new ConnectionSettingsFragment();
                    FragmentCacheManager.getInstance().putFragment(R.id.nav_setting, settingFragment);
                    Log.d("MainViewModel", "Preloading ConnectionSettingsFragment completed");
                }

                // Notify on main thread after preloading is complete
                mainHandler.post(() -> {
                    Log.d("MainViewModel", "All fragments preloading completed");
                });
            } catch (Exception e) {
                Log.e("MainViewModel", "Failed to preload fragments: " + e.getMessage());
            }
        });
    }

    public BindingCommand<View> onDrawerOpenedCommand = new BindingCommand<>(new BindingConsumer<View>() {

        @Override
        public void call(View drawerLayout) {
            changeDrawerLayout.setValue(drawerLayout);
        }
    });

    public BindingCommand<View> onDrawerClosedCommand = new BindingCommand<>(new BindingConsumer<View>() {

        @Override
        public void call(View drawerLayout) {
            changeDrawerLayout.setValue(drawerLayout);
        }
    });

    // command for switch Fragment
    public BindingCommand<Integer> switchFragmentCommand = new BindingCommand<>(integer -> {
        // switch Fragment
        fragmentChangeEvent.setValue(integer); // Here, different fragments can be set according to logic
        handleNavigationItemClick(integer);
    });

    public void closeDrawer() {
        closeDrawerCommand.call(); // Call parameterless method
    }

    public void handleNavigationItemClick(int itemId) {
        MainActivity activity = activityRef.get();
        if (activity == null) return;
        
        // Use ViewPager2 to switch fragments
        if (viewPager != null && mainFragmentAdapter != null) {
            int position = mainFragmentAdapter.getPositionById(itemId);
            viewPager.setCurrentItem(position, false); // false means disable smooth animation to avoid middle fragment flashing issue
            
            // Get corresponding fragment
            Fragment targetFragment = getFragmentByPosition(position);
            
            // set fragment title
            if (targetFragment instanceof TitleProviderListener) {
                activity.setTitle(((TitleProviderListener) targetFragment).getTitle());
            }
        } else {
            // Fallback solution: use original fragment switching method
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
                switchFragment(targetFragment, null);
                // set fragment title
                if (targetFragment instanceof TitleProviderListener) {
                    activity.setTitle(((TitleProviderListener) targetFragment).getTitle());
                }
            }
        }
    }
    
    /**
     * Get corresponding fragment by position
     */
    private Fragment getFragmentByPosition(int position) {
        if (mainFragmentAdapter == null) return null;
        
        switch (position) {
            case MainFragmentAdapter.FRAGMENT_HOME:
                return new HomeFragment();
            case MainFragmentAdapter.FRAGMENT_TRANSACTION:
                return new TransactionFragment();
            case MainFragmentAdapter.FRAGMENT_SETTINGS:
                return new ConnectionSettingsFragment();
            default:
                return null;
        }
    }

    private Fragment createFragment(int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                homeFragment = new HomeFragment();
                return homeFragment;
            case R.id.nav_setting:
                return new ConnectionSettingsFragment();
            case R.id.nav_transaction:
                return new TransactionFragment();
        }

        return null;
    }


    private void switchFragment(Fragment targetFragment, Bundle args) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        //Avoid submitting transactions after state saving
        if (fragmentManager.isStateSaved()) {
            return;
        }


        if (targetFragment.isAdded() && targetFragment.getFragmentManager() != fragmentManager) {
            Log.w("FragmentSwitch", "Fragment already attached to different manager, creating new instance");

            targetFragment = createNewFragmentInstance(targetFragment.getClass(), args);
            if (targetFragment == null) return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        String tag = targetFragment.getClass().getName();

        // Add smooth fragment transition animation
        transaction.setCustomAnimations(
                android.R.anim.fade_in,  // Enter animation
                android.R.anim.fade_out, // Exit animation
                android.R.anim.fade_in,  // Pop enter animation
                android.R.anim.fade_out  // Pop exit animation
        );

        //Set or update parameters
        if (args != null) {
            if (targetFragment.getArguments() == null) {
                targetFragment.setArguments(new Bundle(args));
            } else {
                targetFragment.getArguments().putAll(args);
            }
        }

        // hide Fragment
        if (currentFragment != null && currentFragment != targetFragment && currentFragment.isAdded()) {
            transaction.hide(currentFragment);
            TRACE.d("hide currentFragment");
        }
        //Processing the target fragment
        if (!targetFragment.isAdded()) {
            transaction.add(R.id.nav_host_fragment, targetFragment, tag);
        } else {
            transaction.show(targetFragment);
        }
        // Use commitNowAllowingStateLoss() instead of commitAllowingStateLoss() to ensure immediate animation execution
        transaction.commitNowAllowingStateLoss();
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

    public boolean onKeyDownInHome(int keyCode, KeyEvent event) {
        TRACE.i("noe hoeme = " + homeFragment);
        if (homeFragment != null) {
            return homeFragment.onKeyDown(keyCode, event);
        }
        return false;
    }

    /**
     * Clear resources to avoid memory leaks
     */
    public void clearResources() {
        // Release Fragment references
        currentFragment = null;
        homeFragment = null;
        
        // Release ViewPager2 and Adapter references
        viewPager = null;
        mainFragmentAdapter = null;
        
        // Clear Activity references
        activityRef = null;
        activity = null;
        
        // Close thread pool
        try {
            if (preloadExecutor != null && !preloadExecutor.isShutdown()) {
                preloadExecutor.shutdownNow();
            }
        } catch (Exception e) {
            Log.e("MainViewModel", "Failed to close thread pool: " + e.getMessage());
        }
        
        // Clear Handler messages
        try {
            if (mainHandler != null) {
                mainHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            Log.e("MainViewModel", "Failed to clear Handler messages: " + e.getMessage());
        }
        
        TRACE.i("MainViewModel resources cleared");
    }

}