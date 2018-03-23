package com.tws.commonlib.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;

import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.controller.widget.SaundProgressBar;
import com.tws.commonlib.wificonfig.BaseConfig;
import com.tws.commonlib.wificonfig.WiFiConfigureContext;

import java.util.Calendar;
import java.util.TimeZone;

public class AddDeviceWirelessSettingActivity extends BaseActivity implements IIOTCListener {
    private int percent = 0;
    private IMyCamera camera;
    private boolean succeeded = false;
    private final int CONFIG_WIFI_SUCCESS = 0;
    private final int CONFIG_WIFI_FAIL = 1;
    private final int CONFIG_WIFI_WRONG_PWD = 2;
    private final int CONFIG_WIFI_RECONNECT = 3;
    private boolean addSuccess = false;
    private long startTime;
    private int times = 100;
    private String dev_uid;
    private int config_result = -1;
    WiFiConfigureContext wifiConfiger;
    SaundProgressBar regularprogressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.activity_add_device_wireless_setting);
        this.setTitle(getString(R.string.title_add_camera));
        initView();
        percent = 0;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            config_result = msg.what;
            switch (msg.what) {
                case CONFIG_WIFI_SUCCESS:
                    if (mHandler.hasMessages(CONFIG_WIFI_RECONNECT)) {
                        mHandler.removeMessages(CONFIG_WIFI_RECONNECT);
                    }
                    wifiConfiger.stopConfig();
                    if (!camera.isExist()) {
                        save();
                    }
                    if (percent < 90) {
                        percent = 92;
                    }
                    times = 1;
                    break;
                case CONFIG_WIFI_WRONG_PWD:
                    if (mHandler.hasMessages(CONFIG_WIFI_RECONNECT)) {
                        mHandler.removeMessages(CONFIG_WIFI_RECONNECT);
                    }
                    wifiConfiger.stopConfig();
                    times = 1;

                    if (!camera.isExist()) {
                        save();
                    }
                    if (percent < 90) {
                        percent = 92;
                    }
                    times = 1;
                    break;
                case CONFIG_WIFI_FAIL:

                    break;
                case CONFIG_WIFI_RECONNECT:
                    if (percent < 100) {
                        camera.start();
                    }
                    break;
            }
        }
    };
    private Handler handler = new Handler();

    private Runnable task = new Runnable() {
        public void run() {
            long nowTime = Calendar.getInstance(TimeZone.getTimeZone("gmt")).getTimeInMillis();
            if (percent < regularprogressbar.getMax()) {
                percent++;
                regularprogressbar.setProgress(percent);
                handler.postDelayed(this, times * 10);//设置延迟时间，此处是1秒
            } else {
                if (mHandler.hasMessages(CONFIG_WIFI_RECONNECT)) {
                    mHandler.removeMessages(CONFIG_WIFI_RECONNECT);
                }
                wifiConfiger.stopConfig();
                if (config_result == CONFIG_WIFI_SUCCESS) {
                    back2List();
                } else if (config_result == CONFIG_WIFI_WRONG_PWD) {
                    back2List();
                } else {
                    if (camera != null) {
                        camera.asyncStop(null);
                        camera.unregisterIOTCListener(AddDeviceWirelessSettingActivity.this);
                    }
                    percent = 0;
                    Intent intent = AddDeviceWirelessSettingActivity.this.getIntent();
                    intent.setClass(AddDeviceWirelessSettingActivity.this, AddDeviceWirelessSettingFailActivity.class);
                    startActivity(intent);
                }
            }
        }
    };

    @Override
    protected void initView() {
        super.initView();
        Bundle extras = this.getIntent().getExtras();
        dev_uid = extras.getString(TwsDataValue.EXTRA_KEY_UID);
        if (dev_uid.length() == 20) {
            wifiConfiger = new WiFiConfigureContext(AddDeviceWirelessSettingActivity.this, WiFiConfigureContext.VOICE_TYPE_FACEBER);
        } else {
            wifiConfiger = new WiFiConfigureContext(AddDeviceWirelessSettingActivity.this, WiFiConfigureContext.VOICE_TYPE_HICHIP);
        }
        wifiConfiger.setData(extras.getString("ssid"), extras.getString("password"), extras.getInt("authMode"));
        wifiConfiger.setUid(dev_uid);
        regularprogressbar = (SaundProgressBar) this.findViewById(R.id.regularprogressbar);
        regularprogressbar.setMax(100);
        Drawable indicator = getResources().getDrawable(
                R.drawable.progress_indicator);
        Rect bounds = new Rect(0, 0, indicator.getIntrinsicWidth() + 5,
                indicator.getIntrinsicHeight() + 5);
        indicator.setBounds(bounds);

        regularprogressbar.setProgressIndicator(indicator);
        regularprogressbar.setProgress(0);
        regularprogressbar.setVisibility(View.VISIBLE);

    }

    public void setWifi() throws Exception {
        addSuccess = false;
        startTime = Calendar.getInstance(TimeZone.getTimeZone("gmt")).getTimeInMillis();
        wifiConfiger.startConfig();
        wifiConfiger.setReceiveListner(new BaseConfig.OnReceivedListener() {
            @Override
            public void OnReceived(final String status, final String ip, final String UID) {
                if (!addSuccess && dev_uid.equalsIgnoreCase(UID)) {
                    addSuccess = true;
                    wifiConfiger.setReceiveListner(null);

                    if (camera == null) {
                        dev_uid = UID;
                        camera = IMyCamera.MyCameraFactory.shareInstance().createCamera(getText(R.string.hint_input_camera_name).toString(), dev_uid, "admin", "admin");
                        camera.setCameraModel(NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264.ordinal());
                    }

                    mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
                }
            }
        });
        //SmartLink.GetInstance().RunConfig();
        if (!IMyCamera.NO_USE_UID.equalsIgnoreCase(dev_uid)) {
            connectCamera();
        }
    }

    public void back2List() {
        refreshList();
        back2Activity(MainActivity.class);
    }

    /**
     * 返回到搜索摄像机的界面
     */
    public void back() {
        finish();
    }

    void back2Search() {
        finish();
        back2Activity(SearchCameraActivity.class);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(task);
        wifiConfiger.clearReceiveListner();
        if (mHandler.hasMessages(CONFIG_WIFI_RECONNECT)) {
            mHandler.removeMessages(CONFIG_WIFI_RECONNECT);
        }
        wifiConfiger.stopConfig();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            setWifi();
            handler.post(task);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null && camera.getUid() != null && !camera.getUid().equalsIgnoreCase(IMyCamera.NO_USE_UID)) {
            camera.unregisterIOTCListener(this);
            if (!camera.isExist()) {
                camera.asyncStop(null);
            }
        }
    }


    private void save() {
        /* add value to server data base */

        camera.save(AddDeviceWirelessSettingActivity.this);

    }

    void connectCamera() {
        /* add value to server data base */
        camera = IMyCamera.MyCameraFactory.shareInstance().createCamera(getText(R.string.hint_input_camera_name).toString(), dev_uid, "admin", "admin");
        camera.setCameraModel(NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264.ordinal());
        //看是否能正确启动摄像机
        camera.registerIOTCListener(this);
        camera.connect();
    }


    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(final IMyCamera camera, int resultCode) {
        if (resultCode == NSCamera.CONNECTION_STATE_CONNECTED) {//如果判定输入的摄像头密码和用户正确则添加
            if (percent < 100) {
                mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_WRONG_PASSWORD) {//连接摄像机的时候发现密码错误，弹出相应提示框
            if (percent < 100) {
                mHandler.obtainMessage(CONFIG_WIFI_WRONG_PWD).sendToTarget();
            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_FIND_DEVICE) {
            if (percent < 100) {
                mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_SLEEPING) {//连接摄像机的时候发现密码错误，弹出相应提示框
//            if (percent < 100) {
//                mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
//            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_CONNECTING) {

        } else {
            if (percent < 100) {
                camera.asyncStop(new IMyCamera.TaskExecute() {
                    @Override
                    public void onPosted(IMyCamera c, Object data) {
                        mHandler.sendEmptyMessageDelayed(CONFIG_WIFI_RECONNECT, 5000);
                    }
                });
            }
        }
//        this.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//            }
//        });
    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {

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
}
