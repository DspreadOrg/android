package com.dspread.demoui.activities.mp5801;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.UnsupportedEncodingException;

public class PrintBarCodeActivity extends Activity {
	private TextView et_input;
	private Button bt_bar;
	private ImageView image_eq;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print_barcode);
		et_input=(EditText)findViewById(R.id.et_input);
		bt_bar = (Button) findViewById(R.id.bt_bar);
		image_eq=(ImageView) findViewById(R.id.image_eq);
		bt_bar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (PrintSettingActivity.pl.getState() != PrinterClass.STATE_CONNECTED) {
					Toast.makeText(
							PrintBarCodeActivity.this,
							PrintBarCodeActivity.this.getResources().getString(
									R.string.str_unconnected), Toast.LENGTH_SHORT).show();
					return;
				}
				
				String message = et_input.getText().toString();

				if (message.getBytes().length > message.length()) {
					Toast.makeText(
							PrintBarCodeActivity.this,
							PrintBarCodeActivity.this.getResources().getString(
									R.string.str_cannotcreatebar), Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (message.length() > 0) {
					
					byte[] btdata=null;
					try {
						btdata=message.getBytes("ASCII");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//Enable the barcode
					PrintSettingActivity.pl.isEnableBarCode(true);
					
					
					//Set the barcode height is 162
					PrintSettingActivity.pl.setBarcodeHeigh(162);;
					
					
					//Set HRI character print location on bottom
					PrintSettingActivity.pl.setHRILocation(PrinterClass.PrintLocation.PRINT_BELOW);;


					PrintSettingActivity.pl.write(new byte[]{0x1d,0x77,0x02});
					
					//Print the barcode use code128
					
					byte[] qrHead=new byte[]{0x1d,0x6b,0x49,(byte) btdata.length};
//					byte[] qrHead=new byte[]{0x1d,0x6b,0x44,(byte) btdata.length};
					
					byte[] barCodeData=new byte[qrHead.length+btdata.length];
					System.arraycopy(qrHead, 0, barCodeData, 0, qrHead.length);
					System.arraycopy(btdata, 0, barCodeData, qrHead.length, btdata.length);
					
//					MainActivity.pl.write(new byte[]{0x1B,0x61,0x01});
					//居中指令
					PrintSettingActivity.pl.setAlignment(PrinterClass.Alignment.ALIGN_MIDDLE);
					PrintSettingActivity.pl.write(barCodeData);
					PrintSettingActivity.pl.printText("\n\n\n");
					//PrintSettingActivity.pl.moveNextBlackLocation();
					//居左指令
					PrintSettingActivity.pl.setAlignment(PrinterClass.Alignment.ALIGN_LEFT);
//					PrintService.pl.printText("\r\n");
					
//					Bitmap btMap = BarcodeCreater.creatBarcode(PrintBarCodeActivity.this,
//							message, 384, 100, true, 1);
//					MainActivity.pl.printImage(btMap);
					
//					MainActivity.pl.write(new byte[] { 0x1d, 0x0c });
					
					try {
						image_eq.setImageBitmap(CreateOneDCode(message));
					} catch (WriterException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}

			}
		});
	}
	
	/**
     * 用于将给定的内容生成成一维码 注：目前生成内容为中文的话将直接报错，要修改底层jar包的内容
     *
     * @param content 将要生成�?��码的内容
     * @return 返回生成好的�?��码bitmap
     * @throws WriterException WriterException异常
     */
    public Bitmap CreateOneDCode(String content) throws WriterException {
        // 生成�?��条码,编码时指定大�?不要生成了图片以后再进行缩放,这样会模糊导致识别失�?
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.CODE_128, 500, 200);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参�?api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    
    
    
}
