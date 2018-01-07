package com.tws.commonlib.bean;

import android.graphics.Bitmap;

import com.tutk.IOTC.NSCamera;

/**
 * Created by Administrator on 2018/1/7.
 */

public interface IIOTCListener {
    void receiveFrameData(final IMyCamera camera, final int avChannel, final Bitmap bmp);

    void receiveFrameInfo(final IMyCamera camera, final int avChannel, final long bitRate, final int frameRate, final int onlineNm, final int frameCount,
                          final int incompleteFrameCount);

    void receiveSessionInfo(final IMyCamera camera, final int resultCode);

    void receiveChannelInfo(final IMyCamera camera, final int avChannel, final int resultCode);

    void receiveIOCtrlData(final IMyCamera camera, final int avChannel, final int avIOCtrlMsgType, final byte[] data);

    void initSendAudio(IMyCamera paramCamera, boolean paramBoolean);

    void receiveOriginalFrameData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3);

    void receiveRGBData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3);
    void receiveRecordingData(IMyCamera paramCamera, final int avChannel, int paramInt1, String path);
}
