package com.tws.commonlib.activity.setting;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.bean.TwsIOCTRLDEFs;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class ModifyCameraPasswordActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    EditText edit_cameraOldPassword;
    EditText edit_cameraNewPassword;
    EditText edit_cameraConfirmPassword;
    String newPassword;
    private static final int Reconnect = 0xC1;
    boolean isModifying = false;
    int maxRetryCount = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modify_camerapassword);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setTitle(getString(R.string.title_modify_camera_password));
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        maxRetryCount = 5;
                        save();
                        break;
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        finish();
                        break;
                }
            }
        });
        edit_cameraOldPassword = (EditText) findViewById(R.id.edit_cameraOldPassword);
        edit_cameraNewPassword = (EditText) findViewById(R.id.edit_cameraNewPassword);
        edit_cameraConfirmPassword = (EditText) findViewById(R.id.edit_cameraConfirmPassword);
        if (!camera.getPassword().equals(IMyCamera.DEFAULT_PASSWORD) && MyConfig.isStrictPwd()) {
            edit_cameraOldPassword.requestFocus();
            title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        } else {
            edit_cameraOldPassword.setText(IMyCamera.DEFAULT_PASSWORD);
            edit_cameraNewPassword.requestFocus();
        }
        ((CheckBox) findViewById(R.id.cb_showpassword)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                if (check) {
                    //��ʾ����
                    ((EditText) findViewById(R.id.edit_cameraOldPassword)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ((EditText) findViewById(R.id.edit_cameraNewPassword)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ((EditText) findViewById(R.id.edit_cameraConfirmPassword)).setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    ((EditText) findViewById(R.id.edit_cameraOldPassword)).setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ((EditText) findViewById(R.id.edit_cameraNewPassword)).setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ((EditText) findViewById(R.id.edit_cameraConfirmPassword)).setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (camera.getPassword().equalsIgnoreCase(IMyCamera.DEFAULT_PASSWORD) && MyConfig.isStrictPwd()) {
                return true;
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    boolean save() {
        String oldPwd = edit_cameraOldPassword.getText().toString();
        String newPwd = edit_cameraNewPassword.getText().toString();
        String confirmPwd = edit_cameraConfirmPassword.getText().toString();
        //tips_all_field_can_not_empty">请输入所有选项。
        if (oldPwd.length() == 0 || newPwd.length() == 0 || confirmPwd.length() == 0) {
            showAlert(getText(R.string.alert_all_field_can_not_empty).toString());
            return false;
        }

        //tips_old_password_is_wrong">旧密码输入错误
        if (!oldPwd.equals(camera.getPassword())) {
            showAlert(getText(R.string.alert_old_password_is_wrong).toString());
            return false;
        }

        //tips_new_passwords_do_not_match">两次输入的新密码不一致。
        if (!newPwd.equals(confirmPwd)) {
            showAlert(getText(R.string.alert_new_passwords_do_not_match).toString());
            return false;
        } else if (newPwd.equals(oldPwd)) {//新旧密码一致
            showAlert(getText(R.string.alert_old_new_password_same).toString());
            return false;
        }
        //密码的长度为6-12位
        if (newPwd.length() > 12 || newPwd.length() < 6) {
            showAlert(getText(R.string.alert_modify_pwd_pwdstring_format_error).toString());
            return false;
        }

        if (true || TwsTools.isUserPwdLegal(newPwd)) {
            if (camera != null) {
                newPassword = newPwd;
                if (camera.getP2PType() == IMyCamera.CameraP2PType.HichipP2P) {
                    byte[] old_auth = HiChipDefines.HI_P2P_S_AUTH.parseContent(0, camera.getAccount(), camera.getPassword());
                    byte[] new_auth = HiChipDefines.HI_P2P_S_AUTH.parseContent(0, camera.getAccount(), newPassword);
                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_SET_USER_PARAM, HiChipDefines.HI_P2P_SET_AUTH.parseContent(new_auth, old_auth));
                } else {
                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetPasswdReq.parseContent(oldPwd, newPwd));
                }
                isModifying = true;
                if (maxRetryCount > 0) {
                    handler.sendEmptyMessageDelayed(Reconnect, 10000);
                }
                maxRetryCount--;
            }

            showLoadingProgress(getText(R.string.modify).toString(), true, 60000, new TwsProgressDialog.OnTimeOutListener() {
                @Override
                public void onTimeOut(TwsProgressDialog dialog) {
                    TwsToast.showToast(ModifyCameraPasswordActivity.this, getText(R.string.alert_modify_security_code_failed).toString());
                    camera.asyncStop(null);
                    ModifyCameraPasswordActivity.this.finish();
                }
            });
        } else {
            showAlert(getText(R.string.alert_modify_pwd_pwdstring_format_error).toString());
            //textView.setText("密码包含非法字符");
            return false;
        }
        return true;
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
        handler.sendMessage(msg);
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

    public void doClickLL(View view) {
        ((LinearLayout) view).getChildAt(1).requestFocus();
    }


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {

                case TwsIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_RESP://设置密码返回值
                    dismissLoadingProgress();
                    if ((camera.getP2PType() == IMyCamera.CameraP2PType.HichipP2P && msg.arg1 == 0) || camera.getP2PType() == IMyCamera.CameraP2PType.TutkP2P && data[0] == 0x00) {

                        camera.setPassword(newPassword);
                        camera.sync2Db(ModifyCameraPasswordActivity.this);
                        Toast.makeText(ModifyCameraPasswordActivity.this, getText(R.string.tips_modify_security_code_succ).toString(), Toast.LENGTH_SHORT).show();
                        camera.stop();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.start();
                            }
                        }, 1000);
                        back2Activity(MainActivity.class);

                    } else {
                        isModifying = false;
                        showAlert(getText(R.string.alert_modify_security_code_failed).toString());
                    }

                    break;
                case NSCamera.CONNECTION_STATE_CONNECTED:
                    if (isModifying) {
                        save();
                    }
                    break;
                case Reconnect:
                    camera.asyncStop(new IMyCamera.TaskExecute() {
                        @Override
                        public void onPosted(IMyCamera c, Object data) {
                            c.start();
                        }
                    });
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
