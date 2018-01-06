package com.tws.commonlib.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.ICameraPlayStateCallback;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.L;
import com.tutk.IOTC.Monitor;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.MyLiveViewGLMonitor;
import com.tws.commonlib.base.ScreenSwitchUtils;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PlaybackActivity extends BaseActivity implements IRegisterIOTCListener, View.OnTouchListener, ICameraPlayStateCallback {

    private static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH = 14;
    private static final int STS_CHANGE_CHANNEL_STREAMINFO = 99;
    private static final int PLAY_TIMEOUT = 98;
    private static final int DISCONNECT_DELAY = 97;

    private final int OPT_MENU_ITEM_PLAY = 0;

    //private TouchedMonitor monitor = null;
    private MyLiveViewGLMonitor monitor = null;
    private MyCamera mCamera = null;

    private TextView txtEventType;
    private TextView txtEventTime;


    private String mDevUID;

    private String mEventUUID;
    private int mEvtType;
    // private long mEvtTime;
    private AVIOCTRLDEFs.STimeDay mEvtTime2;

    private int mVideoWidth;
    private int mVideoHeight;

    private final int MEDIA_STATE_STOPPED = 0;
    private final int MEDIA_STATE_PLAYING = 1;
    private final int MEDIA_STATE_PAUSED = 2;
    private final int MEDIA_STATE_OPENING = 3;

    private int mPlaybackChannel = -1;
    private int mMediaState = MEDIA_STATE_STOPPED;
    boolean toolsVisible;
    Bitmap snap;
    private boolean isQuiting = false;

    LinearLayout ll_playback() {
        return (LinearLayout) findViewById(R.id.ll_playback);
    }

    ImageView img_playback() {
        return (ImageView) findViewById(R.id.img_playback);
    }

    ProgressBar videoProgressBar() {
        return (ProgressBar) findViewById(R.id.videoProgressBar);
    }

    RelativeLayout monitorLayout() {
        return (RelativeLayout) findViewById(R.id.monitorLayout);
    }

    ScreenSwitchUtils instance;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // instance = ScreenSwitchUtils.init(this.getApplicationContext());
        mDevUID = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (NSCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.uid.equalsIgnoreCase(mDevUID)) {
                mCamera = (MyCamera) _camera;
                mCamera.registerIOTCListener(this);
                mCamera.resetEventCount();
                break;
            }
        }

        Bundle bundle = this.getIntent().getExtras();
        mDevUID = bundle != null ? bundle.getString(TwsDataValue.EXTRA_KEY_UID) : "";
        mEvtType = bundle != null ? bundle.getInt("event_type") : -1;
        // mEvtTime = bundle != null ? bundle.getLong("event_time") : -1;
        mEventUUID = bundle != null ? bundle.getString("event_uuid") : "";
        if (bundle.containsKey("event_time2")) {
            byte[] d = bundle.getByteArray("event_time2");
            // d[7] = (byte)((int)d[7]+10);
            mEvtTime2 = bundle != null ? new STimeDay(d) : null;
        }
        /*monitor = (Monitor)findViewById(R.id.monitor);
        monitor.setOnTouchListener(null);
		monitor.setClickable(false);
		monitor.setFocusable(false);
		monitor.setLongClickable(false);
		monitor.setOnGenericMotionListener(null);
		monitor.setPressed(false);*/


        toolsVisible = true;
//        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int rotation = display.getOrientation();
        setupViewInPortraitLayout();
