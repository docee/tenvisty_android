package com.tws.commonlib.wificonfig;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.tws.commonlib.App;


/**
 * Created by Administrator on 2017/7/24.
 * AP6212配置WiFi
 */

public abstract class BaseConfig {
    OnReceivedListener listener;
    public  BaseConfig  setReceiveListner(OnReceivedListener l){
        this.listener = l;
        return this;
    }
    protected String ssid;
    protected String pwd;
    protected int authMode = -1;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    protected String uid;

    public void setAuthMode(int authMode) {
        this.authMode = authMode;
    }

    public int getAuthMode() {
        return authMode;
    }

    public String getSsid() {
        return ssid;
    }

    public String getPwd() {
        return pwd;
    }

    public int getWifiIpAddressInt() {
        WifiManager wifiManager = (WifiManager) App.getContext().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            return 0;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    public void set(String ssid, String pwd) {
        this.ssid = ssid;
        this.pwd = pwd;
    }

    public abstract void runConfig();

    public abstract void stopConfig();

    public  interface  OnReceivedListener{
        public  void  OnReceived(String status,String ip,String UID);
    }
}
