package com.tws.commonlib.activity.hichip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hichip.base.HiLog;
import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.setting.SensitivitySettingActivity;
import com.tws.commonlib.base.CameraClient;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.bean.TwsIOCTRLDEFs;

import org.json.JSONObject;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class EventSetting_HichipActivity extends BaseActivity implements IIOTCListener {
    private static final int SENSITIVITY_SET = 0xA1;
    private String dev_uid;
    ToggleButton togbtn_push;
    ToggleButton togbtn_sdrecord;
    ToggleButton togbtn_email;
    TextView txt_email;
    ToggleButton togbtn_ftp_pic;
    ToggleButton togbtn_ftp_video;
    private HiChipDefines.HI_P2P_S_ALARM_PARAM param;
    boolean hasPIR;
    int sensLevel;
    String[] sensLevelList;
    TextView txt_sens;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param2 = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param3 = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param4 = null;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP:
                    dismissLoadingProgress();
                    HiChipDefines.HI_P2P_S_MD_PARAM md_param_temp = new HiChipDefines.HI_P2P_S_MD_PARAM(data);

                    if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_1) {
                        md_param = md_param_temp;
                        if (md_param.struArea.u32Enable == 0) {
                            sensLevel = 4;
                        } else {
                            int sensitivity = md_param.struArea.u32Sensi;
                            for (int i = 0; i < TwsDataValue.SensValues.length; i++) {
                                if (sensitivity >= TwsDataValue.SensValues[i]) {
                                    sensLevel = i;
                                    break;
                                }
                            }
                        }

                        txt_sens.setText(sensLevelList[sensLevel]);
                    } else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_2) {
                        md_param2 = md_param_temp;
                    } else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_3) {
                        md_param3 = md_param_temp;
                    } else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_4) {
                        md_param4 = md_param_temp;
                    }
                    break;
                case HiChipDefines.HI_P2P_GET_ALARM_PARAM:
                    dismissLoadingProgress();
                    hideLoadingView(R.id.togbtn_sdrecord);
                    hideLoadingView(R.id.togbtn_ftp_pic);
                    hideLoadingView(R.id.togbtn_ftp_video);
                    togbtn_sdrecord.setEnabled(true);
                    togbtn_ftp_pic.setEnabled(true);
                    togbtn_ftp_video.setEnabled(true);
                    param = new HiChipDefines.HI_P2P_S_ALARM_PARAM(data);

                    togbtn_sdrecord.setChecked(param.u32SDRec == 1 ? true : false);
                    txt_email.setText(param.u32EmailSnap == 1 ? getString(R.string.on) :  getString(R.string.off));
                    txt_email.setSelected(param.u32EmailSnap == 1 ? true : false);
                    togbtn_ftp_pic.setChecked(param.u32FtpSnap == 1 ? true : false);
                    togbtn_ftp_video.setChecked(param.u32FtpRec == 1 ? true : false);
                    break;
                case HiChipDefines.HI_P2P_SET_ALARM_PARAM:
                    TwsToast.showToast(EventSetting_HichipActivity.this,getString(R.string.tips_setting_succ));
                    break;

            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_setting_hichip);
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
        togbtn_sdrecord = (ToggleButton) findViewById(R.id.togbtn_sdrecord);
        togbtn_ftp_pic = (ToggleButton) findViewById(R.id.togbtn_ftp_pic);
        togbtn_ftp_video = (ToggleButton) findViewById(R.id.togbtn_ftp_video);
        txt_email = (TextView)findViewById(R.id.txt_email);
        togbtn_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPush(togbtn_push.isChecked());
            }
        });
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEventSetting();
            }
        };

        togbtn_sdrecord.setOnClickListener(clickListener);

        togbtn_ftp_pic.setOnClickListener(clickListener);

        togbtn_ftp_video.setOnClickListener(clickListener);
        hideLoadingView(R.id.togbtn_push);
        togbtn_push.setEnabled(true);
        togbtn_push.setChecked(camera.isPushOpen());
    }

    public  void onResume(){
        super.onResume();
        camera.registerIOTCListener(this);
        getEventSetting();
    }

    public void getEventSetting() {
        showLoadingProgress();
        txt_email.setText(getString(R.string.loading));
        togbtn_sdrecord.setEnabled(false);
        togbtn_ftp_pic.setEnabled(false);
        togbtn_ftp_video.setEnabled(false);
        camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_ALARM_PARAM, null);
    }

    public void clickLine(View view){
        if(param != null) {
            Intent intent = new Intent();
            intent.putExtras(this.getIntent());
            if (view.getId() == R.id.ll_ftpSetting) {
                camera.unregisterIOTCListener(this);
                intent.setClass(this, FTPSetting_HichipActivity.class);
                startActivityForResult(intent, getRequestCode(R.id.ll_ftpSetting));
            } else if (view.getId() == R.id.ll_emailSetting) {
                camera.unregisterIOTCListener(this);
                intent.putExtra("enabel", param.u32EmailSnap);
                intent.setClass(this, MailSetting_HichipActivity.class);
                startActivityForResult(intent, getRequestCode(R.id.ll_ftpSetting));
            }
        }
    }

    public void setEventSetting() {
        if (param != null) {
            param.u32SDRec = togbtn_sdrecord.isChecked() ? 1 : 0;
            //param.u32EmailSnap = txt_email.isSelected() ? 1 : 0;
            param.u32FtpSnap = togbtn_ftp_pic.isChecked() ? 1 : 0;
            param.u32FtpRec = togbtn_ftp_video.isChecked() ? 1 : 0;

            HiLog.e("\n param.u32SDRec: " + param.u32SDRec + "\n param.u32EmailSnap: " + param.u32EmailSnap
                    + "\n param.u32FtpSnap: " + param.u32FtpSnap + "\n param.u32FtpRec" + param.u32FtpRec);
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_SET_ALARM_PARAM, param.parseContent());
        }
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
            intent.setClass(this, SensitivitySetting_HichipActivity.class);
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
            showLoadingProgress();
            camera.openPush(new CameraClient.ServerResultListener2() {
                @Override
                public void serverResult(String resultString, JSONObject jsonArray) {
                    if (EventSetting_HichipActivity.this != null && !EventSetting_HichipActivity.this.isFinishing()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgress();
                                TwsToast.showToast(EventSetting_HichipActivity.this, getString(R.string.tips_setting_succ));
                            }
                        });
                    }
                }
            }, new CameraClient.ServerResultListener2() {
                @Override
                public void serverResult(String resultString, JSONObject jsonArray) {
                    if (EventSetting_HichipActivity.this != null && !EventSetting_HichipActivity.this.isFinishing()) {
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
            });
        } else {
            camera.closePush(EventSetting_HichipActivity.this);
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
            HiChipDefines.HI_P2P_S_MD_PARAM mdparam2 = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_2, 0, 0, 0, 0, 0, 0)
            );
            camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam2.parseContent());

            HiChipDefines.HI_P2P_S_MD_PARAM mdparam3 = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_3, 0, 0, 0, 0, 0, 0)
            );
            camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam3.parseContent());

            HiChipDefines.HI_P2P_S_MD_PARAM mdparam4 = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_4, 0, 0, 0, 0, 0, 0)
            );
            camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam4.parseContent());


            HiChipDefines.HI_P2P_S_MD_PARAM mdparam = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_1, 0, 0, 0, 0, 0, 0)
            );
            camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam.parseContent());
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
        msg.arg1 = avChannel;
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
