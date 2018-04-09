package com.tws.commonlib.activity.aoni;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.TimezoneItemListAdapter;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimezoneSetting_AoniActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    private TimezoneItemListAdapter adapter;
    private int enableDst;
    private int setTimezoneIndex = -1;
    AVIOCTRLDEFs.SMsgAVIoctrlNtpConfig ntpConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timezone_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_camera_setting_timezone));
        camera.registerIOTCListener(this);
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }


    void setRemoteData(int nId) {
        if (camera != null) {
            showLoadingProgress();
            setTimezoneIndex = nId;
            if (ntpConfig == null) {
                getRemoteData();
            } else {
                ntpConfig.TimeZone = (byte) (setTimezoneIndex + 1);
                byte[] data = ntpConfig.parseContent();
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_NTP_CONFIG_REQ, data);
            }
        }
    }

    void getRemoteData() {
        if (camera != null) {
            showLoadingProgress();
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_NTP_CONFIG_REQ, new byte[1]);
        }
    }

    @Override
    protected void initView() {
        super.initView();
        String[] sourceList = getResources().getStringArray(R.array.device_timezone_old);
        adapter = new TimezoneItemListAdapter(this, sourceList);
        ListView listview_itemlist = (ListView) findViewById(R.id.listview_itemlist);
        listview_itemlist.setAdapter(adapter);

        listview_itemlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setPos(position);
                setRemoteData(position);
            }
        });
        enableDst = this.getIntent().getIntExtra("dst", 0);
        int pos = this.getIntent().getIntExtra("timezone", -999);
        if (pos != -999) {
            adapter.setPos(pos);
        }
        adapter.notifyDataSetChanged();
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
                    if (Packet.byteArrayToInt_Little(data, 0) == 0) {
                        dismissLoadingProgress();
                        camera.unregisterIOTCListener(TimezoneSetting_AoniActivity.this);
                        setResult(RESULT_OK, new Intent().putExtra("timezone", adapter.getPos()));
                        TimezoneSetting_AoniActivity.this.finish();
                    } else {
                        showAlert(getString(R.string.alert_setting_fail));
                    }
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_NTP_CONFIG_RESP: {
                    ntpConfig = new AVIOCTRLDEFs.SMsgAVIoctrlNtpConfig(data);
                    if (setTimezoneIndex >= 0) {
                        setRemoteData(setTimezoneIndex);
                    }
                }
                break;

            }
            super.handleMessage(msg);
        }
    };
}
