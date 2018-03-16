package com.dspread.demoui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.io.ObjectOutputStream.PutField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.ByteArrayBuffer;

import com.dspread.demoui.R;
import com.dspread.xpos.EmvAppTag;
import com.dspread.xpos.EmvCapkTag;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.CardTradeMode;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.DoTransactionType;
import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.DoTradeMode;
import com.dspread.xpos.QPOSService.EMVDataOperation;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.EncryptType;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.FORMATID;
import com.dspread.xpos.QPOSService.LcdModeAlign;
import com.dspread.xpos.QPOSService.PanStatus;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.UpdateInformationResult;
import com.dspread.xpos.Tlv;

import envelope.DukptKeys;
import envelope.Envelope;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Trace;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbDevice;


public class MainActivity extends Activity {

	private Button doTradeButton,serialBtn;
	private Button operateCardBtn;
	private Spinner cmdSp;
	private EditText amountEditText;
	private EditText statusEditText,blockAdd,status;
	private ListView appListView;
	private LinearLayout mafireLi,mafireUL;
	private Dialog dialog;
	private String nfcLog="";
	private Spinner mafireSpinner;
	private Button pollBtn,pollULbtn,veriftBtn,veriftULBtn,readBtn,writeBtn,finishBtn,finishULBtn,getULBtn,readULBtn,fastReadUL,writeULBtn,transferBtn;

	private Button btnUSB,btnGetId,btnGetInfo;
	private Button btnQuickEMV;
	private Button btnQuickEMVtrade;
	private Button btnBT;
	private Button btnDisconnect;

	private QPOSService pos;
	private MyPosListener listener;

	private String amount = "";
	private String cashbackAmount = "";
	private boolean isPinCanceled = false;
	private String blueTootchAddress = "";
	public static final String POS_BLUETOOTH_ADDRESS = "POS_BLUETOOTH_ADDRESS";

