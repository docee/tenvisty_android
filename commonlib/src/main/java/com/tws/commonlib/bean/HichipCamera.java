package com.tws.commonlib.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.sdk.HiChipP2P;
import com.hichip.sdk.HiChipSDK;
import com.hichip.tools.Packet;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.App;
import com.tws.commonlib.R;
import com.tws.commonlib.base.CameraClient;
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

public class HichipCamera extends HiCamera implements IMyCamera,ICameraIOSessionCallback,ICameraPlayStateCallback {

    protected List<IIOTCListener> mIOTCListeners = Collections.synchronizedList(new Vector<IIOTCListener>());
    protected List<IPlayStateListener> mPlayStateListeners = Collections.synchronizedList(new Vector<IPlayStateListener>());
    private String nickName;
    private  CameraState cameraState;
    private  int eventNum;
    private Bitmap snapshot;
    private  long lastPushTime;
    private int videoQuality;
    private com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE timezone = null;
    private com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE_EXT timezone_ext = null;
    private boolean isInitTime = false;

    private float videoRatio = 0;
    public HichipCamera(Context context, String nikename, String uid, String username, String password) {
        super(context, uid, username, password);
        this.nickName = nikename;
        this.registerIOSessionListener(this);
        this.registerPlayStateListener(this);
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
    public boolean isPlaying() {
        return false;
    }

    @Override
    public CameraState getState() {
        return cameraState == null?CameraState.None : cameraState;
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
                    ex.onPosted(HichipCamera.this,null);
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void stop() {
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
                    ex.onPosted(HichipCamera.this,null);
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
        super.sendIOCtrl(type,data);
    }

    @Override
    public void asyncSendIOCtrl(int avChannel, int type, byte[] data) {
        super.sendIOCtrl(type,data);
    }

    @Override
    public void stopVideo() {
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
                    te.onPosted(HichipCamera.this,null);
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
                    te.onPosted(HichipCamera.this,null);
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
                    te.onPosted(HichipCamera.this,null);
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
                    te.onPosted(HichipCamera.this,null);
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
    public void openPush(CameraClient.ServerResultListener2 succListner, CameraClient.ServerResultListener2 errorListner) {

    }

    @Override
    public void closePush(Context context) {

    }

    @Override
    public boolean isPushOpen() {
        return false;
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
            db.updateDeviceInfoByDBUID(this.getUid(), this.getNickName(), "", "", "admin", this.getPassword(), this.isPushOpen()?1:0, 0, 0, this.getVideoQuality());
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
    public void saveSnapShot(int channel, final String subFolder, final String fileName, final TaskExecute te) {
        this.asyncSnapshot(new TaskExecute() {
            @Override
            public void onPosted(IMyCamera c, Object bmp) {
                boolean isErr = false;

                String filePath = null;
                if (bmp != null) {
                    try {
                        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "");
                        File targetFolder = null;
                        if (subFolder == null) {
                            targetFolder = new File(rootFolder.getAbsolutePath() + "/android/data/" + MyConfig.getFolderName());
                        } else {
                            targetFolder = new File(rootFolder.getAbsolutePath() + "/" + MyConfig.getFolderName() + "/" + subFolder + "/" + c.getUid());
                        }

                        if (!rootFolder.exists()) {
                            rootFolder.mkdir();
                        }
                        if (!targetFolder.exists()) {
                            targetFolder.mkdirs();
                        }
                        filePath = targetFolder.getAbsolutePath() + "/" + fileName;
                        File ff = new File(filePath);

                        if (fileName.equalsIgnoreCase(HichipCamera.this.getUid()) || !ff.exists()) {
                            isErr = !TwsTools.saveBitmap((Bitmap) bmp, filePath);
                            if (isErr) {
                                Thread.sleep(100);
                                bmp = HichipCamera.super.getSnapshot();
                                isErr = !TwsTools.saveBitmap((Bitmap) bmp, filePath);
                            }
                            if (!isErr && fileName.equalsIgnoreCase(c.getUid())) {
                                c.setSnapshot((Bitmap) bmp);
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
    public String  getCameraStateDesc() {
        if(this.getState() == CameraState.None || this.getState() == null) {
            if (this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTING || this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTED) {
                return App.getContext().getString(R.string.camera_state_connecting);
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_LOGIN) {
                return App.getContext().getString(R.string.camera_state_connected);
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_DISCONNECTED || this.getConnectState() == CAMERA_CONNECTION_STATE_UIDERROR) {
                return App.getContext().getString(R.string.camera_state_disconnect);
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                return App.getContext().getString(R.string.camera_state_passwordWrong);
            }
        }
        else{
            if(this.getState() == CameraState.Rebooting || this.getState() == CameraState.WillRebooting){
                return App.getContext().getString(R.string.tips_rebooting);
            }
            else if(this.getState() == CameraState.Reseting || this.getState() == CameraState.WillReseting) {
                return App.getContext().getString(R.string.tips_reseting);
            }
            else if(this.getState() == CameraState.Upgrading || this.getState() == CameraState.WillUpgrading) {
                return App.getContext().getString(R.string.tips_upgrading);
            }
        }
        return "";
    }

    @Override
    public int getCameraStateBackgroundColor() {
        if(this.getState() == CameraState.None|| this.getState() == null) {
            if (this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTING || this.getConnectState() == CAMERA_CONNECTION_STATE_CONNECTED) {
                return R.drawable.shape_state_connecting;
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_LOGIN) {
                return R.drawable.shape_state_online;
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_DISCONNECTED || this.getConnectState() == CAMERA_CONNECTION_STATE_UIDERROR) {
                return R.drawable.shape_state_offline;
            } else if (this.getConnectState() == CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                return R.drawable.shape_state_pwderror;
            }
        }
        else{
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

    @Override
    public void receiveSessionState(HiCamera hiCamera, int state) {
        int accState = state;
        if(SessionStateHashMap.containsKey(state)){
            accState = (int)SessionStateHashMap.get(state);
        }
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveSessionInfo(HichipCamera.this,accState);
        }
    }

    @Override
    public void receiveIOCtrlData(HiCamera hiCamera, int type, byte[] bytes, int state) {
        int accType = type;
        if(IOTCHashMap.containsKey(type)){
            accType = (int)IOTCHashMap.get(type);
        }
        for (int i = 0; i < mIOTCListeners.size(); i++) {
            IIOTCListener listener = mIOTCListeners.get(i);
            listener.receiveIOCtrlData(HichipCamera.this,state,accType,bytes);
        }
    }

    @Override
    public void callbackState(HiCamera hiCamera, int state, int w, int h) {
        if(state >=3 && state<=5){
            int accState = state;
            if(state == 3){
                for (int i = 0; i < mIOTCListeners.size(); i++) {
                    IIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveRecordingData(HichipCamera.this, -999, 3, "");
                }
            }
            else {
                accState = state == 4?1:2;
                for (int i = 0; i < mIOTCListeners.size(); i++) {
                    IIOTCListener listener = mIOTCListeners.get(i);
                    listener.receiveRecordingData(HichipCamera.this, -999, accState, "");
                }
            }
        }
        else {
            for (int i = 0; i < mPlayStateListeners.size(); i++) {
                IPlayStateListener listener = mPlayStateListeners.get(i);
                listener.callbackState(HichipCamera.this, -999, state, w, h);
            }
        }
    }
    public  CameraP2PType getP2PType(){
        return CameraP2PType.HichipP2P;
    }

    @Override
    public void callbackPlayUTC(HiCamera hiCamera, int state) {
        for (int i = 0; i < mPlayStateListeners.size(); i++) {
            IPlayStateListener listener = mPlayStateListeners.get(i);
            listener.callbackPlayUTC(HichipCamera.this,state);
        }
    }

    public  static boolean IsP2PInited;
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

        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED,TwsSessionState.CONNECTION_STATE_DISCONNECTED);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_CONNECTING,TwsSessionState.CONNECTION_STATE_CONNECTING);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_CONNECTED,TwsSessionState.CONNECTION_STATE_CONNECTING);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD,TwsSessionState.CONNECTION_STATE_WRONG_PASSWORD);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_LOGIN,TwsSessionState.CONNECTION_STATE_CONNECTED);
        SessionStateHashMap.put(HiCamera.CAMERA_CONNECTION_STATE_UIDERROR,TwsSessionState.CAMERA_CONNECTION_STATE_UIDERROR);
        SessionStateHashMap.put(HiCamera.CAMERA_CHANNEL_STREAM_ERROR,TwsSessionState.CAMERA_CHANNEL_STREAM_ERROR);
        SessionStateHashMap.put(HiCamera.CAMERA_CHANNEL_CMD_ERROR,TwsSessionState.CAMERA_CHANNEL_CMD_ERROR);

        IOTCHashMap.put(HiChipDefines.HI_P2P_SET_USER_PARAM,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_RESP);
        IOTCHashMap.put( HiChipDefines.HI_P2P_GET_WIFI_PARAM,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_GET_WIFI_LIST,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_GET_MD_PARAM,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP);
        IOTCHashMap.put(HiChipDefines.HI_P2P_SET_MD_PARAM,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_SETMOTIONDETECT_RESP);
      //  IOTCHashMap.put(HiChipDefines.HI_P2P_GET_SD_INFO,TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP);


    }

}
