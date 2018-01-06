package com.tws.commonlib.base;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/7/7.
 */

public class TwsToast {
    //static Toast toast;
    public static void showToast(Context context, String str) {
        if(context != null) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }
}
