package com.dspread.demoui.activity;

import static com.dspread.demoui.BaseApplication.addActivity;
import static com.dspread.demoui.BaseApplication.finishAllActivities;

import android.content.Context;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;
import com.dspread.demoui.fragment.AboutFragment;
import com.dspread.demoui.fragment.DeviceInfoFragment;
import com.dspread.demoui.fragment.DeviceUpdataFragment;
import com.dspread.demoui.fragment.HomeFragment;
import com.dspread.demoui.fragment.LogsFragment;
import com.dspread.demoui.fragment.MifareCardsFragment;
import com.dspread.demoui.fragment.PrinterHelperFragment;
import com.dspread.demoui.fragment.ScanFragment;
import com.dspread.demoui.fragment.SettingFragment;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.demoui.utils.UpdateAppHelper;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity implements TitleUpdateListener, NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    private SettingFragment settingFragment;
    private HomeFragment homeFragment;
    private DeviceInfoFragment deviceInfoFragment;
    private DeviceUpdataFragment deviceUpdataFragment;
    private AboutFragment aboutFragment;
    private PrinterHelperFragment printerHelperFragment;
    private ScanFragment scanFragment;
    private LogsFragment logsFragment;
    private FragmentTransaction transaction;
    private TextView deviceConnectType;
    private TextView tvAppVersion;
    ExtendedFloatingActionButton floatingActionButton;
    private MenuItem menuItem;
    private MifareCardsFragment mifareCardsFragment;

    SharedPreferencesUtil connectType;
    ActionBarDrawerToggle toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        floatingActionButton = findViewById(R.id.fab);
        View headerView = navigationView.getHeaderView(0);
        deviceConnectType = headerView.findViewById(R.id.device_connect_type);
        tvAppVersion = headerView.findViewById(R.id.tv_appversion);
        menuItem = navigationView.getMenu().findItem(R.id.nav_printer);
        drawerStateChanged();
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        addActivity(this);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


            switchFragment(0);
        floatingActionButton.setOnClickListener(view -> {
            toolbar.setTitle(getString(R.string.show_log));
            switchFragment(5);
            drawerLayout.close();
        });

//        BaseApplication baseApplication = new BaseApplication();
//        baseApplication.onCreate();
//        baseApplication.attachBaseContext(this);
//        BaseApplication.getApplicationInstance = this;

        if (getIntent().getBooleanExtra("showSettingFragment", false)) {
            // 加载 mainFragment
            switchFragment(1);
            drawerLayout.close();
        }

        if (!"D20".equals(deviceModel)) {
            menuItem.setVisible(true);

        } else {
            menuItem.setVisible(false);
        }
//        if ("D20".equals(deviceModel)||"D30".equals(deviceModel)||"D60".equals(deviceModel)){
//            open(QPOSService.CommunicationMode.UART_SERVICE, this);
//        }
        toolbar.setTitle(getString(R.string.menu_payment));
