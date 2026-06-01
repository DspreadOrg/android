package com.dspread.pos.ui.main;

import android.content.pm.ActivityInfo;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.dspread.pos.common.manager.FragmentCacheManager;
import com.dspread.pos.posAPI.POSManager;
import com.dspread.pos.ui.home.HomeFragment;
import com.dspread.pos.ui.transaction.SerachKeyboardUtils;
import com.dspread.pos.utils.DevUtils;
import com.dspread.pos.utils.Mydialog;
import com.dspread.pos.utils.TRACE;
import com.dspread.pos_android_app.BR;
import com.dspread.pos_android_app.R;
import com.dspread.pos_android_app.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.posthog.PostHog;
import com.tencent.upgrade.core.DefaultUpgradeStrategyRequestCallback;
import com.tencent.upgrade.core.UpgradeManager;

import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.base.BaseActivity;
import me.goldze.mvvmhabit.utils.SPUtils;

public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {
    public void setToolbarTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ActionBarDrawerToggle toggle;
    private HomeFragment homeFragment;
    
    // ViewPager2 and Adapter
    private ViewPager2 viewPager;
    private MainFragmentAdapter mainFragmentAdapter;
    private boolean isShowingExitDialog = false;

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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void initData() {
        super.initData();
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navView;
        navigationView.setItemIconTintList(null);
        toolbar = binding.toolbar;
        View headerView = navigationView.getHeaderView(0);
//        tvAppVersion = headerView.findViewById(R.id.tv_appversion);

        ImageView closeImage = headerView.findViewById(R.id.image_black);
        closeImage.setOnClickListener(v -> viewModel.closeDrawer());
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Initialize ViewPager2 and Adapter
        viewPager = binding.navHostFragment;
        mainFragmentAdapter = new MainFragmentAdapter(this);
        viewPager.setAdapter(mainFragmentAdapter);
        viewPager.setOffscreenPageLimit(2); // Preload adjacent 2 fragments
        viewPager.setUserInputEnabled(false); // Disable user swiping because we use navigation menu to switch fragments
        
        // Add page change callback to sync navigation menu selection
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                TRACE.d("ViewPager2 page changed to position: " + position);
                // Update navigation menu selected state based on current page
                updateNavigationMenuSelection(position);
            }
        });
        
        // Pass ViewPager2 to ViewModel
        viewModel.setViewPager(viewPager, mainFragmentAdapter);
        
        // Default show HomeFragment (position 0)
        viewPager.setCurrentItem(MainFragmentAdapter.FRAGMENT_HOME, false);
        // Manually trigger the initial selection update
        updateNavigationMenuSelection(MainFragmentAdapter.FRAGMENT_HOME);
        Map<String, Object> props = new HashMap<>();
        props.put("name", Build.MODEL);
        props.put("login_time", System.currentTimeMillis());

        // 关键：调用 identify
        PostHog.Companion.identify(Build.MODEL, props, null);

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
//                String packageVersionName = DevUtils.getPackageVersionName(MainActivity.this, "com.dspread.pos_android_app");
//                tvAppVersion.setText(getString(R.string.app_version) + packageVersionName);
                SerachKeyboardUtils.hideKeyboard(MainActivity.this);
                checkUpdate();
            }
        });

        viewModel.closeDrawerCommand.observe(this, unused -> {
            drawerLayout.close();
        });

    }

    private void checkUpdate() {
        UpgradeManager.getInstance().checkUpgrade(true, null, new DefaultUpgradeStrategyRequestCallback());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TRACE.i("main is onDestroy");
        
        // Clear ViewModel resources to avoid memory leaks
        if (viewModel != null) {
            viewModel.clearResources();
        }
        
        POSManager.getInstance().close();
        SPUtils.getInstance().put("isConnected", false);
        SPUtils.getInstance().put("device_type", "");
        SPUtils.getInstance().put("bluetoothName", "");
        SPUtils.getInstance().put("bluetoothAddress", "");
        SPUtils.getInstance().put("isSelectUartSuccess", false);
        SPUtils.getInstance().put("isSelectUsbSuccess", false);
        
        // Clear ViewPager2 and Adapter resources
        if (mainFragmentAdapter != null) {
            mainFragmentAdapter = null;
        }
        if (viewPager != null) {
            viewPager = null;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_UP) {
                toolbar.setTitle(getString(R.string.menu_payment));

                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.close();
                    return true;
                }
                        
                // Get current fragment from ViewPager2
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                        
                if (currentFragment instanceof HomeFragment) {
                    // Only exit when current fragment is HomeFragment
                    FragmentCacheManager.getInstance().clearCache();
                    TRACE.d("back run from HomeFragment");
                    exit();
                } else {
                    // Navigate back to HomeFragment if current fragment is not HomeFragment
                    TRACE.d("back run from other fragment, navigating to HomeFragment");
                    viewModel.handleNavigationItemClick(R.id.nav_home);
                }
            }
            return true;
        } else {
            if (action == KeyEvent.ACTION_UP) {
                // Get current fragment from ViewPager2
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                if (currentFragment instanceof HomeFragment) {
                    homeFragment = (HomeFragment) currentFragment;
                    return homeFragment.onKeyDown(keyCode, event);
                }
                return false;
            }

            return super.dispatchKeyEvent(event);
        }
    }

    private void exit() {
        // If the dialog box is already displayed, do not create it again.
        if (isShowingExitDialog && Mydialog.manualExitDialog != null && Mydialog.manualExitDialog.isShowing()) {
            return;
        }

        // First, close any existing old dialog boxes
        if (Mydialog.manualExitDialog != null && Mydialog.manualExitDialog.isShowing()) {
            Mydialog.manualExitDialog.dismiss();
        }

        isShowingExitDialog = true;
        Mydialog.manualExitDialog(MainActivity.this, getString(R.string.msg_exit), new Mydialog.OnMyClickListener() {
            @Override
            public void onCancel() {
                isShowingExitDialog = false;
                if (Mydialog.manualExitDialog != null && Mydialog.manualExitDialog.isShowing()) {
                    Mydialog.manualExitDialog.dismiss();
                }
            }

            @Override
            public void onConfirm() {
                isShowingExitDialog = false;
                if (Mydialog.manualExitDialog != null && Mydialog.manualExitDialog.isShowing()) {
                    Mydialog.manualExitDialog.dismiss();
                }
                finish();
            }
        });
    }
    
    /**
     * Update navigation menu selection and toolbar title based on current page position
     * @param position the current ViewPager2 page position
     */
    public void updateNavigationMenuSelection(int position) {
        if (navigationView == null || mainFragmentAdapter == null) {
            TRACE.w("Navigation view or adapter is null, cannot update selection");
            return;
        }
        
        try {
            // Get corresponding menu item ID from position
            int menuItemId = mainFragmentAdapter.getIdByPosition(position);
            
            // Update navigation menu checked state
            navigationView.setCheckedItem(menuItemId);
            TRACE.d("Navigation menu updated to item: " + menuItemId);
            
            // Update toolbar title based on position
            String title = getTitleByPosition(position);
            if (title != null) {
                setToolbarTitle(title);
                TRACE.d("Toolbar title updated to: " + title);
            } else {
                TRACE.w("Could not get title for position: " + position);
            }
        } catch (Exception e) {
            TRACE.e("Error updating navigation menu selection: " + e.getMessage());
        }
    }
    
    /**
     * Get title by fragment position
     * @param position the fragment position
     * @return the title string, or null if not found
     */
    private String getTitleByPosition(int position) {
        String title;
        switch (position) {
            case MainFragmentAdapter.FRAGMENT_HOME:
                title = getString(R.string.menu_payment); // "Sale"
                //TRACE.d("getTitleByPosition: position=" + position + " -> title='" + title + "'");
                return title;
            case MainFragmentAdapter.FRAGMENT_TRANSACTION:
                title = getString(R.string.transaction); // "Transaction"
               // TRACE.d("getTitleByPosition: position=" + position + " -> title='" + title + "'");
                return title;
            case MainFragmentAdapter.FRAGMENT_SETTINGS:
                title = getString(R.string.menu_setting); // "Setting"
               // TRACE.d("getTitleByPosition: position=" + position + " -> title='" + title + "'");
                return title;
            case MainFragmentAdapter.FRAGMENT_MIFARE:
                title = getString(R.string.menu_mifareCards); // "Mifare Cards"
               // TRACE.d("getTitleByPosition: position=" + position + " -> title='" + title + "'");
                return title;
            default:
                TRACE.w("Unknown position: " + position);
                return null;
        }
    }
}



