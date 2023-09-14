package com.dspread.demoui.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dspread.helper.printer.PrintService;

import java.text.NumberFormat;

public class PrintUtils {

    public static void checkPrintStatus(byte[] readBuf, Context context) {

        if (readBuf[0] == 0x13) {
            PrintService.isFUll = true;
            ShowMsg("Status"+":"+"Buffer full",context);
        } else if (readBuf[0] == 0x11) {
            PrintService.isFUll = false;
            ShowMsg("Status"+":"+"Buffer null",context);
        } else if (readBuf[2] == 12) {
            ShowMsg("Status"+":"+"no paper",context);
        } else if (readBuf[2] == 00) {
            ShowMsg("Status"+":"+"has paper",context);
        }  else if (readBuf[0] == 0x04) {
            ShowMsg("Status"+":"+"High temperature",context);

        } else if (readBuf[0] == 0x02) {
            ShowMsg("Status"+":"+"Low power",context);

        }else {
            String readMessage = new String(readBuf, 0, readBuf.length);
            Log.e("", ""+readMessage);

            //计算公式 *3/8200
            if(readMessage.contains("current")){
                String[] str_vol=readMessage.split(":");

                int int_vol=Integer.valueOf(str_vol[1].trim());

                // 创建一个数值格式化对象
                NumberFormat numberFormat = NumberFormat.getInstance();
                // 设置精确到小数点后2位
                numberFormat.setMaximumFractionDigits(1);

                String result = numberFormat.format((float)int_vol*3/(float)8200*100);

                ShowMsg("Current voltage："+int_vol+",Percentage of electricity："+result+"%",context);

            }

            if(readMessage.contains("temp")){

                String[] str_vol=readMessage.split(":");

                int int_temp=Integer.valueOf(str_vol[1].trim());

                ShowMsg("Current Temperature："+int_temp+"摄氏度",context);

            }

            if (readMessage.contains("800"))// 80mm paper
            {
                PrintService.imageWidth = 72;
                Toast.makeText(context, "80mm",
                        Toast.LENGTH_SHORT).show();
                Log.e("", "imageWidth:"+"80mm");
            } else if (readMessage.contains("580"))// 58mm paper
            {
                PrintService.imageWidth = 48;
                Toast.makeText(context, "58mm",
                        Toast.LENGTH_SHORT).show();
                Log.e("", "imageWidth:"+"58mm");
            }
        }
    }

    private static void ShowMsg(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
