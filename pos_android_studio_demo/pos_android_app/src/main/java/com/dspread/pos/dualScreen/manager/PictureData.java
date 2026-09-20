package com.dspread.pos.dualScreen.manager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 图片显示数据
 *
 * <p>描述一张图片在副屏上的显示位置和尺寸。</p>
 */
public class PictureData implements Parcelable {
    private String path;
    private int startX;
    private int startY;
    private int width;
    private int height;

    public PictureData() {
    }

    public PictureData(String path, int startX, int startY, int width, int height) {
        this.path = path;
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    protected PictureData(Parcel in) {
        path = in.readString();
        startX = in.readInt();
        startY = in.readInt();
        width = in.readInt();
        height = in.readInt();
    }

    public static final Creator<PictureData> CREATOR = new Creator<PictureData>() {
        @Override
        public PictureData createFromParcel(Parcel in) {
            return new PictureData(in);
        }

        @Override
        public PictureData[] newArray(int size) {
            return new PictureData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeInt(startX);
        dest.writeInt(startY);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
