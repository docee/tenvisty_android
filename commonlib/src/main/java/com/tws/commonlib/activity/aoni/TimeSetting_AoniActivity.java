package com.tws.commonlib.activity.aoni;

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
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import java.util.Date;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimeSetting_AoniActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    String[] timezoneSourceList;
    TextView txt_timezone;
    int accTimezoneIndex = -999;
    TextView txt_time;
    ToggleButton togbtn_dst;
    LinearLayout ll_setdst;
    AVIOCTRLDEFs.SMsgAVIoctrlNtpConfig ntpConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_time_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
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
        timezoneSourceList = getResources().getStringArray(R.array.device_timezone_old);
        txt_timezone = (TextView) findViewById(R.id.txt_timezone);
        txt_time = (TextView) findViewById(R.id.txt_time);
        togbtn_dst = (ToggleButton) findViewById(R.id.togbtn_dst);
        ll_setdst = (LinearLayout) findViewById(R.id.ll_setdst);
        //adapter.notifyDataSetChanged();
        togbtn_dst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    setDst(b);
                }
            }
        });
        ll_setdst.setVisibility(View.GONE);
        getRemoteData();
    }

    void setDst(boolean enable) {
        if (camera != null && accTimezoneIndex != -999) {
            showLoadingProgress();
            String[] timezoneInfo = TwsDataValue.TimeZoneField[accTimezoneIndex];
            byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSetTZoneReq.parseContent(timezoneInfo[0], enable ? 1 : 0);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ZONE_INFO_REQ, data);
        }
    }

    void asyncTime(int mode) {
        if (camera != null) {
            if (ntpConfig != null) {
                showLoadingProgress();
                AVIOCTRLDEFs.Ntp_set_time time = new AVIOCTRLDEFs.Ntp_set_time();
                Date date = new Date();
                time.year = date.getYear() + 1900;
                time.month = date.getMonth() + 1;
                time.date = date.getDate();
                time.hour = date.getHours();
                time.minute = date.getMinutes();
                time.second = date.getSeconds();
                this.ntpConfig.mod = mode;
                if (!TwsTools.getString(this.ntpConfig.Server).equals("128.138.140.44") && !TwsTools.getString(this.ntpConfig.Server).equals(TwsDataValue.NTP_SERVER)) {
                    byte[] serverBytes = TwsDataValue.NTP_SERVER.getBytes();
                    System.arraycopy(TwsDataValue.NTP_SERVER.getBytes(), 0, this.ntpConfig.Server, 0, serverBytes.length);
                }
                byte[] data = this.ntpConfig.parseContent();

                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_NTP_CONFIG_REQ, data);
            }
        }
    }

    void getRemoteData() {
        showLoadingProgress();
        if (camera != null) {
            showLoadingView(R.id.txt_timezone);
            ((LinearLayout.LayoutParams)txt_timezone.getLayoutParams()).weight = 0;
            showLoadingView(R.id.txt_time);
            showLoadingView(R.id.togbtn_dst);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_NTP_CONFIG_REQ, new byte[1]);
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
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_NTP_CONFIG_RESP: {
                    dismissLoadingProgress();
                    int result = Packet.byteArrayToInt_Little(data, 0);
                    if (result == 0) {
                        getRemoteData();
//                        if (ntpConfig != null && ntpConfig.mod == 2) {
//                            asyncTime(1);
//                        } else {
//                            getRemoteData();
//                        }
                    } else {
                        showAlert(getString(R.string.alert_setting_fail));
                    }
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_NTP_CONFIG_RESP: {
                    dismissLoadingProgress();
                    ntpConfig = new AVIOCTRLDEFs.SMsgAVIoctrlNtpConfig(data);
                    hideLoadingView(R.id.txt_time);
                    txt_time.setText(String.format("%d-%d-%d %d:%d:%d", ntpConfig.time.year, ntpConfig.time.month, ntpConfig.time.date, ntpConfig.time.hour, ntpConfig.time.minute, ntpConfig.time.second));

                    accTimezoneIndex = ntpConfig.TimeZone - 1;
                    txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);
                    hideLoadingView(R.id.txt_timezone);
                    ((LinearLayout.LayoutParams)txt_timezone.getLayoutParams()).weight = 2;
                    hideLoadingView(R.id.togbtn_dst);
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
            intent.setClass(this, TimezoneSetting_AoniActivity.class);
            intent.putExtra("timezone", accTimezoneIndex);
            intent.putExtra("dst", togbtn_dst.isChecked() ? 1 : 0);
            startActivityForResult(intent, getRequestCode(R.id.ll_select_time_zone));
        } else if (view.getId() == R.id.ll_time_ajust) {
            asyncTime(1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                Bundle b = data.getExtras(); //data为B中回传的Intent
                if (requestCode == getRequestCode(R.id.ll_select_time_zone)) {
                    accTimezoneIndex = b.getInt("timezone");
                    txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);
                }
                break;
            default:
                break;
        }
    }

    public String[] getTimeZoneInfo(int pos) {
        return TwsDataValue.TimeZoneField[pos];
    }


}