//        switchFragment(0);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                String packageVersionName = UpdateAppHelper.getPackageVersionName(MainActivity.this, "com.dspread.demoui");
                tvAppVersion.setText(getString(R.string.app_version) + packageVersionName);
                drawerStateChanged();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                HideKeyboard(drawerView);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }


    String deviceModel = Build.MODEL;
    String deviceManufacturer = Build.MANUFACTURER;

    public void drawerStateChanged() {
        SharedPreferencesUtil connectType = SharedPreferencesUtil.getInstance(this);
        String conType = (String) connectType.get("conType", "");
        if ("blue".equals(conType)) {
            deviceConnectType.setText(getString(R.string.setting_blu));
//            bluetoothRelaPer();
        } else if ("uart".equals(conType)) {
            deviceConnectType.setText(getString(R.string.setting_uart));
        } else if ("usb".equals(conType)) {
            deviceConnectType.setText(getString(R.string.setting_usb));
        } else if ("Dspread".equals(deviceManufacturer) || "D20".equals(deviceModel) || "D30".equals(deviceModel) || "mp600".equals(deviceModel) || "D60".equals(deviceModel) || "D70".equals(deviceModel)) {
            connectType.put("conType", "uart");
            deviceConnectType.setText(getString(R.string.setting_uart));
        } else {
//            bluetoothRelaPer();
            connectType.put("conType", "blue");
            deviceConnectType.setText(getString(R.string.setting_blu));
        }
    }

    @Override
    public void setFragmentTitle(String value) {
        setTitle(value);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_home:
                toolbar.setTitle(getString(R.string.menu_payment));
                switchFragment(0);
                drawerLayout.close();
                break;
            case R.id.nav_setting:
                toolbar.setTitle(getString(R.string.menu_setting));
                switchFragment(1);
                drawerLayout.close();
                break;
            case R.id.nav_deviceinfo:
                toolbar.setTitle(getString(R.string.device_info));
                switchFragment(2);
                drawerLayout.close();
                break;
            case R.id.nav_deviceupdate:
                toolbar.setTitle(getString(R.string.device_update));
                switchFragment(3);
                drawerLayout.close();
                break;
            case R.id.nav_about:
                toolbar.setTitle(getString(R.string.about));
                switchFragment(4);
                drawerLayout.close();
                break;
            case R.id.nav_log:
                toolbar.setTitle(getString(R.string.show_log));
                switchFragment(5);
                drawerLayout.close();
                break;
            case R.id.nav_printer:
                toolbar.setTitle(getString(R.string.printer));
                switchFragment(6);
                drawerLayout.close();
                break;
            case R.id.nav_scan:
                toolbar.setTitle(getString(R.string.scan));
                switchFragment(7);
                drawerLayout.close();
                break;
            case R.id.nav_mifareCards:
                toolbar.setTitle(getString(R.string.menu_mifareCards));
                switchFragment(8);
                drawerLayout.close();
                break;
            case R.id.nav_exit:
                Mydialog.manualExitDialog(MainActivity.this, getString(R.string.msg_exit), new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCancel() {
                        Mydialog.manualExitDialog.dismiss();
                    }

                    @Override
                    public void onConfirm() {
//                        finish();
                        finishAllActivities();
                        Mydialog.manualExitDialog.dismiss();
                    }
                });
                break;

            default:
                break;
        }
        return true;
    }


    public void switchFragment(int i) {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        hideFragemts();
        switch (i) {
            case 0:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, homeFragment);
                }
                transaction.show(homeFragment);
                break;
            case 1:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, settingFragment);
                }
                transaction.show(settingFragment);
                break;
            case 2:
                if (deviceInfoFragment == null) {
                    deviceInfoFragment = new DeviceInfoFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, deviceInfoFragment);
                }
                transaction.show(deviceInfoFragment);
                break;
            case 3:
                if (deviceUpdataFragment == null) {
                    deviceUpdataFragment = new DeviceUpdataFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, deviceUpdataFragment);
                }
                transaction.show(deviceUpdataFragment);
                break;
            case 4:
                if (aboutFragment == null) {
                    aboutFragment = new AboutFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, aboutFragment);
                }
                transaction.show(aboutFragment);
                break;
            case 5:
                if (logsFragment == null) {
                    logsFragment = new LogsFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, logsFragment);
                }
                transaction.show(logsFragment);

                break;
            case 6:
                if (printerHelperFragment == null) {
                    printerHelperFragment = new PrinterHelperFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, printerHelperFragment);
                }
                transaction.show(printerHelperFragment);
                break;
            case 7:
                if (scanFragment == null) {
                    scanFragment = new ScanFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, scanFragment);
                }
                transaction.show(scanFragment);
                break;
            case 8:
                if (mifareCardsFragment == null) {
                    mifareCardsFragment = new MifareCardsFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, mifareCardsFragment);
                }
                transaction.show(mifareCardsFragment);
                break;
            default:
                break;
        }
        transaction.commit();
    }

    private void hideFragemts() {
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
        if (deviceInfoFragment != null) {
            transaction.hide(deviceInfoFragment);
        }
        if (deviceUpdataFragment != null) {
            transaction.hide(deviceUpdataFragment);
        }
        if (aboutFragment != null) {
            transaction.hide(aboutFragment);
        }
        if (logsFragment != null) {
            transaction.hide(logsFragment);
        }
        if (printerHelperFragment != null) {
            transaction.hide(printerHelperFragment);
        }
        if (scanFragment != null) {
            transaction.hide(scanFragment);
        }
        if (mifareCardsFragment != null) {
            transaction.hide(mifareCardsFragment);
        }

    }

    private static final int BLUETOOTH_CODE = 100;
    private static final int LOCATION_CODE = 101;
    LocationManager lm;//【Location management】

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((BaseApplication) getApplication()).setQposService(null);
//        System.exit(0);
//        finish();
        finishAllActivities();
//        System.exit(0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            toolbar.setTitle(getString(R.string.menu_payment));
            switchFragment(0);
            drawerLayout.close();
            exit();
            return true;
        } else if (homeFragment != null) {
            return homeFragment.onKeyDown(keyCode, event);  // 让 Fragment 处理按键事件
        }
        TRACE.i("main keyode = " + keyCode);

        return super.onKeyDown(keyCode, event);
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
//                    finish();
                    finishAllActivities();
                    Mydialog.manualExitDialog.dismiss();
                }
            });
        }
    }

    public static void HideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

        }
    }


}



