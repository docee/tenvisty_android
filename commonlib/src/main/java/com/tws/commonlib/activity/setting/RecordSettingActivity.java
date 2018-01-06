package com.tws.commonlib.activity.setting;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.ItemListAdapter;
import com.tws.commonlib.bean.CustomContentBean;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.util.ArrayList;
import java.util.List;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class RecordSettingActivity extends BaseActivity implements IRegisterIOTCListener {

    private String dev_uid;
    private ItemListAdapter adapter;
    private int setType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (MyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.uid.equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                camera.registerIOTCListener(this);
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_camera_setting_record));
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        String[] sourceList = getResources().getStringArray(R.array.record_type);
        adapter = new ItemListAdapter(this, sourceList);
        ListView listview_itemlist = (ListView) findViewById(R.id.listview_itemlist);
        listview_itemlist.setAdapter(adapter);

        listview_itemlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setPos(position);
                adapter.notifyDataSetChanged();
                setRecordSetting(position);
            }
        });
        getRecordSetting();
    }

    void getRecordSetting() {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
        }
    }

    void setRecordSetting(int type) {
        setType = type;
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRECORD_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetRecordReq.parseContent(0, setType));
        }
    }

    public void doClickLL(View view) {

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

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETRECORD_RESP:
                    dismissLoadingProgress();
                    int recordType = Packet.byteArrayToInt_Little(data, 4);
                    if (adapter.getCount() > recordType) {
                        adapter.setPos(recordType);
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETRECORD_RESP:
                    if (data[0] == 0x00) {
                        dismissLoadingProgress();
                        Intent intent = new Intent();
                        intent.putExtra("data", RecordSettingActivity.this.setType);
                        setResult(RESULT_OK, intent);
                        RecordSettingActivity.this.finish();
                    } else {
                        getRecordSetting();
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
