package com.tws.commonlib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.zxing.activity.CaptureActivity;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.A2bigA;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class SaveCameraActivity extends BaseActivity {

    private final int REQUEST_CODE_GETUID_BY_SCAN_BARCODE = 0;
    private final int REQUEST_CODE_GETUID_BY_LAN_SEARCH = 1;

    private EditText edtUID;
    private EditText edtSecurityCode;
    private EditText edtName;
    private Button btnScan;
    private String dev_uid;
    private IMyCamera camera;
    Button btnShowPassword;

    String dev_nickname = "";
    String view_pwd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_save_camera);
        this.setTitle(getResources().getString(R.string.title_add_camera));
        initView();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                        save(title.GetView(which));
                        break;
                }
            }
        });

        edtUID = (EditText) findViewById(R.id.edtUID);
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(btnScanClickListener);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {//如果从上一个界面中传过来的参数不为空
            dev_uid = bundle.getString(TwsDataValue.EXTRA_KEY_UID);
            edtUID.setText(dev_uid);
        }

        edtUID.setTransformationMethod(new A2bigA());
        edtSecurityCode = (EditText) findViewById(R.id.edtSecurityCode);
        edtName = (EditText) findViewById(R.id.edtNickName);
        edtName.setSelection(edtName.getText().toString().length());

        final EditText edittxt = (EditText) findViewById(R.id.edtSecurityCode);
        edittxt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    EditText passwordEditText = (EditText) findViewById(R.id.edtSecurityCode);
                    passwordEditText.setText("");
                }
            }
        });
        btnShowPassword = (Button) findViewById(R.id.btnShowPassword);
        edtSecurityCode.requestFocus();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_CODE_GETUID_BY_SCAN_BARCODE) {//扫描二维码结果

            if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {

                String contents = intent.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                if (contents.length() > 20) {
                    String temp = "";

                    for (int t = 0; t < contents.length(); t++) {
                        if (contents.substring(t, t + 1).matches("[A-Z0-9]{1}"))
                            temp += contents.substring(t, t + 1);
                    }
                    contents = temp;
                }

                edtUID.setText(contents);

//				edtSecurityCode.requestFocus();

            }
            else if(resultCode == CaptureActivity.RESULT_CODE_SEARCH_LAN){
                Intent intent1 = SaveCameraActivity.this.getIntent();
                intent1.putExtra(TwsDataValue.EXTRAS_KEY_FROM,SaveCameraActivity.class.getName());
                intent1.setClass(SaveCameraActivity.this,SearchCameraActivity.class);
                startActivityForResult(intent1,REQUEST_CODE_GETUID_BY_LAN_SEARCH);
            }

            else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }
        else if(requestCode == REQUEST_CODE_GETUID_BY_LAN_SEARCH){
            if(intent != null){
                String uid = intent.getStringExtra(TwsDataValue.EXTRA_KEY_UID);
                edtUID.setText(uid);
            }
        }
    }


    /**
     * 跳转至二维码扫描界面
     */
    private View.OnClickListener btnScanClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent();
            intent.setClass(SaveCameraActivity.this, CaptureActivity.class);
            intent.putExtra(TwsDataValue.EXTRAS_KEY_FROM,SaveCameraActivity.class.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 0);
        }
    };


    /**
     * 保存按钮点击事件
     *
     * @param v
     */
    public void save(View v) {

        dev_nickname = edtName.getText().toString();
        String dev_host, dev_port, dev_user;
        view_pwd = edtSecurityCode.getText().toString().trim();

        dev_uid = edtUID.getText().toString().trim();
        if (dev_uid.length() == 0) {
            showAlert(getText(R.string.alert_input_camera_uid));
            return;
        }
        dev_uid = TwsTools.takeInnerUid(dev_uid);
        if (dev_uid == null) {
            showAlert(getText(R.string.alert_invalid_camera_uid));
            return;
        }

        if (view_pwd.length() == 0) {
            showAlert(getText(R.string.alert_input_camera_password));
            return;
        }

        dev_nickname = dev_nickname.trim();
        if (dev_nickname.length() == 0) {
            dev_nickname = edtName.getHint().toString();
        }
        /**
         * 判断是否已经添加过该摄像机
         */
        boolean duplicated = false;
        for (IMyCamera camera_ : TwsDataValue.cameraList()) {

            if (dev_uid.equalsIgnoreCase(camera_.getUid())) {
                duplicated = true;
                break;
            }
        }

        if (duplicated) {
            //MyCamera.init();
            showAlert(getText(R.string.tips_add_camera_duplicated));
            return;
        }

			/* add value to server data base */
        camera = IMyCamera.MyCameraFactory.shareInstance().createCamera(dev_nickname, dev_uid, "admin", view_pwd);
        camera.setCameraModel(NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264.ordinal());
        camera.save(this);
//        camera.start();
//        Intent broadcast = new Intent();
//        broadcast.setAction(TwsDataValue.ACTION_CAMERA_INIT_END);
//        sendBroadcast(broadcast);

//        Bundle extras = new Bundle();
//        extras.putString(TwsDataValue.EXTRA_KEY_UID, dev_uid);
//        Intent intent = new Intent();
//        intent.putExtras(extras);
//        this.setResult(RESULT_OK,intent);
        back2Activity(MainActivity.class);
        //this.finish();
    }


    /**
     * 返回到搜索摄像机的界面
     *
     * @param v
     */
    public void back(View v) {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back(null);
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_MENU) {

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void clickLine(View view) {
        ((RelativeLayout) view).getChildAt(1).requestFocus();
    }

    public void showPassword(View view) {
        if (this.edtSecurityCode.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
            this.edtSecurityCode.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            this.btnShowPassword.setBackgroundResource(R.drawable.ic_password_show);
        } else {
            this.edtSecurityCode.setTransformationMethod(PasswordTransformationMethod.getInstance());
            this.btnShowPassword.setBackgroundResource(R.drawable.ic_password_hidden);
        }
    }

}
