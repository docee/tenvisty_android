package com.tws.commonlib.bean;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd;
import com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.Packet;
import com.tutk.IOTC.st_LanSearchInfo;
import com.tws.commonlib.App;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.LiveViewActivity;
import com.tws.commonlib.base.CameraClient;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.SearchLanAsync;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.db.DatabaseManager;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.Alias;
import com.umeng.message.entity.UMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MyCamera extends Camera implements com.tutk.IOTC.IRegisterIOTCListener {
    //public ConnectCallBack call;

    public int LastAudioMode;

    private String mName;
    private String mUID;
    private String mAcc;
    private String mPwd;
    private String mServerDatabseId;
    private String mCameraStatus;

    private static final Object stopLocker = new Object();
    private int mEventCount = 0;
    private int cbSize = 0;
    private int nIsSupportTimeZone = 0;
    private int nGMTDiff = 0;
    private byte[] szTimeZoneString = new byte[256];
    private boolean bIsMotionDetected;
    private boolean bIsIOAlarm;
    private long rebootTimeout = 120000;

    public float getVideoRatio(Context context) {
        if (videoRatio == 0) {
            DatabaseManager db = new DatabaseManager(context);
            videoRatio = db.getDeviceVideoRatio(this.uid);
            if(videoRatio == 0){
                videoRatio = (float) 16/9;
            }
        }
        return videoRatio;
    }

    public void setVideoRatio(Context context, float videoRatio) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceVideoRatio(this.uid,videoRatio);
        this.videoRatio = videoRatio;
    }

    private float videoRatio = 0;

    public boolean isFirstLogin() {
        return loginState <= 0;
    }

    public void setFirstLogin(boolean f) {
        if (f) {
            loginState = -1;
        } else {
            loginState = 1;
        }
    }

    private int loginState = -1;


    public void setBeginRebootTime(long beginRebootTime) {
        this.beginRebootTime = beginRebootTime;
    }

    private long beginRebootTime;

    public int getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }

    private int videoQuality;

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private boolean isPlaying;
    private UUID mUUID = UUID.randomUUID();
    private List<SStreamDef> mStreamDefs = Collections.synchronizedList(new ArrayList<SStreamDef>());
    public static String DEFAULT_PASSWORD = "admin";
    public static String NO_USE_UID = "00000000000000000000";

    public int getIntId() {
        return TwsTools.GetUIDIntValue(this.getUID());
    }

    public synchronized int getEventNum() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        int eventnum = sp.getInt("eventnum_" + this.getUID(), 0);
        return eventnum;
    }

    public synchronized int refreshEventNum(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int eventnum = sp.getInt("eventnum_" + this.getUID(), 0);
        eventnum++;
        sp.edit().putInt("eventnum_" + this.getUID(), eventnum).commit();
        return eventnum;
    }

    public synchronized int clearEventNum(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int eventnum = sp.getInt("eventnum_" + this.getUID(), 0);
        sp.edit().putInt("eventnum_" + this.getUID(), 0).commit();
        return eventnum;
    }

    public synchronized void setEventNum(Context context, int eventNum) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("eventnum_" + this.getUID(), 0).commit();
    }


    public CameraState getState() {
        return state;
    }

    public void setState(CameraState state) {
        this.state = state;
    }

    private CameraState state = CameraState.None;

    public String getCustomTypeVersion() {
        return customTypeVersion;
    }

    public void setCustomTypeVersion(String customTypeVersion) {
        this.customTypeVersion = customTypeVersion;
    }

    private String customTypeVersion;

    public String getVendorTypeVersion() {
        return vendorTypeVersion;
    }

    public void setVendorTypeVersion(String vendorTypeVersion) {
        this.vendorTypeVersion = vendorTypeVersion;
    }

    private String vendorTypeVersion;

    public String getSystemTypeVersion() {
        return systemTypeVersion;
    }

    public void setSystemTypeVersion(String systemTypeVersion) {
        this.systemTypeVersion = systemTypeVersion;
    }

    private String systemTypeVersion;

    public MyCamera(String name, String uid, String acc, String pwd) {
        mName = name;
        mUID = uid;
        mAcc = acc;
        mPwd = pwd;

        this.name = name;
        this.uid = uid;
        this.user = acc;
        this.pwd = pwd;
        this.registerIOTCListener(this);
        setPlaying(false);
    }

    private Bitmap snapshot;

    public Bitmap getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Bitmap snapshot) {
        this.snapshot = snapshot;
    }

    public void asyncSnapshot(final TaskExecute te, final int channel) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (te != null) {
                    te.onPosted(MyCamera.super.Snapshot(channel));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void start() {
        super.start();
        L.i("IOTCamera", "start connect");
        this.connect(this.uid);
        this.startChannel(Camera.DEFAULT_AV_CHANNEL, this.user, this.pwd);
        this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL,
                AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq
                        .parseContent());
    }

    public void startChannel(int channel) {
        L.i("IOTCamera", "start connect");
        this.startChannel(channel, this.user, this.pwd);
        this.sendIOCtrl(channel,
                AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq
                        .parseContent());
    }

    AsyncTask startTask;

    public void asyncStart(final TaskExecute ex) {
        if (startTask != null) {
            startTask.cancel(true);
        }
        startTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.super.start();

                MyCamera.this.connect(MyCamera.this.uid);
                MyCamera.this.startChannel(Camera.DEFAULT_AV_CHANNEL, MyCamera.this.user, MyCamera.this.pwd);
                MyCamera.this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL,
                        AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                        AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq
                                .parseContent());
                if (ex != null) {
                    ex.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void stop() {
        disconnect();
    }

    AsyncTask stopTask;

    public void asyncStop(final TaskExecute ex) {
        if (stopTask != null) {
            stopTask.cancel(true);
        }
        stopTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                disconnect();
                if (ex != null) {
                    ex.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    AsyncTask stopChannelTask;

    public void asyncStopChannel(final int channel, final TaskExecute ex) {
        if (stopChannelTask != null) {
            stopChannelTask.cancel(true);
        }
        stopChannelTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                stop(channel);
                if (ex != null) {
                    ex.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void startVideo() {
        System.out.println("camera video start" + this.uid);
        setPlaying(true);
        this.startShow(Camera.DEFAULT_AV_CHANNEL);
    }

    AsyncTask startVideoTask;

    public void asyncStartVideo(final TaskExecute te) {
        if (startVideoTask != null) {
            startVideoTask.cancel(true);
        }
        startVideoTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetStreamCtrlReq.parseContent(0, (byte) (getVideoQuality() == 0 ? 1 : 5)));
                System.out.println("camera video start" + MyCamera.this.uid);
                setPlaying(true);
                MyCamera.this.startShow(Camera.DEFAULT_AV_CHANNEL);
                if (te != null) {
                    te.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void stopRecording(final int avChannel) {
        MyCamera.super.stopRecording(avChannel);
    }

    AsyncTask stopRecordingTask;

    public void asyncStopRecording(final int avChannel) {
        if (stopRecordingTask != null) {
            stopRecordingTask.cancel(true);
        }
        stopRecordingTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.super.stopRecording(avChannel);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();

    }

    @Override
    public void sendIOCtrl(final int avChannel, final int type, final byte[] data) {
        MyCamera.super.sendIOCtrl(avChannel, type, data);
    }

    public void asyncSendIOCtrl(final int avChannel, final int type, final byte[] data) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                MyCamera.super.sendIOCtrl(avChannel, type, data);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void stopVideo() {
        setPlaying(false);
        System.out.println("camera video stop" + MyCamera.this.uid);
        stopShow(Camera.DEFAULT_AV_CHANNEL);
    }

    AsyncTask stopVideoTask;

    public void asyncStopVideo(final TaskExecute te) {
        setPlaying(false);
        if (stopVideoTask != null) {
            stopVideoTask.cancel(true);
        }
        stopVideoTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                L.i("IOTCamera", "camera video stop" + MyCamera.this.uid);

                stopShow(Camera.DEFAULT_AV_CHANNEL);
                if (te != null) {
                    te.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void startAudio() {
        startListening(Camera.DEFAULT_AV_CHANNEL);
    }

    AsyncTask startAudioTask;

    public void asyncStartAudio(final TaskExecute te) {
        if (startAudioTask != null) {
            startAudioTask.cancel(true);
        }
        startAudioTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                startListening(Camera.DEFAULT_AV_CHANNEL);
                if (te != null) {
                    te.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void stopAudio() {
        super.stopListening(Camera.DEFAULT_AV_CHANNEL);
    }

    AsyncTask stopAudioTask;

    public void asyncStopAudio(final TaskExecute te) {
        if (stopAudioTask != null) {
            stopAudioTask.cancel(true);
        }
        stopAudioTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.super.stopListening(Camera.DEFAULT_AV_CHANNEL);
                if (te != null) {
                    te.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void startSpeak() {
        startSpeaking(Camera.DEFAULT_AV_CHANNEL);
    }

    AsyncTask startSpeakTask;

    public void asyncStartSpeak(final TaskExecute te) {
        if (startSpeakTask != null) {
            startSpeakTask.cancel(true);
        }
        startSpeakTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                startSpeaking(Camera.DEFAULT_AV_CHANNEL);
                if (te != null) {
                    te.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    @Override
    public void stopSpeak() {
        super.stopSpeaking(Camera.DEFAULT_AV_CHANNEL);
    }

    AsyncTask stopSpeakTask;

    public void asycnStopSpeak(final TaskExecute te) {
        if (stopSpeakTask != null) {
            stopSpeakTask.cancel(true);
        }
        stopSpeakTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.super.stopSpeaking(Camera.DEFAULT_AV_CHANNEL);
                if (te != null) {
                    te.onPosted(null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    private Timer stopPtzTimer = null;

    @Override
    public void ptz(int type) {
        this.sendIOCtrl(0, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) type, (byte) 4, (byte) 0, (byte) 0, (byte) 0, (byte) 0));

        if (stopPtzTimer != null) {
            stopPtzTimer.cancel();
            stopPtzTimer = null;
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sendIOCtrl(0, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_STOP, (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0));
            }
        };

        stopPtzTimer = new Timer(true);
        stopPtzTimer.schedule(task, 1000);
    }

    public void asyncPtz(final int type) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                MyCamera.this.sendIOCtrl(0, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) type, (byte) 4, (byte) 0, (byte) 0, (byte) 0, (byte) 0));

                if (stopPtzTimer != null) {
                    stopPtzTimer.cancel();
                    stopPtzTimer = null;
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        sendIOCtrl(0, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND, SMsgAVIoctrlPtzCmd.parseContent((byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_STOP, (byte) 8, (byte) 0, (byte) 0, (byte) 0, (byte) 0));
                    }
                };

                stopPtzTimer = new Timer(true);
                stopPtzTimer.schedule(task, 1000);
                return null;
            }
        }.execute();
    }

    @Override
    public void connect(String uid) {
        super.connect(uid);
        mUID = uid;
    }

    AsyncTask connectTask;

    public void asyncConnect(final String uid) {
        if (connectTask != null) {
            connectTask.cancel(true);
        }
        connectTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.super.connect(uid);
                mUID = uid;
                return null;
            }
        }.execute();

    }

    @Override
    public void connect(String uid, String pwd) {
        super.connect(uid, pwd);
        mUID = uid;
    }

    public void asyncConnect(final String uid, final String pwd) {
        if (connectTask != null) {
            connectTask.cancel(true);
        }
        connectTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.super.connect(uid, pwd);
                mUID = uid;
                return null;
            }
        }.execute();
    }

    @Override
    public void disconnect() {
        super.disconnect();
        loginState = -1;
        mStreamDefs.clear();
    }

    AsyncTask disconnectTask;

    public void asyncDisconnect() {
        if (disconnectTask != null) {
            disconnectTask.cancel(true);
        }
        disconnectTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.this.disconnect();
                return null;
            }
        }.execute();
    }

    public String getUUID() {
        return mUUID.toString();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
        this.name = name;
    }

    public String getUID() {
        return mUID;
    }

    public String getPassword() {
        //涓嶇悊瑙ｄ负浠�涔堥渶瑕佸畾涔塵Pwd鍙橀噺锛�
        return this.pwd;
    }

    public void setPassword(String pwd) {
        mPwd = pwd;
        this.pwd = pwd;
    }

    public String getServerDatabseId() {
        return mServerDatabseId;
    }

    public void setServerDatabseId(String databaseId) {
        mServerDatabseId = databaseId;
    }

    public String getCameraStatus() {
        return mCameraStatus;
    }

    public void setCameraStatus(String cameraStatus) {
        mCameraStatus = cameraStatus;
    }

    public void resetEventCount() {
        mEventCount = 0;
    }

    public int getEventCount() {
        return mEventCount;
    }

    public int getIsSupportTimeZone() {//0 = flase, 1 = true;
        return nIsSupportTimeZone;
    }

    public int getGMTDiff() {
        return nGMTDiff;
    }

    public byte[] getTimeZoneString() {
        return szTimeZoneString;
    }

    public SStreamDef[] getSupportedStream() {
        SStreamDef[] result = new SStreamDef[mStreamDefs.size()];

        for (int i = 0; i < result.length; i++)
            result[i] = mStreamDefs.get(i);

        return result;
    }

    @Override
    public boolean getAudioInSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 1) == 0;
    }

    @Override
    public boolean getAudioOutSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 2) == 0;
    }

    public boolean getPanTiltSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 4) == 0;
    }

    public boolean getEventListSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 8) == 0;
    }

    public boolean getPlaybackSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 16) == 0;
    }

    public boolean getWiFiSettingSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 32) == 0;
    }

    public boolean getEventSettingSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 64) == 0;
    }

    public boolean getRecordSettingSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 128) == 0;
    }

    public boolean getSDCardFormatSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 256) == 0;
    }

    public boolean getVideoFlipSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 512) == 0;
    }

    public boolean getEnvironmentModeSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 1024) == 0;
    }

    public boolean getMultiStreamSupported(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 2048) == 0;
    }

    public int getAudioOutEncodingFormat(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 4096) == 0 ? AVFrame.MEDIA_CODEC_AUDIO_SPEEX : AVFrame.MEDIA_CODEC_AUDIO_ADPCM;
    }

    public boolean getVideoQualitySettingSupport(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 8192) == 0;
    }

    public boolean getDeviceInfoSupport(int avChannel) {
        return (this.getChannelServiceType(avChannel) & 16384) == 0;
    }

    @Override
    public void receiveChannelInfo(NSCamera arg0, int arg1, int arg2) {
        this.connect_state = arg2;
    }

    @Override
    public void receiveFrameData(NSCamera arg0, int arg1, Bitmap arg2) {

    }

    @Override
    public void receiveFrameInfo(NSCamera arg0, int arg1, long arg2, int arg3, int arg4, int arg5, int arg6) {

    }

    @Override
    public void receiveIOCtrlData(final NSCamera camera, final int avChannel, int avIOCtrlMsgType, final byte[] data) {

        if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP) {

            mStreamDefs.clear();

            int num = Packet.byteArrayToInt_Little(data, 0);

            if (avChannel == 0 && this.getMultiStreamSupported(Camera.DEFAULT_AV_CHANNEL)) {

                for (int i = 0; i < num; i++) {

                    byte[] buf = new byte[8];
                    System.arraycopy(data, i * 8 + 4, buf, 0, 8);
                    SStreamDef streamDef = new SStreamDef(buf);
                    mStreamDefs.add(streamDef);

                    ((Camera) camera).startChannel(streamDef.channel, mAcc, mPwd);
                }
            }

        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT) {

            int evtType = Packet.byteArrayToInt_Little(data, 12);

            if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT) {
                if (!bIsMotionDetected)
                    mEventCount++;
                bIsMotionDetected = true;
            } else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS) {
                bIsMotionDetected = false;
            } else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARM) {
                if (!bIsIOAlarm)
                    mEventCount++;
                bIsIOAlarm = true;
            } else if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS) {
                bIsIOAlarm = false;
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_RESP) {

            byte[] bcbSize = new byte[4];
            byte[] bIsSupportTimeZone = new byte[4];
            byte[] bGMTDiff = new byte[4];

            System.arraycopy(data, 0, bcbSize, 0, 4);
            cbSize = Packet.byteArrayToInt_Little(bcbSize);

            System.arraycopy(data, 4, bIsSupportTimeZone, 0, 4);
            nIsSupportTimeZone = Packet.byteArrayToInt_Little(bIsSupportTimeZone);

            System.arraycopy(data, 8, bGMTDiff, 0, 4);
            nGMTDiff = Packet.byteArrayToInt_Little(bGMTDiff);

            System.arraycopy(data, 12, szTimeZoneString, 0, 256);
            try {
                Log.i("szTimeZoneString", new String(szTimeZoneString, 0, szTimeZoneString.length, "utf-8"));
                Log.i("szTimeZoneString", new String(szTimeZoneString, 0, szTimeZoneString.length, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP) {

            byte[] bcbSize = new byte[4];
            byte[] bIsSupportTimeZone = new byte[4];
            byte[] bGMTDiff = new byte[4];

            System.arraycopy(data, 0, bcbSize, 0, 4);
            cbSize = Packet.byteArrayToInt_Little(bcbSize);

            System.arraycopy(data, 4, bIsSupportTimeZone, 0, 4);
            nIsSupportTimeZone = Packet.byteArrayToInt_Little(bIsSupportTimeZone);

            System.arraycopy(data, 8, bGMTDiff, 0, 4);
            nGMTDiff = Packet.byteArrayToInt_Little(bGMTDiff);

            System.arraycopy(data, 12, szTimeZoneString, 0, 256);

//			byte[] bcbSize = null;
//			
//			System.arraycopy(data, 0, bcbSize, 0, 4);	
//			cbSize = Packet.byteArrayToInt_Little(bcbSize);
//
//			if(cbSize == data.length)
//			{
//				Log.i("IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP ", "cbSize = " + cbSize);
//			}
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_RESP) {
            if (data[0] == 0) {
                state = CameraState.WillRebooting;
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RESET_DEFAULT_RESP) {
            if (data[0] == 0) {
                state = CameraState.WillReseting;
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPGRADE_STATUS) {
            state = CameraState.Upgrading;
            //AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus process = new AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus(data);
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_UPRADE_RESP) {
            if (data[0] == 0) {
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
                state = CameraState.WillUpgrading;
            }
        }
    }

    @Override
    public void receiveSessionInfo(NSCamera arg0, int arg1) {
        L.i("IOTCamera1", "uid:" + uid + " prestate:" + state.toString());
        this.connect_state = arg1;
        if (arg1 == NSCamera.CONNECTION_STATE_CONNECTED) {
            loginState++;
        }
        // && state != CameraState.WillUpgrading
        if ((this.connect_state == NSCamera.CONNECTION_STATE_CONNECTED) && state != CameraState.WillUpgrading && state != CameraState.WillRebooting && state != CameraState.WillReseting) {
            state = CameraState.None;
        } else if (this.connect_state == NSCamera.CONNECTION_STATE_TIMEOUT || this.connect_state == NSCamera.CONNECTION_STATE_CONNECT_FAILED || this.connect_state ==
                NSCamera.CONNECTION_STATE_DISCONNECTED || this.connect_state == NSCamera.CONNECTION_STATE_UNKNOWN_DEVICE) {
            if (state == CameraState.WillRebooting) {
                state = CameraState.Rebooting;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            } else if (state == CameraState.WillReseting) {
                state = CameraState.Reseting;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            }
            //重启，复位，升级固件超时处理
            if (System.currentTimeMillis() - beginRebootTime > rebootTimeout) {
                if (state != CameraState.None) {
                    state = CameraState.None;
                }
            }
//            else if (state == CameraState.WillUpgrading) {
//                state = CameraState.Upgrading;
//            }
        }
        L.i("IOTCamera1", "uid:" + uid + " afterState:" + state.toString());
//        if (this.connect_state == NSCamera.CONNECTION_STATE_TIMEOUT
//                || this.connect_state == NSCamera.CONNECTION_STATE_CONNECT_FAILED) {
//
//            reconnect();
//        }
    }

    Timer reconnetTimer;

    public void reconnect() {
        if (reconnetTimer != null) {
            reconnetTimer.cancel();
            reconnetTimer = null;
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("camera reconnect");
                stop();
                start();
            }
        };

        reconnetTimer = new Timer(true);
        reconnetTimer.schedule(task, 3000);
    }

    @Override
    public void initSendAudio(Camera paramCamera, boolean paramBoolean) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveOriginalFrameData(Camera paramCamera, int paramInt1,
                                         byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2,
                                         int paramInt3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveRGBData(Camera paramCamera, int paramInt1,
                               byte[] paramArrayOfByte, int paramInt2, int paramInt3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveRecordingData(Camera paramCamera, int avChannel, int paramInt1, String path) {

    }

    @Override
    public int wakeUp() {
        return super.wakeUp();
    }

    private long lastPushTime;

    public boolean shouldPush() {
        //相对摄像机时间的每30秒一次回
        boolean result = !(Math.abs(System.currentTimeMillis() - lastPushTime) < 60000);
        if (result && this.pushNotificationStatus > 0) {
            lastPushTime = System.currentTimeMillis();
        }
        return result && this.pushNotificationStatus > 0 && !isPlaying();
        //return this.pushNotificationStatus > 0;
    }

    public void openPush(final CameraClient.ServerResultListener2 succListner, final CameraClient.ServerResultListener2 errorListner) {

        CameraClient.shareCameraClient().openPushCamera(App.getContext(), uid, new CameraClient.ServerResultListener2() {
            @Override
            public void serverResult(String resultString, JSONObject jsonArray) {
                try {
                    if (jsonArray.getInt("ret_code") == 0) {
                        MyCamera.this.pushNotificationStatus = 1;
                        MyCamera.this.sync2Db(App.getContext());
                        if (succListner != null) {
                            succListner.serverResult(resultString, jsonArray);
                        }
                    } else {
                        if (errorListner != null) {
                            errorListner.serverResult(resultString, jsonArray);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (errorListner != null) {
                        errorListner.serverResult(resultString, jsonArray);
                    }
                }
            }
        }, errorListner);
    }

    public void closePush(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int intId = this.getIntId();
        manager.cancel(this.getUID(), intId);
        this.pushNotificationStatus = 0;
        this.sync2Db(App.getContext());
        CameraClient.shareCameraClient().closePushCamera(uid);
    }

    public void editPassword(String newPwd) {
        this.setPassword(newPwd);
    }

    public void remove(Context context) {
        TwsDataValue.cameraList().remove(this);
        DatabaseManager db = new DatabaseManager(context);
        db.removeDeviceByUID(this.uid);
    }

    public void save(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        long dbid = db.addDevice(this.getName(), this.getUID(), "", "", "admin", getPassword(), 0, 0, "0", this.getVideoQuality(), "", 0);
        TwsDataValue.cameraList().add(this);
    }

    public boolean sync2Db(Context context) {
        try {
            DatabaseManager db = new DatabaseManager(context);
            //manager.updateDeviceInfoByDBID(mDevice.DBID, mCamera.uid, mCamera.name, "", "", "admin", mCamera.pwd, evtNotify, 0);
            db.updateDeviceInfoByDBUID(this.getUID(), this.getName(), "", "", "admin", this.getPassword(), this.pushNotificationStatus, 0, 0, this.getVideoQuality());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExist() {
        boolean result = false;
        for (MyCamera c : TwsDataValue.cameraList()) {
            if (c == this) {
                result = true;
                break;
            }
        }
        return result;
    }

    public void modifyName(Context context, String newNickName) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceNameByDBUID(this.uid, newNickName);
    }

    public synchronized static void initP2P() {
        MyConfig.setP2pInitState(1);
        int result = Camera.init();
        MyConfig.setP2pInitState(result >= 0 ? 2 : 0);
    }

    public synchronized static void initPushSDK(Context context) {
        initUMPush(context);
        initXGPush(context);
    }

    public static void initXGPush(final Context context) {
        //MyConfig.setPushSdkIniteState(1);
        // 开启logcat输出，方便debug，发布时请关闭
        XGPushConfig.enableDebug(context, false);
        // 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
        // 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
        // 具体可参考详细的开发指南
        // 传递的参数为ApplicationContext
        //		Context context = getApplicationContext();
        //		XGPushManager.registerPush(context);

        // 2.36（不包括）之前的版本需要调用以下2行代码
        //		Intent service = new Intent(context, XGPushService.class);
        //		context.startService(service);


        // 其它常用的API：
        // 绑定账号（别名）注册：registerPush(context,account)或registerPush(context,account, XGIOperateCallback)，其中account为APP账号，可以为任意字符串（qq、openid或任意第三方），业务方一定要注意终端与后台保持一致。
        // 取消绑定账号（别名）：registerPush(context,"*")，即account="*"为取消绑定，解绑后，该针对该账号的推送将失效
        // 反注册（不再接收消息）：unregisterPush(context)
        // 设置标签：setTag(context, tagName)
        // 删除标签：deleteTag(context, tagName)

        XGPushManager.registerPush(context, new XGIOperateCallback() {

            @Override
            public void onSuccess(Object data, int flag) {
                String token = (String) data;
                TwsDataValue.XGToken = token;
                //TwsToast.showToast(context, token);

                //TwsToast.showToast(context, token);
                //MyConfig.setPushSdkIniteState(2);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                // MyConfig.setPushSdkIniteState(0);
            }
        });

    }

    private static void initUMPush(Context context) {

        /**
         * 初始化common库
         * 参数1:上下文，不能为空
         * 参数2:友盟 app key
         * 参数3:友盟 channel
         * 参数4:设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
         * 参数5:Push推送业务的secret
         */
        UMConfigure.init(context, MyConfig.getUmPushAppKey(), MyConfig.getAppName(), UMConfigure.DEVICE_TYPE_PHONE, MyConfig.getUmPushAppSecret());
        /**
         * 设置组件化的Log开关
         * 参数: boolean 默认为false，如需查看LOG设置为true
         */
        UMConfigure.setLogEnabled(true);
        /**
         * 设置日志加密
         * 参数：boolean 默认为false（不加密）
         */
        UMConfigure.setEncryptEnabled(false);

        PushAgent mPushAgent = PushAgent.getInstance(context);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                TwsDataValue.UMToken = deviceToken;
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
        mPushAgent.setPushCheck(false);
        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                JSONObject arrJson = null;
                String uid = null;
                int type = 0;
                int time = 0;
                try {
                    arrJson = new JSONObject(msg.custom);
                    JSONObject cJson = arrJson.getJSONObject("custom_content");
                    String jsonc = cJson.getString("content");
                    JSONObject conJson = new JSONObject(jsonc);
                    uid = conJson.getString("uid");
                    type = conJson.getInt("type");
                    time = conJson.getInt("time");
                    TwsTools.showAlarmNotification(context, uid, 0, System.currentTimeMillis());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        mPushAgent.setMessageHandler(messageHandler);
    }

    public interface AfterStartShowVideo {
        void doAfter();
    }

    public interface TaskExecute {
        void onPosted(Object data);
    }

    public boolean isConnected() {
        return this.connect_state == NSCamera.CONNECTION_STATE_CONNECTED;
    }

    public void saveSnapShot(final int channel, final String subFolder, final String fileName, final TaskExecute te) {
        this.asyncSnapshot(new TaskExecute() {
            @Override
            public void onPosted(Object bmp) {
                boolean isErr = false;

                String filePath = null;
                if (bmp != null) {
                    try {
                        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "");
                        File targetFolder = null;
                        if (subFolder == null) {
                            targetFolder = new File(rootFolder.getAbsolutePath() + "/android/data/" + MyConfig.getFolderName());
                        } else {
                            targetFolder = new File(rootFolder.getAbsolutePath() + "/" + MyConfig.getFolderName() + "/" + subFolder + "/" + MyCamera.this.getUID());
                        }

                        if (!rootFolder.exists()) {
                            rootFolder.mkdir();
                        }
                        if (!targetFolder.exists()) {
                            targetFolder.mkdirs();
                        }
                        filePath = targetFolder.getAbsolutePath() + "/" + fileName;
                        File ff = new File(filePath);

                        if (fileName.equalsIgnoreCase(MyCamera.this.getUID()) || !ff.exists()) {
                            isErr = !TwsTools.saveBitmap((Bitmap) bmp, filePath);
                            if (isErr) {
                                Thread.sleep(100);
                                bmp = MyCamera.super.Snapshot(channel);
                                isErr = !TwsTools.saveBitmap((Bitmap) bmp, filePath);
                            }
                            if (!isErr && fileName.equalsIgnoreCase(MyCamera.this.getUID())) {
                                MyCamera.this.setSnapshot((Bitmap) bmp);
                            }
                        } else {
                            isErr = false;
                        }
                    } catch (Exception ex) {
                        isErr = true;
                    }

                } else {
                    isErr = true;
                }
                if (te != null) {
                    te.onPosted(isErr ? null : filePath);
                }

            }
        }, channel);
    }

    public enum CameraState {
        None,
        WillRebooting,
        Rebooting,
        WillReseting,
        Reseting,
        WillUpgrading,
        Upgrading
    }

    public boolean hasChannel() {
        return this.mAVChannels.size() > 0;
    }


}
