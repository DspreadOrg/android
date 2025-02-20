package com.dspread.demoui.utils;

import static com.xuexiang.xutil.XUtil.getContentResolver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dsppc11 on 2019/3/21.
 */

public class FileUtils {

    public static final String POS_Storage_Dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dspread" + File.separator;


    public static byte[] readLine(String fileName) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileInputStream fis = null;

        try {

            File file = new File(POS_Storage_Dir + fileName);

            fis = new FileInputStream(file);

            byte[] data = new byte[50];
            int current = 0;
            while ((current = fis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }


        } catch (Exception ex) {

            ex.printStackTrace();

            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return buffer.toByteArray();
    }


    public static byte[] readAssetsLine(String fileName, Context context) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            AssetManager assetManager = contextWrapper.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            byte[] data = new byte[512];
            int current = 0;
            while ((current = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
        return buffer.toByteArray();

    }

    public static String readAssetsLineAsString(String fileName, Context context) {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            AssetManager assetManager = contextWrapper.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            byte[] data = new byte[512];
            int current = 0;
            while ((current = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return  "";
        }
        return buffer.toString();

    }


    /**
     * Get all file paths in the specified directory
     *
     * @param dirPath Directory of files to be queried
     */
    public static List<String> getAllFiles(String dirPath) {
        File f = new File(dirPath);
        if (!f.exists()) {//judge whether the path exists
            f.mkdir();
            return null;
        }

        File[] files = f.listFiles();

        if (files == null) {//judge permission
            return null;
        }

        ArrayList fileList = new ArrayList();
        for (File _file : files) {//traverse directory
            if (_file.isFile()) {
                String _name = _file.getName();
//                String filePath = _file.getAbsolutePath();//get file path
                String fileName = _file.getName();//get file name
//                fileList.add(filePath.concat(fileName));
                fileList.add(fileName);
            } else if (_file.isDirectory()) {//query subdirectory
                getAllFiles(_file.getAbsolutePath());
            } else {
            }
        }
        return fileList;
    }

    public static byte[] readBytesFromUri(Uri uri) {
        try {
            // 通过 ContentResolver 打开文件输入流
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[4 * 1024]; // 4KB 缓冲区
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                inputStream.close();
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