	private boolean isTest = false;
	private boolean isUsb = true;
	private boolean isUart = true;
	private boolean isPosComm = false;
	ArrayList<String> list=new ArrayList<String>();
	private boolean isOTG = false;
	private boolean isQuickEmv=false;
	private boolean isDotrade=false;
	private int type;
	private UsbDevice usbDevice;
	private InnerListview m_ListView;
	private MyListViewAdapter m_Adapter = null;
	private ImageView imvAnimScan;
	private AnimationDrawable animScan;
	private List<BluetoothDevice> lstDevScanned;
	private Handler hdStopScan;
	private static final int REFRESH_PROOGRESS = 1002;
	private int time=0;
	private boolean flag=false;
	private static final int PROGRESS_UP = 1001;
	private boolean isNormalBlu=false;//判断是否为普通蓝牙的标志
	private boolean isCardExisted=false;
	private Button updateFwBtn;
	private Handler myHandler=new Handler();
	private int index=0;
	private String bufksn;
	private Runnable r=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isCardExisted=pos.isCardExist(20);
			TRACE.i("isCardExisted:"+isCardExisted);
			if(isCardExisted){
				statusEditText.setText("card is existed,pls remove card!");
				myHandler.postDelayed(r, 500);
			}else if(!isCardExisted){
				myHandler.removeCallbacks(r);
				pos.doTrade(30);
			}
			
		}
	};
	
	private Handler updata_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PROGRESS_UP:
				statusEditText.setText(msg.obj.toString() + "%");
				break;
			default:
				break;
			}
		};
	};
	
	private void onBTPosSelected(Activity activity, View itemView, int index) {
		if(isNormalBlu){
			pos.stopScanQPos2Mode();
		}else {
			pos.stopScanQposBLE();
		}
		start_time = new Date().getTime();
		if (index == 0) {
			/* index for audio list */
			open(CommunicationMode.AUDIO);
			posType = POS_TYPE.AUDIO;
			pos.openAudio();
		} else if (index == 1 && isUart) {
			/* COM portl list */
			open(CommunicationMode.UART);
			TRACE.d("+++++++UART");
			posType = POS_TYPE.UART;
			blueTootchAddress = "/dev/ttyMT0";//使用串口，同方那边地址为/dev/ttyS1 ttyMT0
			pos.setDeviceAddress(blueTootchAddress);
			pos.openUart();
		} else {
			Map<String, ?> dev = (Map<String, ?>) m_Adapter.getItem(index);
			blueTootchAddress = (String) dev.get("ADDRESS");
			sendMsg(1001);
		}
	}

	protected List<Map<String, ?>> generateAdapterData() {
		if(isNormalBlu){
			lstDevScanned=pos.getDeviceList();
		}else{
			lstDevScanned=pos.getBLElist();
		}
//		TRACE.d("lstDevScanned----"+lstDevScanned);
		List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
		//
		Map<String, Object> itmAudio = new HashMap<String, Object>();
		itmAudio.put("ICON", Integer.valueOf(R.drawable.ic_headphones_on));
		itmAudio.put("TITLE", getResources().getString(R.string.audio));
		itmAudio.put("ADDRESS", getResources().getString(R.string.audio));

		data.add(itmAudio);

		if (isUart) {
			//
			Map<String, Object> itmSerialPort = new HashMap<String, Object>();
			itmSerialPort.put("ICON", Integer.valueOf(R.drawable.serialport));
			itmSerialPort.put("TITLE",
					getResources().getString(R.string.serialport));
			itmSerialPort.put("ADDRESS",
					getResources().getString(R.string.serialport));

			data.add(itmSerialPort);
			//
		}

		for (BluetoothDevice dev : lstDevScanned) {
			Map<String, Object> itm = new HashMap<String, Object>();
			itm.put("ICON",
					dev.getBondState() == BluetoothDevice.BOND_BONDED ? Integer
							.valueOf(R.drawable.bluetooth_blue) : Integer
							.valueOf(R.drawable.bluetooth_blue_unbond));
			itm.put("TITLE", dev.getName() + "(" + dev.getAddress() + ")");
			itm.put("ADDRESS", dev.getAddress());
			//
			data.add(itm);
		}
		//
		return data;
	}

	private void refreshAdapter() {
		if (m_Adapter != null) {
			m_Adapter.clearData();
			m_Adapter = null;
		}
		//
		List<Map<String, ?>> data = generateAdapterData(); 
		m_Adapter = new MyListViewAdapter(this, data);
		//
		m_ListView.setAdapter(m_Adapter);
		
		setListViewHeightBasedOnChildren(m_ListView);
	}

	private class MyListViewAdapter extends BaseAdapter {
		private List<Map<String, ?>> m_DataMap;
		private LayoutInflater m_Inflater;

		public void clearData() {
			m_DataMap.clear();
			m_DataMap = null;
		}

		public MyListViewAdapter(Context context, List<Map<String, ?>> map) {
			this.m_DataMap = map;
			this.m_Inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return m_DataMap.size();
		}

		@Override
		public Object getItem(int position) {
			return m_DataMap.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = m_Inflater.inflate(R.layout.bt_qpos_item, null);
			}
			ImageView m_Icon = (ImageView) convertView
					.findViewById(R.id.item_iv_icon);
			TextView m_TitleName = (TextView) convertView
					.findViewById(R.id.item_tv_lable);
			//
			Map<String, ?> itemdata = (Map<String, ?>) m_DataMap.get(position);
			int idIcon = (Integer) itemdata.get("ICON");
			String sTitleName = (String) itemdata.get("TITLE");
			//
			m_Icon.setBackgroundResource(idIcon);
			m_TitleName.setText(sTitleName);
			//
			return convertView;
		}

	}

	//设置listview的高度
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
		listView.setLayoutParams(params);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//当窗口为用户可见，保持设备常开，并保持亮度不变
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (!isUart) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setContentView(R.layout.activity_main);
		// 打开蓝牙设备
		BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
		if(!adapter.isEnabled()){//表示蓝牙不可用
			Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enabler);
		}
		imvAnimScan = (ImageView) findViewById(R.id.img_anim_scanbt);
		animScan = (AnimationDrawable) getResources().getDrawable(
				R.anim.progressanmi);
		imvAnimScan.setBackgroundDrawable(animScan);
		mafireLi=(LinearLayout) findViewById(R.id.mifareid);
		mafireUL=(LinearLayout) findViewById(R.id.ul_ll);
		status=(EditText) findViewById(R.id.status);
		operateCardBtn=(Button) findViewById(R.id.operate_card);
		updateFwBtn=(Button) findViewById(R.id.updateFW);
		cmdSp=(Spinner) findViewById(R.id.cmd_spinner);
		String[] cmdList=new String[]{"add","reduce","restore"};
		ArrayAdapter<String> cmdAdapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, cmdList);
		cmdSp.setAdapter(cmdAdapter);
		mafireSpinner=(Spinner) findViewById(R.id.verift_spinner);
		blockAdd=(EditText) findViewById(R.id.block_address);
		String[] keyClass = new String[] { "Key A", "Key B" };
		ArrayAdapter<String> spinneradapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, keyClass);
		mafireSpinner.setAdapter(spinneradapter);
		doTradeButton = (Button) findViewById(R.id.doTradeButton);//开始交易
		serialBtn=(Button) findViewById(R.id.serialPort);
		amountEditText = (EditText) findViewById(R.id.amountEditText);
		statusEditText = (EditText) findViewById(R.id.statusEditText);
		btnBT = (Button) findViewById(R.id.btnBT);//选择设备开始扫描按钮
		btnUSB = (Button) findViewById(R.id.btnUSB);//扫描USB设备
		btnDisconnect = (Button) findViewById(R.id.disconnect);//断开连接
		btnGetInfo=(Button) findViewById(R.id.getPosInfo);
		btnQuickEMV   = (Button) findViewById(R.id.btnQuickEMV);//隐藏按钮
		btnQuickEMVtrade   = (Button) findViewById(R.id.btnQuickEMVtrade);
		pollBtn=(Button) findViewById(R.id.search_card);
		pollULbtn=(Button) findViewById(R.id.poll_ulcard);
		veriftBtn=(Button) findViewById(R.id.verify_card);
		veriftULBtn=(Button) findViewById(R.id.verify_ulcard);
		readBtn=(Button) findViewById(R.id.read_card);
		writeBtn=(Button) findViewById(R.id.write_card);
		finishBtn=(Button) findViewById(R.id.finish_card);
		finishULBtn=(Button) findViewById(R.id.finish_ulcard);
		getULBtn=(Button) findViewById(R.id.get_ul);
		readULBtn=(Button) findViewById(R.id.read_ulcard);
		fastReadUL=(Button) findViewById(R.id.fast_read_ul);
		writeULBtn=(Button) findViewById(R.id.write_ul);
		transferBtn=(Button) findViewById(R.id.transfer_card);
		
		Intent intent=getIntent();
		type=intent.getIntExtra("connect_type", 0);
		switch (type) {
		case 1:
			btnBT.setVisibility(View.GONE);
			open(CommunicationMode.AUDIO);
			posType = POS_TYPE.AUDIO;
			pos.openAudio();
			break;
		case 2:
			btnBT.setVisibility(View.GONE);
			serialBtn.setVisibility(View.VISIBLE);
			serialBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					open(CommunicationMode.UART);
					TRACE.d("+++++++UART");
					posType = POS_TYPE.UART;
					blueTootchAddress = "/dev/ttyMT0";//同方那边是s1，天波是s3
					pos.setDeviceAddress(blueTootchAddress);
					pos.openUart();
				}
			});
			break;
		case 3://普通蓝牙
			open(CommunicationMode.BLUETOOTH);
			posType=POS_TYPE.BLUETOOTH;
			btnBT.setVisibility(View.VISIBLE);
			isNormalBlu=true;
			break;
		case 4://其他蓝牙
			open(CommunicationMode.BLUETOOTH_BLE);
			posType=POS_TYPE.BLUETOOTH_BLE;
			btnBT.setVisibility(View.VISIBLE);
			isNormalBlu=false;
			break;
		}
		
		ScrollView parentScrollView=(ScrollView) findViewById(R.id.parentScrollview);
		parentScrollView.smoothScrollTo(0, 0);
		m_ListView = (InnerListview) findViewById(R.id.lv_indicator_BTPOS);
		m_ListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onBTPosSelected(MainActivity.this, view, position);
				m_ListView.setVisibility(View.GONE);
				animScan.stop();
				imvAnimScan.setVisibility(View.GONE);
			}
		});

		
		MyOnClickListener myOnClickListener = new MyOnClickListener();
		//以下是按钮的点击事件
		doTradeButton.setOnClickListener(myOnClickListener);//开始
		btnBT.setOnClickListener(myOnClickListener);
		btnDisconnect.setOnClickListener(myOnClickListener);
		btnUSB.setOnClickListener(myOnClickListener);
		btnGetInfo.setOnClickListener(myOnClickListener);
		updateFwBtn.setOnClickListener(myOnClickListener);
		
		btnQuickEMV.setOnClickListener(myOnClickListener);
		btnQuickEMVtrade.setOnClickListener(myOnClickListener);
		pollBtn.setOnClickListener(myOnClickListener);
		pollULbtn.setOnClickListener(myOnClickListener);
		finishBtn.setOnClickListener(myOnClickListener);
		finishULBtn.setOnClickListener(myOnClickListener);
		readBtn.setOnClickListener(myOnClickListener);
		writeBtn.setOnClickListener(myOnClickListener);
		veriftBtn.setOnClickListener(myOnClickListener);
		veriftULBtn.setOnClickListener(myOnClickListener);
		operateCardBtn.setOnClickListener(myOnClickListener);
		getULBtn.setOnClickListener(myOnClickListener);
		readULBtn.setOnClickListener(myOnClickListener);
		fastReadUL.setOnClickListener(myOnClickListener);
		writeULBtn.setOnClickListener(myOnClickListener);
		transferBtn.setOnClickListener(myOnClickListener);
	}
   
	private POS_TYPE posType = POS_TYPE.BLUETOOTH;

	private static enum POS_TYPE {
		BLUETOOTH, AUDIO, UART,USB,OTG,BLUETOOTH_BLE
	}

	private String sdkVersion;
	/**
	 * 打开设备，获取类对象，开始监听
	 * @param mode
	 */
	private void open(CommunicationMode mode) {
		TRACE.d("open");
		listener = new MyPosListener();
		//实现类的单例模式
		pos = QPOSService.getInstance(mode);
		if (pos == null) {
			statusEditText.setText("CommunicationMode unknow");
			return;
		}
		pos.setConext(getApplicationContext());
		//通过handler处理，监听MyPosListener，实现QposService的接口，（回调接口）
		Handler handler = new Handler(Looper.myLooper());
		pos.initListener(handler, listener);
		sdkVersion = pos.getSdkVersion();
	}

	/**
	 * 关闭设备
	 */
	private void close() {
		TRACE.d("close");
		if (pos == null) {
			return;
		}
		if (posType == POS_TYPE.AUDIO) {
			pos.closeAudio();
		} else if (posType == POS_TYPE.BLUETOOTH) {
//			pos.disconnectBT();
			pos.disConnectBtPos();
		}else if(posType == POS_TYPE.BLUETOOTH_BLE){
			pos.disconnectBLE();
		} else if (posType == POS_TYPE.UART) {
			pos.closeUart();
		}else if (posType == POS_TYPE.USB) {
			pos.closeUsb();
		}else if(posType == POS_TYPE.OTG){
			pos.closeUsb();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// MenuItem audioitem = menu.findItem(R.id.audio_test);
		if (pos != null) {
			if (pos.getAudioControl()) {
//				audioitem.setTitle("音效控制:打开");
				audioitem.setTitle(R.string.audio_open);
			} else {
//				audioitem.setTitle("音效控制:关闭");
				audioitem.setTitle(R.string.audio_close);
			}
		} else {
//			audioitem.setTitle("音效控制:未知");
			audioitem.setTitle(R.string.audio_unknow);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {

		return super.onMenuOpened(featureId, menu);
	}

	MenuItem audioitem = null;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		//下面也是获取菜单栏的布局
//		getLayoutInflater().inflate(R.menu.activity_main, menu);
		inflater.inflate(R.menu.activity_main, menu);
		audioitem = menu.findItem(R.id.audio_test);
		if (pos != null) {
			if (pos.getAudioControl()) {
				audioitem.setTitle("音效控制:打开");
			} else {
				audioitem.setTitle("音效控制:关闭");
			}
		} else {
			 audioitem.setTitle("音效控制:点击查看");
		}
		return true;
	}
	
	class UpdateThread extends Thread {
		public void run() {
			
			while (true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int progress = pos.getUpdateProgress();
				if (progress < 100) {
					Message msg = updata_handler.obtainMessage();
					msg.what = PROGRESS_UP;
					msg.obj = progress;
					msg.sendToTarget();
					continue;
				}
				Message msg = updata_handler.obtainMessage();
				msg.what = PROGRESS_UP;
				msg.obj = "升级完成";
				msg.sendToTarget();
				break;
			}
		};
	};


	/**
	 * 菜单栏的点击事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (pos == null) {
			Toast.makeText(getApplicationContext(), "设备未连接", Toast.LENGTH_LONG).show();
			return true;
		}
		else if(item.getItemId()== R.id.reset_qpos){
			boolean a=pos.resetPosStatus();
			if(a){
				statusEditText.setText("pos reset");
			}
			/*pos.setCardTradeMode(CardTradeMode.UNALLOWED_LOW_TRADE);
			statusEditText.setText("降级设置");*/
		}else if(item.getItemId() == R.id.menu_update){// update the device
			byte[] data = readLine("upgrader.asc");
			pos.updatePosFirmware(data, blueTootchAddress);
			UpdateThread updateThread = new UpdateThread();
			updateThread.start();
		}
		else if(item.getItemId() == R.id.input_pin_or_not){
			/*flag=!flag;
			Toast.makeText(MainActivity.this, ""+flag, Toast.LENGTH_LONG).show();*/
//			pos.setDesKey("0000E68FCB6E9C9F8D064521C87B0000");
//			isDotrade=!isDotrade;
			flag=!flag;
//			pos.getEncryptData("70563".getBytes(), "0", "0", 10);
		}
		
		else if(item.getItemId() == R.id.get_ksn){
			pos.getKsn();//获取ksn
//			pos.getPin(1, 0, 6, "enter the num", "970418XXXXXX3358", "20170310", 15);
		}
		else if(item.getItemId() == R.id.getEncryptData){
			//获得加密数据
			pos.getEncryptData("70563".getBytes(), "1", "0", 10);
		}
		else if(item.getItemId() ==R.id.addKsn){
			pos.addKsn("00");
		}
		else if(item.getItemId() == R.id.doTradeLogOperation){
			pos.doTradeLogOperation(DoTransactionType.GetOne, 0);
		}
		else if(item.getItemId()==R.id.injectKeys){//inject key
			/*1.set the track ipek,emv ipek,pin ipek keys and track ksn,emv ksn,pin ksn and tmk.
			  2.use the getRsa tool to get the RSA_Public_key
			  3.put the pem file to the assets folder,then use the DukptKeys.setFilePath value
			  4.use the method Envelope.getDigitalEnvelopStr() to get the envelopStr
			  5.use the method pos.udpateWorkKey(envelopStr) to inject your keys
			   The follow is the simple*/
			DukptKeys.setTrackipek("A4C122E2887212F60682B64DCBF705B8");
			DukptKeys.setTrackksn("09117121100165E00001");
			DukptKeys.setEmvipek("A4C122E2887212F60682B64DCBF705B8");
			DukptKeys.setEmvksn("09117121100165E00001");
			DukptKeys.setPinipek("A4C122E2887212F60682B64DCBF705B8");
			DukptKeys.setPinksn("09117121100165E00001");
			DukptKeys.setRSA_public_key("D0ED9CFB50633EB8CE7180B1A920FCBBB6CDE9609A6498311AEAA07F125E601B7B4F6A870ACD45BB336C09211C408BF8B7457BCA07AAE2BF23FA02D88FAB49A32DA1AF57F7D01867583514454DBB2481AF162A91982F1CFA962555B3B8603DDF23A879BEB01A9A79D864CBDA2E03A92C9E4600AF3DDD91744F41DB901B713173");
			DukptKeys.setTmk("0123456789ABCDEFFEDCBA9876543210");
			DukptKeys.setFilePath("/assets/rsa_private_pkcs8.pem");
			String envelopStr=Envelope.getDigitalEnvelopStr();
			pos.udpateWorkKey(envelopStr);
		}
		else if(item.getItemId()==R.id.get_update_key){//get the key value
			pos.getUpdateCheckValue();
		}

		else if(item.getItemId() == R.id.set_sleepmode_time){//设置设备睡眠时间
			pos.setSleepModeTime(200);//the time is in 10s and 10000s
		}
		else if(item.getItemId() == R.id.set_shutdowm_time){
			pos.setShutDownTime(120);
		}
		//更新ipek
		else if(item.getItemId()==R.id.updateIPEK){
			pos.doUpdateIPEKOperation("00", "01807031800000E00000", "368CD0597F7E476BC0BBEF9DE2A7A913", "431E2F0000000000", "01807031800000E00000", "368CD0597F7E476BC0BBEF9DE2A7A913", "431E2F0000000000", "01807031800000E00000", "368CD0597F7E476BC0BBEF9DE2A7A913", "431E2F0000000000");
		}else if(item.getItemId()==R.id.getSleepTime){
//			pos.getSleepModeTime();
			pos.getShutDownTime();
		}
		else if(item.getItemId() == R.id.getQuickEmvStatus){
			pos.getQuickEMVStatus(EMVDataOperation.getEmv, "9F061000000000000000000000000000000000");
		}
		else if(item.getItemId() == R.id.setQuickEmvStatus){
			pos.setQuickEmvStatus(true);
		}
		else if(item.getItemId() == R.id.updateEMVAPP){
//			list.add(EmvAppTag.Application_Identifier_AID_terminal+"00000000000000000000000000000000");
//			list.add(EmvAppTag.Terminal_Capabilities+"e0f8c8");
			list.add(EmvAppTag.Terminal_Country_Code+"0484");
			/*list.add(EmvAppTag.ICS+"F4F0F0FAAFFE8000");
			list.add(EmvAppTag.Terminal_type+"22");
			list.add(EmvAppTag.Terminal_Capabilities+"60B8C8");
			list.add(EmvAppTag.Additional_Terminal_Capabilities+"F000F0A001");
			list.add(EmvAppTag.status+"01");
			list.add(EmvAppTag.Electronic_cash_Terminal_Transaction_Limit+"000000500000");
			list.add(EmvAppTag.terminal_contactless_offline_floor_limit+"000000000000");
			
			list.add(EmvAppTag.terminal_execute_cvm_limit+"000000999999");
			list.add(EmvAppTag.Terminal_Floor_Limit+"00000000");
			list.add(EmvAppTag.Identity_of_each_limit_exist+"0F");
			list.add(EmvAppTag.terminal_status_check+"01");
			list.add(EmvAppTag.Terminal_Default_Transaction_Qualifiers+"36C04000");
			list.add(EmvAppTag.Contactless_CVM_Required_limit+"000000060000");*/
//			list.add(EmvAppTag.terminal_contactless_transaction_limit+"000000060000");
//			list.add(EmvAppTag.terminal_execute_cvm_limit+"000000000000");
//			list.add(EmvAppTag.Contactless_CVM_Required_limit+"000000001000");
//			pos.updateEmvAPP(EMVDataOperation.update,"9F0608A000000333010101DF2006000000100000DF010100DF14039F3704DF170199DF180101DF1205D84004F8009F1B0400010000DF2106000000100000DF160199DF150400004000DF1105D84000A8009F08020020DF19060000001000009F7B06000000100000DF13050010000000","");
			statusEditText.setText("updating emvapp...");
			sendMsg(1701);
		}
		else if(item.getItemId() == R.id.updateEMVCAPK){
			list.add(EmvCapkTag.RID+"A000000004");
			list.add(EmvCapkTag.Public_Key_Index+"F1");
			list.add(EmvCapkTag.Public_Key_Module+"A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7");
			list.add(EmvCapkTag.Public_Key_CheckValue+"D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
			list.add(EmvCapkTag.Pk_exponent+"03");
			statusEditText.setText("updating emvcapk...");
			sendMsg(1702);
		}
		else if (item.getItemId() == R.id.audio_test) {
			if (pos == null) {
				Toast.makeText(getApplicationContext(), "设备未连接", Toast.LENGTH_LONG).show();
				return true;
			}
			if (pos.getAudioControl()) {
				pos.setAudioControl(false);
				item.setTitle("音效控制:关闭");
			} else {
				pos.setAudioControl(true);
				item.setTitle("音效控制:打开");
			}
			return true;
		}
		else if (item.getItemId() == R.id.about) {
//			Intent intent = new Intent(MainActivity.this, AboutActivity.class);
//			startActivity(intent);
//			return true;
			statusEditText.setText("SDK版本："+sdkVersion);
		}
		
		else if(item.getItemId() == R.id.setBuzzer){
			pos.doSetBuzzerOperation(3);//显示设置蜂鸣器响3次
//			pos.cbc_mac(24, 0, 0, "08401002017011815513368cuongdaoviet@gmail.com87CE01E91D9B0EB1A4E29A10BB916DB612345678000001100100179BxcZQRa1i0WZXyd8Q0000001302700334916022300065", 10);
		}
		else if (item.getItemId() == R.id.menu_get_deivce_info) {
			statusEditText.setText(R.string.getting_info);
			pos.getQposInfo();
//			pos.getIccCardNo("201801261112");
		} else if (item.getItemId() == R.id.menu_get_pos_id) {
			statusEditText.setText(R.string.getting_pos_id);
//			pos.doUpdateIPEKOperation("03", "00000332100300E00000", "B77DA5FF9A126CD67AB15039F9C2E1B1", "93906AA157EE2604", "00000332100300E00000", "B77DA5FF9A126CD67AB15039F9C2E1B1", "93906AA157EE2604", "00000332100300E00000", "B77DA5FF9A126CD67AB15039F9C2E1B1", "93906AA157EE2604");
			pos.getQposId();
		} else if(item.getItemId()==R.id.setMasterkey){
			pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885");
		}else if (item.getItemId() == R.id.one) {
			HashMap<Integer, Tlv> map=new HashMap<Integer, Tlv>();
			map=pos.getTag("9F0607A00000000310105F300202209F02060000000001009F160F4243544553542031323334353637385F24031809304F07A00000000310109F34035E03009A031712229F03060000000000005A084374520007376277570E4374520007376277D1809220866F9F100706010A0360AC009F4E0F61626364000000000000000000000082025C008E0E000000000000000042035E031F025F25031509309F0702FF009F0D05F0608488009F0E0500100800009F0F05F0689498009F2608919EB54167F569719F2701409F360200549C01009F3303E028C89F3704ACBAC7119F3901059F4005F000F0A001950502000080009B02E8008407A00000000310105F2A0201565F3401009F0902008C9F1A0208409F1E0838333230314943439F3501229F4104000000015F200D2F52414B455348204B554D41525F280205868A023030500A566973612044656269749F0802008C00000000000000");
			String tag="";
			String value="";
			String content="";
			for(int i=0;i<map.size();i++){
				tag=map.get(i).getTag();
				value=map.get(i).getValue();
				content+="tag: "+tag+" value: "+value+"\n";
			}
			statusEditText.setText("tagValue result:\n"+content);
		} else if (item.getItemId() == R.id.two) {
			 String plainData=pos.getPlainData("00000332100300E00050", "A0CED1AF17F76D170B3D985706D6E5CC1058FF86EBE12A9160F20FB4D1EE924CC7433851CC1984914C4D92C56198DA80D9178C6E659157810A3FADA0A547CA2852F71C2C8FF061C2103133637360D2786C6DF42FD2D30F86B00A17EF4AD5654F68BA3990883A51901DC28C447CAF8D9C2275D0264778A9172A08443E0F125926091D0BE88D2B7198010C45704B62058A79F6680185107EBDCF6DE1FF52BE76751144DDEF6F4846F6F13384FCA0AAF1AB6DEE2FFC1C9B5D4874B792DC05276C628767DC25E8BE742050F431DC498FB5B8E2F320321D92DC993D2BCCC2A59DD46C0C797089D57BCBE7252E7094640C75BA955BA0D6CC077424BECC470B2D9B71BD18B704D1A169FF9E0D6B724D7F3A0B00070F8211FF38F226355043CAC634148CEC415AA5339ADB8AF2C95493DE8BFF0AF3A32B0844270C01945C094844ACD49330F749F9015954940949018FF8F66B90EF0CD4B392F38DA4");
			 statusEditText.setText("result:\n"+plainData);
//			pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885");
		} else if (item.getItemId() == R.id.menu_get_pin) {
			statusEditText.setText(R.string.input_pin);
//			pos.getPin("201402121655");
			pos.getPin(1, 0, 6, "please input pin", "5295172002836310", "", 20);
		} else if (item.getItemId() == R.id.menu_icc) {
//			if (pos != null && pos.getBluetoothState()) {//判断蓝牙是否连接
				Intent intent = new Intent(this, IccActivity.class);
				intent.putExtra("adress", blueTootchAddress);
				startActivity(intent);
//				 finish();
//			} else {
//				Toast.makeText(getApplicationContext(), "设备未连接", Toast.LENGTH_LONG).show();
//			}
		} else if (item.getItemId() == R.id.other) {
			final String[] nItems = new String[] { getResources().getString(R.string.mcr_single_mac), getResources().getString(R.string.mcr_double_mac), getResources().getString(R.string.ic_single_mac), getResources().getString(R.string.ic_double_mac) };
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.list)).setItems(nItems, null).setNegativeButton(getResources().getString(R.string.cancel), null);
			//对话框的点击事件没有写
			//TODO
			builder.setItems(nItems, new DialogInterface.OnClickListener() {
					
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					switch (arg1) {
					case 0:
						
						break;

					default:
						break;
					}
				}
			});
			builder.show();
		}else if (item.getItemId() == R.id.isCardExist) {
			pos.isCardExist(30);
		}else if(item.getItemId() == R.id.menu_operate_mafire){
			showSingleChoiceDialog();
		}
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		TRACE.d("onPause");
		if (pos != null) {
			if(isNormalBlu){
				//停止扫描普通蓝牙
				pos.stopScanQPos2Mode();
			}else{
				//停止扫描ble的蓝牙
				pos.stopScanQposBLE();
			}
			close();
//			pos.onDestroy();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		TRACE.d("onResume");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		TRACE.d("onDestroy");
		if (pos != null) {
			close();
//			pos.onDestroy();
			pos=null;
		}
//		android.os.Process.killProcess(android.os.Process.myPid());//直接杀死进程，保证在无意退出系统后能重新加载扫描蓝牙
	}
	
	private int yourChoice=0;
	private void showSingleChoiceDialog(){
	    final String[] items = { "Mafire classic 1","Mafire UL"};
//	    yourChoice = -1;
	    AlertDialog.Builder singleChoiceDialog = 
	        new AlertDialog.Builder(MainActivity.this);
	    singleChoiceDialog.setTitle("please select one");
	    // 第二个参数是默认选项，此处设置为0
	    singleChoiceDialog.setSingleChoiceItems(items, 0, 
	        new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            yourChoice = which;
	        }
	    });
	    singleChoiceDialog.setPositiveButton("OK", 
	        new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
//	            if (yourChoice != -1) {
	               if(yourChoice==0){
	            	   mafireLi.setVisibility(View.VISIBLE);//display m1 mafire card
	            	   mafireUL.setVisibility(View.GONE);//display ul mafire card
	               }else if(yourChoice==1){
	            	   mafireLi.setVisibility(View.GONE);
	            	   mafireUL.setVisibility(View.VISIBLE);
	               }
	            }
