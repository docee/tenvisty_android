package com.tws.commonlib.activity.aoni;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.ItemListAdapter;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class SensitivitySetting_AoniActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    private ItemListAdapter adapter;
    private int setPos;
    private boolean hasPIR = false;
    public static final int SENSITIVITY_SET_SUCC = 0xB1;
    int sensLevel = -1;
    int armEnable = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensitivity_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
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
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ARM_STATUS_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
        }
    }

    void setSensitivity(int level) {
        showLoadingProgress();
        sensLevel = level;
        if (camera != null) {
            Log.i("Sensi11111111", TwsDataValue.SensValues[4 - sensLevel] + "set");
            //关闭PIR
            if (level == 0) {
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ARM_STATUS_REQ, AVIOCTRLDEFs.SMsgArmEnable.parseContent(false));
            } else {
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ARM_STATUS_REQ, AVIOCTRLDEFs.SMsgArmEnable.parseContent(true));
            }
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
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PIR_SENSITIVITY_RESP:
                    dismissLoadingProgress();
                    int sensitivity = Packet.byteArrayToInt_Little(data, 0);
                    int l = 0;
                    Log.i("Sensi11111111", sensitivity + "get");
                    for (int i = 0; i < TwsDataValue.SensValues.length; i++) {
                        if (sensitivity >= TwsDataValue.SensValues[i]) {
                            l = i;
                            break;
                        }
                    }
                    if (l == TwsDataValue.SensValues.length - 1) {
                        l--;
                    }
                    adapter.setPos(l);
                    adapter.notifyDataSetChanged();
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_ARM_STATUS_RESP:
                    armEnable = Packet.byteArrayToInt_Little(data, 0);
                    if (armEnable == 0) {
                        dismissLoadingProgress();
                        adapter.setPos(adapter.getCount() - 1);
                        adapter.notifyDataSetChanged();
                    } else {
                        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetMotionDetectReq.parseContent(0));
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_PIR_SENSITIVITY_RESP: {
                    int result = Packet.byteArrayToInt_Little(data, 0);
                    if (result == 0) {
                        dismissLoadingProgress();
                        Intent intent = new Intent();
                        intent.putExtra("data", SensitivitySetting_AoniActivity.this.sensLevel);
                        setResult(SENSITIVITY_SET_SUCC, intent);
                        SensitivitySetting_AoniActivity.this.finish();
                    } else {
                        searchSensitivity();
                    }
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_ARM_STATUS_RESP:
                    int result = Packet.byteArrayToInt_Little(data, 0);
                    if (result == 0) {
                        if (sensLevel == 0) {
                            dismissLoadingProgress();
                            Intent intent = new Intent();
                            intent.putExtra("data", SensitivitySetting_AoniActivity.this.sensLevel);
                            setResult(SENSITIVITY_SET_SUCC, intent);
                            SensitivitySetting_AoniActivity.this.finish();
                        } else {
                            int sens = TwsDataValue.SensValues[4 - sensLevel];
                            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_PIR_SENSITIVITY_REQ, AVIOCTRLDEFs.SMsgPirSensitivity.parseContent(sens));
                        }
                    } else {
                        searchSensitivity();
                    }
                    break;


            }
            super.handleMessage(msg);
        }
    };

}
