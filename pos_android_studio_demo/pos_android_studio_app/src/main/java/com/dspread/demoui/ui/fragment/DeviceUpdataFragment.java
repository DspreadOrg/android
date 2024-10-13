package com.dspread.demoui.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dspread.demoui.R;
import com.dspread.demoui.activity.BaseApplication;
import com.dspread.demoui.activity.PaymentActivity;
import com.dspread.demoui.beans.Constants;
import com.dspread.demoui.ui.dialog.Mydialog;
import com.dspread.demoui.utils.FileUtils;
import com.dspread.demoui.utils.SharedPreferencesUtil;
import com.dspread.demoui.utils.TitleUpdateListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import static com.dspread.demoui.activity.BaseApplication.pos;
import static com.dspread.demoui.ui.dialog.Mydialog.BLUETOOTH;
import static com.dspread.demoui.ui.dialog.Mydialog.UART;
import static com.dspread.demoui.ui.dialog.Mydialog.USB_OTG_CDC_ACM;
import static com.dspread.demoui.utils.Utils.getKeyIndex;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DeviceUpdataFragment extends Fragment implements View.OnClickListener {
    TitleUpdateListener myListener;
    private RelativeLayout updateIpek;
    //    private RelativeLayout updateGetsleeptime;
//    private RelativeLayout updateEmvapp;
//    private RelativeLayout updateEmvrid;
    private RelativeLayout setMasterkey;
    //    private RelativeLayout rlClear;
    private RelativeLayout updateWorkkey;
    private RelativeLayout updateFirmware;
    private RelativeLayout updateEmvByXml;
    private static ProgressBar progressBar;
    private static TextView tvProgress;
    SharedPreferencesUtil connectType;
    String conType;
    public static UpdateThread  updateThread ;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.sendValue(getString(R.string.device_update));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_updata, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        updateIpek = view.findViewById(R.id.update_ipek);
        setMasterkey = view.findViewById(R.id.set_masterkey);
        updateWorkkey = view.findViewById(R.id.update_workkey);
        updateFirmware = view.findViewById(R.id.update_firmware);
        updateEmvByXml = view.findViewById(R.id.update_emvByXml);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgress = view.findViewById(R.id.tv_progress);
        updateIpek.setOnClickListener(this);
        setMasterkey.setOnClickListener(this);
        updateWorkkey.setOnClickListener(this);
        updateFirmware.setOnClickListener(this);
        updateEmvByXml.setOnClickListener(this);
        connectType = SharedPreferencesUtil.getmInstance(getActivity());
    }

    @Override
    public void onClick(View v) {
        conType = (String) connectType.get("conType", "");
        BaseApplication.getApplicationInstance = getActivity();
        switch (v.getId()) {
            case R.id.update_ipek:
                if (conType != null && "uart".equals(conType)) {
                    int keyIndex = getKeyIndex();
                    String ipekGrop = "0" + keyIndex;
                    pos.doUpdateIPEKOperation(ipekGrop, "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944");
                          } else {
                    updateDevice("updateIpeK");
                }
                break;
            case R.id.set_masterkey:
                if (conType != null && "uart".equals(conType)) {
                    int keyIndex = getKeyIndex();
                    pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", keyIndex);
                } else {
                    updateDevice("setMasterkey");
                }
                break;
            case R.id.update_workkey:
                if (conType != null && "uart".equals(conType)) {
                    int keyIndex = getKeyIndex();
                    pos.updateWorkKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",//PIN KEY
                            "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",  //TRACK KEY
                            "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", //MAC KEY
                            keyIndex, 5);
                } else {
                    updateDevice("updateWorkkey");
                }
                break;
            case R.id.update_firmware:
                if (conType != null && "uart".equals(conType)) {
//                    Mydialog.loading(getActivity(),getString(R.string.updateFirmware));
                    UpdateThread.concelFlag = false;
                  updateFirmware();
                } else {
                    updateDevice("updateFirmware");
                }
                break;
            case R.id.update_emvByXml:
                if (!canshow) {
                    return;
                }
                canshow = false;
                showTimer.start();
                if (conType != null && "uart".equals(conType)) {
                    Mydialog.loading(getActivity(),getString(R.string.updateEMVByXml));
                    pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("emv_profile_tlv.xml", getActivity())));
                } else {
                    updateDevice("updateEmvByXml");
                }
                break;
            default:
                break;
        }
    }

    public void updateDevice(String updatedevice) {

        SharedPreferencesUtil connectType = SharedPreferencesUtil.getmInstance(getActivity());
        String conType = (String) connectType.get("conType", "");
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        if ("blue".equals(conType)) {
            intent.putExtra("deviceUpdate", updatedevice);
            intent.putExtra("connect_type", BLUETOOTH);
            startActivity(intent);
        } else if ("uart".equals(conType)) {
            intent.putExtra("deviceUpdate", updatedevice);
            intent.putExtra("connect_type", UART);
            startActivity(intent);
        } else if ("usb".equals(conType)) {
            intent.putExtra("deviceUpdate", updatedevice);
            intent.putExtra("connect_type", USB_OTG_CDC_ACM);
            startActivity(intent);
        }
    }



    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;

    public void updateFirmware() {
       Mydialog.loading(getActivity(),getString(R.string.updateFirmware));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            if (Mydialog.Ldialog!=null) {
                Mydialog.Ldialog.dismiss();
            }
            updateFirmware();
        } else{
            byte[] data = null;
            data = FileUtils.readAssetsLine("D20(mercado-墨西哥)_master.asc", getActivity());
            if (data != null) {
                int a = pos.updatePosFirmware(data, "");
//                Mydialog.loading(PaymentActivity.this, progres + "%");
                if (a == -1) {
                    Mydialog.ErrorDialog(getActivity(), getString(R.string.charging_warning), new Mydialog.OnMyClickListener() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onConfirm() {
                            Mydialog.ErrorDialog.dismiss();
                        }
                    });
                    return;
                }
                Log.w("updateThread.start()","updateThread.start(1234)");
                updateThread = new UpdateThread();
                updateThread.start();
                Log.w("updateThread.start()","updateThread.start()");

            } else {
                Mydialog.ErrorDialog(getActivity(), getString(R.string.does_the_file_exist), new Mydialog.OnMyClickListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirm() {
                        Mydialog.ErrorDialog.dismiss();
                    }
                });
                return;

            }
        }
    }

    public static Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1002:
                    tvProgress.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    updateThread.interrupt();
                    Log.w("handlermessage", "progress---" + Constants.transData.getPosId());
                    break;
                case 1003:
                    int progress = msg.arg1;
                    tvProgress.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    tvProgress.setText(progress + " %");
                    progressBar.setProgress(progress);
                    Log.w("handlermessage", "progress---" + progress);
                    break;

                default:
                    break;
            }
        }
    };

    public static class UpdateThread extends Thread {
        public static boolean concelFlag = false;
        int progress = 0;
        @Override
        public void run() {
            Log.w("UpdateThread","UpdateThread--------------");
            while (!concelFlag) {
                int i = 0;
                while (!concelFlag && i < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    i++;
                }
                if (concelFlag) {
                    Log.w("update","concelflag");
                    Message msg = new Message();
                    msg.what = 1002;
                    msg.arg1 = progress;
                    mHandler.sendMessage(msg);
                    break;

                }
                if (pos == null) {
                    return;
                }
                progress = pos.getUpdateProgress();
                if (progress < 100) {
                            progress++;
                            Message msg = new Message();
                            msg.what = 1003;
                            msg.arg1 = progress;
                            mHandler.sendMessage(msg);
                    continue;
                }
                break;
            }

        }

    }
    private boolean canshow = true;
    private CountDownTimer showTimer = new CountDownTimer(1500, 500) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            canshow = true;
        }

    };
}