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
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.hichip.EventSetting_HichipActivity;
import com.tws.commonlib.base.CameraClient;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import org.json.JSONObject;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class EventSettingActivity extends BaseActivity implements IIOTCListener {
    private static final int SENSITIVITY_SET = 0xA1;
    private String dev_uid;
    ToggleButton togbtn_push;
    boolean hasPIR;
    int sensLevel;
    String[] sensLevelList;
    TextView txt_sens;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_RESP: {
                    int pirSensitivity = Packet.byteArrayToInt_Little(data, 4);
                    hasPIR = true;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP:
                    dismissLoadingProgress();
                    int sensitivity = Packet.byteArrayToInt_Little(data, 4);
                    for(int i=0;i<TwsDataValue.SensValues.length;i++){
                        if(sensitivity >= TwsDataValue.SensValues[i]){
                            sensLevel = i;
                            break;
                        }
                    }
                    txt_sens.setText(sensLevelList[sensLevel]);
                    break;


            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }
        //低功耗设备不支持邮箱报警
        //  if(camera.wakeUp()>0){
        //findViewById(R.id.ll_setMail).setVisibility(View.GONE);
        // }
        txt_sens = (TextView) findViewById(R.id.txt_sens);
        sensLevelList = getResources().getStringArray(R.array.motion_detection_sensitivity);
        this.setTitle(getResources().getString(R.string.title_camera_setting_event));
        initView();
        camera.registerIOTCListener(this);
        searchSensitivity();
    }

    @Override
    protected void initView() {
        super.initView();
        togbtn_push = (ToggleButton) findViewById(R.id.togbtn_push);
        togbtn_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPush(togbtn_push.isChecked());
            }
        });
        togbtn_push.setChecked(camera.isPushOpen());
    }

    public void goSetting(View view) {
        Intent intent = new Intent();
        intent.putExtras(this.getIntent());
        int requestCode = 0;
//        if (view.getId() == R.id.ll_setMail) {
//            intent.setClass(this, MailSettingActivity.class);
//        }
//        else if (view.getId() == R.id.ll_setRecord) {
//            intent.setClass(this, RecordSettingActivity.class);
//
//        }
        if (view.getId() == R.id.ll_setSensitivity) {
            requestCode = SENSITIVITY_SET;
            intent.setClass(this, SensitivitySettingActivity.class);
        }
        startActivityForResult(intent, requestCode);
        //startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SENSITIVITY_SET) {
            if (resultCode == SensitivitySettingActivity.SENSITIVITY_SET_SUCC) {
                int level = data.getIntExtra("data", 0);
                txt_sens.setText(sensLevelList[4 - level]);
            }
        }

    }

    void setPush(boolean push) {
        if (push) {
            final CameraClient.ServerResultListener2 succListener =   new CameraClient.ServerResultListener2() {
                @Override
                public void serverResult(String resultString, JSONObject jsonArray) {
                    if (EventSettingActivity.this != null && !EventSettingActivity.this.isFinishing()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgress();
                                TwsToast.showToast(EventSettingActivity.this, getString(R.string.tips_setting_succ));
                            }
                        });
                    }
                }
            };
            final CameraClient.ServerResultListener2 failListener =  new CameraClient.ServerResultListener2() {
                @Override
                public void serverResult(String resultString, JSONObject jsonArray) {
                    if (EventSettingActivity.this != null && !EventSettingActivity.this.isFinishing()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgress();
                                togbtn_push.setChecked(false);
                                showAlert(getString(R.string.alert_setting_fail));
                            }
                        });
                    }
                }
            };
            showLoadingProgress(getString(R.string.process_setting), true, 30000, new TwsProgressDialog.OnTimeOutListener() {
                @Override
                public void onTimeOut(TwsProgressDialog dialog) {
                    showLoadingProgress(getString(R.string.process_setting), true, 30000, new TwsProgressDialog.OnTimeOutListener() {
                        @Override
                        public void onTimeOut(TwsProgressDialog dialog) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    togbtn_push.setChecked(false);
                                    showAlert(getString(R.string.alert_setting_fail));
                                }
                            });
                        }
                    });
                    camera.openPush(succListener, failListener);
                }
            });
            camera.openPush(succListener, failListener);
        } else {
            camera.closePush(EventSettingActivity.this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }

    void searchSensitivity() {
        showLoadingProgress();
        if (camera != null) {
            // if(camera.wakeUp()<0) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            // }
            // else{
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            // }
        }
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
}
