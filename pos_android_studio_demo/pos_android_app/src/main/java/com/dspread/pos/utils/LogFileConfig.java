package com.dspread.pos.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.goldze.mvvmhabit.utils.SPUtils;

public class LogFileConfig {
    public  File logFileWR = null;

    private String FILE_DS_LOG = "ds_log";
    private String  PATH_DS_LOG= "/DSLogs/";
    private boolean writeFlag = true;
    private static Context mContext;

    private LogFileConfig() {
        LogFileInit(FILE_DS_LOG);
    }

    public void setWriteFlag(boolean writeFlag) {
        this.writeFlag = writeFlag;
    }

    public boolean getWriteFlag() {
        return writeFlag;
    }

    private static class LogFileConfigHolder {
        private static LogFileConfig config = new LogFileConfig();
    }


    public static LogFileConfig getInstance(Context context) {
        mContext = context;
        return LogFileConfigHolder.config;
    }

    private void LogFileInit( String fileName) {
        String model = Build.MODEL;
        String brand = Build.BRAND;

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yy_MM_d-HH:mm:ss");
        String filename = format.format(date);
        if ("".equals(fileName) || null == fileName) {
            filename = filename + "_" + brand + "_" + model + ".txt";
        } else {
            filename = fileName + "_" + filename + "_" + brand + "_" + model + ".txt";
        }

        logFileWR = createMyFile(filename);
    }

    public void writeLog(String str) {
        if (!writeFlag) {
            return;
        }
        if (logFileWR == null) {
            LogFileInit(FILE_DS_LOG);
            return;
        }
        try {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String logDate = format.format(date);

            str += "\r\n";
            str = logDate + "--" + str;
            DataOutputStream d = new DataOutputStream(new FileOutputStream(logFileWR, true));
            d.write(str.getBytes());
            d.flush();
            d.close();
            // Upload logs to Bugly
//            CrashReport.postCatchedException(new Exception(str));
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public String readLog(){
        InputStream inputStream = null;
        Reader reader = null;
        BufferedReader bufferedReader = null;
        try {

            File file= logFileWR;
            inputStream = new FileInputStream(file);
            reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            StringBuilder result = new StringBuilder();
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                result.append(temp);
            }

//            int maxLength = 512; // Bugly has a limit on the length of each data segment
//            int segments = (result.length() + maxLength - 1) / maxLength;
//
//            for (int i = 0; i < segments; i++) {
//                int start = i * maxLength;
//                int end = Math.min((i + 1) * maxLength, result.length());
//                String segment = result.substring(start, end);
//
//                // Upload log fragments
//                Map<String, String> map = new HashMap<>();
//                map.put("logFileName", file.getName());
//                map.put("segmentIndex", String.valueOf(i));
//                map.put("totalSegments", String.valueOf(segments));
//                map.put("logContent", segment);
//
////                CrashReport.putUserData(mContext,
////                        "customLog_" + SPUtils.getInstance().getString("posID")+"_"+file.getName() + "_" + i,
////                        JSON.toJSONString(map));
//            }
            BuglyLog.e(
                    file.getName(),
                    result.toString());
            CrashReport.putUserData(mContext,"POSID", SPUtils.getInstance().getString("posID"));

            // 2. 设置日志文件路径（Bugly会在崩溃时自动上传）
            // Set scene labels when payment
            CrashReport.setUserSceneTag(mContext, 90001);
            String uniqueID = java.util.UUID.randomUUID().toString() + "_"+ file.getName() + "_" + System.currentTimeMillis();
            TRACE.i( "uniqueID:" + uniqueID);
            CrashReport.putUserData(mContext, "log_uuid", uniqueID);
            // Trigger upload
            CrashReport.postCatchedException(
                        new BuglyCustomLogException("CustomLog: " +uniqueID));
            //  info 级别日志，Bugly 控制台能看到独立的日志记录
            TRACE.i( "result:" + result);
            deleteDir(file);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    private File createMyFile(String fileName) {
        File file = null;
        try {
            file = new File(mContext.getExternalFilesDir(null).getAbsolutePath() + File.separator + fileName);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (Exception e) {

        }
        Log.d("pos", "File Path：" + file.getAbsolutePath());

        return file;
    }



    /**
     * Recursively delete all files in the directory and all files in subdirectories
     * @param dir Directory of files to be deleted
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public  boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();

            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
            return dir.delete();
    }

    public boolean deleteAllFile() {
        File dir = new File(Environment.getExternalStorageDirectory(), PATH_DS_LOG);
        if (!dir.exists()) {
            return true;
        }
            String[] children = dir.list();
            if (children != null && children.length > 0) {
                for (int i=0; i<children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
            }
            }
        // The directory is currently empty and can be deleted
        return dir.delete();

    }

    public static byte[] readAssetsLine(String fileName, Context context) {
//        Tip.d("file name:"+fileName);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            android.content.ContextWrapper contextWrapper = new ContextWrapper(context);
            AssetManager assetManager = contextWrapper.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            byte[] data = new byte[512];
            int current = 0;
            while ((current = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        return buffer.toByteArray();
    }

}
