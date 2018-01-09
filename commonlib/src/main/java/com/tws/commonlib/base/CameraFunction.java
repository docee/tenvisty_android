package com.tws.commonlib.base;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;

import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.db.DatabaseManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

@SuppressWarnings("deprecation")
public class CameraFunction {

    //判断是否和手机WIFI在同一个局域网
    private static boolean isSameNetwork(Context context, int cameraIP, int cameraNetmask) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启  
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            DhcpInfo di = wifiManager.getDhcpInfo();

            if (cameraIP != ipAddress && cameraNetmask == di.netmask) {
                if ((cameraIP & cameraNetmask) == (ipAddress & di.netmask)) {
                    return true;
                }
            }

        }

        return false;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    private static int ipToInt(String ip) {
        int intIP = 0;
        String[] splitNum = ip.split("\\.");
        for (int i = 0; i < splitNum.length; i++) {
            try {
                intIP += Integer.parseInt(splitNum[i]) << i * 8;
            } catch (NumberFormatException ex) {

            }
        }
        return intIP;
    }

    public static void DoCameraFunctionFlag(final Context context, final HichipCamera camera, final String ip, final String netmask) {

        if (!isSameNetwork(context, ipToInt(ip), ipToInt(netmask))) {
            return;
        }

        //String getUIDUrl = "http://" + ip + "/web/cgi-bin/hi3510/param.cgi?cmd=gethip2pattr";
        String getFuncUrl = "http://" + ip + "/web/function.ini";

        final HttpGet mHttpc = new HttpGet(getFuncUrl);
        mHttpc.addHeader("Authorization", "Basic " + Base64.encodeToString(("admin:" + camera.getPassword()).getBytes(), Base64.DEFAULT).trim());
        try {
            HttpResponse httpResponse = new DefaultHttpClient().execute(mHttpc);
            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    try {
                        byte[] result = EntityUtils.toByteArray(httpResponse.getEntity());
                        camera.setFunctionFlag(context, result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
