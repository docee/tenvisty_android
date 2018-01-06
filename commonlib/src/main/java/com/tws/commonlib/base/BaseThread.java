package com.tws.commonlib.base;

/**
 * Created by Administrator on 2017/9/23.
 */

public class BaseThread extends Thread {

    private Object mWaitObject = new Object();
    protected boolean isRunning;

    public BaseThread() {
    }

    public void stopThread() {
        this.isRunning = false;
        this.weakup();
    }

    public void startThread() {
        this.isRunning = true;
        this.start();
    }

    protected void sleep(int ms) {
        try {
            Object e = this.mWaitObject;
            synchronized (this.mWaitObject) {
                this.mWaitObject.wait((long) ms);
            }
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }

    }

    public void weakup() {
        Object var1 = this.mWaitObject;
        synchronized (this.mWaitObject) {
            this.mWaitObject.notify();
        }
    }


}
