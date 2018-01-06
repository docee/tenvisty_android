package com.tutk.IOTC;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import android.R.integer;
import android.graphics.Bitmap;
import android.util.Log;

public class NSCamera {

    public enum CAMERA_MODEL {
        CAMERA_MODEL_H264,
        CAMERA_MODEL_MJPEG,
        CAMERA_MODEL_SJ
    }

    public static final int CONNECTION_STATE_NONE = 0;
    public static final int CONNECTION_STATE_CONNECTING = 1;
    public static final int CONNECTION_STATE_CONNECTED = 2;
    public static final int CONNECTION_STATE_DISCONNECTED = 3;
    public static final int CONNECTION_STATE_UNKNOWN_DEVICE = 4;
    public static final int CONNECTION_STATE_WRONG_PASSWORD = 5;
    public static final int CONNECTION_STATE_TIMEOUT = 6;
    public static final int CONNECTION_STATE_UNSUPPORTED = 7;
    public static final int CONNECTION_STATE_CONNECT_FAILED = 8;
    public static final int CONNECTION_STATE_SLEEPING = 9;
    public static final int CONNECTION_STATE_WAKINGUP = 10;

    public CAMERA_MODEL cameraModel;
    public String databaseId;
    public String uid;
    public String name;
    public String host;
    public String port;
    public String LANHost;
    public String LANPort;
    public String user;
    public String pwd;
    public String ddns;
    public String camera_own_status;//2:public 1:share 0:oneself
    public String shareFrom;
    public int pushNotificationStatus;//0:off 1:on

    public int connect_state;

    public int mTotalSize = 0;//设备SD卡存储大小

    protected List<IRegisterIOTCListener> mIOTCListeners = Collections.synchronizedList(new Vector<IRegisterIOTCListener>());
    protected ICameraYUVCallback mCameraYUVCallback = null;
    protected ICameraPlayStateCallback mCameraPlayStateCallback = null;
    public boolean registerIOTCListener(IRegisterIOTCListener listener) {
        boolean result = false;

        if (!mIOTCListeners.contains(listener)) {
            Log.i("NSCamera", "register IOTC listener");
            mIOTCListeners.add(listener);
            result = true;
        }

        return result;
    }

    public boolean unregisterIOTCListener(IRegisterIOTCListener listener) {
        boolean result = false;

        if (mIOTCListeners.contains(listener)) {
            Log.i("NSCamera", "unregister IOTC listener");
            mIOTCListeners.remove(listener);
            result = true;
        }

        return result;
    }
    public void registerYUVDataListener(ICameraYUVCallback callback) {
        this.mCameraYUVCallback = callback;
    }

    public void unregisterYUVDataListener(ICameraYUVCallback callback) {
        this.mCameraYUVCallback = null;
    }
    public void registerPlayStateListener(ICameraPlayStateCallback callback) {
        this.mCameraPlayStateCallback = callback;
    }

    public void unregisterPlayStateListener(ICameraPlayStateCallback callback) {
        this.mCameraPlayStateCallback = null;
    }
    public void start() {

    }

    public void stop() {

    }

    public void startVideo() {

    }

    public void stopVideo() {

    }

    public void startAudio() {

    }

    public void stopAudio() {

    }

    public void startSpeak() {

    }

    public void stopSpeak() {

    }

    public void ptz(int type) {

    }


    public Bitmap Snapshot() {
        return null;
    }

    public boolean getAudioInSupported(int avChannel) {
        return true;
    }

    public boolean getAudioOutSupported(int avChannel) {

        return true;
    }

    public int wakeUp() {
        return -1;
    }
}
