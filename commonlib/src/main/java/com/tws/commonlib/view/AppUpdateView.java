package com.tws.commonlib.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.widget.Toast;


import com.tutk.IOTC.L;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.BatteryStatus;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.db.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class AppUpdateView {
    private String currentVersionString = "";
    private ProgressDialog downProgressDialog;
    private String searchResultString = "";
    static final String HOST_URL = "http://download.tenvis.com";//瀹氫箟浜嗙増鏈崌绾ф娴嬫湇鍔″櫒鍦板潃
    private AppUpdate appUpdate = new AppUpdate();
    private Handler handler = new Handler();
    private String newversionString = "";
    private String newappsrcString = "";
    private String newflagString = "";
    private String newdescriptionString = "";
    public Message msgtologin;
    private Message msgtoupdata;
    public static final int TO_LOGIN_ACTIVITY = 0x100;
    public static final int TO_UPDATA_DLG = 0x101;
    private Context context;
    private Activity fromActivity;
    public Handler preHandler;

    public AppUpdateView(final Context context, final Activity fromActivity) {
        this.context = context;
        this.fromActivity = fromActivity;
        this.init();
    }

    public void init() {
        try {
            currentVersionString = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;//鑾峰彇褰撳墠鐗堟湰
        } catch (NameNotFoundException e) {
        }
        msgtologin = new Message();
        msgtologin.what = TO_LOGIN_ACTIVITY;
        msgtoupdata = new Message();
        msgtoupdata.what = TO_UPDATA_DLG;
        preHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TO_LOGIN_ACTIVITY:
                        PackageInfo info = null;
                        try {
                            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        final int currentVersion = info.versionCode;
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

                        Float lastVersion = sp.getFloat("VERSION_KEY", 0);
                        Class<?> cls = null;
//                        if (currentVersion > lastVersion) {
////            绗竴娆″惎鍔ㄥ皢褰撳墠鐗堟湰杩涜瀛樺�?
//                            sp.edit().putFloat("VERSION_KEY", currentVersion).commit();
//                            cls = IntroActivity.class;
//                        } else {
//                            cls = MainActivity.class;
//                        }
                        cls = MainActivity.class;
                        //闇�瑕佺櫥褰�
                        if (MyConfig.isHasAccount()) {
                            Intent mainIntent = new Intent(context, cls);
                            context.startActivity(mainIntent);
                        } else {
                            Intent mainIntent = new Intent(context, cls);
                            context.startActivity(mainIntent);
                        }
                        fromActivity.finish();
                        break;
                    case TO_UPDATA_DLG:


                        break;
                }
            }
        };
    }

    /**
     * 妫�娴嬫洿鏂�
     */
    public void checkNewVersion() {
        if (!MyConfig.isHasCheckForUpdate()) {//鍚﹀垯3s鍚庤繘鍏ョ櫥褰曠晫闈�
            preHandler.sendMessageDelayed(msgtologin, 1000);
            //initCameraList(context);
            return;
        } else {//濡傛灉鏈夋娴嬫洿鏂扮殑鍔熻兘鍒欐娴嬫洿鏂�
            if (AppUpdate.isNetworkAvailable(context)) {
                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean needUpdate = false;
                            try {
                                searchResultString = AppUpdate
                                        .getUpdataVerJSON(HOST_URL
                                                + "/ajax.ashx?method=getfiles&Name=" + MyConfig.getAppCheckForUpdateName());
                                L.i("searchResultString", "searchResultString-->"
                                        + searchResultString);

                                if (searchResultString.length() != 0) {
                                    if (appUpdate
                                            .decUpdataVerJSON(searchResultString)) {// 瑙ｆ瀽鎴愬姛
                                        newversionString = appUpdate
                                                .getUdversionString();
                                        newappsrcString = HOST_URL
                                                + appUpdate.getUdappsrcString();
                                        newflagString = appUpdate.getUdflagString();

                                        newdescriptionString = appUpdate
                                                .getUddescriptionString();
                                        if (newversionString.length() != 0
                                                && currentVersionString.length() != 0) {

                                            if (newversionString.equals(currentVersionString)) {

                                            } else {//鐗堟湰鍙蜂笉涓�鑷达紝鍒欐樉绀哄崌绾ф彁绀�
                                                needUpdate = true;

                                            }
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();

                            }
                            if (needUpdate) {
                                preHandler.sendMessage(msgtoupdata);
                            } else {
                                preHandler.sendMessageDelayed(msgtologin, 3000);
                            }
                        }

                    });
                    thread.start();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    preHandler.sendMessageDelayed(msgtologin, 3000);
                }
            } else {
                preHandler.sendMessageDelayed(msgtologin, 3000);
            }
        }
    }

    public static void initCameraList(Context context) {
        DatabaseManager manager = new DatabaseManager(context);
        SQLiteDatabase db = manager.getReadableDatabase();
        Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[]{
                "_id", "dev_nickname", "dev_uid", "dev_name", "dev_pwd",
                "view_acc", "view_pwd", "event_notification", "camera_channel",
                "snapshot", "ask_format_sdcard", "cameraStatus",
                "dev_videoQuality", "ownerCameraId", "cameraModel", "dev_model_name","dev_battery_mode","dev_battery_percent","dev_battery_time"}, null, null, null, null, "_id LIMIT "
                + TwsDataValue.CAMERA_MAX_LIMITS);
        List<IMyCamera> cameraList = TwsDataValue.cameraList();
        cameraList.clear();
        while (cursor.moveToNext()) {

            long db_id = cursor.getLong(cursor.getColumnIndex("_id"));
            String dev_nickname = cursor.getString(cursor
                    .getColumnIndex("dev_nickname"));
            String dev_uid = cursor.getString(cursor.getColumnIndex("dev_uid"));
            String view_acc = cursor.getString(cursor
                    .getColumnIndex("view_acc"));
            String view_pwd = cursor.getString(cursor
                    .getColumnIndex("view_pwd"));
            String modelName = cursor.getString(cursor
                    .getColumnIndex("dev_model_name"));
            int event_notification = cursor.getInt(cursor
                    .getColumnIndex("event_notification"));
            int channel = cursor
                    .getInt(cursor.getColumnIndex("camera_channel"));

            int cameraModel = cursor
                    .getInt(cursor.getColumnIndex("cameraModel"));
            int videoQuality = cursor.getInt(cursor.getColumnIndex("dev_videoQuality"));
            int batteryMode = cursor
                    .getInt(cursor.getColumnIndex("dev_battery_mode"));
            int batteryPercent = cursor
                    .getInt(cursor.getColumnIndex("dev_battery_percent"));
            long batteryTime = cursor
                    .getLong(cursor.getColumnIndex("dev_battery_time"));

            IMyCamera camera = IMyCamera.MyCameraFactory.shareInstance().createCamera(dev_nickname, dev_uid, view_acc,
                    view_pwd);
            //camera.setSnapshot(loadImageFromUrl(context, camera));
            if (cameraModel == 0) {
                camera.setCameraModel(com.tutk.IOTC.NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264.ordinal());
            }
            camera.setVideoQuality(videoQuality);
            camera.setPushOpen(event_notification > 0);
            //camera.setEventNum(event_notification);
            camera.setDatabaseId(db_id);
            camera.setModelName(modelName);
            camera.getBatteryStatus().setWorkMode(batteryMode);
            camera.getBatteryStatus().setBatPercent(batteryPercent);
            camera.getBatteryStatus().setTime(batteryTime);
            //DeviceInfo dev = new DeviceInfo(db_id, camera.getUUID(), dev_nickname, dev_uid, view_acc, view_pwd, "", event_notification, channel, null);
            //VideoList.DeviceList.add(dev);
            //VideoList.cameraList.add(dev);
            //camera.setServerDatabseId(serverDatabaseIdString);
            //camera.setCameraStatus(cameraStautsString);
            cameraList.add(camera);
//            if (camera.pushNotificationStatus > 0) {
//                camera.openPush(null, null);
//            }
        }

        //cameraList=myCameraList;
        cursor.close();
        db.close();

    }

    public static Bitmap loadImageFromUrl(Context context, IMyCamera camera) {


        //是否SD卡可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //检查是或有保存图片的文件夹，没有就创建一个
            String FileUrl = TwsTools.getFilePath(camera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB);
            File f = new File(FileUrl + "/" + camera.getUid());

            //SD卡中是否有该文件，有则直接读取返回
            if (f.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    Bitmap b = BitmapFactory.decodeStream(fis);
                    return b;
                } catch (OutOfMemoryError error) {
                    return null;
                }
            }
        }


        return null;

    }
}
