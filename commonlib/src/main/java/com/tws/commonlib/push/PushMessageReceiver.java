package com.tws.commonlib.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tencent.android.tpush.XGLocalMessage;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;
import com.tencent.android.tpush.service.channel.c.d;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.App;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class PushMessageReceiver extends XGPushBaseReceiver {

//	private Intent intent = new Intent("com.qq.xgdemo.activity.UPDATE_LISTVIEW");

//	public interface OnMessageReceiverCallback {
//	        void onNotifactionShowedResult();
//    }
//	
//	private static OnMessageReceiverCallback cbk = null;
//	public static void loadSJThread(OnMessageReceiverCallback onCallback) {
//		cbk = onCallback; 
//	}

    @Override
    public void onDeleteTagResult(Context arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onNotifactionClickedResult(Context context,
                                           XGPushClickedResult message) {
//        if (message.getCustomContent() == null) {
//            return;
//        }
//        String key = message.getCustomContent();
//        String uid = null;
//        int type = 0;
//        int time = 0;
//        NSCamera camera = null;
//        if (key != null) {
//            try {
//                JSONObject arrJson = new JSONObject(key);
//                String jsonc = arrJson.getString("content");
//                JSONObject conJson = new JSONObject(jsonc);
//                uid = conJson.getString("uid");
//                type = conJson.getInt("type");
//                time = conJson.getInt("time");
//                Intent intent = new Intent(context, com.tws.commonlib.MainActivity.class);
//                intent.putExtra("uid", uid);
//                intent.putExtra("type", type);
//                intent.putExtra("time", time);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }


    @Override
    public void onNotifactionShowedResult(Context arg0, XGPushShowedResult arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onRegisterResult(Context arg0, int arg1, XGPushRegisterResult arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSetTagResult(Context arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTextMessage(Context arg0, XGPushTextMessage arg1) {
        // TODO Auto-generated method stub

        // TODO Auto-generated method stub

        String key = arg1.getCustomContent();
        String uid = null;
        int type = 0;
        int time = 0;
        NSCamera camera = null;
        if (key != null) {
            try {
                JSONObject arrJson = new JSONObject(key);
                String jsonc = arrJson.getString("content");
                JSONObject conJson = new JSONObject(jsonc);
                uid = conJson.getString("uid");
                type = conJson.getInt("type");
                time = conJson.getInt("time");
                TwsTools.showAlarmNotification(arg0,uid, 1, System.currentTimeMillis());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //String strAlarmType[] = arg0.getResources().getStringArray(R.array.tips_alarm_list_array);
//            XGLocalMessage local_msg = new XGLocalMessage();
//            // ���ñ�����Ϣ���ͣ�1:֪ͨ��2:��Ϣ
//            local_msg.setType(1);
//            // ������Ϣ����
//            local_msg.setTitle(camera.name + " [" + uid + "]");
//            HashMap cc = new HashMap();
//            cc.put("uid",uid);
//            local_msg.setCustomContent(cc);
//            int eventnum = ((MyCamera)camera).refreshEventNum(arg0);
//            int notificationId = ((MyCamera)camera).getIntId();// + eventnum;
//            local_msg.setNotificationId(notificationId);
//            // ������Ϣ����
//
//            local_msg.setContent(strAlarmType[0] + (eventnum > 1 ? (" +" + eventnum) : ""));
////            if (type < strAlarmType.length && type >= 0)
////                local_msg.setContent(strAlarmType[type]);
//            XGPushManager.setTag(arg0,((MyCamera) camera).getUid());
//            XGPushManager.addLocalNotification(arg0, local_msg);
        }
    }

    @Override
    public void onUnregisterResult(Context arg0, int arg1) {
        // TODO Auto-generated method stub
    }

}