package com.tutk.IOTC;

/**
 * Created by Administrator on 2017/7/21.
 */


public class VideoPlayProperty {
    public static final int HI_P2P_STREAM_H265 = 5;
    public volatile int receiveChannel;
    public volatile int width;
    public volatile int heigth;
    public volatile int audioType;
    public volatile int streamType;
    public boolean isQosSetVideo = false;
    public volatile int qosSaveFrameTime = 0;
    public volatile int qosFrameCountInInterval = 0;
    public volatile int qosRecvBitCount = 0;
    public volatile boolean isListening = false;
    public volatile boolean isRecording = false;
    public String recordingPath = null;


    public void setProperty(int width, int height, int audio) {
        this.width = width;
        this.heigth = height;
        this.audioType = audio;
    }

    public void setProperty(byte[] data) {
        this.streamType = data[0] & 255;
        this.width = Packet.byteArrayToInt_Little(data, 4);
        this.heigth = Packet.byteArrayToInt_Little(data, 8);
        this.audioType = Packet.byteArrayToInt_Little(data, 12);
    }
}
