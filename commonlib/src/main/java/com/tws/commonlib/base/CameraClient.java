package com.tws.commonlib.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.App;
import com.tws.commonlib.R;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.db.DatabaseManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class CameraClient {
    static final String HOST_URL_second = "https://home.tenvis.com";
    static final String HOST_URL_first = "https://home.tenvis.com";
    public static final String HOST_URL = "http://push.tenvis.com:8001";
//	static final String HOST_URL_second = "https://test.tenvis.com";
//	static final String HOST_URL_first = "https://test.tenvis.com";
//	public static final String HOST_URL = "https://test.tenvis.com";
    //private String HOST_URL = "https://115.29.190.131";
    /*static final String HOST_URL_second = "http://182.254.209.140";
    static final String HOST_URL_first = "http://182.254.209.140";
	private String HOST_URL = "http://182.254.209.140";*/
    /*static final String HOST_URL_second = "http://192.168.0.45:8080";
    static final String HOST_URL_first = "http://192.168.0.45:8080";
	private String HOST_URL = "http://192.168.0.45:8080";*/

    /*static final String HOST_URL_second = "http://home.tenvis.com";
    static final String HOST_URL_first = "http://www.baidu.com";
    private String HOST_URL = "http://www.baidu.com";*/
    private boolean flagfirsttime = true;

    //static final String HOST_URL = "http://192.240.114.34";
    static final int URL_REQ_TIME = 10000;

    static final String SHARE_STATUS = "1";
    static final String PUBLIC_STATUS = "2";

    static final String CAMERA_DEFAULT_PASSWORD = "admin";

    // server sql
    public static final String OWNER_CAMERA_ID = "ownerCameraId";
    public static final String CAMERA_UID = "cameraUid";
    static final String CAMERA_NAME = "cameraName";
    static final String CAMERA_ACC = "cameraAcc";
    static final String CAMERA_PASSWORD = "cameraPassword";
    static final String CAMERA_IP = "ip";
    static final String CAMERA_PORT = "port";
    static final String CAMERA_DDNS = "ddns";
    static final String CAMERA_CHANNEL = "cameraChannel";
    static final String CAMERA_SHAREFROM = "cameraShareFrom";
    static final String CAMERA_STATUS = "cameraStatus";
    static final String CAMERA_USER_NAME = "cameraUserName";
    static final String CAMERA_PUSH_STATE = "pushState";

    static final String CAMERA_MODEL = "cameraModel";
    static final String CAMERA_MODEL_MJ = "MJ";
    static final String CAMERA_MODEL_H264 = "H264";
    static final String CAMERA_MODEL_SJ = "SJ";

    private String reqString;

    public interface ServerResultListener {

        void serverResult(String resultString, JSONArray jsonArray);
    }

    public interface ServerResultListener2 {

        void serverResult(String resultString, JSONObject jsonObj);
    }

    private ServerResultListener serverResultListener;

    public void addServerResultListener(ServerResultListener action) {
        serverResultListener = action;
    }

    public void removeServerResultListener() {
        serverResultListener = null;
    }


    static CameraClient cameraClient;
    public String userNameString;
    public String passwordString;
    public String newpasswordString;
    public boolean IsLoginSuccess;
    //final Activity cameraClientActivity = new Activity();

    public static CameraClient shareCameraClient() {
        if (cameraClient == null) {
            cameraClient = new CameraClient();
            System.out.println("shareCameraClient");
        }
        return cameraClient;
    }

    public void getHost() {
        //reqString ="/IpCamera/server!getInfo.action";
        //sendURLReq(reqString);
    }

    //添加摄像机
    public void addCamera(IMyCamera camera) {
        saveToDatabase(camera);
    }

    //删除摄像机
    public void deleteCamera(String serverDatabaseId, String cameraStatuString,
                             String uidString) {
        deleteCamera(uidString);
    }



    private String getString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(b[i]);
        }
        return sb.toString();
    }

    public static String getMD5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
