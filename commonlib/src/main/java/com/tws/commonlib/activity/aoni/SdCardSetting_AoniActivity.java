package com.tws.commonlib.activity.aoni;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class SdCardSetting_AoniActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    EditText edt_total_size;
    EditText edt_available_size;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
;
        setContentView(R.layout.activity_sdcard_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_camera_setting_sdcard));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        finish();
                        break;
                }
            }
        });
        edt_total_size = (EditText)findViewById(R.id.edt_total_size);
        edt_available_size = (EditText)findViewById(R.id.edt_available_size);
        getRemoteData();
    }

    void getRemoteData() {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_REQ , AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
        }
    }

   public void formatSdCard(View view) {
       showYesNoDialog(R.string.dialog_msg_format_sd_card, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               switch (which) {
                   case DialogInterface.BUTTON_POSITIVE:
                       // Yes button clicked
                       if (camera != null) {
                           camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_REQ, AVIOCTRLDEFs.SMsgAVIoctrlFormatExtStorageReq.parseContent(0));
                       }
                       showLoadingProgress(getString(R.string.process_formating));
                       break;
                   case DialogInterface.BUTTON_NEGATIVE:
                       // No button clicked

                       break;
               }
           }
       });
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

                    int mTotalSize = Packet.byteArrayToInt_Little(data, 40);
                    int free = Packet.byteArrayToInt_Little(data, 44);
                    edt_available_size.setText(String.valueOf(free) + " MB");
                    edt_total_size.setText(String.valueOf(mTotalSize) + " MB");
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP:
                    if(camera.getSupplier()== IMyCamera.Supllier.AN){
                        getFormatStatus();
                    }
                    else {
                        getRemoteData();
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FORAMT_RESULT_RESP:
                    //0:格式化完毕；1:格式化中；-1:格式化失败；
                    int result = Packet.byteArrayToInt_Little(data,0);
                    if(result == 0){
                        getRemoteData();
                        TwsToast.showToast(SdCardSetting_AoniActivity.this,getString(R.string.toast_fotmat_succ));
                    }
                    else if(result == 1){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getFormatStatus();
                            }
                        }, 5000);
                    }
                    else{
                        getRemoteData();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void getFormatStatus(){
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FORAMT_RESULT_REQ , AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
        }
    }

    public void  clickLine(View view){

    }

}
