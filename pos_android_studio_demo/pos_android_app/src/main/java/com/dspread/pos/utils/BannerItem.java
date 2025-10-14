package com.dspread.pos.utils;

public class BannerItem {
    private int imageResId; // image ID
    private String text;    // text

    public BannerItem(int imageResId, String text) {
        this.imageResId = imageResId;
        this.text = text;
    }

    // getter方法
    public int getImageResId() {
        return imageResId;
    }

    public String getText() {
        return text;
    }
}
