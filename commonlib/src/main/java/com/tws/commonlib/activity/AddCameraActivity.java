package com.tws.commonlib.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.activity.CaptureActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.ConnectionState;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

//import com.tenvis.P2P.global.SmartLink;


/**
 * 搜索局域网内摄像机列表，并显示
 *
 * @author Administrator
 */
public class AddCameraActivity extends BaseActivity implements View.OnClickListener {
    private Button wirelessinstall_btn;
    private Button scanqrcode_btn;
    private Button search_btn;
    private final static int REQUEST_SCANNIN_GREQUEST_CODE = 1;
    private final static int REQUEST_WIFI_CODE = 2;
    private final static int REQUEST_LAN_SEARCH = 3;
    private final static int REQUEST_SAVE_CAMERA = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);
        setTitle(getResources().getString(R.string.title_add_camera));
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        wirelessinstall_btn = (Button) findViewById(R.id.wirelessinstall_btn);
        scanqrcode_btn = (Button) findViewById(R.id.scanqrcode_btn);
        search_btn = (Button) findViewById(R.id.search_btn);

        wirelessinstall_btn.setOnClickListener(this);
        scanqrcode_btn.setOnClickListener(this);
        search_btn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void scanQRC() {
        //扫锟斤拷锟斤拷锟斤拷
        Intent intent = new Intent();
        intent.setClass(this, CaptureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫二维码添加
        if (requestCode == REQUEST_SCANNIN_GREQUEST_CODE) {
            if (resultCode == CaptureActivity.RESULT_CODE_ADD_MANUALLY) { //RESULT_OK = -1
                //手动添加
                Intent intent = new Intent();
                intent.setClass(AddCameraActivity.this, SaveCameraActivity.class);
                startActivityForResult(intent, REQUEST_SAVE_CAMERA);
            } else if (resultCode == CaptureActivity.RESULT_CODE_QR_SCAN) {
                Bundle bundle = data.getExtras();
                String scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);

                if (scanResult != null) {
                    scanResult = scanResult.trim();
                }
                if (scanResult != null && scanResult.length() == 20) {
                    Intent intent = new Intent();
                    intent.putExtra(TwsDataValue.EXTRA_KEY_UID, scanResult);
                    intent.setClass(AddCameraActivity.this, SaveCameraActivity.class);
                    startActivityForResult(intent, REQUEST_SAVE_CAMERA);
                } else {
                    showAlert(getString(R.string.alert_invalid_uid_qrcode));
                }
            }
        } else if (requestCode == REQUEST_SAVE_CAMERA) {

        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.scanqrcode_btn) {
            if (!TwsTools.checkPermission(AddCameraActivity.this, Manifest.permission.CAMERA)) {
                TwsTools.showAlertDialog(AddCameraActivity.this);
                return;
            }

            //ɨ������
            Intent intent = new Intent();
            intent.setClass(AddCameraActivity.this, CaptureActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_SCANNIN_GREQUEST_CODE);
        } else if (v.getId() == R.id.search_btn) {
            //����������UID
            Intent intent = new Intent();
            intent.setClass(AddCameraActivity.this, SearchCameraActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, REQUEST_LAN_SEARCH);
        } else if (v.getId() == R.id.wirelessinstall_btn) {
            //if(isWifiConnected(AddCameraNewActivity.this)){
//			if(!TwsTools.checkPermission(AddCameraActivity.this, Manifest.permission.CAMERA)){
//				TwsTools.showAlertDialog(AddCameraActivity.this);
//				return;
//			}
//			if(checkWifiResult()) {
            Intent intent = new Intent(AddCameraActivity.this, AddDeviceWirelessNoteActivity.class);
            startActivityForResult(intent, REQUEST_WIFI_CODE);
            //	}
        }
    }

    private boolean checkWifiResult() {
        ConnectionState.getInstance(this).CheckConnectState();
        if (!ConnectionState.getInstance(this).isSupportedWifi()) {
            //未连接wifi
            if (!ConnectionState.getInstance(this).isWifiConnected()) {
                showAlert(getString(R.string.alert_connect_to_wifi_first));
            }
            //SSID包含不支持的字符
            else if (!ConnectionState.getInstance(this).isSupportedSsid()) {
                showAlert(String.format(getString(R.string.alert_wifi_ssid_supportchar), ConnectionState.getInstance(this).getNotSupportedChar(ConnectionState.getInstance(this).getSsid())));
            }
            //连接的不是2.4G wifi
            else if (!ConnectionState.getInstance(this).is24GWifi()) {
                showAlert(getString(R.string.alert_wifi_onlysupport_24g));
            }
            //只支持wpa/wpa2加密
            else if (!ConnectionState.getInstance(this).isWpa()) {
                showAlert(getString(R.string.alert_wifi_onlysupport_wpa));
            }
//		      Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);
//	          startActivity(intent);
            return false;
        } else {
            return true;
        }
    }
}
