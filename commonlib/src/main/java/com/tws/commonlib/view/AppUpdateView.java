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
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.db.DatabaseManager;
import com.tws.commonlib.start.IntroActivity;

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

                        if (newflagString.equals("False")) {//鎻愮ず鏇存柊
                            showUpdataDialog();
                        } else {//寮哄埗鏇存柊锛岀洿鎺ヤ笅杞�
                            showdownProgressDialogn();
                            appUpdate.downAppFile(context, newappsrcString, currentdownHandler, MyConfig.getAppName() + "V" + newversionString + ".apk");
                        }

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
            initCameraList(context);
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

    /**
     * 鎻愮ず鏇存柊鐨刣ialog
     */
    public void showUpdataDialog() {
        StringBuffer sb = new StringBuffer();
        sb.append(context.getString(R.string.app_update_current_version));//褰撳墠鐗堟湰锛�
        sb.append(currentVersionString);
        sb.append("\n");
        sb.append(context.getString(R.string.app_update_found_newversion));//"鍙戠幇鏂扮増鏈細"
        sb.append(newversionString);
        sb.append("\n");
        sb.append(newdescriptionString);
        sb.append("\n");
        sb.append(context.getString(R.string.app_update_sure_updata));//"鏄惁鏇存柊锛�"
        Dialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(context.getString(R.string.app_update_updata_app_title))//"杞欢鏇存柊"
                .setMessage(sb.toString())
                .setPositiveButton(context.getString(R.string.app_update_to_updata), new DialogInterface.OnClickListener() {//"鏇存柊"

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        showdownProgressDialog();
                        appUpdate.downAppFile(context, newappsrcString, currentdownHandler, MyConfig.getAppName() + "V" + newversionString + ".apk");
                    }
                })
                .setNegativeButton(context.getString(R.string.app_update_to_after), new DialogInterface.OnClickListener() {//"鏆備笉鏇存柊"

                    @Override
                    public void onClick(DialogInterface dialog, int which) {//濡傛灉閫夋嫨涓嶆洿鏂板垯璺宠繃鐩存帴杩涘叆涓嬩竴涓猘ctivity
                        // TODO Auto-generated method stub
                    /*Message msglogin=new Message();
					msglogin.what=0x101;*/
                        preHandler.sendMessage(msgtologin);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }


    /**
     * 鏇存柊涓嬭浇鎯呭喌鐨刪andler
     */
    public Handler currentdownHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 0:
                        downProgressDialog.show();
                        downProgressDialog.setMax((int) ((appUpdate.getFilelength()) / 1024));
                        //Log.i("currentdownHandler", "downloadprogressbar.setMax");
                        break;
                    case 1:
                        //Log.i("currentdownHandler", "downedlength=="+appUpdate.getDownedlength());
                        downProgressDialog.setProgress((appUpdate.getDownedlength()) / 1024);

                        break;

                    case 2://姝ｅ父涓嬭浇瀹屾垚
                        downProgressDialog.dismiss();
                        //寮哄埗瀹夎
                        if (newflagString.equals("False")) {
                            haveDownload(msg.getData().getString("path"));
                        } else {
                            installNewApk(msg.getData().getString("path"));
                            fromActivity.finish();
                        }

                        Toast.makeText(context, context.getString(R.string.app_update_download_ok), Toast.LENGTH_LONG).show();//"涓嬭浇瀹屾垚"
                        break;

                    case 3://涓柇涓嬭浇--鍦ㄥ彇娑堜笅杞界殑鏃跺�欏凡鍋氬鐞�

                        break;

                    case 4://缃戠粶寮傚父瑕佸仛鐨勫鐞�

                        break;

                    case 5://IO鎿嶄綔寮傚父瑕佸仛鐨勫鐞�
                        Toast.makeText(context, "no permission", Toast.LENGTH_LONG).show();//"涓嬭浇瀹屾垚"
                        if (newflagString.equals("False")) {
                            preHandler.sendMessage(msgtologin);
                        }
                        break;
                }
            }

        }
    };

    /**
     * 涓嬭浇瀹屾垚鏄剧ず鏄惁瀹夎鎻愮ず
     */
    protected void haveDownload(final String path) {
        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Dialog installDialog = new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.app_update_download_ok))//"涓嬭浇瀹屾垚"
                        .setMessage(context.getString(R.string.app_update_sure_install))//"鏄惁瀹夎鏂扮殑搴旂敤锛�"
                        .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {//"纭畾"

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                installNewApk(path);
                                fromActivity.finish();
                            }
                        })
                        .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {//"鍙栨秷"

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                //splashHandler.sendMessage(msgtologin);
                                //finish();
                                //installDialog.dismiss();
                                preHandler.sendMessage(msgtologin);
                            }
                        }).create();
                installDialog.show();
            }
        });
    }

    /**
     * 璋冪敤绯荤粺瀹夎apk鏂囦欢
     */
    protected void installNewApk(final String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 鏄剧ず涓嬭浇杩涘害鐨刣ialog
     */
    @SuppressWarnings("deprecation")
    public void showdownProgressDialog() {
        downProgressDialog = new ProgressDialog(context);
        downProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downProgressDialog.setTitle(context.getString(R.string.app_update_have_downloading));//"姝ｅ湪涓嬭浇"
        downProgressDialog.setIcon(android.R.drawable.ic_menu_more);
        downProgressDialog.setProgressNumberFormat("%1d kb/%2d kb");
        //downProgressDialog.setFeatureDrawable(featureId, drawable);
        //downProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        downProgressDialog.setCancelable(false);
        downProgressDialog.setCanceledOnTouchOutside(false);
        downProgressDialog.setButton(context.getString(R.string.app_update_have_canceldownload), new DialogInterface.OnClickListener() {//"鍙栨秷涓嬭浇"

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                appUpdate.setFlagCancel(true);
                downProgressDialog.dismiss();
                preHandler.sendMessage(msgtologin);
                //splashHandler.sendMessage(msgtologin);
            }
        });

    }

    /**
     * 蹇呴』鍗囩骇鐨勬樉绀�
     */
    @SuppressWarnings("deprecation")
    public void showdownProgressDialogn() {
        downProgressDialog = new ProgressDialog(context);
        downProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downProgressDialog.setTitle(context.getString(R.string.app_update_have_downloading));//"姝ｅ湪涓嬭浇"
        downProgressDialog.setIcon(android.R.drawable.ic_menu_more);
        downProgressDialog.setProgressNumberFormat("%1d kb/%2d kb");
        //downProgressDialog.setFeatureDrawable(featureId, drawable);
        //downProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.drawable.ic_launcher));
        downProgressDialog.setCancelable(false);
        downProgressDialog.setCanceledOnTouchOutside(false);

    }


    public static void initCameraList(Context context) {
        DatabaseManager manager = new DatabaseManager(context);
        SQLiteDatabase db = manager.getReadableDatabase();
        Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[]{
                "_id", "dev_nickname", "dev_uid", "dev_name", "dev_pwd",
                "view_acc", "view_pwd", "event_notification", "camera_channel",
                "snapshot", "ask_format_sdcard", "cameraStatus",
                "dev_videoQuality", "ownerCameraId", "cameraModel"}, null, null, null, null, "_id LIMIT "
                + TwsDataValue.CAMERA_MAX_LIMITS);
        List<MyCamera> cameraList = TwsDataValue.cameraList();
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
            int event_notification = cursor.getInt(cursor
                    .getColumnIndex("event_notification"));
            int channel = cursor
                    .getInt(cursor.getColumnIndex("camera_channel"));

            int cameraModel = cursor
                    .getInt(cursor.getColumnIndex("cameraModel"));
            int videoQuality = cursor.getInt(cursor.getColumnIndex("dev_videoQuality"));

            MyCamera camera = new MyCamera(dev_nickname, dev_uid, view_acc,
                    view_pwd);
            camera.setSnapshot(loadImageFromUrl(context, camera));
            if (cameraModel == 0) {
                camera.cameraModel = com.tutk.IOTC.NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264;
            }
            camera.setVideoQuality(videoQuality);
            camera.pushNotificationStatus = event_notification;
            camera.databaseId = db_id + "";
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

    public static Bitmap loadImageFromUrl(Context context, MyCamera camera) {


        //是否SD卡可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //检查是或有保存图片的文件夹，没有就创建一个
            String FileUrl = Environment.getExternalStorageDirectory() + "/android/data/" + MyConfig.getFolderName() + "/";
            File folder = new File(FileUrl);
            if (!folder.exists()) {
                folder.mkdir();
            }
            File f = new File(FileUrl + camera.uid);
            //SD卡中是否有该文件，有则直接读取返回
            if (f.exists()) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Bitmap b = BitmapFactory.decodeStream(fis);
                return b;
            }
        }


        return null;

    }
}
