package com.tws.commonlib.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.tws.commonlib.R;
import com.tws.commonlib.controller.GifView;


public class AddDeviceWirelessNoteActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_add_device_configwifi_note);
		this.setTitle(getText(R.string.title_add_camera));
		initView();
	}


	@Override
	protected  void  initView(){
		super.initView();
	}

	/**
	 * 底部手动一键配置WIFI按钮的处理，跳转到一键配置WIFI 输入SSID PASSWORD的界面
	 * @param view
	 */
	public void configWifiSsid(View view) {
		Intent intent = new Intent();
		intent.setClass(AddDeviceWirelessNoteActivity.this, AddDeviceWirelessActivity.class);
		startActivity(intent);
	}
}
