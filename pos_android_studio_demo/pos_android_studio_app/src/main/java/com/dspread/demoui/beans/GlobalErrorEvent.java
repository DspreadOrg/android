package com.dspread.demoui.beans;

import com.dspread.xpos.QPOSService;

/**
 * [一句话描述该类的功能]
 *
 * @author : [DH]
 * @createTime : [2024/11/7 14:27]
 * @updateRemark : [说明本次修改内容]
 */
public class GlobalErrorEvent {
    public final QPOSService.Error errorState;

    public GlobalErrorEvent(QPOSService.Error errorState) {
        this.errorState = errorState;
    }
}
