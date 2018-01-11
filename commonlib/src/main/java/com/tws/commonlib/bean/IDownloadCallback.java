package com.tws.commonlib.bean;

/**
 * Created by Administrator on 2018/1/11.
 */

public interface IDownloadCallback {
    int DOWNLOAD_STATE_START = 0;
    int DOWNLOAD_STATE_DOWNLOADING = 1;
    int DOWNLOAD_STATE_END = 2;
    int DOWNLOAD_STATE_ERROR_PATH = 3;
    int DOWNLOAD_STATE_ERROR_DATA = 4;

    void callbackDownloadState(IMyCamera var1, int total, int curSize, int state, String path);
}