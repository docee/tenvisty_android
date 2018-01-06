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
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.ItemListAdapter;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class SensitivitySettingActivity extends BaseActivity implements IRegisterIOTCListener {

    private String dev_uid;
    private MyCamera camera;
    private ItemListAdapter adapter;
    private int setPos;
    private boolean hasPIR = false;
    public static final int SENSITIVITY_SET_SUCC = 0xB1;
    int sensLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensitivity_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (NSCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.uid.equalsIgnoreCase(dev_uid)) {
                camera = (MyCamera) _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_camera_setting_sens));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();

        String[] sourceList = getResources().getStringArray(R.array.motion_detection_sensitivity);
        adapter = new ItemListAdapter(this, sourceList);
        ListView listview_itemlist = (ListView) findViewById(R.id.listview_itemlist);
        listview_itemlist.setAdapter(adapter);

        listview_itemlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setPos(position);
                adapter.notifyDataSetChanged();
                setSensitivity(4 - position);
            }
        });
        //adapter.notifyDataSetChanged();
        searchSensitivity();
    }

    void searchSensitivity() {
        showLoadingProgress();
        if (camera != null) {
            // if(camera.wakeUp()<0) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            // }
            // else{
           // camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
            // }
        }
    }

    void setSensitivity(int level) {
        showLoadingProgress();
        sensLevel = level;
        if (camera != null) {
//            if (hasPIR) {
//                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_AUSDOM_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetMotionDetectReq.parseContent(0, level * 25 - 10));
//            }

            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETMOTIONDETECT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetMotionDetectReq.parseContent(0, TwsDataValue.SensValues[4-sensLevel]));

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
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_RESP: {
                    int pirSensitivity = Packet.byteArrayToInt_Little(data, 4);
                    hasPIR = true;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP:
                    dismissLoadingProgress();
                    int sensitivity = Packet.byteArrayToInt_Little(data, 4);
                    int l = 0;
                    for(int i=0;i<TwsDataValue.SensValues.length;i++){
                        if(sensitivity >= TwsDataValue.SensValues[i]){
                            l = i;
                            break;
                        }
                    }
                    adapter.setPos(l);
                    adapter.notifyDataSetChanged();
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_AUSDOM_PIR_SENSITIVITY_RESP: {
                    //TwsToast.showToast(SensitivitySettingActivity.this, "pir");
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETMOTIONDETECT_RESP:
                    if (data[0] == 0x00) {
                        dismissLoadingProgress();
                        Intent intent = new Intent();
                        intent.putExtra("data", SensitivitySettingActivity.this.sensLevel);
                        setResult(SENSITIVITY_SET_SUCC, intent);
                        SensitivitySettingActivity.this.finish();
                    } else {
                        searchSensitivity();
                    }
                    break;


            }
            super.handleMessage(msg);
        }
    };

}
