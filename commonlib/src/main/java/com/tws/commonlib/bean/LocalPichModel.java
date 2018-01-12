package com.tws.commonlib.bean;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/10/15.
 */

public class LocalPichModel {
    public  int type;
    public LocalPichModel(String path,int type) {
        this.type = type;
        this.path = path;
        if (isVideo()) {
            if(type == 1) {
                this.thumbPath = path + ".jpg";
            }
            else{
                this.thumbPath = path.substring(0,path.lastIndexOf(".mp4"))+".jpg";
            }
        } else {
            this.thumbPath = path;
        }
    }

    public String path;
    public boolean checked;
    public String thumbPath;


    public boolean isVideo() {
        return path != null && path.endsWith(".mp4");
    }
}
