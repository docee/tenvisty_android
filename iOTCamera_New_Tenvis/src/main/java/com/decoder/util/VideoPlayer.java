package com.decoder.util;

import java.io.File;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.R.integer;
import android.graphics.Bitmap;
import android.util.Log;

import com.tutk.IOTC.Packet;

public class VideoPlayer {
    private int video_stream_index;
    static Lock lock = new ReentrantLock();

    private native int initWithVideo(int video_codec);

    private native int decode(byte[] video_data,
                              int lenth, int[] pictureParam, int video_stream_index);

    private native void getYuvData(byte[] yuvData, int video_stream_index);//video stream的标识

    private native int getBitmap(Bitmap bitmap, int video_stream_index);//video stream的标识

    private native void startRecord(String path, boolean isAudioPlay,
                                    boolean isVideoPlay);

    private native void addVideoFrame(byte[] video_data, int lenth,
                                      long tick);

    private native void addAudioFrame(byte[] video_data, int lenth,
                                      long tick);

    private native void pcmConvertMp2(byte[] video_data, int length);

    private native void stopRecord();

    private native void dealloc(int video_stream_index);

    private Bitmap bitmap = null;
    private byte[] yuvData = null;
    private int width, height;
    public static native int RecordMp4init(int width, int height, String videoPath);

    public static native int RecordMp4write(byte[] data, int length);

    public static native int RecordMp4deinit();
    static {
        try {
            System.loadLibrary("avutil-55");
            System.loadLibrary("avcodec-57");
            System.loadLibrary("swresample-2");
            System.loadLibrary("swscale-4");
            System.loadLibrary("avformat-57");
            System.loadLibrary("avfilter-6");
            System.loadLibrary("live-jni");
        }
        catch (Exception ex){
            System.out.println("loadLibrary(live-jni*)," + ex.getMessage());
        }
    }

    public VideoPlayer() {
        lock.lock();
        video_stream_index = initWithVideo(2);//2是H264：case 2:code_id = CODEC_ID_H264;
        System.out.println("video player init " + video_stream_index);
        lock.unlock();
    }

    //pictureParam一个长度为4的int数组，用来存储长宽信息
    public Bitmap decode(byte[] video_data, int[] pictureParam) {
        if (video_stream_index < 0) {
            return null;
        }
        int result = decode(video_data, video_data.length, pictureParam, video_stream_index);
        if (result > 0) {
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(pictureParam[2], pictureParam[3],
                        Bitmap.Config.RGB_565);
            }

            getBitmap(bitmap, video_stream_index);
        }

        return bitmap;
    }

    public Bitmap snapShot() {
        if(width == 0 || height == 0){
            return null;
        }
        if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
            if(bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
                System.gc();
                bitmap = null;
            }
            bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.RGB_565);
        }

        int i = getBitmap(bitmap, video_stream_index);

         return bitmap;
    }

    public byte[] decode2Yuv(byte[] video_data, int[] pictureParam, int[] result) {
        if (video_stream_index < 0) {
            return null;
        }
        result[0] = decode(video_data, video_data.length, pictureParam, video_stream_index);
        if (result[0] > 0) {
            if (yuvData == null || width != pictureParam[2] || height != pictureParam[3]) {
                width = pictureParam[2];
                height = pictureParam[3];
                yuvData = new byte[pictureParam[2] * pictureParam[3] * 3 / 2];
            }
            getYuvData(yuvData, video_stream_index);
        }
        return yuvData;
    }

    public void recordVideo(File file, boolean isAudioPlay, boolean isVideoPlay) {
        String filename = file.getAbsolutePath();
        startRecord(filename, isAudioPlay, isVideoPlay);
    }

    public void realese() {
        lock.lock();
        if (video_stream_index < 0) {
            return;
        }
        yuvData = null;
       // bitmap.recycle();
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
        dealloc(video_stream_index);
        System.out.println("VideoPlayer realese");
        lock.unlock();
    }

    public void stopRecordVideo() {
        stopRecord();
    }

    public void ConvertMp2(byte[] audio_data, int length) {
        pcmConvertMp2(audio_data, length);
    }

    public void writeAudioData(byte[] audio_data, int length, long tick) {
        addAudioFrame(audio_data, length, tick);
    }

    public void writeVideoData(byte[] video_data, int video_lengt, long tick) {
        addVideoFrame(video_data, video_lengt, tick);

    }

    void printHex(byte[] data, int length) {
        for (int i = 0; i < length; i++) {
            System.out.printf("%02x ", data[i]);
        }
        System.out.println(" ");
    }
}