package com.tws.commonlib.activity.setting;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimeSettingActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    String[] timezoneSourceList;
    TextView txt_timezone;
    int accTimezoneIndex = -999;
    TextView txt_time;
    ToggleButton togbtn_dst;
    LinearLayout ll_setdst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_time_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera =  _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_camera_setting_time));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        timezoneSourceList = getResources().getStringArray(R.array.device_timezone_new);
        txt_timezone = (TextView) findViewById(R.id.txt_timezone);
        txt_time = (TextView) findViewById(R.id.txt_time);
        togbtn_dst = (ToggleButton) findViewById(R.id.togbtn_dst);
        ll_setdst =(LinearLayout)findViewById(R.id.ll_setdst);
        //adapter.notifyDataSetChanged();
        togbtn_dst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isPressed()) {
                    setDst(b);
                }
            }
        });
        getRemoteData();
    }

    void setDst(boolean enable) {
        if (camera != null && accTimezoneIndex != -999) {
            showLoadingProgress();
            String[] timezoneInfo = TwsDataValue.TimeZoneField[accTimezoneIndex];
            byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSetTZoneReq.parseContent(timezoneInfo[0],enable?1:0);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ZONE_INFO_REQ, data);
        }
    }

    void asyncTime() {
        if (camera != null) {
            showLoadingProgress();
//            Calendar phoneCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
//            phoneCal.setTimeInMillis(System.currentTimeMillis());
//            byte[] phoneTime = AVIOCTRLDEFs.STimeDay.parseContent(phoneCal.get(Calendar.YEAR), phoneCal.get(Calendar.MONTH) + 1, phoneCal.get(Calendar.DAY_OF_MONTH),
//                    phoneCal.get(Calendar.DAY_OF_WEEK), phoneCal.get(Calendar.HOUR_OF_DAY)+1, phoneCal.get(Calendar.MINUTE), phoneCal.get(Calendar.SECOND));
//            byte[] data2 = AVIOCTRLDEFs.SMsgAVIoctrlTime.parseContent(phoneTime, 0, TwsDataValue.NTP_SERVER, 1);
//            ((MyCamera) camera).sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ, data2);
            byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlTime.parseContent(AVIOCTRLDEFs.STimeDay.parseContent(0, 0, 0, 0, 0, 0, 0), 1, TwsDataValue.NTP_SERVER, 0);
           // Calendar phoneCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
           // phoneCal.setTimeInMillis(System.currentTimeMillis());
            //byte[] phoneTime = AVIOCTRLDEFs.STimeDay.parseContent(phoneCal.get(Calendar.YEAR), phoneCal.get(Calendar.MONTH) + 1, phoneCal.get(Calendar.DAY_OF_MONTH),
                  //  phoneCal.get(Calendar.DAY_OF_WEEK), phoneCal.get(Calendar.HOUR_OF_DAY), phoneCal.get(Calendar.MINUTE), phoneCal.get(Calendar.SECOND));
           // byte[] data2 = AVIOCTRLDEFs.SMsgAVIoctrlTime.parseContent(phoneTime, 0, TwsDataValue.NTP_SERVER, 1);
            //((MyCamera) camera).sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ, data2);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ, data);
        }
    }

    void getRemoteData() {
        showLoadingProgress();
        if (camera != null) {
            showLoadingView(R.id.txt_timezone);
            ((LinearLayout.LayoutParams)txt_timezone.getLayoutParams()).weight = 0;
            showLoadingView(R.id.txt_time);
            showLoadingView(R.id.togbtn_dst);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ZONE_INFO_REQ, new byte[1]);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIME_INFO_REQ,AVIOCTRLDEFs.SMsgAVIoctrlGetTimeReq.parseContent(false));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
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

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIMEZONE_RESP:
                    dismissLoadingProgress();
                    AVIOCTRLDEFs.SMsgAVIoctrlTimeZone timeZone = new AVIOCTRLDEFs.SMsgAVIoctrlTimeZone(data);
                    accTimezoneIndex = timeZone.nGMTDiff + 12;
                    // String strTimeZone = new String(timeZone.szTimeZoneString).trim();
                    txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);
                    hideLoadingView(R.id.txt_timezone);
                    ((LinearLayout.LayoutParams)txt_timezone.getLayoutParams()).weight = 2;
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP:
                    if (data[0] == 0x00) {
                        dismissLoadingProgress();
                        TimeSettingActivity.this.finish();
                    } else {
                        showAlert(getString(R.string.alert_setting_fail));
                        getRemoteData();
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ZONE_INFO_RESP:
                    dismissLoadingProgress();
                    hideLoadingView(R.id.txt_timezone);
                    ((LinearLayout.LayoutParams)txt_timezone.getLayoutParams()).weight = 2;
                    hideLoadingView(R.id.togbtn_dst);
                    AVIOCTRLDEFs.SMsgAVIoctrlGetTZoneResp timezone = new AVIOCTRLDEFs.SMsgAVIoctrlGetTZoneResp(data);
                    txt_timezone.setText("");
                    for(int i=0;i<TwsDataValue.TimeZoneField.length;i++){
                        if(TwsTools.getString(timezone.dstDistrictInfo.dstDistId).indexOf(getTimeZoneInfo(i)[0]) == 0){
                            txt_timezone.setText(timezoneSourceList[i].split(";")[0]);
                            accTimezoneIndex = i;
                            if(getTimeZoneInfo(accTimezoneIndex)[2]=="1"){
                                ll_setdst.setVisibility(View.VISIBLE);
                                togbtn_dst.setChecked(timezone.enable==1);
                            }
                            else{
                                ll_setdst.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIME_INFO_RESP:
                    dismissLoadingProgress();
                    hideLoadingView(R.id.txt_time);
                    AVIOCTRLDEFs.SMsgAVIoctrlTime time = new AVIOCTRLDEFs.SMsgAVIoctrlTime(data);
                    txt_time.setText(time.timeInfo.getTime());
                    //txt_time.setText(String.format("%s/%s/%s %s:%s:%s",(int)time.timeInfo.month+"",(int)time.timeInfo.day+"",(int)time.timeInfo.year+"",(int)time.timeInfo.hour+"",(int)time.timeInfo.minute+"",(int)time.timeInfo.second+""));
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_RESP:
                    if (data[3] == 0) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIME_INFO_REQ,AVIOCTRLDEFs.SMsgAVIoctrlGetTimeReq.parseContent(false));
                            }
                        }, 5000);
                        //dismissLoadingProgress();
                    } else {
                        dismissLoadingProgress();
                        showAlert(getString(R.string.alert_setting_fail));
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ZONE_INFO_RESP:
                    dismissLoadingProgress();
                    if(data[3] == 0){
                        TwsToast.showToast(TimeSettingActivity.this,getString(R.string.toast_setting_succ));
                    }
                    else{
                        showAlert(getString(R.string.alert_setting_fail));
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void goSetting(View view) {
        if (view.getId() == R.id.ll_select_time_zone) {
            Intent intent = new Intent();
            intent.putExtras(this.getIntent());
            intent.setClass(this, TimezoneSettingActivity.class);
            intent.putExtra("timezone", accTimezoneIndex);
            intent.putExtra("dst", togbtn_dst.isChecked()?1:0);
            startActivityForResult(intent, getRequestCode(R.id.ll_select_time_zone));
        }
        else if(view.getId() == R.id.ll_time_ajust){
            asyncTime();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                Bundle b = data.getExtras(); //data为B中回传的Intent
                if (requestCode == getRequestCode(R.id.ll_select_time_zone)) {
                    accTimezoneIndex = b.getInt("timezone");
                    if(getTimeZoneInfo(accTimezoneIndex)[2]=="1"){
                        ll_setdst.setVisibility(View.VISIBLE);
                    }
                    else{
                        ll_setdst.setVisibility(View.GONE);
                    }
                    txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);
                }
                break;
            default:
                break;
        }
    }

    public  String[] getTimeZoneInfo(int pos){
        return  TwsDataValue.TimeZoneField[pos];
    }


}
