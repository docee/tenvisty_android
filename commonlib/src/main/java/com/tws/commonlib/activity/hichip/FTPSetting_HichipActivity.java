package com.tws.commonlib.activity.hichip;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.hichip.content.HiChipDefines;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class FTPSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private HichipCamera camera;
    private boolean isCheck = false;
    private EditText ftp_setting_server_edt, ftp_setting_port_edt, ftp_setting_username_edt,
            ftp_setting_psw_edt, ftp_setting_path_edt;
    private ToggleButton ftp_setting_mode_tgbtn;
    private HiChipDefines.HI_P2P_S_FTP_PARAM_EXT param;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ftp_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_camera_setting_ftp));
        camera.registerIOTCListener(this);
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        setFtpSetting(true);
                        break;
                }
            }
        });
        ftp_setting_server_edt = (EditText) findViewById(R.id.ftp_setting_server_edt);
        ftp_setting_port_edt = (EditText) findViewById(R.id.ftp_setting_port_edt);
        ftp_setting_username_edt = (EditText) findViewById(R.id.ftp_setting_username_edt);
        ftp_setting_psw_edt = (EditText) findViewById(R.id.ftp_setting_psw_edt);
        ftp_setting_path_edt = (EditText) findViewById(R.id.ftp_setting_path_edt);

        ftp_setting_mode_tgbtn = (ToggleButton) findViewById(R.id.ftp_setting_mode_tgbtn);

        getRemoteSetting();

    }

    void getRemoteSetting() {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT, null);
        }
    }

    void setFtpSetting(boolean check) {
        if (param != null) {

            showLoadingProgress(getString(R.string.process_setting));
            isCheck = check;
            String server = ftp_setting_server_edt.getText().toString();

            String port = ftp_setting_port_edt.getText().toString();

            String username = ftp_setting_username_edt.getText().toString();
            String psw = ftp_setting_psw_edt.getText().toString();
            String path = ftp_setting_path_edt.getText().toString();

            param.setStrSvr(server);
            try {
                param.u32Port = Integer.valueOf(port);
            } catch (java.lang.NumberFormatException ex) {
                param.u32Port = 21;
            }
            param.setStrUsernm(username);
            param.setStrPasswd(psw);
            param.setStrFilePath(path);
            param.u32Check = check ? 1 : 0;


            byte[] result = new byte[476];
            byte pos = 0;
            byte[] Channel = com.hichip.tools.Packet.intToByteArray_Little(param.u32Channel);
            System.arraycopy(Channel, 0, result, pos, 4);
            int pos1 = pos + 4;
            System.arraycopy(param.strSvr, 0, result, pos1, 64);
            pos1 += 64;
            byte[] Port = com.hichip.tools.Packet.intToByteArray_Little(param.u32Port);
            System.arraycopy(Port, 0, result, pos1, 4);
            pos1 += 4;
            byte[] Mode = com.hichip.tools.Packet.intToByteArray_Little(param.u32Mode);
            System.arraycopy(Mode, 0, result, pos1, 4);
            pos1 += 4;
            System.arraycopy(param.strUsernm, 0, result, pos1, 64);
            pos1 += 64;
            System.arraycopy(param.strPasswd, 0, result, pos1, 64);
            pos1 += 64;
            System.arraycopy(param.strFilePath, 0, result, pos1, 256);
            pos1 += 256;
            byte[] CreatePath = com.hichip.tools.Packet.intToByteArray_Little(param.u32CreatePath);
            System.arraycopy(CreatePath, 0, result, pos1, 4);
            pos1 += 4;
            byte[] Check = com.hichip.tools.Packet.intToByteArray_Little(param.u32Check);
            System.arraycopy(Check, 0, result, pos1, 4);
            pos1 += 4;
            System.arraycopy(param.strReserved, 0, result, pos1, 8);
            pos1 += 8;
            byte[] sendParam = result;//param.parseContent();
            camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT, sendParam);
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
        msg.arg1 = avChannel;
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

            if (msg.arg1 == 0) {
                switch (msg.what) {
                    case HiChipDefines.HI_P2P_GET_FTP_PARAM_EXT:
                        dismissLoadingProgress();
                        param = new HiChipDefines.HI_P2P_S_FTP_PARAM_EXT(data);
                        if (data.length >= 468) {
                            byte pos = 0;
                            param.u32Channel = com.hichip.tools.Packet.byteArrayToInt_Little(data, 0);
                            int pos1 = pos + 4;
                            System.arraycopy(data, pos1, param.strSvr, 0, 64);
                            pos1 += 64;
                            param.u32Port = com.hichip.tools.Packet.byteArrayToInt_Little(data, pos1);
                            pos1 += 4;
                            param.u32Mode = com.hichip.tools.Packet.byteArrayToInt_Little(data, pos1);
                            pos1 += 4;
                            System.arraycopy(data, pos1, param.strUsernm, 0, 64);
                            pos1 += 64;
                            System.arraycopy(data, pos1, param.strPasswd, 0, 64);
                            pos1 += 64;
                            System.arraycopy(data, pos1, param.strFilePath, 0, 256);
                            pos1 += 256;
                            param.u32CreatePath = com.hichip.tools.Packet.byteArrayToInt_Little(data, pos1);
                            pos1 += 4;
                            param.u32Check = com.hichip.tools.Packet.byteArrayToInt_Little(data, pos1);
                            pos1 += 4;
                            //System.arraycopy(data, pos1, param.strReserved, 0, 8);
                            pos1 += 8;
                        } else {

                        }
                        ftp_setting_server_edt.setText(com.hichip.tools.Packet.getString(param.strSvr));
                        ftp_setting_port_edt.setText(String.valueOf(param.u32Port));
                        ftp_setting_username_edt.setText(com.hichip.tools.Packet.getString(param.strUsernm));
                        ftp_setting_psw_edt.setText(com.hichip.tools.Packet.getString(param.strPasswd));
                        ftp_setting_path_edt.setText(com.hichip.tools.Packet.getString(param.strFilePath));

                        ftp_setting_mode_tgbtn.setChecked(param.u32Mode == 1);
                        break;
                    case HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT:
                        if (isCheck) {
                           setFtpSetting(false);
                        } else {
                            dismissLoadingProgress();
                            camera.unregisterIOTCListener(FTPSetting_HichipActivity.this);
                            FTPSetting_HichipActivity.this.finish();
                            TwsToast.showToast(FTPSetting_HichipActivity.this,
                                    getResources().getString(R.string.toast_setting_succ));
                        }

                        break;


                }
            } else {
                switch (msg.what) {
                    case HiChipDefines.HI_P2P_SET_FTP_PARAM_EXT:
                        if (!isCheck) {
                            dismissLoadingProgress();
                            showAlert(getString(R.string.alert_setting_fail));
                        } else {
                            showAlertnew(android.R.drawable.ic_dialog_alert, getString(R.string.warning), getString(R.string.dialog_msg_test_falied), getString(R.string.cancel), getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            setFtpSetting(false);
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            dismissLoadingProgress();
                                            break;
                                    }
                                }
                            });
                        }
                        break;


                }


            }


            super.handleMessage(msg);
        }
    };

    public void doClickLL(View view) {
        ((LinearLayout) view).getChildAt(1).performClick();
    }

}
