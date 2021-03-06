package com.tws.commonlib.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class ModifyCameraNameActivity extends BaseActivity {

    private String dev_uid;
    private IMyCamera camera;
    EditText edit_cameraName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_modify_cameraname);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera =  _camera;
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
        edit_cameraName = (EditText) findViewById(R.id.edit_cameraName);
        edit_cameraName.setText(camera.getNickName());
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
        camera.setNickName(edit_cameraName.getText().toString().trim());
        camera.sync2Db(this);
        return true;
    }

}
