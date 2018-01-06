package com.tws.commonlib.base;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.android.tpush.XGLocalMessage;
import com.tencent.android.tpush.XGPushManager;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.App;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.PhotoShowActivity;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Created by Administrator on 2017/9/19.
 */

public class TwsTools {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int[] unDisplayViewSize(View view) {
        int size[] = new int[2];
        int width = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        size[0] = view.getMeasuredWidth();
        size[1] = view.getMeasuredHeight();
        return size;
    }

    /**
     * 计算出来的位置，y方向就在anchorView的上面和下面对齐显示，x方向就是与屏幕右边对齐显示
     * 如果anchorView的位置有变化，就可以适当自己额外加入偏移来修正
     *
     * @param anchorView  呼出window的view
     * @param contentView window的内容布局
     * @return window显示的左上角的xOff, yOff坐标
     */
    public static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int windowPos[] = new int[2];
        final int anchorLoc[] = new int[2];
        // 获取锚点View在屏幕上的左上角坐标位置
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        // 获取屏幕的高宽
        final int screenHeight = getScreenHeight(anchorView.getContext());
        final int screenWidth = getScreenWidth(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        // 计算contentView的高宽
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        // 判断需要向上弹出还是向下弹出显示
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }

    /**
     * 获取CPU的核数
     *
     * @return
     */
    static public int getCpuCoresNum() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Log.d("MyDeviceInfo", "CPU Count: " + files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Print exception
            Log.d("MyDeviceInfo", "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }

    /**
     * 获取CPU的频率
     *
     * @return
     */
    public static float getCpuFrequence() {
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line = reader.readLine();
            return (float) Integer.parseInt(line) / 1000000;
//            return StringUtils.parseLongSafe(line, 10, 0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //魅族手机获取格式错误
        catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public static boolean isUserPwdLegal(String pwd) {

        int pwdLength = pwd.length();

        /**
         * 长度不符合要求
         */
        if (pwdLength > 12 || pwdLength < 6) {
            L.i("PWDCHEK-TAG", "长度-不符合要求");
            return false;
        }

        if (!pwd.matches("[0-9A-Za-z\\.@_~!$%^(),|/\\*\\-]{6,12}")) {
            L.i("PWDCHEK-TAG", "密码包含特殊字符-不符合要求");
            return false;
        }
        /**
         * 全为数字
         */
        if (pwd.matches("[0-9]{6,12}")) {
            L.i("PWDCHEK-TAG", "全为数字-不符合要求");
            return false;
        }

        /**
         * 全为大写字母
         */
        if (pwd.matches("[A-Z]{6,12}")) {
            L.i("PWDCHEK-TAG", "全为大写字母-不符合要求");
            return false;
        }

        /**
         * 全为小写字母
         */
        if (pwd.matches("[a-z]{6,12}")) {
            L.i("PWDCHEK-TAG", "全为小写字母-不符合要求");
            return false;
        }

        /**
         * 全为特殊字符
         */
        if (pwd.matches("[\\.@_~!$%^(),|\\*/\\-]{6,12}")) {
            L.i("PWDCHEK-TAG", "全为特殊字符-不符合要求");
            return false;
        }
        L.i("PWDCHEK-TAG", "密码OK");
        return true;
    }

    public static String getString(byte[] data) {

        String result = null;
        try {
            int l = data.length;
            for (int i = 0; i < l; i++) {
                if (data[i] == 0) {
                    l = i;
                    break;
                }
            }
            result = new String(data, 0, l, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            StringBuilder sBuilder = new StringBuilder();

            for (int i = 0; i < data.length; i++) {

                if (data[i] == 0x0)
                    break;

                sBuilder.append((char) data[i]);
            }

            result = sBuilder.toString();
        }
        StringBuilder sBuilder = new StringBuilder();

        return result;
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
        if (!checkPermission(activity, Manifest.permission.RECORD_AUDIO)) {
            list.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!checkPermission(activity, Manifest.permission.CAMERA)) {
            list.add(Manifest.permission.CAMERA);
        }
        if (list.size() > 0) {
            String[] permissions = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                permissions[i] = list.get(i);
            }
            ActivityCompat.requestPermissions(activity, permissions, 0);
        }
    }

    public static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
    }


    public static void showAlertDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.dialog_msg_no_permission));
        builder.setPositiveButton(context.getString(R.string.dialog_btn_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT >= 9) {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
                }
//                intent.setAction("android.intent.action.MAIN");
//                intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                context.startActivity(intent);

            }
        });
        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.show();
    }


    public static String sign(String[] datas) throws NoSuchAlgorithmException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < datas.length; i++) {
            sb.append(datas[i]);
        }
        String s = sb.toString();
        String strMd5 = getMD5(s);
        return strMd5;
    }

    public static String getMD5(String val) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(val.getBytes());
        byte[] m = md5.digest();//加密
        return getString(m);
    }

    public static boolean isValidUid(String uid) {
        return Pattern.matches("^[A-Z0-9]{20}$", uid);
    }

    public static String getFileNameWithTime(int type) {
        return new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + (type == 0 ? ".jpg" : ".mp4");
//        Calendar c = Calendar.getInstance();
//        int mYear = c.get(Calendar.YEAR);
//        int mMonth = c.get(Calendar.MONTH) + 1;
//        int mDay = c.get(Calendar.DAY_OF_MONTH);
//        int mHour = c.get(Calendar.HOUR_OF_DAY);
//        int mMinute = c.get(Calendar.MINUTE);
//        int mSec = c.get(Calendar.SECOND);
//        //		int mMilliSec = c.get(Calendar.MILLISECOND);
//
//        StringBuffer sb = new StringBuffer();
//        if (type == 0) {
//            sb.append("IMG_");
//        }
//        sb.append(mYear);
//        if (mMonth < 10)
//            sb.append('0');
//        sb.append(mMonth);
//        if (mDay < 10)
//            sb.append('0');
//        sb.append(mDay);
//        sb.append('_');
//        if (mHour < 10)
//            sb.append('0');
//        sb.append(mHour);
//        if (mMinute < 10)
//            sb.append('0');
//        sb.append(mMinute);
//        if (mSec < 10)
//            sb.append('0');
//        sb.append(mSec);
//
//
//        if (type == 0) {
//            sb.append(".jpg");
//        } else if (type == 1) {
//            sb.append(".mp4");
//        } else {
//
//        }
//
//        return sb.toString();
    }


    public static boolean saveBitmap(Bitmap bitmap, String fileName) {

        boolean result = false;
        if (bitmap == null || fileName.isEmpty()) {
            return false;
        }
        File tmpFile = new File(fileName + ".tmp");
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        FileOutputStream out;
        try {
            out = new FileOutputStream(tmpFile);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out)) {
                out.flush();
                out.close();
            }
            long l = tmpFile.length();
            if (l < 10000) {
                result = false;
                tmpFile.delete();
            } else {
                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
                tmpFile.renameTo(file);
                result = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return result;
    }


    public final static String getEventType(Context context, int eventType,
                                            boolean isSearch) {

        String result = "";

        switch (eventType) {
            case AVIOCTRLDEFs.AVIOCTRL_EVENT_ALL:
                result = isSearch ? context.getText(R.string.evttype_all)
                        .toString() : context.getText(
                        R.string.evttype_fulltime_recording).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT:
                result = context.getText(R.string.evttype_motion_detection)
                        .toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_VIDEOLOST:
                result = context.getText(R.string.evttype_video_lost).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARM:
                result = context.getText(R.string.evttype_io_alarm).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS:
                result = context.getText(R.string.evttype_motion_pass).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_VIDEORESUME:
                result = context.getText(R.string.evttype_video_resume).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS:
                result = context.getText(R.string.evttype_io_alarm_pass).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_EXPT_REBOOT:
                result = context.getText(R.string.evttype_expt_reboot).toString();
                break;

            case AVIOCTRLDEFs.AVIOCTRL_EVENT_SDFAULT:
                result = context.getText(R.string.evttype_sd_fault).toString();
                break;
        }

        return result;
    }

    public static void showShare(Context context, boolean silent, String platform, boolean captureView, String imagePath, String[] imageArray) {
        shareByEmail(context,"#" + MyConfig.getAppName() + "#",MyConfig.getAppName(),imageArray);
//        OnekeyShare oks = new OnekeyShare();
//        //关闭sso授权
//        oks.disableSSOWhenAuthorize();
//
//        // 分享时Notification的图标和文字  2.5.9以后的版本不     调用此方法
//        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
//        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
//        oks.setTitle("#" + MyConfig.getAppName() + "#");
//        // text是分享文本，所有平台都需要这个字段
//        oks.setText(MyConfig.getAppName());
//        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        if (imageArray != null && imageArray.length > 0) {
//            oks.setImageArray(imageArray);
//            oks.setImagePath(imageArray[0]);
//        } else if (imagePath != null) {
//            //��������ͼƬ������΢����������ͼƬ��Ҫͨ����˺�����߼�д��ӿڣ�������ע�͵���������΢��
//            //oks.setImagePath(imagePath);
//            if (imagePath.endsWith(".mp4")) {
//                oks.setFilePath(imagePath);
//
//            } else {
//                oks.setImagePath(imagePath);
//            }
//        }
//        oks.setSilent(silent);
//        //ָ������ƽ̨����slientһ��ʹ�ÿ���ֱ�ӷ��?ָ����ƽ̨
//        if (platform != null) {
//            oks.setPlatform(platform);
//        }
//        //  oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());
//        // 启动分享GUI
//        oks.show(context);
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }


    public Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;
                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));
                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);
                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    public static void addImageGallery(Context context, String filePath, String filename) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, filePath);//鍏蜂綋鍐呭
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg"); // 绫诲瀷
        context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            //鎻掑叆涓�寮犲浘鐗囧苟涓斿垱寤虹缉鐣ュ浘
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    filePath, filename, null);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));//閫氱煡濯掍綋搴撴暟鎹凡缁忔敼鍙橈紝灏嗘柊鐨勬枃浠舵壂鎻忓苟娣诲姞杩涘獟浣撳簱
    }

    public static int GetUIDIntValue(String uid) {
        int id = 0;
        if (uid != null && uid.length() == 20) {
            uid = uid.toUpperCase();
            for (int i = 0; i < 17; i++) {
                id += ((int) uid.charAt(i)) - 30 << i;
            }
        }
        return id * 200;
    }

    public static void showAlarmNotification(Context context, String uid, int evtType, long evtTime) {

        try {
            MyCamera camera = null;
            for (NSCamera _caemra : TwsDataValue.cameraList()) {
                if (uid!=null && uid.equals(_caemra.uid)) {
                    camera = (MyCamera) _caemra;
                    break;
                }
            }

            if (camera == null) {
                return;
            }

            if (!((MyCamera) camera).shouldPush()) {
                return;
            }
            String[] alarmList = context.getResources().getStringArray(R.array.tips_alarm_list_array);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
//            if(alarmList[evtType] == null){
//
//            }
            builder.setContentText(alarmList[0]);

            builder.setSmallIcon(MyConfig.getAppIconSource());
//            builder.setContentInfo("补充内容");
//            builder.setTicker("新消息");
            builder.setAutoCancel(true);
            builder.setWhen(System.currentTimeMillis());
            Intent intent = new Intent(context, com.tws.commonlib.start.MainActivity.class);
            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, camera.getUID());
            intent.putExtra("eventTime", evtTime);
            int notificationId = camera.getIntId();
            PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId+evtType, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            int eventnum = camera.refreshEventNum(context);
            //+ (evtType==0?" from server um":evtType==1?"from server xg":"from p2p")
            builder.setContentText(alarmList[0] + (eventnum > 1 ? (" +" + eventnum) : ""));
            builder.setContentTitle(camera.getName() + " [" + camera.getUID() + "]");
            Notification notification = builder.build();

            manager.notify(camera.getUID(), notificationId+evtType, notification);
            Intent newIntent = new Intent();
            newIntent.putExtra(TwsDataValue.EXTRA_KEY_UID,uid);
            newIntent.setAction(TwsDataValue.ACTION_CAMERA_REFRESH_ONE_ITEM);
            context.sendBroadcast(newIntent);

//                        XGLocalMessage local_msg = new XGLocalMessage();
//            // ���ñ�����Ϣ���ͣ�1:֪ͨ��2:��Ϣ
//            local_msg.setType(1);
//            // ������Ϣ����
//            local_msg.setTitle(camera.name + " [" + uid + "]");
//            HashMap cc = new HashMap();
//            cc.put("uid",uid);
//            local_msg.setCustomContent(cc);
//            local_msg.setNotificationId(notificationId);
//            // ������Ϣ����
//
//            local_msg.setContent(alarmList[0] + (eventnum > 1 ? (" +" + eventnum) : ""));
////            if (type < strAlarmType.length && type >= 0)
////                local_msg.setContent(strAlarmType[type]);
//            XGPushManager.setTag(context,((MyCamera) camera).getUID());
//            XGPushManager.addLocalNotification(context, local_msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
            * 渠道标志为：
            * 1，andriod（a）
            *
            * 识别符来源标志：
            * 1， wifi mac地址（wifi）；
            * 2， IMEI（imei）；
            * 3， 序列号（sn）；
            * 4， id：随机码。若前面的都取不到时，则随机生成一个随机码，需要缓存。
            *
            * @param context
  * @return
          */
    public static String getDeviceId(Context context) {
        StringBuilder deviceId = new StringBuilder();
        // 渠道标志
        deviceId.append("a");
        try {
            //wifi mac地址
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String wifiMac = info.getMacAddress();
            if(wifiMac != null && !wifiMac.isEmpty()){
                deviceId.append("wifi");
                deviceId.append(wifiMac);
                L.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            if(imei!= null && !imei.isEmpty()){
                deviceId.append("imei");
                deviceId.append(imei);
                L.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if(sn!= null && !sn.isEmpty()){
                deviceId.append("sn");
                deviceId.append(sn);
                L.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
            //如果上面都没有， 则生成一个id：随机码
            String uuid = getUUID(context);
            if(uuid!= null && !uuid.isEmpty()){
                deviceId.append("id");
                deviceId.append(uuid);
                L.e("getDeviceId : ", deviceId.toString());
                return deviceId.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID(context));
        }
        L.e("getDeviceId : ", deviceId.toString());
        return deviceId.toString();
    }
    /**
     * 得到全局唯一UUID
     */
    public static String getUUID(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String uuid = sp.getString("phone_uuid",null);
        if(uuid != null && !uuid.isEmpty()){

        }
        else{
            uuid = UUID.randomUUID().toString();
            sp.edit().putString("phone_uuid",uuid).commit();
        }
        L.e("getDeviceId", "getUUID : " + uuid);
        return uuid;
    }

    /**
     * 字符串转换成日期
     * @param str
     * @return date
     */
    public static Date Str2Date(String str, String pattern) {
        if(pattern == null){
            pattern = "yyyyMMddhhmmss";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public static String Date2Str(Date date,String pattern) {
        if(pattern == null){
            pattern = "yyyyMMddhhmmss";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return  format.format(date);
    }

    public  static  void  shareByEmail(Context context,String title,String body,String[] paths){
        if(paths.length > 1) {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.putExtra(Intent.EXTRA_SUBJECT, title);

            ArrayList<Uri> imageUris = new ArrayList<Uri>();
            for (int i = 0; i < paths.length; i++) {
                File file = new File(paths[i]);
                Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider",
                        file);
                imageUris.add(contentUri);
            }
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            if(paths[0].indexOf(".jpg") >-1 || paths[0].indexOf(".png") >-1|| paths[0].indexOf(".bmp")>-1 ) {
                intent.setType("image/*");
            }
            else{
                intent.setType("video/*");
            }
            intent.setType("message/rfc882");
            Intent.createChooser(intent, context.getString(R.string.lab_share_email_chooseClient));
            context.startActivity(intent);
        }
        else{
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            File file = new File(paths[0]);
            Uri contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider",
                    file);
            intent.putExtra(Intent.EXTRA_STREAM,contentUri);
            if(paths[0].indexOf(".jpg") >-1 || paths[0].indexOf(".png") >-1|| paths[0].indexOf(".bmp")>-1 ) {
                intent.setType("image/*");
            }
            else{
                intent.setType("video/*");
            }
            intent.setType("message/rfc882");
            Intent.createChooser(intent, context.getString(R.string.lab_share_email_chooseClient));
            context.startActivity(intent);
        }
    }
}
