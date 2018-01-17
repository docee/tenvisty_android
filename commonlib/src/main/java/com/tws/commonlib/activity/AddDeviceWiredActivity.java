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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_add_device_wired);
        ConnectionState.getInstance(this).CheckConnectState();
        this.setTitle(getResources().getString(R.string.title_add_camera));
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public  void  doReady(View view){
        Intent intent = this.getIntent();
        intent.setClass(AddDeviceWiredActivity.this,SaveCameraActivity.class);
        startActivity(intent);
    }


}