//        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
//            setupViewInPortraitLayout();
//        } else {
//            setupViewInLandscapeLayout();
//        }

        startPlayBack();
    }

    private void setViewVisible(boolean visible) {
        findViewById(R.id.lay_live_tools_bottom).setVisibility(visible ? View.VISIBLE : View.GONE);
        this.toolsVisible = visible;
    }

    void refreshButton() {
        if (mMediaState == MEDIA_STATE_STOPPED || mMediaState == MEDIA_STATE_PAUSED) {
            img_playback().setImageResource(R.drawable.ic_menu_play);
        } else {
            img_playback().setImageResource(R.drawable.ic_menu_pause);
        }
        ll_playback().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackChannel < 0) {
//                    if (mCamera != null) {
//                        videoProgressBar().setVisibility(View.VISIBLE);
//                        mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0, mEvtTime2.toByteArray()));
//                        mMediaState = MEDIA_STATE_OPENING;
//
//						/* if server no response, close playback function */
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mPlaybackChannel < 0 && mMediaState == MEDIA_STATE_OPENING) {
//                                    mMediaState = MEDIA_STATE_STOPPED;
//                                    showAlert(getString(R.string.alert_play_record_timeout));
//                                }
//                                refreshButton();
//                            }
//                        }, 5000);
                    if (!handler.hasMessages(PLAY_TIMEOUT)) {
                        startPlayBack();
                    }
                    img_playback().setImageResource(R.drawable.ic_menu_pause);
//                    }
                } else {
                    if (mCamera != null) {
                        mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_PAUSE, 0, mEvtTime2.toByteArray()));
                        //videoProgressBar().setVisibility(View.INVISIBLE);
                        // img_playback().setImageResource(R.drawable.ic_menu_play);
                        if (mMediaState == MEDIA_STATE_PLAYING) {
                            videoProgressBar().setVisibility(View.INVISIBLE);
                        } else {
                            videoProgressBar().setVisibility(View.VISIBLE);
                        }
                    }
                }

            }
        });
    }

    @Override
    protected void initView() {
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        if (title != null) {
            title.setTitle(mCamera.name);
            title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
            title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

                @Override
                public void OnNavigationButtonClick(int which) {
                    switch (which) {
                        case NavigationBar.NAVIGATION_BUTTON_LEFT:
                            quit();
                            break;
                    }
                }
            });
        }
        refreshButton();
    }

    private void startPlayBack() {
        if (mCamera != null) {
            System.out.println("startPlayBack");
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0, mEvtTime2.toByteArray()));
            mMediaState = MEDIA_STATE_OPENING;
            videoProgressBar().setVisibility(View.VISIBLE);
                /* if server no response, close playback function */
            handler.removeMessages(PLAY_TIMEOUT);
            handler.sendEmptyMessageDelayed(PLAY_TIMEOUT, 10000);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //instance.start(this);
        // FlurryAgent.onStartSession(this, "Q1SDXDZQ21BQMVUVJ16W");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //instance.stop();
        // FlurryAgent.onEndSession(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //不是退出当前页面,则延迟disconnect
        if (!isQuiting) {
            handler.sendEmptyMessageDelayed(DISCONNECT_DELAY, 3000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.registerPlayStateListener(this);
        if (handler != null && handler.hasMessages(DISCONNECT_DELAY)) {
            handler.removeMessages(DISCONNECT_DELAY);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

//        if (monitor != null)
//            monitor.deattachCamera();
//
//        Configuration cfg = getResources().getConfiguration();
//        if (instance.isPortrait()) {
//            setupViewInPortraitLayout();
//        } else {
//            setupViewInLandscapeLayout();
//        }
//        if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//            setupViewInLandscapeLayout();
//
//        } else if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {
//
//            setupViewInPortraitLayout();
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {

            case KeyEvent.KEYCODE_BACK:

                quit();

                break;
        }

        return super.onKeyDown(keyCode, event);
    }

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		int icon = R.drawable.ic_menu_pause_inverse;
//		if (mMediaState == MEDIA_STATE_STOPPED || mMediaState == MEDIA_STATE_PAUSED)
//			icon = R.drawable.ic_menu_play;
//		else if (mMediaState == MEDIA_STATE_PLAYING)
//			icon = R.drawable.ic_menu_pause;
//
//		if (isCloudEvent == false) {
//			menu.add(Menu.NONE, OPT_MENU_ITEM_PLAY, 0, "Play").setIcon(icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//		}
//
//		return super.onCreateOptionsMenu(menu);
//	}

//	@Override
//	public boolean onOptionsItemSelected(final MenuItem item) {
//
//		int id = item.getItemId();
//
//		if (id == OPT_MENU_ITEM_PLAY) {
//
//			if (isCloudEvent == true) {
//
//			}else {
//				if (mPlaybackChannel < 0) {
//
//					if (mCamera != null) {
//						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0, mEvtTime2.toByteArray()));
//						mMediaState = MEDIA_STATE_OPENING;
//
//						/* if server no response, close playback function */
//						handler.postDelayed(new Runnable() {
//							@Override
//							public void run() {
//								if (mPlaybackChannel < 0 && mMediaState == MEDIA_STATE_OPENING) {
//									mMediaState = MEDIA_STATE_STOPPED;
//									Toast.makeText(PlaybackActivity.this, getText(R.string.tips_play_record_timeout), Toast.LENGTH_SHORT).show();
//								}
//								PlaybackActivity.this.invalidateOptionsMenu();
//							}
//						}, 5000);
//					}
//				} else {
//
//					if (mCamera != null) {
//						mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(mCameraChannel, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_PAUSE, 0, mEvtTime2.toByteArray()));
//					}
//				}
//			}
//
//
//			PlaybackActivity.this.invalidateOptionsMenu();
//		}
//
//		return super.onOptionsItemSelected(item);
//	}

    private void setupViewInLandscapeLayout() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        doFullScreenStatusBar(false);
        setContentView(R.layout.activity_playback_landscape);

        initView();
//		if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
//			bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
//			getSupportActionBar().setBackgroundDrawable(bg);
//
//			bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
//			getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
//		}
//
//		getSupportActionBar().setSubtitle(null);

        txtEventType = null;
        txtEventTime = null;

        // register camera
        monitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor);
        monitor.setMaxZoom(3.0f);
        monitor.setSetFixedScale(false);
        //monitor.setPtzEnable(false);
        // monitor.setOnTouchListener(null);
        monitor.setMonitorOnTouchListener(this);
        if (mPlaybackChannel >= 0) {
            // mCamera.resumePlay(mPlaybackChannel);
            monitor.attachCamera(mCamera, mPlaybackChannel);
        }
        setViewVisible(!this.toolsVisible);
    }

    private void setupViewInPortraitLayout() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_playback_portrait);

        initView();
