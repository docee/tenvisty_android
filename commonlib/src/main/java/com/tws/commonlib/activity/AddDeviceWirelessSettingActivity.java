package com.tws.commonlib.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.CameraClient;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.GifView;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.controller.widget.SaundProgressBar;
import com.tws.commonlib.wificonfig.BaseConfig;
import com.tws.commonlib.wificonfig.WiFiConfigureContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

public class AddDeviceWirelessSettingActivity extends BaseActivity implements IRegisterIOTCListener {
    private int timeout = 60;
    private int percent = 0;
    private MyCamera camera;
    private boolean succeeded = false;
    private final int CONFIG_WIFI_SUCCESS = 0;
    private final int CONFIG_WIFI_FAIL = 1;
    private final int CONFIG_WIFI_WRONG_PWD = 2;
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
        wifiConfiger = new WiFiConfigureContext();
        this.setTitle(getString(R.string.title_add_camera));
        initView();
        percent = 0;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {//此方法在ui线程运行
            config_result = msg.what;
            switch (msg.what) {
                case CONFIG_WIFI_SUCCESS:
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
            }
        }
    };
    private Handler handler = new Handler();

    private Runnable task = new Runnable() {
        public void run() {
            long nowTime = Calendar.getInstance(TimeZone.getTimeZone("gmt")).getTimeInMillis();
            long waitTime = (nowTime - startTime) / 1000;
            if (waitTime >= timeout) {
                if (times > 40) {
                    times -= 10;
                }
            }
            if (percent < regularprogressbar.getMax()) {
                percent++;
                regularprogressbar.setProgress(percent);
                handler.postDelayed(this, times * 10);//设置延迟时间，此处是1秒
            } else {
                wifiConfiger.stopConfig();
                if (MyCamera.NO_USE_UID.equalsIgnoreCase(dev_uid)) {
                    //没有扫二维码，则提示用户是否依然听到摄像机叮咚声音
                    showAlertnew(android.R.drawable.ic_dialog_alert, null, getString(R.string.dialog_msg_onekey_configwifi_soundstop), getString(R.string.no), getString(R.string.yes),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            showAlertnew(android.R.drawable.ic_dialog_alert, null, getString(R.string.dialog_msg_onekey_configwifi_fail), getString(R.string.title_userhelp), getString(R.string.dialog_btn_retry),
                                                    new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            switch (which) {
                                                                case DialogInterface.BUTTON_POSITIVE:
                                                                    AddDeviceWirelessSettingActivity.this.finish();
                                                                    break;
                                                                case DialogInterface.BUTTON_NEGATIVE:
                                                                    go2Help();
                                                                    break;
                                                            }

                                                        }
                                                    });
                                            break;
                                        case DialogInterface.BUTTON_POSITIVE:
                                            AddDeviceWirelessSettingActivity.this.finish();
                                            back2Search();
                                            TwsToast.showToast(AddDeviceWirelessSettingActivity.this, getString(R.string.toast_search_in_lan));
                                            break;
                                    }

                                }
                            });
                } else {
                    if (config_result == CONFIG_WIFI_SUCCESS) {
                        back2List();
                    } else if (config_result == CONFIG_WIFI_WRONG_PWD) {
                        back2List();
                    } else {
                        if (camera != null) {
                            camera.asyncStop(null);
                            camera.unregisterIOTCListener(AddDeviceWirelessSettingActivity.this);
                        }
                        showAlertnew(android.R.drawable.ic_dialog_alert, null, getString(R.string.dialog_msg_onekey_configwifi_fail), getString(R.string.title_userhelp), getString(R.string.dialog_btn_retry),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                AddDeviceWirelessSettingActivity.this.finish();
                                                break;
                                            case DialogInterface.BUTTON_NEGATIVE:
                                                go2Help();
                                                break;
                                        }

                                    }
                                });
                    }
                }
            }
        }
    };

    @Override
    protected void initView() {
        super.initView();
        Bundle extras = this.getIntent().getExtras();
        dev_uid = extras.getString(TwsDataValue.EXTRA_KEY_UID);
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
        if (MyConfig.getWirelessInstallHelpUrl() != null) {
            final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
            title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
            title.setRightBtnBackground(android.R.drawable.ic_menu_help);
            title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

                @Override
                public void OnNavigationButtonClick(int which) {
                    switch (which) {
                        case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                            go2Help();
                            break;
                    }
                }
            });
        }
    }

    private void go2Help() {
        Intent intent = new Intent();
        intent.setClass(this, WebBrowserActivity.class);
        String title = this.getString(R.string.title_userhelp);
        String url = MyConfig.getWirelessInstallHelpUrl();
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        this.finish();
        startActivity(intent);
    }

    public void setWifi() throws Exception {
        addSuccess = false;
        startTime = Calendar.getInstance(TimeZone.getTimeZone("gmt")).getTimeInMillis();
        wifiConfiger.startConfig();
        wifiConfiger.setReceiveListner(new BaseConfig.OnReceivedListener() {
            @Override
            public void OnReceived(final String status, final String ip, final String UID) {
                if (!addSuccess && dev_uid.equalsIgnoreCase(MyCamera.NO_USE_UID) || dev_uid.equalsIgnoreCase(UID)) {
                    addSuccess = true;
                    wifiConfiger.setReceiveListner(null);

                    if (camera == null) {
                        dev_uid = UID;
                        camera = new MyCamera(getText(R.string.camera).toString(), dev_uid, "admin", "admin");
                        camera.cameraModel = NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264;
                    }

                    mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
                }
            }
        });
        //SmartLink.GetInstance().RunConfig();
        if (!MyCamera.NO_USE_UID.equalsIgnoreCase(dev_uid)) {
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
        if (camera != null && camera.uid != null && !camera.uid.equalsIgnoreCase(MyCamera.NO_USE_UID)) {
            camera.unregisterIOTCListener(this);
            if (!camera.isExist()) {
                camera.asyncStop(null);
            }
        }
    }

    @Override
    public void receiveSessionInfo(final NSCamera camera, final int resultCode) {
        if (resultCode == NSCamera.CONNECTION_STATE_CONNECTED) {//如果判定输入的摄像头密码和用户正确则添加
            if (percent < 100) {
                mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_WRONG_PASSWORD) {//连接摄像机的时候发现密码错误，弹出相应提示框
            if (percent < 100) {
                mHandler.obtainMessage(CONFIG_WIFI_WRONG_PWD).sendToTarget();
            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_SLEEPING) {//连接摄像机的时候发现密码错误，弹出相应提示框
            if (percent < 100) {
                mHandler.obtainMessage(CONFIG_WIFI_SUCCESS).sendToTarget();
            }
        } else if (resultCode == NSCamera.CONNECTION_STATE_CONNECTING) {

        } else {
            if (percent < 100) {
                ((MyCamera) camera).asyncStop(new MyCamera.TaskExecute() {
                    @Override
                    public void onPosted(Object data) {
                        camera.start();
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
    public void receiveFrameData(NSCamera camera, int avChannel, Bitmap bmp) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveFrameInfo(NSCamera camera, int avChannel, long bitRate,
                                 int frameRate, int onlineNm, int frameCount,
                                 int incompleteFrameCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveChannelInfo(NSCamera camera, int avChannel,
                                   int resultCode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void receiveIOCtrlData(NSCamera camera, int avChannel,
                                  int avIOCtrlMsgType, byte[] data) {
        // TODO Auto-generated method stub

    }


    private void save() {
        /**
         * 判断是否已经添加过该摄像机
         */
        boolean duplicated = false;
        for (NSCamera camera_ : TwsDataValue.cameraList()) {

            if (camera.uid.equalsIgnoreCase(camera_.uid)) {
                duplicated = true;
                break;
            }
        }

        if (duplicated) {
            //MyCamera.init();
            //showAlert(getText(R.string.alert_camera_exist));
            return;
        }

		/* add value to server data base */

        camera.save(AddDeviceWirelessSettingActivity.this);

    }

    void connectCamera() {
        /* add value to server data base */
        camera = new MyCamera(getText(R.string.camera).toString(), dev_uid, "admin", "admin");
        camera.cameraModel = NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264;
        //看是否能正确启动摄像机
        camera.registerIOTCListener(this);
        camera.start();
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
}
