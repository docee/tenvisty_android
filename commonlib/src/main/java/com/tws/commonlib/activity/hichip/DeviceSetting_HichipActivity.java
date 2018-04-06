package com.tws.commonlib.activity.hichip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.setting.EventSettingActivity;
import com.tws.commonlib.activity.setting.ModifyCameraNameActivity;
import com.tws.commonlib.activity.setting.ModifyCameraPasswordActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class DeviceSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private TextView txt_camera_name;
    private TextView txt_camera_record;
    private TextView txt_camera_wifi;
    private String[] recordTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }
        setTitle(R.string.title_camera_setting);
        initView();
        getSetting();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        ((ImageView) findViewById(R.id.imgview_snap)).setImageBitmap(camera.getSnapshot());
        ((TextView) findViewById(R.id.txt_uid)).setText(camera.getUid());
        txt_camera_name = (TextView) findViewById(R.id.txt_camera_name);
        txt_camera_record = (TextView) findViewById(R.id.txt_camera_record);
        txt_camera_wifi = (TextView) findViewById(R.id.txt_camera_wifi);
        recordTypes = getResources().getStringArray(R.array.record_type);
    }

    void getSetting() {
        if (camera != null) {
            showLoadingView(R.id.txt_camera_wifi);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_GET_WIFI_PARAM, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        txt_camera_name.setText(camera.getNickName());
    }

    public void goSetting(View view) {

        Intent intent = new Intent();
        intent.putExtras(this.getIntent());
        if (view.getId() == R.id.ll_setCameraName) {
            intent.setClass(this, ModifyCameraNameActivity.class);
        } else if (view.getId() == R.id.ll_setCameraPwd) {
            intent.setClass(this, ModifyCameraPasswordActivity.class);
        } else if (view.getId() == R.id.ll_setCameraNetwork) {
            intent.setClass(this, WiFiList_HichipActivity.class);
        } else if (view.getId() == R.id.ll_setCameraEvent) {
            if (camera.getP2PType() == IMyCamera.CameraP2PType.HichipP2P) {
                intent.setClass(this, EventSetting_HichipActivity.class);
            } else {
                intent.setClass(this, EventSettingActivity.class);
            }
        } else if (view.getId() == R.id.ll_setCameraOther) {
            intent.setClass(this, OtherSetting_HichipActivity.class);
        } else if (view.getId() == R.id.ll_setCameraSystem) {
            intent.setClass(this, SystemSetting_HichipActivity.class);
        } else if (view.getId() == R.id.ll_setCameraRecord) {
            intent.setClass(this, TimingRecord_HichipActivity.class);
            startActivityForResult(intent, getRequestCode(R.id.ll_setCameraRecord));
            return;
        }
//        else if (view.getId() == R.id.ll_setCameraShare) {
//
//        }
        if (intent.getClass() != null) {
            startActivity(intent);
        }
    }


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_RESP:
                    if (camera.getP2PType() == IMyCamera.CameraP2PType.TutkP2P) {
                        hideLoadingView(R.id.txt_camera_record);
                        int recordType = Packet.byteArrayToInt_Little(data, 4);
                        if (recordTypes != null && recordTypes.length >= recordType) {
                            txt_camera_record.setText(recordTypes[recordType]);
                        }
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_RESP:
                    hideLoadingView(R.id.txt_camera_wifi);
                    String connectedSsid = "";
                    if (msg.arg1 == 0) {
                        HiChipDefines.HI_P2P_S_WIFI_PARAM wifi_param = new HiChipDefines.HI_P2P_S_WIFI_PARAM(data);

                        connectedSsid = TwsTools.getString(wifi_param.strSSID);
                    }
                    final String fssid = connectedSsid;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txt_camera_wifi.setText(fssid);
                        }
                    });

                    break;
            }
            super.handleMessage(msg);
        }
    };

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
        msg.arg1 = avChannel;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getRequestCode(R.id.ll_setCameraRecord)) {
            if (resultCode == RESULT_OK) {
                int recordType = data.getIntExtra("data", 0);
                if (recordTypes != null && recordTypes.length >= recordType) {
                    txt_camera_record.setText(recordTypes[recordType]);
                }
            }
        }

    }
}
