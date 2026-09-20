package com.dspread.pos.dualScreen.manager;

/**
 * 操作结果回调接口
 */
public interface IResultCallback {
    void onSuccess();

    void onFailure(int code, String msg);
}
