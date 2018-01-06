package com.tws.commonlib.bean;

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

    public boolean isVideo() {
        return path != null && path.contains(".mp4");
    }
}
