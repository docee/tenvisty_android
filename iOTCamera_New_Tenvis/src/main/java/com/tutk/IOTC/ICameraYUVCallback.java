package com.tutk.IOTC;

/**
 * Created by Administrator on 2017/10/11.
 */

public interface ICameraYUVCallback {
    void callbackYUVData(Camera camera,int channel, byte[] yuvData, int length,int width,int height);
}
