package com.dspread.demoui.fragment;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dspread.demoui.R;

import com.dspread.demoui.activity.MainActivity;
import com.dspread.demoui.activity.MyQposClass;
import com.dspread.demoui.utils.TitleUpdateListener;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.interfaces.PosInfoCallback;
import com.dspread.demoui.utils.TRACE;
import com.dspread.xpos.QPOSService;

import java.util.Hashtable;


/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class DeviceInfoFragment extends Fragment implements View.OnClickListener, PosInfoCallback {
    TitleUpdateListener titleListener;
    private RelativeLayout getPosid;
    private RelativeLayout getPosinfo;
    private RelativeLayout getUpdatekey;
    private RelativeLayout getKeycheckvalue;

    private TextView tvShowPosInfo;
    private QPOSService pos;
    private BaseApplication application;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        titleListener = (TitleUpdateListener) getActivity();
//        titleListener.setTitle(getString(R.string.device_info));
        getActivity().setTitle(getString(R.string.device_info));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        application = (BaseApplication) getActivity().getApplication();
        MyQposClass.setPosInfoCallback(this);
        tvShowPosInfo = view.findViewById(R.id.tv_pos_info);
        getPosid = view.findViewById(R.id.get_posid);
        getPosinfo = view.findViewById(R.id.get_posinfo);
        getUpdatekey = view.findViewById(R.id.get_updatekey);
        getKeycheckvalue = view.findViewById(R.id.get_keycheckvalue);
        getPosid.setOnClickListener(this);
        getPosinfo.setOnClickListener(this);
        getUpdatekey.setOnClickListener(this);
        getKeycheckvalue.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        pos = application.getQposService();
        if(pos == null){
            goToSetting();
            return;
        }
        switch (v.getId()) {
            case R.id.get_posid:
                pos.getQposId();
                break;
            case R.id.get_posinfo:
                pos.getQposInfo();
                break;
            case R.id.get_updatekey:
                pos.getUpdateCheckValue();
                break;
            case R.id.get_keycheckvalue:
                pos.getKeyCheckValue(0, QPOSService.CHECKVALUE_KEYTYPE.DUKPT_MKSK_ALLTYPE);
                break;
            default:
                break;
        }
    }

    private void goToSetting(){
        ((MainActivity)getActivity()).switchFragment(1);
    }
    
    @Override
    public void onQposInfoResult(Hashtable<String, String> posInfoData) {
        TRACE.d("onQposInfoResult" + posInfoData.toString());
        String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
        String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
        String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
        String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
        String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
        String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
        String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
        String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
        String batteryPercentage = posInfoData.get("batteryPercentage") == null ? "" : posInfoData.get("batteryPercentage");
        String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
        String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
        String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? "" : posInfoData.get("PCI_firmwareVersion");
        String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? "" : posInfoData.get("PCI_hardwareVersion");
        String compileTime = posInfoData.get("compileTime") == null ? "" : posInfoData.get("compileTime");
        String content = "";
        content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
        content += getString(R.string.firmware_version) + firmwareVersion + "\n";
        content += getString(R.string.usb) + isUsbConnected + "\n";
        content += getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
        content += getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
        content += getString(R.string.battery_percentage) + batteryPercentage + "\n";
//			}
        content += getString(R.string.hardware_version) + hardwareVersion + "\n";
        content += "SUB : " + SUB + "\n";
        content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
        content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
        content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
        content += "PCI FirmwareVresion:" + pciFirmwareVersion + "\n";
        content += "PCI HardwareVersion:" + pciHardwareVersion + "\n";
        content += "compileTime:" + compileTime + "\n";
        tvShowPosInfo.setText(content);
    }

    @Override
    public void onQposIdResult(Hashtable<String, String> posIdTable) {
        TRACE.w("onQposIdResult():" + posIdTable.toString());
        String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
        String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
        String psamId = posIdTable.get("psamId") == null ? "" : posIdTable.get("psamId");
        String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable.get("nfcID");
        String content = "";
        content += getString(R.string.posId) + posId + "\n";
        content += "csn: " + csn + "\n";
        content += "conn: " + pos.getBluetoothState() + "\n";
        content += "psamId: " + psamId + "\n";
        content += "NFCId: " + NFCId + "\n";
        tvShowPosInfo.setText(content);
    }

    @Override
    public void onRequestUpdateKey(String arg0) {
        tvShowPosInfo.setText("update checkvalue : "+arg0);
    }

    @Override
    public void onGetKeyCheckValue(Hashtable<String, String> checkValue) {
        String MKSK_TMK_KCV = "MKSK_TMK_KCV : " + checkValue.get("MKSK_TMK_KCV");
        String DUKPT_PIN_IPEK_KCV = "DUKPT_PIN_IPEK_KCV : " + checkValue.get("DUKPT_PIN_IPEK_KCV");
        String DUKPT_PIN_KSN = "DUKPT_PIN_KSN : " + checkValue.get("DUKPT_PIN_KSN");
        String DUKPT_EMV_IPEK_KCV = "DUKPT_EMV_IPEK_KCV : " + checkValue.get("DUKPT_EMV_IPEK_KCV");
        String DUKPT_EMV_KSN = "DUKPT_EMV_KSN : " + checkValue.get("DUKPT_EMV_KSN");
        String DUKPT_TRK_IPEK_KCV = "DUKPT_TRK_IPEK_KCV : " + checkValue.get("DUKPT_TRK_IPEK_KCV");
        String DUKPT_TRK_KSN = "DUKPT_TRK_KSN : " + checkValue.get("DUKPT_TRK_KSN");
        String MKSK_PIK_KCV = "MKSK_PIK_KCV : " + checkValue.get("MKSK_PIK_KCV");
        String MKSK_TDK_KCV = "MKSK_TDK_KCV : " + checkValue.get("MKSK_TDK_KCV");
        String MKSK_MCK_KCV = "MKSK_MCK_KCV : " + checkValue.get("MKSK_MCK_KCV");
        String TCK_KCV = "TCK_KCV : " + checkValue.get("TCK_KCV");
        String MAGK_KCV = "MAGK_KCV : " + checkValue.get("MAGK_KCV");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(MKSK_TMK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_PIN_IPEK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_PIN_KSN);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_EMV_IPEK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_EMV_KSN);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_TRK_IPEK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(DUKPT_TRK_KSN);
        stringBuffer.append("\n");
        stringBuffer.append(MKSK_PIK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(MKSK_TDK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(MKSK_MCK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(TCK_KCV);
        stringBuffer.append("\n");
        stringBuffer.append(MAGK_KCV);
        tvShowPosInfo.setText("PosKeyCheckValue: \n" + stringBuffer.toString());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            tvShowPosInfo.setText("");
        }
    }

}