//	        }
	    });
	    singleChoiceDialog.show();
	}

	public void dismissDialog() {
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	/**
	 * 根据传输的文件，读取流信息，返回字节数组
	 * @param Filename
	 * @return
	 */
	private byte[] readLine(String Filename) {

		String str = "";
		ByteArrayBuffer buffer = new ByteArrayBuffer(0);
		try {
			android.content.ContextWrapper contextWrapper = new ContextWrapper(this);
			AssetManager assetManager = contextWrapper.getAssets();
			InputStream inputStream = assetManager.open(Filename);
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(inputStream));
			// str = br.readLine();
			int b = inputStream.read();
			while (b != -1) {
				buffer.append((byte) b);
				b = inputStream.read();
			}
			TRACE.d("-----------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toByteArray();
	}
	
	/**
	 * @ClassName: MyPosListener 
	 * @Function: TODO ADD FUNCTION
	 * @date: 2016-11-10 下午6:35:06 
	 * @author qianmengChen
	 */
	class MyPosListener implements QPOSServiceListener {

		@Override
		public void onRequestWaitingUser() {//等待卡片
			TRACE.d("onRequestWaitingUser()");
			dismissDialog();
			statusEditText.setText(getString(R.string.waiting_for_card));
		}

		/**
		 * 返回选择的开始，返回交易的结果
		 */
		@Override
		public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
			TRACE.d("onDoTradeResult");
			dismissDialog();
			String cardNo="";
//			boolean a=pos.resetPosStatus();
//			statusEditText.setText("a:"+a);
			if (result == DoTradeResult.NONE) {
				statusEditText.setText(getString(R.string.no_card_detected));
			} else if (result == DoTradeResult.ICC) {
				statusEditText.setText(getString(R.string.icc_card_inserted));
				TRACE.d("EMV ICC Start");
				pos.doEmvApp(EmvOption.START);
			} else if (result == DoTradeResult.NOT_ICC) {
				statusEditText.setText(getString(R.string.card_inserted));
//				pos.resetPosStatus();
				/*time++;
				if(time<=3){//表示插卡三次错误
					dialog=new Dialog(MainActivity.this);
					dialog.setContentView(R.layout.my_alert_dialog);
//					dialog.setTitle("错误提示");
					TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);
					messageTextView.setText("please remove card"+"\n"+"insert again");
					dialog.show();
//					pos.isCardExist(30);
				}else{
					pos.setCardTradeMode(CardTradeMode.ONLY_SWIPE_CARD);
					statusEditText.setText("please swipe card(only swipe card)");
				}*/
			} else if (result == DoTradeResult.BAD_SWIPE) {
				statusEditText.setText(getString(R.string.bad_swipe));
			} else if (result == DoTradeResult.MCR) {//磁条卡
				TRACE.d("decodeData: " + decodeData);
				String content = getString(R.string.card_swiped);
				String formatID = decodeData.get("formatID");
				if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
					String maskedPAN = decodeData.get("maskedPAN");
					String expiryDate = decodeData.get("expiryDate");
					String cardHolderName = decodeData.get("cardholderName");
					String serviceCode = decodeData.get("serviceCode");
					String trackblock = decodeData.get("trackblock");
					String psamId = decodeData.get("psamId");
					String posId = decodeData.get("posId");
					String pinblock = decodeData.get("pinblock");
					String macblock = decodeData.get("macblock");
					String activateCode = decodeData.get("activateCode");
					String trackRandomNumber = decodeData.get("trackRandomNumber");

					content += getString(R.string.format_id) + " " + formatID + "\n";
					content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
					content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
					content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";

					content += getString(R.string.service_code) + " " + serviceCode + "\n";
					content += "trackblock: " + trackblock + "\n";
					content += "psamId: " + psamId + "\n";
					content += "posId: " + posId + "\n";
					content += getString(R.string.pinBlock) + " " + pinblock + "\n";
					content += "macblock: " + macblock + "\n";
					content += "activateCode: " + activateCode + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
					cardNo=maskedPAN;
				} else if (formatID.equals("FF")) {
					String type = decodeData.get("type");
					String encTrack1 = decodeData.get("encTrack1");
					String encTrack2 = decodeData.get("encTrack2");
					String encTrack3 = decodeData.get("encTrack3");
					content += "cardType:" + " " + type + "\n";
					content += "track_1:" + " " + encTrack1 + "\n";
					content += "track_2:" + " " + encTrack2 + "\n";
					content += "track_3:" + " " + encTrack3 + "\n";
				} else {
					String orderID=decodeData.get("orderId");
					String maskedPAN = decodeData.get("maskedPAN");
					String expiryDate = decodeData.get("expiryDate");
					String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
					String serviceCode = decodeData.get("serviceCode");
					String track1Length = decodeData.get("track1Length");
					String track2Length = decodeData.get("track2Length");
					String track3Length = decodeData.get("track3Length");
					String encTracks = decodeData.get("encTracks");
					String encTrack1 = decodeData.get("encTrack1");
					String encTrack2 = decodeData.get("encTrack2");
					String encTrack3 = decodeData.get("encTrack3");
					String partialTrack = decodeData.get("partialTrack");
					// TODO
					String pinKsn = decodeData.get("pinKsn");
					String trackksn = decodeData.get("trackksn");
					String pinBlock = decodeData.get("pinBlock");
					String encPAN = decodeData.get("encPAN");
					String trackRandomNumber = decodeData.get("trackRandomNumber");
					String pinRandomNumber = decodeData.get("pinRandomNumber");
					if(orderID!=null&&!"".equals(orderID)){
						content+="orderID:"+orderID;
					}
					content += getString(R.string.format_id) + " " + formatID + "\n";
					content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
					content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
					content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
					content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
					content += getString(R.string.trackksn) + " " + trackksn + "\n";
					content += getString(R.string.service_code) + " " + serviceCode + "\n";
					content += getString(R.string.track_1_length) + " " + track1Length + "\n";
					content += getString(R.string.track_2_length) + " " + track2Length + "\n";
					content += getString(R.string.track_3_length) + " " + track3Length + "\n";
					content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
					content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
					content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
					content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
					content += getString(R.string.partial_track) + " " + partialTrack + "\n";
					content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
					content += "encPAN: " + encPAN + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
					content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
					cardNo=maskedPAN;
					
				}
				TRACE.d("swipe card:" + content);
				statusEditText.setText(content);
			} else if ((result == DoTradeResult.NFC_ONLINE) || (result == DoTradeResult.NFC_OFFLINE)) {
				TRACE.d(result+", decodeData: " + decodeData);
				nfcLog=decodeData.get("nfcLog");
				String content = getString(R.string.tap_card);
				String formatID = decodeData.get("formatID");
				if (formatID.equals("31") || formatID.equals("40")
						|| formatID.equals("37") || formatID.equals("17")
						|| formatID.equals("11") || formatID.equals("10")) {
					String maskedPAN = decodeData.get("maskedPAN");
					String expiryDate = decodeData.get("expiryDate");
					String cardHolderName = decodeData.get("cardholderName");
					String serviceCode = decodeData.get("serviceCode");
					String trackblock = decodeData.get("trackblock");
					String psamId = decodeData.get("psamId");
					String posId = decodeData.get("posId");
					String pinblock = decodeData.get("pinblock");
					String macblock = decodeData.get("macblock");
					String activateCode = decodeData.get("activateCode");
					String trackRandomNumber = decodeData
							.get("trackRandomNumber");

					content += getString(R.string.format_id) + " " + formatID
							+ "\n";
					content += getString(R.string.masked_pan) + " " + maskedPAN
							+ "\n";
					content += getString(R.string.expiry_date) + " "
							+ expiryDate + "\n";
					content += getString(R.string.cardholder_name) + " "
							+ cardHolderName + "\n";

					content += getString(R.string.service_code) + " "
							+ serviceCode + "\n";
					content += "trackblock: " + trackblock + "\n";
					content += "psamId: " + psamId + "\n";
					content += "posId: " + posId + "\n";
					content += getString(R.string.pinBlock) + " " + pinblock
							+ "\n";
					content += "macblock: " + macblock + "\n";
					content += "activateCode: " + activateCode + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
					cardNo=maskedPAN;
				} else {

					String maskedPAN = decodeData.get("maskedPAN");
					String expiryDate = decodeData.get("expiryDate");
					String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
					String serviceCode = decodeData.get("serviceCode");
					String track1Length = decodeData.get("track1Length");
					String track2Length = decodeData.get("track2Length");
					String track3Length = decodeData.get("track3Length");
					String encTracks = decodeData.get("encTracks");
					String encTrack1 = decodeData.get("encTrack1");
					String encTrack2 = decodeData.get("encTrack2");
					String encTrack3 = decodeData.get("encTrack3");
					String partialTrack = decodeData.get("partialTrack");
					// TODO
					String pinKsn = decodeData.get("pinKsn");
					String trackksn = decodeData.get("trackksn");
					String pinBlock = decodeData.get("pinBlock");
					String encPAN = decodeData.get("encPAN");
					String trackRandomNumber = decodeData
							.get("trackRandomNumber");
					String pinRandomNumber = decodeData.get("pinRandomNumber");

					content += getString(R.string.format_id) + " " + formatID
							+ "\n";
					content += getString(R.string.masked_pan) + " " + maskedPAN
							+ "\n";
					content += getString(R.string.expiry_date) + " "
							+ expiryDate + "\n";
					content += getString(R.string.cardholder_name) + " "
							+ cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
					content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
					content += getString(R.string.trackksn) + " " + trackksn
							+ "\n";
					content += getString(R.string.service_code) + " "
							+ serviceCode + "\n";
					content += getString(R.string.track_1_length) + " "
							+ track1Length + "\n";
					content += getString(R.string.track_2_length) + " "
							+ track2Length + "\n";
					content += getString(R.string.track_3_length) + " "
							+ track3Length + "\n";
					content += getString(R.string.encrypted_tracks) + " "
							+ encTracks + "\n";
					content += getString(R.string.encrypted_track_1) + " "
							+ encTrack1 + "\n";
					content += getString(R.string.encrypted_track_2) + " "
							+ encTrack2 + "\n";
					content += getString(R.string.encrypted_track_3) + " "
							+ encTrack3 + "\n";
					content += getString(R.string.partial_track) + " "
							+ partialTrack + "\n";
					content += getString(R.string.pinBlock) + " " + pinBlock
							+ "\n";
					content += "encPAN: " + encPAN + "\n";
					content += "trackRandomNumber: " + trackRandomNumber + "\n";
					content += "pinRandomNumber:" + " " + pinRandomNumber
							+ "\n";
					cardNo=maskedPAN;
				}
//				TRACE.w("swipe card:" + content);
//				Hashtable<String, String> h =  pos.getNFCBatchData();
//				content += "NFCbatchData: "+h.get("tlv");
				
				TRACE.w(result+": "+content);
				statusEditText.setText(content);
				sendMsg(8003);
			} else if ((result == DoTradeResult.NFC_DECLINED) ) {
				statusEditText.setText(getString(R.string.transaction_declined));
			}else if (result == DoTradeResult.NO_RESPONSE) {
				statusEditText.setText(getString(R.string.card_no_response));
			}
//			pos.buildPinBLock("B710FDBCDFD7D1D4CD7477C899E71A00", "0000000000000000", 1, 0, 6, "622526XXXXXX5453", "please input pin");
//			pos.getPin(1, 0, 6, "please input pin", "5295172002836310", "", 20);
		}

		@Override
		public void onQposInfoResult(Hashtable<String, String> posInfoData) {
			TRACE.d("onQposInfoResult"+posInfoData);
			String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
			String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
			String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
			String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
			String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
			String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
			String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
			String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
			String batteryPercentage = posInfoData.get("batteryPercentage") == null ? ""
					: posInfoData.get("batteryPercentage");
			String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
			String SUB=posInfoData.get("SUB")== null ? "" : posInfoData.get("SUB");
			String pciFirmwareVersion=posInfoData.get("PCI_firmwareVersion")== null ? ""
					: posInfoData.get("PCI_firmwareVersion");
			String pciHardwareVersion=posInfoData.get("PCI_hardwareVersion")== null ? ""
					: posInfoData.get("PCI_hardwareVersion");
			String content = "";
			content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
			content += getString(R.string.firmware_version) + firmwareVersion + "\n";
			content += getString(R.string.usb) + isUsbConnected + "\n";
			content += getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
				content += getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
				content += getString(R.string.battery_percentage)  + batteryPercentage + "\n";
//			}
			content += getString(R.string.hardware_version) + hardwareVersion + "\n";
			content += "SUB : " + SUB + "\n";
			content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
			content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
			content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
			content+="PCI FirmwareVresion:"+pciFirmwareVersion+"\n";
			content+="PCI HardwareVersion:"+pciHardwareVersion+"\n";
			statusEditText.setText(content);
		}

		/**
		 * 请求交易 
		 * TODO 简单描述该方法的实现功能（可选）
		 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
		 */
		@Override
		public void onRequestTransactionResult(TransactionResult transactionResult) {
			TRACE.d("onRequestTransactionResult");
			// clearDisplay();
			dismissDialog();

			// statusEditText.setText("");
			dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.alert_dialog);
			dialog.setTitle(R.string.transaction_result);
			TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);
            if(isQuickEmv) {
            	messageTextView.setText("please remove card. and send data to online");
            	String customDisplayString = "";
        		try {
        			byte[] paras = "\nPLS REMOVE CARD".getBytes("GBK");
        			customDisplayString = QPOSUtil.byteArray2Hex(paras);
        			pos.lcdShowCustomDisplay(LcdModeAlign.LCD_MODE_ALIGNCENTER, customDisplayString,10);
        		} catch (UnsupportedEncodingException e) {
        			e.printStackTrace();
        			TRACE.d("gbk error");
        		}            	
            	
            } else {
					if (transactionResult == TransactionResult.APPROVED) {
						TRACE.d("TransactionResult.APPROVED");
						String message = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": $" + amount + "\n";
						if (!cashbackAmount.equals("")) {
							message += getString(R.string.cashback_amount) + ": INR" + cashbackAmount;
						}
						messageTextView.setText(message);
					} else if (transactionResult == TransactionResult.TERMINATED) {
						clearDisplay();
						messageTextView.setText(getString(R.string.transaction_terminated));
					} else if (transactionResult == TransactionResult.DECLINED) {
						messageTextView.setText(getString(R.string.transaction_declined));
					} else if (transactionResult == TransactionResult.CANCEL) {
						clearDisplay();
						messageTextView.setText(getString(R.string.transaction_cancel));
					} else if (transactionResult == TransactionResult.CAPK_FAIL) {
						messageTextView.setText(getString(R.string.transaction_capk_fail));
					} else if (transactionResult == TransactionResult.NOT_ICC) {
						messageTextView.setText(getString(R.string.transaction_not_icc));
					} else if (transactionResult == TransactionResult.SELECT_APP_FAIL) {
						messageTextView.setText(getString(R.string.transaction_app_fail));
					} else if (transactionResult == TransactionResult.DEVICE_ERROR) {
						messageTextView.setText(getString(R.string.transaction_device_error));
					} else if(transactionResult == TransactionResult.TRADE_LOG_FULL){
						statusEditText.setText("pls clear the trace log and then to begin do trade");
						messageTextView.setText("the trade log has fulled!pls clear the trade log!");
					}else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
						messageTextView.setText(getString(R.string.card_not_supported));
					} else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
						messageTextView.setText(getString(R.string.missing_mandatory_data));
					} else if (transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
						messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
					} else if (transactionResult == TransactionResult.INVALID_ICC_DATA) {
						messageTextView.setText(getString(R.string.invalid_icc_data));
					}else if (transactionResult == TransactionResult.FALLBACK) {
						messageTextView.setText("trans fallback");
					}else if (transactionResult == TransactionResult.NFC_TERMINATED) {
						clearDisplay();
						messageTextView.setText("NFC Terminated");
					} else if (transactionResult == TransactionResult.CARD_REMOVED) {
						clearDisplay();
						messageTextView.setText("CARD REMOVED");
					}
            }

			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dismissDialog();
				}
			});

			dialog.show();
