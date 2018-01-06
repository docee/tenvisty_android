package com.tws.commonlib.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

public class EditDeviceActivity extends BaseActivity {

    private EditText edtNickName;
    private MyCamera mCamera = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);

        Bundle bundle = this.getIntent().getExtras();
        String devUID = bundle.getString(TwsDataValue.EXTRA_KEY_UID);

        for (NSCamera camera : TwsDataValue.cameraList()) {
            if (devUID.equalsIgnoreCase(camera.uid)) {
                mCamera = (MyCamera) camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_edit_camera_info));
        initView();
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
                        if (save()) {
                            finish();
                        }
                        break;
                }
            }
        });
        /* find view */
        ((TextView) findViewById(R.id.edtUID)).setText(mCamera.uid);
        edtNickName = (EditText) findViewById(R.id.edtNickName);
        edtNickName.setText(mCamera.getName());
        edtNickName.requestFocus();
    }

    public void clickLine(View view) {
        ((LinearLayout) view).getChildAt(1).requestFocus();
    }

    private boolean save() {
        if (edtNickName.getText().length() == 0) {
            showAlert(getString(R.string.alert_input_camera_name));
            edtNickName.requestFocus();
            return false;
        }
        mCamera.setName(edtNickName.getText().toString());
        mCamera.sync2Db(this);
        return true;
    }

}
