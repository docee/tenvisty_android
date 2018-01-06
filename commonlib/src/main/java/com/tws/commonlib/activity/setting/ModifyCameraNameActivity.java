package com.tws.commonlib.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class ModifyCameraNameActivity extends BaseActivity {

    private String dev_uid;
    private MyCamera camera;
    EditText edit_cameraName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modify_cameraname);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (NSCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.uid.equalsIgnoreCase(dev_uid)) {
                camera = (MyCamera) _camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_camera_name));
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
        edit_cameraName = (EditText) findViewById(R.id.edit_cameraName);
        edit_cameraName.setText(camera.name);
        edit_cameraName.requestFocus();
    }

    public void doClickLL(View view) {
        edit_cameraName.requestFocus();
    }

    boolean save() {
        if (edit_cameraName.getText().toString().trim().length() == 0) {
            showAlert(getString(R.string.alert_input_camera_name));
            return false;
        }
        camera.setName(edit_cameraName.getText().toString().trim());
        camera.sync2Db(this);
        return true;
    }

}
