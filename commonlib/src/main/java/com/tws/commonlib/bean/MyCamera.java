package com.tws.commonlib.bean;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd;
import com.tutk.IOTC.AVIOCTRLDEFs.SStreamDef;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.ICameraPlayStateCallback;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.App;
import com.tws.commonlib.R;
import com.tws.commonlib.base.CameraClient;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.db.DatabaseManager;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

public class MyCamera extends Camera implements com.tutk.IOTC.IRegisterIOTCListener, ICameraPlayStateCallback, IMyCamera {
    //public ConnectCallBack call;

    protected List<IIOTCListener> mIOTCListeners = Collections.synchronizedList(new Vector<IIOTCListener>());
    protected List<IPlayStateListener> mPlayStateListeners = Collections.synchronizedList(new Vector<IPlayStateListener>());
    public int LastAudioMode;

    private String mName;
    private String mUID;
    private String mAcc;
    private String mPwd;
    private String mServerDatabseId;
    private String mCameraStatus;
    private String modelName;

    private static final Object stopLocker = new Object();
    private int mEventCount = 0;
    private int cbSize = 0;
    private int nIsSupportTimeZone = 0;
    private int nGMTDiff = 0;
    private byte[] szTimeZoneString = new byte[256];
    private boolean bIsMotionDetected;
    private boolean bIsIOAlarm;
    private long rebootTimeout = 120000;
    private boolean isWakingUp = false;
    private boolean isStopManually = false;
    AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoResp deviceInfo = null;
    AVIOCTRLDEFs.SMsgAVIoctrlDevModelConfig deviceConfig = null;
    private BatteryStatus batteryStatus = new BatteryStatus();
    //bengin index,0:云台，1：:双向语音,2:预置位,3:光学变焦,4:SD卡槽 20170316-yilu
    private byte[] functionFlag;

    public float getVideoRatio(Context context) {
        if (videoRatio == 0) {
            DatabaseManager db = new DatabaseManager(context);
            videoRatio = db.getDeviceVideoRatio(this.uid);
            if (videoRatio == 0) {
                videoRatio = (float) 16 / 9;
            }
        }
        return videoRatio;
    }