//			Toast.makeText(getApplicationContext(), messageTextView.getText().toString(), Toast.LENGTH_LONG).show();
			amount = "";
			cashbackAmount = "";
			amountEditText.setText("");
		}

		@Override
		public void onRequestBatchData(String tlv) {
			TRACE.d("ICC交易结束");
			// dismissDialog();
			String content = getString(R.string.batch_data);
			TRACE.d("tlv:" + tlv);
			content += tlv;
			statusEditText.setText(content);
//			try {
//				Thread.sleep(2000);
//				if(isDotrade){
//					pos.doTrade(30);
//				}
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
		}

		@Override
		public void onRequestTransactionLog(String tlv) {
			TRACE.d("onRequestTransactionLog");
			dismissDialog();
			String content = getString(R.string.transaction_log);
			content += tlv;
			statusEditText.setText(content);
		}

		@Override
		public void onQposIdResult(Hashtable<String, String> posIdTable) {
			TRACE.w("onQposIdResult: "+posIdTable);
			String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
			String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
			String psamId=posIdTable.get("psamId") == null ? "" : posIdTable
					.get("psamId");
			String content = "";
			content += getString(R.string.posId) + posId + "\n";
			content += "csn: " + csn + "\n";
			content += "conn: " + pos.getBluetoothState() + "\n";
			content += "psamId: " + psamId + "\n";
			statusEditText.setText(content);
			if (isTest) {
				sendMsg(1003);
			}

		}

		@Override
		public void onRequestSelectEmvApp(ArrayList<String> appList) {
			TRACE.d("onRequestSelectEmvApp");
			TRACE.d("请选择App -- S，emv卡片的多种配置");
			dismissDialog();

			dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.emv_app_dialog);
			dialog.setTitle(R.string.please_select_app);

			String[] appNameList = new String[appList.size()];
			for (int i = 0; i < appNameList.length; ++i) {
				TRACE.d("i=" + i + "," + appList.get(i));
				appNameList[i] = appList.get(i);
			}
				
			appListView = (ListView) dialog.findViewById(R.id.appList);
			appListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, appNameList));
			appListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

					pos.selectEmvApp(position);
					TRACE.d("请选择App -- 结束 position = " + position);
					dismissDialog();
				}

			});
			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					pos.cancelSelectEmvApp();
					dismissDialog();
				}
			});
			dialog.show();
		}

		@Override
		public void onRequestSetAmount() {
			TRACE.d("输入金额 -- S");
			if (isPosComm) {
				TransactionType transactionType = TransactionType.GOODS;
				String cashbackAmount = "";
				pos.setAmount("10", cashbackAmount, currencyCode, transactionType);
//				isPosComm = false;
				return;
			}
			dismissDialog();
			dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.amount_dialog);
			dialog.setTitle(getString(R.string.set_amount));

			String[] transactionTypes = new String[] {"GOODS", "SERVICES", "CASHBACK", "INQUIRY", "TRANSFER", "PAYMENT","CHANGE_PIN","REFOUND"  };
			((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item,
					transactionTypes));

			dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String amount = ((EditText) (dialog.findViewById(R.id.amountEditText))).getText().toString();
					String cashbackAmount = ((EditText) (dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
					String transactionTypeString = (String) ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();

					if (transactionTypeString.equals("GOODS")) {
						transactionType = TransactionType.GOODS;
					} else if (transactionTypeString.equals("SERVICES")) {
						transactionType = TransactionType.SERVICES;
					} else if (transactionTypeString.equals("CASHBACK")) {
						transactionType = TransactionType.CASHBACK;
					} else if (transactionTypeString.equals("INQUIRY")) {
						transactionType = TransactionType.INQUIRY;
					} else if (transactionTypeString.equals("TRANSFER")) {
						transactionType = TransactionType.TRANSFER;
					} else if (transactionTypeString.equals("PAYMENT")) {
						transactionType = TransactionType.PAYMENT;
					}else if(transactionTypeString.equals("CHANGE_PIN")){
						transactionType = TransactionType.UPDATE_PIN;
					}else if(transactionTypeString.equals("REFOUND")){
						transactionType = TransactionType.REFUND;
					}
					// pos.setAmountIcon("$");//设置pos设备上金额交易的图标
					// pos.setAmountIcon("RMB");
//					amountEditText.setText("$" + amount(amount));
					// amount = "00000000";
					MainActivity.this.amount = amount(amount);
					MainActivity.this.cashbackAmount = cashbackAmount;
//					if (amount.contains(".")) {
//			             String tmp_amount = amount.substring(amount.indexOf(".") + 1, amount.length());
//			             if (tmp_amount.length() == 1) {
//			            	 amount = amount + "0";
//			             }
//			         } else {
//			        	 amount = amount + "00";
//			         }
//			         Integer integer_amount = Integer.valueOf(amount.replace(".", ""));
//			         amountEditText.setText("$" + amount(amount));//luyq modify 20150602
					//pos.setAmount(integer_amount.toString(), cashbackAmount, "156", transactionType);
					pos.setPosDisplayAmountFlag(true);
					pos.setAmount(amount, cashbackAmount, "156", transactionType);
					TRACE.d("输入金额  -- 结束");
					dismissDialog();
				}

			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					pos.cancelSetAmount();
					dialog.dismiss();
				}

			});

			dialog.show();

		}

		/**
		 * 判断是否请求在线连接请求
		 * TODO 简单描述该方法的实现功能（可选）
		 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestIsServerConnected()
		 */
		@Override
		public void onRequestIsServerConnected() {
			TRACE.d("onRequestIsServerConnected");
			pos.isServerConnected(true);
//			TRACE.d("在线过程请求");
//			dismissDialog();
//			dialog = new Dialog(MainActivity.this);
//			dialog.setContentView(R.layout.alert_dialog);
//			dialog.setTitle(R.string.online_process_requested);
//
//			((TextView) dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_connected);
//
//			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					pos.isServerConnected(true);
//					dismissDialog();
//				}
//			});
//
//			dialog.show();
		}

		@Override
		public void onRequestOnlineProcess(String tlv) {
			TRACE.d("onRequestOnlineProcess");
			TRACE.i("return transaction online data:"+tlv);
			/*if(isQuickEmv) {如果是quickemv就直接提示客户拔卡
				statusEditText.setText("please remove card. and send data to online");
            	String customDisplayString = "";
        		try {
        			byte[] paras = "\nPLS REMOVE CARD".getBytes("GBK");
        			customDisplayString = QPOSUtil.byteArray2Hex(paras);
        			pos.lcdShowCustomDisplay(LcdModeAlign.LCD_MODE_ALIGNCENTER, customDisplayString,2);
        		} catch (UnsupportedEncodingException e) {
        			e.printStackTrace();
        			TRACE.d("gbk error");
        		}            	
        		statusEditText.setText("return transaction online data:"+tlv);
			}else*/{
				dismissDialog();
				dialog = new Dialog(MainActivity.this);
				dialog.setContentView(R.layout.alert_dialog);
				dialog.setTitle(R.string.request_data_to_server);
				TRACE.d("onRequestOnlineProcess tlv:" + tlv);
				Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
				decodeData =pos.getICCTag(0, 1, "5A");
				TRACE.i("5A tag: " + decodeData.get("tlv"));
				TRACE.i("onlineProcess: "+decodeData);
				if (isPinCanceled) {
					((TextView) dialog.findViewById(R.id.messageTextView))
							.setText(R.string.replied_failed);
				} else {
					((TextView) dialog.findViewById(R.id.messageTextView))
							.setText(R.string.replied_success);
				}

				dialog.findViewById(R.id.confirmButton).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								if (isPinCanceled) {
									pos.sendOnlineProcessResult(null);
								} else {
									String str = "5A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F009505088004E0009A031406179C01009F02060000000000019F03060000000000009F0702AB009F080200209F0902008C9F0D05D86004A8009F0E0500109800009F0F05D86804F8009F101307010103A02000010A010000000000CE0BCE899F1A0201569F1E0838333230314943439F21031826509F2608881E2E4151E527899F2701809F3303E0F8C89F34030203009F3501229F3602008E9F37042120A7189F4104000000015A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F00";
//									str = "9F26088930C9018CAEBCD69F2701809F101307010103A02802010A0100000000007EF350299F370415B4E5829F360202179505000004E0009A031504169C01009F02060000000010005F2A02015682027C009F1A0201569F03060000000000009F330360D8C89F34030203009F3501229F1E0838333230314943438408A0000003330101019F090200209F410400000001";
									pos.sendOnlineProcessResult("8A023030"+str);
//									TRACE.d("pos.sendOnlineProcessResult++++++++");
								}
								dismissDialog();
							}
						});

				dialog.show();
			}
		}
					
		@Override
		public void onRequestTime() {
			TRACE.d("onRequestTime");
			TRACE.d("要求终端时间。已回覆");
			dismissDialog();
//			String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
			pos.sendTime(terminalTime);
			statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
		}

		@Override
		public void onRequestDisplay(Display displayMsg) {
			TRACE.d("onRequestDisplay");
			dismissDialog();

			String msg = "";
			if (displayMsg == Display.CLEAR_DISPLAY_MSG) {
				msg = "" ;
			} else if(displayMsg == Display.MSR_DATA_READY){
				AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
				builder.setTitle("音频");
				builder.setMessage("Success,Contine ready");
				builder.setPositiveButton("确定", null);
				builder.show();
			}else if (displayMsg == Display.PLEASE_WAIT) {
				msg = getString(R.string.wait);
			} else if (displayMsg == Display.REMOVE_CARD) {
				msg = getString(R.string.remove_card);
			} else if (displayMsg == Display.TRY_ANOTHER_INTERFACE) {
				msg = getString(R.string.try_another_interface);
			} else if (displayMsg == Display.PROCESSING) {
				msg = getString(R.string.processing);
			} else if (displayMsg == Display.INPUT_PIN_ING) {
				msg = "please input pin on pos";
			} else if (displayMsg == Display.MAG_TO_ICC_TRADE) {
				msg = "please insert chip card on pos";
			}else if (displayMsg == Display.CARD_REMOVED) {
				msg = "card removed";
			}
			statusEditText.setText(msg);
		}

		@Override
		public void onRequestFinalConfirm() {
			TRACE.d("onRequestFinalConfirm+确认金额-- S");
			dismissDialog();
			if (!isPinCanceled) {
				dialog = new Dialog(MainActivity.this);
				dialog.setContentView(R.layout.confirm_dialog);
				dialog.setTitle(getString(R.string.confirm_amount));

				String message = getString(R.string.amount) + ": $" + amount;
				if (!cashbackAmount.equals("")) {
					message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
				}

				((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);

				dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						pos.finalConfirm(true);
						dialog.dismiss();
						TRACE.d("确认金额-- 结束");
					}
				});

				dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						pos.finalConfirm(false);
						dialog.dismiss();
					}
				});
				dialog.show();
			} else {
				pos.finalConfirm(false);
			}
		}

		@Override
		public void onRequestNoQposDetected() {
			TRACE.w("onRequestNoQposDetected");
			dismissDialog();
			statusEditText.setText(getString(R.string.no_device_detected));
		}

		@Override
		public void onRequestQposConnected() {
			TRACE.w("onRequestQposConnected");
			Toast.makeText(MainActivity.this, "onRequestQposConnected", Toast.LENGTH_LONG).show();
			dismissDialog();
			long use_time = new Date().getTime() - start_time;
			// statusEditText.setText(getString(R.string.device_plugged));
			statusEditText.setText(getString(R.string.device_plugged) + "--" + getResources().getString(R.string.used) + QPOSUtil.formatLongToTimeStr(use_time, MainActivity.this));
			doTradeButton.setEnabled(true);
			btnDisconnect.setEnabled(true);
			btnQuickEMV.setEnabled(true);
			btnQuickEMVtrade.setEnabled(true);
			selectQuickEMVButtonFlag=false;
		}

		@Override
		public void onRequestQposDisconnected() {
			dismissDialog();
			TRACE.w("onRequestQposDisconnected");
			statusEditText.setText(getString(R.string.device_unplugged));
			btnDisconnect.setEnabled(false);
			doTradeButton.setEnabled(false);
		}

		@Override
		public void onError(Error errorState) {
			TRACE.d("onError");
			dismissDialog();
			amountEditText.setText("");
			if (errorState == Error.CMD_NOT_AVAILABLE) {
				statusEditText.setText(getString(R.string.command_not_available));
			} else if (errorState == Error.TIMEOUT) {
				statusEditText.setText(getString(R.string.device_no_response));
			} else if (errorState == Error.DEVICE_RESET) {
				statusEditText.setText(getString(R.string.device_reset));
			} else if (errorState == Error.UNKNOWN) {
				statusEditText.setText(getString(R.string.unknown_error));
			} else if (errorState == Error.DEVICE_BUSY) {
				statusEditText.setText(getString(R.string.device_busy));
			} else if (errorState == Error.INPUT_OUT_OF_RANGE) {
				statusEditText.setText(getString(R.string.out_of_range));
			} else if (errorState == Error.INPUT_INVALID_FORMAT) {
				statusEditText.setText(getString(R.string.invalid_format));
			} else if (errorState == Error.INPUT_ZERO_VALUES) {
				statusEditText.setText(getString(R.string.zero_values));
			} else if (errorState == Error.INPUT_INVALID) {
				statusEditText.setText(getString(R.string.input_invalid));
			} else if (errorState == Error.CASHBACK_NOT_SUPPORTED) {
				statusEditText.setText(getString(R.string.cashback_not_supported));
			} else if (errorState == Error.CRC_ERROR) {
				statusEditText.setText(getString(R.string.crc_error));
			} else if (errorState == Error.COMM_ERROR) {
				statusEditText.setText(getString(R.string.comm_error));
			} else if (errorState == Error.MAC_ERROR) {
				statusEditText.setText(getString(R.string.mac_error));
			} else if (errorState == Error.CMD_TIMEOUT) {
				statusEditText.setText(getString(R.string.cmd_timeout));
			}
		}

		@Override
		public void onReturnReversalData(String tlv) {
			String content = getString(R.string.reversal_data);
			content += tlv;
			TRACE.i("listener: onReturnReversalData: " + tlv);
			statusEditText.setText(content);
		}

		@Override
		public void onReturnGetPinResult(Hashtable<String, String> result) {
			TRACE.d("onReturnGetPinResult");
			String pinBlock = result.get("pinBlock");
			String pinKsn = result.get("pinKsn");
			String content = "get pin result\n";
			content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
			content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
			statusEditText.setText(content);
			TRACE.i(content);
		}

		@Override
		public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onReturnPowerOffIccResult(boolean arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
			// TODO Auto-generated method stub
			if(arg0){
				pos.sendApdu("123456");
			}
		}

		@Override
		public void onReturnSetSleepTimeResult(boolean isSuccess) {
			String content = "";
			if (isSuccess) {
				content = "set the sleep time success.";
			} else {
				content = "set the sleep time failed.";
			}
			statusEditText.setText(content);
		}

		@Override
		public void onGetCardNoResult(String cardNo) {//获取卡号的回调
			statusEditText.setText("cardNo: " + cardNo);
		}

		@Override
		public void onRequestCalculateMac(String calMac) {
			// statusEditText.setText("calMac: " + calMac);
			// TRACE.d("calMac_result: calMac=> " + calMac);
			TRACE.d("onRequestCalculateMac");
			TRACE.d("calMac_result: calMac=> s: " + calMac);
			if (calMac != null && !"".equals(calMac)) {
				calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
			}
			statusEditText.setText("calMac: " + calMac);
			TRACE.d("calMac_result: calMac=> e: " + calMac);

		}

		@Override
		public void onRequestSignatureResult(byte[] arg0) {
		}

		@Override
		public void onRequestUpdateWorkKeyResult(UpdateInformationResult result) {
			TRACE.d("onRequestUpdateWorkKeyResult");
			if (result == UpdateInformationResult.UPDATE_SUCCESS) {
				statusEditText.setText("update work key success");
			} else if (result == UpdateInformationResult.UPDATE_FAIL) {
				statusEditText.setText("update work key fail");
			} else if (result == UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
				statusEditText.setText("update work key packet vefiry error");
			} else if (result == UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
				statusEditText.setText("update work key packet len error");
			}
		}

		@Override
		public void onReturnCustomConfigResult(boolean isSuccess, String result) {
			TRACE.d("onReturnCustomConfigResult");
			String reString = "Failed";
			if (isSuccess) {
				reString = "Success";
			}
			statusEditText.setText("result: " + reString + "\ndata: " + result);
//			pos.getEncryptData("70533".getBytes(), "0", "0", 15);
		}

		@Override
		public void onRequestSetPin() {
			TRACE.d("onRequestSetPin");
			dismissDialog();
			dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.pin_dialog);
			dialog.setTitle(getString(R.string.enter_pin));
			dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String pin = ((EditText) dialog.findViewById(R.id.pinEditText)).getText().toString();
					if (pin.length() >= 4 && pin.length() <= 12) {
						pos.sendPin(pin);
						dismissDialog();
					}
				}
			});

			dialog.findViewById(R.id.bypassButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					pos.bypassPin();
					pos.sendPin("");
					dismissDialog();
				}
			});

			dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					isPinCanceled = true;
					pos.cancelPin();
					dismissDialog();
				}
			});

			dialog.show();

		}

		@Override
		public void onReturnSetMasterKeyResult(boolean isSuccess) {
			statusEditText.setText("result: " + isSuccess);
		}

		@Override
		public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
			TRACE.d("onReturnBatchSendAPDUResult");
			StringBuilder sb = new StringBuilder();
			sb.append("APDU Responses: \n");
			for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
				sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
			}
			statusEditText.setText("\n" + sb.toString());
		}

		@Override
		public void onBluetoothBondFailed() {
			TRACE.d("onBluetoothBondFailed");
			statusEditText.setText("bond failed");
		}

		@Override
		public void onBluetoothBondTimeout() {
			TRACE.d("onBluetoothBondTimeout");
			statusEditText.setText("bond timeout");
		}

		@Override
		public void onBluetoothBonded() {
			TRACE.d("onBluetoothBonded");
			statusEditText.setText("bond success");

		}

		@Override
		public void onBluetoothBonding() {
			TRACE.d("onBluetoothBonding");
			statusEditText.setText("bonding .....");

		}

		@Override
		public void onReturniccCashBack(Hashtable<String, String> result) {
			TRACE.d("onReturniccCashBack");
			String s = "serviceCode: " + result.get("serviceCode");
			s += "\n";
			s += "trackblock: " + result.get("trackblock");

			statusEditText.setText(s);

		}
        
		
		
		@Override
		public void onLcdShowCustomDisplay(boolean arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onUpdatePosFirmwareResult(UpdateInformationResult arg0) {
		}

		@Override
		public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
			TRACE.d("onReturnDownloadRsaPublicKey");
			if (map == null) {
				TRACE.d("MainActivity++++++++++++++map == null");
				return;
			}
			String randomKeyLen = map.get("randomKeyLen");
			String randomKey = map.get("randomKey");
			String randomKeyCheckValueLen = map.get("randomKeyCheckValueLen");
			String randomKeyCheckValue = map.get("randomKeyCheckValue");
			TRACE.d("randomKey" + randomKey + "    \n    randomKeyCheckValue" + randomKeyCheckValue);
			statusEditText.setText("randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:"
					+ randomKeyCheckValue);
		}

		@Override
		public void onGetPosComm(int mod, String amount, String posid) {
			TRACE.d("onGetPosComm"+mod);
			if (mod == 1) {
				isPosComm = false;
				// MainActivity.this.amount = amount;
				// sendMsg(1003);
				MainActivity.this.amount = "FFFFFFFF";
				pos.doTrade(30);
			}

		}

		@Override
		public void onPinKey_TDES_Result(String arg0) {
			TRACE.d("onPinKey_TDES_Result:=======================" + arg0);
			statusEditText.setText("result:" + arg0);

		}

		@Override
		public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onEmvICCExceptionData(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetInputAmountResult(boolean arg0, String arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnPowerOffNFCResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCbcMacResult(String result) {
			if (result == null || "".equals(result)) {
				statusEditText.setText("cbc_mac:false");
			} else {
				statusEditText.setText("cbc_mac: " + result);
			}
		}

		@Override
		public void onReadBusinessCardResult(boolean arg0, String arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onWriteBusinessCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConfirmAmountResult(boolean arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onQposIsCardExist(boolean cardIsExist) {
			if (cardIsExist) {
//				statusEditText.setText("cardIsExist:"+cardIsExist);
			}else {
//				statusEditText.setText("cardIsExist:"+cardIsExist);
			}
		}

		@Override
		public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
			if(arg0!=null){
				String statuString=arg0.get("status");
				String cardTypeString=arg0.get("cardType");
				String cardUidLen=arg0.get("cardUidLen");
				String cardUid=arg0.get("cardUid");
				String cardAtsLen=arg0.get("cardAtsLen");
				String cardAts=arg0.get("cardAts");
				String ATQA=arg0.get("ATQA");
				String SAK=arg0.get("SAK");
				statusEditText.setText("statuString:"+statuString+"\n"+"cardTypeString:"+cardTypeString+"\ncardUidLen:"+cardUidLen
						+"\ncardUid:"+cardUid+"\ncardAtsLen:"+cardAtsLen+"\ncardAts:"+cardAts
						+"\nATQA:"+ATQA+"\nSAK:"+SAK);
			}else{
				statusEditText.setText("poll card failed");
			}
		}

		@Override
		public void onSetBuzzerResult(boolean arg0) {
			if(arg0){
				statusEditText.setText("蜂鸣器设置成功");
			}else{
				statusEditText.setText("蜂鸣器设置失败");
			}
		}

		@Override
		public void onSetManagementKey(boolean arg0) {
			if(arg0){
				statusEditText.setText("设置主密钥成功");
			}else{
				statusEditText.setText("设置主密钥失败");
			}
		}

		@Override
		public void onReturnUpdateIPEKResult(boolean arg0) {
			if(arg0){
				statusEditText.setText("update IPEK success");
			}else{
				statusEditText.setText("update IPEK fail");
			}
		}

		@Override
		public void onReturnUpdateEMVRIDResult(boolean arg0) {
			if(arg0){
				statusEditText.setText("update RID EMV success");
			}else{
				statusEditText.setText("update RID EMV fail");
			}
		}

		@Override
		public void onReturnUpdateEMVResult(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText("update EMV app success");
			}else{
				statusEditText.setText("update emv app fail~");
			}
		}

		@Override
		public void onBluetoothBoardStateResult(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeviceFound(BluetoothDevice arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				m_ListView.setVisibility(View.VISIBLE);
				animScan.start();
				imvAnimScan.setVisibility(View.VISIBLE);
				refreshAdapter();
				String address=arg0.getAddress();
				String name="";
				name+=address+"\n";
				statusEditText.setText(name);
				TRACE.d("发现有新设备"+name);
			}else {
				statusEditText.setText("没有发现新设备");
				TRACE.d("没有发现新设备");
			}
		}

		@Override
		public void onSetSleepModeTime(boolean arg0) {
			if(arg0){
				statusEditText.setText("set the Sleep timee Success");
			}else{
				statusEditText.setText("set the Sleep timee unSuccess");
			}
		}

		@Override
		public void onReturnGetEMVListResult(String arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null && arg0.length()>0){
				statusEditText.setText("The emv list is : "+arg0);
			}
		}

		@Override
		public void onWaitingforData(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onRequestDeviceScanFinished() {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "扫描结束", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onRequestUpdateKey(String arg0) {
			// TODO Auto-generated method stub
			statusEditText.setText("update checkvalue : "+arg0);
			
		}

		@Override
		public void onReturnGetQuickEmvResult(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText("emv已配置");
//				isQuickEmv=true;
				pos.setQuickEmv(true);
			}else{
				statusEditText.setText("emv未配置");
			}
		}

		@Override
		public void onQposDoGetTradeLogNum(String arg0) {
			int a=Integer.parseInt(arg0, 16);
			if(a>=188){
				statusEditText.setText("the trade num has become max value!!");
				return;
			}
			statusEditText.setText("get log num:"+a);
		}

		@Override
		public void onQposDoTradeLog(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText("clear log success!");
			}else{
				statusEditText.setText("clear log fail!");
			}
		}

		@Override
		public void onAddKey(boolean arg0) {
			if(arg0){
				statusEditText.setText("ksn add 1 success");
			}else{
				statusEditText.setText("ksn add 1 failed");
			}
		}

		@Override
		public void onEncryptData(String arg0) {
			if(arg0!=null){
				statusEditText.setText("get the encrypted result is :"+arg0);
				TRACE.d("get the encrypted result is :"+arg0);
//				pos.getKsn();
//				pos.addKsn("00");
//				pos.getEncryptData("fwe".getBytes(), "0", "0", 10);
			}
		}

		@Override
		public void onQposKsnResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			String pinKsn=arg0.get("pinKsn");
			String trackKsn=arg0.get("trackKsn");
			String emvKsn=arg0.get("emvKsn");
			TRACE.d("get the ksn result is :"+"pinKsn"+pinKsn+"\ntrackKsn"+trackKsn+"\nemvKsn"+emvKsn);
		
		}

		@Override
		public void onQposDoGetTradeLog(String arg0, String arg1) {
			// TODO Auto-generated method stub
			arg1=QPOSUtil.convertHexToString(arg1);
			statusEditText.setText("orderId:"+arg1+"\ntrade log:"+arg0);
		}

		@Override
		public void onFinishMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText("finish success");
			}else{
				statusEditText.setText("finish fail");
			}
		}

		@Override
		public void onVerifyMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText(" onVerifyMifareCardResult success");
			}else{
				statusEditText.setText("onVerifyMifareCardResult fail");
			}
		}

		@Override
		public void onReadMifareCardResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				String addr=arg0.get("addr");
				String cardDataLen=arg0.get("cardDataLen");
				String cardData=arg0.get("cardData");
				statusEditText.setText("addr:"+addr+"\ncardDataLen:"+cardDataLen+"\ncardData:"+cardData);
			}else{
				statusEditText.setText("onReadWriteMifareCardResult fail");
			}
		}

		@Override
		public void onWriteMifareCardResult(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText("write data success!");
			}else{
				statusEditText.setText("write data fail!");
			}
		}

		@Override
		public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				String cmd=arg0.get("Cmd");
				String blockAddr=arg0.get("blockAddr");
				statusEditText.setText("Cmd:"+cmd+"\nBlock Addr:"+blockAddr);
			}else{
				statusEditText.setText("operate failed");
			}
		}

		@Override
		public void getMifareCardVersion(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				String verLen=arg0.get("versionLen");
				String ver=arg0.get("cardVersion");
				statusEditText.setText("versionLen:"+verLen+"\nverison:"+ver);
			}else{
				statusEditText.setText("get mafire UL version failed");
			}
		}

		@Override
		public void getMifareFastReadData(Hashtable<String, String> arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				String startAddr=arg0.get("startAddr");
				String endAddr=arg0.get("endAddr");
				String dataLen=arg0.get("dataLen");
				String cardData=arg0.get("cardData");
				statusEditText.setText("startAddr:"+startAddr+"\nendAddr:"+endAddr+"\ndataLen:"+dataLen
						+"\ncardData:"+cardData);
			}else{
				statusEditText.setText("read fast UL failed");
			}
		}

		@Override
		public void getMifareReadData(Hashtable<String, String> arg0) {
			if(arg0!=null){
				String blockAddr=arg0.get("blockAddr");
				String dataLen=arg0.get("dataLen");
				String cardData=arg0.get("cardData");
				statusEditText.setText("blockAddr:"+blockAddr+"\ndataLen:"+dataLen+"\ncardData:"+cardData);
			}else{
				statusEditText.setText("read mafire UL failed");
			}
		}

		@Override
		public void writeMifareULData(String arg0) {
			if(arg0!=null){
				statusEditText.setText("addr:"+arg0);
			}else{
				statusEditText.setText("write UL failed");
			}
		}

		@Override
		public void verifyMifareULData(Hashtable<String, String> arg0) {
			if(arg0!=null){
				String dataLen=arg0.get("dataLen");
				String pack=arg0.get("pack");
				statusEditText.setText("dataLen:"+dataLen+"\npack:"+pack);
			}else{
				statusEditText.setText("verify UL failed");
			}
		}

		@Override
		public void onGetSleepModeTime(String arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				int time=Integer.parseInt(arg0,16);
				statusEditText.setText("time is ： "+time+" seconds");
			}else{
				statusEditText.setText("get the time is failed");
			}
		}

		@Override
		public void onGetShutDownTime(String arg0) {
			if(arg0!=null){
				statusEditText.setText("shut down time is : "+Integer.parseInt(arg0,16)+"s");
			}else{
				statusEditText.setText("get the shut down time is fail!");
			}
		}

		@Override
		public void onQposDoSetRsaPublicKey(boolean arg0) {
			// TODO Auto-generated method stub
			if(arg0){
				statusEditText.setText("set rsa is successed!");
			}else{
				statusEditText.setText("set rsa is failed!");
			}
		}

		@Override
		public void onQposGenerateSessionKeysResult(Hashtable<String, String> arg0) {
			if(arg0!=null){
				String rsaFileName=arg0.get("rsaReginString");
				String enPinKeyData=arg0.get("enPinKey");
				String enKcvPinKeyData=arg0.get("enPinKcvKey");
				String enCardKeyData=arg0.get("enDataCardKey");
				String enKcvCardKeyData=arg0.get("enKcvDataCardKey");
				statusEditText.setText("rsaFileName:"+rsaFileName+"\nenPinKeyData:"+enPinKeyData+"\nenKcvPinKeyData:"+
						enKcvPinKeyData+"\nenCardKeyData:"+enCardKeyData+"\nenKcvCardKeyData:"+enKcvCardKeyData);
			}else{
				statusEditText.setText("get key failed,pls try again!");
			}
		}

		@Override
		public void transferMifareData(String arg0) {
			// TODO Auto-generated method stub
			if(arg0!=null){
				statusEditText.setText("response data:"+arg0);
			}else{
				statusEditText.setText("transfer data failed!");
			}
		}

		@Override
		public void onReturnRSAResult(String arg0) {
			if(arg0!=null){
				statusEditText.setText("rsa data:\n"+arg0);
			}else{
				statusEditText.setText("get the rsa failed");
			}
		}
	}

	private void clearDisplay() {
		statusEditText.setText("");
	}

	private String amount(String tradeAmount) {
		String rs = "";
		int a = 0;
		if (tradeAmount == null || "".equals(tradeAmount)) {
			return rs;
		}
		try {
			Integer.parseInt(tradeAmount);
		} catch (NumberFormatException e) {
			return rs;
		}
		TRACE.d("---------------:" + tradeAmount);
		if (tradeAmount.startsWith("0")) {
			return rs;
		}
		a = tradeAmount.length();
		if (tradeAmount.length() == 1) {
			rs = "0.0" + tradeAmount;
		} else if (tradeAmount.length() == 2) {
			rs = "0." + tradeAmount;
		} else if (tradeAmount.length() > 2) {
			rs = tradeAmount.substring(0, a - 2) + "." + tradeAmount.substring(a - 2, a);
		}
		return rs;
	}
	
	private long startTime = 0;

	private void si_one() {
		LinkedHashMap<Integer, String[]> example = new LinkedHashMap<Integer, String[]>();
		//将日期按照指定对的格式输出
		String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());

		example.put(1, new String[] { "FE", terminalTime, "00A404000FA0000003334355502D4D4F42494C45" });
		example.put(2, new String[] { "FE", terminalTime, "80E0000000" });
		example.put(3, new String[] { "FE", terminalTime,
				"00D68100404AC0680CDECDF183C0F8435ED4A34F15FE9DF64F7E289A05C0F8435ED4A34F15C0F8435ED4A34F15C0F8435ED4A34F15C0F8435ED4A34F15C0F8435ED4A34F15" });
		example.put(4, new String[] { "FE", terminalTime, "00D682001075681C57D50DC2940100FFFFFFFFFFFF" });// 保存csn
		example.put(5, new String[] { "FE", terminalTime, "00D683000101" });
		example.put(6, new String[] { "FE", terminalTime, "0084000008" });// 取随机数

		pos.VIPOSBatchSendAPDU(example);
	}

	private void si_two() {
		LinkedHashMap<Integer, String[]> example = new LinkedHashMap<Integer, String[]>();
		String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());

		example.put(1, new String[] { "FE", terminalTime, "84F402201C841603538D516996FF92D085F82A1EC2C8D95EC3422ADCE075524904" });
		example.put(2, new String[] { "FE", terminalTime, "84F401131CC2E575D9FEBF0BC8E9B1848350C1A83ED707B462E19A38F7EF908312" });
		example.put(3, new String[] { "FE", terminalTime, "84F401141C6D17E17E576E577FF272F21B08DAACAB6BD70DED617328AFC3DC78B9" });
		example.put(4, new String[] { "FE", terminalTime, "84F401151CA12290798946A652373D849E996A2456FEAE5375A36398DCF582F340" });
		example.put(5, new String[] { "FE", terminalTime, "84F401161C1D8F56B75CC74FADF8453A42E31C1B420FDB9660C5EF19D6051E4865" });
		example.put(6, new String[] { "FE", terminalTime, "84F401171CAE6DD1EFB70D1818F272F21B08DAACAB6BD70DED617328AFCF6FF0E8" });
		example.put(7, new String[] { "FE", terminalTime, "8026000000" });

		pos.VIPOSBatchSendAPDU(example);
	}

	private void apduExample() {
		LinkedHashMap<Integer, String[]> example = new LinkedHashMap<Integer, String[]>();
		String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());

		terminalTime = "20140517162926";
		example.put(1, new String[] { "13", terminalTime, "30303030303030303031303000000000" });
		example.put(2, new String[] { "13", terminalTime, "363231373939383830303030303030303631330000000000" });
		example.put(3, new String[] { "14", terminalTime, "06123456FFFFFFFF" });
		example.put(4, new String[] { "15", terminalTime, "323031343036303331373036333720373644414137333846383136383335373031303080000000008000000000000000" });
		// example.put(5, new String[] {"15",terminalTime,
		// "80FA070078000000000000000032303134303531373136323932362036324431413635374241334333333846443543414443353942363931333932412033374539364236433242454444444331383532303346373136443931413938464632303030303046203031303436444538423633383432303446383643413335"});
		// example.put(6, new String[] {"15",terminalTime,
		// "80FA010078303732364632463442323742373938394235443339304346333531344632333938363444303438343536374645393539363035334335354541353146323943333946333343464346333439463933363130363030302037353638314335374435304443323934303130308000000000008000000000000000"});

		pos.VIPOSBatchSendAPDU(example);
	}

	private String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
	private String currencyCode = "156";
	private TransactionType transactionType = TransactionType.GOODS;
	private void testDoTradeNFC(){
		String customDisplayString = "";
		try {
			byte[] paras = "PLS SWIPE/INSERT CARD".getBytes("GBK");
			customDisplayString = QPOSUtil.byteArray2Hex(paras);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			TRACE.d("gbk error");
		}
		terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
		Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
		hashtable.put("timeout", 30);
		hashtable.put("transactionType", transactionType);
		hashtable.put("TransactionTime", terminalTime);
		hashtable.put("keyIndex", 0);
		hashtable.put("cardTradeMode", CardTradeMode.SWIPE_TAP_INSERT_CARD);
		hashtable.put("currencyCode", "0"+currencyCode);
		hashtable.put("random", "000139");
		hashtable.put("extraData", "1234567890123456");
		hashtable.put("customDisplayString", customDisplayString);
		pos.doTrade(hashtable);
	}
	
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_CONNECTED_DEVICE = 2;
	private static final int REQUEST_SELECT_USB_DEVICE = 3;
	

	class MyOnClickListener implements OnClickListener {

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			statusEditText.setText("");
			if (selectBTFlag) {
				 statusEditText.setText(R.string.wait);
				return;
			}
			else if (v == doTradeButton) {//开始按钮
				if (pos == null) {
					statusEditText.setText(R.string.scan_bt_pos_error);
					return;
				}
	
				if (posType == POS_TYPE.BLUETOOTH) {
					if (blueTootchAddress == null || "".equals(blueTootchAddress)) {
						statusEditText.setText(R.string.scan_bt_pos_error);
						return;
					}
				}
	
				isPinCanceled = false;
				amountEditText.setText("");
				statusEditText.setText(R.string.starting);
				
				terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());	
//				pos.setCardTradeMode(CardTradeMode.ONLY_TAP_CARD);
				
				if (posType == POS_TYPE.UART) {//通用异步收发报机
					pos.doTrade(terminalTime, 0, 30);
				}else {
//					pos.setJudgeDebitOrCreditFlag(true);//做磁条卡判断是借记卡还是信用卡
//					pos.setDesKey("0000E68FCB6E9C9F8D064521C87B0000");
//					pos.doTrade_QF(0x0f, "345", "456");
//					pos.setPanStatus(PanStatus.PLAINTEXT);
					pos.doTrade(30);//start do trade
//					pos.doCheckCard(20);
//					pos.setIsSaveLog(true);
//					pos.doTrade(20, "tra");
				}
			}else if(v == btnUSB){
				USBClass usb = new USBClass();
			    ArrayList<String> deviceList = usb.GetUSBDevices(getBaseContext());
			    if (deviceList == null) {
			    	Toast.makeText(MainActivity.this, "没有权限", Toast.LENGTH_SHORT).show();
					return;
				}
			    final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);

			    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			    builder.setTitle("Select a Reader");
			    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int item) {
			            String selectedDevice = (String) items[item];
			            dialog.dismiss();

			            usbDevice = USBClass.getMdevices().get(selectedDevice);
			            isOTG = true;
			            open(CommunicationMode.USB_OTG_CDC_ACM);
						posType = POS_TYPE.OTG;
						pos.openUsb(usbDevice);
			        }
			    });
			    AlertDialog alert = builder.create();
			    alert.show();
			}
			else if (v == btnBT) {
				isOTG = false;
				if(pos==null){
					if(type==3){
						open(CommunicationMode.BLUETOOTH);
						posType=POS_TYPE.BLUETOOTH;
					}else if(type==4){
						open(CommunicationMode.BLUETOOTH_BLE);
						posType=POS_TYPE.BLUETOOTH_BLE;
					}
				}
				animScan.start();
				imvAnimScan.setVisibility(View.VISIBLE);
				pos.clearBluetoothBuffer();
				close();//扫描前断开蓝牙
				if(isNormalBlu){//普通蓝牙的扫描
//					pos.stopQPos2Mode();//每次开始扫描，需要先停止再开始
					pos.scanQPos2Mode(MainActivity.this,20);//等到扫描结束后再进行下次点击扫描
				}else{//其他蓝牙的扫描
					pos.startScanQposBLE(6);
				}
				refreshAdapter();
				if(m_Adapter!=null){
					TRACE.d("+++++="+m_Adapter);
					m_Adapter.notifyDataSetChanged();//刷新一下
				}
			} else if (v == btnDisconnect) {
				close();
//				pos.disconnectBT();
				/*Set<BluetoothSocket> connectedList = pos.getConnectedSocketList();
				if (connectedList == null) {
					close();
					return;
				}
				ArrayList<CharSequence> list = new ArrayList<CharSequence>();
				for (BluetoothSocket item : connectedList) {
					String value = item.getRemoteDevice().getName() + "\n" + item.getRemoteDevice().getAddress();
					list.add(value);
				}
				Intent intent = new Intent(MainActivity.this, ConnectedDeviceListActivity.class);
				Bundle extras = new Bundle();
				extras.putCharSequenceArrayList("list", list);
				intent.putExtra("bundle", extras);
				startActivityForResult(intent, REQUEST_CONNECTED_DEVICE);*/
			}
			else if (v == btnQuickEMV) {
				statusEditText.setText("updating emv config, please wait...");
				updateEmvConfig();
			}
			else if (v == btnQuickEMVtrade) {
				pos.doTrade();
				isQuickEmv=true;
			}else if(v == btnGetInfo){
				if(pos!=null){
//					pos.getQposInfo();
//					pos.getRSAText(135);
//					pos.setPosSleepTime(300);
				}
			}else if(v == pollBtn){
				statusEditText.setText("begin to poll card!");
				sendMsg(3000);
			}else if(v == pollULbtn){
				statusEditText.setText("begin to poll UL card!");
				sendMsg(3000);
			}else if(v==finishBtn){
				pos.doMifareCard("0E",20);
			}else if(v==finishULBtn){
				pos.doMifareCard("0E",20);
			}else if(v==veriftBtn){
				String keyValue=status.getText().toString();
				String blockaddr=blockAdd.getText().toString();
				String keyclass=(String) mafireSpinner.getSelectedItem();
				pos.setBlockaddr(blockaddr);
				pos.setKeyValue(keyValue);
				pos.doMifareCard("02"+keyclass,20);
			}else if(v==veriftULBtn){
				String keyValue=status.getText().toString();
				pos.setKeyValue(keyValue);
				pos.doMifareCard("0D",20);
			}else if(v == readBtn){
				String blockaddr=blockAdd.getText().toString();
				pos.setBlockaddr(blockaddr);
				pos.doMifareCard("03",20);
			}else if(v == writeBtn){
				String blockaddr=blockAdd.getText().toString();
				String cardData=status.getText().toString();
//				SpannableString s = new SpannableString("please input card data"); 
//		        status.setHint(s);  
				pos.setBlockaddr(blockaddr);
				pos.setKeyValue(cardData);
				pos.doMifareCard("04",20);
			}else if(v == operateCardBtn){
				String blockaddr=blockAdd.getText().toString();
				String cardData=status.getText().toString();
				String cmd=(String) cmdSp.getSelectedItem();
				pos.setBlockaddr(blockaddr);
				pos.setKeyValue(cardData);
				pos.doMifareCard("05"+cmd, 20);
			}else if(v == getULBtn){
				pos.doMifareCard("06", 20);
			}else if(v == readULBtn){
				String blockaddr=blockAdd.getText().toString();
				pos.setBlockaddr(blockaddr);
				pos.doMifareCard("07", 20);
			}else if(v == fastReadUL){
				String endAddr=blockAdd.getText().toString();
				String startAddr=status.getText().toString();
				pos.setKeyValue(startAddr);
				pos.setBlockaddr(endAddr);
				pos.doMifareCard("08", 20);
			}else if(v == writeULBtn){
				String addr=blockAdd.getText().toString();
				String data=status.getText().toString();
				pos.setKeyValue(data);
				pos.setBlockaddr(addr);
				pos.doMifareCard("0B", 20);
			}else if(v == transferBtn){//透传数据
				String data=status.getText().toString();
				String len=blockAdd.getText().toString();
				pos.setMafireLen(Integer.valueOf(len, 16));
				pos.setKeyValue(data);
				pos.doMifareCard("0F", 20);
			}else if(v == updateFwBtn){//update firmware
				byte[] data = readLine("A19IM0112 _master.asc");
				pos.updatePosFirmware(data, blueTootchAddress);
				UpdateThread updateThread = new UpdateThread();
				updateThread.start();
			}
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		isOTG = false;
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE://提示：该demo中自动扫描连接过程中用不到该连接方法
			// When DeviceListActivity returns with a device to connect
			/*if (resultCode == Activity.RESULT_OK) {
				// Get the device MAC address
				int index = data.getExtras().getInt("index");
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				start_time = new Date().getTime();
				if (index == 0) {
					
					open(CommunicationMode.AUDIO);
					posType = POS_TYPE.AUDIO;
					pos.openAudio();
				} else if (index == 1 && isUart) {
					
					open(CommunicationMode.UART);
					TRACE.d(" =====UART");
					posType = POS_TYPE.UART;
					pos.openUart();
				} else if (index == 1 && isUsb) {
					
					open(CommunicationMode.USB);
					TRACE.d("=====USB");
					posType = POS_TYPE.USB;
					pos.setDeviceAddress("/dev/ttyS1");
					pos.openUsb();
				} else {
					if (address.equals("")) {
						Intent settintIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(settintIntent);
						return;
					}
					// open(CommunicationMode.BLUETOOTH_VER2);
					open(CommunicationMode.BLUETOOTH_2Mode);
					TRACE.d("------------>"+pos.isQposPresent());
					posType = POS_TYPE.BLUETOOTH;
					blueTootchAddress = address;
					sendMsg(1001);
				}
			}*/
			break;
		case REQUEST_CONNECTED_DEVICE:
			//断开
			/*if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(ConnectedDeviceListActivity.EXTRA_CONNECTED_ADDRESS);
				if(address.equals("no_devices")){
					return;
				}
				pos.disconnectBT(address);
			}*/
			pos.disconnectBT();
			break;

		}
		
	}

	public void onSelectBluetoothName(final ArrayList<String> btList) {
		dismissDialog();
		TRACE.d("onSelectBluetoothName");

		dialog = new Dialog(MainActivity.this);
		dialog.setContentView(R.layout.search_bt_name);
		dialog.setTitle(R.string.please_select_bt_name);

		String[] appNameList = new String[btList.size()];
		for (int i = 0; i < appNameList.length; ++i) {
			TRACE.d("i=" + i + "," + btList.get(i));
			appNameList[i] = btList.get(i).split(",")[0];
		}

		ListView btListView = (ListView) dialog.findViewById(R.id.btList);
		btListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, appNameList));
		btListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				blueTootchAddress = btList.get(position).split(",")[1];
				dismissDialog();
				TRACE.d("blueTootchAddress:" + blueTootchAddress);
				sendMsg(1001);

			}

		});
		dialog.show();
	}

	private void sendMsg(int what) {
		Message msg = new Message();
		msg.what = what;
		mHandler.sendMessage(msg);
	}

	private boolean selectBTFlag = false;
	private boolean selectQuickEMVButtonFlag = false;
	private long start_time = 0l;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1001:
				btnBT.setEnabled(false);
				btnQuickEMV.setEnabled(false);
				doTradeButton.setEnabled(false);
				selectBTFlag = true;
				selectQuickEMVButtonFlag=true;
				statusEditText.setText(R.string.connecting_bt_pos);
				sendMsg(1002);
				break;
			case 1002:
