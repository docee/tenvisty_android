package com.example.administrator.hh246;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.recorder.util.mp4Recorder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    mp4Recorder recorder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkPermissionAll(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        recorder = new mp4Recorder();
                        recorder.writeDataTest2();
//                        if(recorder != null){
//                            recorder.realese();
//                        }
//                        recorder = new mp4Recorder(1280,720,"/storage/emulated/0/Z5EW4S77VEL6FWRL111A_"+new Date().getTime() +".mp4");
                    }
                }).run();
            }
        });

        findViewById(R.id.btnWrite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recorder != null) {
                    for (int i = 0; i < 50; i++) {
                        recorder.writeData(new byte[0], 320, 2, 1);
                    }
                }
            }
        });

        findViewById(R.id.btnStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(recorder != null){
                            recorder.realese();
                        }
                    }
                }).run();
            }
        });
    }
    public static boolean checkPermission(Context context, String permission) {
//        int checkCallPhonePermission = ContextCompat.
//                checkSelfPermission(context,permission);
//        if(checkCallPhonePermission == PackageManager.PERMISSION_GRANTED){
//            return true;
//        }
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = context.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }
//        if(!result){
//            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, 0);
//        }
        return result;
    }

    /**
     * 检查并征求用户当前app要用到所有的权限
     */
    public static void checkPermissionAll(Activity activity) {
        List<String> list = new ArrayList<String>();
        if (!checkPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (list.size() > 0) {
            String[] permissions = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                permissions[i] = list.get(i);
            }
            ActivityCompat.requestPermissions(activity, permissions, 0);
        }
    }

}
