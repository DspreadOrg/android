package com.dspread.demoui.beans;

public class VersionEnty {

    private int VersionCode;

    private String VersionName;
    private int Code;
    private String Msg;
    private int UpdateStatus;
    private String UploadTime;

    public int getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(int versionCode) {
        VersionCode = versionCode;
    }

    public String getVersionName() {
        return VersionName;
    }

    public void setVersionName(String versionName) {
        VersionName = versionName;
    }

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public int getUpdateStatus() {
        return UpdateStatus;
    }

    public void setUpdateStatus(int updateStatus) {
        UpdateStatus = updateStatus;
    }

    public String getUploadTime() {
        return UploadTime;
    }

    public void setUploadTime(String uploadTime) {
        UploadTime = uploadTime;
    }

    public String getModifyContent() {
        return ModifyContent;
    }

    public void setModifyContent(String modifyContent) {
        ModifyContent = modifyContent;
    }

    public String getDownloadUrl() {
        return DownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        DownloadUrl = downloadUrl;
    }

    public int getApkSize() {
        return ApkSize;
    }

    public void setApkSize(int apkSize) {
        ApkSize = apkSize;
    }

    public String getApkMd5() {
        return ApkMd5;
    }

    public void setApkMd5(String apkMd5) {
        ApkMd5 = apkMd5;
    }

    private String ModifyContent;
    private String DownloadUrl;
    private int ApkSize;
    private String ApkMd5;

}
