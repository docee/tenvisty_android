package com.tws.commonlib.activity.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class SystemSettingActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    private static final int UPDATE_GET_INFO = 0x101;
    String accSystemTypeVersion = null;
    String accCustomTypeVersion = null;
    String accVendorTypeVersion = null;
    boolean isUpdating = false;
    int updateState = -1;
    int resetState = -1;
    int rebootState = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_camera_setting_system));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
    }


    public void doClickLL(View view) {

    }


    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {
        Message msg = new Message();
        msg.what = resultCode;
       // sessionHandler.sendMessage(msg);
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
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_RESP: {
                    AVIOCTRLDEFs.SMsgAVIoctrlRusultResp resp = new AVIOCTRLDEFs.SMsgAVIoctrlRusultResp(data);
                    if (resp.result == 0) {
                        rebootState = 1;
                        dismissLoadingProgress();
                        back2Activity(MainActivity.class);
                    } else {
                        dismissLoadingProgress();
                        showAlert(getString(R.string.alert_reboot_fail));
                    }
                    break;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RESET_DEFAULT_RESP: {
                    AVIOCTRLDEFs.SMsgAVIoctrlRusultResp resp = new AVIOCTRLDEFs.SMsgAVIoctrlRusultResp(data);
                    if (resp.result == 0) {
                        resetState = 1;
                        back2Activity(MainActivity.class);
                    } else {
                        dismissLoadingProgress();
                        showAlert(getString(R.string.alert_reset_fail));
                    }
                    break;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_UPRADE_URL_RESP: {
                    AVIOCTRLDEFs.SMsgAVIoctrlGetUpgradeResp updateInfo = new AVIOCTRLDEFs.SMsgAVIoctrlGetUpgradeResp(data);
                    if (updateInfo.localUrl != null) {
                        String localUrl = TwsTools.getString(updateInfo.localUrl);
                        String upgradeUrl = TwsTools.getString(updateInfo.upgradeUrl);
                        String systemType = TwsTools.getString(updateInfo.systemType);
                        String customType = TwsTools.getString(updateInfo.customType);
                        String vendorType = TwsTools.getString(updateInfo.vendorType);
                        ThreadHttpResqust request = new ThreadHttpResqust(localUrl, upgradeUrl, systemType, customType, vendorType);
                        request.start();
                    }
                    break;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPGRADE_STATUS: {
                    updateState = 2;
                    AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus process = new AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus(data);
                    if (process.p >= 100) {
                        updateState = 3;
                        refreshProgressTest(getString(R.string.dialog_msg_new_firmware_updating_succ_reboot));
                    } else {
                        refreshProgressTest(getString(R.string.dialog_msg_new_firmware_updating) + " " + process.p + "%");
                    }
                    break;
                }
                case UPDATE_GET_INFO: {
                    dismissLoadingProgress();
                    if (msg.arg1 == 0) {
                        String systemType = bundle.getString("systemType");
                        String customType = bundle.getString("customType");
                        String vendorType = bundle.getString("vendorType");
                        final int netType = bundle.getInt("netType");
                        try {
                            JSONObject systemTypeJson = new JSONObject(systemType);
                            JSONObject customTypeJson = new JSONObject(customType);
                            JSONObject vendorTypeJson = new JSONObject(vendorType);
                            JSONArray systemTypeDirect = systemTypeJson.getJSONArray("Direct");
                            JSONArray customTypeDirect = customTypeJson.getJSONArray("Direct");
                            JSONArray vendorTypeDirect = vendorTypeJson.getJSONArray("Direct");
                            if (systemTypeDirect == null || customTypeDirect == null || vendorTypeDirect == null ||
                                    systemTypeDirect.length() == 0 || customTypeDirect.length() == 0 || vendorTypeDirect.length() == 0) {
                                showAlert(getString(R.string.dialog_msg_new_firmware_already_latest));
                                return;
                            }
                            final String systemTypeVersion = systemTypeDirect.getJSONObject(0).getString("Version");
                            final String systemCheck = systemTypeDirect.getJSONObject(0).getString("SystemCheck");
                            final String webCheck = systemTypeDirect.getJSONObject(0).getString("WebCheck");
                            final String usrCheck = systemTypeDirect.getJSONObject(0).getString("UsrCheck");
                            final String customTypeVersion = customTypeDirect.getJSONObject(0).getString("Version");
                            final String customTypeCheck = customTypeDirect.getJSONObject(0).getString("CustomCheck");
                            final String vendorTypeVersion = vendorTypeDirect.getJSONObject(0).getString("Version");
                            final String vendorTypeCheck = vendorTypeDirect.getJSONObject(0).getString("VendorCheck");
                            if (systemTypeVersion.compareTo(accSystemTypeVersion) > 0 || customTypeVersion.compareTo(accCustomTypeVersion) > 0 || vendorTypeVersion.compareTo(accVendorTypeVersion) > 0) {
                                showYesNoDialog(R.string.dialog_msg_new_firmware,R.string.prompt, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                showLoadingProgress(getString(R.string.dialog_msg_new_firmware_updating), false, 240000, new TwsProgressDialog.OnTimeOutListener() {
                                                    @Override
                                                    public void onTimeOut(TwsProgressDialog dialog) {

                                                    }
                                                });
                                                byte[] reqData = AVIOCTRLDEFs.SMsgAVIoctrlSetUpgradeReq.parseContent(netType, AVIOCTRLDEFs.SMsgAVIoctrlSystemDatInfo.parseContent(systemTypeVersion, usrCheck, systemCheck, webCheck), AVIOCTRLDEFs.SMsgAVIoctrlCustomDatInfo.parseContent(customTypeVersion, customTypeCheck), AVIOCTRLDEFs.SMsgAVIoctrlVendorDatInfo.parseContent(vendorTypeVersion, vendorTypeCheck));
                                                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_UPRADE_REQ, reqData);
                                                break;
                                        }
                                    }
                                });
                            } else {
                                showAlert(getString(R.string.dialog_msg_new_firmware_already_latest));
                            }
                        } catch (Exception e) {
                            showAlert(getString(R.string.tips_update_getinfo_fail));
                            e.printStackTrace();
                        }
                    } else {
                        showAlert(getString(R.string.tips_update_getinfo_fail));
                    }
                }
                break;
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_RESP: {
                    if (data != null && data.length > 0) {
                        String firmver = TwsTools.getString(data);
                        String[] arrFirm = firmver.split("\\.");
                        if (arrFirm.length >= 5) {
                            accCustomTypeVersion = arrFirm[0];
                            accVendorTypeVersion = arrFirm[1];
                            accSystemTypeVersion = arrFirm[2] + "." + arrFirm[3] + "." + arrFirm[4];

                        }
                    }

                    if (updateState != 0) {
                        return;
                    }
                    updateState = 1;
                    if (accSystemTypeVersion != null) {
                        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_UPRADE_URL_REQ, new byte[0]);
                    } else {
                        showAlert(getString(R.string.dialog_msg_new_firmware_getaccinfo_failed));
                        dismissLoadingProgress();
                    }
                    break;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_UPRADE_RESP: {
                    if (data[0] != 0) {
                        dismissLoadingProgress();
                        showAlert(getString(R.string.dialog_msg_new_firmware_updating_fail));
                    }
                    else{
                        updateState = 1;
                        dismissLoadingProgress();
                        back2Activity(MainActivity.class);
                    }
                    break;
                }
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
        dismissLoadingProgress();
    }

    public void goSetting(View view) {
        Intent intent = new Intent();
        intent.putExtras(this.getIntent());
        if (view.getId() == R.id.ll_system_reset) {
            //复位
            showYesNoDialog(R.string.dialog_msg_restore_factory_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            showLoadingProgress(getString(R.string.process_reseting));
                            // Yes button clicked
                            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RESET_DEFAULT_REQ, new byte[0]);
                            resetState = 0;
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            });
        } else if (view.getId() == R.id.ll_system_reboot) {
            //重启
            showYesNoDialog(R.string.dialog_msg_reboot_camera, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            showLoadingProgress(getString(R.string.process_rebooting));
                            rebootState = 0;
                            // Yes button clicked
                            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_REBOOT_REQ, new byte[0]);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            // No button clicked
                            break;
                    }
                }
            });
        } else if (view.getId() == R.id.ll_system_update) {
            updateState = 0;
            //升级固件
            showLoadingProgress(getString(R.string.process_upgrading_check), true, 90000, new TwsProgressDialog.OnTimeOutListener() {
                @Override
                public void onTimeOut(TwsProgressDialog dialog) {
                    updateState = -1;
                    TwsToast.showToast(SystemSettingActivity.this,getString(R.string.process_connect_timeout));
                }
            });
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_REQ, new byte[1]);
        }
    }

    public String getFirmInfo(String url) {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = null;
        String result = null;
        try {
            httpResponse = new DefaultHttpClient().execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                try {
                    result = EntityUtils.toString(httpResponse.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class ThreadHttpResqust extends Thread {
        String localUrl;
        String remoteUrl;
        String systemType;
        String customType;
        String vendorType;

        public ThreadHttpResqust(String _localUrl, String _remoteUrl, String _systemType, String _customType, String _vendorType) {
            this.localUrl = _localUrl;
            this.remoteUrl = _remoteUrl;
            this.systemType = _systemType;
            this.customType = _customType;
            this.vendorType = _vendorType;
        }

        public void run() {

            Message msg = new Message();
            msg.what = UPDATE_GET_INFO;
            try {

                String systemTypeResp = null;
                String customTypeResp = null;
                String vendorTypeResp = null;
                int netType = 1;

                //内网接口
                if (systemTypeResp != null) {
                    systemTypeResp = getFirmInfo(localUrl + systemType + "msg.json");
                    customTypeResp = getFirmInfo(localUrl + customType + "msg.json");
                    vendorTypeResp = getFirmInfo(localUrl + vendorType + "msg.json");
                }
                //外网接口
                else {
                    netType = 0;
                    systemTypeResp = getFirmInfo(remoteUrl + systemType + "msg.json");
                    customTypeResp = getFirmInfo(remoteUrl + customType + "msg.json");
                    vendorTypeResp = getFirmInfo(remoteUrl + vendorType + "msg.json");
                }

                Bundle bundle = new Bundle();
                bundle.putString("systemType", systemTypeResp);
                bundle.putString("customType", customTypeResp);
                bundle.putString("vendorType", vendorTypeResp);
                bundle.putInt("netType", netType);
                msg.setData(bundle);
                msg.arg1 = 0;

            } catch (Exception e) {
                msg.arg1 = -1;
            }
            handler.sendMessage(msg);
            /**        HiCam最后版本           **/


        }
    }
}
