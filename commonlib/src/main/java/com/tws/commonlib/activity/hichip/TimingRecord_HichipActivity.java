package com.tws.commonlib.activity.hichip;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hichip.content.HiChipDefines;
import com.hichip.data.HiDeviceInfo;
import com.hichip.sdk.HiChipP2P;
import com.tutk.IOTC.Camera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.SetScheduleTimeController;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimingRecord_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private HichipCamera camera;
    private final static int REQUEST_GET_WIFI = 0x998;
    boolean isTimeout = false;
    boolean isRequestWiFiListForResult = false;
    TextView txt_videoTime;
    private HiChipDefines.HI_P2P_S_REC_AUTO_PARAM rec_param;
    private static final int GOKE_TIME = 600;
    private static final int HISI_TIME = 900;
    private int recordTime = HISI_TIME;
    private EditText edit_duration;
    private SetScheduleTimeController setScheduleTimeController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timing_record_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_camera_setting_record_timing));
        camera.registerIOTCListener(this);
        initView();
        getTimingRecordSetting();
    }

    private  void  getTimingRecordSetting(){
        showLoadingProgress();
        camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_REC_AUTO_PARAM, new byte[0]);
    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        save();
                        break;
                }
            }
        });
        if (camera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE) {
            recordTime = GOKE_TIME;
        }
        txt_videoTime = (TextView) findViewById(R.id.txt_videoTime);
        edit_duration = (EditText) findViewById(R.id.edit_duration);
        txt_videoTime.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        txt_videoTime.getPaint().setAntiAlias(true);//抗锯齿
        txt_videoTime.setText(getString(R.string.loading));
        txt_videoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setScheduleTimeController.showRecordTypePopView(getString(R.string.title_camera_setting_record_timing));
            }
        });
        setScheduleTimeController = new SetScheduleTimeController(this, 1, camera, new SetScheduleTimeController.OnSchuduleResult() {

            @Override
            public void onGetRemoteData(boolean[] weekDayChecked) {
                // TODO Auto-generated method stub
                dismissLoadingProgress();
            }

            @Override
            public void onSetRemoteData() {
                // TODO Auto-generated method stub
                TwsToast.showToast(TimingRecord_HichipActivity.this, getString(R.string.tips_setting_succ));
                finish();
            }

            @Override
            public void onWriteResult(String text, int type) {
                // TODO Auto-generated method stub
                txt_videoTime.setText(text);
            }
        });
        setScheduleTimeController.setArrTitles(new String[]{getString(R.string.tips_recordtime_none_title), getString(R.string.tips_recordtime_allday_title), getString(R.string.tips_recordtime_custom_title)});
        setScheduleTimeController.setArrDescs(new String[]{getString(R.string.tips_recordtime_none_desc), getString(R.string.tips_recordtime_allday_desc), getString(R.string.tips_recordtime_custom_desc)});
        setScheduleTimeController.GetSchedule();
    }

    void save() {
        isTimeout = false;
        if(rec_param == null) {
            return;
        }

        String str = edit_duration.getText().toString().trim();
        int rec_time_val = 0;
        if(str!=null && str.length()>0) {
            try {
                rec_time_val = Integer.valueOf(str);
            }catch (Exception ex){
                rec_time_val = recordTime;
            }
        }

        if(rec_time_val < 15 || rec_time_val > recordTime) {

            Toast.makeText(TimingRecord_HichipActivity.this, String.format(getText(R.string.alert_recording_time_range).toString(), recordTime), Toast.LENGTH_LONG).show();
            return;
        }



        showLoadingProgress(getString(R.string.process_setting), true, 60000, new TwsProgressDialog.OnTimeOutListener() {
            @Override
            public void onTimeOut(TwsProgressDialog dialog) {
                isTimeout = true;
                dismissLoadingProgress();
                TwsToast.showToast(TimingRecord_HichipActivity.this, getString(R.string.process_connect_timeout));

            }
        });

        rec_param.u32Enable = setScheduleTimeController.GetSelectType() == 0?0:1;//togbtn_motion_detection.isChecked()?1:0;
        rec_param.u32FileLen = rec_time_val;

        camera.sendIOCtrl(0,HiChipDefines.HI_P2P_SET_REC_AUTO_PARAM,rec_param.parseContent());

        if(rec_param.u32Enable != 0){
            setScheduleTimeController.SetSchedule();
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
        Log.i(this.getClass().getSimpleName(), "connect state " + camera.getUid() + " " + resultCode);
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = resultCode;
        msg.obj = camera;
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
        msg.arg1 = avIOCtrlMsgType;
        msg.arg2 = avChannel;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        msg.obj = camera;
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

    public void doClickLL(View view) {
        ((LinearLayout) view).getChildAt(1).requestFocus();
    }


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case TwsDataValue.HANDLE_MESSAGE_IO_RESP: {
                    if (msg.arg2 == 0) {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_REC_AUTO_PARAM:
                                dismissLoadingProgress();
                                rec_param = new HiChipDefines.HI_P2P_S_REC_AUTO_PARAM(data);
                                edit_duration.setText(String.valueOf(rec_param.u32FileLen));

                                boolean isOpenRec = rec_param.u32Enable == 1 ? true : false;

                                if (!isOpenRec) {
                                    setScheduleTimeController.SetSelectType(0);
                                }
                                break;

                            case HiChipDefines.HI_P2P_SET_REC_AUTO_PARAM: {
                                dismissLoadingProgress();
                                if (rec_param != null && rec_param.u32Enable == 0) {
                                    TwsToast.showToast(TimingRecord_HichipActivity.this, getString(R.string.tips_setting_succ));
                                    finish();
                                }
                            }
                            break;
                        }
                    }
                }
                break;
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


}
