package com.dspread.pos.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
            return null;
        }

        return buffer.toByteArray();
    }


    /**
     * Get all file paths in the specified directory
     *
     * @param dirPath The directory to query
     */
    public static List<String> getAllFiles(String dirPath) {
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
//            Tip.d("路径不存在");
            f.mkdir();
            return null;
        }

        File[] files = f.listFiles();

        if (files == null) {//判断权限
            return null;
        }

        ArrayList fileList = new ArrayList();
        for (File _file : files) {//遍历目录
            if (_file.isFile()) {
                String _name = _file.getName();
//                String filePath = _file.getAbsolutePath();//获取文件路径
                String fileName = _file.getName();//获取文件名
//                fileList.add(filePath.concat(fileName));
                fileList.add(fileName);
            } else if (_file.isDirectory()) {//查询子目录
                getAllFiles(_file.getAbsolutePath());
            } else {
            }
        }
        return fileList;
    }

    //    public static void save(Context context,String data) {
//        FileOutputStream out = null;
//        BufferedWriter writer = null;
//        try {
//            out =context.openFileOutput("dspread",Context.MODE_APPEND);
//            writer = new BufferedWriter(new OutputStreamWriter(out));
//            writer.write(data);
//            writer.write("\r\n");
//            Tip.d("writing...");
//        } catch (IOException e){
//            e.printStackTrace();
//        } finally {
//            try {
//                if(writer != null){
//                    writer.close();
//                }
//            } catch (IOException e){
//                e.printStackTrace();
//            }
//        }
//    }
    public static void save(String data) {
        try {
//            Tip.d("saving.....");
            String FileName = "testresult.txt";
            File dictionaryFile = new File(POS_Storage_Dir);
            if (!dictionaryFile.exists()) {
                dictionaryFile.mkdir();
//                Tip.d("create new one file");
            }

            File textFile = new File(POS_Storage_Dir + FileName);
            if (!textFile.exists()) {
                textFile.createNewFile();
//                Tip.d("create new txt");
            }
//            Tip.d("done");
            RandomAccessFile raf = new RandomAccessFile(POS_Storage_Dir + FileName, "rwd");
            raf.seek(textFile.length());
            raf.write(data.getBytes());
            raf.write("\r\n".getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void saveFile(byte[] data, Context context) throws IOException {
        if (data != null) {
            File file = new File(context.getExternalFilesDir(null).getAbsolutePath() + File.separator + "firmware.txt");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedOutputStream outStream = null;
            try {
                outStream = new BufferedOutputStream(new FileOutputStream(file));
                outStream.write(data);
                outStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != outStream) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
