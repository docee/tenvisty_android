package com.tws.commonlib.bean;

/**
 * Created by Administrator on 2018/1/7.
 */

public interface IPlayStateListener {
    int PLAY_STATE_START = 0;
    int PLAY_STATE_EDN = 1;
    int PLAY_STATE_POS = 2;
    int PLAY_STATE_RECORDING_START = 3;
    int PLAY_STATE_RECORDING_END = 4;
    int PLAY_STATE_RECORD_ERROR = 5;

    void callbackState(IMyCamera camera,int channel, int state, int w, int h);

    void callbackPlayUTC(IMyCamera var1, int var2);
}
