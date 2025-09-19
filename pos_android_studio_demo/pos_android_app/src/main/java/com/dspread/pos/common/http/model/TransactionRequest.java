package com.dspread.pos.common.http.model;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @SerializedName("filter")
    private String filter;

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    @SerializedName("deviceSn")
    private String deviceSn;

    public TransactionRequest(String deviceSn, String filter) {
        this.deviceSn = deviceSn;
        this.filter = filter;
    }
}
