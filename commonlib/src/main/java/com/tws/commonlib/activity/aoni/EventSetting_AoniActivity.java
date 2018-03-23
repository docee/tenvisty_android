package com.tws.commonlib.activity.aoni;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.hichip.TimingRecord_HichipActivity;
import com.tws.commonlib.activity.setting.SensitivitySettingActivity;
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
public class EventSetting_AoniActivity extends BaseActivity implements IIOTCListener {
    private static final int SENSITIVITY_SET = 0xA1;
    private String dev_uid;
    ToggleButton togbtn_push;
    boolean hasPIR;
    int sensLevel = -1;
    int armEnable = -1;
    String[] sensLevelList;
    TextView txt_sens;
    ToggleButton togbtn_record;
    ToggleButton togbtn_push_battery;
    TextView txt_duration;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_RESP: {
                    dismissLoadingProgress();
                    int sensitivity = Packet.byteArrayToInt_Little(data, 4);
                    for (int i = 0; i < TwsDataValue.SensValues.length; i++) {
                        if (sensitivity >= TwsDataValue.SensValues[i]) {
                            sensLevel = i;
                            break;
                        }
                    }
                    txt_sens.setText(sensLevelList[sensLevel]);
                    break;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_RESP: {
                    dismissLoadingProgress();
                    int recordType = Packet.byteArrayToInt_Little(data, 4);
                    togbtn_record.setChecked(recordType == 2);
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRCD_DURATION_RESP:
                    if (Packet.byteArrayToInt_Little(data, 0) == 0) {
                        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRCD_DURATION_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
                        TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_succ));
                    } else {
                        dismissLoadingProgress();
                        TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_failed));
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRECORD_RESP:
                    dismissLoadingProgress();
                    if (Packet.byteArrayToInt_Little(data, 0) == 0) {
                        TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_succ));
                    } else {
                        togbtn_record.setChecked(!togbtn_record.isChecked());
                        TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_failed));
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRCD_DURATION_RESP: {
                    dismissLoadingProgress();
                    AVIOCTRLDEFs.SMsgAVIoctrlRcdDuration dur = new AVIOCTRLDEFs.SMsgAVIoctrlRcdDuration(data);
                    Log.i("GETRCD_DURATION_RESP", dur.durasecond + "");
                    txt_duration.setText("  " + dur.durasecond + "  ");
                    txt_duration.setTextColor(R.drawable.txt_timingrecord_color);
                    txt_duration.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
                    txt_duration.getPaint().setAntiAlias(true);//抗锯齿
                    final int duration = dur.durasecond;
                    txt_duration.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showModifyRecordDurationHint(duration);
                        }
                    });
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_BAT_PUSH_EN_RESP: {
                    dismissLoadingProgress();
                    if (Packet.byteArrayToInt_Little(data, 0) == 0) {
                        TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_succ));
                    } else {
                        togbtn_push_battery.setChecked(!togbtn_push_battery.isChecked());
                        TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_failed));
                    }
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_BAT_PUSH_EN_RESP: {
                    dismissLoadingProgress();
                    int on = Packet.byteArrayToInt_Little(data, 0);
                    togbtn_push_battery.setChecked(on == 1);
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ARM_STATUS_RESP: {
                    armEnable = Packet.byteArrayToInt_Little(data, 0);
                    if (armEnable == 0) {
                        txt_sens.setText(sensLevelList[sensLevelList.length - 1]);
                    } else {
                        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
                    }
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PIR_SENSITIVITY_RESP: {
                    dismissLoadingProgress();
                    int sensitivity = Packet.byteArrayToInt_Little(data, 0);
                    for (int i = 0; i < TwsDataValue.SensValues.length; i++) {
                        if (sensitivity >= TwsDataValue.SensValues[i]) {
                            sensLevel = i;
                            break;
                        }
                    }
                    if (sensLevel == TwsDataValue.SensValues.length - 1) {
                        sensLevel--;
                    }
                    txt_sens.setText(sensLevelList[sensLevel]);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 显示密码错误的对话框
     */
    public void showModifyRecordDurationHint(int originValue) {

        Log.i("1233333", "==showPasswordWrongHint==");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dlg = builder.create();
        dlg.setTitle(getString(R.string.dialog_title_camera_modify_recordduration));
        dlg.setIcon(android.R.drawable.ic_menu_save);
        LayoutInflater inflater = dlg.getLayoutInflater();
        View view = inflater.inflate(R.layout.hint_record_duration, null);
        dlg.setView(view);
        dlg.setCanceledOnTouchOutside(false);

        final EditText edt_duration = (EditText) view.findViewById(R.id.edt_duration);
        final Button btnOK = (Button) view.findViewById(R.id.btnOK);
        final Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        edt_duration.setText(originValue + "");
        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String duration = edt_duration.getText().toString();
                int seconds = 10;
                try {
                    seconds = Integer.parseInt(duration);
                    if (seconds < 10 || seconds > 300) {
                        Toast.makeText(EventSetting_AoniActivity.this, String.format(getText(R.string.alert_alarm_recording_time_range).toString(), 10, 300), Toast.LENGTH_LONG).show();
                        return;
                    }
                    setRecordDuration(seconds);
                } catch (Exception ex) {

                }
                dlg.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dlg.dismiss();
            }
        });

        dlg.show();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_setting_aoni);
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
        getSetting();
    }

    @Override
    protected void initView() {
        super.initView();
        togbtn_push = (ToggleButton) findViewById(R.id.togbtn_push);
        togbtn_record = (ToggleButton) findViewById(R.id.togbtn_record);
        togbtn_push_battery = (ToggleButton) findViewById(R.id.togbtn_push_battery);
        txt_duration = (TextView) findViewById(R.id.txt_duration);
        togbtn_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPush(togbtn_push.isChecked());
            }
        });
        togbtn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRecord(((ToggleButton) view).isChecked());
            }
        });
        togbtn_push_battery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBatteryPush(((ToggleButton) view).isChecked());
            }
        });
        togbtn_push.setChecked(camera.isPushOpen());
    }

    public void setBatteryPush(boolean on) {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_BAT_PUSH_EN_REQ, AVIOCTRLDEFs.SMsgAVIoctrlBatPush.parseContent(on ? 1 : 0));
        }
    }

    public void setRecord(Boolean on) {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRECORD_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetRecordReq.parseContent(0, on ? 2 : 0));
        }
    }

    public void setRecordDuration(int seconds) {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRCD_DURATION_REQ, AVIOCTRLDEFs.SMsgAVIoctrlRcdDuration.parseContent(seconds));
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
            intent.setClass(this, SensitivitySetting_AoniActivity.class);
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
            final CameraClient.ServerResultListener2 succListener = new CameraClient.ServerResultListener2() {
                @Override
                public void serverResult(String resultString, JSONObject jsonArray) {
                    if (EventSetting_AoniActivity.this != null && !EventSetting_AoniActivity.this.isFinishing()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgress();
                                TwsToast.showToast(EventSetting_AoniActivity.this, getString(R.string.tips_setting_succ));
                            }
                        });
                    }
                }
            };
            final CameraClient.ServerResultListener2 failListener = new CameraClient.ServerResultListener2() {
                @Override
                public void serverResult(String resultString, JSONObject jsonArray) {
                    if (EventSetting_AoniActivity.this != null && !EventSetting_AoniActivity.this.isFinishing()) {
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
            camera.closePush(EventSetting_AoniActivity.this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }

    void getSetting() {
        showLoadingProgress();
        if (camera != null) {
            // camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            //camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ARM_STATUS_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRCD_DURATION_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_BAT_PUSH_EN_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
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
