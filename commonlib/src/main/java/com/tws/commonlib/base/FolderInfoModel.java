package com.tws.commonlib.base;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2017/10/16.
 */

public class FolderInfoModel {
    public String thumbPath;
    public String uid;
    public String cameraName;
    public int photoCount;
    public int videoCount;

    public FolderInfoModel(String _thumb, String _uid, String _cameraName, int _photoCount, int _videoCount) {
        this.thumbPath = _thumb;
        this.uid = _uid;
        this.cameraName = _cameraName;
        this.photoCount = _photoCount;
        this.videoCount = _videoCount;
    }
}
