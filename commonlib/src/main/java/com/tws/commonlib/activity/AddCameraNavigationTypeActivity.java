package com.tws.commonlib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.zxing.activity.CaptureActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.bean.TwsDataValue;

/**
 * Created by Administrator on 2018/1/15.
 */

public class AddCameraNavigationTypeActivity extends BaseActivity implements View.OnClickListener {
    public static  final int ADDCAMERA_WIRELESS = 0;
    public static  final int ADDCAMERA_WIRED = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera_navigation_type);
        setTitle(getResources().getString(R.string.title_add_camera));
        initView();
    }

    @Override
    public void initView(){
        super.initView();
        findViewById(R.id.ll_addcamera_wireless).setOnClickListener(this);
        findViewById(R.id.ll_addcamera_wired).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        int type = 0;
        if(view.getId() == R.id.ll_addcamera_wireless){
            type = ADDCAMERA_WIRELESS;
            intent.putExtra("type", type);
            intent.setClass(AddCameraNavigationTypeActivity.this, AddDeviceWirelessActivity.class);
            startActivity(intent);
        }
        else if(view.getId() == R.id.ll_addcamera_wired){
            type = ADDCAMERA_WIRED;
            intent.putExtra("type", type);
            intent.setClass(AddCameraNavigationTypeActivity.this, AddDeviceWiredActivity.class);
            startActivity(intent);
        }
    }

}
