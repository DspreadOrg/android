package com.dspread.demoui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.BaseApplication;
import com.dspread.demoui.R;
import com.dspread.demoui.beans.GlobalErrorEvent;
import com.dspread.demoui.interfaces.MifareCardOperationCallback;
import com.dspread.demoui.utils.TRACE;
import com.dspread.xpos.QPOSService;

//import org.bouncycastle.jcajce.provider.symmetric.ARC4;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Hashtable;

public class MifareCardsActivity extends AppCompatActivity implements View.OnClickListener, MifareCardOperationCallback {
    private LinearLayout ll_classic,ll_desfire;
    private Button btnPollCard, btnVerifyCard, btnOperateCard, btnWriteCard, btnReadCard, btnFinishCard;
    private EditText etKeyValue, etBlock, etCardData, etWriteCard, etCardstate;
    private RadioGroup rgkeyClass, rgAddrr;
    private String keyclass = "Key A";
    private String blockaddr = "";
    private EditText etDesfireState, etSendApdu;
    private Button btnPowerOnNfc, btnSendApdu, btnPowerOffNfc;
    private TextView tvTitle;
    private String mifareCardOperationType = "add";
    private QPOSService pos;
    private ImageView ivBackTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_mifare_cards);
        initView();
        Intent intent = getIntent();
        String type = intent.getStringExtra("cardType");
        if("Classic".equals(type)){
            ll_classic.setVisibility(View.VISIBLE);
            ll_desfire.setVisibility(View.GONE);
            tvTitle.setText(getString(R.string.operate_mifareCards));
        }else if("Desfire".equals(type)){
            ll_classic.setVisibility(View.GONE);
            ll_desfire.setVisibility(View.VISIBLE);
            tvTitle.setText(getString(R.string.operate_mifarDesfire));
        }
        pos = ((BaseApplication)getApplication()).getQposService();
        MyQposClass.setMifareCardOperationCallback(this);
        EventBus.getDefault().register(this);
    }

    private void initView(){
        tvTitle = findViewById(R.id.tv_title);
        ivBackTitle = findViewById(R.id.iv_back_title);
        ll_classic = findViewById(R.id.ll_classic);
        ll_desfire = findViewById(R.id.ll_desfire);
        btnPollCard = findViewById(R.id.btn_pollCard);
        btnVerifyCard = findViewById(R.id.btn_verifyCard);
        btnOperateCard = findViewById(R.id.btn_operateCard);
        btnWriteCard = findViewById(R.id.btn_writeCard);
        btnReadCard = findViewById(R.id.btn_readCard);
        btnFinishCard = findViewById(R.id.btn_finishCard);
        etKeyValue = findViewById(R.id.et_keyValue);
        etBlock = findViewById(R.id.et_block);
        rgkeyClass = findViewById(R.id.rg_keyClass);

        etCardData = findViewById(R.id.et_cardData);
        etWriteCard = findViewById(R.id.et_writeCard);
        etCardstate = findViewById(R.id.et_cardstate);
        rgAddrr = findViewById(R.id.rg_addrr);

        etDesfireState = findViewById(R.id.et_desfireState);
        etSendApdu = findViewById(R.id.et_sendApdu);
        btnPowerOnNfc = findViewById(R.id.btn_powerOnNfc);
        btnSendApdu = findViewById(R.id.btn_sendApdu);
        btnPowerOffNfc = findViewById(R.id.btn_powerOffNfc);
        btnPowerOnNfc.setOnClickListener(this);
        btnSendApdu.setOnClickListener(this);
        btnPowerOffNfc.setOnClickListener(this);

        btnPollCard.setOnClickListener(this);
        btnVerifyCard.setOnClickListener(this);
        btnOperateCard.setOnClickListener(this);
        btnWriteCard.setOnClickListener(this);
        btnReadCard.setOnClickListener(this);
        btnFinishCard.setOnClickListener(this);
        ivBackTitle.setOnClickListener(this);

        rgkeyClass.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbtn_KeyA:
                    keyclass = "Key A";
                    break;
                case R.id.rbtn_KeyB:
                    keyclass = "Key B";
                    break;
                default:
                    break;
            }
        });
        rgAddrr.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbtn_Add:
                    mifareCardOperationType = "add";
                    break;
                case R.id.rbtn_Reduce:
                    mifareCardOperationType = "reduce";
                    break;
                case R.id.rbtn_Restore:
                    mifareCardOperationType = "restore";
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back_title:
                finish();
                break;
            case R.id.btn_pollCard:
                pos.pollOnMifareCard(20);
                break;
            case R.id.btn_finishCard:
                pos.finishMifareCard(20);
                break;
            case R.id.btn_verifyCard:
                String keyValue = etKeyValue.getText().toString();
                blockaddr = etBlock.getText().toString();
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(keyValue);
                pos.authenticateMifareCard(QPOSService.MifareCardType.CLASSIC, keyclass, blockaddr, keyValue, 20);
                break;
            case R.id.btn_operateCard:
                blockaddr = etBlock.getText().toString();
                String cardData = etCardData.getText().toString();
                if ("add".equals(mifareCardOperationType)) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.ADD, blockaddr, cardData, 20);
                } else if ("reduce".equals(mifareCardOperationType)) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.REDUCE, blockaddr, cardData, 20);
                } else if ("restore".equals(mifareCardOperationType)) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.RESTORE, blockaddr, cardData, 20);
                }
                break;
            case R.id.btn_writeCard:
                blockaddr = etBlock.getText().toString();
                String writeCard = etWriteCard.getText().toString();
                pos.writeMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, writeCard, 20);
                break;
            case R.id.btn_readCard:
                blockaddr = etBlock.getText().toString();
                pos.readMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, 20);
                break;
            case R.id.btn_powerOnNfc:
                pos.powerOnNFC(false, 20);
                break;
            case R.id.btn_sendApdu:
                String apduString = etSendApdu.getText().toString();
                if (apduString != null && !"".equals(apduString)) {
                    pos.sendApduByNFC(apduString, 20);
                } else {
                    Toast.makeText(this, getString(R.string.please_send_apdu_data), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_powerOffNfc:
                pos.powerOffNFC(20);
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGlobalErrorEvent(GlobalErrorEvent event) {
        // 处理事件
        TRACE.i("error == "+event.errorState);
       QPOSService.Error error = event.errorState;
       runOnUiThread(new Runnable() {
           @Override
           public void run() {
               if(ll_desfire.getVisibility() == View.VISIBLE){
                   etDesfireState.setText("Operate cards failed! "+event.errorState);
               }else {
                   etCardstate.setText("Operate cards failed! "+event.errorState);
               }
           }
       });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (arg0 != null) {
                    TRACE.d("onSearchMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                    String statuString = arg0.get("status");
                    String cardTypeString = arg0.get("cardType");
                    String cardUidLen = arg0.get("cardUidLen");
                    String cardUid = arg0.get("cardUid");
                    String cardAtsLen = arg0.get("cardAtsLen");
                    String cardAts = arg0.get("cardAts");
                    String ATQA = arg0.get("ATQA");
                    String SAK = arg0.get("SAK");
                    etCardstate.setText("statuString:" + statuString + "\n" + "cardTypeString:" + cardTypeString + "\ncardUidLen:" + cardUidLen + "\ncardUid:" + cardUid + "\ncardAtsLen:" + cardAtsLen + "\ncardAts:" + cardAts + "\nATQA:" + ATQA + "\nSAK:" + SAK);
                } else {
                    etCardstate.setText(getString(R.string.poll_card_failed));
                }
            }
        });
    }

    @Override
    public void onFinishMifareCardResult(boolean arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (arg0) {
                    etCardstate.setText(getString(R.string.finish_success));
                } else {
                    etCardstate.setText(getString(R.string.finish_fail));
                }
            }
        });

    }

    @Override
    public void onVerifyMifareCardResult(boolean arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (arg0) {
                    etCardstate.setText(getString(R.string.Verify_success));
                } else {
                    etCardstate.setText(getString(R.string.Verify_fail));
                }
            }
        });
    }

    @Override
    public void onReadMifareCardResult(Hashtable<String, String> arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (arg0 != null) {
                    TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                    String addr = arg0.get("addr");
                    String cardDataLen = arg0.get("cardDataLen");
                    String cardData = arg0.get("cardData");
                    etCardstate.setText("addr:" + addr + "\ncardDataLen:" + cardDataLen + "\ncardData:" + cardData);
                } else {
                    etCardstate.setText(getString(R.string.read_fail));
                }
            }
        });

    }

    @Override
    public void onWriteMifareCardResult(boolean arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (arg0) {
                    etCardstate.setText(getString(R.string.write_success));
                } else {
                    etCardstate.setText(getString(R.string.write_fail));
                }
            }
        });

    }

    @Override
    public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (arg0 != null) {
                    TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                    String cmd = arg0.get("Cmd");
                    String blockAddr = arg0.get("blockAddr");
                    etCardstate.setText("Cmd:" + cmd + "\nBlock Addr:" + blockAddr);
                } else {
                    etCardstate.setText(getString(R.string.operate_failed));
                }
            }
        });

    }

    @Override
    public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etDesfireState.setText("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            }
        });
    }

    @Override
    public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etDesfireState.setText("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
            }
        });
    }

    @Override
    public void onReturnPowerOffNFCResult(boolean arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                etDesfireState.setText(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
            }
        });
    }
}