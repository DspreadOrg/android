package com.dspread.pos.ui.setting.device_config;

public class DeviceConfigItem {
    private String code;
    private String name;
    private int flagResId;
    private int numericCode;
    private boolean selected;

    public DeviceConfigItem(String code, String name, int flagResId, int numericCode) {
        this.code = code;
        this.name = name;
        this.flagResId = flagResId;
        this.numericCode = numericCode;
    }

    // Getter and Setter methods
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getFlagResId() { return flagResId; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public int getNumericCode() { return numericCode; }
}