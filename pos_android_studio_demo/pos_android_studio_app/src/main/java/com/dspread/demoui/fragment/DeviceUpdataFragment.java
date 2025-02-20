package com.dspread.demoui.fragment;

import static com.dspread.demoui.utils.Utils.getKeyIndex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;

import com.dspread.demoui.activity.MainActivity;
import com.dspread.demoui.activity.MyQposClass;
import com.dspread.demoui.beans.Constants;

import com.dspread.demoui.interfaces.PosUpdateCallback;
import com.dspread.demoui.utils.FileUtils;
import com.dspread.demoui.utils.SharedPreferencesUtil;

import com.dspread.demoui.utils.TitleUpdateListener;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DeviceUpdataFragment extends Fragment implements View.OnClickListener, PosUpdateCallback {
    TitleUpdateListener myListener;
    private RelativeLayout updateIpek;
    private RelativeLayout setMasterkey;
    private RelativeLayout updateWorkkey;
    private RelativeLayout updateFirmware;
    private RelativeLayout updateFirmwareByOTA;
    private RelativeLayout updateEmvByXml;
    private TextView tv_pos_result;
    private QPOSService pos;
    private BaseApplication application;
    private UpdateThread updateThread;
    private SharedPreferencesUtil preferencesUtil;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myListener = (TitleUpdateListener) getActivity();
        myListener.setFragmentTitle(getString(R.string.device_update));
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
        application = (BaseApplication) getActivity().getApplication();
        preferencesUtil = SharedPreferencesUtil.getInstance(getActivity());
        MyQposClass.setPosUpdateCallback(this);
        updateIpek = view.findViewById(R.id.update_ipek);
        tv_pos_result = view.findViewById(R.id.tv_pos_result);
        setMasterkey = view.findViewById(R.id.set_masterkey);
        updateWorkkey = view.findViewById(R.id.update_workkey);
        updateFirmware = view.findViewById(R.id.update_firmware);
        updateFirmwareByOTA = view.findViewById(R.id.update_firmware_by_ota);
        updateEmvByXml = view.findViewById(R.id.update_emvByXml);
        updateIpek.setOnClickListener(this);
        setMasterkey.setOnClickListener(this);
        updateWorkkey.setOnClickListener(this);
        updateFirmware.setOnClickListener(this);
        updateFirmwareByOTA.setOnClickListener(this);
        updateEmvByXml.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        pos = application.getQposService();
        if(pos == null){
            goToSetting();
            return;
        }
        switch (v.getId()) {
            case R.id.update_ipek:
                int keyIndex = getKeyIndex();
                String ipekGrop = "0" + keyIndex;
                pos.doUpdateIPEKOperation(ipekGrop, "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944", "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944");
                break;
            case R.id.set_masterkey:
                pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", 0);
                break;
            case R.id.update_workkey:
//                pos.updateWorkKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",//PIN KEY
//                        "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",  //TRACK KEY
//                        "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", //MAC KEY
//                        0, 5);

                pos.updateWorkKey(0,"1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",
                        "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",
                        "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885");
                break;
            case R.id.update_firmware:
                updateFirmware();
                break;
            case R.id.update_firmware_by_ota:
                break;
            case R.id.update_emvByXml:
                tv_pos_result.setText("Emv is updating...");
                pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("emv_profile_tlv.xml", getActivity())));
                break;
            default:
                break;
        }
    }

    private void goToSetting(){
        ((MainActivity)getActivity()).switchFragment(1);
    }

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1003:
                    int progress = msg.arg1;
                    tv_pos_result.setText(progress + " %");
                    break;
                default:
                    break;
            }
        }
    };

    public void updateFirmware() {
        preferencesUtil.put("operationType","updateFirmware");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            byte[] data = null;
            data = FileUtils.readAssetsLine("A29DW.asc", getActivity());
            if (data != null) {
                int a = pos.updatePosFirmware(data, (String) preferencesUtil.get(Constants.BluetoothAddress,""));
                if (a == -1) {
                    tv_pos_result.setText(getString(R.string.charging_warning));
                }else {
                    updateThread = new UpdateThread();
                    updateThread.start();
                }
            } else {
                tv_pos_result.setText(getString(R.string.does_the_file_exist));
            }
        }
    }

    @Override
    public void onReturnUpdateIPEKResult(boolean arg0) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_pos_result.setText("Update IPEK is " + arg0);
            }
        });

    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess, Hashtable<String, String> result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_pos_result.setText("Set Mater key is: "+isSuccess+ " kcv is : "+result.toString());
            }
        });
    }

    @Override
    public void onReturnSetMasterKeyResult(boolean isSuccess) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_pos_result.setText("Set Mater key is: "+isSuccess);
            }
        });

    }

    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result, Hashtable<String, String> checkValue) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                    tv_pos_result.setText(getString(R.string.updateworkkey_success));
                } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
                    tv_pos_result.setText(getString(R.string.updateworkkey_fail));
                } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                    tv_pos_result.setText(getString(R.string.workkey_vefiry_error));
                } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                    tv_pos_result.setText(getString(R.string.workkey_packet_Len_error));
                }
                String info = tv_pos_result.getText().toString();
                tv_pos_result.setText(info + " Kcv is: "+checkValue.toString());
            }
        });
    }

    @Override
    public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                    tv_pos_result.setText(getString(R.string.updateworkkey_success));
                } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
                    tv_pos_result.setText(getString(R.string.updateworkkey_fail));
                } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                    tv_pos_result.setText(getString(R.string.workkey_vefiry_error));
                } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                    tv_pos_result.setText(getString(R.string.workkey_packet_Len_error));
                }
            }
        });

    }

    @Override
    public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateThread.concelSelf();
                if (arg0 == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                    tv_pos_result.setText("update Firmware success");
                } else if (arg0 == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
                    tv_pos_result.setText("update Firmware failed");
                } else if (arg0 == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                    tv_pos_result.setText("update Firmware packet error");
                }
            }
        });
    }

    @Override
    public void onReturnCustomConfigResult(boolean isSuccess, String result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_pos_result.setText("update EMV is "+isSuccess);
            }
        });

    }

    class UpdateThread extends Thread {
        private boolean concelFlag = false;
        int progress = 0;

        @Override
        public void run() {
            preferencesUtil.put(Constants.updateFirmwareStatus,true);
            while (!concelFlag) {
                int i = 0;
                while (!concelFlag && i < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                if (concelFlag) {
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
        public void concelSelf() {
            concelFlag = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}