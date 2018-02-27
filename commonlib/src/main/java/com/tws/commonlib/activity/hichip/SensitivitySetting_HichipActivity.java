package com.tws.commonlib.activity.hichip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.ItemListAdapter;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.bean.TwsIOCTRLDEFs;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class SensitivitySetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    private ItemListAdapter adapter;
    private int setPos;
    private boolean hasPIR = false;
    public static final int SENSITIVITY_SET_SUCC = 0xB1;
    int sensLevel;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param2 = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param3 = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param4 = null;

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
                setSensitivity(TwsDataValue.SensValues.length - 1 - position);
            }
        });
        //adapter.notifyDataSetChanged();
        searchSensitivity();
    }

    void searchSensitivity() {
        showLoadingProgress();
        if (camera != null) {
            HiChipDefines.HI_P2P_S_MD_PARAM mdparam2 = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0,new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_2,0,0,0,0,0,0)
            );
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam2.parseContent());

            HiChipDefines.HI_P2P_S_MD_PARAM mdparam3 = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0,new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_3,0,0,0,0,0,0)
            );
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam3.parseContent());

            HiChipDefines.HI_P2P_S_MD_PARAM mdparam4 = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0,new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_4,0,0,0,0,0,0)
            );
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam4.parseContent());


            HiChipDefines.HI_P2P_S_MD_PARAM mdparam = new HiChipDefines.HI_P2P_S_MD_PARAM(
                    0,new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_1,0,0,0,0,0,0)
            );
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam.parseContent());
        }
    }

    void setSensitivity(int level) {
        showLoadingProgress();
        sensLevel = level;
        if (camera != null) {
            md_param.struArea.u32Sensi =  TwsDataValue.SensValues[TwsDataValue.SensValues.length-1-sensLevel];
            if(sensLevel == 0){
                md_param.struArea.u32Enable = 0;
            }
            else{
                md_param.struArea.u32Enable = 1;
            }
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_SET_MD_PARAM, md_param.parseContent());

            if(sensLevel == TwsDataValue.SensValues.length -1 && md_param2!= null) {
                md_param2.struArea.u32Enable = 0;
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_SET_MD_PARAM, md_param2.parseContent());
            }
            if(sensLevel == TwsDataValue.SensValues.length -1&& md_param3!= null) {
                md_param3.struArea.u32Enable = 0;
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_SET_MD_PARAM, md_param3.parseContent());
            }
            if(sensLevel == TwsDataValue.SensValues.length -1 && md_param4!= null) {
                md_param4.struArea.u32Enable = 0;
                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_SET_MD_PARAM, md_param4.parseContent());
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
        msg.arg1 = avChannel;
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
                case TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP:
                    dismissLoadingProgress();
                    HiChipDefines.HI_P2P_S_MD_PARAM md_param_temp = new HiChipDefines.HI_P2P_S_MD_PARAM(data);

                    if(md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_1)
                    {
                        md_param = md_param_temp;
                        if(md_param.struArea.u32Enable==0){
                            sensLevel = 0;
                        }
                        else{
                            int sensitivity = md_param.struArea.u32Sensi;
                            for(int i=0;i<TwsDataValue.SensValues.length;i++){
                                if(sensitivity >= TwsDataValue.SensValues[i]){
                                    sensLevel = TwsDataValue.SensValues.length - 1 - i;
                                    break;
                                }
                            }
                        }

                        adapter.setPos(TwsDataValue.SensValues.length - 1 - sensLevel);
                        adapter.notifyDataSetChanged();
                    }
                    else if(md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_2) {
                        md_param2 = md_param_temp;
                    }
                    else if(md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_3) {
                        md_param3 = md_param_temp;
                    }
                    else if(md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_4) {
                        md_param4 = md_param_temp;
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETMOTIONDETECT_RESP:
                    if (msg.arg1 == 0) {
                        dismissLoadingProgress();
                        Intent intent = new Intent();
                        intent.putExtra("data", SensitivitySetting_HichipActivity.this.sensLevel);
                        setResult(SENSITIVITY_SET_SUCC, intent);
                        SensitivitySetting_HichipActivity.this.finish();
                    } else {
                        searchSensitivity();
                    }
                    break;


            }
            super.handleMessage(msg);
        }
    };

}
