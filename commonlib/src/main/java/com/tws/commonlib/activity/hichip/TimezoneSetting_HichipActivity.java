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
import com.hichip.sdk.HiChipP2P;
import com.hichip.system.HiDefaultData;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.adapter.TimezoneItemListAdapter;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import java.util.Date;
import java.util.TimeZone;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class TimezoneSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private HichipCamera camera;
    private TimezoneItemListAdapter adapter;
    private int enableDst;
    private boolean mIsSupportZoneExt;
    private int preIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timezone_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
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
            if (mIsSupportZoneExt) {
                int desMode = 0;
                if (HiDefaultData.TimeZoneField1[nId][2] == "0") {
                    desMode = 0;
                }
                else if(preIndex == nId){
                    desMode = enableDst;
                }
                else {
                    desMode = TimeZone.getDefault().inDaylightTime(new Date())?1:0;
                }
                byte[] byte_time = HiDefaultData.TimeZoneField1[nId][0].getBytes();

                if (byte_time.length <= 32) {
                    camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT,
                            HiChipDefines.HI_P2P_S_TIME_ZONE_EXT.parseContent(byte_time, desMode));
                }
            } else {
                int desMode = 0;

                int tz = HiDefaultData.TimeZoneField[nId][0];

                if (HiDefaultData.TimeZoneField[nId][1] == 0) {
                    desMode = 0;
                }
                else if(preIndex == nId){
                    desMode = enableDst;
                }
                else {
                    desMode = TimeZone.getDefault().inDaylightTime(new Date())?1:0;
                }
                camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_ZONE, HiChipDefines.HI_P2P_S_TIME_ZONE.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, tz, desMode));
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();
        mIsSupportZoneExt = camera.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT);

        String[] sourceList = null;
        if (mIsSupportZoneExt) {// 支持新时区
            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
            sourceList = getResources().getStringArray(R.array.device_timezone_new);
        } else {
            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
            sourceList = getResources().getStringArray(R.array.device_timezone_old);
        }
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
        preIndex= this.getIntent().getIntExtra("timezone", -999);
        if (preIndex != -999) {
            adapter.setPos(preIndex);
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

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT:
                case HiChipDefines.HI_P2P_GET_TIME_ZONE:
                    dismissLoadingProgress();
                    break;
                case HiChipDefines.HI_P2P_SET_TIME_ZONE_EXT:
                case HiChipDefines.HI_P2P_SET_TIME_ZONE:
                    camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REBOOT, new byte[0]);
                    break;
                case HiChipDefines.HI_P2P_SET_REBOOT:
                    dismissLoadingProgress();
                    TimezoneSetting_HichipActivity.this.back2Activity(MainActivity.class);
                    break;

            }
            super.handleMessage(msg);
        }
    };
}
