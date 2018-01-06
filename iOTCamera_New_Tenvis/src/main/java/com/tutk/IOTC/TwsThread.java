package com.tutk.IOTC;

/**
 * Created by Administrator on 2017/7/21.
 */


public class TwsThread extends Thread {
    private Object mWaitObject = new Object();
    protected boolean isRunning;

    public TwsThread() {
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
            synchronized(this.mWaitObject) {
                this.mWaitObject.wait((long)ms);
            }
        } catch (InterruptedException var4) {
            var4.printStackTrace();
        }

    }

    public void weakup() {
        Object var1 = this.mWaitObject;
        synchronized(this.mWaitObject) {
            this.mWaitObject.notify();
        }
    }
}
