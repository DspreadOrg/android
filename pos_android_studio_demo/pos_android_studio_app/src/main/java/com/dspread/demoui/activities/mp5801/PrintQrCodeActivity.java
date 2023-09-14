package com.dspread.demoui.activities.mp5801;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.activities.printer.PrintSettingActivity;
import com.dspread.helper.printer.PrinterClass;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

public class PrintQrCodeActivity extends Activity {
	private TextView et_input;
	private Button bt_2d;
	private ImageView image_qr;
	String str;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_qrcode);

		et_input = (EditText) findViewById(R.id.et_input);
		image_qr = (ImageView) findViewById(R.id.image_qr);
		bt_2d = (Button) findViewById(R.id.bt_2d);
		et_input.setText("Print test：test" 
		);

		bt_2d.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (PrintSettingActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
					Toast.makeText(
							PrintQrCodeActivity.this,
							PrintQrCodeActivity.this.getResources().getString(
									R.string.str_unconnected), Toast.LENGTH_SHORT).show();
					return;
				}
				 str = et_input.getText().toString();
//				 byte[] btdata=null;
//				 try {
//				 btdata=str.getBytes("ASCII");
//				 } catch (UnsupportedEncodingException e) {
//				 // TODO Auto-generated catch block
//				 e.printStackTrace();
//				 }
//				 short datalen=(short) (btdata.length+3);
//				 byte pL=(byte)(datalen&0xff);
//				 byte pH=(byte)(datalen>>8);
//				/*
//				 * QR Code 设置单元大小 【格式】 ASCII GS ( k pL pH 1 C n 十六进制 1D 28 6B 03
//				 * 00 31 43 n 十进制 29 40 107 03 0 49 67 n 功能：设置QR CODE 单元大小。
//				 * 说明：·n 对应QR版本号， 决定QR CODE的高度与宽度。 · 1≤n ≤16。(十六进制为0x01≤n ≤0x0f)
//				 */
//				 MainActivity.pl.write(new
//				 byte[]{0x1d,0x28,0x6b,0x03,0x00,0x31,0x43,0x05});
//				
//				 MainActivity.pl.write(new
//				 byte[]{0x1d,0x28,0x6b,0x03,0x00,0x31,0x45,0x05});
//
//				 byte[] qrHead=new
//				 byte[]{0x1d,0x28,0x6b,pL,pH,0x31,0x50,0x30};
//				
//				 byte[] qrData=new byte[qrHead.length+datalen];
//				 System.arraycopy(qrHead, 0, qrData, 0, qrHead.length);
//				 System.arraycopy(btdata, 0, qrData, qrHead.length,
//				 btdata.length);
//
//				 //使二维码居中
//				 MainActivity.pl.write(new byte[]{0x1b,0x61,0x01});
//				 
//				 MainActivity.pl.write(qrData);
//				 MainActivity.pl.write(new byte[]{0x1d,0x28,0x6b,0x03,0x00,0x31,0x51,0x30});
//				 MainActivity.pl.write(new byte[] { 0x1d, 0x0c });
//				MainActivity.pl.write(new byte[]{0x1b,0x61,0x01});
//				MainActivity.pl.write(new byte[]{0x1d,0x21,0x01});
//				MainActivity.pl.write(new byte[]{0x1b,0x45,0x01});
//				MainActivity.pl.printText("民建饭店\n");
//				MainActivity.pl.printText("本地推荐饿了么下单\n");
//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						MainActivity.pl.write(new byte[]{0x1b,0x61,0x00});
//						MainActivity.pl.printImage(twoBtmap2One(createQRImage("民建饭店", 300, 300), word2bitmap(str)));
//					}
//				}, 200);
			
				PrintSettingActivity.pl.printImage(createQRImage(str, 300, 300));

				image_qr.setImageBitmap(createQRImage(str, 300, 300));

				PrintSettingActivity.pl.moveNextBlackLocation();

			}
		});
	}

	/**
	 * Generate the QR code The address or string to be translated, which can be Chinese
	 * 
	 * @param url
	 * @param width
	 * @param height
	 * @return
	 */
	public Bitmap createQRImage(String url, final int width, final int height) {
		try {
			// 判断URL合法性
			if (url == null || "".equals(url) || url.length() < 1) {
				return null;
			}
			Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(url,
					BarcodeFormat.QR_CODE, width, height, hints);
			int[] pixels = new int[width * height];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					} else {
						pixels[y * width + x] = 0xffffffff;
					}
				}
			}
			// 生成二维码图片的格式，使用ARGB_8888
			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveMyBitmap(String bitName, Bitmap mBitmap) {
		File f = new File("/sdcard/" + bitName + ".png");
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// DebugMessage.put("在保存图片时出错："+e.toString());
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 文字转图片
	 * @param str
	 * @return
	 */
	public Bitmap word2bitmap(String str){
		Bitmap bMap = Bitmap.createBitmap(300,
				300, Config.ARGB_8888);
		Canvas canvas = new Canvas(bMap);
		canvas.drawColor(Color.WHITE);
		TextPaint textPaint = new TextPaint();
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(35.0F);
		StaticLayout layout = new StaticLayout(str, textPaint,
				bMap.getWidth(), Alignment.ALIGN_NORMAL,
				(float) 1.0, (float) 0.0, true);
		layout.draw(canvas);
		return bMap;
	}

	/**
	 * 两张图片合并成一张
	 * @param bitmap1
	 * @param bitmap2
	 * @return
	 */
	public Bitmap twoBtmap2One(Bitmap bitmap1,Bitmap bitmap2){
		Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth()+bitmap2.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
		Canvas canvas = new Canvas(bitmap3);
		canvas.drawBitmap(bitmap1, new Matrix(), null);
		canvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null); 
		return bitmap3;
	}
	
}