//				pos.stopQPos2Mode();
				if(isNormalBlu){
					pos.connectBluetoothDevice(true, 25, blueTootchAddress);
				}else{
					pos.connectBLE(blueTootchAddress);
				}
				btnBT.setEnabled(true);
				selectBTFlag = false;
				break;
			case 1003:
				//点击开始运行
				pos.doTrade(30);
				break;
			case 8003:
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				String content="";
				if(nfcLog==null){
					Hashtable<String, String> h =  pos.getNFCBatchData();
					TRACE.i("nfc batchdata1: "+h);
//					Hashtable<String, String> h =pos.getICCTag(1, 0, "");
					content = statusEditText.getText().toString()+ "\nNFCbatchData: "+h.get("tlv");
				}else{
					content = statusEditText.getText().toString()+ "\nNFCbatchData: "+nfcLog;
				}
				statusEditText.setText(content);
				break;	
			case 3000:
				pos.doMifareCard("01",20);
				break;
			case 1701:
				pos.updateEmvAPP(EMVDataOperation.update,list);
				break;
			case 1702:
				pos.updateEmvCAPK(EMVDataOperation.Add, list);
				break;
			default:
				break;
			}
		}
	};
	
	public void updateEmvConfig(){
		String emvAppCfg = QPOSUtil.byteArray2Hex(readLine("emv_app.bin"));
		String emvCapkCfg = QPOSUtil.byteArray2Hex(readLine("emv_capk.bin"));
		TRACE.d("emvAppCfg: "+emvAppCfg);
		TRACE.d("emvCapkCfg: "+emvCapkCfg);
		pos.updateEmvConfig(emvAppCfg,emvCapkCfg);
	}

	/*The following methods used in China*/
	public void calcMacSingle(String cal) {//The calculation of unionpay MAC(Haploid mac key) 
		if(cal.length()%2 != 0){
			cal += "0";
		}
		byte[] mab = QPOSUtil.HexStringToByteArray(cal);
		byte[] ecb = QPOSUtil.ecb(mab);
		pos.calcMacSingleAll(QPOSUtil.byteArray2Hex(ecb),10);
	}

	public void calcMacDouble(String cal) {//The calculation of unionpay MAC(Double mac key)
		byte[] mab = QPOSUtil.HexStringToByteArray(cal);
		byte[] ecb = QPOSUtil.ecb(mab);
		pos.calcMacDoubleAll(QPOSUtil.byteArray2Hex(ecb), 0, 10);
	}

	public void tdesPin(String s) {// Encrypted pin
//		pos.pinKey_TDES_ALL(0, "0123456789012345", 10);
		pos.pinKey_TDES_ALL(0, s, 5);
	}

	public void updateWorkKey(){//update work key
		pos.udpateWorkKey(
				"8365AF96CE566FF16BDD241189B2ED05",	"",//PIN KEY
				"B37DD4DD6456EF0717A3D3343E8B5F85","",  //TRACK KEY
				"B37DD4DD6456EF0717A3D3343E8B5F85",	"", //MAC KEY
				0,5);
	}
	
	public void setMasterKey(String key, String checkValue){
//		pos.setMasterKey("B37DD4DD6456EF0717A3D3343E8B5F85","0A9559BAF6B2814C", 0, 5);// 
		pos.setMasterKey(key, checkValue, 0, 5);
	}
	/*---------------------------------------------*/
	
	private static final String FILENAME = "dsp_axdd";
	/**
	 * desc:保存对象
	 * @param context
	 * @param key 
	 * @param obj 要保存的对象，只能保存实现了serializable的对象
	 * modified:	
	 */
	public static void saveObject(Context context,String key ,Object obj){
		try {
			// 保存对象
			SharedPreferences.Editor sharedata = context.getSharedPreferences(FILENAME, 0).edit();
			//先将序列化结果写到byte缓存中，其实就分配一个内存空间
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			ObjectOutputStream os=new ObjectOutputStream(bos);
			//将对象序列化写入byte缓存
			os.writeObject(obj);
			//将序列化的数据转为16进制保存
			String bytesToHexString = bytesToHexString(bos.toByteArray());
			//保存该16进制数组
			sharedata.putString(key, bytesToHexString);
			sharedata.commit();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("", "保存obj失败");
		}
	}
	/**
	 * desc:将数组转为16进制
	 * @param bArray
	 * @return
	 * modified:	
	 */
	public static String bytesToHexString(byte[] bArray) {
		if(bArray == null){
			return null;
		}
		if(bArray.length == 0){
			return "";
		}
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = 0; i < bArray.length; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
	/**
	 * desc:获取保存的Object对象
	 * @param context
	 * @param key
	 * @return
	 * modified:	
	 */
	public Object readObject(Context context,String key ){
		try {
			SharedPreferences sharedata = context.getSharedPreferences(FILENAME, 0);
			if (sharedata.contains(key)) {
				 String string = sharedata.getString(key, "");
				 if(string==null || "".equals(string)){
					 return null;
				 }else{
					 //将16进制的数据转为数组，准备反序列化
					 byte[] stringToBytes = StringToBytes(string);
					   ByteArrayInputStream bis=new ByteArrayInputStream(stringToBytes);
					   ObjectInputStream is=new ObjectInputStream(bis);
					   //返回反序列化得到的对象
					   Object readObject = is.readObject();
					   return readObject;
				 }
			}
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//所有异常返回null
		return null;
		
	}
	/**
	 * desc:将16进制的数据转为数组
	 * <p>创建人：聂旭阳 , 2014-5-25 上午11:08:33</p>
	 * @param data
	 * @return
	 * modified:	
	 */
	public static byte[] StringToBytes(String data){
		String hexString=data.toUpperCase().trim();
		if (hexString.length()%2!=0) {
			return null;
		}
		byte[] retData=new byte[hexString.length()/2];
		for(int i=0;i<hexString.length();i++)
		{
			int int_ch;  // 两位16进制数转化后的10进制数
			char hex_char1 = hexString.charAt(i); ////两位16进制数中的第一位(高位*16)
			int int_ch1;
			if(hex_char1 >= '0' && hex_char1 <='9')
				int_ch1 = (hex_char1-48)*16;   //// 0 的Ascll - 48
			else if(hex_char1 >= 'A' && hex_char1 <='F')
				int_ch1 = (hex_char1-55)*16; //// A 的Ascll - 65
			else
				return null;
			i++;
			char hex_char2 = hexString.charAt(i); ///两位16进制数中的第二位(低位)
			int int_ch2;
			if(hex_char2 >= '0' && hex_char2 <='9')
				int_ch2 = (hex_char2-48); //// 0 的Ascll - 48
			else if(hex_char2 >= 'A' && hex_char2 <='F')
				int_ch2 = hex_char2-55; //// A 的Ascll - 65
			else
				return null;
			int_ch = int_ch1+int_ch2;
			retData[i/2]=(byte) int_ch;//将转化后的数放入Byte里
		}
		return retData;
  }
}