//    public String getMD5(String val) throws NoSuchAlgorithmException {
//        MessageDigest md5 = MessageDigest.getInstance("MD5");
//        md5.update(val.getBytes());
//        byte[] m = md5.digest();//加密
//        return getString(m);
//    }

    public String createSign(String uid, String timestamp, String secret) throws NoSuchAlgorithmException {
        String origin = uid + timestamp + secret;
        String sign = getMD5(origin);
        return sign;
    }

    public String createSign(String[] paras) {
        Arrays.sort(paras);
        String origin = "";
        for (int i = 0; i < paras.length; i++) {
            origin += paras[i];
        }
        String sign = getMD5(origin);
        return sign;
    }

    //开启Push功能
    //cameraSupplier对应不同固件方案商不同eventType定义
    public void openPushCamera(Context context, String uidString,String cameraFeature, ServerResultListener2 succListener, ServerResultListener2 errorListener) {
        if (uidString == null || uidString.length() == 0) {
            System.out.println("openPushCamera camera fail,uidString is null");
            return;
        }
        if(TwsDataValue.XGToken == null){
            TwsDataValue.XGToken = "";
        }
        if( TwsDataValue.UMToken == null){
            TwsDataValue.UMToken = "";
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        String appid = MyConfig.getPackageName();
        String sign = createSign(new String[]{uidString, TwsDataValue.XGToken, TwsDataValue.UMToken,timestamp, appid,cameraFeature,  "tenvisapp"});
        reqString = "/api/push/open?token1=" + TwsDataValue.XGToken + "&token2=" + TwsDataValue.UMToken + "&uid=" + uidString + "&appid=" + appid + "&timestamp=" + timestamp + "&sign=" + sign + "&platform=android&feature="+cameraFeature;
        sendURLReq(reqString, succListener, errorListener);
    }

    //关闭Push功能
    public void closePushCamera(String uidString) {
        if (uidString == null || uidString.length() == 0) {
            System.out.println("closePushCamera camera fail,uidString is null");
            return;
        }
        String timestamp = System.currentTimeMillis() / 1000 + "";
        String appid = MyConfig.getPackageName();
        String sign = createSign(new String[]{uidString, TwsDataValue.XGToken, TwsDataValue.UMToken, timestamp, appid, "tenvisapp"});
        reqString = "/api/push/Close?token1=" + TwsDataValue.XGToken + "&token2=" + TwsDataValue.UMToken + "&uid=" + uidString + "&appid=" + appid + "&timestamp=" + timestamp + "&sign=" + sign + "&platform=android";
        sendURLReq(reqString);
    }

    public void closePushCameraByPhone(String uidString, String phoneTotken) throws NoSuchAlgorithmException {
        if (uidString == null || uidString.length() == 0 || phoneTotken == null || phoneTotken.length() == 0) {
            System.out.println("closePushCamera camera fail,uidString or phoneTotken is null");
            return;
        }

        long timeStamp = System.currentTimeMillis() / 1000;
        //secret = "tenvisapp"
        String sign = TwsTools.sign(new String[]{uidString, timeStamp + "", "tenvisapp"});
        //String
        //string uid, string deviceToken, string appid, string sign, string timestamp
        // reqString = String.format("/api/push/close?uid=%s&deviceToken=%s&appid=%s&sign=%s&timestamp=%s", uidString, phoneTotken, GlobalConfig.GetInstance(null).getAppName(), sign, timeStamp + "");
        //System.out.println("reqString " + reqString);
        // sendURLReq(reqString);
        closePushCamera(uidString);
    }


    private void sendURLReq(final String reqString) {
        L.i("httprequest", reqString);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream instream = null;
                try {
                    HttpClient httpclient = SSLTrustAllSocketFactory.getClient();

                    // Prepare a request object
                    HttpGet httpget = new HttpGet(HOST_URL + reqString);
                    L.i("Login http--->>", HOST_URL + reqString + "--flagfirsttime=" + flagfirsttime);
                    // Execute the request
                    HttpResponse response;

                    HttpConnectionParams.setConnectionTimeout(
                            httpclient.getParams(), URL_REQ_TIME);//设置请求连接超时20s
                    HttpConnectionParams.setSoTimeout(httpclient.getParams(),
                            URL_REQ_TIME);//设置等待数据超时

                    httpclient.getParams().setParameter(
                            ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
                    System.out.println("connecting...");
                    response = httpclient.execute(httpget);

                    System.out.println("connected");
                    // Get hold of the response entity
                    HttpEntity entity = response.getEntity();

                    if (entity != null) {

                        // A Simple JSON Response Read
                        instream = entity.getContent();
                        final String result = convertStreamToString(instream);

                        L.i("result", result);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("parseJson");
                                parseJson(result);
                            }
                        });
                    }

                } catch (ConnectTimeoutException connectTimeoutException) {

                    if (flagfirsttime) {
                        flagfirsttime = false;
                        setHOST_URL();
                        //sendURLReq(reqString);
                        //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
                    } else {
                        flagfirsttime = true;
                        setHOST_URL();
                        sendServerResult(R.string.toast_connect_timeout);
                    }
                    //	Log.i("result", "connect_timeout");
                    //如果请求超时则自动连接另外的服务器-->>

                } catch (SocketTimeoutException socketTimeoutException) {
                    if (flagfirsttime) {
                        flagfirsttime = false;
                        setHOST_URL();
                        // sendURLReq(reqString);
                        //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
                    } else {
                        flagfirsttime = true;
                        setHOST_URL();
                        sendServerResult(R.string.toast_connect_timeout);
                    }
                    //	Log.i("result", "connect_timeout--sock");
                    //如果返回数据超时则连接到另一个服务器-->>

                } catch (Exception e) {
                    if (flagfirsttime) {
                        flagfirsttime = false;
                        setHOST_URL();
                        //sendURLReq(reqString);
                        //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();

                    } else {
                        flagfirsttime = true;
                        setHOST_URL();
                        // sendServerResult(R.string.network_error);
                    }
                    Log.i("result", "network_error");
                    e.printStackTrace();

                } finally {
                    if (instream != null) {
                        try {
                            instream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

            private void sendServerResult(final int errorId) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        //Log.v("result", "sendServerResult(final int errorId)");
                        if (serverResultListener != null) {
                            serverResultListener.serverResult(App
                                    .getResourceString(errorId), null);
                        }
                    }
                });
            }

        });
        thread.start();
    }

    private void sendURLReq(final String reqString, final ServerResultListener2 succListener, final ServerResultListener2 errorLisenter) {
        L.i("httprequest", reqString);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream instream = null;
                try {
                    HttpClient httpclient = SSLTrustAllSocketFactory.getClient();

                    // Prepare a request object
                    HttpGet httpget = new HttpGet(HOST_URL + reqString);
                    L.i("Login http--->>", HOST_URL + reqString + "--flagfirsttime=" + flagfirsttime);
                    // Execute the request
                    HttpResponse response;

                    HttpConnectionParams.setConnectionTimeout(
                            httpclient.getParams(), URL_REQ_TIME);//设置请求连接超时20s
                    HttpConnectionParams.setSoTimeout(httpclient.getParams(),
                            URL_REQ_TIME);//设置等待数据超时

                    httpclient.getParams().setParameter(
                            ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
                    System.out.println("connecting...");
                    response = httpclient.execute(httpget);

                    System.out.println("connected");
                    // Get hold of the response entity
                    HttpEntity entity = response.getEntity();

                    if (entity != null) {

                        // A Simple JSON Response Read
                        instream = entity.getContent();
                        final String result = convertStreamToString(instream);
                        parseJson(result, succListener, errorLisenter);
                        L.i("result", result);
                    }

                } catch (ConnectTimeoutException connectTimeoutException) {

                    if (flagfirsttime) {
                        flagfirsttime = false;
                        setHOST_URL();
                        //sendURLReq(reqString);
                        //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
                    } else {
                        flagfirsttime = true;
                        setHOST_URL();
                        sendServerResult(R.string.toast_connect_timeout);
                    }
                    //	Log.i("result", "connect_timeout");
                    //如果请求超时则自动连接另外的服务器-->>

                } catch (SocketTimeoutException socketTimeoutException) {
                    if (flagfirsttime) {
                        flagfirsttime = false;
                        setHOST_URL();
                        // sendURLReq(reqString);
                        //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
                    } else {
                        flagfirsttime = true;
                        setHOST_URL();
                        sendServerResult(R.string.toast_connect_timeout);
                    }
                    //	Log.i("result", "connect_timeout--sock");
                    //如果返回数据超时则连接到另一个服务器-->>

                } catch (Exception e) {
                    if (flagfirsttime) {
                        flagfirsttime = false;
                        setHOST_URL();
                        //sendURLReq(reqString);
                        //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();

                    } else {
                        flagfirsttime = true;
                        setHOST_URL();
                        // sendServerResult(R.string.network_error);
                    }
                    Log.i("result", "network_error");
                    e.printStackTrace();

                } finally {
                    if (instream != null) {
                        try {
                            instream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }

            private void sendServerResult(final int errorId) {
                if (errorLisenter != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            if (errorLisenter != null) {
                                //Log.v("result", "sendServerResult(final int errorId)");
                                errorLisenter.serverResult(App
                                        .getResourceString(errorId), null);
                            }
                        }
                    });
                }
            }

        });
        thread.start();
    }

    private static String convertStreamToString(InputStream is)
            throws IOException {
        /*
         * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private void parseJson(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            String resultsString = jsonObject.getString("result").toString();

            //Log.i("result", resultsString);
            if (resultsString.equals("LOGIN_OK")) {

                if (jsonArray.length() > 1 && jsonArray.getJSONObject(1).isNull("username") == false) {
                    String string = jsonArray.getJSONObject(1).getString("username").toString();
                    if (string != null && string.length() != 0) {
                        userNameString = new String(string);
                    }
                }

                getCameraList(jsonArray);
                saveUserToDatabase(userNameString, passwordString);
            } else if (resultsString.equals("REGIST_OK")) {
                saveUserToDatabase(userNameString, passwordString);
            } else if (resultsString.equals("MODIFY_OK")) {//更改用户密码成功
                //passwordString=newpasswordString;
                passwordString = "";
                saveUserToDatabase(userNameString, passwordString);
            }
//			else if (resultsString.equals("GET_SERVER_INFO_OK")) {//获取优先使用的域名成功，将访问的域名设为优先访问的域名，失败不做处理
//				if (jsonArray.length() > 1 && jsonArray.getJSONObject(1).isNull("domainName") == false) {
//					String domainString = jsonArray.getJSONObject(1).getString("domainName").toString().trim();
//					if (domainString != null && domainString.length() != 0) {
//						HOST_URL = domainString;
//					}
//				}
//				return;
//			}
            else if (resultsString.equals("GET_SERVER_INFO_FAIL")) {//获取失败不做处理
                return;
            }/*else if (resultsString.equals("updateDdnsById_OK")) {
                return;
			}*/
            if (serverResultListener != null) {
                serverResultListener.serverResult(resultsString, jsonArray);
            }

        } catch (Exception e) {//有返回但是不是我们的服务器返回的--数据不是json--报服务器错误
            e.printStackTrace();
            /*if (serverResultListener != null) {
                if (flagfirsttime) {
					flagfirsttime=false;
					setHOST_URL();
					sendURLReq(reqString);
					//Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
				}else {
					flagfirsttime=true;
					setHOST_URL();
					serverResultListener.serverResult(App
							.getResourceString(R.string.server_error),null);
				}				
			}*/
            if (flagfirsttime) {
                flagfirsttime = false;
                setHOST_URL();
                sendURLReq(reqString);
                //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
            } else {
                flagfirsttime = true;
                setHOST_URL();
                if (serverResultListener != null) {
                    serverResultListener.serverResult(String.format(App
                            .getResourceString(R.string.toast_connect_timeout), MyConfig.getApkName()), null);
                }
            }

        }
    }

    private void parseJson(String result, ServerResultListener2 succlistener, ServerResultListener2 errorlistener) {
        try {
//            JSONArray jsonArray = new JSONArray(result);
//            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONObject jsonObj = new JSONObject(result);
            if (succlistener != null) {
                succlistener.serverResult(result, jsonObj);
            }

        } catch (Exception e) {//有返回但是不是我们的服务器返回的--数据不是json--报服务器错误
            e.printStackTrace();
            /*if (serverResultListener != null) {
                if (flagfirsttime) {
					flagfirsttime=false;
					setHOST_URL();
					sendURLReq(reqString);
					//Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
				}else {
					flagfirsttime=true;
					setHOST_URL();
					serverResultListener.serverResult(App
							.getResourceString(R.string.server_error),null);
				}
			}*/
            if (flagfirsttime) {
                flagfirsttime = false;
                setHOST_URL();
                sendURLReq(reqString, succlistener, errorlistener);
                //Toast.makeText(App.getContext(), "正在尝试第二次连接",Toast.LENGTH_SHORT).show();
            } else {
                flagfirsttime = true;
                setHOST_URL();
                if (errorlistener != null) {
                    errorlistener.serverResult(String.format(App
                            .getResourceString(R.string.toast_connect_timeout), MyConfig.getApkName()), null);
                }
            }

        }
    }

    public void saveUserToDatabase(String userNameString_,
                                   String passwordString_) {
        DatabaseManager manager = new DatabaseManager(App.getContext());
        manager.saveUser(userNameString_, passwordString_);
    }

    private void getCameraList(JSONArray jsonArray) {

//		DatabaseManager manager = new DatabaseManager(App.getContext());
//		SQLiteDatabase db = manager.getReadableDatabase();
//		db.execSQL(DatabaseHelper.SQLCMD_DROP_TABLE_DEVICE);
//		db.execSQL(DatabaseHelper.SQLCMD_CREATE_TABLE_DEVICE);

        for (int i = 2; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (App.getContext() != null) {

                    String namesString = jsonObject.getString(CAMERA_NAME)
                            .toString();
                    String uidString = jsonObject.getString(CAMERA_UID)
                            .toString();
                    String passwordString = jsonObject.getString(
                            CAMERA_PASSWORD).toString();
                    String cameraModelString = jsonObject.getString(
                            CAMERA_MODEL).toString();
                    String cameraUserString = jsonObject.getString(
                            CAMERA_ACC).toString();

                    String statusString = jsonObject.getString(CAMERA_STATUS)
                            .toString();
                    String cameraShareFormString = jsonObject.getString(CAMERA_SHAREFROM);
                    String serverDatabaseIdString = jsonObject.getString(OWNER_CAMERA_ID);
                    int channel = Integer.valueOf(jsonObject.getString(
                            CAMERA_CHANNEL).toString());

					/*System.out.println("getCameraList " + "cameraModelString " + cameraModelString + " "
                            + uidString + " " + cameraUserString + " "  + passwordString );*/
                    if (cameraModelString == null || cameraModelString.equals(CAMERA_MODEL_H264) || cameraModelString.isEmpty()) {
                        String pushStateString = jsonObject.getString(
                                CAMERA_PUSH_STATE).toString();

                        IMyCamera myCamera = IMyCamera.MyCameraFactory.shareInstance().createCamera(namesString, uidString, cameraUserString, passwordString);
                        myCamera.setCameraModel(NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264.ordinal());
                        if(serverDatabaseIdString == null) {
                            myCamera.setDatabaseId(Long.parseLong(serverDatabaseIdString));
                        }
                        myCamera.setPushOpen(pushStateString == null);

                        TwsDataValue.cameraList.add(myCamera);
                    } else if (cameraModelString.equals(CAMERA_MODEL_SJ)) {
                        String pushStateString = jsonObject.getString(
                                CAMERA_PUSH_STATE).toString();

                        IMyCamera myCamera = IMyCamera.MyCameraFactory.shareInstance().createCamera(namesString, uidString, cameraUserString, passwordString);
                        myCamera.setCameraModel(NSCamera.CAMERA_MODEL.CAMERA_MODEL_H264.ordinal());
                        if(serverDatabaseIdString == null) {
                            myCamera.setDatabaseId(Long.parseLong(serverDatabaseIdString));
                        }
                        myCamera.setPushOpen(pushStateString == null);
                        TwsDataValue.cameraList.add(myCamera);
                    }
                } else {
                    System.out
                            .println("getCameraList fail because context is null");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void setHOST_URL() {
//		if (HOST_URL.equals(HOST_URL_first)) {
//			HOST_URL=HOST_URL_second;
//		}else {			
//			HOST_URL = HOST_URL_first;
//		}
    }

    //绑定推送Token与设备
    public void BindDevicePushToken(String pushtoken, Context context) {
        String app = context.getPackageName();
        reqString = "/IpCamera/userLogin!BindPhonePushToken.action?pushtoken=" + pushtoken + "&app=" + app + "&os=android&phoneId=" + android.os.Build.SERIAL;
        sendURLReq(reqString);
    }

    //绑定用户与设备
    public void BindUserDevice(Context context) {
        if (this.userNameString != null && this.userNameString.length() > 0) {
            String app = context.getPackageName();
            reqString = "/IpCamera/userLogin!BindUserPhone.action?username=" + this.userNameString + "&app=" + app + "&os=android&phoneId=" + android.os.Build.SERIAL;
            sendURLReq(reqString);
        }
    }

    //取消绑定用户与设备
    public void UnBindUserDevice(Context context) {
        if (this.userNameString != null && this.userNameString.length() > 0) {
            String app = context.getPackageName();
            reqString = "/IpCamera/userLogin!UnBindUserPhone.action?username=" + this.userNameString + "&app=" + app + "&os=android&phoneId=" + android.os.Build.SERIAL;
            sendURLReq(reqString);
        }
    }

    private void saveToDatabase(IMyCamera camera) {
        JSONArray data = new JSONArray();
        try {
            DatabaseManager manager = new DatabaseManager(App.getContext());
            long db_id = manager.addDevice(camera.getAccount(), camera.getUid(), "", "", "admin", camera.getPassword(), 0, 0, "0", camera.getVideoQuality(), "", 0);
            JSONObject obj = new JSONObject();
            obj.put(CameraClient.CAMERA_UID, camera.getUid());
            obj.put(CameraClient.OWNER_CAMERA_ID, db_id);
            data.put(1, obj);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (serverResultListener != null) {
                serverResultListener.serverResult("ADD_FAIL_UID_EXIST", data);
            }
            return;
        }
        if (serverResultListener != null) {
            serverResultListener.serverResult("ADD_OK", data);
        }
        //VideoList.cameraList.add(myCamera);
    }

    public void deleteCamera(String uid) {
        try {
            DatabaseManager manager = new DatabaseManager(App.getContext());
            //SQLiteDatabase db= manager.getReadableDatabase();
            manager.removeDeviceByUID(uid);
            if (serverResultListener != null) {
                serverResultListener.serverResult("DELETE_OK", new JSONArray());
            }
        } catch (Exception e) {
            if (serverResultListener != null) {
                serverResultListener.serverResult("DELETE_FAIL", new JSONArray());
            }
        }
    }
}
