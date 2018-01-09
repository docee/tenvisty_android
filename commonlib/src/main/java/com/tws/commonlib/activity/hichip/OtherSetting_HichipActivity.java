package com.tws.commonlib.activity.hichip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ToggleButton;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.setting.DeviceInfoActivity;
import com.tws.commonlib.activity.setting.SdCardSettingActivity;
import com.tws.commonlib.activity.setting.TimeSettingActivity;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class OtherSetting_HichipActivity extends BaseActivity implements IIOTCListener,View.OnClickListener {

    private String dev_uid;
    private HichipCamera camera;
    ToggleButton togbtn_reverse;
    ToggleButton togbtn_inverse;
    HiChipDefines.HI_P2P_S_DISPLAY display_param = null;
    //ToggleButton togbtn_alarm_led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_other_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
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
        togbtn_reverse.setEnabled(false);
        togbtn_inverse.setEnabled(false);
        //togbtn_alarm_led.setOnClickListener(this);
    }

//    void setAlarmLed() {
//        showLoadingProgress();
//        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_REQ, AVIOCTRLDEFs.SMsgAVIOCtrlSetAlarmLedReq.parseContent(togbtn_alarm_led.isChecked() ? 1 : 0));
//    }

    void getSetting() {
        showLoadingProgress();
        if (camera != null) {
            showLoadingView(R.id.togbtn_reverse);
            showLoadingView(R.id.togbtn_inverse);
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_DISPLAY_PARAM, null);
        }
    }


    void setVideoMode() {
        if(display_param != null) {
            display_param.u32Mirror = togbtn_reverse.isChecked() ? 1 : 0;
            display_param.u32Flip = togbtn_inverse.isChecked() ? 1 : 0;
            camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_DISPLAY_PARAM, display_param.parseContent());
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

                case HiChipDefines.HI_P2P_GET_DISPLAY_PARAM://视频翻转
                    dismissLoadingProgress();

                    display_param = new HiChipDefines.HI_P2P_S_DISPLAY(data);

                    togbtn_reverse.setChecked(display_param.u32Mirror > 0);
                    togbtn_inverse.setChecked(display_param.u32Flip > 0);

                    togbtn_reverse.setEnabled(true);
                    togbtn_inverse.setEnabled(true);
                    hideLoadingView(R.id.togbtn_reverse);
                    hideLoadingView(R.id.togbtn_inverse);
                    break;
                case HiChipDefines.HI_P2P_SET_DISPLAY_PARAM://视频翻转
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
            intent.setClass(this, TimeSetting_HichipActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setTime));
        } else if (view.getId() == R.id.ll_setSDCard) {
            intent.setClass(this, SdCardSetting_HichipActivity.class);
            startActivityForResult(intent,getRequestCode(R.id.ll_setSDCard));

        } else if (view.getId() == R.id.ll_setDeviceInfo) {
            intent.setClass(this, DeviceInfo_HichipActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setDeviceInfo));
        }
        else if(view.getId() == R.id.ll_setAudio){
            intent.setClass(this, AudioSetting_HichipActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setAudio));
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