//		if (Build.VERSION.SDK_INT < Build_VERSION_CODES_ICE_CREAM_SANDWICH) {
//			bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
//			getSupportActionBar().setBackgroundDrawable(bg);
//
//			bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
//			getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
//		}
//
//		getSupportActionBar().setSubtitle(getText(R.string.dialog_Playback).toString() + " : " + mDevNickname);

        txtEventType = (TextView) findViewById(R.id.txtEventType);
        txtEventTime = (TextView) findViewById(R.id.txtEventTime);

        txtEventType.setText(TwsTools.getEventType(PlaybackActivity.this, mEvtType, false));
        if (mEvtTime2 != null) {
            txtEventTime.setText(mEvtTime2.getLocalTime());
        }

        monitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor);
        monitor.setSetFixedScale(false);
        monitor.setMaxZoom(3.0f);
        //monitor.setPtzEnable(false);
        monitor.setOnTouchListener(null);

        if (mPlaybackChannel >= 0) {
            //mCamera.resumePlay(mPlaybackChannel);
            monitor.attachCamera(mCamera, mPlaybackChannel);
        }
    }

    private void disConnect() {
        L.i("IOTCamera_playback", "stop playback wait");
        mMediaState = MEDIA_STATE_STOPPED;
        if (monitor != null) {
            monitor.deattachCamera();
        }
        L.i("IOTCamera_playback", "stop playback wait 1");
        if (handler.hasMessages(PLAY_TIMEOUT)) {
            handler.removeMessages(PLAY_TIMEOUT);
        }
        L.i("IOTCamera_playback", "stop playback wait 2");
        if (mCamera != null) {
            if (mPlaybackChannel >= 0) {
                mCamera.stop(mPlaybackChannel);
                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0, mEvtTime2.toByteArray()));
                mPlaybackChannel = -1;
            }
            L.i("IOTCamera_playback", "stop playback wait 3");
            mCamera.unregisterPlayStateListener(this);
        }
        L.i("IOTCamera_playback", "stop playback");

        img_playback().setImageResource(R.drawable.ic_menu_play);
    }

    private void quit() {
        isQuiting = true;
        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
        }

        disConnect();
