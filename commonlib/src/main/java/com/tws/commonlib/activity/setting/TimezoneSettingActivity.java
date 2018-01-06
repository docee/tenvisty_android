package com.tws.commonlib.activity.setting;

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
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.TimezoneItemListAdapter;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimezoneSettingActivity extends BaseActivity implements IRegisterIOTCListener {

    private String dev_uid;
    private MyCamera camera;
    private TimezoneItemListAdapter adapter;
    private int enableDst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timezone_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (NSCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.uid.equalsIgnoreCase(dev_uid)) {
                camera = (MyCamera) _camera;
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
            String[] timezoneInfo = TwsDataValue.TimeZoneField[nId];
            byte[] data = AVIOCTRLDEFs.SMsgAVIoctrlSetTZoneReq.parseContent(timezoneInfo[0],enableDst);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ZONE_INFO_REQ, data);
        }
    }

    @Override
    protected void initView() {
        super.initView();
        String[] sourceList = getResources().getStringArray(R.array.device_timezone_new);
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
        enableDst = this.getIntent().getIntExtra("dst",0);
        int pos = this.getIntent().getIntExtra("timezone", -999);
        if (pos != -999) {
            adapter.setPos(pos);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void receiveFrameData(NSCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(NSCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(NSCamera camera, int resultCode) {

    }

    @Override
    public void receiveChannelInfo(NSCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(NSCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void initSendAudio(Camera paramCamera, boolean paramBoolean) {

    }

    @Override
    public void receiveOriginalFrameData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {

    }

    @Override
    public void receiveRGBData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {

    }

    @Override
    public void receiveRecordingData(Camera paramCamera, int avChannel, int paramInt1, String path) {

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ZONE_INFO_RESP:
                    dismissLoadingProgress();
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ZONE_INFO_RESP:
                    if (data[3] == 0) {
                        dismissLoadingProgress();
                        setResult(RESULT_OK, new Intent().putExtra("timezone", adapter.getPos()));
                        TimezoneSettingActivity.this.finish();
                    } else {
                        showAlert(getString(R.string.alert_setting_fail));
                    }
                    break;

            }
            super.handleMessage(msg);
        }
    };
}
