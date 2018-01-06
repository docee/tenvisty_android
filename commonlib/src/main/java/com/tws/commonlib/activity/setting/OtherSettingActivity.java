package com.tws.commonlib.activity.setting;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class OtherSettingActivity extends BaseActivity implements IRegisterIOTCListener,View.OnClickListener {

    private String dev_uid;
    private MyCamera camera;
    ToggleButton togbtn_reverse;
    ToggleButton togbtn_inverse;
    //ToggleButton togbtn_alarm_led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_other_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (NSCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.uid.equalsIgnoreCase(dev_uid)) {
                camera = (MyCamera) _camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_camera_setting_other));
        initView();
        getSetting();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        togbtn_reverse = (ToggleButton) findViewById(R.id.togbtn_reverse);
        togbtn_inverse = (ToggleButton) findViewById(R.id.togbtn_inverse);
        //togbtn_alarm_led = (ToggleButton) findViewById(R.id.togbtn_alarm_led);
        togbtn_inverse.setOnClickListener(this);
        togbtn_reverse.setOnClickListener(this);
        //togbtn_alarm_led.setOnClickListener(this);
    }

//    void setAlarmLed() {
//        showLoadingProgress();
//        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_REQ, AVIOCTRLDEFs.SMsgAVIOCtrlSetAlarmLedReq.parseContent(togbtn_alarm_led.isChecked() ? 1 : 0));
//    }

    void getSetting() {
        showLoadingProgress();
        if (camera != null) {
            showLoadingView(R.id.txt_timezone);
            showLoadingView(R.id.togbtn_reverse);
            showLoadingView(R.id.togbtn_inverse);
           // showLoadingView(R.id.togbtn_alarm_led);

            //camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_REQ, AVIOCTRLDEFs.SMsgAVIOCtrlGetAlarmLedReq.parseContent(0));
           // camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_VIDEOMODE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetVideoModeReq.parseContent(0));
            //camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ENVIRONMENT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetEnvironmentReq.parseContent(0));
        }
    }


    void setVideoMode() {
        // showLoadingProgress();
        int videoMode = (togbtn_inverse.isChecked() ? 1 : 0) + (togbtn_reverse.isChecked() ? 2 : 0);
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetVideoModeReq.parseContent(0, (byte) videoMode));
    }

    void setInverse(boolean inverse) {
        showLoadingProgress();
    }

    public void doClickLL(View view) {

    }


    @Override
    public void receiveFrameData(NSCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(NSCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(NSCamera camera, int resultCode) {

    }

    @Override
    public void receiveChannelInfo(NSCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(NSCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void initSendAudio(Camera paramCamera, boolean paramBoolean) {

    }

    @Override
    public void receiveOriginalFrameData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {

    }

    @Override
    public void receiveRGBData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {

    }

    @Override
    public void receiveRecordingData(Camera paramCamera, int avChannel, int paramInt1, String path) {

    }


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            dismissLoadingProgress();
            switch (msg.what) {

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_VIDEOMODE_RESP://视频翻转
                    dismissLoadingProgress();

                    int videoMode = data[4];

                    togbtn_reverse.setChecked((videoMode & 0x2) > 0);
                    togbtn_inverse.setChecked((videoMode & 0x1) > 0);

                    hideLoadingView(R.id.togbtn_reverse);
                    hideLoadingView(R.id.togbtn_inverse);
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ://视频翻转
                    dismissLoadingProgress();

                    break;
//                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_RESP://时区
//                    dismissLoadingProgress();
//                    AVIOCTRLDEFs.SMsgAVIoctrlTimeZone timeZone = new AVIOCTRLDEFs.SMsgAVIoctrlTimeZone(data);
//                    String strTimeZone = new String(timeZone.szTimeZoneString).trim();
//                    txt_timezone.setText(strTimeZone);
//                    hideLoadingView(R.id.txt_timezone);
//                    break;
//                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_RESP://视频翻转
//                    dismissLoadingProgress();
//                    break;
//                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_RESP:
//
//                    int alarmLed = data[0];
//                    togbtn_alarm_led.setChecked(alarmLed == 1);
//                    hideLoadingView(R.id.togbtn_alarm_led);
//                    break;
//                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_OSD_ONOFF_RESP://视频翻转
//                    dismissLoadingProgress();
//                    break;

            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }

    public void goSetting(View view) {
        Intent intent = new Intent();
        intent.putExtras(this.getIntent());
        if (view.getId() == R.id.ll_setTime) {
            intent.setClass(this, TimeSettingActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setTime));
        } else if (view.getId() == R.id.ll_setSDCard) {
            intent.setClass(this, SdCardSettingActivity.class);
            startActivityForResult(intent,getRequestCode(R.id.ll_setSDCard));

        } else if (view.getId() == R.id.ll_setDeviceInfo) {
            intent.setClass(this, DeviceInfoActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setDeviceInfo));
        }
    }



    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.togbtn_inverse || view.getId() == R.id.togbtn_reverse){
            setVideoMode();
        }
//        else if(view.getId() == R.id.togbtn_alarm_led){
//            setAlarmLed();
//        }
    }
}
