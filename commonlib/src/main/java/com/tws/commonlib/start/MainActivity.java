package com.tws.commonlib.start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hichip.base.HiLog;
import com.hichip.sdk.HiChipSDK;
import com.tws.commonlib.R;
import com.tws.commonlib.push.RedirectAdapter;
import com.tws.commonlib.view.AppUpdateView;

/**
 * 娆㈣繋鐣岄潰锛屼富瑕佸仛浜嗕竴浜涙暟鎹垵濮嬪寲鎿嶄綔鍜岀増鏈崌绾ф娴?
 * @author Administrator
 *
 */
public class MainActivity extends AppCompatActivity {

	private final static int HANDLE_MESSAGE_INIT_END = 0x90000001;
	private static boolean isFirstLaunch = true;

	private String currentVersionString="";

	final Context context = this;
	private  BroadcastReceiver mRegistrationBroadcastReceiver;
	private  boolean isReceiverRegistered;
	private final String TAG = "MainActivity";
	//private Handler currentdownHandler;
	private static long initSdkTime;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			}
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (RedirectAdapter.checkPushRedirect(this)) {
			this.finish();
			return;
		}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_splash);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
////		//浠ヤ笅涓夊彞璁剧疆灞忓箷鍏ㄥ睆锛岃娉ㄦ剰缁ф壙鐨勬槸SherlockActivity
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


		// VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);


//		try {
//			currentVersionString = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;//鑾峰彇褰撳墠鐗堟湰
//		} catch (NameNotFoundException e) {
//		}
//
//		Splash splash = (Splash) findViewById(R.id.splash);
//
//		if (splash != null) {
//			splash.setVersion(currentVersionString);//璁剧疆闇?瑕佹樉绀虹殑鐗堟湰鍙凤紝褰撳墠鐗堟湰淇℃伅
//
//			if (!isFirstLaunch) {
//				splash.setVisibility(View.INVISIBLE);
//			}
//		}

		//Activity鐣岄潰鍒囨崲鍔ㄧ敾
		overridePendingTransition(R.anim.mainfadein, R.anim.splashfadeout);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// requestEnd();
				AppUpdateView updateView  = new AppUpdateView(MainActivity.this.context,MainActivity.this);
				updateView.checkNewVersion();
			}
		}, 2000);


	}



	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.onStartSession(this, "Q1SDXDZQ21BQMVUVJ16W");
	}

	@Override
	protected void onStop() {
		super.onStop();
		// FlurryAgent.onEndSession(this);
	}


	private void unregisterReceiver(){
		if(mRegistrationBroadcastReceiver!=null){
			LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
			isReceiverRegistered = false;
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
//        if (RedirectAdapter.checkPushRedirect(this)) {
//            this.finish();
//            return;
//        }
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
}