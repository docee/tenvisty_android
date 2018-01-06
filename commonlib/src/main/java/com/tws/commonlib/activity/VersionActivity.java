package com.tws.commonlib.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tutk.IOTC.AVAPIs;
import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.controller.NavigationBar;

public class VersionActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        this.setTitle(getString(R.string.title_version));
        initView();
    }

    @Override
    public void initView() {
        super.initView();
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String version = " ";
        if (info != null) {
            version = info.versionName;
        }
        TextView app_version_tv = (TextView) findViewById(R.id.app_version_tv);
        app_version_tv.setText(version);


        TextView txt_SDK_version = (TextView) this.findViewById(R.id.txt_SDK_version);
        int iotcVersion[] = new int[4];

//        IOTCAPIs.IOTC_Get_Version(iotcVersion);
//        byte[] targets = new byte[4];
//        targets[3] = (byte) ( iotcVersion[0] & 0xFF);
//        targets[2] = (byte) ( iotcVersion[0] >> 8 & 0xFF);
//        targets[1] = (byte) ( iotcVersion[0] >> 16 & 0xFF);
//        targets[0] = (byte) ( iotcVersion[0] >> 24 & 0xFF);
        int avapiVer = AVAPIs.avGetAVApiVer();
        byte[] bytAvapiVer = new byte[4];
        bytAvapiVer[3] = (byte) ( avapiVer & 0xFF);
        bytAvapiVer[2] = (byte) ( avapiVer >> 8 & 0xFF);
        bytAvapiVer[1] = (byte) ( avapiVer >> 16 & 0xFF);
        bytAvapiVer[0] = (byte) ( avapiVer >> 24 & 0xFF);
        txt_SDK_version.setText(String.format("%d.%d.%d.%d",bytAvapiVer[0],bytAvapiVer[1],bytAvapiVer[2],bytAvapiVer[3]));
        TextView appname_textview = (TextView) this.findViewById(R.id.appname_textview);
        appname_textview.setText(MyConfig.getAppName());
        TextView privacyTextView = (TextView) this.findViewById(R.id.privacy_textview);

        privacyTextView.setVisibility(View.INVISIBLE);
    }

}
