//package com.tws.commonlib.activity;
//
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.view.KeyEvent;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//
//import com.tutk.IOTC.AVIOCTRLDEFs;
//import com.tutk.IOTC.Camera;
//import com.tutk.IOTC.IRegisterIOTCListener;
//import com.tutk.IOTC.NSCamera;
//import com.tws.commonlib.MainActivity;
//import com.tws.commonlib.R;
//import com.tws.commonlib.base.MyConfig;
//import com.tws.commonlib.base.TwsTools;
//import com.tws.commonlib.bean.MyCamera;
//import com.tws.commonlib.bean.TwsDataValue;
//import com.tws.commonlib.controller.NavigationBar;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
//
///**
// * 添加摄像机的界面
// *
// * @author Administrator
// */
//public class ModifyCameraPasswordActivity extends BaseActivity implements IRegisterIOTCListener {
//
//    private String dev_uid;
//    private MyCamera camera;
//    EditText edit_cameraOldPassword;
//    EditText edit_cameraNewPassword;
//    EditText edit_cameraConfirmPassword;
//    String newPassword;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_modify_camerapassword);
//        this.setTitle(getResources().getString(R.string.title_modify_camera_password));
//        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
//        for (NSCamera _camera : TwsDataValue.cameraList()) {
//            if (_camera.uid.equalsIgnoreCase(dev_uid)) {
//                camera = (MyCamera) _camera;
//                break;
//            }
//        }
//        initView();
//        camera.registerIOTCListener(this);
//    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if(camera.getPassword().equalsIgnoreCase(MyCamera.DEFAULT_PASSWORD) && MyConfig.isStrictPwd()) {
//                return true;
//            }
//
//        }
//
//        return super.onKeyDown(keyCode, event);
//    }
//    @Override
//  protected  void initView() {
//        if(!camera.getPassword().equalsIgnoreCase(MyCamera.DEFAULT_PASSWORD) || !MyConfig.isStrictPwd()){
//            super.initView();
//        }
//
//        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
//        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
//        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {
//
//            @Override
//            public void OnNavigationButtonClick(int which) {
//                switch (which) {
//                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
//                        save();
//                        break;
//                }
//            }
//        });
//        edit_cameraOldPassword = (EditText) findViewById(R.id.edit_cameraOldPassword);
//        edit_cameraNewPassword = (EditText) findViewById(R.id.edit_cameraNewPassword);
//        edit_cameraConfirmPassword = (EditText) findViewById(R.id.edit_cameraConfirmPassword);
//        if(!camera.pwd.equals(MyCamera.DEFAULT_PASSWORD)) {
//            edit_cameraOldPassword.requestFocus();
//            title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
//        }
//        else{
//            edit_cameraOldPassword.setText(MyCamera.DEFAULT_PASSWORD);
//            edit_cameraNewPassword.requestFocus();
//        }
//    }
//
//
//    boolean save() {
//        String oldPwd = edit_cameraOldPassword.getText().toString();
//        String newPwd = edit_cameraNewPassword.getText().toString();
//        String confirmPwd = edit_cameraConfirmPassword.getText().toString();
//        //tips_all_field_can_not_empty">请输入所有选项。
//        if (oldPwd.length() == 0 || newPwd.length() == 0 || confirmPwd.length() == 0) {
//            showAlert(getText(R.string.alert_all_field_can_not_empty).toString());
//            return false;
//        }
//
//        //tips_old_password_is_wrong">旧密码输入错误
//        if (!oldPwd.equals(camera.pwd)) {
//            showAlert(getText(R.string.alert_old_password_is_wrong).toString());
//            return false;
//        }
//
//        //tips_new_passwords_do_not_match">两次输入的新密码不一致。
//        if (!newPwd.equals(confirmPwd)) {
//            showAlert(getText(R.string.alert_new_passwords_do_not_match).toString());
//            return false;
//        }else if (newPwd.equals(oldPwd)) {//新旧密码一致
//            showAlert(getText(R.string.alert_old_new_password_same).toString());
//            return false;
//        }
//        //密码的长度为6-12位
//        if (newPwd.length()>12||newPwd.length()<6) {
//            showAlert(getText(R.string.alert_modify_pwd_pwdstring_format_error).toString());
//            return false;
//        }
//
//        if (TwsTools.isUserPwdLegal(newPwd)) {
//            if (camera != null){
//                newPassword = newPwd;
//                ((MyCamera)camera).sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetPasswdReq.parseContent(oldPwd, newPwd));
//            }
//
//            showLoadingProgress(getText(R.string.modify).toString());
//        }else {
//            showAlert(getText(R.string.alert_modify_pwd_pwdstring_format_error).toString());
//            //textView.setText("密码包含非法字符");
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void receiveFrameData(NSCamera camera, int avChannel, Bitmap bmp) {
//
//    }
//
//    @Override
//    public void receiveFrameInfo(NSCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {
//
//    }
//
//    @Override
//    public void receiveSessionInfo(NSCamera camera, int resultCode) {
//
//    }
//
//    @Override
//    public void receiveChannelInfo(NSCamera camera, int avChannel, int resultCode) {
//
//    }
//
//    @Override
//    public void receiveIOCtrlData(NSCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
//        Bundle bundle = new Bundle();
//        bundle.putInt("sessionChannel", avChannel);
//        bundle.putByteArray("data", data);
//
//        Message msg = new Message();
//        msg.what = avIOCtrlMsgType;
//        msg.setData(bundle);
//        handler.sendMessage(msg);
//    }
//
//    @Override
//    public void initSendAudio(Camera paramCamera, boolean paramBoolean) {
//
//    }
//
//    @Override
//    public void receiveOriginalFrameData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {
//
//    }
//
//    @Override
//    public void receiveRGBData(Camera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {
//
//    }
//
//    @Override
//    public void receiveRecordingData(Camera paramCamera, int avChannel, int paramInt1, String path) {
//
//    }
//
//    public void doClickLL(View view) {
//        ((LinearLayout)view).getChildAt(1).requestFocus();
//    }
//
//
//    private Handler handler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//
//            Bundle bundle = msg.getData();
//            byte[] data = bundle.getByteArray("data");
//
//            switch (msg.what) {
//
//                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETPASSWORD_RESP://设置密码返回值
//                    dismissLoadingProgress();
//                    if (data[0] == 0x00){
//
//                        camera.pwd = newPassword;
//                        camera.editPassword(newPassword);
//                        Toast.makeText(ModifyCameraPasswordActivity.this, getText(R.string.tips_modify_security_code_succ).toString(), Toast.LENGTH_SHORT).show();
//                        camera.stop();
//                        camera.start();
//                        back2Activity(MainActivity.class);
//
//                    }else {
//                        showAlert(getText(R.string.alert_modify_security_code_failed).toString());
//                    }
//
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };
//
//    @Override
//    public void  onDestroy(){
//        super.onDestroy();
//        if(camera != null){
//            camera.unregisterIOTCListener(this);
//        }
//    }
//}
