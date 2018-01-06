package com.tws.commonlib.base;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/7/11.
 */

public class TwsProgressDialog extends android.app.ProgressDialog {
    private long mTimeOut = 0;// 默认timeOut为0即无限大
    private OnTimeOutListener mTimeOutListener = null;// timeOut后的处理器
    private Timer mTimer = null;// 定时器
    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            if(mTimeOutListener != null){
                mTimeOutListener.onTimeOut(TwsProgressDialog.this);
                if(TwsProgressDialog.this.isShowing()) {
                    cancel();
                }
            }
        }
    };

    public TwsProgressDialog(Context context) {
        super(context);
    }

    public void setTimeOut(long t, OnTimeOutListener timeOutListener) {
        mTimeOut = t;
        if (timeOutListener != null) {
            this.mTimeOutListener = timeOutListener;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTimer != null) {

            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mTimeOut != 0) {
            mTimer = new Timer();
            TimerTask timerTast = new TimerTask() {
                @Override
                public void run() {
                    //    dismiss();
                    Message msg = mHandler.obtainMessage();
                    mHandler.sendMessage(msg);
                }
            };
            mTimer.schedule(timerTast, mTimeOut);
        }

    }
    @Override
    public  void  cancel(){
        super.cancel();
        if(mTimeOutListener != null){
            mTimeOutListener = null;
        }
        if(mTimer!=null) {
            mTimer.cancel();
        }
    }

    public static TwsProgressDialog createProgressDialog(Context context,
                                                      long time, OnTimeOutListener listener) {
        TwsProgressDialog progressDialog = new TwsProgressDialog(context);
        if (time != 0) {
            progressDialog.setTimeOut(time, listener);
        }
        return progressDialog;
    }

    public interface OnTimeOutListener {
        void onTimeOut(TwsProgressDialog dialog);
    }
}
