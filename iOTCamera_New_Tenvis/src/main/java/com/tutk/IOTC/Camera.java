package com.tutk.IOTC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.decoder.util.DecADPCM;
import com.decoder.util.DecEncG711;
import com.decoder.util.DecG726;
import com.decoder.util.DecMp3;
import com.decoder.util.DecSpeex;
import com.decoder.util.VideoPlayer;
import com.encoder.util.EncADPCM;
import com.encoder.util.EncG726;
import com.encoder.util.EncSpeex;
//import com.hichip.coder.H264Decoder;
//import com.hichip.coder.HiCoderBitmap;
import com.hichip.coder.EncMp4;
import com.misc.RefInteger;
import com.misc.objc.CFRunLoop;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlAVStream;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlCurrentFlowInfo;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetFlowInfoResp;

import static java.lang.Thread.State.TERMINATED;

public class Camera extends NSCamera {
    AVIGeneratorH mAVIRecorder = null;


    public static final String DEFAULT_FILENAME_LOG = "IOTCamera_log.txt";
    public static final String strCLCF = "\r\n";
    protected static String strSDPath = null;

    private static volatile int mCameraCount = 0;
    private static int mDefaultMaxCameraLimit = 4;

    public static final int DEFAULT_AV_CHANNEL = 0;

    private final Object mWaitObjectForConnected = new Object();

    private ThreadConnectDev mThreadConnectDev = null;
    private ThreadCheckDevStatus mThreadChkDevStatus = null;
    private ThreadSendAudio mThreadSendAudio = null;

    private volatile int nGet_SID = -1;
    private volatile int mSID = -1;
    private volatile int mSessionMode = -1;
    private volatile int[] bResend = new int[1];
    private volatile int nRecvFrmPreSec;
    private volatile int nDispFrmPreSec;
    private volatile int tempAvIndex = -1;


    private boolean mInitAudio = false;
    private AudioTrack mAudioTrack = null;
    private int mCamIndex = 0;
    private HiGLMonitor mMonitor = null;
    /* camera info */
    private String mDevUID;
    private String mDevPwd;
    public static int nFlow_total_FPS_count = 0;
    public static int nFlow_total_FPS_count_noClear = 0;

    protected List<AVChannel> mAVChannels = Collections.synchronizedList(new Vector<AVChannel>());

    public Camera() {
        mDevUID = "";
        mDevPwd = "";
        strSDPath = Environment.getExternalStorageDirectory().toString();
    }

