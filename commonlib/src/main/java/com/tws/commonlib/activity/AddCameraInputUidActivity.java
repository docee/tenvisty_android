package com.tws.commonlib.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.activity.CaptureActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.A2bigA;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2018/1/15.
 */

public class AddCameraInputUidActivity extends BaseActivity {
    EditText edtUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera_input_uid);
        setTitle(getResources().getString(R.string.title_add_camera));
        initView();
    }

    @Override
    public void initView() {
        super.initView();
        NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setTitle(this.getTitle().toString());
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationButtonLeftListner(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        finish();
                        break;
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        confirmUID();
                        break;
                }
            }
        });
        edtUID = (EditText) findViewById(R.id.edtUID);
        edtUID.setTransformationMethod(new A2bigA());
    }

    private void confirmUID() {
        String uid = edtUID.getText().toString();
        if (uid.isEmpty()) {
            showAlert(getString(R.string.alert_input_camera_uid));
            return;
        }
        uid = TwsTools.takeInnerUid(uid);
        if (uid == null) {
            showAlert(getString(R.string.alert_invalid_camera_uid));
        } else {
            if (uid != null) {
                boolean duplicated = false;
                for (IMyCamera camera_ : TwsDataValue.cameraList()) {
                    if (uid.equalsIgnoreCase(camera_.getUid())) {
                        duplicated = true;
                        break;
                    }
                }

                if (duplicated) {
                    showAlert(getText(R.string.alert_camera_exist), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    return;
                }
            }
            Intent intent = new Intent();
            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, uid);
            intent.setClass(this, AddCameraNavigationTypeActivity.class);
            startActivity(intent);
        }
    }

    public void clickLine(View view) {
        ((RelativeLayout) view).getChildAt(1).requestFocus();
    }
}
