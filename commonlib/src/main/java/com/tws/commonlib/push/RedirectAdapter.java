package com.tws.commonlib.push;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/9/29.
 */

public class RedirectAdapter {

    public static boolean checkPushRedirect(Activity activity) {
        String uid = null;
        int evtType = 0;
        IMyCamera camera = null;
        // 判断是否从推送通知栏打开的
        XGPushClickedResult click = XGPushManager.onActivityStarted(activity);
        if (click != null) {
            try {
                JSONObject arrJson = new JSONObject(click.getCustomContent());
                uid = arrJson.getString("uid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //从推送通知栏打开-Service打开Activity会重新执行Laucher流程
            //查看是不是全新打开的面板

            //如果有面板存在则关闭当前的面板
        } else {
            Intent intent = activity.getIntent();
            if (intent != null) {
                uid = intent.getStringExtra(TwsDataValue.EXTRA_KEY_UID);
                evtType = intent.getIntExtra(TwsDataValue.EXTRA_ALARM_EVENT_ID, 0);
            }
        }
        if (uid != null) {
            for (IMyCamera c : TwsDataValue.cameraList()) {
                if (c.getUid().equalsIgnoreCase(uid)) {
                    camera = c;
                    break;
                }
            }
        }
        if (camera != null) {
            NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            //int eventnum = ((MyCamera) camera).clearEventNum(activity);
            int intId = camera.getIntId();
            manager.cancel(camera.getUid(), intId + evtType);
            if (evtType != 0) {
                camera.clearEventNum(activity, evtType);
            }
//            for (int i = 1; i <= eventnum; i++) {
//                manager.cancel(intId + i);
//            }
            if ((activity).isTaskRoot()) {
                return false;
            }
            Intent intent = activity.getIntent();
            intent.setClass(activity, com.tws.commonlib.MainActivity.class);
            activity.startActivity(intent);
            return true;
        }
        return false;
    }
}