    //暂定音/视频 解码
    public void pausePlay(int avChannel) {
        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (ch.getChannel() == avChannel) {
                    ch.setPausePlay(true);
                    break;
                }
            }
        }
    }

    //暂定音/视频 解码
    public void resumePlay(int avChannel) {
        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (ch.getChannel() == avChannel) {
                    ch.setPausePlay(false);
                    break;
                }
            }
        }
    }


    public int getSessionMode() {
        return mSessionMode;
    }

    public int getMSID() {
        return mSID;
    }

    public int gettempAvIndex() {
        return tempAvIndex;
    }

    public int getbResend() {
        return bResend[0];
    }

    public int getRecvFrmPreSec() {
        return nRecvFrmPreSec;
    }

    public int getDispFrmPreSec() {
        return nDispFrmPreSec;
    }

    public long getChannelServiceType(int avChannel) {
        long ret = 0;
        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (ch.getChannel() == avChannel) {
                    ret = ch.getServiceType();
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 搜索内网的设备
     *
     * @return
     */
    public synchronized static st_LanSearchInfo[] SearchLAN() {

        int num[] = new int[1];
        int version[] = new int[4];
        IOTCAPIs.IOTC_Get_Version(version);
        st_LanSearchInfo[] result = null;

        result = IOTCAPIs.IOTC_Lan_Search(num, 2000);
//        st_LanSearchInfo2[] aa = IOTCAPIs.IOTC_Lan_Search2(num, 2000);
//        aa = IOTCAPIs.IOTC_Lan_Search2_Ex(num, 2000,1000);

//		result = IOTCAPIs.SA(num, 0xFD86AA1C);

        return result;
    }

    public synchronized static st_LanSearchInfo[] SearchLAN(int waitTimeMs) {

        int num[] = new int[1];
        st_LanSearchInfo[] result = null;

        result = IOTCAPIs.IOTC_Lan_Search(num, waitTimeMs);

        return result;
    }

    /**
     * 设置最大的设备数目限制
     *
     * @param limit
     */
    public static void setMaxCameraLimit(int limit) {
        mDefaultMaxCameraLimit = limit;
    }

    /**
     * 初始化IOTC和AV
     * nRet = IOTCAPIs.IOTC_Initialize2(port);
     * <p>
     * nRet = AVAPIs.avInitialize(mDefaultMaxCameraLimit * 16);
     */
    public synchronized static int init() {
        int nRet = 0;
        if (mCameraCount == 0) {
            int port = (int) (10000 + (System.currentTimeMillis() % 10000));

            // nRet = IOTCAPIs.ialize(port, "50.19.254.134", "122.248.234.207", "m4.iotcplatform.com", "m5.iotcplatform.com");
            /**
             * 必须使用IOTC相关函数前调用（IOTC_Set_Max_Session_Number()除外）
             * port:制定随机的UDP端口号
             * return：=0成功，<0失败（4种值）
             */
            IOTCAPIs.IOTC_Setup_Session_Alive_Timeout(30);
            //IOTCAPIs.IOTC_Setup_DetectNetwork_Timeout(1000);
            nRet = IOTCAPIs.IOTC_Initialize2(port);
            //IOTCAPIs.IOTC_Setup_LANConnection_Timeout(500);
            //IOTCAPIs.IOTC_Setup_P2PConnection_Timeout(1000);

            L.i("IOTCamera", "IOTC_Initialize2() returns " + nRet);

            if (nRet < 0) {
                return nRet;
            }

            /**
             * Initialize AV module
             * 必须使用AV相关函数前调用
             * param:the max number of AV channels.如果小于1将会置1
             * return：实际设置的最大AV channels的数目
             */
            nRet = AVAPIs.avInitialize(mDefaultMaxCameraLimit * 16);
            Log.i("IOTCamera", "avInitialize() = " + nRet);

            if (nRet < 0) {
                return nRet;
            }
        }

        mCameraCount++;
        return nRet;
    }

    /**
     * @return
     */
    public synchronized static int uninit() {

        int nRet = 0;

        if (mCameraCount > 0) {
            mCameraCount--;

            if (mCameraCount == 0) {
                /**
                 * deinitialize AV module(必须在IOTCAPIs.IOTC_DeInitialize()前操作)
                 */
                nRet = AVAPIs.avDeInitialize();
                L.i("IOTCamera", "avDeInitialize() returns " + nRet);
                /**
                 * deinitialize IOTC module
                 */
                nRet = IOTCAPIs.IOTC_DeInitialize();
                L.i("IOTCamera", "IOTC_DeInitialize() returns " + nRet);
            }
        }

        return nRet;
    }

    /**
     * 判断会话是否连接
     *
     * @return
     */
    public boolean isSessionConnected() {
        return mSID >= 0;
    }

    /**
     * 判断通道是否可用
     *
     * @param avChannel
     * @return
     */
    public boolean isChannelConnected(int avChannel) {

        boolean result = false;

        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (avChannel == ch.getChannel()) {
                    result = mSID >= 0 && ch.getAVIndex() >= 0;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 发送IOTC相关请求
     *
     * @param avChannel
     * @param type
     * @param data
     */
    public void sendIOCtrl(int avChannel, int type, byte[] data) {

        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (avChannel == ch.getChannel()) {
                    ch.IOCtrlQueue.Enqueue(type, data);
                }
            }
        }
    }

    /**
     * 连接此UID，判断该UID是否合法可用
     *
     * @param uid ThreadConnectDev(0);
     *            ThreadCheckDevStatus();
     */
    public void connect(String uid) {

        mDevUID = uid;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(0);
            mThreadConnectDev.startThread();
        }

        if (mThreadChkDevStatus == null) {
            mThreadChkDevStatus = new ThreadCheckDevStatus();
            L.i("IOTCamera", uid + " ThreadCheckDevStatus ");
            mThreadChkDevStatus.startThread();
        }
    }

    /**
     * 连接此UID
     * mThreadConnectDev = new ThreadConnectDev(1);
     * mThreadChkDevStatus = new ThreadCheckDevStatus();
     *
     * @param uid
     * @param pwd
     */
    public void connect(String uid, String pwd) {
        mDevUID = uid;
        mDevPwd = pwd;

        if (mThreadConnectDev == null) {
            mThreadConnectDev = new ThreadConnectDev(1);
            mThreadConnectDev.startThread();
        }

        if (mThreadChkDevStatus == null) {
            mThreadChkDevStatus = new ThreadCheckDevStatus();
            L.i("IOTCamera", uid + " ThreadCheckDevStatus ");
            mThreadChkDevStatus.startThread();
        }
    }

    /**
     * 关闭此连接，清除相关通道，关闭会话
     * AVAPIs.avClientStop(ch.getAVIndex());
     * IOTCAPIs.IOTC_Session_Close(mSID);
     */
    public void disconnect() {
        L.i("IOTCamera", uid + " disconnect 关闭此连接，清除相关通道，关闭会话");
        synchronized (mAVChannels) {

            for (AVChannel ch : mAVChannels) {
                stop(ch);
            }
        }

        mAVChannels.clear();

        synchronized (mWaitObjectForConnected) {
            mWaitObjectForConnected.notify();
        }

        if (mThreadChkDevStatus != null) {
            mThreadChkDevStatus.stopThread();
            mThreadChkDevStatus = null;
        }
        if (mThreadConnectDev != null) {
            mThreadConnectDev.stopThread();
            mThreadConnectDev = null;
        }

        if (mSID >= 0) {

            /**
             * close a IOTC session
             */
            IOTCAPIs.IOTC_Session_Close(mSID);
            L.i("IOTCamera", "IOTC_Session_Close(nSID = " + mSID + ")");
            mSID = -1;
        }

        mSessionMode = -1;

    }

    /**
     * 摄像机启动
     *
     * @param avChannel
     * @param viewAccount
     * @param viewPasswd  AVChannel ch = new AVChannel(avChannel, viewAccount, viewPasswd);
     *                    ThreadStartDev(ch);
     *                    ThreadRecvIOCtrl(ch);
     *                    ThreadSendIOCtrl(ch);
     */
    public void startChannel(int avChannel, String viewAccount, String viewPasswd) {

        AVChannel session = null;

        synchronized (mAVChannels) {
            for (AVChannel ch : mAVChannels) {
                if (ch.getChannel() == avChannel) {
                    session = ch;
                    break;
                }
            }
        }

        if (session == null) {
            L.i("IOTCamera", uid + " startChannel 摄像机启动");
            AVChannel ch = new AVChannel(avChannel, viewAccount, viewPasswd);
            mAVChannels.add(ch);

            ch.threadStartDev = new ThreadStartDev(ch);
            ch.threadStartDev.startThread();

            ch.threadRecvIOCtrl = new ThreadRecvIOCtrl(ch);
            ch.threadRecvIOCtrl.startThread();

            ch.threadSendIOCtrl = new ThreadSendIOCtrl(ch);
            ch.threadSendIOCtrl.startThread();

        } else {

            if (session.threadStartDev == null) {
                session.threadStartDev = new ThreadStartDev(session);
                session.threadStartDev.startThread();
            }

            if (session.threadRecvIOCtrl == null) {
                session.threadRecvIOCtrl = new ThreadRecvIOCtrl(session);
                session.threadRecvIOCtrl.startThread();
            }

            if (session.threadSendIOCtrl == null) {
                session.threadSendIOCtrl = new ThreadSendIOCtrl(session);
                session.threadSendIOCtrl.startThread();
            }
        }
    }

    /**
     * 关闭该通道
     * AVAPIs.avClientStop(ch.getAVIndex());
     *
     * @param avChannel
     */
    public void stop(int avChannel) {

        L.i("IOTCamera", uid + " stop 关闭该通道");
        synchronized (mAVChannels) {

            int idx = -1;

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    idx = i;
                    stop(ch);
                    break;
                }
            }

            if (idx >= 0) {
                mAVChannels.remove(idx);
            }
        }
    }

    private void stop(AVChannel ch) {
        if (mThreadSendAudio != null) {
            mThreadSendAudio.stopThread();
        }
        // stopSpeaking(ch.getChannel());

        if (ch.threadRecording != null) {
            ch.threadRecording.stopThread();
        }
        if (ch.threadDecAudio != null) {
            ch.threadDecAudio.stopThread();
        }

        if (ch.threadDecVideo != null) {
            ch.threadDecVideo.stopThread();
        }

        if (ch.threadRecvAudio != null) {
            ch.threadRecvAudio.stopThread();
        }

        if (ch.threadRecvVideo != null) {
            ch.threadRecvVideo.stopThread();
        }

        if (ch.threadRecvIOCtrl != null) {
            ch.threadRecvIOCtrl.stopThread();
        }

        if (ch.threadSendIOCtrl != null) {
            ch.threadSendIOCtrl.stopThread();
        }

        if (ch.threadStartDev != null) {
            ch.threadStartDev.stopThread();
        }

        ch.RecordFrameQueue.removeAll();
        if (ch.RecordFirstFrame != null && !ch.RecordFirstFrame.isRecycled()) {
            ch.RecordFirstFrame.recycle();
        }
        ch.RecordFirstFrame = null;
        ch.AudioFrameQueue.removeAll();

        ch.VideoFrameQueue.removeAll();
        ch.RecordFrameTempQueue.removeAll();
        ch.IOCtrlQueue.removeAll();

        while ((ch.threadStartDev != null && ch.threadStartDev.getState() != TERMINATED)
                || (ch.threadSendIOCtrl.getState() != TERMINATED)
                || (ch.threadSendIOCtrl != null && ch.threadRecvIOCtrl.getState() != TERMINATED)
                || (ch.threadRecvVideo != null && ch.threadRecvVideo.getState() != TERMINATED)
                || (ch.threadRecvAudio != null && ch.threadRecvAudio.getState() != TERMINATED)
                || (ch.threadDecVideo != null && ch.threadDecVideo.getState() != TERMINATED)
                || (ch.threadDecAudio != null && ch.threadDecAudio.getState() != TERMINATED)
                || (ch.threadRecording != null && ch.threadRecording.getState() != TERMINATED)
                || (mThreadSendAudio != null && mThreadSendAudio.getState() != TERMINATED)) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ch.threadStartDev = null;
        ch.threadSendIOCtrl = null;
        ch.threadRecvIOCtrl = null;
        ch.threadRecvVideo = null;
        ch.threadRecvAudio = null;
        ch.threadDecVideo = null;
        ch.threadDecAudio = null;
        ch.threadRecording = null;
        mThreadSendAudio = null;
        if (ch.getAVIndex() >= 0) {

            /**
             * stop一个不再使用的通道
             */
            AVAPIs.avClientStop(ch.getAVIndex());
            L.i("IOTCamera", "avClientStop(avIndex = " + ch.getAVIndex() + ")");
        }
    }

    /**
     * 开始接收视频数据并进行解码
     *
     * @param avChannel
     */
    public void startShow(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    if (ch.threadRecvVideo == null) {
                        ch.VideoFrameQueue.removeAll();

                        ch.RecordFrameTempQueue.removeAll();
                        ch.threadRecvVideo = new ThreadRecvVideo2(ch);
                        ch.threadRecvVideo.startThread();
                    }

                    if (ch.threadDecVideo == null) {
                        ch.threadDecVideo = new ThreadDecodeVideo3(ch);
                        ch.threadDecVideo.startThread();
                    }

                    break;
                }
            }
        }
    }

    /**
     * 结束视频数据接收线程
     * 结束视频数据解码线程
     * 清除队列中的视频数据
     *
     * @param avChannel
     */
    public void stopShow(int avChannel) {

        L.i("IOTCamera", uid + " stopShow 结束视频数据接收线程");
        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {
                    if (ch.threadRecording != null) {
                        ch.threadRecording.stopThread();
//                        try {
//                            ch.threadRecording.interrupt();
//                            ch.threadRecording.join();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                    if (ch.threadRecvVideo != null) {
                        ch.threadRecvVideo.stopThread();
                    }

                    if (ch.threadDecVideo != null) {
                        ch.threadDecVideo.stopThread();
//                        try {
//                            ch.threadDecVideo.interrupt();
//                            ch.threadDecVideo.join();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }

                    ch.VideoFrameQueue.removeAll();
                    ch.RecordFrameQueue.removeAll();
                    ch.RecordFrameTempQueue.removeAll();
                    if (ch.RecordFirstFrame != null && !ch.RecordFirstFrame.isRecycled()) {
                        ch.RecordFirstFrame.recycle();
                    }
                    ch.RecordFirstFrame = null;
                    while ((ch.threadRecvVideo != null && ch.threadRecvVideo.getState() != TERMINATED)
                            || (ch.threadDecVideo != null && ch.threadDecVideo.getState() != TERMINATED)
                            || (ch.threadRecording != null && ch.threadRecording.getState() != TERMINATED)) {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ch.threadRecvVideo = null;
                    ch.threadDecVideo = null;
                    ch.threadRecording = null;
                    break;
                }
            }
        }
    }

    /**
     * 开启通话
     *
     * @param avChannel
     */
    public void startSpeaking(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == avChannel) {

                    ch.AudioFrameQueue.removeAll();

                    if (mThreadSendAudio == null) {
                        mThreadSendAudio = new ThreadSendAudio(ch);
                        mThreadSendAudio.startThread();
                    }

                    break;
                }
            }
        }
    }

    /**
     * 停止通话，将线程置空
     *
     * @param avChannel
     */
    public void stopSpeaking(int avChannel) {

        L.i("IOTCamera", uid + " stopSpeaking 停止通话，将线程置空");
        if (mThreadSendAudio != null) {
            mThreadSendAudio.stopThread();
            while (mThreadSendAudio.getState() != TERMINATED) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            try {
//                mThreadSendAudio.interrupt();
//                mThreadSendAudio.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            mThreadSendAudio = null;
        }
    }

    /**
     * 开启监听，接收音频数据
     *
     * @param avChannel
     */
    public void startListening(int avChannel) {

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {

                    ch.AudioFrameQueue.removeAll();

                    if (ch.threadRecvAudio == null) {
                        ch.threadRecvAudio = new ThreadRecvAudio(ch);
                        ch.threadRecvAudio.startThread();
                    }


                    if (ch.threadDecAudio == null) {
                        ch.threadDecAudio = new ThreadDecodeAudio(ch);
                        ch.threadDecAudio.startThread();
                    }


                    break;
                }
            }
        }
    }

    /**
     * 停止监听，停止解码音频，清空队列中的音频数据，将相关线程置空
     *
     * @param avChannel
     */
    public void stopListening(int avChannel) {

        L.i("IOTCamera", uid + " stopListening 停止监听，停止解码音频，清空队列中的音频数据，将相关线程置空");
        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {

                    if (ch.threadRecvAudio != null) {
                        ch.threadRecvAudio.stopThread();
                        ch.threadRecvAudio = null;
                    }

                    if (ch.threadDecAudio != null) {
                        ch.threadDecAudio.stopThread();
                        ch.threadDecAudio = null;
                    }

                    ch.AudioFrameQueue.removeAll();

                    break;
                }
            }
        }
    }

    /**
     * 开始录像
     *
     * @param filename
     * @param w        int 录像视频宽带
     * @param h        int 录像视频高度
     * @return
     */
    public void start_record(String filename, final int w, final int h) {
        L.i("IOTCamera", uid + " start_record 开始录像");
        synchronized (mAVChannels) {
            for (int i = 0; i < mAVChannels.size(); i++) {
                AVChannel ch = mAVChannels.get(i);

                if (ch.getChannel() == 0) {
                    if (ch.threadRecvVideo == null) {
                        ch.VideoFrameQueue.removeAll();

                        ch.threadRecvVideo = new ThreadRecvVideo2(ch);
                        ch.threadRecvVideo.startThread();
                    }

                    if (ch.threadDecVideo == null) {
                        ch.threadDecVideo = new ThreadDecodeVideo3(ch);
                        ch.threadDecVideo.startThread();
                    }
                    break;
                }
            }
        }

        final File file = new File(filename);
        CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {
            public void run() {
                mAVIRecorder = new AVIGeneratorH(file);//封装成avi

                mAVIRecorder.addVideoStream(w, h);

                if (mThreadSendAudio != null) {
                    mAVIRecorder.addTalkStream();
                }
                mAVIRecorder.addAudioStream();
                try {
                    mAVIRecorder.startAVI();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 结束录像
     */
    public void stop_record() {
        CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

            public void run() {
                if (mAVIRecorder != null)
                    try {
                        mAVIRecorder.finishAVI();
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                mAVIRecorder = null;
            }
        });
    }

    /**
     * 快照，返回最新的一帧图像
     */
    public Bitmap Snapshot() {
        int avChannel = 0;

        Bitmap result = null;

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {
                    if (ch.threadDecVideo != null && ch.threadDecVideo.videoPlayer != null) {
                        return ch.threadDecVideo.videoPlayer.snapShot();
                    }
                    break;
                }
            }
        }

        return result;
    }

    public Bitmap Snapshot(int avChannel) {
        Bitmap result = null;

        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {

                AVChannel ch = mAVChannels.get(i);

                if (avChannel == ch.getChannel()) {
                    if (ch.threadDecVideo != null && ch.threadDecVideo.videoPlayer != null && ch.threadDecVideo.isDecodeBuffer) {
                        return ch.threadDecVideo.videoPlayer.snapShot();
                    }
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 初始化音频解码器
     *
     * @param sampleRateInHz
     * @param channel
     * @param dataBit
     * @param codec_id
     * @return
     */
    private synchronized boolean audioDev_init(int sampleRateInHz, int channel, int dataBit, int codec_id) {

        L.i("IOTCamera", uid + " audioDev_init 初始化音频解码器");
        if (!mInitAudio) {

            int channelConfig = 2;
            int audioFormat = 2;
            int mMinBufSize = 0;

            channelConfig = (channel == AVFrame.AUDIO_CHANNEL_STERO) ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
            audioFormat = (dataBit == AVFrame.AUDIO_DATABITS_16) ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
            mMinBufSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

            if (mMinBufSize == AudioTrack.ERROR_BAD_VALUE || mMinBufSize == AudioTrack.ERROR)
                return false;

            try {

                mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, mMinBufSize, AudioTrack.MODE_STREAM);
                L.i("IOTCamera", "init AudioTrack with SampleRate:" + sampleRateInHz + " " + ((dataBit == AVFrame.AUDIO_DATABITS_16) ? String.valueOf(16) : String.valueOf(8)) + "bit " + (channel == AVFrame.AUDIO_CHANNEL_STERO ? "Stereo" : "Mono"));

            } catch (IllegalArgumentException iae) {

                iae.printStackTrace();
                return false; // return----------------------------------------
            }

            if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                DecSpeex.InitDecoder(sampleRateInHz);
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                int bit = (dataBit == AVFrame.AUDIO_DATABITS_16) ? 16 : 8;
                DecMp3.InitDecoder(sampleRateInHz, bit);
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || codec_id == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                DecADPCM.ResetDecoder();
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                DecG726.g726_dec_state_create((byte) DecG726.G726_16, DecG726.FORMAT_LINEAR);
            }

            mAudioTrack.setStereoVolume(1.0f, 1.0f);
            mAudioTrack.play();
            mInitAudio = true;

            return true;
        } else
            return false;
    }

    /**
     * 释放相关音频解码器
     *
     * @param codec_id
     */
    private synchronized void audioDev_stop(int codec_id) {
        L.i("IOTCamera", uid + " audioDev_stop 释放相关音频解码器");
        if (mInitAudio) {

            if (mAudioTrack != null) {
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            }

            if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                DecSpeex.UninitDecoder();
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                DecMp3.UninitDecoder();
            } else if (codec_id == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                DecG726.g726_dec_state_destroy();
            }

            mInitAudio = false;
        }
    }

    /**
     * 与该摄像机建立连接的线程（设备是否存在）
     *
     * @author Administrator
     */
    private class ThreadConnectDev extends TwsThread {

        private int mConnType = -1;
        private Object m_waitForStopConnectThread = new Object();

        /**
         * 链接类型
         *
         * @param connType
         */
        public ThreadConnectDev(int connType) {
            mConnType = connType;
        }

        @Override
        public void stopThread() {
            super.stopThread();
            if (nGet_SID == -1)
                IOTCAPIs.IOTC_Connect_Stop_BySID(nGet_SID);

            synchronized (m_waitForStopConnectThread) {
                m_waitForStopConnectThread.notify();
            }
        }

        public void run() {

            L.i("IOTCamera", uid + " ThreadConnectDev 开启连接摄像机");
            int nRetryForIOTC_Conn = 0;
            int waittimeout = 1000 * 20;

            long startTime = System.currentTimeMillis();
            //synchronized (mIOTCListeners) {
            for (int i = 0; i < mIOTCListeners.size(); i++) {
                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECTING);
            }
            // }
            while (isRunning && mSID < 0) {


                if (mConnType == 0) {//未传入密码
                    /**
                     * 用于客户端获取free session ID
                     * return：>0:为IOTC session ID
                     * 		   <0:客户端的IOTC session已达到最大值
                     */
                    nGet_SID = IOTCAPIs.IOTC_Get_SessionID();
                    Log.i("IOTCamera", "IOTC_Get_SessionID SID = " + nGet_SID);
                    if (nGet_SID >= 0) {
                        /**
                         * 用于客户端连接设备和绑定session ID
                         * UID：欲连接的UID
                         * SID：上面获取到的空闲的session ID
                         * return：>=0并且等于SID成功
                         *         <0 失败相关的Error code
                         */
                        mSID = IOTCAPIs.IOTC_Connect_ByUID_Parallel(mDevUID, nGet_SID);
                        Log.i("IOTCamera", "IOTC_Connect_ByUID_Parallel mSID = " + mSID);
                        nGet_SID = -1;
                    }
                } else if (mConnType == 1) {//传入密码
                    nGet_SID = IOTCAPIs.IOTC_Get_SessionID();
                    L.i("IOTCamera", "IOTC_Get_SessionID SID = " + nGet_SID);
                    if (nGet_SID >= 0) {
                        mSID = IOTCAPIs.IOTC_Connect_ByUID_Parallel(mDevUID, nGet_SID);
                        nGet_SID = -1;
                    }

                } else {
                    return;
                }

                long now = System.currentTimeMillis();
                boolean isTimeout = Math.abs(now - startTime) > waittimeout;
                if (mSID >= 0) {//存在该设备

                    St_SInfo stSInfo = new St_SInfo();
                    // synchronized (mIOTCListeners) {
//						for (int i = 0; i < mIOTCListeners.size(); i++) {
//							IRegisterIOTCListener listener = mIOTCListeners.get(i);
//							listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECTED);
//						}
                    // }
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.notify();
                    }
                } else if (mSID == IOTCAPIs.IOTC_ER_CONNECT_IS_CALLING) {

                    try {
                        synchronized (m_waitForStopConnectThread) {
                            m_waitForStopConnectThread.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if (mSID == IOTCAPIs.IOTC_ER_DEVICE_OFFLINE || mSID == IOTCAPIs.IOTC_ER_CAN_NOT_FIND_DEVICE) {
                    if (isTimeout) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_DISCONNECTED);
                        }
                        break;
                    } else {
                        try {
                            synchronized (m_waitForStopConnectThread) {
                                m_waitForStopConnectThread.wait(3000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (mSID == IOTCAPIs.IOTC_ER_TIMEOUT || mSID == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT) {

                    try {
                        synchronized (m_waitForStopConnectThread) {
                            m_waitForStopConnectThread.wait(3000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (mSID == IOTCAPIs.IOTC_ER_UNKNOWN_DEVICE || mSID == IOTCAPIs.IOTC_ER_UNLICENSE) {

                    Log.i("=mSID=", "mSID=" + mSID);
                    //synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_UNKNOWN_DEVICE);
                    }
                    break;
                    // }

                } else if (mSID == IOTCAPIs.IOTC_ER_DEVICE_NOT_SECURE_MODE ||
                        mSID == IOTCAPIs.IOTC_ER_DEVICE_SECURE_MODE) {

                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_UNSUPPORTED);
                    }
                    // }

                    break;
                } else if (mSID == IOTCAPIs.IOTC_ER_SLEEPING) {

                    //synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_SLEEPING);
                    }
                    // }

                    break;
                } else {
                    if (isTimeout) {
                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECT_FAILED);
                        }
                        // }
                        break;
                    } else {
                        try {
                            synchronized (m_waitForStopConnectThread) {
                                m_waitForStopConnectThread.wait(1000);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Math.abs(System.currentTimeMillis() - startTime) > waittimeout) {
                        break;
                    }
                }
            }

            L.i("IOTCamera", "===ThreadConnectDev exit===");
        }
    }

    /**
     * 启动设备的线程
     * avIndex = AVAPIs.avClientStart2(...)
     *
     * @author Administrator
     */
    private class ThreadStartDev extends TwsThread {
        private AVChannel mAVChannel;
        private Object mWaitObject = new Object();

        public ThreadStartDev(AVChannel channel) {
            mAVChannel = channel;
        }

        @Override
        public void stopThread() {
            super.stopThread();
            if (mSID >= 0) {
                L.i("IOTCamera", "avClientExit(" + mSID + ", " + mAVChannel.getChannel() + ")");
                /**
                 * 用于AV client退出avClientStart()进程
                 */
                AVAPIs.avClientExit(mSID, mAVChannel.getChannel());
            }

            synchronized (mWaitObject) {
                mWaitObject.notify();
            }
        }

        @Override
        public void run() {

            L.i("IOTCamera", uid + " ThreadStartDev 开启登录摄像机");
            // int nRetryForAVClientStart = 0;
            int avIndex = -1;

            while (isRunning) {

                if (mSID < 0) {

                    try {
                        synchronized (mWaitObjectForConnected) {
                            mWaitObjectForConnected.wait(100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    continue;
                }

                // synchronized (mIOTCListeners) {
                for (int i = 0; i < mIOTCListeners.size(); i++) {

                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_CONNECTING);
                }
                // }

                /**
                 * 设备所支持的功能
                 */
                int[] nServType = new int[1];
                nServType[0] = -1;


                // Log.i("IOTCamera", "avClientStart2(" + mSID + ")");
                // if (mAVChannel.getChannel() == 0) {
                avIndex = AVAPIs.avClientStart2(mSID, mAVChannel.getViewAcc(), mAVChannel.getViewPwd(), 30, nServType, mAVChannel.getChannel(), bResend);
                // } else {
                //avIndex = AVAPIs.avClientStart(mSID, mAVChannel.getViewAcc(), mAVChannel.getViewPwd(), 30, nServType, mAVChannel.getChannel());
                //   }
                /**
                 * 启动一个AV client
                 * nIOTCSessionID    [in] The session ID of the IOTC session to start AV client
                 * cszViewAccount    [in] The view account for authentication
                 * cszViewPassword   [in] The view password for authentication
                 * nTimeout          [in] The timeout for this function in unit of second Specify it as 0 will make this AV client try connection once and this process will exit
                 *                        immediately if not connection is unsuccessful.
                 * pnServType        [out] The user-defined service type set when an AV server starts. Can be NULL.
                 * nIOTCChannelID    [in] The channel ID of the channel to start AV client
                 * pnResend          [out] Set the re-send is enabled or not.
                 *
                 * return：AV channel ID if return value>=0
                 *         Error code if return value<0
                 */
                //avIndex = AVAPIs.avClientStart(mSID, mAVChannel.getViewAcc(), mAVChannel.getViewPwd(), 30, nServType, mAVChannel.getChannel());

                tempAvIndex = avIndex;
                L.i("IOTCamera", "avClientStart(" + mAVChannel.getChannel() + ", " + mAVChannel.getViewAcc() + ", " + mAVChannel.getViewPwd() + ") in Session(" + mSID + ") returns " + avIndex + " bResend = " + bResend[0]);
                long servType = nServType[0];

                if (avIndex >= 0) {

                    mAVChannel.setAVIndex(avIndex);
                    mAVChannel.setServiceType(servType);
                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECTED);
                    }
                    // }
                    break;

                } else if (avIndex == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT || avIndex == IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE) {
                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {
                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_TIMEOUT);
                    }
                    // }

                } else if (avIndex == AVAPIs.AV_ER_TIMEOUT) {
                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveChannelInfo(Camera.this, mAVChannel.getChannel(), CONNECTION_STATE_TIMEOUT);
                    }
                    // }

                } else if (avIndex == AVAPIs.AV_ER_WRONG_VIEWACCorPWD) {
                    // synchronized (mIOTCListeners) {
                    for (int i = 0; i < mIOTCListeners.size(); i++) {

                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                        listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_WRONG_PASSWORD);
                    }
                    // }

                    break;

                } else {

                    try {
                        synchronized (mWaitObject) {
                            mWaitObject.wait(1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // mAVChannel.threadStartDev = null;

            L.i("IOTCamera", "===ThreadStartDev exit===");
        }
    }

    /**
     * 用于检测这个IOTC session是否仍然活跃的线程
     *
     * @author Administrator
     */
    private class ThreadCheckDevStatus extends TwsThread {


        @Override
        public void run() {
            super.run();

            L.i("IOTCamera", uid + " ThreadCheckDevStatus 开启验证摄像机状态");
            St_SInfo stSInfo = new St_SInfo();
            int ret = 0;
            long loginTime = 0;
            long waitTimeout = 20 * 1000;
            while (isRunning && mSID < 0) {

                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while (isRunning) {

                L.i("IOTCamera", "IOTC_Session_Check(" + mSID + ") ");
                if (mSID >= 0) {

                    /**
                     * 用于检测这个IOTC session是否仍然活跃
                     */
                    ret = IOTCAPIs.IOTC_Session_Check(mSID, stSInfo);
                    L.i("IOTCamera", "IOTC_Session_Check(" + mSID + ") " + ret);
                    if (ret >= 0) {

                        if (mSessionMode != stSInfo.Mode) {

                            mSessionMode = stSInfo.Mode;
                            loginTime = System.currentTimeMillis();
                            /*
                            synchronized (mIOTCListeners) {
								for (int i = 0; i < mIOTCListeners.size(); i++) {

									IRegisterIOTCListener listener = mIOTCListeners.get(i);
									listener.receiveSessionInfo(Camera.this, STS_CHANGE_SESSION_MODE);
								}
							}
							*/
                        }
                    } else {
                        if (ret == IOTCAPIs.IOTC_ER_REMOTE_TIMEOUT_DISCONNECT || ret == IOTCAPIs.IOTC_ER_TIMEOUT || ret == IOTCAPIs.IOTC_ER_SESSION_CLOSE_BY_REMOTE) {

                            // synchronized (mIOTCListeners) {
                            for (int i = 0; i < mIOTCListeners.size(); i++) {

                                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_TIMEOUT);
                            }
                            // }

                        } else if (ret == IOTCAPIs.IOTC_ER_SLEEPING) {


                            //  synchronized (mIOTCListeners) {
                            for (int i = 0; i < mIOTCListeners.size(); i++) {

                                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_SLEEPING);
                            }
                            // }

                        } else {

                            L.i("IOTCamera", "IOTC_Session_Check(" + mSID + ") Failed return " + ret);

                            // synchronized (mIOTCListeners) {
                            for (int i = 0; i < mIOTCListeners.size(); i++) {

                                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                listener.receiveSessionInfo(Camera.this, CONNECTION_STATE_CONNECT_FAILED);
                            }
                            // }
                        }

                        boolean isTimeout = Math.abs(System.currentTimeMillis() - loginTime) > waitTimeout;
                        if (isTimeout) {
                            break;
                        }
                    }
                }
                this.sleep(5000);
            }

            L.i("IOTCamera", "===ThreadCheckDevStatus exit===");
        }
    }

    /**
     * 接收视频数据的线程
     *
     * @author Administrator
     */
    private class ThreadRecvVideo2 extends TwsThread {
        private static final int MAX_BUF_SIZE = 1280 * 720 * 3;
        private AVChannel mAVChannel;

        public ThreadRecvVideo2(AVChannel channel) {
            mAVChannel = channel;
        }


        @Override
        public void run() {
            L.i("IOTCamera", uid + " ThreadRecvVideo2 开启接受视频帧2");
            System.out.println("ThreadRecvVideo2 start");
            System.gc();
            mAVChannel.width = 0;
            mAVChannel.heigth = 0;

            while (isRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mAVChannel.VideoBPS = 0;

            byte[] buf = null;
            try {
                buf = new byte[MAX_BUF_SIZE];
            } catch (OutOfMemoryError e) {
                System.gc();
                buf = null;
                buf = new byte[MAX_BUF_SIZE];
                Log.e(this.getClass().getSimpleName(), "OutOfMemoryError");
            }

            byte[] pFrmInfoBuf = new byte[AVFrame.FRAMEINFO_SIZE];

            int[] pFrmNo = new int[1];
            int nCodecId = 0;
            int nReadSize = 0;
            int nFrmCount = 0;
            int nIncompleteFrmCount = 0;
            int nOnlineNumber = 0;
            long nPrevFrmNo = 0x0FFFFFFF;
            long nLastTimeStamp = System.currentTimeMillis();

            int nFlow_total_frame_count = 0;
            int nFlow_lost_incomplete_frame_count = 0;
            int nFlow_total_expected_frame_size = 0;
            int nFlow_total_actual_frame_size = 0;
            long nFlow_timestamp = System.currentTimeMillis();
            nRecvFrmPreSec = 0;

            int[] outBufSize = new int[1];
            int[] outFrmSize = new int[1];
            int[] outFrmInfoBufSize = new int[1];

            if (mSID >= 0 && mAVChannel.getAVIndex() >= 0)
            /**
             * Clean the video buffer.
             * A client with multiple device connection application should call this function
             * to clean video buffer while switch to another devices.
             */
                AVAPIs.avClientCleanVideoBuf((mAVChannel.getAVIndex()));
//				AVAPIs.avClientCleanBuf(mAVChannel.getAVIndex());
            mAVChannel.VideoFrameQueue.removeAll();
            mAVChannel.RecordFrameTempQueue.removeAll();

            if (isRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                System.out.println("IOTYPE_USER_IPCAM_START");
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_START, Packet.intToByteArray_Little(mCamIndex));
            }


            while (isRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

                    if (System.currentTimeMillis() - nLastTimeStamp > 1000) {

                        nLastTimeStamp = System.currentTimeMillis();
                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {

                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveFrameInfo(Camera.this, mAVChannel.getChannel(), (mAVChannel.AudioBPS + mAVChannel.VideoBPS) * 8 / 1024, mAVChannel.VideoFPS, nOnlineNumber, nFrmCount, nIncompleteFrmCount);
                        }
                        // }

                        mAVChannel.VideoFPS = mAVChannel.VideoBPS = mAVChannel.AudioBPS = 0;
                    }

                    if (mAVChannel.flowInfoInterval > 0 && (System.currentTimeMillis() - nFlow_timestamp) > (mAVChannel.flowInfoInterval * 1000)) {

                        int elapsedTimeMillis = (int) (System.currentTimeMillis());
                        sendIOCtrl(mAVChannel.mChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_CURRENT_FLOWINFO, SMsgAVIoctrlCurrentFlowInfo.parseContent(mAVChannel.getChannel(), nFlow_total_frame_count, nFlow_total_frame_count - nFlow_total_FPS_count, nFlow_total_expected_frame_size, nFlow_total_actual_frame_size, elapsedTimeMillis));
                        nFlow_total_frame_count = 0;
                        nFlow_lost_incomplete_frame_count = 0;
                        nFlow_total_expected_frame_size = 0;
                        nFlow_total_actual_frame_size = 0;
                        nFlow_total_FPS_count = 0;
                        nFlow_timestamp = System.currentTimeMillis();
                    }
                    /**
                     * receives frame data from an AV serve
                     *
                     * nAVChannelID          [in]  The channel ID of the AV channel to be received
                     * abFrameData           [out] The frame data to be received
                     * nFrameDataMaxSize     [in]  The max size of the frame data
                     * pnActualFrameSize     [in]  The actual size of frame data to be received, maybe less than expected size
                     * pnExpectedFrameSize   [in]  The size of frame data expect to be received that sent from av server
                     * abFrameInfo           [out] The video frame information to be received
                     * nFrameInfoMaxSize     [in]  The max size of the video frame information
                     * pnActualFrameInfoSize [in]  The actual size of the video frame information to be received
                     * pnFrameIdx            [out] The index of current receiving video frame
                     *
                     * returns: The actual length of received result stored in abFrameData if successfully.
                     */
                    nReadSize = AVAPIs.avRecvFrameData2(mAVChannel.getAVIndex(), buf, buf.length, outBufSize, outFrmSize, pFrmInfoBuf, pFrmInfoBuf.length, outFrmInfoBufSize, pFrmNo);

//					nReadSize = AVAPIs.avRecvFrameData(mAVChannel.getAVIndex(), buf, buf.length, pFrmInfoBuf, pFrmInfoBuf.length, pFrmNo);

//					System.out.println("avRecvFrameData2 " + nReadSize + " " + mAVChannel.getAVIndex() + " " + outBufSize);
                    if (nReadSize >= 0) {

                        mAVChannel.VideoBPS += outBufSize[0];
                        nFrmCount++;

                        byte[] frameData = new byte[nReadSize];
                        System.arraycopy(buf, 0, frameData, 0, nReadSize);//将数据copy到frameData

                        AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, pFrmInfoBuf, frameData, nReadSize);

                        nCodecId = (int) frame.getCodecId();
                        nOnlineNumber = (int) frame.getOnlineNum();

                        if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {
//							System.out.println("VideoFrameQueue after" + pFrmNo[0] + " " + nPrevFrmNo);
                            if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {
//								System.out.println("VideoFrameQueue receive");
                                nPrevFrmNo = pFrmNo[0];
                                nRecvFrmPreSec++;

                                mAVChannel.VideoFrameQueue.addLast(frame);
                                if (mAVChannel.mVideoPlayProperty.isRecording) {
                                    //FrameData record_frame = new FrameData(buf, nRet1);
                                    mAVChannel.RecordFrameQueue.addLast(new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, pFrmInfoBuf.clone(), frameData.clone(), nReadSize));
                                } else {
                                    if (frame.isIFrame()) {
                                        mAVChannel.RecordFrameTempQueue.removeAll();
                                    }
                                    mAVChannel.RecordFrameTempQueue.addLast(new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, pFrmInfoBuf.clone(), frameData.clone(), nReadSize));
                                }
                                //synchronized (mIOTCListeners) {
                                    for (int i = 0; i < mIOTCListeners.size(); i++) {
                                        IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                        listener.receiveOriginalFrameData(Camera.this, this.mAVChannel.getChannel(), pFrmInfoBuf, 24, frameData, nReadSize);
                                    }
                                //}
                            } else {
                                L.i("IOTCamera", "Incorrect frame no(" + pFrmNo[0] + "), prev:" + nPrevFrmNo + " -> drop frame");
                            }

                        } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MPEG4) {

                            if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {
                                nPrevFrmNo = pFrmNo[0];
                                nRecvFrmPreSec++;
                                mAVChannel.VideoFrameQueue.addLast(frame);
                                // synchronized (mIOTCListeners) {
                                for (int i = 0; i < mIOTCListeners.size(); i++) {
                                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                    listener.receiveOriginalFrameData(Camera.this, this.mAVChannel.getChannel(), pFrmInfoBuf, 24, frameData, nReadSize);
                                }
                                // }
                            }

                        } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MJPEG) {
                            nRecvFrmPreSec++;
                            Bitmap bmp = null;
                            try {
                                bmp = BitmapFactory.decodeByteArray(frameData, 0, nReadSize);
                            } catch (OutOfMemoryError e) {
                                System.gc();
                                bmp = null;
                                Log.e("Camera", "OutOfMemoryError");
                            }


                            if (bmp != null) {
                                mAVChannel.VideoFPS++;
                                nFlow_total_FPS_count++;
                                nFlow_total_FPS_count_noClear++;
                                nDispFrmPreSec++;
                                // synchronized (mIOTCListeners) {
                                for (int i = 0; i < mIOTCListeners.size(); i++) {
                                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                    listener.receiveFrameData(Camera.this, mAVChannel.getChannel(), bmp);
                                    listener.receiveOriginalFrameData(Camera.this, this.mAVChannel.getChannel(), pFrmInfoBuf, 24, frameData, nReadSize);
                                }
                                //  }
                                mAVChannel.LastFrame = bmp;
                                if (mAVChannel.mVideoPlayProperty.isRecording) {
                                    if (mAVChannel.RecordFirstFrame == null) {
                                        mAVChannel.RecordFirstFrame = bmp;
                                    }
                                }
                            }
                            sleep(32);
                        }


                        nFlow_total_actual_frame_size += outBufSize[0];
                        nFlow_total_expected_frame_size += outFrmSize[0];
                        nFlow_total_frame_count++;


                    } else if (nReadSize == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {

                        L.i("IOTCamera", "AV_ER_SESSION_CLOSE_BY_REMOTE");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {

                        L.i("IOTCamera", "AV_ER_REMOTE_TIMEOUT_DISCONNECT");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_DATA_NOREADY) {

                        sleep(32);

                        //Log.i("IOTCamera", "AV_ER_DATA_NOREADY");
                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_BUFPARA_MAXSIZE_INSUFF) {

                        continue;

                    } else if (nReadSize == AVAPIs.AV_ER_MEM_INSUFF) {

                        nFrmCount++;
                        nIncompleteFrmCount++;
                        nFlow_lost_incomplete_frame_count++;
                        nFlow_total_frame_count++;
                        L.i("IOTCamera", "AV_ER_MEM_INSUFF");

                    } else if (nReadSize == AVAPIs.AV_ER_LOSED_THIS_FRAME) {

                        L.i("IOTCamera", "AV_ER_LOSED_THIS_FRAME");

                        nFrmCount++;
                        nIncompleteFrmCount++;
                        nFlow_lost_incomplete_frame_count++;
                        nFlow_total_frame_count++;

                    } else if (nReadSize == AVAPIs.AV_ER_INCOMPLETE_FRAME) {

                        nFlow_total_actual_frame_size += outBufSize[0];
                        nFlow_total_expected_frame_size += outFrmSize[0];
                        nFrmCount++;
                        nFlow_total_frame_count++;
                        mAVChannel.VideoBPS += outBufSize[0];


                        if (outFrmInfoBufSize[0] == 0 || (outFrmSize[0] * 0.9) != outBufSize[0] || (int) pFrmInfoBuf[2] == AVFrame.IPC_FRAME_FLAG_PBFRAME) {
                            nIncompleteFrmCount++;
                            nFlow_lost_incomplete_frame_count++;
                            L.i("IOTCamera", ((int) pFrmInfoBuf[2] == AVFrame.IPC_FRAME_FLAG_PBFRAME ? "P" : "I") + " frame, outFrmSize(" + outFrmSize[0] + ") * 0.9 = " + ((outFrmSize[0] * 0.9)) + " > outBufSize(" + outBufSize[0] + ")");
                            continue;
                        }

                        byte[] frameData = new byte[outFrmSize[0]];
                        System.arraycopy(buf, 0, frameData, 0, outFrmSize[0]);
                        nCodecId = Packet.byteArrayToShort_Little(pFrmInfoBuf, 0);

                        if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MJPEG || nCodecId == AVFrame.MEDIA_CODEC_VIDEO_MPEG4) {

                            nIncompleteFrmCount++;
                            nFlow_lost_incomplete_frame_count++;
                            continue;


                        } else if (nCodecId == AVFrame.MEDIA_CODEC_VIDEO_H264) {

                            if (outFrmInfoBufSize[0] == 0 || (outFrmSize[0] * 0.9) != outBufSize[0] || (int) pFrmInfoBuf[2] == AVFrame.IPC_FRAME_FLAG_PBFRAME) {
                                nIncompleteFrmCount++;
                                L.i("IOTCamera", ((int) pFrmInfoBuf[2] == AVFrame.IPC_FRAME_FLAG_PBFRAME ? "P" : "I") + " frame, outFrmSize(" + outFrmSize[0] + ") * 0.9 = " + ((outFrmSize[0] * 0.9)) + " > outBufSize(" + outBufSize[0] + ")");
                                continue;
                            }

                            AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, pFrmInfoBuf, frameData, outFrmSize[0]);

                            if (frame.isIFrame() || pFrmNo[0] == (nPrevFrmNo + 1)) {
                                nPrevFrmNo = pFrmNo[0];
                                nRecvFrmPreSec++;
                                mAVChannel.VideoFrameQueue.addLast(frame);
                                nFlow_total_actual_frame_size += outBufSize[0];
                                nFlow_total_expected_frame_size += outFrmSize[0];

                                L.i("IOTCamera", "AV_ER_INCOMPLETE_FRAME - H264 or MPEG4");
                            } else {
                                nIncompleteFrmCount++;
                                nFlow_lost_incomplete_frame_count++;
                                L.i("IOTCamera", "AV_ER_INCOMPLETE_FRAME - H264 or MPEG4 - LOST");
                            }

                        } else {
                            nIncompleteFrmCount++;
                            nFlow_lost_incomplete_frame_count++;
                        }
                    }
                }

            }// while--end

            L.i("IOTCamera", "===ThreadRecvVideo exit wait===");
            mAVChannel.VideoFrameQueue.removeAll();
            mAVChannel.RecordFrameTempQueue.removeAll();
            if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                System.out.println("IOTYPE_USER_IPCAM_START stop");
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_STOP, Packet.intToByteArray_Little(mCamIndex));
                AVAPIs.avClientCleanBuf(mAVChannel.getAVIndex());
            }


            buf = null;
            System.out.println("ThreadRecvVideo2 end");
            L.i("IOTCamera", "===ThreadRecvVideo exit===");
        }
    }

    private class ThreadDecodeVideo3 extends TwsThread {
        private byte[] yuvBuffer;
        private boolean isDecodeBuffer;
        public VideoPlayer videoPlayer;
        private AVChannel mAVChannel;

        private ThreadDecodeVideo3(AVChannel channel) {
            this.yuvBuffer = null;
            this.isDecodeBuffer = false;
            mAVChannel = channel;
            videoPlayer = new VideoPlayer();
        }

        public byte[] getYuvBuffer() {
            L.i("IOTCamera", "yuv buffer: " + this.isDecodeBuffer);
            return !this.isDecodeBuffer ? null : this.yuvBuffer;
        }

        public void run() {
            System.gc();
            L.i("IOTCamera", "===ThreadDecodeVideo start===" + Camera.this.uid);
            boolean nRet = false;
            int avFrameSize = 0;
            AVFrame avFrame = null;
            boolean isFristIFrame = false;
            Boolean IsSnapFrame = Boolean.valueOf(false);
            long lastFrameTimeStamp = 0;
            long delayTime = 0;
            int[] out_width = new int[1];
            int[] out_height = new int[1];
            int[] out_size = new int[1];
            boolean bInitMpeg4 = false;
            long firstTimeStampFromDevice = 0;
            long firstTimeStampFromLocal = 0;
            long lastUpdateDispFrmPreSec = 0;
            nDispFrmPreSec = 0;
            long t1 = 0, t2 = 0;
            long sleepTime = 0;
            mAVChannel.VideoFPS = 0;
            boolean bSkipThisRound = false;
            int[] result = new int[1];
            while (this.isRunning) {
                avFrame = null;
                if (mAVChannel.VideoFrameQueue.getCount() > 0 && !mAVChannel.isPausePlay()) {
                    avFrame = mAVChannel.VideoFrameQueue.removeHead();
                    if (avFrame == null)
                        continue;
                    avFrameSize = avFrame.getFrmSize();
                } else {
                    sleep(33);
                    continue;
                }
                if (!avFrame.isIFrame() && delayTime > 2000) {
                    long skipTime = (avFrame.getTimeStamp() - lastFrameTimeStamp);
                    L.i("IOTCamera", "case 1. low decode performance, drop " + (avFrame.isIFrame() ? "I" : "P") + " frame, skip time: " + (avFrame.getTimeStamp() - lastFrameTimeStamp) + ", total skip: " + skipTime);
                    lastFrameTimeStamp = avFrame.getTimeStamp();
                    delayTime -= skipTime;
                    bSkipThisRound = true;
                    continue;
                }

                if (!avFrame.isIFrame() && bSkipThisRound) {
                    long skipTime = (avFrame.getTimeStamp() - lastFrameTimeStamp);
                    L.i("IOTCamera", "case 2. low decode performance, drop " + (avFrame.isIFrame() ? "I" : "P") + " frame, skip time: " + (avFrame.getTimeStamp() - lastFrameTimeStamp) + ", total skip: " + skipTime);
                    lastFrameTimeStamp = avFrame.getTimeStamp();
                    delayTime -= skipTime;
                    continue;
                }
                if (avFrameSize > 0) {
                    out_size[0] = 0;
                    out_width[0] = 0;
                    out_height[0] = 0;

                    bSkipThisRound = false;

                    if (avFrame != null) {
                        int frameSize = avFrame.getFrmSize();
                        if (frameSize > 0) {
//                            if (!isFristIFrame) {
//                                if (!avFrame.isIFrame()) {
//                                    continue;
//                                }
//
//                                isFristIFrame = true;
//                            }
                            int[] framePara = new int[4];
                            this.yuvBuffer = videoPlayer.decode2Yuv(avFrame.frmData, framePara, result);
                            // int nRet1 = H264Decoder.HIH264Dec_decoder(handle[0], frame.frmData, frameSize, width, heigth, this.yuvBuffer);
                            if (result[0] > 0) {
                                out_width[0] = framePara[2];
                                out_height[0] = framePara[3];
                                out_size[0] = out_width[0] * out_height[0] * 2;
                                if (out_size[0] > 0 && out_width[0] > 0 && out_height[0] > 0) {
                                    mAVChannel.width = framePara[2];
                                    mAVChannel.heigth = framePara[3];
                                    this.isDecodeBuffer = true;
                                    if (Camera.this.mCameraYUVCallback != null) {
                                        Camera.this.mCameraYUVCallback.callbackYUVData(Camera.this, mAVChannel.getChannel(), this.yuvBuffer, this.yuvBuffer.length, framePara[2], framePara[3]);
                                    }
                                    if (!IsSnapFrame.booleanValue() && Camera.this.mCameraPlayStateCallback != null) {
                                        IsSnapFrame = Boolean.valueOf(true);
                                        Camera.this.mCameraPlayStateCallback.callbackState(Camera.this, mAVChannel.getChannel(), 0, framePara[2], framePara[3]);
                                    }
                                }
                            }
                        }

                        // ------ calculate sleep time ------
                        if (avFrame != null && firstTimeStampFromDevice != 0 && firstTimeStampFromLocal != 0) {

                            long t = System.currentTimeMillis();
                            t2 = t - t1;

                            sleepTime = (firstTimeStampFromLocal + (avFrame.getTimeStamp() - firstTimeStampFromDevice)) - t;
                            delayTime = sleepTime * -1;
                            // Log.i("IOTCamera", "decode time(" + t2 + "); sleep time (" + sleepTime + ") = t0 (" + firstTimeStampFromLocal + ") + (Tn (" + avFrame.getTimeStamp() + ") - T0 (" + firstTimeStampFromDevice + ") " + (avFrame.getTimeStamp() - firstTimeStampFromDevice) + ") - tn' (" + t + ")" );

                            if (sleepTime >= 0) {

                                // sometimes, the time interval from device will large than 1 second, must reset the base timestamp
                                if ((avFrame.getTimeStamp() - lastFrameTimeStamp) > 1000) {
                                    firstTimeStampFromDevice = avFrame.getTimeStamp();
                                    firstTimeStampFromLocal = t;
                                    L.i("IOTCamera", "RESET base timestamp");

                                    if (sleepTime > 1000) sleepTime = 33;
                                }

                                if (sleepTime > 1000) sleepTime = 1000;
                                try {
                                    sleep(sleepTime);
                                } catch (Exception e) {

                                }
                            }

                            lastFrameTimeStamp = avFrame.getTimeStamp();
                        }

                        if (firstTimeStampFromDevice == 0 || firstTimeStampFromLocal == 0) {
                            firstTimeStampFromDevice = lastFrameTimeStamp = avFrame.getTimeStamp();
                            firstTimeStampFromLocal = System.currentTimeMillis();
                        }
                        mAVChannel.VideoFPS++;
                        nFlow_total_FPS_count++;
                        nFlow_total_FPS_count_noClear++;
                        nDispFrmPreSec++;

                        long now = System.currentTimeMillis();

                        if ((now - lastUpdateDispFrmPreSec) > 60000) {
                            nDispFrmPreSec = 0;
                            nRecvFrmPreSec = 0;
                            lastUpdateDispFrmPreSec = now;
                        }
                    }

                } else {
                    this.sleep(33);
                }
                if (avFrame != null) {
                    avFrame.frmData = null;
                    avFrame = null;
                }
            }
            this.isDecodeBuffer = false;
            if (Camera.this.mCameraPlayStateCallback != null) {
                Camera.this.mCameraPlayStateCallback.callbackState(Camera.this, mAVChannel.getChannel(), 1, mAVChannel.width, mAVChannel.heigth);
            }
            //H264Decoder.HIH264Dec_uninit(handle[0]);
            if (videoPlayer != null) {
                videoPlayer.realese();
            }
            L.i("IOTCamera", "===ThreadDecodeVideo exit===" + Camera.this.uid);
            System.gc();
            return;
        }
    }

    private class ThreadRecvAudio extends TwsThread {

        private final int MAX_BUF_SIZE = 1280;
        private int nReadSize = 0;

        private AVChannel mAVChannel;

        public ThreadRecvAudio(AVChannel channel) {
            mAVChannel = channel;
        }


        @Override
        public void run() {

            L.i("IOTCamera", uid + " ThreadRecvAudio 开启接受音频");

            while (isRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {

                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            mAVChannel.AudioBPS = 0;
            byte[] recvBuf = new byte[MAX_BUF_SIZE];
            byte[] bytAVFrame = new byte[AVFrame.FRAMEINFO_SIZE];
            int[] pFrmNo = new int[1];


            byte[] mp3OutBuf = new byte[65535];
            short[] speexOutBuf = new short[160];
            byte[] adpcmOutBuf = new byte[640];
            byte[] G726OutBuf = new byte[2048];
            long[] G726OutBufLen = new long[1];

            byte[] G711OutBuf = new byte[2048];
            long[] G711OutBufLen = new long[1];

            boolean bFirst = true;
            boolean bInitAudio = false;

            int nSamplerate = 44100;
            int nDatabits = 1;
            int nChannel = 1;
            int nCodecId = 0;
            int nFPS = 0;

            if (mSID >= 0 && mAVChannel.getAVIndex() >= 0)
                AVAPIs.avClientCleanAudioBuf((mAVChannel.getAVIndex()));


            mAVChannel.AudioFrameQueue.removeAll();

            if (isRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0)
                mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART, Packet.intToByteArray_Little(mCamIndex));

            while (isRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

					/*
                    int nBufCnt = AVAPIs.avCheckAudioBuf(mAVChannel.getAVIndex());

					if (nBufCnt < nFPS) {

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						continue;
					}
					*/

                    nReadSize = AVAPIs.avRecvAudioData(mAVChannel.getAVIndex(), recvBuf, recvBuf.length, bytAVFrame, AVFrame.FRAMEINFO_SIZE, pFrmNo);

                    if (nReadSize < 0 && nReadSize != AVAPIs.AV_ER_DATA_NOREADY)
                        L.i("====Camera===", "avRecvAudioData < 0");

                    if (nReadSize > 0) {

                        L.i("===IOTCamera=ThreadRecvAudio=", "avRecvAudioData(" + mSID + ") = " + nReadSize);

                        mAVChannel.AudioBPS += nReadSize;

                        byte[] frameData = new byte[nReadSize];
                        System.arraycopy(recvBuf, 0, frameData, 0, nReadSize);

                        AVFrame frame = new AVFrame(pFrmNo[0], AVFrame.FRM_STATE_COMPLETE, bytAVFrame, frameData, nReadSize);

                        nCodecId = (int) frame.getCodecId();

                        mAVChannel.AudioFrameQueue.addLast(frame);
//						if (bFirst) {
//
//							if (!mInitAudio && (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3 || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726|| nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G711)) {
//
//								bFirst = false;
//
//								nSamplerate = AVFrame.getSamplerate(frame.getFlags());
//								nDatabits = (int) (frame.getFlags() & 0x02);
//								nDatabits = (nDatabits == 0x02) ? 1 : 0;
//								nChannel = (int) (frame.getFlags() & 0x01);
//
//								if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
//									L.i("====Camera==bFirst=", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX");
//									nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 160;
//								}
//								else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM){
//									L.i("====Camera=bFirst==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM");
//									nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 640;
//								}
//								else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
//									L.i("====Camera=bFirst==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM");
//									nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / frame.getFrmSize();
//								}
//
//								bInitAudio = audioDev_init(nSamplerate, nChannel, nDatabits, nCodecId);
//
//								if (!bInitAudio)
//									break;
//
//							}
//						}




						/*
                        try {
							Thread.sleep(1000 / nFPS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						*/

                    } else if (nReadSize == AVAPIs.AV_ER_DATA_NOREADY) {
                        L.i("Camera=nReadSize == AVAPIs.AV_ER_DATA_NOREADY", "avRecvAudioData returns AV_ER_DATA_NOREADY");

                        sleep(nFPS == 0 ? 33 : (1000 / nFPS));

                    } else if (nReadSize == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                        L.i("---->>>Camera", "avRecvAudioData returns AV_ER_LOSED_THIS_FRAME");
                    } else {
                        sleep(nFPS == 0 ? 33 : (1000 / nFPS));
                        L.i("--->>>Camera", "avRecvAudioData returns " + nReadSize);
                    }
                }
            } // while(true);


            if (bInitAudio)
                audioDev_stop(nCodecId);


            mAVChannel.IOCtrlQueue.Enqueue(mAVChannel.getAVIndex(), AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP, Packet.intToByteArray_Little(mCamIndex));

            L.i("IOTCamera", "===ThreadRecvAudio exit===");
        }
    }

    private class ThreadDecodeAudio extends TwsThread {

        private AVChannel mAVChannel;

        public ThreadDecodeAudio(AVChannel channel) {
            mAVChannel = channel;
        }


        private void recordingMp4Audio(byte[] pcmbuf, int pts, AVFrame frame) {
            AVFrame record_frame = new AVFrame(frame.getFrmNo(), frame.getFrmState(), HI_P2P_S_AVFrame.parseContent(frame.getCodecId(), pcmbuf.length, frame.getTimeStamp(), pts), pcmbuf, pcmbuf.length);
            this.mAVChannel.RecordFrameQueue.addLast(record_frame);
        }

        @Override
        public void run() {

            L.i("IOTCamera", uid + " ThreadDecodeAudio 开启解码音频");
            byte[] mp3OutBuf = new byte[65535];
            short[] speexOutBuf = new short[160];
            byte[] adpcmOutBuf = new byte[640];
            byte[] G726OutBuf = new byte[2048];
            long[] G726OutBufLen = new long[1];

            byte[] G711OutBuf = new byte[2048];
            long[] G711OutBufLen = new long[1];
            byte[] audioBuffer = new byte[160];
            boolean bFirst = true;
            boolean bInitAudio = false;

            int nCodecId = -1;
            int nSamplerate = -1;
            int nDatabits = -1;
            int nChannel = -1;

            int nFPS = 0;

            long firstTimeStampFromDevice = 0;
            long firstTimeStampFromLocal = 0;
            long sleepTime = 0;


            while (isRunning) {
                if (mAVChannel.AudioFrameQueue.getCount() > 0 && !mAVChannel.isPausePlay()) {

                    AVFrame frame = mAVChannel.AudioFrameQueue.removeHead();
                    nCodecId = frame.getCodecId();

//					if (bFirst) {
//
//						if (!mInitAudio && (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3 || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726|| nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G711)) {
//
//							L.i("ThreadDecodeAudio-->bFirst","if (!mInitAudio && (nCodecI");
//							bFirst = false;
//
//							nSamplerate = AVFrame.getSamplerate(frame.getFlags());
//							nDatabits = (int) (frame.getFlags() & 0x02);
//							nDatabits = (nDatabits == 0x02) ? 1 : 0;
//							nChannel = (int) (frame.getFlags() & 0x01);
//
//							bInitAudio = audioDev_init(nSamplerate, nChannel, nDatabits, nCodecId);
//
//							if (!bInitAudio)
//								break;
//						}
//					}
                    if (bFirst) {

                        if (!mInitAudio && (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3 || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726 || nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G711)) {

                            bFirst = false;

                            nSamplerate = AVFrame.getSamplerate(frame.getFlags());
                            nDatabits = frame.getFlags() & 0x02;
                            nDatabits = (nDatabits == 0x02) ? 1 : 0;
                            nChannel = frame.getFlags() & 0x01;

                            if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                                L.i("====Camera==bFirst=", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX");
                                nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 160;
                            } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                                L.i("====Camera=bFirst==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM");
                                nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / 640;
                            } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                                L.i("====Camera=bFirst==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM");
                                nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / frame.getFrmSize();
                            }

                            bInitAudio = audioDev_init(nSamplerate, nChannel, nDatabits, nCodecId);

                            if (!bInitAudio)
                                break;

                        }
                    }
                    if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                        L.i("====Camera=Decode==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_SPEEX");
                        DecSpeex.Decode(frame.frmData, frame.getFrmSize(), speexOutBuf);
                        mAudioTrack.write(speexOutBuf, 0, 160);
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3) {
                        L.i("====Camera=Decode==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_MP3");
                        int len = DecMp3.Decode(frame.frmData, frame.getFrmSize(), mp3OutBuf);
                        mAudioTrack.write(mp3OutBuf, 0, len);
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / len;
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                        L.i("====Camera=Decode==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_ADPCM");
                        DecADPCM.Decode(frame.frmData, frame.getFrmSize(), adpcmOutBuf);
                        mAudioTrack.write(adpcmOutBuf, 0, 640);
                        /*	if (mAVIRecorder!=null) {
                                try {
									mAVIRecorder.addAudio(adpcmOutBuf, 0, 640);//
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}*/

                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                        L.i("====Camera=Decode==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_PCM");
                        mAudioTrack.write(frame.frmData, 0, frame.getFrmSize());
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                        Log.i("====Camera=Decode==", "nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G726");
                        DecG726.g726_decode(frame.frmData, frame.getFrmSize(), G726OutBuf, G726OutBufLen);
                        L.i("IOTCamera", "G726 decode size:" + G726OutBufLen[0]);
                        mAudioTrack.write(G726OutBuf, 0, (int) G726OutBufLen[0]);

                        if (mAVChannel.mVideoPlayProperty.isRecording) {
                            mAVChannel.RecordFrameQueue.addLast(new AVFrame(frame.getFrmNo(), AVFrame.FRM_STATE_COMPLETE, frame.frmHead, G726OutBuf, (int) G726OutBufLen[0]));
                        }
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / (int) G726OutBufLen[0];
                    } else if (nCodecId == AVFrame.MEDIA_CODEC_AUDIO_G711) {
                        //DecG726.g726_decode(recvBuf, nReadSize, G726OutBuf, G726OutBufLen);
                        G711OutBufLen[0] = DecEncG711.Decode(frame.frmData, frame.getFrmSize(), G711OutBuf);
                        mAudioTrack.write(G711OutBuf, 0, (int) G711OutBufLen[0]);
//                        while (true) {
//                            System.arraycopy(frame.frmData, pos1, audioBuffer, 0, 160);
//                            G711OutBufLen[0] = DecEncG711.Decode(audioBuffer, 160, G711OutBuf);
//                            mAudioTrack.write(G711OutBuf, 0, (int)G711OutBufLen[0]);
//                            pos1 += frameSize / sliceCount;
//                            if (mAVChannel.mVideoPlayProperty.isRecording && G711OutBuf[0] > 0) {
//                                this.recordingMp4Audio(G711OutBuf, frame.getTimeStamp()  + 40*pos1 / frameSize, frame);
//                            }
//
//                            if (pos1 >= frameSize) {
//                                break;
//                            }
//
////                            try {
////                                this.sleep(15);
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
//                        }
                        nFPS = ((nSamplerate * (nChannel == AVFrame.AUDIO_CHANNEL_MONO ? 1 : 2) * (nDatabits == AVFrame.AUDIO_DATABITS_8 ? 8 : 16)) / 8) / (int) G711OutBufLen[0];
                    }

//					try {
//						Thread.sleep(1000 / nFPS);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}

                } else {
                    sleep(100);
                }
            }

            if (bInitAudio) {
                audioDev_stop(nCodecId);
            }

            L.i("IOTCamera", "===ThreadDecodeAudio exit===");
        }
    }

    private class ThreadSendAudio extends TwsThread {

        private static final int SAMPLE_RATE_IN_HZ = 8000;
        private int avIndexForSendAudio = -1;
        private int chIndexForSendAudio = -1;
        private AVChannel mAVChannel = null;

        RefInteger adpcm_decode_sample = new RefInteger(0);
        RefInteger adpcm_decode_index = new RefInteger(0);
        RefInteger adpcm_encode_sample = new RefInteger(0);
        RefInteger adpcm_encode_index = new RefInteger(0);
        int talk_seq;
        int talk_tick;
        private long op_t;
        private long op_r_t;
        private long op_w_t;
        private long local_start_tick;
        private long camera_start_tick;

        public ThreadSendAudio(AVChannel ch) {
            L.i("ThreadSendAudio-->nCodecId ==", " hreadSendAudio(AVChannel ch");
            mAVChannel = ch;
        }

        @Override
        public void stopThread() {
            super.stopThread();
            if (mSID >= 0 && chIndexForSendAudio >= 0) {
                AVAPIs.avServExit(mSID, chIndexForSendAudio);
                sendIOCtrl(mAVChannel.mChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTOP, SMsgAVIoctrlAVStream.parseContent(chIndexForSendAudio));
            }
        }

        @Override
        public void run() {
            super.run();

            L.i("IOTCamera", uid + " ThreadSendAudio 开启通话");
            if (mSID < 0) {
                L.i("IOTCamera", "=== ThreadSendAudio exit because SID < 0 ===");
                return;
            }

            boolean bInitSpeexEnc = false;
            boolean bInitG726Enc = false;
            boolean bInitADPCM = false;
            boolean bInitPCM = false;

            boolean bInitG711 = false;

            int nMinBufSize = 0;
            int nReadBytes = 0;

			/* wait for connection */
            chIndexForSendAudio = IOTCAPIs.IOTC_Session_Get_Free_Channel(mSID);

            if (chIndexForSendAudio < 0) {
                L.i("Camera-ThreadSendAudio--chIndexForSendAudio < 0", "=== ThreadSendAudio exit becuase no more channel for connection ===");
                return;
            }

            sendIOCtrl(mAVChannel.mChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART, SMsgAVIoctrlAVStream.parseContent(chIndexForSendAudio));

            L.i("Camera-ThreadSendAudio--", "start avServerStart(" + mSID + ", " + chIndexForSendAudio + ")");

            while (isRunning && (avIndexForSendAudio = AVAPIs.avServStart(mSID, null, null, 60, 0, chIndexForSendAudio)) < 0) {
                L.i("Camera-ThreadSendAudio--", "avServerStart(" + mSID + ", " + chIndexForSendAudio + ") : " + avIndexForSendAudio);
            }

            L.i("Camera-ThreadSendAudio--", "avServerStart(" + mSID + ", " + chIndexForSendAudio + ") : " + avIndexForSendAudio);

			/* init speex encoder */
            if (isRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                EncSpeex.InitEncoder(8);
                bInitSpeexEnc = true;

                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 640;

                L.i("Camera-ThreadSendAudio--", "Speex encoder init");
            }

			/* init ADPCM encoder */
            if (isRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                EncADPCM.ResetEncoder();
                bInitADPCM = true;

                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 640;

                L.i("Camera-ThreadSendAudio--", "ADPCM encoder init");
            }

			/* init G726 encoder */
            if (isRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                EncG726.g726_enc_state_create((byte) EncG726.G726_16, EncG726.FORMAT_LINEAR);
                bInitG726Enc = true;

                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 320;

                L.i("Camera-ThreadSendAudio--", "G726 encoder init");
            }

			/* init G711 encoder */
            if (isRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_G711) {
                //EncG726.g726_enc_state_create((byte) EncG726.G726_16, EncG726.FORMAT_LINEAR);
                //bInitG726Enc = true;
                bInitG711 = true;

                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                // nMinBufSize = 320;

                L.i("==IOTCamera==", "G711 encoder init");
            }

            if (isRunning && mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                L.i("Camera-ThreadSendAudio--", "AVFrame.MEDIA_CODEC_AUDIO_PCM encoder init");
                bInitPCM = true;
                nMinBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            }

			/* init mic of phone */
            AudioRecord recorder = null;
            if (isRunning && (bInitADPCM || bInitG726Enc || bInitSpeexEnc || bInitPCM || bInitG711)) {
                L.i("Camera-ThreadSendAudio--", "init mic of phone  recorder = new AudioRecord(Me");
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, nMinBufSize);
                L.i("Camera-ThreadSendAudio--", "recorder = new AudioRecord");
                if (recorder != null && recorder.getState() == AudioRecord.STATE_INITIALIZED) {//增加一个是否初始化了的判断：2014-10-15
                    recorder.startRecording();
                }
                //recorder.startRecording();
                L.i("Camera-ThreadSendAudio--", "recorder.startRecording();");
            }

            short[] inSpeexBuf = new short[160];
            byte[] inADPCMBuf = new byte[640];
            byte[] inG726Buf = new byte[320];
            byte[] inPCMBuf = new byte[640];

            byte[] inG711Buf = new byte[320];
            // byte[] inG711Buf = new byte[640];

            byte[] outSpeexBuf = new byte[38];
            byte[] outADPCMBuf = new byte[160];
            byte[] outG726Buf = new byte[2048];
            long[] outG726BufLen = new long[1];

            byte[] outG711Buf = new byte[2048];
            // byte[] outG711Buf = new byte[320];
            long[] outG711BufLen = new long[1];

            File file = new File(Environment.getExternalStorageDirectory(), "talkbuffer1");
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            /* send audio data continuously */
            while (isRunning) {
                // read speaker data


                if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_SPEEX) {
                    L.i("Camera-ThreadSendAudio--", "AVFrame.MEDIA_CODEC_AUDIO_SPEEX");
                    nReadBytes = recorder.read(inSpeexBuf, 0, inSpeexBuf.length);

                    if (nReadBytes > 0) {
                        int len = EncSpeex.Encode(inSpeexBuf, nReadBytes, outSpeexBuf);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_SPEEX, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());


                        AVAPIs.avSendAudioData(avIndexForSendAudio, outSpeexBuf, len, frameInfo, 16);

                        try {
                            out.write(outSpeexBuf);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_ADPCM) {
                    L.i("Camera-ThreadSendAudio--", "AVFrame.MEDIA_CODEC_AUDIO_ADPCM");
                    nReadBytes = recorder.read(inADPCMBuf, 0, inADPCMBuf.length);

                    if (nReadBytes > 0) {
                        EncADPCM.Encode(inADPCMBuf, nReadBytes, outADPCMBuf);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_ADPCM, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, outADPCMBuf, nReadBytes / 4, frameInfo, 16);

/*//					byte[] chunk=new byte[4+4+4+1+4+160];
//						int talk_tick=(int)System.currentTimeMillis();
//						int t=(int)System.currentTimeMillis()/1000;
//
//						LibcMisc.memcpy(chunk, talk_tick, 4);
//						LibcMisc.memcpy(chunk, 4,talk_seq, 4);
*/                        //outADPCMBuf[23+13]=(byte)(sample&0xFF);

                        if (mAVIRecorder != null) {
                            try {
                                Log.i("send audio==addtalk", "outADPCMBuf--" + nReadBytes);
                                mAVIRecorder.addTalk(outADPCMBuf, 0, 160);//
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        try {
                            out.write(inADPCMBuf);
                            L.i("======out.write(outADPCMBuf)=====", Arrays.toString(outADPCMBuf));
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_G726) {
                    L.i("Camera-ThreadSendAudio--", "AVFrame.MEDIA_CODEC_AUDIO_G726");
                    nReadBytes = recorder.read(inG726Buf, 0, inG726Buf.length);

                    if (nReadBytes > 0) {

                        EncG726.g726_encode(inG726Buf, nReadBytes, outG726Buf, outG726BufLen);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_G726, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, outG726Buf, (int) outG726BufLen[0], frameInfo, 16);
                    /*	if (mAVIRecorder!=null) {
                            try {
								mAVIRecorder.addAudio(outG726Buf, 0, (int) outG726BufLen[0]);//
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}*/
                        try {
                            out.write(outG726Buf);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_PCM) {
                    L.i("Camera-ThreadSendAudio--", "AVFrame.MEDIA_CODEC_AUDIO_PCM");
                    nReadBytes = recorder.read(inPCMBuf, 0, inPCMBuf.length);

                    if (nReadBytes > 0) {
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_PCM, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, inPCMBuf, nReadBytes, frameInfo, 16);

                        try {
                            out.write(inPCMBuf);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else if (mAVChannel.getAudioCodec() == AVFrame.MEDIA_CODEC_AUDIO_G711) {

                    L.i("==IOTCamera==", "AVFrame.MEDIA_CODEC_AUDIO_G711");
                    nReadBytes = recorder.read(inG711Buf, 0, inG711Buf.length);
                    L.i("==IOTCamera==", "nReadBytes==" + nReadBytes);
                    if (nReadBytes > 0) {

                        //EncG726.g726_encode(inG726Buf, nReadBytes, outG711Buf, outG711BufLen);
                        outG711BufLen[0] = DecEncG711.Encode(inG711Buf, nReadBytes, outG711Buf);

                        L.i("==IOTCamera==", "outG711BufLen[0]" + outG711BufLen[0] + ";inG711Buf=" + inG711Buf.length + ";outG711Buf=" + outG711Buf.length + "inG711Buf=" + inG711Buf + ";outG711Buf=" + outG711Buf);
                        byte flag = (AVFrame.AUDIO_SAMPLE_8K << 2) | (AVFrame.AUDIO_DATABITS_16 << 1) | AVFrame.AUDIO_CHANNEL_MONO;
                        byte[] frameInfo = AVIOCTRLDEFs.SFrameInfo.parseContent((short) AVFrame.MEDIA_CODEC_AUDIO_G711, flag, (byte) 0, (byte) 0, (int) System.currentTimeMillis());

                        AVAPIs.avSendAudioData(avIndexForSendAudio, outG711Buf, (int) outG711BufLen[0], frameInfo, 16);
                        Log.i("==IOTCamera==", "outG711BufLen[0]=" + outG711BufLen[0]);
                        try {
                            out.write(outG711Buf);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }
            }

            try {
                out.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

			/* uninit speex encoder */
            if (bInitSpeexEnc) {
                EncSpeex.UninitEncoder();
            }

			/* uninit g726 encoder */
            if (bInitG726Enc) {
                EncG726.g726_enc_state_destroy();
            }

			/* uninit speaker of phone */
            if (recorder != null) {
                try {
                    recorder.stop();
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
                recorder.release();
                recorder = null;
            }

			/* close connection */
            if (avIndexForSendAudio >= 0) {
                AVAPIs.avServStop(avIndexForSendAudio);
            }

            if (chIndexForSendAudio >= 0) {
                IOTCAPIs.IOTC_Session_Channel_OFF(mSID, chIndexForSendAudio);
            }

            avIndexForSendAudio = -1;
            chIndexForSendAudio = -1;

            L.i("IOTCamera", "===ThreadSendAudio exit===");
        }
    }

    private class ThreadSendIOCtrl extends TwsThread {

        private AVChannel mAVChannel;

        public ThreadSendIOCtrl(AVChannel channel) {
            mAVChannel = channel;
        }


        @Override
        public void stopThread() {
            super.stopThread();
            if (mAVChannel.getAVIndex() >= 0) {
                L.i("IOTCamera", "avSendIOCtrlExit(" + mAVChannel.getAVIndex() + ") wait");
                AVAPIs.avSendIOCtrlExit(mAVChannel.getAVIndex());
                L.i("IOTCamera", "avSendIOCtrlExit(" + mAVChannel.getAVIndex() + ") ");
            }
        }

        @Override
        public void run() {

            L.i("IOTCamera", uid + " ThreadSendIOCtrl 开启IO指令发送");

            while (isRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (isRunning && mSID >= 0 && mAVChannel.getAVIndex() >= 0) {
                int nDelayTime_ms = 0;
                AVAPIs.avSendIOCtrl(mAVChannel.getAVIndex(), AVAPIs.IOTYPE_INNER_SND_DATA_DELAY, Packet.intToByteArray_Little(nDelayTime_ms), 4);
                L.i("IOTCamera", "avSendIOCtrl(" + mAVChannel.getAVIndex() + ", 0x" + Integer.toHexString(AVAPIs.IOTYPE_INNER_SND_DATA_DELAY) + ", " + getHex(Packet.intToByteArray_Little(nDelayTime_ms), 4) + ")");
            }

            while (isRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0 && !mAVChannel.IOCtrlQueue.isEmpty()) {

                    IOCtrlQueue.IOCtrlSet data = mAVChannel.IOCtrlQueue.Dequeue();

                    if (isRunning && data != null) {

                        int ret = AVAPIs.avSendIOCtrl(mAVChannel.getAVIndex(), data.IOCtrlType, data.IOCtrlBuf, data.IOCtrlBuf.length);

                        if (ret >= 0) {
                            L.i("IOTCamera", "avSendIOCtrl1(" + mAVChannel.getAVIndex() + "IOCtrlType" + data.IOCtrlType + ", 0x" + Integer.toHexString(data.IOCtrlType) + ", " + getHex(data.IOCtrlBuf, data.IOCtrlBuf.length) + ")");
                        } else {
                            L.i("IOTCamera", "avSendIOCtrl failed : " + ret);
                        }
                    }
                } else {
                    sleep(50);
                }
            }

            L.i("IOTCamera", "===ThreadSendIOCtrl exit===");
        }
    }

    private class ThreadRecvIOCtrl extends TwsThread {

        private final int TIME_OUT = 0;

        private AVChannel mAVChannel;

        public ThreadRecvIOCtrl(AVChannel channel) {
            mAVChannel = channel;
        }


        @Override
        public void run() {

            L.i("IOTCamera", uid + " ThreadRecvIOCtrl 开启IO指令接受");


            while (isRunning && (mSID < 0 || mAVChannel.getAVIndex() < 0)) {
                try {
                    synchronized (mWaitObjectForConnected) {
                        mWaitObjectForConnected.wait(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            int idx = 0;

            while (isRunning) {

                if (mSID >= 0 && mAVChannel.getAVIndex() >= 0) {

                    int[] ioCtrlType = new int[1];
                    byte[] ioCtrlBuf = new byte[1024];

                    int nRet = AVAPIs.avRecvIOCtrl(mAVChannel.getAVIndex(), ioCtrlType, ioCtrlBuf, ioCtrlBuf.length, TIME_OUT);

                    if (nRet >= 0) {

                        L.i("IOTCamera", "avRecvIOCtrl(" + mAVChannel.getAVIndex() + ", 0x" + Integer.toHexString(ioCtrlType[0]) + ", " + getHex(ioCtrlBuf, nRet) + ")");

                        byte[] data = new byte[nRet];
                        System.arraycopy(ioCtrlBuf, 0, data, 0, nRet);

                        if (ioCtrlType[0] == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_RESP) {

                            int channel = Packet.byteArrayToInt_Little(data, 0);
                            int format = Packet.byteArrayToInt_Little(data, 4);

                            for (AVChannel ch : mAVChannels) {
                                if (ch.getChannel() == channel) {
                                    ch.setAudioCodec(format);
                                    break;
                                }
                            }
                        }

                        if (ioCtrlType[0] == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FLOWINFO_REQ) {


                            int channel = Packet.byteArrayToInt_Little(data, 0);
                            int collect_interval = Packet.byteArrayToInt_Little(data, 4);

                            for (AVChannel ch : mAVChannels) {
                                if (ch.getChannel() == channel) {
                                    ch.flowInfoInterval = collect_interval;
                                    sendIOCtrl(mAVChannel.mChannel, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FLOWINFO_RESP, SMsgAVIoctrlGetFlowInfoResp.parseContent(channel, ch.flowInfoInterval));
                                    break;
                                }
                            }
                        }
                        L.i("AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FLOWINFO_REQ", "AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FLOWINFO_REQ ++");

                        // synchronized (mIOTCListeners) {
                        for (int i = 0; i < mIOTCListeners.size(); i++) {
                            IRegisterIOTCListener listener = mIOTCListeners.get(i);
                            listener.receiveIOCtrlData(Camera.this, mAVChannel.getChannel(), ioCtrlType[0], data);
                        }
                        // }

                    } else {
                        sleep(100);
                    }
                }
            }

            L.i("IOTCamera", "===ThreadRecvIOCtrl exit===");
        }
    }

    private class AVChannel {
        public volatile int width;
        public volatile int heigth;
        private volatile int mChannel = -1;
        private volatile int mAVIndex = -1;
        private long mServiceType = 0xFFFFFFFF;
        private String mViewAcc;
        private String mViewPwd;
        private int mAudioCodec;
        public volatile VideoPlayProperty mVideoPlayProperty = null;

        public boolean isPausePlay() {
            return pausePlay;
        }

        public void setPausePlay(boolean pausePlay) {
            this.pausePlay = pausePlay;
        }

        private volatile boolean pausePlay = false;
        public IOCtrlQueue IOCtrlQueue;
        public AVFrameQueue VideoFrameQueue;
        public AVFrameQueue AudioFrameQueue;
        public AVFrameQueue RecordFrameQueue;
        public AVFrameQueue RecordFrameTempQueue;
        public Bitmap RecordFirstFrame;

        public Bitmap LastFrame;

        public int VideoFPS;
        public int VideoBPS;
        public int AudioBPS;

        public int flowInfoInterval;

        public AVChannel(int channel, String view_acc, String view_pwd) {
            mChannel = channel;
            mViewAcc = view_acc;
            mViewPwd = view_pwd;
            mServiceType = 0xFFFFFFFF;

            VideoFPS = VideoBPS = AudioBPS = flowInfoInterval = 0;

            LastFrame = null;

            IOCtrlQueue = new IOCtrlQueue();
            VideoFrameQueue = new AVFrameQueue();
            AudioFrameQueue = new AVFrameQueue();
            RecordFrameQueue = new AVFrameQueue();
            RecordFrameTempQueue = new AVFrameQueue();
            if (RecordFirstFrame != null && !RecordFirstFrame.isRecycled()) {
                RecordFirstFrame.recycle();
            }
            RecordFirstFrame = null;
            mVideoPlayProperty = new VideoPlayProperty();
        }

        public int getChannel() {
            return mChannel;
        }

        public synchronized int getAVIndex() {
            return mAVIndex;
        }

        public synchronized void setAVIndex(int idx) {
            mAVIndex = idx;
        }

        public synchronized long getServiceType() {
            return mServiceType;
        }

        public synchronized int getAudioCodec() {
            return mAudioCodec;
        }

        public synchronized void setAudioCodec(int codec) {
            mAudioCodec = codec;
        }

        public synchronized void setServiceType(long serviceType) {
            mServiceType = serviceType;
            mAudioCodec = (serviceType & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
        }

        public String getViewAcc() {
            return mViewAcc;
        }

        public String getViewPwd() {
            return mViewPwd;
        }

        public ThreadStartDev threadStartDev = null;
        public ThreadRecvIOCtrl threadRecvIOCtrl = null;
        public ThreadSendIOCtrl threadSendIOCtrl = null;
        public ThreadRecvVideo2 threadRecvVideo = null;
        public ThreadRecvAudio threadRecvAudio = null;
        public ThreadDecodeVideo3 threadDecVideo = null;
        public ThreadDecodeAudio threadDecAudio = null;
        public ThreadRecording threadRecording = null;
    }

    private class IOCtrlQueue {

        public class IOCtrlSet {

            public int IOCtrlType;
            public byte[] IOCtrlBuf;

            public IOCtrlSet(int avIndex, int type, byte[] buf) {
                IOCtrlType = type;
                IOCtrlBuf = buf;
            }

            public IOCtrlSet(int type, byte[] buf) {
                IOCtrlType = type;
                IOCtrlBuf = buf;
            }
        }

        LinkedList<IOCtrlSet> listData = new LinkedList<IOCtrlSet>();

        public synchronized boolean isEmpty() {
            return listData.isEmpty();
        }

        public synchronized void Enqueue(int type, byte[] data) {
            listData.addLast(new IOCtrlSet(type, data));
        }

        public synchronized void Enqueue(int avIndex, int type, byte[] data) {
            listData.addLast(new IOCtrlSet(avIndex, type, data));
        }

        public synchronized IOCtrlSet Dequeue() {

            return listData.isEmpty() ? null : listData.removeFirst();
        }

        public synchronized void removeAll() {
            if (!listData.isEmpty())
                listData.clear();
        }
    }

    private static final String HEXES = "0123456789ABCDEF";

    static String getHex(byte[] raw, int size) {

        if (raw == null) {
            return null;
        }

        final StringBuilder hex = new StringBuilder(2 * raw.length);

        int len = 0;

        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F))).append(" ");

            if (++len >= size)
                break;
        }

        return hex.toString();
    }

    public void startRecording(String path, int avChannel) {
        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {
                AVChannel ch = mAVChannels.get(i);
                if (ch.getChannel() == avChannel) {

                    if (ch.mVideoPlayProperty != null) {
                        //if(ch.mVideoPlayProperty.receiveChannel == 2) {
                        ch.mVideoPlayProperty.recordingPath = path;
                        ch.mVideoPlayProperty.isRecording = true;
                        this.startRecordingThread(ch);
                        //}
                    }

                    break;
                }
            }
        }
    }

    public void stopRecording(int avChannel) {
        synchronized (mAVChannels) {

            for (int i = 0; i < mAVChannels.size(); i++) {
                AVChannel ch = mAVChannels.get(i);
                if (ch.getChannel() == avChannel) {

                    ch.mVideoPlayProperty.recordingPath = null;
                    ch.mVideoPlayProperty.isRecording = false;
                    this.stopRecordingThread(ch);

                    break;
                }
            }
        }
    }

    private void startRecordingThread(AVChannel avChannel) {
        if (avChannel.threadRecording == null) {
            avChannel.threadRecording = new Camera.ThreadRecording(avChannel);
            avChannel.threadRecording.startThread();
        }

    }

    private void stopRecordingThread(AVChannel avChannel) {
        if (avChannel.threadRecording != null) {
            avChannel.threadRecording.stopThread();
        }

        avChannel.threadRecording = null;
    }

    private class ThreadRecording extends TwsThread {
        private AVChannel mAVChannel;
        private boolean beginRecord;

        public ThreadRecording(AVChannel channel) {
            mAVChannel = channel;
        }

        public void run() {

            L.i("IOTCamera", uid + " ThreadRecording 开启录像");
            mAVChannel.RecordFrameQueue.removeAll();
            if (mAVChannel.RecordFirstFrame != null && !mAVChannel.RecordFirstFrame.isRecycled()) {
                mAVChannel.RecordFirstFrame.recycle();
            }
            mAVChannel.RecordFirstFrame = null;
            boolean isFristIFrame = false;
            int[] handle = new int[1];
            int chipVerion = 1;
            beginRecord = false;
//            if (mAVChannel.width == 0) {
//                // synchronized (mIOTCListeners) {
//                for (int i = 0; i < mIOTCListeners.size(); i++) {
//                    IRegisterIOTCListener listener = mIOTCListeners.get(i);
//                    listener.receiveRecordingData(Camera.this, mAVChannel.mChannel, 2, mAVChannel.mVideoPlayProperty.recordingPath);
//                }
//                // }
//                return;
//            }
            while (mAVChannel.threadDecVideo == null || mAVChannel.width == 0 || mAVChannel.RecordFrameQueue.getCount() == 0) {
                this.sleep(33);
            }
            EncMp4.HIEncMp4init(handle, mAVChannel.width, mAVChannel.heigth, mAVChannel.mVideoPlayProperty.recordingPath, chipVerion);
            boolean frameType = false;
            // synchronized (mIOTCListeners) {
            for (int i = 0; i < mIOTCListeners.size(); i++) {
                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                listener.receiveRecordingData(Camera.this, mAVChannel.mChannel, 0, mAVChannel.mVideoPlayProperty.recordingPath);
            }
            // }
            while (this.isRunning) {
                if (mAVChannel.threadDecVideo == null || mAVChannel.width == 0 || mAVChannel.RecordFrameQueue.getCount() == 0) {
                    this.sleep(33);
                } else {
                    AVFrame frame = mAVChannel.RecordFrameQueue.removeHead();
                    if (frame != null) {
                        byte frameType1;
                        if (frame.getCodecId() > AVFrame.MEDIA_CODEC_VIDEO_MJPEG) {
                            frameType1 = 2;
                        } else {
                            if (frame.isIFrame()) {
                                frameType1 = 0;
                                frameType = true;
                            } else {
                                frameType1 = 1;
                            }
                        }
                        if (!frameType) {
                            if (mAVChannel.RecordFrameTempQueue.getCount() > 0) {
                                while (mAVChannel.RecordFrameTempQueue.getCount() > 0) {
                                    AVFrame preframe = mAVChannel.RecordFrameTempQueue.removeHead();
                                    int preFrameType = preframe.isIFrame() ? 0 : 1;
                                    EncMp4.HIEncMp4write(handle[0], preframe.frmData, preframe.getFrmSize(), preFrameType, preframe.getTimeStamp());
                                    // if (frame.getTimeStamp() - preframe.getTimeStamp() > 2000) {
                                    //break;
                                    // }
                                }
                                frameType = true;
                            }
                            //continue;
                        }

                        int result = EncMp4.HIEncMp4write(handle[0], frame.frmData, frame.getFrmSize(), frameType1, frame.getTimeStamp());
                        if (!beginRecord) {
                            beginRecord = true;
                            for (int i = 0; i < mIOTCListeners.size(); i++) {
                                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                                listener.receiveRecordingData(Camera.this, mAVChannel.mChannel, 3, mAVChannel.mVideoPlayProperty.recordingPath);
                            }
                        }
                        Log.i("Recording", result + "");
                    }
                }
            }

            EncMp4.HIEncMp4deinit(handle[0]);
            // synchronized (mIOTCListeners) {
            for (int i = 0; i < mIOTCListeners.size(); i++) {
                IRegisterIOTCListener listener = mIOTCListeners.get(i);
                listener.receiveRecordingData(Camera.this, mAVChannel.mChannel, 1, mAVChannel.mVideoPlayProperty.recordingPath);
            }
            // }
//				if(HiCamera.this.mCameraPlayStateCallback != null) {
//					HiCamera.this.mCameraPlayStateCallback.callbackState(HiCamera.this, 4, 0, 0);
//				}

        }
    }

    public int wakeUp() {
        this.stop();
        this.connect_state = NSCamera.CONNECTION_STATE_WAKINGUP;
        return IOTCAPIs.IOTC_WakeUp_WakeDevice(uid);
    }
}
