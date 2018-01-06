package com.tws.commonlib.base;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectionChangeReceiver extends BroadcastReceiver {  
  
    @Override  
    public void onReceive(Context context, Intent intent) {

        ConnectionState.getInstance(context).CheckConnectState();

    }  
  
}  