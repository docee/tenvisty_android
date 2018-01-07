package com.tws.commonlib.activity.setting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class DeviceInfoActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    EditText edt_mode;
    EditText edt_version;
    EditText edt_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_info);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_setting_devinfo));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
//        edt_mode = (EditText) findViewById(R.id.edt_mode);
        edt_version = (EditText) findViewById(R.id.edt_version);
        edt_uid = (EditText) findViewById(R.id.edt_uid);
        edt_uid.setText(this.camera.getUid());
        getRemoteData();
    }

    void getRemoteData() {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
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

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP:
                    dismissLoadingProgress();

                    byte[] bytModel = new byte[16];
                    byte[] bytVender = new byte[16];
                    System.arraycopy(data, 0, bytModel, 0, 16);
                    System.arraycopy(data, 16, bytVender, 0, 16);

                    String model = TwsTools.getString(bytModel);
                    String vender = TwsTools.getString(bytVender);
                    int version = Packet.byteArrayToInt_Little(data, 32);
                    //edt_mode.setText(model);
                    String strBinaryVersion = Integer.toBinaryString(version);
                    while (strBinaryVersion.length() < 32) {
                        strBinaryVersion = "0" + strBinaryVersion;
                    }
                    String strVersion = "";
                    for (int i = 0; i < 4; i++) {
                        strVersion += Integer.parseInt(strBinaryVersion.substring(i * 8, i * 8 + 8), 2) + "";
                        if (i != 3) {
                            strVersion += ".";
                        }
                    }
//                    if(vender.toUpperCase().equalsIgnoreCase("FB")) {
//                        edt_version.setText(camera.getSystemTypeVersion() + "." + camera.getVendorTypeVersion() + "." + camera.getCustomTypeVersion());
//                    }
//                    else{
                        edt_version.setText(strVersion);
                    //}+"/"+camera.getSystemTypeVersion() + "." + camera.getVendorTypeVersion() + "." + camera.getCustomTypeVersion()
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP:
                    getRemoteData();
                    break;

            }
            super.handleMessage(msg);
        }
    };

}
