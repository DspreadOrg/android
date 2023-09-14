package com.dspread.demoui.activities.mp5801;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.activities.printer.PrintSettingActivity;
import com.dspread.demoui.utils.ConstantUtil;
import com.dspread.demoui.utils.PrintUtils;
import com.dspread.helper.printer.PrinterClass;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 打印文本类
 */
public class PrintTextActivity extends Activity {
    List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
    private EditText et_input;
    private CheckBox checkBoxAuto;
    private Button bt_print;
    private Thread autoprint_Thread;

    int times = 500;// Automatic print time interval
    boolean isPrint = true;
    String message = "";
    public static String LanguageStr = "GBK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_text);
        List<String> list = new ArrayList<>();
        isPrint = true;
        et_input = (EditText) findViewById(R.id.et_input_1);
//		et_input.setText(" اختبار الطباعة  12345678اختبار الطباعةاختبار الطباعةاختبار ة abcdefg");
        ((Button) findViewById(R.id.bt_status)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                message = et_input.getText().toString().trim();
                PrintSettingActivity.pl.printText(message);
                long pre = System.currentTimeMillis();
                byte[] readBuf = PrintSettingActivity.pl.getPrinterStatusForLong();
                long next = System.currentTimeMillis();
                long time = next - pre;
                Log.i("km test time:", time + "");
                if (readBuf == null) {
                    Toast.makeText(PrintTextActivity.this, "failed to get print status", Toast.LENGTH_SHORT).show();
                    return;
                }
                PrintUtils.checkPrintStatus(readBuf, PrintTextActivity.this);
            }
        });
//		 et_input.setText("อาเหวิน : สวัสดียามค");
//		MainActivity.pl.write(new byte[] { 0x1b, 0x23,0x23,0x43,0x44,0x54,0x59,0x02});

//		registerReceiver(mReceiver, makeFilter());

        bt_print = (Button) findViewById(R.id.bt_print);
        bt_print.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                message = et_input.getText().toString().trim();

                //0x1b, 0x74,0x15，表示选择中文简体打印模式
                //0x1b, 0x74,0x35,加泰隆语,丹麦语,德语,英语,西班牙语,芬兰语,法语,冰岛语,意大利语,荷兰语,挪威语,葡萄牙语,
//				                                                     印度尼西亚语,巴士克语,南非语,法罗语,马来语,斯瓦希里语,加里西亚语,瑞典语

//				MainActivity.pl.write(new byte[] { 0x1b, 0x74,0x39});
//				//右对齐
                PrintSettingActivity.pl.setAlignment(PrinterClass.Alignment.ALIGN_LEFT);
//				//字体加粗
//				PrintService.pl.write(new byte[] { 0x1b, 0x45,0x01});

//				PrintSettingActivity.pl.printText(message);
                PrintSettingActivity.pl.printText(ConstantUtil.TO_PRINT_CONTENT);
                PrintSettingActivity.pl.printText(message);
//				byte[] send = null;
//				try {
//					send = message.getBytes("UTF-8");
//					send = message.getBytes("GBK");
//				send = message.getBytes();
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				MainActivity.pl.write(send);
                PrintSettingActivity.pl.printText("\n");
                Log.e("tag", "" + et_input.getText().toString().trim());
                //0x1d, 0x0c 表示切换到下一黑标处（无黑标时切换一行）
//				MainActivity.pl.write(new byte[] { 0x1d, 0x0c });

                //0x1d, 0x0c 表示切换到下一黑标处（无黑标时切换一行）
                PrintSettingActivity.pl.moveNextBlackLocation();

            }
        });

        checkBoxAuto = (CheckBox) findViewById(R.id.checkBoxAuto);
        autoprint_Thread = new Thread() {
            public void run() {
                while (isPrint) {
                    if (checkBoxAuto.isChecked()) {
                        String message = et_input.getText().toString();

//						PrintSettingActivity.pl.printText(message);
                        PrintSettingActivity.pl.printText(ConstantUtil.TO_PRINT_CONTENT);
                        PrintSettingActivity.pl.printText("\n");
                        try {
                            Thread.sleep(times);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        autoprint_Thread.start();

    }

    public byte[] getText(String textStr) {

        byte[] send1;
        try {
            send1 = textStr.getBytes(LanguageStr);
        } catch (UnsupportedEncodingException var4) {
            send1 = textStr.getBytes();
        }

        return send1;
    }

    public boolean printText(String textStr) {
        return PrintSettingActivity.pl.write(this.getText(textStr));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Resources res = getResources();
        String[] cmdStr = res.getStringArray(R.array.cmd);
        for (int i = 0; i < cmdStr.length; i++) {
            String[] cmdArray = cmdStr[i].split(",");
            if (cmdArray.length == 2) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("title", cmdArray[0]);
                map.put("description", cmdArray[1]);
                menu.add(0, i, i, cmdArray[0]);
                listData.add(map);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        Map map = listData.get(item.getItemId());
        String cmd = map.get("description").toString();
        byte[] bt = PrintCmdActivity.hexStringToBytes(cmd);
        PrintSettingActivity.pl.write(bt);
        Toast toast = Toast.makeText(this, "Send Success", Toast.LENGTH_SHORT);
        toast.show();
        return false;
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        isPrint = false;
//		unregisterReceiver(mReceiver);
        super.onStop();
    }

    private static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        if (width > w) {
            float scaleWidth = ((float) w) / width;
            float scaleHeight = ((float) h) / height + 24;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleWidth);
            Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                    height, matrix, true);
            return resizedBitmap;
        } else {
            Bitmap resizedBitmap = Bitmap.createBitmap(w, height + 24,
                    Config.RGB_565);
            Canvas canvas = new Canvas(resizedBitmap);
            Paint paint = new Paint();
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, (w - width) / 2, 0, paint);
            return resizedBitmap;
        }
    }

}
