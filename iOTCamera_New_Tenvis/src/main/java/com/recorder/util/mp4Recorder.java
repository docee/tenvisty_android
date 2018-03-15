package com.recorder.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2018/3/8.
 */

public class mp4Recorder {
    private int video_stream_index;
    static Lock lock = new ReentrantLock();
    static {
        try {
            System.loadLibrary("fdk-aac");
            System.loadLibrary("mp4record");
        }catch (Exception ex){
            System.out.println("loadLibrary(mp4record*)," + ex.getMessage());
        }
    }
    public static native int initMp4(int width, int height, String videoPath);
    public static native int uninitMp4(int videoStreamIndex);
    public static native int writeData(byte[] data,int size,int frameType, int pts,int videoStreamIndex);

    public mp4Recorder(int width, int height, String videoPath) {
        lock.lock();
        video_stream_index = initMp4(width,height,videoPath);
        System.out.println("video player init " + video_stream_index);
        lock.unlock();
    }
    public void realese() {
        lock.lock();
        if (video_stream_index < 0) {
            return;
        }
        int ret = uninitMp4(video_stream_index);
        System.out.println("VideoPlayer realese");
        lock.unlock();
    }

    public  void writeData(byte[] data,int size,int frameType, int pts){
        try{
            writeData(data,size,frameType,pts,video_stream_index);
        }
        catch (Exception ex){

        }
    }

}
