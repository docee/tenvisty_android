package com.tws.commonlib.activity.hichip;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.hichip.base.HiLog;
import com.hichip.content.HiChipDefines;
import com.hichip.sdk.HiChipP2P;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.St_SInfo;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.CameraFolderActivity;
import com.tws.commonlib.activity.EventListActivity;
import com.tws.commonlib.base.HiLiveViewGLMonitor;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.ScreenSwitchUtils;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.IPlayStateListener;
import com.tws.commonlib.bean.TwsDataValue;

import java.io.File;
import java.util.Timer;

/**
 * 瀹炴椂瑙嗛瑙傜湅鐣岄潰
 *
 * @author Administrator
 */
public class LiveView_HichipActivity extends BaseActivity implements
        ViewSwitcher.ViewFactory, IIOTCListener, View.OnTouchListener, IPlayStateListener {

    private static final int BUILD_VERSION_CODES_ICE_CREAM_SANDWICH = 14;
    // private static final String FILE_TYPE = "image/*";

    private static final int REQUEST_CODE_ALBUM = 99;


    // private TouchedMonitor monitor = null;
    private HiLiveViewGLMonitor monitor = null;
    public HichipCamera mCamera = null;

    private String mDevUID;
    private String mDevUUID;
    private String mConnStatus = "";
    private int mVideoFPS;
    private long mVideoBPS;
    private int mOnlineNm;
    private int mFrameCount;
    private int mIncompleteFrameCount;
    private int mSelectedChannel;


    private boolean mIsRecording = false;
    private boolean mLedOn = false;
    private BitmapDrawable bg;
    private BitmapDrawable bgSplit;

    private ProgressBar videoLoadProgressBar;

    private Timer ptz_timer;
    PopupWindow popupWindow;
    //private FileOperation mFile = null;
    private ScreenSwitchUtils instance;
    int videoQuality;
    playState playState;
    private PopupWindow mPopupWindow;
    private int select_preset = 0;
    String[] souceList;

    Button btn_stream;
    LinearLayout lay_live_tools_top;
    LinearLayout lay_live_tools_bottom;
    boolean toolsVisible;
    int videoWidth;
    int videoHeight;

    public Button getBtn_stream() {
        return (Button) findViewById(R.id.btn_stream);
    }

    public Button getBtn_talk() {
        return (Button) findViewById(R.id.btn_talk);
    }

    public ImageView getBtn_listen() {
        return (ImageView) findViewById(R.id.btn_listen);
    }

    public ImageView getBtn_recording() {
        return (ImageView) findViewById(R.id.btn_record);
    }

    public LinearLayout ll_recording_tip() {
        return (LinearLayout) findViewById(R.id.ll_recording_tip);
    }

    public TextView txt_recording_tip_time() {
        return (TextView) findViewById(R.id.txt_recording_tip_time);
    }

    private long lastReceiveFrameTime = 0;
    private boolean isVideoShowing = false;

    AVIOCTRLDEFs.SMsgAVIoctrlGetPreListResp presetList;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_live_view_portrait);

        bg = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped);
        bgSplit = (BitmapDrawable) getResources().getDrawable(
                R.drawable.bg_striped_split_img);

        Bundle bundle = this.getIntent().getExtras();
        mDevUID = bundle.getString(TwsDataValue.EXTRA_KEY_UID);
        //鏍规嵁浼犺繃鏉ョ殑UID鎵惧埌鐩稿簲鐨凜amera
        for (IMyCamera camera : TwsDataValue.cameraList()) {
            if (mDevUID.equalsIgnoreCase(camera.getUid())) {
                mCamera = (HichipCamera) camera;// 鎵惧埌camera
                break;
            }
        }

        if (mCamera != null) {
            playState = new playState(mCamera);
            mCamera.registerIOTCListener(this);// 娉ㄥ唽鐩戝惉
            mCamera.registerPlayStateListener(this);
            if (mCamera.isNotConnect()) {
                mCamera.start();
            }
        }
        instance = ScreenSwitchUtils.init(this.getApplicationContext());
        souceList = getResources().getStringArray(R.array.stream_quality);
        setupViewInPortraitLayout();// 绔栧睆

        toolsVisible = true;
    }// onCreate over


    @Override
    protected void onStart() {
        super.onStart();
        instance.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        instance.stop();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (handler.hasMessages(TwsDataValue.HANDLE_MESSAGE_RECONNECT)) {
            handler.removeMessages(TwsDataValue.HANDLE_MESSAGE_RECONNECT);
        }
        if (mCamera != null) {//鍋滄璇煶

            delayHandler.removeMessages(0);
            mCamera.saveSnapShot(mSelectedChannel, TwsTools.getFilePath(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB), TwsTools.getFileNameWithTime(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB), new IMyCamera.TaskExecute() {
                @Override
                public void onPosted(IMyCamera c, Object data) {
                    Intent intent = new Intent();
                    intent.setAction(TwsDataValue.ACTION_CAMERA_REFRESH_ONE_ITEM);
                    intent.putExtra(TwsDataValue.EXTRA_KEY_UID, c.getUid());
                    LiveView_HichipActivity.this.sendBroadcast(intent);
                }
            });
            stopRecording();
            mCamera.stopVideo();
            mCamera.stopAudio();
            mCamera.unregisterPlayStateListener(LiveView_HichipActivity.this);
            mCamera.unregisterIOTCListener(LiveView_HichipActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoLoadProgressBar.setVisibility(View.VISIBLE);

        //LiveViewActivity.this.mFile = new FileOperation(LiveViewActivity.this.mCamera.uid, LiveViewActivity.this.mSelectedChannel);

        isVideoShowing = false;
        if (mCamera != null) {
            mCamera.registerIOTCListener(LiveView_HichipActivity.this);
            mCamera.registerPlayStateListener(this);
            if(mCamera.isConnected()) {
                mCamera.asyncStartVideo(monitor, new IMyCamera.TaskExecute() {
                    @Override
                    public void onPosted(IMyCamera c, Object data) {
                        if (playState.isListening()) {
                            c.startAudio();
                        }
                    }
                });
            }else{
                mCamera.start();
            }
        }
        //GCMUtils.SetRemoteEventCamera(null, this.getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (instance.isPortrait()) {
            setupViewInPortraitLayout();
        } else {
            setupViewInLandscapeLayout();
        }
        setRecodingView();
        refreshViewStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ALBUM) {
            monitor = (HiLiveViewGLMonitor) findViewById(R.id.monitor);
        }
    }

    /**
     * 璁剧疆妯睆鏄剧ず
     */
    private void setupViewInLandscapeLayout() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 璁剧疆绐椾綋濮嬬粓鐐逛寒
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        doFullScreenStatusBar(false);
        setContentView(R.layout.activity_live_view_hi_landscape);
        if (Build.VERSION.SDK_INT < BUILD_VERSION_CODES_ICE_CREAM_SANDWICH) {//4.0浠ヤ笅鐨勮缃產ctionBar鐨勮儗鏅�

            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            //getSupportActionBar().setBackgroundDrawable(bg);

            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            //getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
        }
        monitor = (HiLiveViewGLMonitor) findViewById(R.id.monitor);
        monitor.setOnTouchListener(this);
        monitor.setCamera(mCamera);
        mCamera.setLiveShowMonitor(monitor);
