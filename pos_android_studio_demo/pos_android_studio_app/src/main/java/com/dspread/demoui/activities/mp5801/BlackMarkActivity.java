package com.dspread.demoui.activities.mp5801;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.activities.printer.PrintSettingActivity;
import com.dspread.helper.printer.BtService;
import com.dspread.helper.printer.PrinterClass;


import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/*
 * 设置黑标的界面
 */
public class BlackMarkActivity extends Activity implements OnClickListener {

	private Button bm_open, bm_close, btn_to_bmark, btn_update;
	private EditText et_heigt, et_wight,et_position, et_voltage;

	private boolean isOpen = false;

	private String str_heigt, str_wight, str_position,str_voltage;
	private int int_heigt, int_wight, int_position,int_voltage;

	private SharedPreferences mySharedPreferences;
	private SharedPreferences.Editor editor ;
    private RadioGroup my_RadioGroup;
    private RadioButton bm_text,bm_qr,bm_bar;
    int choose=1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_blackmark);

		if (PrintSettingActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
			this.finish();
			return;
		}
		mySharedPreferences= getSharedPreferences("BMNum", 
				Activity.MODE_PRIVATE); 
		editor = mySharedPreferences.edit();
		init();
		
		my_RadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId==bm_bar.getId()){
					choose=3;
				}else if(checkedId==bm_qr.getId()){
					choose=2;
				}else{
					choose=1;
				}
			}
		});

	}

	private void init() {
		// TODO Auto-generated method stub
		bm_open = (Button) findViewById(R.id.bm_open);
		bm_close = (Button) findViewById(R.id.bm_close);
		btn_to_bmark = (Button) findViewById(R.id.btn_to_bmark);
		btn_update = (Button) findViewById(R.id.btn_update);

		et_heigt = (EditText) findViewById(R.id.et_heigt);
		et_wight = (EditText) findViewById(R.id.et_wight);
		et_position=(EditText) findViewById(R.id.et_position);
		et_voltage = (EditText) findViewById(R.id.et_voltage);
		
		my_RadioGroup=(RadioGroup) findViewById(R.id.my_RadioGroup);
		bm_text=(RadioButton) findViewById(R.id.bm_text);
		bm_qr=(RadioButton) findViewById(R.id.bm_qr);
		bm_bar=(RadioButton) findViewById(R.id.bm_bar);

		bm_open.setOnClickListener(this);
		bm_close.setOnClickListener(this);
		btn_to_bmark.setOnClickListener(this);
		btn_update.setOnClickListener(this);
		
		SharedPreferences sharedPreferences_get= getSharedPreferences("BMNum", 
				Activity.MODE_PRIVATE); 
		et_heigt.setText(sharedPreferences_get.getString("heigt", ""));
		et_wight.setText(sharedPreferences_get.getString("wight", ""));
		et_position.setText(sharedPreferences_get.getString("position", ""));
		et_voltage.setText(sharedPreferences_get.getString("voltage", ""));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (PrintSettingActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
			isOpen = false;
			this.finish();
			return;
		}
		switch (v.getId()) {
		case R.id.bm_open:
			PrintSettingActivity.pl.write(new byte[] { 0x1F, 0x1B, 0x1F, (byte) 0x80,
					0x04, 0x05, 0x06, 0x44 });
			isOpen = true;
			bm_open.setTextColor(android.graphics.Color.GREEN);
			bm_open.setEnabled(false);
			bm_close.setTextColor(android.graphics.Color.BLACK);
			bm_close.setEnabled(true);
			break;
		case R.id.bm_close:
			PrintSettingActivity.pl.write(new byte[] { 0x1F, 0x1B, 0x1F, (byte) 0x80,
					0x04, 0x05, 0x06, 0x66 });
			
			isOpen = false;
			bm_close.setTextColor(android.graphics.Color.RED);
			bm_close.setEnabled(false);
			bm_open.setTextColor(android.graphics.Color.BLACK);
			bm_open.setEnabled(true);
			break;
		case R.id.btn_to_bmark:
			if (isOpen) {
				switch (choose) {
				case 1:
					PrintSettingActivity.pl.printText("TesCode:123456789");
					PrintSettingActivity.pl.moveNextBlackLocation();
					break;
				case 2:
					printQRCode();
					PrintSettingActivity.pl.moveNextBlackLocation();
					break;
                case 3:
                	printBarCode();
					PrintSettingActivity.pl.moveNextBlackLocation();
					break;
				default:
					break;
				}
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.black_mark_to_plz_open), Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.btn_update:
			if (isOpen) {

				str_heigt = et_heigt.getText().toString();

				str_wight = et_wight.getText().toString();

				str_voltage = et_voltage.getText().toString();
				
				str_position=et_position.getText().toString();

				if (str_heigt == null || str_heigt.length() <= 0) {
					Toast.makeText(getApplicationContext(), "heigt is null",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (str_wight == null || str_wight.length() <= 0) {
					Toast.makeText(getApplicationContext(), "wight is null",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (str_position == null || str_position.length() <= 0) {
					Toast.makeText(getApplicationContext(), "position is null",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (str_voltage == null || str_voltage.length() <= 0) {
					Toast.makeText(getApplicationContext(), "voltage is null",
							Toast.LENGTH_SHORT).show();
					return;
				}

				int_heigt = Integer.parseInt(str_heigt, 10);
				Log.e("int_heigt", ""+int_heigt);
				int_wight = Integer.parseInt(str_wight, 10)*8;
				int_position = Integer.parseInt(str_position, 10);
				int_voltage = Integer.parseInt(str_voltage, 10);
				
//				// 高度
				PrintSettingActivity.pl.write(twoToOne(new BtService(BlackMarkActivity.this, null, null).maxBlackMarkHeiLen, toLH(int_heigt)));
////			// 宽度
				PrintSettingActivity.pl.write(twoToOne(new BtService(BlackMarkActivity.this, null, null).maxBlackMarkWeiLen, toLH(int_wight)));
////			//起始�?
				PrintSettingActivity.pl.write(twoToOne(new BtService(BlackMarkActivity.this, null, null).setBmBeginStep,
						toLH(int_position)));
				// 电压
				PrintSettingActivity.pl.write(twoToOne(new BtService(BlackMarkActivity.this, null, null).setBmVoltage,toLH(int_voltage))
						);
				
//				Log.e("起始�?,Arrays.toString(toLH(int_position)));
//				
//				Log.e("测试", Arrays.toString(toLH(2500)));
				
				Log.e("起始位置",Arrays.toString(twoToOne(new BtService(BlackMarkActivity.this, null, null).setBmBeginStep,
						toLH(int_position))));
				
				editor.putString("heigt", str_heigt);
				editor.putString("wight", str_wight);
				editor.putString("position", str_position);
				editor.putString("voltage", str_voltage);
				
				editor.commit();
				
				Toast.makeText(getApplicationContext(), "Update Success", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						getString(R.string.black_mark_to_plz_open), Toast.LENGTH_SHORT)
						.show();
			}
			break;
		default:
			break;
		}
	}

	public void printBarCode(){
		byte[] btdata=null;
		try {
			btdata="1234567".getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Enable the barcode
		PrintSettingActivity.pl.isEnableBarCode(true);;
		
		//Set the barcode height is 162
		PrintSettingActivity.pl.setBarcodeHeigh(162);;

		PrintSettingActivity.pl.write(new byte[]{0x1b,0x24,0x10,0x00});
		
		//Set HRI character print location on bottom
		PrintSettingActivity.pl.setHRILocation(PrinterClass.PrintLocation.PRINT_BELOW);;

		PrintSettingActivity.pl.write(new byte[]{0x1d,0x77,0x02});
		
		//Print the barcode use code128
		byte[] qrHead=new byte[]{0x1d,0x6b,0x49,(byte) btdata.length};
		
		byte[] barCodeData=new byte[qrHead.length+btdata.length];
		System.arraycopy(qrHead, 0, barCodeData, 0, qrHead.length);
		System.arraycopy(btdata, 0, barCodeData, qrHead.length, btdata.length);

		PrintSettingActivity.pl.write(barCodeData);
		
	}
	
	public void printQRCode(){
		byte[] btdata=null;
		try {
			btdata="1234567".getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		short datalen=(short) (btdata.length+3);
		byte pL=(byte)(datalen&0xff);
		byte pH=(byte)(datalen>>8);


		PrintSettingActivity.pl.setQRCodeSize(0x33);;


		PrintSettingActivity.pl.setQRCodeErrorLevel(0x05);

		PrintSettingActivity.pl.write(new byte[]{0x1b,0x24,0x65,0x00});

		byte[] qrHead=new byte[]{0x1d,0x28,0x6b,pL,pH,0x31,0x50,0x30};
		
		byte[] qrData=new byte[qrHead.length+datalen];
		System.arraycopy(qrHead, 0, qrData, 0, qrHead.length);
		System.arraycopy(btdata, 0, qrData, qrHead.length, btdata.length);

		PrintSettingActivity.pl.write(qrData);
		PrintSettingActivity.pl.write(new byte[]{0x1d,0x28,0x6b,0x03,0x00,0x31,0x51,0x30});
		PrintSettingActivity.pl.printText("\r\n");
		
	}
	
	
	 public static StringBuffer bytesToString(byte[] bytes)
	 {
	  StringBuffer sBuffer = new StringBuffer();
	  for (int i = 0; i < bytes.length; i++)
	  {
	   String s = Integer.toHexString(bytes[i] & 0xff);
	   if (s.length() < 2)
	    sBuffer.append('0');
	   sBuffer.append(s + " ");
	  }
	  return sBuffer;
	 }
	
	/**
	 * 小端模式 将int转为低字节在前，高字节在后的byte数组
	 * 
	 * @param n
	 *            int
	 * @return byte[]
	 */
	public static byte[] toLH(int n) {
		byte[] b = new byte[2];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
//		b[2] = (byte) (n >> 16 & 0xff);
//		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}

	/** 
	  * 将int转为高字节在前，低字节在后的byte数组 
	  * @param n int 
	  * @return byte[] 
	  */  
	public static byte[] toHH(int n) {  
	  byte[] b = new byte[2];
		b[0] = (byte) (n >> 8 & 0xff);
		b[1] = (byte) (n & 0xff); 
	  return b;  
	}   
	
	
	/**
	 * 合并数组
	 * 
	 * @param data1
	 * @param data2
	 * @return
	 */
	public byte[] twoToOne(byte[] data1, byte[] data2) {

		byte[] data3 = new byte[data1.length + data2.length];
		System.arraycopy(data1, 0, data3, 0, data1.length);
		System.arraycopy(data2, 0, data3, data1.length, data2.length);
		return data3;

	}
	
}