//        Bundle extras = new Bundle();
//        extras.putInt("event_type", mEvtType);
//        // extras.putLong("event_time", mEvtTime);
//        extras.putByteArray("event_time2", mEvtTime2.toByteArray());
//        extras.putString("event_uuid",);
//        extras.putString(TwsDataValue.EXTRA_KEY_UID, mDevUID);
//
//        Intent intent = new Intent();
        setResult(RESULT_OK, this.getIntent());
        finish();
    }

    private static boolean isSDCardValid() {

        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 淇濆瓨鍥剧墖锛屽苟涓旀坊鍔犺嚦澶氬獟浣撳簱
     *
     * @param fileName
     * @param frame
     * @return
     */
    private boolean saveImage(String fileName, Bitmap frame) {

        if (fileName == null || fileName.length() <= 0)
            return false;

        boolean bErr = false;
        FileOutputStream fos = null;

        try {

            // 灏嗗浘鐗囦繚瀛樺埌filename鏂囦欢
            fos = new FileOutputStream(fileName, false);
            frame.compress(Bitmap.CompressFormat.JPEG, 10, fos);//杩涜鍘嬬缉鐒跺悗鍐欏叆鍒版枃浠�
            fos.flush();
            fos.close();

        } catch (Exception e) {

            bErr = true;
            System.out.println("saveImage(.): " + e.getMessage());

        } finally {

            if (bErr) {

                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        }
        // addImageGallery(new File(fileName));
        return true;
    }

    void saveSnap() {
        if (mCamera != null) {
            if (isSDCardValid()) {// 濡傛灉sd鍗″彲鐢�
                if (!TwsTools.checkPermission(PlaybackActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                String filenameString = mDevUID + "_" + mEvtType + mEvtTime2.year + mEvtTime2.month + mEvtTime2.day + mEvtTime2.wday + mEvtTime2.hour + mEvtTime2.minute + mEvtTime2.second + ".jpg";
                mCamera.saveSnapShot(mPlaybackChannel, TwsDataValue.Remte_RECORDING_DIR, filenameString, null);
            }
        }
    }

    @Override
    public void receiveFrameData(final NSCamera camera, int sessionChannel, Bitmap bmp) {
        System.out.println("receiveFrameData");
        if (mCamera == camera && sessionChannel == mPlaybackChannel && bmp != null) {
            mVideoWidth = bmp.getWidth();
            mVideoHeight = bmp.getHeight();
            if (snap == null && bmp != null && mEvtTime2 != null) {
                snap = bmp;
                saveSnap();
            }
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    videoProgressBar().setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    @Override
    public void receiveSessionInfo(final NSCamera camera, int resultCode) {
        Bundle bundle = new Bundle();
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = resultCode;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(final NSCamera camera, int sessionChannel, int resultCode) {

        if (mCamera == camera) {
            Bundle bundle = new Bundle();
            Message msg = handler.obtainMessage();
            msg.what = TwsDataValue.HANDLE_MESSAGE_CHANNEL_STATE;
            msg.arg1 = resultCode;
            msg.arg2 = sessionChannel;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveFrameInfo(final NSCamera camera, int sessionChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

        if (mCamera == camera && sessionChannel == mPlaybackChannel) {
            Bundle bundle = new Bundle();
            bundle.putInt("sessionChannel", sessionChannel);
            bundle.putInt("videoFPS", frameRate);
            bundle.putLong("videoBPS", bitRate);
            bundle.putInt("frameCount", frameCount);
            bundle.putInt("inCompleteFrameCount", incompleteFrameCount);

            Message msg = handler.obtainMessage();
            msg.what = STS_CHANGE_CHANNEL_STREAMINFO;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void receiveIOCtrlData(final NSCamera camera, int sessionChannel, int avIOCtrlMsgType, byte[] data) {

        if (mCamera == camera) {
            Bundle bundle = new Bundle();
            bundle.putInt("sessionChannel", sessionChannel);
            bundle.putByteArray("data", data);

            Message msg = new Message();
            msg.what = avIOCtrlMsgType;
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            if (msg.what == PLAY_TIMEOUT) {

                if (mPlaybackChannel < 0 && mMediaState == MEDIA_STATE_OPENING) {
//                    mMediaState = MEDIA_STATE_STOPPED;
//                    showAlert(getString(R.string.alert_play_record_timeout));
                    startPlayBack();
                }

//                initView();
            } else if (msg.what == STS_CHANGE_CHANNEL_STREAMINFO) {

                int videoFPS = bundle.getInt("videoFPS");
                long videoBPS = bundle.getLong("videoBPS");
                int frameCount = bundle.getInt("frameCount");
                int inCompleteFrameCount = bundle.getInt("inCompleteFrameCount");
                if (videoBPS < 1) {
                    if (mMediaState == MEDIA_STATE_PLAYING) {
                        videoProgressBar().setVisibility(View.VISIBLE);
                    }
                } else {
                    videoProgressBar().setVisibility(View.INVISIBLE);
                }


            } else if (msg.what == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL_RESP) {

                int command = Packet.byteArrayToInt_Little(data, 0);
                int result = Packet.byteArrayToInt_Little(data, 4);

                switch (command) {

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START:

                        System.out.println("AVIOCTRL_RECORD_PLAY_START");

                        if (mMediaState == MEDIA_STATE_OPENING) {
                            if (0 <= result && result <= 31) {

                                mPlaybackChannel = result;
                                mMediaState = MEDIA_STATE_PLAYING;

                                if (mCamera != null) {
                                    mCamera.startChannel(mPlaybackChannel, mCamera.user, mCamera.getPassword());
                                }
                                refreshButton();
                                handler.removeMessages(PLAY_TIMEOUT);
                            } else {

                                //showAlert(getString(R.string.alert_play_record_failed));
                            }
                        }

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_PAUSE:

                        System.out.println("AVIOCTRL_RECORD_PLAY_PAUSE");

                        if (mPlaybackChannel >= 0 && mCamera != null) {

                            if (mMediaState == MEDIA_STATE_PAUSED)
                                mMediaState = MEDIA_STATE_PLAYING;
                            else if (mMediaState == MEDIA_STATE_PLAYING)
                                mMediaState = MEDIA_STATE_PAUSED;

                            if (mMediaState == MEDIA_STATE_PAUSED) {
                                // mCamera.pausePlay(mPlaybackChannel);
                                monitor.deattachCamera();
                            } else {
                                //mCamera.resumePlay(mPlaybackChannel);
                                monitor.attachCamera(mCamera, mPlaybackChannel);
                            }
                            refreshButton();
                        }

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP:

                        System.out.println("AVIOCTRL_RECORD_PLAY_STOP");

                        if (mPlaybackChannel >= 0 && mCamera != null) {
                            mCamera.stop(mPlaybackChannel);
                            //mCamera.pausePlay(mPlaybackChannel);
                            monitor.deattachCamera();
                        }

                        mPlaybackChannel = -1;
                        mMediaState = MEDIA_STATE_STOPPED;

                        refreshButton();

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_END:

                        System.out.println("AVIOCTRL_RECORD_PLAY_END");

                        if (mPlaybackChannel >= 0 && mCamera != null) {
                            mCamera.stop(mPlaybackChannel);
                            // mCamera.pausePlay(mPlaybackChannel);
                            monitor.deattachCamera();

                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0, mEvtTime2.toByteArray()));
                        }

                        Toast.makeText(PlaybackActivity.this, getText(R.string.tips_play_record_end), Toast.LENGTH_LONG).show();


                        mPlaybackChannel = -1;
                        mMediaState = MEDIA_STATE_STOPPED;
                        refreshButton();
                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_BACKWARD:

                        break;

                    case AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_FORWARD:

                        break;
                }
            } else if (msg.what == TwsDataValue.HANDLE_MESSAGE_CHANNEL_STATE) {
                int requestCode = msg.arg1;
                int channel = msg.arg2;
                if (requestCode == NSCamera.CONNECTION_STATE_TIMEOUT) {
                    mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_STOP, 0, mEvtTime2.toByteArray()));
                    // mCamera.stop(channel);
                    TwsToast.showToast(PlaybackActivity.this, "timeout");
                    mCamera.asyncStopChannel(channel, new MyCamera.TaskExecute() {
                        @Override
                        public void onPosted(Object data) {
                            mPlaybackChannel = -1;
                            mMediaState = MEDIA_STATE_STOPPED;
                            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL, AVIOCTRLDEFs.SMsgAVIoctrlPlayRecord.parseContent(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.AVIOCTRL_RECORD_PLAY_START, 0, mEvtTime2.toByteArray()));
                        }
                    });
                }
            } else if (msg.what == TwsDataValue.HANDLE_MESSAGE_SESSION_STATE) {

                int requestCode = msg.arg1;
                if (requestCode == NSCamera.CONNECTION_STATE_CONNECTED) {
                    if (mPlaybackChannel >= 0) {
                        mCamera.startShow(mPlaybackChannel);
                        mCamera.startListening(mPlaybackChannel);
                        // mCamera.resumePlay(mPlaybackChannel);
                        monitor.attachCamera(mCamera, mPlaybackChannel);
                    }
                } else if (requestCode == NSCamera.CONNECTION_STATE_TIMEOUT) {
                    disConnect();
                    startPlayBack();
                }
            } else if (msg.what == DISCONNECT_DELAY) {
                disConnect();
            }
            super.handleMessage(msg);
        }
    };

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
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.monitor) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    //HiLog.v("MotionEvent.ACTION_DOWN");

                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    //	HiLog.v("MotionEvent.ACTION_UP");
                    setViewVisible(!this.toolsVisible);
                    break;
                }
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public void callbackState(Camera camera, int channel, int state, int w, int h) {
        if (channel == mPlaybackChannel) {
            if (state == 0) {
                mVideoWidth = w;
                mVideoHeight = h;
                if (snap == null && mEvtTime2 != null) {
                    saveSnap();
                }
                if (w != 0 && h != 0 &&Math.abs(this.mCamera.getVideoRatio(PlaybackActivity.this) - w / h) > 0.2) {
                    this.mCamera.setVideoRatio(PlaybackActivity.this, (float) w / h);
                    if(monitor != null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                monitor.resizeVideoWrapper(PlaybackActivity.this.mCamera);
                            }
                        });
                    }
                }
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoProgressBar().setVisibility(View.INVISIBLE);
                    }
                });

            }
        }
    }

    @Override
    public void callbackPlayUTC(Camera var1, int var2) {

    }

    private void performAnim2(final View view, int oringhHeight, int newHeight) {
        //View是否显示的标志
        //属性动画对象
        ValueAnimator va;
        //显示view，高度从oringhHeight变到newHeight值
        va = ValueAnimator.ofInt(oringhHeight, newHeight);
        //va = ValueAnimator.ofInt(newHeight);

        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h = (Integer) valueAnimator.getAnimatedValue();
                //动态更新view的高度
                view.getLayoutParams().height = h;
                view.requestLayout();
            }
        });
        va.setDuration(100);
        //开始动画
        va.start();
    }

}