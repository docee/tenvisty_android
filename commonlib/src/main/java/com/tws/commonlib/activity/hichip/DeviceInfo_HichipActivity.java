package com.tws.commonlib.activity.hichip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class DeviceInfo_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private HichipCamera camera;
    EditText edt_mode;
    EditText edt_version;
    EditText edt_fmversion;
    EditText edt_network;
    EditText edt_ip;
    EditText edt_submask;
    EditText edt_gateway;
    EditText edt_dns;
    EditText edt_uid;
    LinearLayout ll_fmversion;
    ImageView img_splitline;
    private HiChipDefines.HI_P2P_GET_DEV_INFO_EXT deviceInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_info_hichip);

        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
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
        edt_fmversion = (EditText)findViewById(R.id.edt_fmversion);
        ll_fmversion = (LinearLayout)findViewById(R.id.ll_fmversion);
        img_splitline = (ImageView)findViewById(R.id.img_splitline);
        edt_network = (EditText) findViewById(R.id.edt_network);
        edt_ip = (EditText) findViewById(R.id.edt_ip);
        edt_submask = (EditText) findViewById(R.id.edt_submask);
        edt_dns = (EditText) findViewById(R.id.edt_dns);
        edt_gateway = (EditText) findViewById(R.id.edt_gateway);
        edt_uid = (EditText) findViewById(R.id.edt_uid);
        edt_uid.setText(this.camera.getUid());
        deviceInfo=camera.getDeciveInfo();
        if(deviceInfo!=null && deviceInfo.aszWebVersion[0] != 0) {
            String fwVersion = com.hichip.tools.Packet.getString(deviceInfo.aszWebVersion);
            edt_version.setText(fwVersion);
            edt_version.setText(com.hichip.tools.Packet.getString(deviceInfo.aszSystemSoftVersion));
            String state[]=getResources().getStringArray(R.array.net_work_style);
            edt_network.setText(state[deviceInfo.u32NetType]);
            setFirmVersionVisibility();
        }
        else{
            getRemoteData();
        }
        camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_NET_PARAM, new byte[0]);
    }

    void getRemoteData() {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_DEV_INFO_EXT, new byte[0]);
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

                case HiChipDefines.HI_P2P_GET_NET_PARAM:

                    HiChipDefines.HI_P2P_S_NET_PARAM net_param = new HiChipDefines.HI_P2P_S_NET_PARAM(data);
                    String ip = com.hichip.tools.Packet.getString(net_param.strIPAddr);
                    String mask= com.hichip.tools.Packet.getString(net_param.strNetMask);
                    String getway = com.hichip.tools.Packet.getString(net_param.strGateWay);
                    String dns= com.hichip.tools.Packet.getString(net_param.strFDNSIP);

                    edt_ip.setText(ip);
                    edt_submask.setText(mask);
                    edt_gateway.setText(getway);
                    edt_dns.setText(dns);

                    dismissLoadingProgress();
                    break;
                case HiChipDefines.HI_P2P_GET_DEV_INFO_EXT:
                    dismissLoadingProgress();

                    int pos = 0;
                    pos += 4;
                    pos += 32;
                    pos += 32;
                    pos += 4;
                    pos += HiChipDefines.HI_P2P_MAX_VERLENGTH;
                    pos += 32;
                    pos += HiChipDefines.HI_P2P_MAX_STRINGLENGTH;
                    pos += 4;
                    pos += HiChipDefines.HI_P2P_MAX_STRINGLENGTH;
                    pos += HiChipDefines.HI_P2P_MAX_STRINGLENGTH;
                    pos += 4;
                    pos += 4;
                    pos += 4;
                    if(deviceInfo == null) {
                        deviceInfo = new HiChipDefines.HI_P2P_GET_DEV_INFO_EXT(data);
                    }
                    System.arraycopy(data, pos, deviceInfo.aszWebVersion, 0, HiChipDefines.HI_P2P_MAX_VERLENGTH);
//                    if(vender.toUpperCase().equalsIgnoreCase("FB")) {
//                        edt_version.setText(camera.getSystemTypeVersion() + "." + camera.getVendorTypeVersion() + "." + camera.getCustomTypeVersion());
//                    }
//                    else{
                    String fwVersion = com.hichip.tools.Packet.getString(deviceInfo.aszWebVersion);
                        edt_version.setText(fwVersion);
                    String state[]=getResources().getStringArray(R.array.net_work_style);
                    edt_network.setText(state[deviceInfo.u32NetType]);
                    setFirmVersionVisibility();
                    //}+"/"+camera.getSystemTypeVersion() + "." + camera.getVendorTypeVersion() + "." + camera.getCustomTypeVersion()
                    break;

            }
            super.handleMessage(msg);
        }
    };

    private void setFirmVersionVisibility(){
        String fwVersion = com.hichip.tools.Packet.getString(deviceInfo.aszWebVersion);
        if(fwVersion == null || fwVersion.trim().length() == 0){
            ll_fmversion.setVisibility(View.GONE);
            img_splitline.setVisibility(View.GONE);
        }
        else{

            int iMainVerson = 0;
            String strMainVersion = fwVersion.substring(1, 3);
            try{
                iMainVerson = Integer.parseInt(strMainVersion);
                if(iMainVerson >= 16){
                    edt_fmversion.setText(fwVersion);
                    ll_fmversion.setVisibility(View.VISIBLE);
                    img_splitline.setVisibility(View.VISIBLE);
                }
                else{
                    ll_fmversion.setVisibility(View.GONE);
                    img_splitline.setVisibility(View.GONE);
                }
            }
            catch(Exception ex){
                ll_fmversion.setVisibility(View.GONE);
                img_splitline.setVisibility(View.GONE);
            }
        }
    }

}
