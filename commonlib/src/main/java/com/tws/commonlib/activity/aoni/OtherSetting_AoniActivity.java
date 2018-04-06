package com.tws.commonlib.activity.aoni;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ToggleButton;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.setting.DeviceInfoActivity;
import com.tws.commonlib.activity.setting.SdCardSettingActivity;
import com.tws.commonlib.activity.setting.TimeSettingActivity;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class OtherSetting_AoniActivity extends BaseActivity implements IIOTCListener, View.OnClickListener {

    private String dev_uid;
    private IMyCamera camera;
    ToggleButton togbtn_ledlight;
    //ToggleButton togbtn_alarm_led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_other_setting_aoni);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
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
        togbtn_ledlight = (ToggleButton) findViewById(R.id.togbtn_ledlight);
        togbtn_ledlight.setOnClickListener(this);
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
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_REQ, AVIOCTRLDEFs.SMsgAVIOCtrlGetAlarmLedReq.parseContent(0));
            // camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlTimeZone.parseContent());
            //camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ENVIRONMENT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetEnvironmentReq.parseContent(0));
        }
    }

    void setInverse(boolean inverse) {
        showLoadingProgress();
    }

    public void doClickLL(View view) {

    }


    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {

    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
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


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            dismissLoadingProgress();
            switch (msg.what) {
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_RESP: {
                    int led =Packet.byteArrayToInt_Little(data,0);
                    togbtn_ledlight.setChecked(led == 1);
                    hideLoadingView(R.id.togbtn_ledlight);
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ://视频翻转
                    dismissLoadingProgress();
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_RESP:
                    dismissLoadingProgress();
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_OSD_ONOFF_RESP:
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
            intent.setClass(this, TimeSetting_AoniActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setTime));
        } else if (view.getId() == R.id.ll_setSDCard) {
            intent.setClass(this, SdCardSetting_AoniActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setSDCard));

        } else if (view.getId() == R.id.ll_setDeviceInfo) {
            intent.setClass(this, DeviceInfoActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setDeviceInfo));
        }
    }

    private void setLedLight(boolean on) {
        showLoadingProgress();
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_REQ, AVIOCTRLDEFs.SMsgAVIOCtrlSetAlarmLedReq.parseContent(on ? 1 : 0));
    }

    private void setOsdTime(boolean on) {
        showLoadingProgress();
        byte[] arrayOfByte = new byte[8];
        System.arraycopy(Packet.intToByteArray_Little(1), 0, arrayOfByte, 0, 4);
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_OSD_ONOFF_REQ,arrayOfByte);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.togbtn_ledlight) {
            setLedLight(((ToggleButton) view).isChecked());
        }
//        else if(view.getId() == R.id.togbtn_alarm_led){
//            setAlarmLed();
//        }
    }
}
