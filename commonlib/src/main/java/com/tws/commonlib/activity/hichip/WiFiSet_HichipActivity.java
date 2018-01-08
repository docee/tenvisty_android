package com.tws.commonlib.activity.hichip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hichip.content.HiChipDefines;
import com.hichip.sdk.HiChipP2P;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class WiFiSet_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    EditText edtWifiSsid;
    EditText edtWifiPassword;
    byte[] ssid;
    byte enctype;
    byte mode;
    Button btnShowPassword;
    private final static int REQUEST_GET_WIFI = 0x998;
    boolean isTimeout = false;
    boolean isRequestWiFiListForResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_setting);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera =  _camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_setting_wifi));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        save();
                        break;
                }
            }
        });
        edtWifiSsid = (EditText) findViewById(R.id.edtWifiSsid);

        edtWifiPassword = (EditText) findViewById(R.id.edtWifiPassword);
        btnShowPassword = (Button) findViewById(R.id.btnShowPassword);
        ssid = this.getIntent().getExtras().getByteArray("ssid");
        enctype = this.getIntent().getExtras().getByte("enctype");
        mode = this.getIntent().getExtras().getByte("mode");
        edtWifiSsid.setText(TwsTools.getString(ssid));
        TwsTools.tintDrawable(((ImageView) findViewById(R.id.wifiSsidImgView)).getDrawable(), ContextCompat.getColorStateList(this, R.color.colorPrimary));
        TwsTools.tintDrawable(((ImageView) findViewById(R.id.wifiPasswordImgView)).getDrawable(), ContextCompat.getColorStateList(this, R.color.colorPrimary));

        if (enctype == 1) {
            findViewById(R.id.ll_password).setVisibility(View.GONE);
        } else {
            edtWifiPassword.requestFocus();
        }

    }

    void save() {
        isTimeout = false;
        if (enctype != 1 && edtWifiPassword.getText().length() == 0) {
            showAlert(getString(R.string.alert_input_wifi_password));
            return;
        }
        showLoadingProgress(getString(R.string.process_setting), true, 4000, new TwsProgressDialog.OnTimeOutListener() {
            @Override
            public void onTimeOut(TwsProgressDialog dialog) {
                isTimeout = true;
                handler.removeMessages(REQUEST_GET_WIFI);

                dismissLoadingProgress();
                TwsToast.showToast(WiFiSet_HichipActivity.this, getString(R.string.tips_setting_succ));

                WiFiSet_HichipActivity.this.setResult(RESULT_OK);
                camera.stop();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        camera.start();
                    }
                }, 1000);
                WiFiSet_HichipActivity.this.back2Activity(MainActivity.class);
            }
        });
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_SET_WIFI_PARAM,
                HiChipDefines.HI_P2P_S_WIFI_PARAM.parseContent(
                        HiChipP2P.HI_P2P_SE_CMD_CHN, 0,
                        mode,enctype, ssid,
                        edtWifiPassword.getText().toString().getBytes()));
    }

    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {
        Log.i(this.getClass().getSimpleName(), "connect state " + camera.getUid() + " " + resultCode);
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = resultCode;
        msg.obj = camera;
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
        msg.arg1 = avIOCtrlMsgType;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        msg.obj = camera;
        handler.sendMessage(msg);
    }

    @Override
    public void initSendAudio(IMyCamera paramCamera, boolean paramBoolean) {

    }

    @Override
    public void receiveOriginalFrameData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {

    }

    @Override
    public void receiveRGBData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {

    }

    @Override
    public void receiveRecordingData(IMyCamera paramCamera, int avChannel, int paramInt1, String path) {

    }

    public void doClickLL(View view) {
        ((LinearLayout) view).getChildAt(1).requestFocus();
    }


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {


            }
            super.handleMessage(msg);

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
        handler.removeMessages(REQUEST_GET_WIFI);
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


}
