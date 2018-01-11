package com.tws.commonlib.bean;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.hichip.base.HiLog;
import com.hichip.base.SharePreUtils;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.control.HiGLMonitor;
import com.hichip.push.HiPushSDK;
import com.hichip.sdk.HiChipP2P;
import com.hichip.sdk.HiChipSDK;
import com.hichip.tools.Packet;
import com.tencent.android.tpush.XGPushConfig;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.App;
import com.tws.commonlib.R;
import com.tws.commonlib.base.CameraClient;
import com.tws.commonlib.base.CameraFunction;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.db.DatabaseManager;
import com.tws.commonlib.fragment.CameraFragment;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Created by Administrator on 2018/1/7.
 */

public class HichipCamera extends HiCamera implements IMyCamera, ICameraIOSessionCallback, ICameraPlayStateCallback {

    protected List<IIOTCListener> mIOTCListeners = Collections.synchronizedList(new Vector<IIOTCListener>());
    protected List<IPlayStateListener> mPlayStateListeners = Collections.synchronizedList(new Vector<IPlayStateListener>());
    private String nickName;
    private CameraState cameraState;
    private int eventNum;
    private Bitmap snapshot;
    private long lastPushTime;
    private int videoQuality;
    private com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE timezone = null;
    private com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE_EXT timezone_ext = null;
    private long beginRebootTime;
    private long rebootTimeout = 120000;
    //bengin index,0:云台，1：:双向语音,2:预置位,3:光学变焦,4:SD卡槽 20170316-yilu
    private byte[] functionFlag;

    private float videoRatio = 0;
    private boolean isInitTime = false;
    private boolean hasSummerTimer;

    public HichipCamera(Context context, String nikename, String uid, String username, String password) {
        super(context, uid, username, password);
        this.nickName = nikename;
        this.registerIOSessionListener(this);
        this.registerPlayStateListener(this);
    }

    public boolean isInitTime() {
        return isInitTime;
    }

    public void setInitTime(boolean isInitTime) {
        this.isInitTime = isInitTime;
    }

    public void setSummerTimer(boolean hasSummerTimer) {
        this.hasSummerTimer = hasSummerTimer;
    }

    public boolean getSummerTimer() {
        return this.hasSummerTimer;
    }

    @Override
    public String getNickName() {
        return nickName;
    }

    @Override
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public String getAccount() {
        return "admin";
    }

    @Override
    public int getEventCount() {
        return 0;
    }

    @Override
    public float getVideoRatio(Context context) {
        if (videoRatio == 0) {
            DatabaseManager db = new DatabaseManager(context);
            videoRatio = db.getDeviceVideoRatio(this.getUid());
            if (videoRatio == 0) {
                videoRatio = (float) 16 / 9;
            }
        }
        return videoRatio;
    }

