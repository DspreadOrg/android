package com.dspread.demoui.activity;

import static com.dspread.demoui.activity.BaseApplication.getApplicationInstance;
import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.dspread.demoui.utils.Utils.open;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dspread.demoui.R;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.ui.fragment.AboutFragment;
import com.dspread.demoui.ui.fragment.AutoFragment;
import com.dspread.demoui.ui.fragment.DeviceInfoFragment;
import com.dspread.demoui.ui.fragment.DeviceUpdataFragment;
import com.dspread.demoui.ui.fragment.HomeFragment;
import com.dspread.demoui.ui.fragment.LogsFragment;
import com.dspread.demoui.ui.fragment.PrinterHelperFragment;
import com.dspread.demoui.ui.fragment.ScanFragment;
import com.dspread.demoui.ui.fragment.SettingFragment;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.demoui.utils.UpdateAppHelper;
import com.dspread.xpos.QPOSService;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;

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
    private AutoFragment autoFragment;
    SharedPreferencesUtil connectType;

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
        floatingActionButton.setOnClickListener(view -> {
            toolbar.setTitle(getString(R.string.show_log));
            switchFragment(5);
            drawerLayout.close();
        });
        BaseApplication baseApplication = new BaseApplication();
        baseApplication.onCreate();
        baseApplication.attachBaseContext(this);
        BaseApplication.getApplicationInstance = this;
        if (!"D20".equals(deviceModel)) {
            menuItem.setVisible(true);
        } else {
            menuItem.setVisible(false);
        }
        if ("D20".equals(deviceModel) || "D30".equals(deviceModel) || "D60".equals(deviceModel)) {
            connectType = SharedPreferencesUtil.getmInstance(this);
            String conType = (String) connectType.get("conType", "");
            if (conType == null || "uart".equals(conType)) {
                open(QPOSService.CommunicationMode.UART_SERVICE, this);
            }
            if ("blue".equals(conType)) {
                bluetoothRelaPer();
            }
        } else {
            bluetoothRelaPer();
        }
        toolbar.setTitle(getString(R.string.menu_payment));
        switchFragment(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                String packageVersionName = UpdateAppHelper.getPackageVersionName(MainActivity.this, "com.dspread.demoui");
                tvAppVersion.setText(getString(R.string.app_version) + packageVersionName);
                drawerStateChanged();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                TRACE.d("onDrawerOpened");
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
        SharedPreferencesUtil connectType = SharedPreferencesUtil.getmInstance(this);
        String conType = (String) connectType.get("conType", "");
        if ("blue".equals(conType)) {
            deviceConnectType.setText(getString(R.string.setting_blu));
        } else if ("uart".equals(conType)) {
            deviceConnectType.setText(getString(R.string.setting_uart));
        } else if ("usb".equals(conType)) {
            deviceConnectType.setText(getString(R.string.setting_usb));
        } else if ("Dspread".equals(deviceManufacturer) || "D20".equals(deviceModel) || "D30".equals(deviceModel) || "mp600".equals(deviceModel) || "D60".equals(deviceModel)) {
            connectType.put("conType", "uart");

            deviceConnectType.setText(getString(R.string.setting_uart));
        } else {
            connectType.put("conType", "blue");
            deviceConnectType.setText(getString(R.string.setting_blu));
        }
    }

    @Override
    public void sendValue(String value) {
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
            case R.id.nav_exit:
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
                break;
            case R.id.nav_autotrade:
                toolbar.setTitle(getString(R.string.auto_trade));
               switchFragment(8);
                drawerLayout.close();
                break;

            default:
                break;
        }
        return true;
    }


    private void switchFragment(int i) {
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
               if (autoFragment == null) {
                    autoFragment = new AutoFragment();
                    transaction.add(R.id.nav_host_fragment_content_main, autoFragment);
                }
                transaction.show(autoFragment);
                break;
            default:
                break;
        }
        transaction.commit();
    }

    private void hideFragemts() {
        if (homeFragment != null) {
            TRACE.d("homeFragment");
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
        if (autoFragment != null) {
            transaction.hide(autoFragment);
        }

    }

    private static final int BLUETOOTH_CODE = 100;
    private static final int LOCATION_CODE = 101;
    LocationManager lm;//【Location management】

    public void bluetoothRelaPer() {
        android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//if bluetooth is disabled, add one fix
            Intent enabler = new Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enabler);
        }
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//Location service is on
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission denied
                // Request authorization
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        String[] list = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_ADVERTISE};
                        ActivityCompat.requestPermissions(this, list, BLUETOOTH_CODE);

                    }
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
                }
//                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "System detects that the GPS location service is not turned on", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                }
            });
            launcher.launch(intent);


        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
//        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            toolbar.setTitle(getString(R.string.menu_payment));
            switchFragment(0);
            drawerLayout.close();
            exit();
            return true;
        }
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
                    finish();

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



