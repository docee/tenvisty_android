package com.tws.commonlib.activity.aoni;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.App;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.setting.EventSettingActivity;
import com.tws.commonlib.activity.setting.ModifyCameraNameActivity;
import com.tws.commonlib.activity.setting.ModifyCameraPasswordActivity;
import com.tws.commonlib.activity.setting.OtherSettingActivity;
import com.tws.commonlib.activity.setting.RecordSettingActivity;
import com.tws.commonlib.activity.setting.SystemSettingActivity;
import com.tws.commonlib.activity.setting.WiFiListActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class DeviceSetting_AoniActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private TextView txt_camera_name;
    private TextView txt_camera_wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera_setting_aoni);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }
        setTitle(R.string.title_deviceSetting);
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
        txt_camera_wifi = (TextView) findViewById(R.id.txt_camera_wifi);
    }

    void getSetting() {
        if (camera != null) {
            showLoadingView(R.id.txt_camera_wifi);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetWifiReq.parseContent());
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
            intent.setClass(this, WiFiListActivity.class);
        } else if (view.getId() == R.id.ll_setCameraEvent) {
            if (camera.getSupplier() == IMyCamera.Supllier.AN) {
                intent.setClass(this, EventSetting_AoniActivity.class);
            } else {
                intent.setClass(this, EventSettingActivity.class);
            }
        } else if (view.getId() == R.id.ll_setCameraOther) {
            if (camera.getSupplier() == IMyCamera.Supllier.AN) {
                intent.setClass(this, OtherSetting_AoniActivity.class);
            } else {
                intent.setClass(this, OtherSettingActivity.class);
            }
        } else if (view.getId() == R.id.ll_setCameraSystem) {
            if (camera.getSupplier() == IMyCamera.Supllier.AN) {
                intent.setClass(this, SystemSetting_AoniActivity.class);
            } else {
                intent.setClass(this, SystemSettingActivity.class);
            }
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
                case TwsDataValue.HANDLE_MESSAGE_IO_RESP: {
                    switch (msg.arg1) {
                        case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_RESP:
                            hideLoadingView(R.id.txt_camera_wifi);
                            String connectedSsid = "";
                            byte[] ssid = new byte[32];
                            System.arraycopy(data, 0, ssid, 0, 32);
                            int connectStatus = data[67];
                            if (connectStatus == 1 || connectStatus == 3 || connectStatus == 4 || !camera.supportCable()) {
                                connectedSsid = TwsTools.getString(ssid);
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
                }
                break;
                case TwsDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    if (camera.isSleeping()) {
                        showAlert(App.getTopActivity(),getString(R.string.alert_camera_wakeup), null, false, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                back2Activity(MainActivity.class);
                            }
                        });
                    }
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
        Bundle bundle = new Bundle();

        Message msg = new Message();
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        ;
        msg.arg1 = resultCode;
        msg.setData(bundle);
        handler.sendMessage(msg);
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
        msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
        ;
        msg.arg1 = avIOCtrlMsgType;
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
}
