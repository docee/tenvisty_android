package com.tws.commonlib.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.activity.CaptureActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.ConnectionChangeReceiver;
import com.tws.commonlib.base.ConnectionState;
import com.tws.commonlib.base.INetworkChangeCallback;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

//import com.tenvis.P2P.global.SmartLink;

public class AddDeviceWiredActivity extends BaseActivity {
    private EditText edtWifiPassword;
    private Button btnShowPassword;
    private EditText edtWifiSsid;
    private WifiManager mWifiManager;
    private final static int REQUEST_CODE_GETUID_BY_SCAN_BARCODE = 0;
    private final static int REQUEST_CODE_GETUID_BY_SEARCHLAN = 2;
    private final static int REQUEST_CODE_GETUID_BY_INPUT_UID_MANUALLY = 3;
    private final static int GO_TO_CONFIG = 1;
    private TextView txt_connected;
    private String uid;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_add_device_wired);
        ConnectionState.getInstance(this).CheckConnectState();
        this.setTitle(getResources().getString(R.string.title_add_camera));
        initView();
        ScanQCR();
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
                        //back2Activity(AddDeviceWirelessNoteActivity.class);
                        break;
                }
            }
        });
    }



    public void ScanQCR() {
        Intent intent = new Intent();
        intent.setClass(AddDeviceWiredActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_CODE_GETUID_BY_SCAN_BARCODE) {//扫描二维码结果

            if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {

                String contents = intent.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                uid = TwsTools.takeInnerUid(contents);
                if (uid != null) {
                    boolean duplicated = false;
                    for (IMyCamera camera_ : TwsDataValue.cameraList()) {
                        if (uid.equalsIgnoreCase(camera_.getUid())) {
                            duplicated = true;
                            break;
                        }
                    }

                    if (duplicated) {
                        //MyCamera.init();
                        showAlert(getText(R.string.alert_camera_exist), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        return;
                    }
                }

//				edtSecurityCode.requestFocus();

            } else if (resultCode == CaptureActivity.RESULT_CODE_INPUT_UID_MANUALLY) {
                Intent intent2 = AddDeviceWiredActivity.this.getIntent();
                intent2.setClass(AddDeviceWiredActivity.this, AddCameraInputUidActivity.class);
                startActivityForResult(intent2, REQUEST_CODE_GETUID_BY_INPUT_UID_MANUALLY);
            } else if (resultCode == CaptureActivity.RESULT_CODE_SEARCH_LAN) {
                Intent intent2 = AddDeviceWiredActivity.this.getIntent();
                intent2.setClass(AddDeviceWiredActivity.this, SearchCameraActivity.class);
                startActivityForResult(intent2, REQUEST_CODE_GETUID_BY_SEARCHLAN);
            } else if (resultCode == CaptureActivity.RESULT_CODE_ADD_MANUALLY) {
                uid = IMyCamera.NO_USE_UID;
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                finish();
            }
        } else if (requestCode == REQUEST_CODE_GETUID_BY_SEARCHLAN) {
            if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                finish();
            }
        } else if (requestCode == REQUEST_CODE_GETUID_BY_INPUT_UID_MANUALLY) {
            if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                finish();
            } else if (resultCode == RESULT_OK) {
                if (intent != null) {
                    this.uid = intent.getStringExtra(TwsDataValue.EXTRA_KEY_UID);
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public  void  doReady(View view){
        Intent intent = this.getIntent();
        intent.putExtra(TwsDataValue.EXTRA_KEY_UID,uid);
        intent.setClass(AddDeviceWiredActivity.this,SaveCameraActivity.class);
        startActivity(intent);
    }


}
