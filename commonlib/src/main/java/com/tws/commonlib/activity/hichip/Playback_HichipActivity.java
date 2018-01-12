package com.tws.commonlib.activity.hichip;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.drm.DrmStore;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hichip.base.HiLog;
import com.hichip.base.HiThread;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.EventListActivity;
import com.tws.commonlib.base.MyLiveViewGLMonitor;
import com.tws.commonlib.base.MyPlaybackGLMonitor;
import com.tws.commonlib.base.ScreenSwitchUtils;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.IPlayStateListener;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.widget.AdapterView.OnItemClickListener;

public class Playback_HichipActivity extends BaseActivity implements IIOTCListener, IPlayStateListener, View.OnTouchListener, View.OnClickListener {

    private final static int HANDLE_MESSAGE_PROGRESSBAR_RUN = 0x90000002;
    private final static int HANDLE_MESSAGE_SEEKBAR_RUN = 0x90000003;

    public final static short HI_P2P_PB_PLAY = 1;
    public final static short HI_P2P_PB_STOP = 2;
    public final static short HI_P2P_PB_PAUSE = 3;
    public final static short HI_P2P_PB_SETPOS = 4;
    public final static short HI_P2P_PB_GETPOS = 5;

    private int video_width;
    private int video_height;

    private ProgressThread pthread = null;

    private ProgressBar prs_loading;
    private ImageView img_shade;

    private byte[] startTime;
    private byte[] oldStartTime;
    private MyPlaybackGLMonitor mMonitor;
    private HichipCamera mCamera;
    private SeekBar prs_playing;
    private boolean mVisible = true;

    private long playback_time;
    private long startTimeLong;
    private long endTimeLong;

    private int progressTime;

    private short model;// PLAY=1,STOP=2,PAUSE=3,SETPOS=4,GETPOS=5

    private RelativeLayout playback_view_screen;
    private boolean visible = true;
    private boolean isSelected = true;
    private ImageView play_btn_playback_online, play_btn_exit;
    private ConnectionChangeReceiver myReceiver;
    private boolean isPlaying = false;
    private LinearLayout mllPlay;
    private TextView mTvStartTime, mTvEndTime;
    private final static SimpleDateFormat sdf = new SimpleDateFormat("00:mm:ss");
    private boolean mIsDrag = false;
    private LinearLayout mLlCurrPro;
    private TextView mTvCurrPro, mTvPrecent;
    private boolean mIsEnd = false;
    private boolean mFlag = false;
    int eventType = 0;
    boolean hasSaveSnapshot = false;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        myReceiver = new ConnectionChangeReceiver();
        this.registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置无标题

        setContentView(R.layout.activity_playback_landscape_hichip);

        Bundle bundle = this.getIntent().getExtras();
        String uid = bundle.getString(TwsDataValue.EXTRA_KEY_UID);
        byte[] b_startTime = bundle.getByteArray("st");
        oldStartTime = new byte[8];
        System.arraycopy(b_startTime, 0, oldStartTime, 0, 8);
        playback_time = bundle.getLong("pb_time");
        eventType = bundle.getInt("event_type");
        startTimeLong = bundle.getLong(EventList_HichipActivity.VIDEO_PLAYBACK_START_TIME);
        endTimeLong = bundle.getLong(EventList_HichipActivity.VIDEO_PLAYBACK_END_TIME);

        for (IMyCamera camera : TwsDataValue.cameraList()) {
            if (camera.getUid().equals(uid)) {
                mCamera = (HichipCamera) camera;
                break;
            }
        }

        initView();
        setListerners();
        showLoadingShade();
        mCamera.registerIOTCListener(this);
        mCamera.registerPlayStateListener(this);

