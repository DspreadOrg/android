package com.dspread.demoui.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.activities.printer.PrintSettingActivity;
import com.dspread.demoui.beans.VersionEnty;
import com.dspread.demoui.http.XHttpUpdateHttpService;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.UpdateAppHelper;
import com.dspread.demoui.widget.CustomDialog;
import com.google.gson.Gson;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate.proxy.impl.DefaultUpdateChecker;
import com.xuexiang.xutil.app.PathUtils;
import com.xuexiang.xutil.display.CProgressDialogUtils;
import com.xuexiang.xutil.resource.ResUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class WelcomeActivity extends BaseActivity implements OnClickListener {
    private Button audio, serial_port, normal_blu, other_blu, print;
    private Intent intent;
    private static final int BLUETOOTH_CODE = 100;
    private static final int LOCATION_CODE = 101;
    private LocationManager lm;//【Location management】
    private Button mp600Print;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle(getString(R.string.title_welcome));
        audio = (Button) findViewById(R.id.audio);
        serial_port = (Button) findViewById(R.id.serial_port);
        normal_blu = (Button) findViewById(R.id.normal_bluetooth);
        other_blu = (Button) findViewById(R.id.other_bluetooth);
        mProgressBar = findViewById(R.id.pb_loading);
        print = (Button) findViewById(R.id.print);
        mp600Print = findViewById(R.id.mp600_print);
        if (Build.MODEL.equals("D20")) {
            print.setVisibility(View.GONE);
        }
        audio.setOnClickListener(this);
        serial_port.setOnClickListener(this);
        normal_blu.setOnClickListener(this);
        other_blu.setOnClickListener(this);
        print.setOnClickListener(this);
        mp600Print.setOnClickListener(this);
        bluetoothRelaPer();
        try {
            checkNewVersion();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkNewVersion() throws IOException {
        mProgressBar.setVisibility(View.VISIBLE);
        String txtPath = PathUtils.getAppExtCachePath() + "/unknown_version/update_forced.json";
        TRACE.d("txtPath:" + txtPath);
        String s = readerMethod(new File(txtPath));
        TRACE.d("Json:" + s);
        Gson gson = new Gson();
        VersionEnty versionEnty = gson.fromJson(s, VersionEnty.class);
        int versionCode = versionEnty.getVersionCode();
        String versionName = versionEnty.getVersionName();
        String modifyContent = versionEnty.getModifyContent();
        int packageVersionCode = UpdateAppHelper.getPackageVersionCode(WelcomeActivity.this, "com.dspread.demoui");
        if (packageVersionCode < versionCode) {
            // tip upgrade
            dialog(versionName, modifyContent);
            mProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "No new version found", Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    public void onToolbarLinstener() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_welcome;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio://Audio
                intent = new Intent(this, OtherActivity.class);
                intent.putExtra("connect_type", 1);
                startActivity(intent);
                break;
            case R.id.serial_port://Serial Port
                intent = new Intent(this, OtherActivity.class);

                intent.putExtra("connect_type", 2);
                startActivity(intent);
                break;
            case R.id.normal_bluetooth://Normal Bluetooth
                intent = new Intent(this, MainActivity.class);

                intent.putExtra("connect_type", 3);
                startActivity(intent);
                break;
            case R.id.other_bluetooth://Other Bluetooth，such as：BLE，，，
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("connect_type", 4);
                startActivity(intent);
                break;
            case R.id.print:
                Log.d("pos", "print");
                intent = new Intent(this, PrintSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.mp600_print: //PrintSerialActivity
                intent = new Intent(this, PrintSettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void bluetoothRelaPer() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//if bluetooth is disabled, add one fix
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enabler);
        }
        lm = (LocationManager) WelcomeActivity.this.getSystemService(WelcomeActivity.this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//Location service is on
            if (ContextCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("POS_SDK", "Permission Denied");
                // Permission denied
                // Request authorization
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        String[] list = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE};
                        ActivityCompat.requestPermissions(WelcomeActivity.this, list, BLUETOOTH_CODE);

                    }
                } else {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
                }
//                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            } else {
                // have permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                        String[] list = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE};
                        ActivityCompat.requestPermissions(WelcomeActivity.this, list, BLUETOOTH_CODE);
                    }
                }
                Toast.makeText(WelcomeActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("BRG", "System detects that the GPS location service is not turned on");
            Toast.makeText(WelcomeActivity.this, "System detects that the GPS location service is not turned on", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is agreed by the user
                    Toast.makeText(WelcomeActivity.this, getString(R.string.msg_allowed_location_permission), Toast.LENGTH_LONG).show();
                } else {
                    // Permission is denied by the user
                    Toast.makeText(WelcomeActivity.this, getString(R.string.msg_not_allowed_loaction_permission), Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }


    private void dialog(String versionName, String modifyContent) {
        // final String modifyContent1 = "update name#123#fix bug#update name#123#fix bug";
        //final String content = modifyContent.replaceAll("#", "\n");
        CustomDialog.Builder builder = new CustomDialog.Builder(WelcomeActivity.this);
        builder.setTitle("Found New Version");
        builder.setMessage(
                "upgrade version：" + versionName + "？" + "\n" +
                        "\n"
                        + modifyContent
        );
        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Upgrade",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String downloadUrl = "https://gitlab.com/api/v4/projects/4128550/jobs/artifacts/master/raw/pos_android_studio_demo/pos_android_studio_app/build/outputs/apk/release/pos_android_studio_app-release.apk?job=assembleRelease";
                        UpdateAppHelper.useApkDownLoadFunction(WelcomeActivity.this, downloadUrl);
                    }
                });
        builder.setCloseButton(new CustomDialog.OnCloseClickListener() {
            @Override
            public void setCloseOnClick() {

                if (customDialog != null) {
                    customDialog.dismiss();
                }
            }
        });

        customDialog = builder.create(R.layout.dialog_update_layout);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();
    }

    private CustomDialog customDialog;

    private String byteToMB(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size > kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }


    private static String readerMethod(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        return sb.toString();
    }


}
