package com.tws.commonlib.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.decoding.Intents;
import com.tws.commonlib.R;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import java.net.PortUnreachableException;

/**
 * Created by Administrator on 2018/1/15.
 */

public class AddCameraNavigationTypeActivity extends BaseActivity implements View.OnClickListener {
    public static final int ADDCAMERA_WIRELESS = 0;
    public static final int ADDCAMERA_WIRED = 1;
    private final static int REQUEST_CODE_GETUID_BY_SCAN_BARCODE = 0;
    private final static int REQUEST_CODE_GETUID_BY_SEARCHLAN = 2;
    private final static int REQUEST_CODE_GETUID_BY_INPUT_UID_MANUALLY = 3;
    private String uid;
    public static final int UID_FROM_SCANQRCODE = 1;
    public static final int UID_FROM_INPUT_MANUAL = 2;
    private int uidFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera_navigation_type);
        setTitle(getResources().getString(R.string.title_add_camera));
        initView();
    }

    @Override
    public void initView() {
        super.initView();
        findViewById(R.id.ll_addcamera_wireless).setOnClickListener(this);
        findViewById(R.id.ll_addcamera_wired).setOnClickListener(this);
        this.uid = this.getIntent().getStringExtra(TwsDataValue.EXTRA_KEY_UID);
        tryConnectUID(this.uid);
    }

    @Override
    public void onClick(View view) {
        int type = 0;
        Intent intent = new Intent();
        if (view.getId() == R.id.ll_addcamera_wireless) {
            type = ADDCAMERA_WIRELESS;
            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, this.uid);
            intent.putExtra("uidfrom", uidFrom);
            intent.setClass(AddCameraNavigationTypeActivity.this, AddDeviceWirelessActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.ll_addcamera_wired) {
            type = ADDCAMERA_WIRED;
            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, this.uid);
            intent.putExtra("uidfrom", uidFrom);
            intent.setClass(AddCameraNavigationTypeActivity.this, AddDeviceWiredActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }


    public void tryConnectUID(String uid) {
        IMyCamera camera = IMyCamera.MyCameraFactory.shareInstance().createCamera(getString(R.string.hint_input_camera_name), uid, "admin", "admin");
        TwsDataValue.setTryConnectcamera(camera);
        camera.connect();
    }

    public void onResume(){
        super.onResume();
    }

    public  void  onPause(){
        super.onPause();
    }

}
