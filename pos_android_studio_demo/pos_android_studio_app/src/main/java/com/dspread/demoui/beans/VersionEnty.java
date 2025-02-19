package com.dspread.demoui.beans;

public class VersionEnty {

    private Object VersionCode;

    private Object VersionName;

    public String getBuildId() {
        return buildId;
    }

    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    private  String buildId;


    public Object getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(Object versionCode) {
        VersionCode = versionCode;
    }

    public Object getVersionName() {
        return VersionName;
    }

    public void setVersionName(Object versionName) {
        VersionName = versionName;
    }


    public Object getModifyContent() {
        return ModifyContent;
    }

    public void setModifyContent(Object modifyContent) {
        ModifyContent = modifyContent;
    }

    public String getDownloadUrl() {
        return DownloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        DownloadUrl = downloadUrl;
    }


    private Object ModifyContent;
    private String DownloadUrl;

    public int getApkSize() {
        return ApkSize;
    }

    public void setApkSize(int apkSize) {
        ApkSize = apkSize;
    }

    private int ApkSize;

    private int Code;

    public int getCode() {
        return Code;
    }

    public void setCode(int code) {
        Code = code;
    }

    public int getUpdateStatus() {
        return UpdateStatus;
    }

    public void setUpdateStatus(int updateStatus) {
        UpdateStatus = updateStatus;
    }

    private int UpdateStatus;
}
