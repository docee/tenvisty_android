package com.tws.commonlib.bean;

/**
 * Created by Administrator on 2018/1/7.
 */

public class TwsSessionState {
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

    public static final int CAMERA_CONNECTION_STATE_LOGIN = 11;
    public static final int CAMERA_CONNECTION_STATE_UIDERROR = 12;
    public static final int CAMERA_CHANNEL_STREAM_ERROR = 13;
    public static final int CAMERA_CHANNEL_CMD_ERROR = 14;
}
