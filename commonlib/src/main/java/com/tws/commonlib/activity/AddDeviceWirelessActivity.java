package com.tws.commonlib.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

public class AddDeviceWirelessActivity extends BaseActivity implements INetworkChangeCallback {
    private EditText edtWifiPassword;
    private Button btnShowPassword;
    private EditText edtWifiSsid;
    private WifiManager mWifiManager;
    private final static int REQUEST_CODE_GETUID_BY_SCAN_BARCODE = 0;
    private final static int GO_TO_CONFIG = 1;

    private String uid;
    private ProgressDialog dialog;
    ConnectionChangeReceiver broadcast;
    IntentFilter intentFilter;

    private String getWifiSsid() {
        if (edtWifiSsid != null) {
            return edtWifiSsid.getText().toString();
        }
        return "";
    }

    private String getWifiPassword() {
        if (edtWifiPassword != null) {
            return edtWifiPassword.getText().toString();
        }
        return "";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectionState.getInstance(this).registerIOSessionListener(this);
        this.setContentView(R.layout.activity_add_device_wireless);
        ConnectionState.getInstance(this).CheckConnectState();
        if (ConnectionState.getInstance(this).isSupportedWifi()) {
            this.setTitle(getResources().getString(R.string.title_add_camera));
            initView();
            ScanQCR();
            registerBroadReceiver();
        } else {
            finish();
        }

    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        back2Activity(AddDeviceWirelessNoteActivity.class);
                        break;
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        if (checkValue()) {
                            Intent intent = new Intent();
                            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, uid);
                            intent.putExtra("ssid", getWifiSsid());
                            intent.putExtra("authMode", ConnectionState.getInstance(AddDeviceWirelessActivity.this).GetAutoMode());
                            intent.putExtra("password", getWifiPassword());
                            intent.setClass(AddDeviceWirelessActivity.this, AddDeviceWirelessSettingActivity.class);
                            startActivityForResult(intent, GO_TO_CONFIG);
                        }
                        break;
                }
            }
        });

        title.setNavigationButtonLeftListner(null);
        this.btnShowPassword = (Button) this.findViewById(R.id.btnShowPassword);
        this.edtWifiSsid = (EditText) this.findViewById(R.id.edtWifiSsid);
        this.edtWifiPassword = (EditText) this.findViewById(R.id.edtWifiPassword);
        edtWifiSsid.setText(ConnectionState.getInstance(this).getSsid());
        this.edtWifiPassword.requestFocus();
    }

    private boolean checkValue() {
        boolean result = true;
        if (!ConnectionState.getInstance(this).isSupportedWifi()) {
            //未连接wifi
            if (!ConnectionState.getInstance(this).isWifiConnected()) {
                showAlert(getString(R.string.alert_connect_to_wifi_first));
            } else if (!ConnectionState.getInstance(this).isSupportedSsid()) {
                String notSupportedCharsSsid = ConnectionState.getInstance(this).getNotSupportedChar(ConnectionState.getInstance(this).getSsid());
                showAlert(String.format(getString(R.string.alert_wifi_ssid_supportchar), notSupportedCharsSsid), getString(R.string.alert_wifi_ssid_supportchar).indexOf('%'), notSupportedCharsSsid.length(), Color.RED);
            }
            //连接的不是2.4G wifi
            else if (!ConnectionState.getInstance(this).is24GWifi()) {
                showAlert(getString(R.string.alert_wifi_onlysupport_24g));
            }
            //只支持wpa/wpa2加密
            else if (!ConnectionState.getInstance(this).isWpa()) {
                showAlert(getString(R.string.alert_wifi_onlysupport_wpa));
            }
            result = false;
        }
//        if(result){
//            String wifiPwd = edtWifiPassword.getText().toString().trim();
//            if(wifiPwd.length() == 0){
//                showAlert(getString(R.string.alert_input_wifi_psw));
//                result = false;
//            }else{
//                String notSupportedChars = ConnectionState.getInstance(this).getNotSupportedChar(wifiPwd);
//                if(notSupportedChars.length() > 0){
//                    notSupportedChars = notSupportedChars.trim();
//                    showAlert(String.format(getString(R.string.alert_wifi_psw_supportchar),notSupportedChars),getString(R.string.alert_wifi_psw_supportchar).indexOf('%'),notSupportedChars.length(),Color.RED);
//                    result = false;
//                }
//            }
//        }
        return result;
    }

    public void showPassword(View v) {
        if (this.edtWifiPassword.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
            this.edtWifiPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            this.btnShowPassword.setBackgroundResource(R.drawable.ic_password_show);
        } else {
            this.edtWifiPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            this.btnShowPassword.setBackgroundResource(R.drawable.ic_password_hidden);
        }
    }


    public void ScanQCR() {
        Intent intent = new Intent();
        intent.putExtra("hasskip", true);
        intent.setClass(AddDeviceWirelessActivity.this, CaptureActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_CODE_GETUID_BY_SCAN_BARCODE) {//扫描二维码结果

            if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {

                String contents = intent.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                uid = TwsTools.takeInnerUid(contents);
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

//				edtSecurityCode.requestFocus();

            } else if (resultCode == CaptureActivity.RESULT_CODE_ADD_MANUALLY) {
                uid = IMyCamera.NO_USE_UID;
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                finish();
            }
        }
    }

    private boolean checkWifiResult() {

        if (edtWifiSsid != null) {
            edtWifiSsid.setText(ConnectionState.getInstance(this).getSsid());
        }

        if (!ConnectionState.getInstance(this).isSupportedWifi()) {
            //未连接wifi
            if (!ConnectionState.getInstance(this).isWifiConnected()) {
                TwsToast.showToast(this, getString(R.string.alert_connect_to_wifi_first));
            }
            //SSID包含不支持的字符
            else if (!ConnectionState.getInstance(this).isSupportedSsid()) {
                TwsToast.showToast(this, String.format(getString(R.string.alert_wifi_ssid_supportchar), ConnectionState.getInstance(this).getNotSupportedChar(ConnectionState.getInstance(this).getSsid())));
            }
            //连接的不是2.4G wifi
            else if (!ConnectionState.getInstance(this).is24GWifi()) {
                TwsToast.showToast(this, getString(R.string.alert_wifi_onlysupport_24g));
            }
            //只支持wpa/wpa2加密
            else if (!ConnectionState.getInstance(this).isWpa()) {
                TwsToast.showToast(this, getString(R.string.alert_wifi_onlysupport_wpa));
            }
//		      Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);
//	          startActivity(intent);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void receiveNetworkType(int networkType) {
        // TODO Auto-generated method stub
        checkWifiResult();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //停止接受网络改变广播
        if (broadcast != null) {
            unregisterReceiver(broadcast);
        }
        ConnectionState.getInstance(this).unregisterIOSessionListener(this);
    }

    void registerBroadReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        broadcast = new ConnectionChangeReceiver();
        registerReceiver(broadcast, intentFilter);
    }

}
