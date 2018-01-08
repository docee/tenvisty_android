package com.tws.commonlib.activity.hichip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hichip.content.HiChipDefines;
import com.hichip.data.HiDeviceInfo;
import com.hichip.sdk.HiChipP2P;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.bean.TwsIOCTRLDEFs;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class AudioSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private HichipCamera camera;
    TextView txt_audio_input_value;
    TextView txt_audio_output_value;
    SeekBar seekbar_audio_input;
    SeekBar seekbar_audio_output;
    private int maxInputValue = 100;
    private int maxOutputValue = 100;
    private HiChipDefines.HI_P2P_S_AUDIO_ATTR audio_attr;

    private static final int Reconnect = 0xC1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = (HichipCamera) _camera;
                break;
            }
        }
        this.setTitle(getString(R.string.title_camera_setting_audio));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        if(camera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE){
            maxInputValue=16;
            maxOutputValue=13;
        }
        txt_audio_input_value = (TextView) findViewById(R.id.txt_audio_input_value);
        txt_audio_output_value = (TextView) findViewById(R.id.txt_audio_output_value);
        seekbar_audio_input = (SeekBar)findViewById(R.id.seekbar_audio_input);
        seekbar_audio_output = (SeekBar)findViewById(R.id.seekbar_audio_output);
        seekbar_audio_input.setMax(maxInputValue - 1);
        seekbar_audio_output.setMax(maxOutputValue - 1);
        seekbar_audio_input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                txt_audio_input_value.setText(String.valueOf(progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(audio_attr == null)
                    return;

                int invol = seekbar_audio_input.getProgress() + 1;
                int outvol = seekbar_audio_output.getProgress() + 1;
                int inmode = audio_attr.u32InMode;
                camera.sendIOCtrl(0,HiChipDefines.HI_P2P_SET_AUDIO_ATTR, HiChipDefines.HI_P2P_S_AUDIO_ATTR.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN,audio_attr.u32Enable, audio_attr.u32Stream, audio_attr.u32AudioType, inmode, invol, outvol));

            }
        });
        seekbar_audio_output.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                txt_audio_output_value.setText(String.valueOf(progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(audio_attr == null)
                    return;

                int invol = seekbar_audio_input.getProgress() + 1;
                int outvol = seekbar_audio_output.getProgress() + 1;
                int inmode = audio_attr.u32InMode;
                camera.sendIOCtrl(0,HiChipDefines.HI_P2P_SET_AUDIO_ATTR, HiChipDefines.HI_P2P_S_AUDIO_ATTR.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN,audio_attr.u32Enable, audio_attr.u32Stream, audio_attr.u32AudioType, inmode, invol, outvol));

            }
        });
        getAudioSetting();
    }

    private void getAudioSetting(){
        showLoadingProgress();
        camera.sendIOCtrl(0,HiChipDefines.HI_P2P_GET_AUDIO_ATTR, null);
    }



    boolean save() {
      return true;
    }

    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {
        Message msg = new Message();
        msg.what = resultCode;
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
        msg.arg1 = avChannel;
        msg.setData(bundle);
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

                case HiChipDefines.HI_P2P_GET_AUDIO_ATTR://
                    dismissLoadingProgress();
                    audio_attr = new HiChipDefines.HI_P2P_S_AUDIO_ATTR(data);
                    seekbar_audio_input.setProgress(audio_attr.u32InVol - 1);
                    seekbar_audio_output.setProgress(audio_attr.u32OutVol - 1);
                    break;
                case HiChipDefines.HI_P2P_SET_AUDIO_ATTR:
                    TwsToast.showToast(AudioSetting_HichipActivity.this,getString(R.string.tips_setting_succ));

                    break;

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
    }
}