//        if (videoHeigth != 0 && videoWidth != 0) {
//            monitor.saveMatrix(0, 0, videoWidth, videoHeigth);
//        }

        setFunctions();
        videoLoadProgressBar = (ProgressBar) findViewById(R.id.videoProgressBar);
        lay_live_tools_top = (LinearLayout) findViewById(R.id.lay_live_tools_top);
        lay_live_tools_bottom = (LinearLayout) findViewById(R.id.lay_live_tools_bottom);
        //monitor.setTouchListener(this);
        setViewVisible(!this.toolsVisible);
        getBtn_stream().setText(souceList[playState.getVideoQuality()]);
        getBtn_talk().setOnTouchListener(this);
        initBtn();

    }

    private void setViewVisible(boolean visible) {
        lay_live_tools_top.setVisibility(visible ? View.VISIBLE : View.GONE);
        lay_live_tools_bottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        getBtn_talk().setVisibility((visible && playState.isListening()) ? View.VISIBLE : View.GONE);
        this.toolsVisible = visible;
    }


    private void setFunctions() {
        if (((HichipCamera) camera).hasListen(LiveView_HichipActivity.this)) {
            getBtn_listen().setVisibility(View.VISIBLE);
        } else {
            getBtn_talk().setVisibility(View.INVISIBLE);
        }
        if (findViewById(R.id.ll_talk) != null) {
            if (((HichipCamera) camera).hasListen(LiveView_HichipActivity.this)) {
                findViewById(R.id.ll_talk).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_talk).setVisibility(View.GONE);
            }
        }

        if (findViewById(R.id.ll_preset) != null) {
            if (((HichipCamera) camera).hasPreset(LiveView_HichipActivity.this)) {
                findViewById(R.id.ll_preset).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_preset).setVisibility(View.GONE);
            }
        }

        if (findViewById(R.id.ll_listen) != null) {
            if (((HichipCamera) camera).hasListen(LiveView_HichipActivity.this)) {
                findViewById(R.id.ll_listen).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_listen).setVisibility(View.GONE);
            }
        }

        if (findViewById(R.id.ll_zoom) != null) {
            if (((HichipCamera) camera).hasZoom(LiveView_HichipActivity.this)) {
                findViewById(R.id.ll_zoom).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_zoom).setVisibility(View.GONE);
            }
        }
    }

    /**
     * 璁剧疆绔栧睆鏄剧ず
     */
    private void setupViewInPortraitLayout() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        doFullScreenStatusBar(true);
        setContentView(R.layout.activity_live_view_hi_portrait);

        this.setTitle(mCamera.getNickName());
        super.initView();
        getBtn_talk().setOnTouchListener(this);
        if (Build.VERSION.SDK_INT < BUILD_VERSION_CODES_ICE_CREAM_SANDWICH) {
            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            //getSupportActionBar().setBackgroundDrawable(bg);

            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            //getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
        }

        monitor = (HiLiveViewGLMonitor) findViewById(R.id.monitor);
        monitor.setOnTouchListener(this);
        monitor.setCamera(mCamera);
        mCamera.setLiveShowMonitor(monitor);
