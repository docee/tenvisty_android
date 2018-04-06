package com.tws.commonlib.activity.hichip;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.hichip.base.HiLog;
import com.hichip.content.HiChipDefines;
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
public class SdCardSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    EditText edt_total_size;
    EditText edt_available_size;
    private boolean isCheckingFormatResult = false;
    private int storage = -1;
    private static  final  int CheckingFormatResult = 0x66;
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
            camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_SD_INFO, new byte[0]);
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
                           camera.sendIOCtrl(0,HiChipDefines.HI_P2P_SET_FORMAT_SD,null);
                       }
                       showLoadingProgress();
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
    public  void onPause(){
        super.onPause();
        if(handler.hasMessages(CheckingFormatResult)) {
            handler.removeMessages(CheckingFormatResult);
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
                case HiChipDefines.HI_P2P_GET_SD_INFO:
                    HiChipDefines.HI_P2P_S_SD_INFO sd_info = new HiChipDefines.HI_P2P_S_SD_INFO(data);
                    HiLog.v("u32Space"+ sd_info.u32Space+",u32LeftSpace:"+sd_info.u32LeftSpace+", storage:"+storage+", isCheckingFormatResult:"+isCheckingFormatResult);
                    int used = sd_info.u32Space - sd_info.u32LeftSpace;
                    if(storage < 0 || sd_info.u32Space !=0){
                        edt_available_size.setText(String.valueOf(sd_info.u32LeftSpace/1024) + " MB");
                        edt_total_size.setText(String.valueOf(sd_info.u32Space/1024) + " MB");
                        dismissLoadingProgress();
                        if(isCheckingFormatResult){
                            isCheckingFormatResult= false;
                            TwsToast.showToast(SdCardSetting_HichipActivity.this, getString(R.string.toast_fotmat_succ));
                            //finish();
                        }
                    }
                    if(storage <= 0){
                        storage = sd_info.u32Space;
                    }
                    //��ʽ��SD��ʱ��ȡ����SD��������0����ʱ���»�ȡSD����Ϣ
                    if(sd_info.u32LeftSpace==0&&sd_info.u32Space==0 && storage>0){
                        getRemoteData();
                    }
                    break;
                case HiChipDefines.HI_P2P_SET_FORMAT_SD:
                    if(storage > 0){
                        isCheckingFormatResult = true;
                        this.sendEmptyMessageDelayed(CheckingFormatResult,5000);
                    }
                    else{
                        TwsToast.showToast(SdCardSetting_HichipActivity.this, getString(R.string.toast_fotmat_succ));
                        dismissLoadingProgress();
                    }
                    break;
                case CheckingFormatResult:
                    getRemoteData();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void  clickLine(View view){

    }

}
