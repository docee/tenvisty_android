package com.tutk.IOTC;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

/**
 * Created by Administrator on 2017/10/11.
 */


public class HiGLMonitor extends GLSurfaceView implements ICameraYUVCallback{
    private static final float DEFAULT_MAX_ZOOM_SCALE = 2.0f;
    private HiGLRender renderer = null;

    public int getmCurVideoWidth() {
        return mCurVideoWidth;
    }

    private int mCurVideoWidth = 0;

    public int getmCurVideoHeight() {
        return mCurVideoHeight;
    }

    private int mCurVideoHeight = 0;
    private byte[] videoBuffer;
    protected NSCamera mCamera;
    protected int mAVChannel = -1;
    private float mCurrentScale = 1.0f;
    private float mCurrentMaxScale = DEFAULT_MAX_ZOOM_SCALE;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            HiGLMonitor.this.renderer.writeSample(HiGLMonitor.this.videoBuffer, HiGLMonitor.this.mCurVideoWidth, HiGLMonitor.this.mCurVideoHeight);
        }
    };

    public HiGLMonitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.renderer = new HiGLRender(this);
        this.setEGLContextClientVersion(2);
        this.setRenderer(this.renderer);
    }

    public HiGLMonitor(Context context) {
        super(context);
        this.renderer = new HiGLRender(this);
        this.setEGLContextClientVersion(2);
        this.setRenderer(this.renderer);
    }

    @Override
    public  void onResume(){
        super.onResume();
    }

    public void setMatrix(int x, int y, int w, int h) {
        if(this.renderer != null) {
            this.renderer.setMatrix(x, y, w, h);
        }
    }

    protected void setYuvFrameData(byte[] yuvdata, int w, int h) {
        if(this.mCurVideoWidth != w || this.mCurVideoHeight != h) {
            this.mCurVideoWidth = w;
            this.mCurVideoHeight = h;
            this.videoBuffer = null;
            this.videoBuffer = new byte[this.mCurVideoWidth * this.mCurVideoHeight * 3 / 2 + 256];
        }

        if(yuvdata.length <= this.videoBuffer.length) {
            System.arraycopy(yuvdata, 0, this.videoBuffer, 0, yuvdata.length);
            Message msg = new Message();
            this.handler.sendMessage(msg);
        }
    }

    @Override
    public void callbackYUVData(Camera camera,int channel, byte[] yuvdata, int length,int w,int h) {
        if(channel == mAVChannel) {
            if (this.mCurVideoWidth != w || this.mCurVideoHeight != h) {
                this.mCurVideoWidth = w;
                this.mCurVideoHeight = h;
                this.videoBuffer = null;
                this.videoBuffer = new byte[this.mCurVideoWidth * this.mCurVideoHeight * 3 / 2 + 256];
            }

            if (yuvdata.length <= this.videoBuffer.length) {
                System.arraycopy(yuvdata, 0, this.videoBuffer, 0, yuvdata.length);
                Message msg = new Message();
                this.handler.sendMessage(msg);
            }

        }
    }

    public void attachCamera(NSCamera camera, int avChannel) {
        mCamera = camera;
        mCamera.registerYUVDataListener(this);
        mAVChannel = avChannel;
    }
    public void deattachCamera() {

        mAVChannel = -1;

        if (mCamera != null) {
            mCamera.unregisterYUVDataListener(this);
            mCamera = null;
        }
    }
    public void setMaxZoom(float value) {
        mCurrentMaxScale = value;
    }
}