        if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
            startTime = oldStartTime;
        } else {
            if (mCamera.getSummerTimer()) {
                HiChipDefines.STimeDay newTime = new HiChipDefines.STimeDay(oldStartTime, 0);
                newTime.resetData(-1);
                startTime = newTime.parseContent();
            } else {
                startTime = oldStartTime;
            }
        }
        startPlayBack();
        model = HI_P2P_PB_PLAY;
    }

    private void setListerners() {
        mMonitor.setOnTouchListener(this);
        play_btn_exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Playback_HichipActivity.this.finish();
            }
        });
        mMonitor.setOnClickListener(this);
    }

    private void startPlayBack() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                mCamera.startPlayback(new HiChipDefines.STimeDay(startTime, 0), mMonitor);
            }
        }.start();
    }

    protected void initView() {
        mMonitor = (MyPlaybackGLMonitor) findViewById(R.id.monitor_playback_view);
        mCamera.setLiveShowMonitor(mMonitor);
        play_btn_exit = (ImageView) findViewById(R.id.play_btn_playback_exit);
        prs_loading = (ProgressBar) findViewById(R.id.prs_loading);
        img_shade = (ImageView) findViewById(R.id.img_shade);
        mllPlay = (LinearLayout) findViewById(R.id.rl_play);
        mTvStartTime = (TextView) findViewById(R.id.tv_start_time);
        mTvEndTime = (TextView) findViewById(R.id.tv_end_time);
        mTvEndTime.setText(sdf.format(new Date(endTimeLong - startTimeLong)));
        progressTime = (int) ((endTimeLong - startTimeLong) / 1000);
        prs_playing = (SeekBar) findViewById(R.id.prs_playing);
        prs_playing.setMax(progressTime);
        prs_playing.setProgress(0);
        mLlCurrPro = (LinearLayout) findViewById(R.id.ll_cureent_progress);
        mTvCurrPro = (TextView) findViewById(R.id.tv_current_pro);
        mTvPrecent = (TextView) findViewById(R.id.tv_precent);

        prs_playing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mIsEnd) {
                    mCamera.startPlayback(new HiChipDefines.STimeDay(startTime, 0), mMonitor);
                }
                int count = seekBar.getProgress();
                int pre = count * 100 / progressTime;
                if (pre < 1) {   //1.文件的时长的1%  （0-6） 秒重头开始播放   下面要的是整数
                    if (!mIsEnd) {
                        mCamera.stopPlayback();
                        mCamera.startPlayback(new HiChipDefines.STimeDay(startTime, 0), mMonitor);
                    }
                } else {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_POS_SET,
                            HiChipDefines.HI_P2P_PB_SETPOS_REQ.parseContent(0, pre, startTime));
                }
                model = HI_P2P_PB_PAUSE;
                mIsDrag = false;
                mLlCurrPro.setVisibility(View.GONE);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsDrag = true;

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mIsDrag) {
                    mLlCurrPro.setVisibility(View.VISIBLE);
                    // mTvCurrPro.setText(sdf.format(new Date(progress *
                    // 1000)));
                    // mTvDuraTime.setText(sdf.format(new Date(endTimeLong -
                    // startTimeLong)));
                    double dou = ((double) progress / progressTime) * 100;
                    int rate = (int) Math.round(dou);
                    mTvPrecent.setText(rate + "%100");
                } else {
                    mLlCurrPro.setVisibility(View.GONE);
                    // mTvCurrPro.setText(" ");
                    // mTvPrecent.setText(" ");
                }

            }
        });

        playback_view_screen = (RelativeLayout) findViewById(R.id.playback_view_screen);
        playback_view_screen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (model == 0) {
                    return;
                }
                visible = !visible;
                mllPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        });

        play_btn_playback_online = (ImageView) findViewById(R.id.play_btn_playback_online);
        play_btn_playback_online.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (mCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    play_btn_playback_online.setClickable(false);
                    if (mIsEnd) {
                        mCamera.startPlayback(new HiChipDefines.STimeDay(startTime, 0), mMonitor);
                    } else {
                        if (isPlaying) {
                            play_btn_playback_online.setSelected(true);
                        } else {
                            play_btn_playback_online.setSelected(false);
                        }
                        isPlaying = !isPlaying;
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL, HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
                    }
                }
            }
        });
    }

    private void setImageVisible(boolean b) {
        if (b) {
            prs_playing.setVisibility(View.VISIBLE);
            play_btn_playback_online.setVisibility(View.VISIBLE);

        } else {

            play_btn_playback_online.setVisibility(View.GONE);
            prs_playing.setVisibility(View.GONE);
        }
    }

    private void showLoadingShade() {

        prs_loading.setMax(100);
        prs_loading.setProgress(10);
        pthread = new ProgressThread();
        pthread.startThread();
    }

    private void displayLoadingShade() {
        if (pthread != null)
            pthread.stopThread();
        pthread = null;
        prs_loading.setVisibility(View.GONE);
        img_shade.setVisibility(View.GONE);

        visible = true;
        setImageVisible(visible);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            mCamera.registerIOTCListener(this);
            mCamera.registerPlayStateListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {

            if (model != 0) {
                model = 0;
                oldTime = 0;
            }

            mCamera.stopPlayback();
            mCamera.unregisterIOTCListener(this);
            mCamera.unregisterPlayStateListener(this);
            HiLog.e("unregister");

        } else {
            HiLog.e("camera == null");
        }

        if (pthread != null) {

            pthread.stopThread();
            pthread = null;

        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
    }

    private class ProgressThread extends HiThread {
        public void run() {
            while (isRunning) {
                sleep(100);
                Message msg = handler.obtainMessage();
                msg.what = HANDLE_MESSAGE_PROGRESSBAR_RUN;
                handler.sendMessage(msg);
            }
        }
    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int arg1, int arg3, byte[] arg2) {

        if (mCamera != camera)
            return;

        Bundle bundle = new Bundle();
        bundle.putByteArray(TwsDataValue.EXTRAS_KEY_DATA, arg2);
        Message msg = handler.obtainMessage();
        msg.what = arg1;
        msg.arg2 = arg3;
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    @Override
    public void initSendAudio(IMyCamera paramCamera, boolean paramBoolean) {

    }

    @Override
    public void receiveOriginalFrameData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {

    }

    @Override
    public void receiveRGBData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {

    }

    @Override
    public void receiveRecordingData(IMyCamera paramCamera, int avChannel, int paramInt1, String path) {

    }

    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera arg0, int arg1) {
        if (arg0 != mCamera || mCamera == null) {
            return;
        }
        Message message = Message.obtain();
        message.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        message.arg1 = arg1;
        message.obj = arg0;
        handler.sendMessage(message);

    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TwsDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    HichipCamera camera = (HichipCamera) msg.obj;
                    switch (msg.arg1) {
                        case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
                        case HiCamera.CAMERA_CHANNEL_STREAM_ERROR:

                            if (camera != null) {
                                camera.stopPlayback();
                                // camera.disconnect();
                                // camera.connect();
                            }
                            TwsToast.showToast(Playback_HichipActivity.this, getString(R.string.toast_connect_drop));
                            // PlaybackOnlineActivity.this.finish();
                            NetworkError();
                            break;

                    }

                    break;

                case ICameraPlayStateCallback.PLAY_STATE_START:

                    isPlaying = true;
                    play_btn_playback_online.setClickable(true);
                    model = HI_P2P_PB_PLAY;
                    mIsEnd = false;
                    video_width = msg.arg1;
                    video_height = msg.arg2;
                    play_btn_playback_online.setSelected(false);


                    resetMonitorSize();
                    break;
                case ICameraPlayStateCallback.PLAY_STATE_EDN:
                    Log.i("tedu", "--------PLAY_STATE_EDN------");
                    isPlaying = false;
                    mIsEnd = true;
                    model = HI_P2P_PB_STOP;
                    // mTvStartTime.setText(sdf.format(new Date(0)));

                    play_btn_playback_online.setSelected(true);
                    play_btn_playback_online.setClickable(true);
                    prs_playing.setProgress(progressTime);

                    mCamera.stopPlayback();

                    TwsToast.showToast(Playback_HichipActivity.this, getString(R.string.tips_play_record_end));

                    break;
                case ICameraPlayStateCallback.PLAY_STATE_POS:

                    break;

                case HANDLE_MESSAGE_PROGRESSBAR_RUN:

                    int cur = prs_loading.getProgress();

                    if (cur >= 100) {
                        prs_loading.setProgress(10);
                    } else {
                        prs_loading.setProgress(cur + 8);
                    }

                    model = HI_P2P_PB_PLAY;

                    break;

                case HANDLE_MESSAGE_SEEKBAR_RUN:
                    if (!mIsDrag) {
                        prs_playing.setProgress(msg.arg1);
                    }
                    mTvStartTime.setText(sdf.format(new Date(msg.arg1 * 1000)));
                    if (!hasSaveSnapshot) {
                        hasSaveSnapshot = true;
                        saveSnap();
                    }
                    break;
                case HiChipDefines.HI_P2P_PB_POS_SET:
                    // try {
                    // Thread.sleep(600); // 每一帧的时间间隔是500毫秒
                    // } catch (InterruptedException e) {
                    //
                    // e.printStackTrace();
                    // }
                    model = HI_P2P_PB_PLAY;

                    if (!isPlaying && !mIsEnd) {
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_PLAY_CONTROL,
                                HiChipDefines.HI_P2P_S_PB_PLAY_REQ.parseContent(0, HI_P2P_PB_PAUSE, startTime));
                        play_btn_playback_online.setSelected(false);
                        isPlaying = !isPlaying;
                        // // if (isPlaying) {
                        // play_btn_playback_online.setSelected(isSelected);
                    }

                    break;

                case HiChipDefines.HI_P2P_PB_PLAY_CONTROL://stopPlayback   startPlayback 都会回调这个CONTROL *****注意******
                    //isSelected = !isSelected;
                    // if (isPlaying) {
                    //play_btn_playback_online.setSelected(isSelected);
                    // }
                    play_btn_playback_online.setClickable(true);

                    break;

            }
        }

    };

    private void resetMonitorSize() {

        if (video_width == 0 || video_height == 0) {
            return;
        }
        displayLoadingShade();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screen_width = dm.widthPixels;
        int screen_height = dm.heightPixels;
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(wlp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        int width = screen_width;
        int height = screen_height;

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        mMonitor.setLayoutParams(lp);
    }

    long oldTime;

    @Override
    public void callbackPlayUTC(IMyCamera camera, int timeInteger) {
        // +++
        if (mCamera != camera || model == HI_P2P_PB_PAUSE || model == 0)
            return;

        if (oldTime == 0) {
            oldTime = (long) timeInteger;
        }
        long sub = (long) timeInteger - oldTime;
        int step = (int) (sub / 1000);
        Message msg = handler.obtainMessage();
        msg.what = HANDLE_MESSAGE_SEEKBAR_RUN;
        msg.arg1 = step;
        handler.sendMessage(msg);
    }

    @Override
    public void callbackState(IMyCamera camera, int channel, int state, int w, int h) {
        if (mCamera != camera)
            return;

        if (state == ICameraPlayStateCallback.PLAY_STATE_START) {
            HiLog.e("state=PLAY_STATE_START");
        }

        Message msg = handler.obtainMessage();
        msg.what = state;
        msg.arg1 = w;
        msg.arg2 = h;
        handler.sendMessage(msg);
    }


    public void NetworkError() {
        showAlertnew(0, null, getString(R.string.camera_state_disconnect), getString(R.string.finish), null,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Playback_HichipActivity.this.finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                Playback_HichipActivity.this.finish();
                                break;
                        }

                    }
                });
    }

    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mobNetInfo != null && !mobNetInfo.isConnected() && wifiNetInfo != null && !wifiNetInfo.isConnected()) {
                // if (isPlaying) {
                TwsToast.showToast(context, getString(R.string.toast_connect_drop));
                if (mCamera != null) {
                    mCamera.stopPlayback();
                    // mCamera.disconnect();
                    // mCamera.connect();
                }
                // PlaybackOnlineActivity.this.finish();
                NetworkError();
                return;
                // }
            }
        }
    }

    private static boolean isSDCardValid() {

        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    void saveSnap() {
        if (mCamera != null) {
            if (isSDCardValid()) {// 濡傛灉sd鍗″彲鐢�
                if (!TwsTools.checkPermission(Playback_HichipActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                String filenameString = TwsTools.getFileNameWithTime(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB, startTimeLong, eventType);// mCamera.getUid() + "_" + eventType + startCal.get(Calendar.YEAR) + (startCal.get(Calendar.MONTH) + 1) + startCal.get(Calendar.DAY_OF_MONTH) + "0" + startCal.get(Calendar.HOUR_OF_DAY) + startCal.get(Calendar.MINUTE) + startCal.get(Calendar.SECOND) + ".jpg";
                mCamera.saveSnapShot(0, TwsTools.getFilePath(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB), filenameString, new IMyCamera.TaskExecute() {
                    @Override
                    public void onPosted(IMyCamera camera, Object data) {
                        hasSaveSnapshot = data != null;
                    }
                });
            }
        }
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
        if (v.getId() == R.id.monitor_playback_view) {
            int nCnt = event.getPointerCount();
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
                mMonitor.setTouchMove(2);
                for (int i = 0; i < nCnt; i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);
                    Point pt = new Point((int) x, (int) y);
                }
                xlenOld = Math.abs((int) event.getX(0) - (int) event.getX(1));
                ylenOld = Math.abs((int) event.getY(0) - (int) event.getY(1));
                nLenStart = Math.sqrt((double) xlenOld * xlenOld + (double) ylenOld * ylenOld);

            } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
                mMonitor.setTouchMove(2);
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
                        mMonitor.setTouchMove(0);
                        if (model == 0) {
                            return false;
                        }
                        // if (mVisible) {
                        // mllPlay.animate().translationX(1.0f).translationY(mllPlay.getHeight()).start();
                        // } else {
                        // mllPlay.animate().translationX(1.0f).translationY(1.0f).start();
                        // }
                        // mVisible = !mVisible;

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mMonitor.getTouchMove() != 0)
                            break;
                        if (Math.abs(move_x - action_down_x) > 40 || Math.abs(move_y - action_down_y) > 40) {
                            mMonitor.setTouchMove(1);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
            }
        }
        return false;
    }

    int moveX;
    int moveY;

    private void resetMonitorSize(boolean large, double move) {

        if (mMonitor.height == 0 && mMonitor.width == 0) {

            initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
        }

        moveX = (int) (move / 2);
        moveY = (int) ((move * mMonitor.screen_height / mMonitor.screen_width) / 2);

        if (large) {
            if (mMonitor.width <= 2 * mMonitor.screen_width && mMonitor.height <= 2 * mMonitor.screen_height) {

                mMonitor.left -= (moveX / 2);
                mMonitor.bottom -= (moveY / 2);
                mMonitor.width += (moveX);
                mMonitor.height += (moveY);
            }
        } else {
            mMonitor.left += (moveX / 2);
            mMonitor.bottom += (moveY / 2);
            mMonitor.width -= (moveX);
            mMonitor.height -= (moveY);
        }

        if (mMonitor.left > 0 || mMonitor.width < (int) mMonitor.screen_width || mMonitor.height < (int) mMonitor.screen_height || mMonitor.bottom > 0) {
            initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
        }
        if (mMonitor.width > (int) mMonitor.screen_width) {
            mMonitor.setState(1);
        } else {
            mMonitor.setState(0);
        }

        mMonitor.setMatrix(mMonitor.left, mMonitor.bottom, mMonitor.width, mMonitor.height);

    }

    private void initMatrix(int screen_width, int screen_height) {
        mMonitor.left = 0;
        mMonitor.bottom = 0;

        mMonitor.width = screen_width;
        mMonitor.height = screen_height;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.monitor_playback_view) {
            if (mVisible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        mllPlay.animate().translationX(1.0f).translationY(mllPlay.getHeight()).start();
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        mllPlay.animate().translationX(1.0f).translationY(1.0f).start();
                    }
                }
            }
            mVisible = !mVisible;

        }

    }

}