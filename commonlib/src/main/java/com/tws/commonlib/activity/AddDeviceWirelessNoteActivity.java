package com.tws.commonlib.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hichip.tools.HiSinVoiceData;
import com.tws.commonlib.R;
import com.tws.commonlib.bean.TwsDataValue;
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

		if(TwsDataValue.getTryConnectcamera() != null && TwsDataValue.getTryConnectcamera().isSessionConnected()){
			showAlert(getString(R.string.dialog_msg_add_camera_connected),getString(R.string.prompt),false, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					Intent intent = AddDeviceWirelessNoteActivity.this.getIntent();
					intent.setClass(AddDeviceWirelessNoteActivity.this,SaveCameraActivity.class);
					startActivity(intent);
					finish();
				}
			});
			return;
		}
	}

	/**
	 * 底部手动一键配置WIFI按钮的处理，跳转到一键配置WIFI 输入SSID PASSWORD的界面
	 * @param view
	 */
	public void configWifiSsid(View view) {
		if(TwsDataValue.getTryConnectcamera() != null && TwsDataValue.getTryConnectcamera().isSessionConnected()){
			showAlert(getString(R.string.dialog_msg_add_camera_connected),getString(R.string.prompt),false, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					Intent intent = AddDeviceWirelessNoteActivity.this.getIntent();
					intent.setClass(AddDeviceWirelessNoteActivity.this,SaveCameraActivity.class);
					startActivity(intent);
					finish();
				}
			});
			return;
		}
		Intent intent = this.getIntent();
		intent.setClass(AddDeviceWirelessNoteActivity.this, AddDeviceWirelessSettingActivity.class);
		startActivity(intent);
	}
}
