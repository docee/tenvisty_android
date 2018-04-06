package com.tws.commonlib.activity.hichip;

import android.content.DialogInterface;
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

import com.hichip.content.HiChipDefines;
import com.hichip.sdk.HiChipP2P;
import com.hichip.system.HiDefaultData;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimeSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private HichipCamera camera;
    String[] timezoneSourceList;
    TextView txt_timezone;
    TextView txt_time;
    int accTimezoneIndex = -999;
    ToggleButton togbtn_dst;
    LinearLayout ll_setdst;
    private boolean mIsSupportZoneExt;
    private HiChipDefines.HI_P2P_S_TIME_ZONE timezone;

    protected HiChipDefines.HI_P2P_S_TIME_ZONE_EXT time_ZONE_EXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_time_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
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
        txt_timezone = (TextView) findViewById(R.id.txt_timezone);
        togbtn_dst = (ToggleButton) findViewById(R.id.togbtn_dst);
        ll_setdst = (LinearLayout) findViewById(R.id.ll_setdst);
        txt_time = (TextView)findViewById(R.id.txt_time);
        //adapter.notifyDataSetChanged();
        togbtn_dst.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                if (compoundButton.isPressed()) {
                    showYesNoDialog(R.string.alert_device_time_setting_reboot_camera, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    setDst(b);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    togbtn_dst.setChecked(!b);
                                    break;
                            }
                        }
                    });
                }
            }
        });
        mIsSupportZoneExt = camera.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT);
        if (mIsSupportZoneExt) {// 支持新时区
            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
            timezoneSourceList = getResources().getStringArray(R.array.device_timezone_new);
        } else {
            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
            timezoneSourceList = getResources().getStringArray(R.array.device_timezone_old);
        }
        getRemoteData();
    }

    void setDst(boolean enable) {
        if (camera != null && accTimezoneIndex != -999) {
            showLoadingProgress();
            if (mIsSupportZoneExt) {
                int desMode = enable ? 1 : 0;
                byte[] byte_time = HiDefaultData.TimeZoneField1[accTimezoneIndex][0].getBytes();

                if (byte_time.length <= 32) {
                    camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT,
                            HiChipDefines.HI_P2P_S_TIME_ZONE_EXT.parseContent(byte_time, desMode));
                }
            } else {
                int tz = HiDefaultData.TimeZoneField[accTimezoneIndex][0];
                int desMode = enable ? 1 : 0;
                camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_ZONE, HiChipDefines.HI_P2P_S_TIME_ZONE.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, tz, desMode));
            }
        }
    }

    void asyncTime() {
        if (camera != null) {
            showLoadingProgress();
            if (!camera.syncPhoneTime()) {
                dismissLoadingProgress();
            }
        }
    }

    void getRemoteData() {
        showLoadingProgress();
        if (camera != null) {
            showLoadingView(R.id.txt_timezone);
            showLoadingView(R.id.txt_time);
            showLoadingView(R.id.togbtn_dst);
            if (mIsSupportZoneExt) {// 支持新时区
                camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
            } else {
                camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
            }
            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_PARAM, null);
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
                case HiChipDefines.HI_P2P_GET_TIME_PARAM:
                    dismissLoadingProgress();
                    hideLoadingView(R.id.txt_time);
                    if (data != null && data.length >= 24) {
                        HiChipDefines.HI_P2P_S_TIME_PARAM dt = new HiChipDefines.HI_P2P_S_TIME_PARAM(data);
                        txt_time.setText(String.format("%d-%d-%d %d:%d:%d", dt.u32Year, dt.u32Month, dt.u32Day, dt.u32Hour, dt.u32Minute, dt.u32Second));
                    }
                    break;
                case HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT:// 新时区
                    dismissLoadingProgress();
                    if (data != null && data.length >= 36) {
                        time_ZONE_EXT = new HiChipDefines.HI_P2P_S_TIME_ZONE_EXT(data);
                        camera.setTimezoneExt(time_ZONE_EXT);
                        int index = 0;
                        for (int i = 0; i < HiDefaultData.TimeZoneField1.length; i++) {
                            if (isEqual(time_ZONE_EXT.sTimeZone, HiDefaultData.TimeZoneField1[i][0])) {
                                index = i;
                                break;
                            }
                        }
                        accTimezoneIndex = index;
                        txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);
                        hideLoadingView(R.id.txt_timezone);
                        hideLoadingView(R.id.togbtn_dst);
                        if (HiDefaultData.TimeZoneField1[accTimezoneIndex][2].equals("1")) {
                            ll_setdst.setVisibility(View.VISIBLE);
                        } else {
                            ll_setdst.setVisibility(View.GONE);
                        }
                    }
                    break;
                case HiChipDefines.HI_P2P_GET_TIME_ZONE:
                    timezone = new HiChipDefines.HI_P2P_S_TIME_ZONE(data);
                    if (data.length >= 12) {
                        camera.setTimezone(timezone);
                    }
                    togbtn_dst.setChecked(timezone.u32DstMode == 1);
                    int index = 0;
                    for (int i = 0; i < 24; i++) {
                        if (HiDefaultData.TimeZoneField[i][0] == timezone.s32TimeZone) {
                            index = i;
                            break;
                        }
                    }
                    accTimezoneIndex = index;
                    txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);

                    if (HiDefaultData.TimeZoneField[accTimezoneIndex][1] == 1) {
                        ll_setdst.setVisibility(View.VISIBLE);
                    } else {
                        ll_setdst.setVisibility(View.GONE);
                    }
                    hideLoadingView(R.id.togbtn_dst);
                    hideLoadingView(R.id.txt_timezone);
                    dismissLoadingProgress();

                    break;
                case HiChipDefines.HI_P2P_SET_TIME_PARAM:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_PARAM, null);
                        }
                    }, 2000);

                    break;
                case HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT:
                case HiChipDefines.HI_P2P_SET_TIME_ZONE:
                    //if(!mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)){
                    camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REBOOT, new byte[0]);

                    TwsToast.showToast(TimeSetting_HichipActivity.this, getString(R.string.toast_setting_succ));
                    //}else{
                    //HiToast.showToast(EquipmentTimeSettingActivity.this, getString(R.string.tips_device_time_setting_timezone_new));
                    //}

                    break;
                case HiChipDefines.HI_P2P_SET_REBOOT:
                    dismissLoadingProgress();
                    TwsToast.showToast(TimeSetting_HichipActivity.this, getString(R.string.reboot));
                    TimeSetting_HichipActivity.this.back2Activity(MainActivity.class);
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private boolean isEqual(byte[] bys, String str) {
        String string = new String(bys);
        String temp = string.substring(0, str.length());
        if (temp.equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }

    public void goSetting(View view) {
        if (view.getId() == R.id.ll_select_time_zone) {
            Intent intent = new Intent();
            intent.putExtras(this.getIntent());
            intent.setClass(this, TimezoneSetting_HichipActivity.class);
            intent.putExtra("timezone", accTimezoneIndex);
            intent.putExtra("dst", togbtn_dst.isChecked() ? 1 : 0);
            startActivityForResult(intent, getRequestCode(R.id.ll_select_time_zone));
        } else if (view.getId() == R.id.ll_time_ajust) {
            asyncTime();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                Bundle b = data.getExtras(); //data为B中回传的Intent
                if (requestCode == getRequestCode(R.id.ll_select_time_zone)) {
                    accTimezoneIndex = b.getInt("timezone");
                    if (mIsSupportZoneExt) {// 支持新时区
                        if (HiDefaultData.TimeZoneField1[accTimezoneIndex][2].equals("1")) {
                            ll_setdst.setVisibility(View.VISIBLE);
                        } else {
                            ll_setdst.setVisibility(View.GONE);
                        }
                    } else {
                        if (HiDefaultData.TimeZoneField[accTimezoneIndex][1] == 1) {
                            ll_setdst.setVisibility(View.VISIBLE);
                        } else {
                            ll_setdst.setVisibility(View.GONE);
                        }
                    }
                    txt_timezone.setText(timezoneSourceList[accTimezoneIndex].split(";")[0]);
                }
                break;
            default:
                break;
        }
    }


}
