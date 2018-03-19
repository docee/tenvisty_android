package com.tws.commonlib.wificonfig;

import android.util.Log;

import com.broadcom.cooee.Cooee;

/**
 * Created by Administrator on 2017/7/24.
 * AP6212配置WiFi
 */

public class CooConfig extends BaseConfig {
    private Thread mThread = null;
    private boolean mDone = false;
    private int mIP;//IP
    private int ssidTypeValue = 4;   //认证类型  默认是4
    private int ssidAlgValue = 4;    //加密算法  默认是4

    private CooConfig() {

    }

    private static CooConfig instance;

    public synchronized static CooConfig singleInstance() {
        if (instance == null) {
            instance = new CooConfig();
        }
        return instance;
    }

    @Override
    public void runConfig() {
        if (!mDone) {
            mDone = true;
            mIP = getWifiIpAddressInt();
            final String ssid = super.ssid + ":AONI_IPC:0:" + ssidTypeValue + ":" + ssidAlgValue; // 必须添加后缀 ":AONI_IPC:0:0:0"
            final String password = super.pwd;
            if (mThread == null) {
                mThread = new Thread() {
                    public void run() {
                        long times = 1;
                        while (mDone) {
                            //Cooee.send(ssid, password, mLocalIp, "0123456789abcdef");
                            Cooee.send(ssid, password, mIP);
//                            Cooee.send(ssid, password);
                            Log.e("正在发送广播", "");
                            try {
                                times++;
                                if (times % 1000 == 0) {
                                    Thread.sleep(500);
                                } else {
                                    //Thread.sleep(10);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
            mThread.start();
        } else {
            mDone = false;
            mThread = null;
        }
    }

    @Override
    public void stopConfig() {
        mDone = false;
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
    }
}
