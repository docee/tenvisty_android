package com.tws.commonlib.bean;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/10/15.
 */

public class LocalPichModel {
    public LocalPichModel(String path) {
        this.path = path;
        if (isVideo()) {
            this.thumbPath = path + ".jpg";
        } else {
            this.thumbPath = path;
        }
    }

    public String path;
    public boolean checked;
    public String thumbPath;

    public Bitmap getThumbBmp() {
        return thumbBmp;
    }

    public void setThumbBmp(Bitmap thumbBmp) {
        if(this.thumbBmp != null && !this.thumbBmp.isRecycled()){
            this.thumbBmp.recycle();
            this.thumbBmp = null;
            System.gc();
        }
        this.thumbBmp = thumbBmp;
    }

    private Bitmap thumbBmp;

    public boolean isVideo() {
        return path != null && path.contains(".mp4");
    }
}
