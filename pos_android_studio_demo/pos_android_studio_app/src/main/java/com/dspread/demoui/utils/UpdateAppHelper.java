package com.dspread.demoui.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.xuexiang.xaop.annotation.Permission;
import com.xuexiang.xaop.consts.PermissionConsts;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate._XUpdate;
import com.xuexiang.xupdate.service.OnFileDownloadListener;
import com.xuexiang.xutil.app.PathUtils;
import com.xuexiang.xutil.display.HProgressDialogUtils;
import com.xuexiang.xutil.file.FileUtils;

import java.io.File;

public class UpdateAppHelper {


    @Permission(PermissionConsts.STORAGE)
    public static void useApkDownLoadFunction(Context mContext, String downloadUrl) {
        XUpdate.newBuild(mContext)
                // 注意在Android10及以上存在存储权限问题，不建议设置在外部存储下载目录
                .apkCacheDir(PathUtils.getAppExtCachePath())
                .build()
                .download(downloadUrl, new OnFileDownloadListener() {
                    @Override
                    public void onStart() {
                        deleteFile(new File(PathUtils.getAppExtCachePath()));
                        HProgressDialogUtils.showHorizontalProgressDialog(mContext, "Download Progress", false);
                    }

                    @Override
                    public void onProgress(float progress, long total) {
                        HProgressDialogUtils.setProgress(Math.round(progress * 100));
                    }

                    @Override
                    public boolean onCompleted(File file) {
                        TRACE.d("下载路径：" + FileUtils.getFileByPath(file.getPath()));
                        HProgressDialogUtils.cancel();
                        _XUpdate.startInstallApk(mContext, FileUtils.getFileByPath(file.getPath()));
                        return false;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        HProgressDialogUtils.cancel();
                    }
                });
    }



    public static Boolean deleteFile(File file) {
        //判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            return false;
        }
        //获取目录下子文件
        File[] files = file.listFiles();
        //遍历该目录下的文件对象
        for (File f : files) {
            //判断子目录是否存在子目录,如果是文件则删除
            if (f.isDirectory()) {
                //递归删除目录下的文件
                deleteFile(f);
            } else {
                //文件删除
                f.delete();
                //打印文件名
            }
        }
        return true;
    }
//VersionCode和VersionName的获取：

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getPackageVersionName(Context context, String pkgName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0); //PackageManager.GET_CONFIGURATIONS
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static int getPackageVersionCode(Context context, String pkgName) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
