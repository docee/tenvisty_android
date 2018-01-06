package com.tutk.IOTC;

import android.graphics.Bitmap;

public interface IRegisterIOTCListener {

	void receiveFrameData(final NSCamera camera, final int avChannel, final Bitmap bmp);

	void receiveFrameInfo(final NSCamera camera, final int avChannel, final long bitRate, final int frameRate, final int onlineNm, final int frameCount,
                          final int incompleteFrameCount);

	void receiveSessionInfo(final NSCamera camera, final int resultCode);

	void receiveChannelInfo(final NSCamera camera, final int avChannel, final int resultCode);

	void receiveIOCtrlData(final NSCamera camera, final int avChannel, final int avIOCtrlMsgType, final byte[] data);
	
	void initSendAudio(Camera paramCamera, boolean paramBoolean);
	
	void receiveOriginalFrameData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3);
	
	void receiveRGBData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3);
	void receiveRecordingData(Camera paramCamera, final int avChannel, int paramInt1, String path);
}