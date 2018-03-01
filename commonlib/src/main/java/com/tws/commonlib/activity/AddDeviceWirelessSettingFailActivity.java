package com.tws.commonlib.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.controller.widget.SaundProgressBar;
import com.tws.commonlib.wificonfig.BaseConfig;
import com.tws.commonlib.wificonfig.WiFiConfigureContext;

import java.util.Calendar;
import java.util.TimeZone;

public class AddDeviceWirelessSettingFailActivity extends BaseActivity {

    public int uidFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.activity_add_device_wireless_setting_fail);
        this.setTitle(getString(R.string.title_add_camera_fail));
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        if (MyConfig.getWirelessInstallHelpUrl() != null) {
            final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
            title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
            title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
            title.setRightBtnBackground(android.R.drawable.ic_menu_help);
            title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

                @Override
                public void OnNavigationButtonClick(int which) {
                    switch (which) {
                        case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                            go2Help();
                            break;
                        case NavigationBar.NAVIGATION_BUTTON_LEFT:
                            back2Activity(AddDeviceWirelessActivity.class);
                            break;
                    }
                }
            });
        }
        Bundle extras = this.getIntent().getExtras();
        findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back2Activity(AddDeviceWirelessActivity.class);
            }
        });
        uidFrom = this.getIntent().getIntExtra("uidfrom", 0);
        if(TwsDataValue.getTryConnectcamera() != null && TwsDataValue.getTryConnectcamera().getP2PType() == IMyCamera.CameraP2PType.HichipP2P){
            ((TextView)findViewById(R.id.txt_msg)).setText(getString(R.string.tips_add_camera_fail_1));
        }
        else{
            ((TextView)findViewById(R.id.txt_msg)).setText(getString(R.string.tips_add_camera_fail_2));
        }
    }

    private void go2Help() {
        Intent intent = new Intent();
        intent.setClass(this, WebBrowserActivity.class);
        String title = this.getString(R.string.title_userhelp);
        String url = MyConfig.getWirelessInstallHelpUrl();
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        startActivity(intent);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
