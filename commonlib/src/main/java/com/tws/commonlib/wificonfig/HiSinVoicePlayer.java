package com.tws.commonlib.wificonfig;

import android.content.Context;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.libra.sinvoice.BufferData;
import com.libra.sinvoice.BufferQueue;
import com.libra.sinvoice.Encoder;
import com.libra.sinvoice.LogHelper;
import com.libra.sinvoice.PcmPlayer;
import com.libra.sinvoice.SinVoicePlayer;
import com.tutk.IOTC.TwsThread;

/**
 * Created by Administrator on 2018/1/15.
 */

public class HiSinVoicePlayer implements com.libra.sinvoice.Encoder.Listener, Encoder.Callback, com.libra.sinvoice.PcmPlayer.Listener, com.libra.sinvoice.PcmPlayer.Callback {
    private static final String TAG = "SinVoicePlayer";
    private static final int STATE_START = 1;
    private static final int STATE_STOP = 2;
    private Encoder mEncoder;
    private PcmPlayer mPlayer;
    private BufferQueue mBufferQueue;
    private int mState;
    private SinVoicePlayer.Listener mListener;
    private TwsThread mPlayThread;
    private TwsThread mEncodeThread;
    private int mSampleRate;
    private int mBufferSize;
    private Handler mHanlder;
    public static final int BITS_8 = 128;
    public static final int BITS_16 = 32768;
    private static final int MSG_END = 2;

    public HiSinVoicePlayer() {
        this(32000, 3);
    }

    public HiSinVoicePlayer(int sampleRate, int buffCount) {
        this.mState = 2;
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, 4, 2);
        LogHelper.d("SinVoicePlayer", "AudioTrackMinBufferSize: " + bufferSize + "  sampleRate:" + sampleRate);
        this.mBufferQueue = new BufferQueue(buffCount, bufferSize);
        this.mSampleRate = sampleRate;
        this.mBufferSize = bufferSize;
        this.mEncoder = new Encoder(this);
        this.mEncoder.setListener(this);
        this.mPlayer = new PcmPlayer(this, sampleRate, 4, 2, bufferSize);
        this.mPlayer.setListener(this);
        this.mHanlder = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case 2:
                        HiSinVoicePlayer.this.stop();
                    default:
                }
            }
        };
    }

    public void init(Context context) {
        this.mEncoder.init(context);
    }

    public void uninit() {
        this.mEncoder.uninit();
    }

    public void setListener(SinVoicePlayer.Listener listener) {
        this.mListener = listener;
    }

    public void play(String text, boolean repeat, int muteInterval) {
        if(text != null) {
            int tokenLen = text.length();
            if(tokenLen > 0) {
                int[] tokens = new int[tokenLen];

                int i;
                for(i = 0; i < tokenLen; ++i) {
                    int index = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ@_~!#$%^&*,:;./\\[]{}<>|`+-=\"".indexOf(text.charAt(i));
                    if(index <= -1) {
                        break;
                    }

                    tokens[i] = index;
                }

                if(i >= tokenLen) {
                    this.play(tokens, tokenLen, repeat, muteInterval);
                }
            }
        }

    }

    public void play(int[] tokens, int tokenLen, String text) {
        this.play(tokens, tokenLen, false, 0);
    }

    public void play(final int[] tokens, final int tokenLen, final boolean repeat, final int muteInterval) {
        if(2 == this.mState) {
            LogHelper.d("SinVoicePlayer", "play start");
            this.mState = 1;
            this.mBufferQueue.set();
            this.mPlayThread = new TwsThread() {
                public void run() {
                    HiSinVoicePlayer.this.mPlayer.start();
                }
                @Override
                public void stopThread(){
                    HiSinVoicePlayer.this.mPlayer.stop();
                }
            };
            if(this.mPlayThread != null) {
                this.mPlayThread.startThread();
            }
            this.mEncodeThread = new TwsThread() {
                public void run() {
                    Log.e("hichip", "mBufferSize:" + HiSinVoicePlayer.this.mBufferSize);
                    HiSinVoicePlayer.this.mEncoder.start(HiSinVoicePlayer.this.mSampleRate, HiSinVoicePlayer.this.mBufferSize, tokens, tokenLen, muteInterval, repeat);
                }
                @Override
                public void stopThread(){
                    HiSinVoicePlayer.this.mEncoder.stop();
                }
            };
            if(this.mEncodeThread != null) {
                this.mEncodeThread.startThread();
                LogHelper.d("gujicheng", "encode start");
            }

            LogHelper.d("SinVoicePlayer", "play end");
            if(this.mListener != null) {
                this.mListener.onSinVoicePlayStart();
            }
        }

    }

    public void stop() {
        if(1 == this.mState) {
            this.mState = 2;
            LogHelper.d("SinVoicePlayer", "stop start");
            this.mEncoder.stop();
            this.mPlayer.stop();
            this.mBufferQueue.reset();
            if(this.mPlayThread != null) {
                this.mPlayThread.stopThread();
//                try {
//                    LogHelper.d("SinVoicePlayer", "wait for player thread exit");
//                    this.mPlayThread.join();
//                } catch (InterruptedException var3) {
//                    var3.printStackTrace();
//                }

                this.mPlayThread = null;
            }

            if(this.mEncodeThread != null) {
//                try {
//                    LogHelper.d("SinVoicePlayer", "wait for encode thread exit");
//                    this.mEncodeThread.join();
//                } catch (InterruptedException var2) {
//                    var2.printStackTrace();
//                }
                this.mEncodeThread.stopThread();
                this.mEncodeThread = null;
            }

            if(this.mListener != null) {
                this.mListener.onSinVoicePlayEnd();
            }

            LogHelper.d("SinVoicePlayer", "stop end");
        }

    }

    public void freeEncodeBuffer(BufferData buffer) {
        this.mBufferQueue.putFull(buffer);
    }

    public BufferData getEncodeBuffer() {
        return this.mBufferQueue.getEmpty();
    }

    public BufferData getPlayBuffer() {
        return this.mBufferQueue.getFull();
    }

    public void freePlayData(BufferData data) {
        this.mBufferQueue.putEmpty(data);
    }

    public void onPlayStart() {
        LogHelper.d("SinVoicePlayer", "onPlayStart");
    }

    public void onPlayStop() {
        LogHelper.d("SinVoicePlayer", "onPlayStop");
        this.mHanlder.sendEmptyMessage(2);
    }

    public void onBeginEncode() {
        LogHelper.d("SinVoicePlayer", "onBeginGen");
    }

    public void onFinishEncode() {
        LogHelper.d("SinVoicePlayer", "onFinishGen");
    }

    public void onStartEncode() {
        LogHelper.d("SinVoicePlayer", "onStartGen");
    }

    public void onEndEncode() {
        LogHelper.d("SinVoicePlayer", "onEndcode End");
    }

    public void onSinToken(int[] tokens) {
        if(this.mListener != null && tokens != null) {
            LogHelper.d("SinVoicePlayer", "onSinToken " + tokens.length);
            this.mListener.onSinToken(tokens);
        }

    }

    public interface Listener {
        void onSinVoicePlayStart();

        void onSinVoicePlayEnd();

        void onSinToken(int[] var1);
    }
}
