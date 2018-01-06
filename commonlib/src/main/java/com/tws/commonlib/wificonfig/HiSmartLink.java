package com.tws.commonlib.wificonfig;

import com.hichip.HiSmartWifiSet;

/**
 * Created by Administrator on 2017/7/24.
 * AP6212配置WiFi
 */

public class HiSmartLink extends BaseConfig {
    private Thread mThread = null;
    private boolean mDone = false;
    private int mIP;//IP
    private  HiSmartLink(){

    }
    private  static HiSmartLink instance;
    public synchronized static HiSmartLink singleInstance(){
        if(instance == null){
            instance = new HiSmartLink();
        }
        return  instance;
    }
    @Override
    public void runConfig() {
        if(ssid == null || pwd == null){
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            HiSmartWifiSet.HiStartSmartConnection(ssid, pwd, (byte) authMode);
        }
    }

    @Override
    public void stopConfig() {
        HiSmartWifiSet.HiStopSmartConnection();
    }
}