//        if (videoHeigth != 0 && videoWidth != 0) {
//            monitor.saveMatrix(0, 0, videoWidth, videoHeigth);
//        }

        setFunctions();
        videoLoadProgressBar = (ProgressBar) findViewById(R.id.videoProgressBar);

        getBtn_stream().setText(souceList[playState.getVideoQuality()]);
        TextView txt_state = (TextView) findViewById(R.id.txt_state);
        if (txt_state != null) {
            txt_state.setBackgroundResource(mCamera.getCameraStateBackgroundColor());
//            String conType = "";
//            try {
//                conType = (mCamera.getSessionMode() == 0 ? "P2P" : (mCamera.getSessionMode() == 1 ? "RELAY" : "LAN"));
//            } catch (Exception ex) {
//
//            }
            txt_state.setText(mCamera.getCameraStateDesc());
        }
        initBtn();

    }


    //SD鍗℃槸鍚﹀彲鐢�
    private static boolean isSDCardValid() {

        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 浜戝彴鎺у埗鍛戒护锛堢幇鍦ㄥ凡涓嶇敤锛�
     *
     * @param view
     */
    public void ptz(View view) {
        int ptzType = Integer.parseInt(view.getTag().toString());
        mCamera.ptz(ptzType);
    }

    /**
     * 閫�鍑哄綋鍓嶇晫闈紝鍥炲埌涓婚〉闈ideoList
     */
    private void quit() {
//        if (monitor != null) {
//            monitor.deattachCamera();
//        }
//
//        if (mCamera != null) {
//
//            mCamera.unregisterIOTCListener(this);
//            mCamera.stopSpeak();
//            mCamera.stopAudio();
//            mCamera.stopVideo();
//        }
        setResult(RESULT_OK);
        finish();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {

            case KeyEvent.KEYCODE_BACK:
                if (instance.isPortrait()) {
                    quit();
                } else {
                    instance.toggleScreen();
                    return false;
                }
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void receiveFrameData(final IMyCamera camera, int avChannel,
                                 final Bitmap bmp) {
        if (mCamera == camera && avChannel == mSelectedChannel) {
            lastReceiveFrameTime = System.currentTimeMillis();
            isVideoShowing = true;
        }
    }

    /**
     * 瑙嗛鏁版嵁鐩稿叧鍙傛暟淇℃伅
     */
    @Override
    public void receiveFrameInfo(final IMyCamera camera, int avChannel,
                                 long bitRate, int frameRate, int onlineNm, int frameCount,
                                 int incompleteFrameCount) {
        if (mCamera == camera && avChannel == mSelectedChannel) {
            mVideoFPS = frameRate;
            mVideoBPS = bitRate;
            mOnlineNm = onlineNm;
            mFrameCount = frameCount;
            mIncompleteFrameCount = incompleteFrameCount;

            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);

            Message msg = handler.obtainMessage();
            msg.what = TwsDataValue.HANDLE_MESSAGE_STS_CHANGE_STREAMINFO;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveChannelInfo(final IMyCamera camera, int avChannel,
                                   int resultCode) {
        if (mCamera == camera && avChannel == mSelectedChannel) {

            Bundle bundle = new Bundle();
            bundle.putInt("avChannel", avChannel);

            Message msg = handler.obtainMessage();
            msg.what = resultCode;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveSessionInfo(final IMyCamera camera, final int resultCode) {
        Bundle bundle = new Bundle();
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = resultCode;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void receiveIOCtrlData(final IMyCamera camera, int avChannel,
                                  int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();

        bundle.putInt("avChannel", avChannel);
        bundle.putByteArray("data", data);
        Message msg = new Message();
        msg.arg1 = avIOCtrlMsgType;
        msg.arg2 = avChannel;
        msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
        msg.setData(bundle);
        handler.sendMessage(msg);
            /*
             * Message msg = handler.obtainMessage(); msg.what =
			 * avIOCtrlMsgType; handler.sendMessage(msg);
			 */

    }

    private Handler delayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (playState.isRecording()) {
                        TwsToast.showToast(LiveView_HichipActivity.this, getString(R.string.toast_stop_record));
                        stopRecording();
                    }
                    break;
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            int avChannel = bundle.getInt("avChannel");
            byte[] data = bundle.getByteArray("data");

            St_SInfo stSInfo = new St_SInfo();
            int requestCode = msg.arg1;
            switch (msg.what) {
                case TwsDataValue.HANDLE_MESSAGE_RECONNECT:
                    mCamera.asyncStop(new IMyCamera.TaskExecute() {
                        @Override
                        public void onPosted(IMyCamera camera, Object data) {
                            mCamera.start();
                        }
                    });
                    break;
                case TwsDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    if (requestCode == NSCamera.CONNECTION_STATE_CONNECTED) {
                        mCamera.startLiveShow(mCamera.getVideoQuality(), monitor);
                    } else if (requestCode == NSCamera.CONNECTION_STATE_CONNECT_FAILED || requestCode ==
                            NSCamera.CONNECTION_STATE_DISCONNECTED || requestCode == NSCamera.CONNECTION_STATE_UNKNOWN_DEVICE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                videoLoadProgressBar.setVisibility(View.VISIBLE);
                            }
                        });
                        this.sendEmptyMessageDelayed(TwsDataValue.HANDLE_MESSAGE_RECONNECT, 3000);
                    }
                    TextView txt_state = (TextView) findViewById(R.id.txt_state);
                    if (txt_state != null) {
                        txt_state.setBackgroundResource(mCamera.getCameraStateBackgroundColor());
                        if (mCamera != null) {
                            //(camera.getSessionMode() == 0 ? "P2P" : (camera.getSessionMode() == 1 ? "RELAY" : "LAN")) + " " +
                            txt_state.setText(mCamera.getCameraStateDesc());
                        }
                    }
                    break;
                case TwsDataValue.HANDLE_MESSAGE_IO_RESP:
                    switch (requestCode) {
                        case HiChipDefines.HI_P2P_SET_PTZ_PRESET:
                            if (msg.arg2 == 0) {
                                TwsToast.showToast(LiveView_HichipActivity.this, getString(R.string.tips_setting_succ));
                            } else {
                                TwsToast.showToast(LiveView_HichipActivity.this, getString(R.string.tips_setting_failed));
                            }

                            break;
                    }
                    break;

                case TwsDataValue.HANDLE_MESSAGE_STS_CHANGE_STREAMINFO:
                    if (mVideoBPS < 1) {
                        videoLoadProgressBar.setVisibility(View.VISIBLE);
                        delayHandler.sendEmptyMessageDelayed(0, 10000);
                        //playState.setRecordingPause(true);
                        isVideoShowing = false;
                    } else {
                        delayHandler.removeMessages(0);
                        videoLoadProgressBar.setVisibility(View.GONE);
                        isVideoShowing = true;
                        //playState.setRecordingPause(false);
                    }
                    break;


            }

            super.handleMessage(msg);
        }
    };

    private boolean inArray(String str, String[] source) {
        if (source != null) {
            for (int i = 0; i < source.length; i++) {
                if (source[i].equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public View makeView() {
        TextView t = new TextView(this);
        return t;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initSendAudio(IMyCamera paramCamera, boolean paramBoolean) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveOriginalFrameData(IMyCamera paramCamera, int paramInt1,
                                         byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2,
                                         int paramInt3) {
        // TODO Auto-generated method stub
//	    if (this.mFile == null);
//	    while ((this.mCamera != paramCamera) || (paramInt1 != this.mSelectedChannel))
//	      return;
//	    int i = paramInt2 + paramInt3;
//	    byte[] arrayOfByte = new byte[4];
//	    arrayOfByte[0] = Integer.valueOf(0xFF & i >> 0).byteValue();
//	    arrayOfByte[1] = Integer.valueOf(0xFF & i >> 8).byteValue();
//	    arrayOfByte[2] = Integer.valueOf(0xFF & i >> 16).byteValue();
//	    arrayOfByte[3] = Integer.valueOf(0xFF & i >> 24).byteValue();
//	    this.mFile.WriteData("TUTKH264".getBytes(), "TUTKH264".length());
//	    this.mFile.WriteData(arrayOfByte, 4);
//	    this.mFile.WriteData(paramArrayOfByte1, paramInt2);
//	    this.mFile.WriteData(paramArrayOfByte2, paramInt3);
    }

    @Override
    public void receiveRGBData(IMyCamera paramCamera, int paramInt1,
                               byte[] paramArrayOfByte, int paramInt2, int paramInt3) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveRecordingData(IMyCamera paramCamera, int avChannel, int paramInt1, String path) {
        Message msg = recordingHandler.obtainMessage();
        msg.what = paramInt1;
        recordingHandler.sendMessage(msg);
    }

    void startListen() {
//        if (!isVideoShowing) {
//            showAlert(getString(R.string.alert_play_video_first));
//            return;
//        }
        if (mCamera != null
                && mCamera.isConnected()) {

            if (playState.isSpeaking()) {
                return;
            }
            getBtn_listen().setImageResource(R.drawable.ic_btn_listen_on);


            if (!playState.isListening()) {
                playState.setListening(true);
                mCamera.startAudio();
            }
            if (!instance.isPortrait()) {
                if (this.toolsVisible) {
                    getBtn_talk().setVisibility(View.VISIBLE);
                }
            }
        }
    }

    void stopListen() {
        if (playState.isListening()) {
            playState.setListening(false);
            mCamera.stopAudio();
        }
        if (instance.isPortrait()) {
            getBtn_listen().setImageResource(R.drawable.ic_btn_listen_off);
        } else {
            getBtn_listen().setImageResource(R.drawable.ic_btn_liveview_listen_close);
        }
        if (!instance.isPortrait()) {
            getBtn_talk().setVisibility(View.GONE);
        }
    }

    void startSpeak() {

        if (!isVideoShowing) {
            showAlert(getString(R.string.alert_play_video_first));
            return;
        }
        if (mCamera != null
                && mCamera.isConnected()) {

            if (!TwsTools.checkPermission(LiveView_HichipActivity.this, Manifest.permission.RECORD_AUDIO) ||
                    !TwsTools.checkPermission(LiveView_HichipActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                TwsTools.showAlertDialog(LiveView_HichipActivity.this);
                return;
            }
            instance.stop();
            playState.setSpeaking(true);
            mCamera.startSpeak();
        }
    }

    void stopSpeak() {
        instance.start(this);
        playState.setSpeaking(false);
        mCamera.stopSpeak();
    }

    void snap() {

//        if (!isVideoShowing) {
//            showAlert(getString(R.string.alert_play_video_first));
//            return;
//        }
        if (mCamera != null && mCamera.isConnected()) {

            if (isSDCardValid()) {// 濡傛灉sd鍗″彲鐢�
                if (!TwsTools.checkPermission(LiveView_HichipActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAlert(getString(R.string.dialog_msg_no_permission));
                    return;
                }
                final String fileName = TwsTools.getFileNameWithTime(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_MANUALLY);
                mCamera.saveSnapShot(mSelectedChannel, TwsTools.getFilePath(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_MANUALLY), fileName, new IMyCamera.TaskExecute() {
                    @Override
                    public void onPosted(IMyCamera c, Object data) {
                        String path = (String) data;
                        if (path != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LiveView_HichipActivity.this, LiveView_HichipActivity.this.getText(R.string.tips_snapshot_ok), Toast.LENGTH_SHORT).show();
                                }
                            });
                            TwsTools.addImageGallery(LiveView_HichipActivity.this, path, fileName);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LiveView_HichipActivity.this.showAlert(LiveView_HichipActivity.this.getString(R.string.alert_snapshot_failed));
                                }
                            });
                        }

                    }
                });
            } else {// 濡傛灉sd鍗′笉鍙敤
                showAlert(getString(R.string.alert_snapshot_no_sdcard));
            }
        }
    }

    public void doClick(View view) {
        if (view.getId() == R.id.btn_record) {
            if (playState.isRecording()) {
                stopRecording();
            } else {
                startRecording();
            }
        } else if (view.getId() == R.id.btn_stream) {
            if (playState.isRecording()) {
                TwsToast.showToast(LiveView_HichipActivity.this, getString(R.string.tip_not_switch_quality));
            } else {
                popupWindow = showPopupWindow(view, souceList, new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        if (!playState.isRecording()) {
                            if (playState.getVideoQuality() != position) {
                                playState.setVideoQuality(position);

                                mCamera.asyncStopVideo(new IMyCamera.TaskExecute() {
                                    @Override
                                    public void onPosted(IMyCamera c, Object data) {
                                        mCamera.stop();
                                    }
                                });
                            }
                        }
                        popupWindow.dismiss();
                    }
                });
            }
        } else if (view.getId() == R.id.btn_exit) {
            instance.toggleScreen();
        } else if (view.getId() == R.id.btn_listen) {
            if (!playState.isListening()) {
                startListen();
            } else {
                stopListen();
            }
        } else if (view.getId() == R.id.btn_fullscreen) {
            instance.toggleScreen();
            //  setupViewInLandscapeLayout();
        } else if (view.getId() == R.id.btn_snap) {
            snap();
        } else if (view.getId() == R.id.btn_live_preset) {
            clickPreset((ImageView) view);
        } else if (view.getId() == R.id.btn_live_zoom) {
            clickZoom((ImageView) view);
        } else if (view.getId() == R.id.btn_event) {
//            if (!isVideoShowing) {
//                showAlert(getString(R.string.alert_camera_connected_failed));
//                return;
//            }
            goActivity(EventList_HichipActivity.class);
        } else if (view.getId() == R.id.btn_folder) {
            Intent intent = new Intent(LiveView_HichipActivity.this,
                    CameraFolderActivity.class);
            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, mDevUID);
            startActivity(intent);
//            File folder = new File(Environment.getExternalStorageDirectory()
//                    .getAbsolutePath() + "/" + MyConfig.getFolderName() + "/Snapshot/" + mDevUID);// 鍥剧墖鏂囦欢鐩綍
//
//            String[] allFiles = folder.list();// 鑾峰彇folder鐩綍涓嬫墍鏈夋枃浠跺垪琛紝濡傛灉涓虹┖杩斿洖null
//
//            if (allFiles != null && allFiles.length > 0) {
//
//                String file = folder.getAbsolutePath() + "/"
//                        + allFiles[allFiles.length - 1];
//
//                Intent intent = new Intent(LiveViewActivity.this,
//                        CameraFolderActivity.class);
//                intent.putExtra("snap", mDevUID);
//                intent.putExtra("images_path", folder.getAbsolutePath());
//                startActivity(intent);
//
//            } else {// 濡傛灉涓虹┖
//                String msg = LiveViewActivity.this.getText(
//                        R.string.tips_no_snapshot_found).toString();// tips_no_snapshot_found">鏌ユ棤蹇収
//                Toast.makeText(LiveViewActivity.this, msg, Toast.LENGTH_SHORT)
//                        .show();
//            }
        }
    }

    void goActivity(Class<?> cls) {
        mCamera.stopVideo();
        if (playState.isSpeaking()) {
            mCamera.stopSpeak();
        }
        if (playState.isListening()) {
            mCamera.stopAudio();
        }
        Bundle extras = new Bundle();
        Intent intent = new Intent();

        extras.putString(TwsDataValue.EXTRA_KEY_UID, mCamera.getUid());
        intent.putExtras(extras);
        intent.setClass(LiveView_HichipActivity.this, cls);
        startActivity(intent);
    }

    void goEventActivity() {
        mCamera.stopVideo();

        Bundle extras = new Bundle();
        Intent intent = new Intent();

        extras.putString(TwsDataValue.EXTRA_KEY_UID, mCamera.getUid());

        intent.putExtras(extras);
        intent.setClass(LiveView_HichipActivity.this, EventList_HichipActivity.class);
        startActivity(intent);
    }

    private float action_down_x;
    private float action_down_y;

    float lastX;
    float lastY;

    int xlenOld;
    int ylenOld;

    float move_x;
    float move_y;

    public float left;
    public float width;
    public float height;
    public float bottom;

    double nLenStart = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        if (v.getId() == R.id.btn_talk) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    //HiLog.v("MotionEvent.ACTION_DOWN");
                    if (playState.isListening()) {
                        mCamera.stopAudio();
                        getBtn_listen().setImageResource(R.drawable.ic_btn_listen_pause);
                    }
                    startSpeak();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    //	HiLog.v("MotionEvent.ACTION_UP");
                    if (playState.isListening()) {
                        mCamera.startAudio();
                        getBtn_listen().setImageResource(R.drawable.ic_btn_listen_on);
                    }
                    stopSpeak();
                    break;
                }
                default:
                    break;
            }
        } else if (v.getId() == R.id.monitor) {
            int nCnt = event.getPointerCount();
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
                monitor.setTouchMove(2);
                for (int i = 0; i < nCnt; i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);

                    Point pt = new Point((int) x, (int) y);
                }

                xlenOld = Math.abs((int) event.getX(0) - (int) event.getX(1));
                ylenOld = Math.abs((int) event.getY(0) - (int) event.getY(1));
                nLenStart = Math.sqrt((double) xlenOld * xlenOld + (double) ylenOld * ylenOld);

            } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
                monitor.setTouchMove(2);
                // mMonitor.setState(3);
                for (int i = 0; i < nCnt; i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);

                    Point pt = new Point((int) x, (int) y);

                }

                int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
                int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

                int moveX = Math.abs(xlen - xlenOld);
                int moveY = Math.abs(ylen - ylenOld);

                double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
                if (moveX < 20 && moveY < 20) {

                    return false;
                }

                if (nLenEnd > nLenStart) {
                    resetMonitorSize(true, nLenEnd);
                } else {
                    resetMonitorSize(false, nLenEnd);
                }

                xlenOld = xlen;
                ylenOld = ylen;
                nLenStart = nLenEnd;

                return true;
            } else if (nCnt == 1) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        L.i("LiveView", "ACTION_MOVE");
                        action_down_x = event.getRawX();
                        action_down_y = event.getRawY();

                        lastX = action_down_x;
                        lastY = action_down_y;

                        // HiLog.e("ACTION_DOWN");
                        monitor.setTouchMove(0);
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (monitor.getTouchMove() != 0)
                            break;
                        L.i("LiveView", "ACTION_MOVE");

                        move_x = event.getRawX();
                        move_y = event.getRawY();

                        if (Math.abs(move_x - action_down_x) > 40 || Math.abs(move_y - action_down_y) > 40) {
                            monitor.setTouchMove(1);
                            // HiLog.e("ACTION_MOVE");
                        }


                        break;
                    case MotionEvent.ACTION_UP: {
                        if (monitor.getTouchMove() != 0) {
                            break;
                        }

                        // if(mToolsBarVisibility == View.VISIBLE) {
                        // setToolsBarsVisibility(View.GONE);
                        // }
                        // else if(mToolsBarVisibility == View.GONE) {
                        // setToolsBarsVisibility(View.VISIBLE);
                        // }
                        if (!instance.isPortrait()) {
                            setViewVisible(!this.toolsVisible);
                        }
                        // HiTools.hideVirtualKey(this);

                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return false;
    }

    void initBtn() {
        if (playState.isListening()) {
            startListen();
        } else {
            stopListen();
        }
        if (playState.isRecording()) {
            startRecording();
        } else {
            stopRecording();
        }

    }

    String filePath = null;

    void startRecording() {
        if (!isVideoShowing) {
            showAlert(getString(R.string.alert_play_video_first));
            return;
        }
        if (!playState.isRecording()) {
            if (!isSDCardValid()) {
                showAlert(getString(R.string.alert_snapshot_no_sdcard));
                return;
            }
            if (!TwsTools.checkPermission(LiveView_HichipActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                TwsTools.showAlertDialog(LiveView_HichipActivity.this);
                return;
            }

            String file = TwsTools.getFilePath(mCamera.getUid(), TwsTools.PATH_RECORD_MANUALLY) + "/" + TwsTools.getFileNameWithTime(mCamera.getUid(), TwsTools.PATH_RECORD_MANUALLY);
            filePath = file;
            setRecodingView();
            mCamera.startRecording(file, mSelectedChannel);
//            playState.setRecording(true);
//            txt_recording_tip_time().setText("00:00");
//            playState.setRecordingSeconds(0);
//            recordingHandler.postDelayed(recordingTask, 1000);
        }
//        ll_recording_tip().setVisibility(View.VISIBLE);
//        if (instance.isPortrait()) {
//            getBtn_recording().setImageResource(R.drawable.ic_btn_record_press);
//        } else {
//            getBtn_recording().setImageResource(R.drawable.ic_btn_liveview_record_open);
//        }
    }

    void stopRecording() {
        if (playState.isRecording()) {
            delayHandler.removeMessages(0);
            if (playState.isRecording()) {
                mCamera.stopRecording(mSelectedChannel);
                //myCamera.stop_record();
                playState.setRecording(false);
                if (filePath != null) {
                    File recordFile = new File(filePath);
                    if (recordFile.exists() && recordFile.isFile() && recordFile.length() < videoHeight * videoWidth * 2 / 8 / 20) {
                        recordFile.delete();
                    } else if (recordFile.exists() && recordFile.isFile()) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(filePath))));
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRecodingView();
                }
            });

            recordingHandler.removeCallbacks(recordingTask);
        }

    }

    @Override
    public void callbackState(IMyCamera camera, int channel, int state, int w, int h) {
        if (state == 1) {
            LiveView_HichipActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopRecording();
                }
            });
            isVideoShowing = false;
        } else if (state == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videoLoadProgressBar.setVisibility(View.GONE);
                }
            });

            isVideoShowing = true;
            if (w >= 1000) {
                playState.setVideoQuality(0);
            } else {
                playState.setVideoQuality(1);
            }
            videoWidth = w;
            videoHeight = h;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView txt_videoQuality = (TextView) findViewById(R.id.txt_videoQuality);
                    if (txt_videoQuality != null) {
                        txt_videoQuality.setText(String.format("%d x %d", videoWidth, videoHeight));
                    }
                    mCamera.saveSnapShot(mSelectedChannel, TwsTools.getFilePath(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB), TwsTools.getFileNameWithTime(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB), new IMyCamera.TaskExecute() {
                        @Override
                        public void onPosted(IMyCamera c, Object data) {

                        }
                    });
                }
            });
        }
        if (w != 0 && h != 0 && Math.abs(this.mCamera.getVideoRatio(LiveView_HichipActivity.this) - (float) w / h) > 0.2) {
            this.mCamera.setVideoRatio(LiveView_HichipActivity.this, (float) w / h);
            if (monitor != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        monitor.resizeVideoWrapper(LiveView_HichipActivity.this.mCamera);
                    }
                });
            }
        }
    }

    @Override
    public void callbackPlayUTC(IMyCamera var1, int var2) {

    }

    class playState {
        private playState(IMyCamera camera) {
            this.camera = camera;
        }

        public IMyCamera getCamera() {
            return camera;
        }

        IMyCamera camera;

        boolean isListening;
        boolean isSpeaking;
        boolean isRecording;

        public boolean isRecordingPause() {
            return isRecordingPause;
        }

        public void setRecordingPause(boolean recordingPause) {
            isRecordingPause = recordingPause;
        }

        boolean isRecordingPause;

        public int getRecordingSeconds() {
            return recordingSeconds;
        }

        public void setRecordingSeconds(int recordingSeconds) {
            this.recordingSeconds = recordingSeconds;
        }

        int recordingSeconds;

        public int getVideoQuality() {
            return mCamera.getVideoQuality();
        }

        public void setVideoQuality(final int videoQuality) {
            //this.videoQuality = videoQuality;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getBtn_stream().setText(souceList[videoQuality]);
                }
            });
            mCamera.setVideoQuality(videoQuality);
            mCamera.sync2Db(LiveView_HichipActivity.this);
        }

        int videoQuality;

        public boolean isListening() {
            return isListening;
        }

        public void setListening(boolean listening) {
            isListening = listening;
        }

        public boolean isSpeaking() {
            return isSpeaking;
        }

        public void setSpeaking(boolean speaking) {
            isSpeaking = speaking;
        }

        public boolean isRecording() {
            return isRecording;
        }

        public void setRecording(boolean recording) {
            isRecording = recording;
            if (recording) {
                isRecordingPause = false;
            }
        }

    }

    void setRecodingView() {
        if (playState.isRecording()) {
            ll_recording_tip().setVisibility(View.VISIBLE);
            if (instance.isPortrait()) {
                getBtn_recording().setImageResource(R.drawable.ic_btn_record_press);
            } else {
                getBtn_recording().setImageResource(R.drawable.ic_btn_liveview_record_open);
            }
            getBtn_stream().setBackgroundResource(R.drawable.btn_videoquality_shape_disable);
        } else {
            ll_recording_tip().setVisibility(View.INVISIBLE);
            if (instance.isPortrait()) {
                getBtn_recording().setImageResource(R.drawable.ic_btn_record_nor);
            } else {
                getBtn_recording().setImageResource(R.drawable.ic_btn_liveview_record_close);
            }
            getBtn_stream().setBackgroundResource(R.drawable.btn_videoquality_shape_normal);
        }
    }

    private Handler recordingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //初始化
                case 0:
                    if (!playState.isRecording()) {
                        playState.setRecording(true);
                        txt_recording_tip_time().setText("00:00");
                        playState.setRecordingSeconds(0);
                    }
                    setRecodingView();
                    break;
                //停止录像
                case 1:
                    setRecodingView();
                    break;
                //录像失败
                case 2:
                    showAlert(getString(R.string.alert_record_failed));
                    stopRecording();
                    break;
                //开始录像
                case 3:
                    recordingHandler.postDelayed(recordingTask, 1000);
                    break;
            }
        }

    };

    private Runnable recordingTask = new Runnable() {
        public void run() {
            if (!playState.isRecordingPause()) {
                // TODOAuto-generated method stub
                playState.setRecordingSeconds(playState.getRecordingSeconds() + 1);
                int hours = playState.getRecordingSeconds() / 60 / 60;
                int minutes = playState.getRecordingSeconds() % (60 * 60) / 60;
                int seconds = playState.getRecordingSeconds() % 60;
                String text = "";
                if (hours > 0) {
                    text += (hours >= 10 ? hours : ("0" + hours)) + ":";
                }
                text += (minutes >= 10 ? minutes : ("0" + minutes)) + ":";
                text += seconds >= 10 ? seconds : ("0" + seconds);
                txt_recording_tip_time().setText(text);
            }
            //需要执行的代码
            recordingHandler.postDelayed(this, 1 * 1000);//设置延迟时间，此处是1秒
        }
    };

    private void refreshViewStatus() {
//        if (isVideoShowing) {
//            getBtn_recording().setEnabled(true);
//            getBtn_listen().setEnabled(true);
//            getBtn_talk().setEnabled(true);
//            getBtn_stream().setEnabled(true);
//        } else {
//            getBtn_listen().setEnabled(false);
//            getBtn_recording().setEnabled(false);
//            getBtn_talk().setEnabled(false);
//            getBtn_stream().setEnabled(false);
//        }
    }

    //光学变焦
    private void clickZoom(ImageView iv) {
        @SuppressLint("InflateParams")
        View customView = getLayoutInflater().inflate(R.layout.popview_zoom_focus, null, false);

        mPopupWindow = new PopupWindow(customView);
        ColorDrawable cd = new ColorDrawable(-0000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        // width = 210 height = 90
        int offsetx = TwsTools.dip2px(this, 20);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int offsety = TwsTools.dip2px(this, 90);

        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, offsety - location[1]);

        // 拉近操作
        Button btnZoomin = (Button) customView.findViewById(R.id.btn_zoomin);
        btnZoomin.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_ZOOMIN,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        // 拉远按钮
        Button btnZoomout = (Button) customView.findViewById(R.id.btn_zoomout);
        btnZoomout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_ZOOMOUT,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        // 聚焦+
        Button btnFocusin = (Button) customView.findViewById(R.id.btn_focusin);
        btnFocusin.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_FOCUSIN,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        // 聚焦-
        Button btnFocusout = (Button) customView.findViewById(R.id.btn_focusout);
        btnFocusout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_FOCUSOUT,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP,
                            HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) HiLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
    }

    // 预设位
    private void clickPreset(ImageView iv) {
        @SuppressLint("InflateParams") final View customView = getLayoutInflater().inflate(R.layout.popview_preset, null, false);

        mPopupWindow = new PopupWindow(customView);
        ColorDrawable cd = new ColorDrawable(0000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        // w:210 h:40+5*3+35*2 = 125

		/*
         * if (getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_PORTRAIT) { int offsetx =
		 * -HiTools.dip2px(this, 80); int offsety = HiTools.dip2px(this, 40);
		 * mPopupWindow.showAsDropDown(iv,offsetx,offsety); } else
		 * if(getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_LANDSCAPE){
		 */
        int offsetx = TwsTools.dip2px(this, 20);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int offsety = TwsTools.dip2px(this, 90);
        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, offsety - location[1]);
        // }

        if (presetList != null && presetList.Points != null) {
            for (AVIOCTRLDEFs.SMsgAVIoctrlPointInfo info : presetList.Points) {
                if (info != null && info.BitID <= 8) {
                    if (info.BitID == 1) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_0)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 2) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_1)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 3) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_2)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 4) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_3)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 5) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_4)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 6) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_5)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 7) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_6)).setTextColor(Color.GREEN);
                    } else if (info.BitID == 8) {
                        ((RadioButton) customView.findViewById(R.id.radio_quality_7)).setTextColor(Color.GREEN);
                    }
                }
            }
        }
        final RadioGroup radio_group_preset = (RadioGroup) customView.findViewById(R.id.radio_group_preset);
        Button btn_set = (Button) customView.findViewById(R.id.btn_preset_set);

        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                // isSetPTZReset = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((RadioButton) customView.findViewById(radio_group_preset.getCheckedRadioButtonId())).setTextColor(Color.GREEN);
                        select_preset = Integer.parseInt((customView.findViewById(radio_group_preset.getCheckedRadioButtonId())).getTag().toString());
                    }
                });
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_PRESET,
                        HiChipDefines.HI_P2P_S_PTZ_PRESET.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_PRESET_ACT_SET, select_preset));
            }
        });
        ((RadioButton) customView.findViewById(R.id.radio_quality_0)).performClick();
        Button btn_call = (Button) customView.findViewById(R.id.btn_preset_call);
        btn_call.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                select_preset = Integer.parseInt((customView.findViewById(radio_group_preset.getCheckedRadioButtonId())).getTag().toString());
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_PRESET,
                        HiChipDefines.HI_P2P_S_PTZ_PRESET.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_PRESET_ACT_CALL, select_preset));
            }
        });

        Button btn_preset_clear = (Button) customView.findViewById(R.id.btn_preset_clear);
        btn_preset_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((RadioButton) customView.findViewById(radio_group_preset.getCheckedRadioButtonId())).setTextColor(ContextCompat.getColor(LiveView_HichipActivity.this, R.color.lightestgray));
                select_preset = Integer.parseInt((customView.findViewById(radio_group_preset.getCheckedRadioButtonId())).getTag().toString());
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_PRESET,
                        HiChipDefines.HI_P2P_S_PTZ_PRESET.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_PRESET_ACT_DEL, select_preset));
            }
        });

    }

    int moveX;
    int moveY;

    private void resetMonitorSize(boolean large, double move) {

        if (monitor.height == 0 && monitor.width == 0) {

            initMatrix((int) monitor.screen_width, (int) monitor.screen_height);
        }

        moveX = (int) (move / 2);
        moveY = (int) ((move * monitor.screen_height / monitor.screen_width) / 2);

        if (large) {
            HiLog.e(" larger and larger ");
            if (monitor.width <= 2 * monitor.screen_width && monitor.height <= 2 * monitor.screen_height) {

                monitor.left -= (moveX / 2);
                monitor.bottom -= (moveY / 2);
                monitor.width += (moveX);
                monitor.height += (moveY);
            }
        } else {
            HiLog.e(" smaller and smaller ");

            monitor.left += (moveX / 2);
            monitor.bottom += (moveY / 2);
            monitor.width -= (moveX);
            monitor.height -= (moveY);
        }

        if (monitor.left > 0 || monitor.width < (int) monitor.screen_width || monitor.height < (int) monitor.screen_height || monitor.bottom > 0) {
            initMatrix((int) monitor.screen_width, (int) monitor.screen_height);
        }

        HiLog.e("mMonitor.left=" + monitor.left + " mMonitor.bottom=" + monitor.bottom + "\n mMonitor.width=" + monitor.width + " mMonitor.height=" + monitor.height);

        if (monitor.width > (int) monitor.screen_width) {
            monitor.setState(1);
        } else {
            monitor.setState(0);
        }

        monitor.setMatrix(monitor.left, monitor.bottom, monitor.width, monitor.height);

    }

    private void initMatrix(int screen_width, int screen_height) {
        monitor.left = 0;
        monitor.bottom = 0;

        monitor.width = screen_width;
        monitor.height = screen_height;
    }
}