    public void setVideoRatio(Context context, float videoRatio) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceVideoRatio(this.uid, videoRatio);
        this.videoRatio = videoRatio;
    }

    private float videoRatio = 0;

    public boolean isFirstLogin() {
        return loginState <= 0;
    }

    private void setFirstLogin(boolean f) {
        if (f) {
            loginState = -1;
        } else {
            loginState = 1;
        }
    }

    public String getAccount() {
        return mAcc;
    }

    public String getSoftVersion() {
        return getSystemTypeVersion() + "." + getVendorTypeVersion() + "." + getCustomTypeVersion();
    }

    public void setSoftVersion(String version) {
        String[] arr = version.split("\\.");
        if (arr.length == 3) {
            setSystemTypeVersion(arr[0]);
            setVendorTypeVersion(arr[1]);
            setCustomTypeVersion(arr[2]);
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

    @Override
    public int getCameraModel() {
        return 0;
    }

    @Override
    public int setCameraModel(int mode) {
        return 0;
    }

    @Override
    public String getCameraStateDesc() {
        if (this.getState() == CameraState.None || this.getState() == null) {
            if (this.connect_state == NSCamera.CONNECTION_STATE_NONE || this.connect_state == CONNECTION_STATE_CONNECTING || this.isWakingUp() || this.connect_state == CONNECTION_STATE_FIND_DEVICE) {
                return App.getContext().getString(R.string.camera_state_connecting);
            } else if (this.connect_state == NSCamera.CONNECTION_STATE_CONNECTED) {
                return App.getContext().getString(R.string.camera_state_connected);
            } else if (this.connect_state == CONNECTION_STATE_DISCONNECTED || this.connect_state == CONNECTION_STATE_UNKNOWN_DEVICE || this.connect_state == CONNECTION_STATE_TIMEOUT || this.connect_state == CONNECTION_STATE_UNSUPPORTED || this.connect_state == CONNECTION_STATE_CONNECT_FAILED) {
                return App.getContext().getString(R.string.camera_state_disconnect);
            } else if (this.connect_state == CONNECTION_STATE_WRONG_PASSWORD) {
                return App.getContext().getString(R.string.camera_state_passwordWrong);
            } else if (this.connect_state == CONNECTION_STATE_SLEEPING) {
                return App.getContext().getString(R.string.camera_state_sleep);
            } else {
                return App.getContext().getString(R.string.camera_state_disconnect);
            }
        } else {
            if (this.getState() == CameraState.Rebooting || this.getState() == CameraState.WillRebooting) {
                return App.getContext().getString(R.string.tips_rebooting);
            } else if (this.getState() == CameraState.Reseting || this.getState() == CameraState.WillReseting) {
                return App.getContext().getString(R.string.tips_reseting);
            } else if (this.getState() == CameraState.Upgrading || this.getState() == CameraState.WillUpgrading) {
                return App.getContext().getString(R.string.tips_upgrading);
            }

        }
        return "";
    }

    @Override
    public int getCameraStateBackgroundColor() {
        if (this.getState() == CameraState.None || this.getState() == null) {
            if (this.connect_state == NSCamera.CONNECTION_STATE_NONE || this.connect_state == CONNECTION_STATE_CONNECTING || this.isWakingUp() || this.connect_state == CONNECTION_STATE_FIND_DEVICE) {
                return R.drawable.shape_state_connecting;
            } else if (this.connect_state == NSCamera.CONNECTION_STATE_CONNECTED) {
                return R.drawable.shape_state_online;
            } else if (this.connect_state == CONNECTION_STATE_DISCONNECTED || this.connect_state == CONNECTION_STATE_UNKNOWN_DEVICE || this.connect_state == CONNECTION_STATE_TIMEOUT || this.connect_state == CONNECTION_STATE_UNSUPPORTED || this.connect_state == CONNECTION_STATE_CONNECT_FAILED) {
                return R.drawable.shape_state_offline;
            } else if (this.connect_state == CONNECTION_STATE_WRONG_PASSWORD || this.connect_state == CONNECTION_STATE_SLEEPING || this.connect_state == CONNECTION_STATE_WAKINGUP) {
                return R.drawable.shape_state_pwderror;
            } else {
                return R.drawable.shape_state_offline;
            }
        } else {
            return R.drawable.shape_state_connecting;
        }
        //return 0;
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

    public int getIntId() {
        return TwsTools.GetUIDIntValue(this.getUid());
    }

    @Override
    public int getTotalSDSize() {
        return 0;
    }

    @Override
    public void setTotalSDSize(int total) {

    }

    public synchronized int getEventNum() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(App.getContext());
        int eventnum = sp.getInt("eventnum_" + this.getUid(), 0);
        return eventnum;
    }

    @Override
    public void setEventNum(int eventNum) {
        this.pushNotificationStatus = eventNum;
    }

    public synchronized int refreshEventNum(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int eventnum = sp.getInt("eventnum_" + this.getUid(), 0);
        eventnum++;
        sp.edit().putInt("eventnum_" + this.getUid(), eventnum).commit();
        return eventnum;
    }

    public synchronized int clearEventNum(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int eventnum = sp.getInt("eventnum_" + this.getUid(), 0);
        sp.edit().putInt("eventnum_" + this.getUid(), 0).commit();
        return eventnum;
    }

    public synchronized void setEventNum(Context context, int eventNum) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt("eventnum_" + this.getUid(), 0).commit();
    }


    public CameraState getState() {
        return cameraState == null ? CameraState.None : cameraState;
    }

    public void setState(CameraState state) {
        this.cameraState = state;
    }

    private CameraState cameraState = CameraState.None;

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

    @Override
    public long getDatabaseId() {
        return 0;
    }

    @Override
    public void setDatabaseId(long dbId) {

    }

    @Override
    public void registerIOTCListener(IIOTCListener listener) {
        if (!mIOTCListeners.contains(listener)) {
            Log.i("NSCamera", "register IOTC listener");
            mIOTCListeners.add(listener);
        }
    }

    @Override
    public void unregisterIOTCListener(IIOTCListener listener) {
        if (mIOTCListeners.contains(listener)) {
            Log.i("NSCamera", "unregister IOTC listener");
            mIOTCListeners.remove(listener);
        }
    }

    @Override
    public void registerPlayStateListener(IPlayStateListener listener) {
        if (!mPlayStateListeners.contains(listener)) {
            Log.i("NSCamera", "register IOTC listener");
            mPlayStateListeners.add(listener);
        }
    }

    @Override
    public void unregisterPlayStateListener(IPlayStateListener listener) {
        if (mPlayStateListeners.contains(listener)) {
            Log.i("NSCamera", "unregister IOTC listener");
            mPlayStateListeners.remove(listener);
        }
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
        this.registerPlayStateListener(this);
        setPlaying(false);
    }

    private Bitmap snapshot;

    public Bitmap getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Bitmap snapshot) {
        if (this.snapshot != null && !this.snapshot.isRecycled()) {
            this.snapshot.recycle();
            this.snapshot = null;
            System.gc();
        }
        this.snapshot = snapshot;
    }

    public void asyncSnapshot(final TaskExecute te, final int channel) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (te != null) {
                    te.onPosted(MyCamera.this, MyCamera.super.Snapshot(channel));
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
                L.i("IOTCamera", "start connect");
                MyCamera.super.start();

                MyCamera.this.connect(MyCamera.this.uid);
                MyCamera.this.startChannel(Camera.DEFAULT_AV_CHANNEL, MyCamera.this.user, MyCamera.this.pwd);
                MyCamera.this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL,
                        AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ,
                        AVIOCTRLDEFs.SMsgAVIoctrlGetAudioOutFormatReq
                                .parseContent());
                if (ex != null) {
                    ex.onPosted(MyCamera.this, null);
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
                    ex.onPosted(MyCamera.this, null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();
    }

    AsyncTask wakeupTask;

    @Override
    public void asyncWakeUp(final TaskExecute ex) {
        isWakingUp = true;
        if (wakeupTask != null) {
            wakeupTask.cancel(true);
        }
        wakeupTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                MyCamera.this.wakeUp();
                if (ex != null) {
                    ex.onPosted(MyCamera.this, null);
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
                    ex.onPosted(MyCamera.this, null);
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
                    te.onPosted(MyCamera.this, null);
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
                    te.onPosted(MyCamera.this, null);
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
                    te.onPosted(MyCamera.this, null);
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
                    te.onPosted(MyCamera.this, null);
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
                    te.onPosted(MyCamera.this, null);
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
                    te.onPosted(MyCamera.this, null);
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
        isStopManually = false;
        super.connect(uid);
        mUID = uid;
    }

    AsyncTask connectTask2;

    @Override
    public void connect() {
        if (connectTask2 != null) {
            connectTask2.cancel(true);
        }
        connectTask2 = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                MyCamera.super.connect(getUid());
                mUID = getUid();
                return null;
            }
        }.execute();
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
        isStopManually = true;
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

    public String getNickName() {
        return mName;
    }

    public void setNickName(String nickName) {
        mName = nickName;
        this.name = nickName;
    }

    public String getUid() {
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
    public void receiveChannelInfo(NSCamera arg0, int channel, int state) {
        this.connect_state = state;

        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveChannelInfo(MyCamera.this, channel, state);
        }
    }

    @Override
    public void receiveFrameData(NSCamera arg0, int channel, Bitmap bmp) {
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveFrameData(MyCamera.this, channel, bmp);
        }
    }

    @Override
    public void receiveFrameInfo(NSCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount,
                                 int incompleteFrameCount) {
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveFrameInfo(MyCamera.this, avChannel, bitRate, frameRate, onlineNm, frameCount,
                    incompleteFrameCount);
        }
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
            cbSize = Packet.byteArrayToInt_Little(bcbSize, 0);

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
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_RESP || avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_SYSTEM_RESP) {
            if (data[0] == 0) {
                cameraState = CameraState.WillRebooting;
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RESET_DEFAULT_RESP) {
            if (data[0] == 0) {
                cameraState = CameraState.WillReseting;
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPGRADE_STATUS) {
            cameraState = CameraState.Upgrading;
            final AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus process = new AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus(data);
            if (process.p >= 100) {
                this.setState(CameraState.WillRebooting);
            }
            //AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus process = new AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus(data);
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_UPRADE_RESP) {
            if (data[0] == 0) {
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
                cameraState = CameraState.WillUpgrading;
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIME_INFO_RESP) {
            if (this.isFirstLogin()) {
                this.setFirstLogin(false);
                AVIOCTRLDEFs.SMsgAVIoctrlTime time = new AVIOCTRLDEFs.SMsgAVIoctrlTime(data);
                if (time.adjustFlg == 0) {
                    Calendar phoneCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
                    phoneCal.setTimeInMillis(System.currentTimeMillis());
                    byte[] phoneTime = AVIOCTRLDEFs.STimeDay.parseContent(phoneCal.get(Calendar.YEAR), phoneCal.get(Calendar.MONTH) + 1, phoneCal.get(Calendar.DAY_OF_MONTH),
                            phoneCal.get(Calendar.DAY_OF_WEEK), phoneCal.get(Calendar.HOUR_OF_DAY), phoneCal.get(Calendar.MINUTE), phoneCal.get(Calendar.SECOND));
                    byte[] data2 = AVIOCTRLDEFs.SMsgAVIoctrlTime.parseContent(phoneTime, 1, TwsDataValue.NTP_SERVER, 1);
                    this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ, data2);
                }
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP) {
            deviceInfo = new AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoResp(data);
            if (this.modelName == null || !this.modelName.equals(TwsTools.getString(deviceInfo.model))) {
                this.modelName = TwsTools.getString(deviceInfo.model);
                this.updateModelName(App.getTopActivity());
                getInitConfig();
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEMODE_TO_SHARE_RESP) {
            int timeMode = Packet.byteArrayToInt_Little(data, 0);
            //0:China   1:America  2:Europe
            Log.i("TimeMode", timeMode + "");
            if (timeMode == 2) {
                byte[] reqData = new byte[8];
                System.arraycopy(Packet.intToByteArray_Little(0), 0, reqData, 0, 4);
                this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIMEMODE_TO_SHARE_REQ, reqData);
            }

        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIMEMODE_TO_SHARE_RESP) {
            if (Packet.byteArrayToInt_Little(data, 0) == 0) {
                this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEMODE_TO_SHARE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
            }
        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_DEVICEMODEL_CONFIG_RESP) {
            deviceConfig = new AVIOCTRLDEFs.SMsgAVIoctrlDevModelConfig(data);

        } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_BAT_PRAM_RESP) {
            if (batteryStatus == null) {
                batteryStatus = new BatteryStatus();
            }
            AVIOCTRLDEFs.SMsgGetBatPramResp batParams = new AVIOCTRLDEFs.SMsgGetBatPramResp(data);
            batteryStatus.setBatPercent(batParams.bat_percent);
            batteryStatus.setWorkMode(batParams.work_mode);
            batteryStatus.setTime(new Date().getTime());
            updateBatteryStatus(App.getTopActivity());
        }

        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveIOCtrlData(MyCamera.this, avChannel, avIOCtrlMsgType, data);
        }
    }

    public  void getInitConfig(){
        this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
        if (this.getSupplier() == Supllier.AN) {
            //this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEMODE_TO_SHARE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
            // this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_DEVICEMODEL_CONFIG_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
            this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_BAT_PRAM_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());

        } else if(this.getSupplier() == Supllier.FB){
            this.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIME_INFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetTimeReq.parseContent(true));
        }
    }

    @Override
    public void receiveSessionInfo(NSCamera arg0, int sessionState) {
        if (isStopManually) {
            return;
        }
        L.i("IOTCamera1", "uid:" + uid + " prestate:" + cameraState.toString() + " sessionState:" + sessionState);
        this.connect_state = sessionState;
//        if (sessionState != NSCamera.CONNECTION_STATE_SLEEPING && sessionState != NSCamera.CONNECTION_STATE_WAKINGUP && sessionState != NSCamera.CONNECTION_STATE_CONNECTING && sessionState != NSCamera.CONNECTION_STATE_NONE) {
//
//            L.i("IOTCamera1isWakingUp", "uid:" + uid + " sessionState:" + sessionState);
//        }
        if (sessionState == NSCamera.CONNECTION_STATE_CONNECTED) {
            loginState++;
            isWakingUp = false;
            if (MyConfig.isStrictPwd() && this.getPassword().equals(TwsDataValue.DEFAULT_PASSWORD)) {//检测该摄像机密码格式是否符合要求

            } else if (this.isFirstLogin()) {
                getInitConfig();
            }
        } else if (sessionState == NSCamera.CONNECTION_STATE_WRONG_PASSWORD) {
            isWakingUp = false;
        }
        // && state != CameraState.WillUpgrading
        if ((this.connect_state == NSCamera.CONNECTION_STATE_CONNECTED) && cameraState != CameraState.WillUpgrading && cameraState != CameraState.WillRebooting && cameraState != CameraState.WillReseting) {
            cameraState = CameraState.None;
        } else if (this.connect_state == NSCamera.CONNECTION_STATE_TIMEOUT || this.connect_state == NSCamera.CONNECTION_STATE_CONNECT_FAILED || this.connect_state ==
                NSCamera.CONNECTION_STATE_DISCONNECTED || this.connect_state == NSCamera.CONNECTION_STATE_UNKNOWN_DEVICE) {
            if (cameraState == CameraState.WillRebooting) {
                cameraState = CameraState.Rebooting;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            } else if (cameraState == CameraState.WillReseting) {
                cameraState = CameraState.Reseting;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            }
            //重启，复位，升级固件超时处理
            if (System.currentTimeMillis() - beginRebootTime > rebootTimeout) {
                if (cameraState != CameraState.None) {
                    cameraState = CameraState.None;
                }
            }
//            else if (state == CameraState.WillUpgrading) {
//                state = CameraState.Upgrading;
//            }
        }
        L.i("IOTCamera1", "uid:" + uid + " afterState:" + cameraState.toString());
//        if (this.connect_state == NSCamera.CONNECTION_STATE_TIMEOUT
//                || this.connect_state == NSCamera.CONNECTION_STATE_CONNECT_FAILED) {
//
//            reconnect();
//        }
        int accSessionState = sessionState;
        if (SessionStateHashMap.containsKey(sessionState)) {
            accSessionState = (int) SessionStateHashMap.get(sessionState);
        }
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveSessionInfo(MyCamera.this, accSessionState);
        }
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
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.initSendAudio(MyCamera.this, paramBoolean);
        }
    }

    @Override
    public void receiveOriginalFrameData(Camera paramCamera, int paramInt1,
                                         byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2,
                                         int paramInt3) {
        // TODO Auto-generated method stub
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveOriginalFrameData(MyCamera.this, paramInt1,
                    paramArrayOfByte1, paramInt2, paramArrayOfByte2,
                    paramInt3);
        }
    }

    @Override
    public void receiveRGBData(Camera paramCamera, int paramInt1,
                               byte[] paramArrayOfByte, int paramInt2, int paramInt3) {
        // TODO Auto-generated method stub
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveRGBData(MyCamera.this, paramInt1,
                    paramArrayOfByte, paramInt2, paramInt3);
        }
    }

    @Override
    public void receiveRecordingData(Camera paramCamera, int avChannel, int paramInt1, String path) {

        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveRecordingData(MyCamera.this, avChannel, paramInt1, path);
        }
    }


    @Override
    public void receiveRecordingData(Camera paramCamera, int avChannel, int paramInt1, String path, int dataType, int dataLength, byte[] data) {


    }

    @Override
    public int wakeUp() {
        isWakingUp = true;
        stop();
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

        if ((TwsDataValue.XGToken == null || TwsDataValue.XGToken.isEmpty()) && (TwsDataValue.UMToken == null || TwsDataValue.UMToken.isEmpty())) {
            if (TwsDataValue.XGToken == null || TwsDataValue.XGToken.isEmpty()) {
                MyCamera.initXGPush(App.getContext());
            } else {
                MyCamera.initUMPush(App.getContext());
            }
            return;
        }

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
        manager.cancel(this.getUid(), intId);
        this.pushNotificationStatus = 0;
        this.sync2Db(App.getContext());
        if ((TwsDataValue.XGToken == null || TwsDataValue.XGToken.isEmpty()) && (TwsDataValue.UMToken == null || TwsDataValue.UMToken.isEmpty())) {
            if (TwsDataValue.XGToken == null || TwsDataValue.XGToken.isEmpty()) {
                MyCamera.initXGPush(App.getContext());
            } else {
                MyCamera.initUMPush(App.getContext());
            }
            return;
        }
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
        long dbid = db.addDevice(this.getNickName(), this.getUid(), "", "", "admin", getPassword(), 0, 0, "0", this.getVideoQuality(), "", 0);
        TwsDataValue.cameraList().add(this);
    }

    public boolean sync2Db(Context context) {
        try {
            DatabaseManager db = new DatabaseManager(context);
            //manager.updateDeviceInfoByDBID(mDevice.DBID, mCamera.uid, mCamera.name, "", "", "admin", mCamera.pwd, evtNotify, 0);
            db.updateDeviceInfoByDBUID(this.getUid(), this.getNickName(), "", "", "admin", this.getPassword(), this.pushNotificationStatus, 0, 0, this.getVideoQuality());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateModelName(Context context) {
        try {
            DatabaseManager db = new DatabaseManager(context);
            db.updateDeviceModel(this.getUid(), this.getModelName());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateBatteryStatus(Context context) {
        try {
            if (batteryStatus != null) {
                DatabaseManager db = new DatabaseManager(context);
                db.updateDeviceBatteryStatus(this.getUid(), batteryStatus.getWorkMode(), batteryStatus.getBatPercent(), batteryStatus.getTime());
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExist() {
        boolean result = false;
        for (IMyCamera c : TwsDataValue.cameraList()) {
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
        if (MyConfig.getPushSdkIniteState() == 1) {
            return;
        }
        MyConfig.setPushSdkIniteState(1);
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
                MyConfig.setPushSdkIniteState(2);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                MyConfig.setPushSdkIniteState(0);
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
        UMConfigure.setLogEnabled(false);
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

    @Override
    public void callbackState(Camera camera, int channel, int state, int w, int h) {
        for (int i = 0; i < mPlayStateListeners.size(); i++) {
            IPlayStateListener listener = mPlayStateListeners.get(i);
            listener.callbackState(MyCamera.this, channel, state, w, h);
        }
    }

    @Override
    public void callbackPlayUTC(Camera var1, int var2) {
        for (int i = 0; i < mPlayStateListeners.size(); i++) {
            IPlayStateListener listener = mPlayStateListeners.get(i);
            listener.callbackPlayUTC(MyCamera.this, var2);
        }
    }

    public interface AfterStartShowVideo {
        void doAfter();
    }


    public boolean isConnected() {
        return this.connect_state == NSCamera.CONNECTION_STATE_CONNECTED;
    }

    public boolean isSessionConnected() {
        return this.connect_state == NSCamera.CONNECTION_STATE_FIND_DEVICE || isConnected() || isPasswordWrong();
    }

    public void saveSnapShot(final int channel, final String filePath, final String fileName, final TaskExecute te) {
        this.asyncSnapshot(new TaskExecute() {
            @Override
            public void onPosted(IMyCamera c, Object bmp) {
                boolean isErr = false;

                String fullFilePath = null;
                if (bmp != null) {
                    try {
                        fullFilePath = filePath + "/" + fileName;
                        File ff = new File(fullFilePath);

                        if (fileName.equalsIgnoreCase(MyCamera.this.getUid()) || !ff.exists()) {
                            isErr = !TwsTools.saveBitmap((Bitmap) bmp, fullFilePath);
//                            if (isErr) {
//                                Thread.sleep(100);
//                                bmp = MyCamera.super.Snapshot(channel);
//                                isErr = !TwsTools.saveBitmap((Bitmap) bmp, fullFilePath);
//                            }
                            bmp = null;
                            if (fileName.equalsIgnoreCase(c.getUid())) {
                                c.setSnapshot(null);
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
                    te.onPosted(c, isErr ? null : filePath);
                }

            }
        }, channel);
    }


    public boolean hasChannel() {
        return this.mAVChannels.size() > 0;
    }

    public boolean isNotConnect() {
        return this.connect_state == CONNECTION_STATE_NONE;
    }

    public boolean isDisconnect() {
        return this.connect_state >= NSCamera.CONNECTION_STATE_DISCONNECTED && this.connect_state <= NSCamera.CONNECTION_STATE_CONNECT_FAILED;
    }

    @Override
    public boolean isPasswordWrong() {
        return this.connect_state == NSCamera.CONNECTION_STATE_WRONG_PASSWORD;
    }

    @Override
    public boolean isSleeping() {
        return this.connect_state == NSCamera.CONNECTION_STATE_SLEEPING;
    }

    @Override
    public boolean isWakingUp() {
        return this.connect_state == NSCamera.CONNECTION_STATE_WAKINGUP || isWakingUp;
    }

    @Override
    public boolean isConnecting() {
        return this.connect_state == NSCamera.CONNECTION_STATE_CONNECTING || this.connect_state == NSCamera.CONNECTION_STATE_FIND_DEVICE;
    }

    @Override
    public boolean isPushOpen() {
        return this.pushNotificationStatus > 0;
    }

    @Override
    public boolean setPushOpen(boolean open) {
        if (this.pushNotificationStatus <= 0) {
            this.pushNotificationStatus = 1;
        }
        return true;
    }

    public CameraP2PType getP2PType() {
        return CameraP2PType.TutkP2P;
    }

    public Supllier getSupplier() {
        if (this.modelName == null) {
            return Supllier.UnKnown;
        } else {
            return modelName.equals("E936") ? Supllier.AN : Supllier.FB;
        }
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public void registerDownloadListener(IDownloadCallback listener) {

    }

    @Override
    public void unregisterDownloadListener(IDownloadCallback listener) {

    }

    public boolean supportCable() {
        if (this.modelName != null && modelName.equals("E936")) {
            return false;
        } else {
            return true;
        }
    }

    private byte[] defaultFunctionFlag() {
        if (MyConfig.getDefaultCameraFunction() == null) {
            return new byte[]{49, 49, 48, 48, 49};
        } else {
            return MyConfig.getDefaultCameraFunction();
        }
    }

    public byte[] getFunctionFlag(Context context) {
        if (this.functionFlag == null) {
            DatabaseManager db = new DatabaseManager(context);
            this.functionFlag = db.getDeviceFunction(this.getUid());
            if (this.functionFlag == null) {
                return this.defaultFunctionFlag();
            } else {
                return this.functionFlag;
            }
        } else {
            return this.functionFlag;
        }
    }

    public boolean isDefaultFunc() {
        return this.functionFlag == null;
    }

    public void setFunctionFlag(Context context, byte[] functionFlag) {
        this.functionFlag = functionFlag;
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceFunction(this.getUid(), functionFlag);
    }

    public boolean hasSetFunctionFlag() {
        return this.functionFlag != null;
    }

    public boolean hasPTZ(Context context) {
        return getFunctionFlag(context)[0] == 49;
    }

    public boolean hasListen(Context context) {
        return true;// getFunctionFlag(context)[1] == 49;
    }

    public boolean hasPreset(Context context) {
        return this.getSupplier()!=Supllier.AN;// getFunctionFlag(context)[2] == 49;
    }

    public boolean hasZoom(Context context) {
        return false;// getFunctionFlag(context)[3] == 49;
    }

    public boolean hasSDSlot(Context context) {
        return true;// getFunctionFlag(context)[4] == 49;
    }

    @Override
    public boolean supportBattery() {
        return this.batteryStatus.getTime() > 0;
    }

    public BatteryStatus getBatteryStatus() {
        return this.batteryStatus;
    }

    public static HashMap IOTCHashMap;
    public static HashMap SessionStateHashMap;

    static {
        SessionStateHashMap = new HashMap();
        IOTCHashMap = new HashMap();

        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_NONE, TwsSessionState.CONNECTION_STATE_NONE);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_CONNECTING, TwsSessionState.CONNECTION_STATE_CONNECTING);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_CONNECTED, TwsSessionState.CONNECTION_STATE_CONNECTED);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_DISCONNECTED, TwsSessionState.CONNECTION_STATE_DISCONNECTED);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_UNKNOWN_DEVICE, TwsSessionState.CONNECTION_STATE_UNKNOWN_DEVICE);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_WRONG_PASSWORD, TwsSessionState.CONNECTION_STATE_WRONG_PASSWORD);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_TIMEOUT, TwsSessionState.CONNECTION_STATE_TIMEOUT);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_UNSUPPORTED, TwsSessionState.CONNECTION_STATE_UNSUPPORTED);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_CONNECT_FAILED, TwsSessionState.CONNECTION_STATE_CONNECT_FAILED);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_SLEEPING, TwsSessionState.CONNECTION_STATE_SLEEPING);
        SessionStateHashMap.put(NSCamera.CONNECTION_STATE_WAKINGUP, TwsSessionState.CONNECTION_STATE_WAKINGUP);
    }
}