    @Override
    public void setVideoRatio(Context context, float ratio) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceVideoRatio(this.getUid(), videoRatio);
        this.videoRatio = videoRatio;
    }

    @Override
    public boolean isFirstLogin() {
        return false;
    }


    @Override
    public CameraState getState() {
        return cameraState == null ? CameraState.None : cameraState;
    }

    private void setState(CameraState state) {
        cameraState = state;
    }

    @Override
    public int getEventNum() {
        return eventNum;
    }

    @Override
    public void setEventNum(int eventNum) {
        this.eventNum = eventNum;
    }

    @Override
    public int refreshEventNum(Context context) {
        this.eventNum++;
        return this.eventNum;
    }

    @Override
    public int clearEventNum(Context context) {
        this.eventNum = 0;
        return this.eventNum;
    }

    @Override
    public String getSoftVersion() {
        return null;
    }

    @Override
    public void setSoftVersion(String version) {

    }

    @Override
    public Bitmap getSnapshot() {
        return snapshot;
    }

    @Override
    public void setSnapshot(Bitmap snapshot) {
        if (this.snapshot != null && !this.snapshot.isRecycled()) {
            this.snapshot.recycle();
            this.snapshot = null;
            System.gc();
        }
        this.snapshot = snapshot;
    }

    @Override
    public void asyncSnapshot(final TaskExecute te, int channel) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (te != null) {
                    te.onPosted(HichipCamera.this, HichipCamera.super.getSnapshot());
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
        if (getUid() != null && getUid().length() > 4) {
            String temp = getUid().substring(0, 5);
            String str = getUid().substring(0, 4);
            if (temp.equalsIgnoreCase("FDTAA") || str.equalsIgnoreCase("DEAA") || str.equalsIgnoreCase("AAES")) {
                return;
            } else {
                super.connect();
                return;
            }
        } else {
            return;
        }
    }

    AsyncTask startTask;

    @Override
    public void asyncStart(final TaskExecute ex) {
        if (startTask != null) {
            startTask.cancel(true);
        }
        startTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                HichipCamera.this.start();
                if (ex != null) {
                    ex.onPosted(HichipCamera.this, null);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void stop() {
        setPlaying(false);
        super.disconnect();
    }

    AsyncTask stopTask;

    @Override
    public void asyncStop(final TaskExecute ex) {
        if (stopTask != null) {
            stopTask.cancel(true);
        }
        stopTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                HichipCamera.this.stop();
                if (ex != null) {
                    ex.onPosted(HichipCamera.this, null);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void startVideo() {

    }

    @Override
    public void asyncStartVideo(TaskExecute te) {

    }

    AsyncTask startVideoTask;

    public void asyncStartVideo(final HiGLMonitor monitor, final TaskExecute te) {
        if (startVideoTask != null) {
            startVideoTask.cancel(true);
        }
        startVideoTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                HichipCamera.this.startLiveShow(HichipCamera.this.getVideoQuality(), monitor);
                System.out.println("camera video start" + HichipCamera.this.getUid());
                setPlaying(true);
                if (te != null) {
                    te.onPosted(HichipCamera.this, null);
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
    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    private boolean isPlaying;

    @Override
    public void stopRecording(int avChannel) {
        super.stopRecording();
    }

    @Override
    public void asyncStopRecording(int avChannel) {
        super.stopRecording();
    }

    @Override
    public void startRecording(String file, int channel) {
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveRecordingData(HichipCamera.this, -999, 0, "");
        }
        super.startRecording(file);
    }

    @Override
    public void sendIOCtrl(int avChannel, int type, byte[] data) {
        super.sendIOCtrl(type, data);
    }

    @Override
    public void asyncSendIOCtrl(int avChannel, int type, byte[] data) {
        super.sendIOCtrl(type, data);
    }

    @Override
    public void stopVideo() {

        setPlaying(false);
        super.stopLiveShow();
    }

    AsyncTask stopVideoTask;

    @Override
    public void asyncStopVideo(final TaskExecute te) {
        if (stopVideoTask != null) {
            stopVideoTask.cancel(true);
        }
        stopVideoTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                HichipCamera.this.stopVideo();
                if (te != null) {
                    te.onPosted(HichipCamera.this, null);
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
        super.startListening();
    }

    AsyncTask startAudioTask;

    @Override
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
                HichipCamera.this.startAudio();
                if (te != null) {
                    te.onPosted(HichipCamera.this, null);
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
        super.stopListening();
    }

    AsyncTask stopAudioTask;

    @Override
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
                HichipCamera.this.stopListening();
                if (te != null) {
                    te.onPosted(HichipCamera.this, null);
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
        super.startTalk();
    }

    AsyncTask startSpeakTask;

    @Override
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
                HichipCamera.this.startSpeak();
                if (te != null) {
                    te.onPosted(HichipCamera.this, null);
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
        super.stopTalk();
    }

    AsyncTask stopSpeakTask;

    @Override
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
                HichipCamera.this.stopSpeak();
                if (te != null) {
                    te.onPosted(HichipCamera.this, null);
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
    public void ptz(int type) {

    }

    @Override
    public void asyncPtz(int type) {

    }

    @Override
    public boolean shouldPush() {
        boolean result = !(Math.abs(System.currentTimeMillis() - lastPushTime) < 60000);
        if (result && this.isPushOpen()) {
            lastPushTime = System.currentTimeMillis();
        }
        return result && this.isPushOpen() && !isPlaying();
    }

    @Override
    public void openPush(final CameraClient.ServerResultListener2 succListner, final CameraClient.ServerResultListener2 errorListner) {
        OnBindPushResult bindPushResult_open = new OnBindPushResult() {
            @Override
            public void onBindSuccess(HichipCamera camera) {

                if (!camera.handSubXYZ()) {
                    camera.setServerData(TwsDataValue.CAMERA_ALARM_ADDRESS);
                } else {
                    camera.setServerData(TwsDataValue.CAMERA_ALARM_ADDRESS_THERE);
                }
                pushState = 1;
                HichipCamera.this.sync2Db(App.getContext());
                sendRegister();
                if (succListner != null) {
                    succListner.serverResult("succ", null);
                }
            }

            @Override
            public void onBindFail(HichipCamera camera) {
                if (errorListner != null) {
                    errorListner.serverResult("fail", null);
                }
            }

            @Override
            public void onUnBindSuccess(HichipCamera camera) {

            }

            @Override
            public void onUnBindFail(HichipCamera camera) {
            }

        };

        this.bindPushState(true, bindPushResult_open);
    }

    @Override
    public void closePush(Context context) {
        OnBindPushResult bindPushResult_close = new OnBindPushResult() {
            @Override
            public void onBindSuccess(HichipCamera camera) {

            }

            @Override
            public void onBindFail(HichipCamera camera) {

            }

            @Override
            public void onUnBindSuccess(HichipCamera camera) {

            }

            @Override
            public void onUnBindFail(HichipCamera camera) {

            }

        };
        this.bindPushState(false, bindPushResult_close);
        setPushState(0);
        sync2Db(App.getContext());
    }

    @Override
    public boolean isPushOpen() {
        return this.pushState > 0;
    }

    @Override
    public boolean setPushOpen(boolean open) {
        return false;
    }

    @Override
    public void remove(Context context) {
        TwsDataValue.cameraList().remove(this);
        this.unregisterIOSessionListener();
        snapshot = null;
        DatabaseManager db = new DatabaseManager(context);
        db.removeDeviceByUID(this.getUid());
    }

    @Override
    public void save(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        long dbid = db.addDevice(this.getNickName(), this.getUid(), "", "", "admin", getPassword(), 0, 0, "0", this.getVideoQuality(), "", 0);
        TwsDataValue.cameraList().add(this);
    }

    @Override
    public boolean sync2Db(Context context) {
        try {
            DatabaseManager db = new DatabaseManager(context);
            //manager.updateDeviceInfoByDBID(mDevice.DBID, mCamera.uid, mCamera.name, "", "", "admin", mCamera.pwd, evtNotify, 0);
            db.updateDeviceInfoByDBUID(this.getUid(), this.getNickName(), "", "", "admin", this.getPassword(), this.isPushOpen() ? 1 : 0, 0, 0, this.getVideoQuality());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
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

    @Override
    public boolean isConnected() {
        return super.getConnectState() == CAMERA_CONNECTION_STATE_LOGIN;
    }

    @Override
    public boolean isDisconnect() {
        return super.getConnectState() == CAMERA_CONNECTION_STATE_DISCONNECTED;
    }

    @Override
    public boolean isPasswordWrong() {
        return super.getConnectState() == CAMERA_CONNECTION_STATE_WRONG_PASSWORD;
    }

    @Override
    public boolean isConnecting() {
        return super.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTING;
    }

    @Override
    public boolean isNotConnect() {
        return super.getConnectState() == CAMERA_CONNECTION_STATE_DISCONNECTED;
    }

    @Override
    public void saveSnapShot(int channel, final String filePath, final String fileName, final TaskExecute te) {
        this.asyncSnapshot(new TaskExecute() {
            @Override
            public void onPosted(IMyCamera c, Object bmp) {
                boolean isErr = false;

                String fullFilePath = null;
                if (bmp != null) {
                    try {
                        fullFilePath = filePath + "/" + fileName;
                        File ff = new File(fullFilePath);

                        if (fileName.equalsIgnoreCase(HichipCamera.this.getUid()) || !ff.exists()) {
                            isErr = !TwsTools.saveBitmap((Bitmap) bmp, fullFilePath);
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

    @Override
    public int getVideoQuality() {
        return videoQuality;
    }

    @Override
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
            if (this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTING || this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTED) {
                return App.getContext().getString(R.string.camera_state_connecting);
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_LOGIN) {
                return App.getContext().getString(R.string.camera_state_connected);
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_DISCONNECTED || this.getConnectState() == CAMERA_CONNECTION_STATE_UIDERROR) {
                return App.getContext().getString(R.string.camera_state_disconnect);
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                return App.getContext().getString(R.string.camera_state_passwordWrong);
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
            if (this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTING || this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTED) {
                return R.drawable.shape_state_connecting;
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_LOGIN) {
                return R.drawable.shape_state_online;
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_DISCONNECTED || this.getConnectState() == CAMERA_CONNECTION_STATE_UIDERROR) {
                return R.drawable.shape_state_offline;
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                return R.drawable.shape_state_pwderror;
            }
        } else {
            return R.drawable.shape_state_connecting;
        }
        return 0;
    }

    @Override
    public int getIntId() {
        return 0;
    }

    @Override
    public int getTotalSDSize() {
        return 0;
    }

    @Override
    public void setTotalSDSize(int total) {

    }

    @Override
    public String getCustomTypeVersion() {
        return null;
    }

    @Override
    public void setCustomTypeVersion(String customTypeVersion) {

    }

    @Override
    public String getVendorTypeVersion() {
        return null;
    }

    @Override
    public void setVendorTypeVersion(String vendorTypeVersion) {

    }

    @Override
    public String getSystemTypeVersion() {
        return null;
    }

    @Override
    public void setSystemTypeVersion(String systemTypeVersion) {

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

    protected void setServer(HichipCamera mCamera) {

        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }
        //如果数据库保存的还是老地址就解绑并绑定新的地址
        if (mCamera.getServerData() != null && !mCamera.getServerData().equals(TwsDataValue.CAMERA_ALARM_ADDRESS)) {
            if (mCamera.getPushState() > 1) {
                mCamera.bindPushState(false, bindPushResult);
                return;
            }
        }

        sendServer(mCamera);
        sendRegisterToken(mCamera);

    }

    @Override
    public void receiveSessionState(HiCamera hiCamera, int connect_state) {
        int accState = connect_state;
        if (SessionStateHashMap.containsKey(connect_state)) {
            accState = (int) SessionStateHashMap.get(connect_state);
        }
        if (accState == NSCamera.CONNECTION_STATE_CONNECTED) {
            cameraLogin();
        }
        if ((accState == NSCamera.CONNECTION_STATE_CONNECTED) && cameraState != CameraState.WillUpgrading && cameraState != CameraState.WillRebooting && cameraState != CameraState.WillReseting) {
            cameraState = CameraState.None;
        } else if (accState == NSCamera.CONNECTION_STATE_DISCONNECTED) {
            if (cameraState == CameraState.WillRebooting) {
                cameraState = CameraState.Rebooting;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            } else if (cameraState == CameraState.WillReseting) {
                cameraState = CameraState.Reseting;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            } else if (cameraState == CameraState.WillUpgrading) {
                cameraState = CameraState.Upgrading;
                beginRebootTime = System.currentTimeMillis();
                rebootTimeout = 120000;
            }
            //重启，复位，升级固件超时处理
            if (System.currentTimeMillis() - beginRebootTime > rebootTimeout) {
                if (cameraState != CameraState.None) {
                    cameraState = CameraState.None;
                }
            }
        }

        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveSessionInfo(HichipCamera.this, accState);
        }
    }

    @Override
    public void receiveIOCtrlData(HiCamera hiCamera, int type, byte[] data, int state) {
        int accType = type;
        if (IOTCHashMap.containsKey(type)) {
            accType = (int) IOTCHashMap.get(type);
        }
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveIOCtrlData(HichipCamera.this, state, accType, data);
        }
        if (type == HiChipDefines.HI_P2P_SET_REBOOT) {
            setState(CameraState.WillRebooting);
        } else if (type == HiChipDefines.HI_P2P_SET_RESET) {
            setState(CameraState.WillReseting);
        } else if (type == HiChipDefines.HI_P2P_SET_DOWNLOAD) {
            setState(cameraState.WillUpgrading);
        }

        if (type == HiChipDefines.HI_P2P_GET_DEV_INFO_EXT) {
            int pos = 0;
            pos += 4;
            pos += 32;
            pos += 32;
            pos += 4;
            pos += HiChipDefines.HI_P2P_MAX_VERLENGTH;
            pos += 32;
            pos += HiChipDefines.HI_P2P_MAX_STRINGLENGTH;
            pos += 4;
            pos += HiChipDefines.HI_P2P_MAX_STRINGLENGTH;
            pos += HiChipDefines.HI_P2P_MAX_STRINGLENGTH;
            pos += 4;
            pos += 4;
            pos += 4;
            //从固件版本号信息中获取功能位信息--20170712
            try {
                if (getDeciveInfo() != null) {
                    System.arraycopy(data, pos, getDeciveInfo().aszWebVersion, 0, HiChipDefines.HI_P2P_MAX_VERLENGTH);
                    String strWv = Packet.getString(getDeciveInfo().aszWebVersion);
                    if (strWv != null) {
                        String[] arrWv = strWv.split("\\.");
                        if (arrWv.length > 4) {
                            String strFunc = arrWv[4];
                            int intFunc = Integer.parseInt(strFunc);
                            String strBinaryFunc = Integer.toBinaryString(intFunc);
                            while (strBinaryFunc.length() < 5) {
                                strBinaryFunc = "0" + strBinaryFunc;
                            }
                            byte[] arrByt = new byte[strBinaryFunc.length()];
                            for (int i = 0; i < arrByt.length; i++) {
                                if (strBinaryFunc.charAt(i) == '1') {
                                    arrByt[i] = 49;
                                }
                            }
                            setFunctionFlag(App.getContext(), arrByt);
                        }
                    }
                }
            } catch (Exception ex) {

            }
            if (!hasSetFunctionFlag()) {
                this.sendIOCtrl(HiChipDefines.HI_P2P_GET_NET_PARAM, new byte[0]);
            }
        } else if (type == HiChipDefines.HI_P2P_GET_NET_PARAM) {
            if (!hasSetFunctionFlag()) {
                if (data != null && data.length >= 332) {
                    HiChipDefines.HI_P2P_S_NET_PARAM net_param = new HiChipDefines.HI_P2P_S_NET_PARAM(data);
                    String ip = Packet.getString(net_param.strIPAddr);
                    String netmask = Packet.getString(net_param.strNetMask);
                    try {
                        CameraFunction.DoCameraFunctionFlag(App.getContext(), this, ip, netmask);
                    } catch (Exception ex) {

                    }
                }
            }
        }
        if (type == HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT) {
            if (data != null && data.length >= 36) {
                HiChipDefines.HI_P2P_S_TIME_ZONE_EXT time_ZONE_EXT = new HiChipDefines.HI_P2P_S_TIME_ZONE_EXT(data);
                this.setTimezoneExt(time_ZONE_EXT);
                this.setSummerTimer(time_ZONE_EXT.u32DstMode == 1);
                if (this.isInitTime()) {
                    this.setInitTime(false);
                    this.syncPhoneTime();
                }
            }
        }
        if (type == HiChipDefines.HI_P2P_GET_TIME_ZONE) {
            HiChipDefines.HI_P2P_S_TIME_ZONE timezone = new HiChipDefines.HI_P2P_S_TIME_ZONE(data);
            this.setTimezone(timezone);

            if (this.isInitTime()) {
                this.setInitTime(false);
                this.syncPhoneTime();
            }
        }

        if (type == HiChipDefines.HI_P2P_GET_TIME_PARAM) {
            if (data != null && data.length >= 24) {
                HiChipDefines.HI_P2P_S_TIME_PARAM dt = new HiChipDefines.HI_P2P_S_TIME_PARAM(data);
                //濡傛灉鎽勫儚鏈烘椂闂存槸鍒濆鏃堕棿锛屽垯鍚屾鎵嬫満鏃堕棿銆�
                if (dt.u32Year <= 1970) {
                    this.setInitTime(true);
                    getTimeZone();
                }
            }
        }

        if (type == HiChipDefines.HI_P2P_SET_TIME_PARAM) {
            this.setInitTime(false);
        }
    }

    private void getTimeZone() {
        boolean mIsSupportZoneExt = this.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT);
        if (mIsSupportZoneExt) {// 支持新时区
            this.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
        } else {
            this.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
        }
    }

    @Override
    public void callbackState(HiCamera hiCamera, int state, int w, int h) {
        if (state >= 3 && state <= 5) {
            int accState = state;
            if (state == 3) {
                for (int i = 0; i < mIOTCListeners.size(); i++) {
                    IIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveRecordingData(HichipCamera.this, -999, 3, "");
                }
            } else {
                accState = state == 4 ? 1 : 2;
                for (int i = 0; i < mIOTCListeners.size(); i++) {
                    IIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveRecordingData(HichipCamera.this, -999, accState, "");
                }
            }
        } else {
            for (int i = 0; i < mPlayStateListeners.size(); i++) {
                IPlayStateListener listener = mPlayStateListeners.get(i);
                listener.callbackState(HichipCamera.this, -999, state, w, h);
            }
        }
    }

    public CameraP2PType getP2PType() {
        return CameraP2PType.HichipP2P;
    }

    @Override
    public void callbackPlayUTC(HiCamera hiCamera, int state) {
        for (int i = 0; i < mPlayStateListeners.size(); i++) {
            IPlayStateListener listener = mPlayStateListeners.get(i);
            listener.callbackPlayUTC(HichipCamera.this, state);
        }
    }

    public static boolean IsP2PInited;

    public static void initP2P() {
        HiChipSDK.init(new HiChipSDK.HiChipInitCallback() {

            @Override
            public void onSuccess() {
                HiLog.e("SDK INIT success");
                IsP2PInited = true;
            }

            @Override
            public void onFali(int arg0, int arg1) {
                HiLog.e("SDK INIT fail");
                IsP2PInited = false;
            }
        });

    }

    public static void unInitP2P() {
        HiChipSDK.uninit();
    }

    private String serverData;

    public void setServerData(String serverData) {
        this.serverData = serverData;
        DatabaseManager db = new DatabaseManager(App.getContext());
        db.updateDeviceServerData(this.getUid(), serverData);
    }

    public String getServerData() {
        if (this.serverData == null) {
            DatabaseManager db = new DatabaseManager(App.getContext());
            this.serverData = db.getDeviceServerData(this.getUid());
        }
        return this.serverData;
    }

    public interface OnBindPushResult {
        public void onBindSuccess(HichipCamera camera);

        public void onBindFail(HichipCamera camera);

        public void onUnBindSuccess(HichipCamera camera);

        public void onUnBindFail(HichipCamera camera);
    }

    public void bindPushState(boolean isBind, OnBindPushResult bindPushResult) {
        if (TwsDataValue.XGToken == null) {
            return;
        }
        /* 地址变更 解绑时 用旧的服务器 */
        if (!isBind && this.getServerData() != null && !this.getServerData().equals(TwsDataValue.CAMERA_ALARM_ADDRESS)) {
            push = new HiPushSDK(TwsDataValue.XGToken, getUid(), TwsDataValue.company(), pushResult, this.getServerData());
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && !handSubXYZ()) {
            push = new HiPushSDK(TwsDataValue.XGToken, getUid(), TwsDataValue.company(), pushResult, TwsDataValue.CAMERA_ALARM_ADDRESS);
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && handSubXYZ()) {
            push = new HiPushSDK(TwsDataValue.XGToken, getUid(), TwsDataValue.company(), pushResult, TwsDataValue.CAMERA_ALARM_ADDRESS_THERE);
        } else {// old device
            push = new HiPushSDK(TwsDataValue.XGToken, getUid(), TwsDataValue.company(), pushResult, TwsDataValue.CAMERA_OLD_ALARM_ADDRESS);
        }
        onBindPushResult = bindPushResult;
        if (isBind) {
            push.bind();
        } else {
            push.unbind(getPushState());
        }
    }

    private int pushState = TwsDataValue.DEFAULT_PUSH_STATE;

    public int getPushState() {
        return pushState;
    }

    public void setPushState(int pushState) {
        this.pushState = pushState;
    }

    public HiPushSDK push;
    private OnBindPushResult onBindPushResult;
    private boolean isSetValueWithoutSave = false;
    private HiPushSDK.OnPushResult pushResult = new HiPushSDK.OnPushResult() {
        @Override
        public void pushBindResult(int subID, int type, int result) {
            isSetValueWithoutSave = true;

            if (type == HiPushSDK.PUSH_TYPE_BIND) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    pushState = subID;
                    if (onBindPushResult != null)
                        onBindPushResult.onBindSuccess(HichipCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result || HiPushSDK.PUSH_RESULT_NULL_TOKEN == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onBindFail(HichipCamera.this);
                }
            } else if (type == HiPushSDK.PUSH_TYPE_UNBIND) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onUnBindSuccess(HichipCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onUnBindFail(HichipCamera.this);
                }

            }

        }
    };

    protected void sendServer(HichipCamera mCamera) {
        // //测试
        // mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
        if (mCamera.getServerData() == null) {
            mCamera.setServerData(TwsDataValue.CAMERA_ALARM_ADDRESS);
        }
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }
        if (mCamera.push != null) {
            byte[] info = CamHiDefines.HI_P2P_ALARM_ADDRESS.parseContent(mCamera.push.getPushServer());
            mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET, info);
        }
    }

    OnBindPushResult bindPushResult = new OnBindPushResult() {
        @Override
        public void onBindSuccess(HichipCamera camera) {

            if (!camera.handSubXYZ()) {
                camera.setServerData(TwsDataValue.CAMERA_ALARM_ADDRESS);
            } else {
                camera.setServerData(TwsDataValue.CAMERA_ALARM_ADDRESS_THERE);
            }
            sendServer(camera);
            sendRegisterToken(camera);
        }

        @Override
        public void onBindFail(HichipCamera camera) {
        }

        @Override
        public void onUnBindSuccess(HichipCamera camera) {
            camera.bindPushState(true, bindPushResult);
        }

        @Override
        public void onUnBindFail(HichipCamera camera) {
            // 把SubId存放到sharePrefence
            if (camera.isPushOpen()) {
                SharePreUtils.putInt("subId", App.getContext(), camera.getUid(), camera.getPushState());
            }

        }

    };

    private void sendRegister() {
        if (this.getPushState() == 1) {
            return;
        }
        if (!this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, this.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), this.getPushState() > 0 ? 1 : 0);
        this.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST, info);
    }

    protected void sendRegisterToken(HichipCamera mCamera) {
        if (mCamera.getPushState() == 1 || mCamera.getPushState() == 0) {

            return;
        }

        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), 1);

        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST, info);
    }

    /**
     * 处理UID前缀为XXX YYYY ZZZ
     *
     * @return 如果是则返回 true
     */
    public boolean handSubXYZ() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : TwsDataValue.SUBUID) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    private void cameraLogin() {
        HiLog.v("mainactivity cameraLogin:" + this.getUid());
        boolean mIsSupportZoneExt = this.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT);
        int timezoneSource = 0;
        if (mIsSupportZoneExt) {
            // 支持新时区
            this.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
        } else {
            this.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
        }
        if (!this.hasSetFunctionFlag()) {
            this.sendIOCtrl(HiChipDefines.HI_P2P_GET_DEV_INFO_EXT, new byte[0]);
        }

        this.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_PARAM, null);
        if (getPushState() > 0) {
            setServer(this);
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
        return getFunctionFlag(context)[1] == 49;
    }

    public boolean hasPreset(Context context) {
        return getFunctionFlag(context)[2] == 49;
    }

    public boolean hasZoom(Context context) {
        return getFunctionFlag(context)[3] == 49;
    }

    public boolean hasSDSlot(Context context) {
        return getFunctionFlag(context)[4] == 49;
    }

    //end index,0:云台，1：:双向语音,2:预置位,3:光学变焦,4:SD卡槽 20170316-yilu
    public boolean syncPhoneTime() {
        if (timezone != null || timezone_ext != null) {
            TimeZone tz = null;
            long offset = 0;
            if (timezone != null && (timezone.u32DstMode == 1 || timezone.u32DstMode == 0)) {
                int dstMode = timezone.u32DstMode;
                if (dstMode == 1) {
                    String[] specifiedIDs = TimeZone.getAvailableIDs(timezone.s32TimeZone * 60 * 60 * 1000);
                    for (int i = 0; i < specifiedIDs.length; i++) {
                        if (TimeZone.getTimeZone(specifiedIDs[i]).useDaylightTime()) {
                            tz = TimeZone.getTimeZone(specifiedIDs[i]);
                            break;
                        }
                    }
                }
                if (tz == null) {
                    tz = TimeZone.getTimeZone("GMT" + (timezone.s32TimeZone > 0 ? "+" : (timezone.s32TimeZone == 0 ? " " : "")) + timezone.s32TimeZone);
                }
            } else if (timezone_ext != null && (timezone_ext.u32DstMode == 1 || timezone_ext.u32DstMode == 0)) {
                tz = TimeZone.getTimeZone(Packet.getString(timezone_ext.sTimeZone));
                if (tz.useDaylightTime() && timezone_ext.u32DstMode == 0) {
                    offset = -60 * 60 * 1000;
                }
            }
            if (tz != null) {
                Calendar cal = Calendar.getInstance(tz);
                cal.setTimeInMillis(System.currentTimeMillis() + offset);

                byte[] time = HiChipDefines.HI_P2P_S_TIME_PARAM.parseContent(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

                this.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_PARAM, time);
                return true;
            }
        }
        return false;
    }


    public HiChipDefines.HI_P2P_S_TIME_ZONE_EXT getTimezoneExt() {
        return timezone_ext;
    }

    public void setTimezoneExt(com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE_EXT timezone) {
        this.timezone_ext = timezone;
    }

    public HiChipDefines.HI_P2P_S_TIME_ZONE getTimezone() {
        return timezone;
    }

    public void setTimezone(com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE timezone) {
        this.timezone = timezone;
    }

    public static HashMap IOTCHashMap;
    public static HashMap SessionStateHashMap;

    static {
        SessionStateHashMap = new HashMap();
        IOTCHashMap = new HashMap();

        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED, TwsSessionState.CONNECTION_STATE_DISCONNECTED);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_CONNECTING, TwsSessionState.CONNECTION_STATE_CONNECTING);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_CONNECTED, TwsSessionState.CONNECTION_STATE_CONNECTING);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD, TwsSessionState.CONNECTION_STATE_WRONG_PASSWORD);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_LOGIN, TwsSessionState.CONNECTION_STATE_CONNECTED);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_UIDERROR, TwsSessionState.CAMERA_CONNECTION_STATE_UIDERROR);
        SessionStateHashMap.put(HiCamera.CAMERA_CHANNEL_STREAM_ERROR, TwsSessionState.CAMERA_CHANNEL_STREAM_ERROR);
        SessionStateHashMap.put(HiCamera.CAMERA_CHANNEL_CMD_ERROR, TwsSessionState.CAMERA_CHANNEL_CMD_ERROR);

        IOTCHashMap.put(HiChipDefines.HI_P2P_SET_USER_PARAM, TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_GET_WIFI_PARAM, TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_GET_WIFI_LIST, TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_GET_MD_PARAM, TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_SET_MD_PARAM, TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_SETMOTIONDETECT_RESP);
        //  IOTCHashMap.put(HiChipDefines.HI_P2P_GET_SD_INFO,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP);


    }

}
