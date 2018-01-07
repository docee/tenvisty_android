package com.tws.commonlib.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.HiGLMonitor;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.MyCamera;

/**
 * Created by Administrator on 2017/10/11.
 */


public class MyLiveViewGLMonitor extends HiGLMonitor implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private static final int PTZ_DELAY = 1500;
    private long ptzTime = 0;
    private GestureDetector mGestureDetector;
    private static final int FLING_MIN_DISTANCE = 50;
    public static int PTZ_STEP = 50;//云台步长

    Matrix matrix = new Matrix();

    private View.OnTouchListener mOnTouchListener;

    private Activity context;

    private int state = 0;//normal=0, larger=1,two finger touch=3
    private int touchMoved;  //not move=0,  move=1, two point=2

    private SurfaceHolder sfh;
    public int left;
    public int width;
    public int height;
    public int bottom;
    public float screen_width;
    public float screen_height;
    int tempWidth;
    int tempHeight;
    float tempScreenWidth;
    float tempScreenHeight;
    int phoneScreenWidth;
    int phoneScreenHeight;


    public void setSetFixedScale(boolean setFixedScale) {
        this.setFixedScale = setFixedScale;
    }

    boolean setFixedScale = true;

    public MyLiveViewGLMonitor(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, this);
        super.setOnTouchListener(this);
        setOnTouchListener(this);
        setFocusable(true);
        setClickable(true);
        setLongClickable(true);
        this.context = (Activity) context;
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        phoneScreenHeight = dm.heightPixels;
        phoneScreenWidth = dm.widthPixels;
//         DisplayMetrics dm = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//        fullscreen_width = dm.widthPixels;
//        fullscreen_height = dm.heightPixels;
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                MyLiveViewGLMonitor.this.screen_width = MyLiveViewGLMonitor.this.getMeasuredWidth();
//                MyLiveViewGLMonitor.this.screen_height = MyLiveViewGLMonitor.this.getMeasuredHeight();
//            }
//        }, 100);
    }


    public int getTouchMove() {
        return this.touchMoved;
    }

    public void setTouchMove(int touchMoved) {
        this.touchMoved = touchMoved;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //		HiLog.e("==========MyGLMonitor  onPause===========");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        this.screen_width = this.getMeasuredWidth();
        this.screen_height = this.getMeasuredHeight();
    }

    //View当前的位置
    private float rawX = 0;
    private float rawY = 0;
    //View之前的位置
    private float lastX = 0;
    private float lastY = 0;

    int xlenOld;
    int ylenOld;

    private int pyl = 20;
    double nLenStart = 0;

    @SuppressLint("WrongCall")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        //		HiLog.v("onTouch:");

        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(v, event);
        }

        int nCnt = event.getPointerCount();
        L.i("IOTCamera", "mMonitor.state=" + state);
        if (state == 1) {
            if (nCnt == 2) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    L.i("IOTCamera", "mMonitor.ACTION_DOWN");
                    //获取手指落下的坐标并保存
                    rawX = (event.getRawX());
                    rawY = (event.getRawY());
                    lastX = rawX;
                    lastY = rawY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchMoved == 2) {
                        break;
                    }

                    L.i("IOTCamera", "mMonitor.ACTION_MOVE");
                    //手指拖动时，获得当前位置
                    rawX = event.getRawX();
                    rawY = event.getRawY();
                    //手指移动的x轴和y轴偏移量分别为当前坐标-上次坐标
                    float offsetX = rawX - lastX;
                    float offsetY = rawY - lastY;
                    //通过View.layout来设置左上右下坐标位置
                    //获得当前的left等坐标并加上相应偏移量
                    if (Math.abs(offsetX) < pyl && Math.abs(offsetY) < pyl) {

                        return false;
                    }

                    left += offsetX;
                    bottom -= offsetY;

                    if (left > 0) {
                        left = 0;
                    }
                    if (bottom > 0) {
                        bottom = 0;
                    }

                    if ((left + width < (screen_width))) {
                        left = (int) (screen_width - width);
                    }

                    if (bottom + height < screen_height) {
                        bottom = (int) (screen_height - height);
                    }


                    if (left <= (-width)) {
                        left = (-width);
                    }

                    if (bottom <= (-height)) {
                        bottom = (-height);
                    }


                    setMatrix(left,
                            bottom,
                            width,
                            height);
                    //移动过后，更新lastX与lastY
                    lastX = rawX;
                    lastY = rawY;
                    break;
            }
            return true;
        } else if (state == 0 && nCnt == 1) {
            L.i("IOTCamera", "mMonitor.mGestureDetector");
            return mGestureDetector.onTouchEvent(event);
        }
        return true;

		/*
        if(mOnTouchListener != null) {
			mOnTouchListener.onTouch(v, event);
		}




		return mGestureDetector.onTouchEvent(event);*/

        //		HiLog.v("onTouch:"+event);

        //		return false;
    }

    public void setScreenSize(float width,float height) {
        this.screen_width = width;
        this.screen_height = height;
    }

    public void saveMatrix(int left, int bottom, int video_width, int video_height) {
        initMatrix();
//        this.left = left;
//        this.bottom = bottom;
//        this.width = video_width;
//        this.height = video_height;
    }


    float resetWidth;
    float resetHeight;

    public void setView() {
        WindowManager.LayoutParams wlp = context.getWindow().getAttributes();
        wlp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;

        context.getWindow().setAttributes(wlp);
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float screen_width = dm.widthPixels;
        float screen_height = dm.heightPixels;

        if (resetWidth == 0) {
            resetWidth = screen_width;
            resetHeight = screen_height;
        }
        resetWidth += 100;
        resetHeight += 100;

		/*SurfaceView sfv=MyLiveViewGLMonitor.this;
        sfh=sfv.getHolder();
		HiLog.e(sfh==null?"sfh is null":"sfh is not null");
		canvas = sfh.lockCanvas();
		HiLog.e(canvas==null?"canvas is null":"canvas is not null");
		if(canvas!=null){


			canvas.scale((float)1.5,(float)1.5,screen_width / 2, screen_height / 2);

			canvas.drawColor(android.R.color.transparent);
			canvas.restore();
			sfh.unlockCanvasAndPost(canvas);


		}*/
        /*	FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int)resetWidth, (int)resetHeight);
		setLayoutParams(lp);
		 */
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        //		HiLog.v("onDown:");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
        //		HiLog.v("onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        //		HiLog.v("onSingleTapUp");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // TODO Auto-generated method stub
        //		HiLog.v("onScroll:");


        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        //		HiLog.v("onLongPress:");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        //		HiLog.v("velocityX: " + Math.abs(velocityX) + ", velocityY: " + Math.abs(velocityY));
        if (mCamera == null)
            return false;

        long curTime = System.currentTimeMillis();
        if (curTime - ptzTime > 500) {
            ptzTime = curTime;
        } else {
            return false;
        }

        this.scrollTo((int) velocityX, (int) velocityY);

        invalidate();
        if (state == 0) {
            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
                mCamera.ptz(AVIOCTRLDEFs.AVIOCTRL_PTZ_RIGHT);
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
                mCamera.ptz(AVIOCTRLDEFs.AVIOCTRL_PTZ_LEFT);
            } else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > Math.abs(velocityX)) {
                mCamera.ptz(AVIOCTRLDEFs.AVIOCTRL_PTZ_DOWN);
            } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > Math.abs(velocityX)) {
                mCamera.ptz(AVIOCTRLDEFs.AVIOCTRL_PTZ_UP);
            }
            new Handler().postDelayed(new Runnable() {

                public void run() {

                    if (mCamera != null && mAVChannel >= 0)
                        mCamera.ptz(AVIOCTRLDEFs.AVIOCTRL_PTZ_STOP);
                }

            }, PTZ_DELAY);
        }

        return false;
    }

    public void setMonitorOnTouchListener(View.OnTouchListener mOnTouchListener) {
        this.mOnTouchListener = mOnTouchListener;
    }



    @Override
    public void callbackYUVData(Camera camera, int channel, byte[] yuvdata, int length, int video_width, int video_height) {
        super.callbackYUVData(camera, channel, yuvdata, length, video_width, video_height);
//        if (setFixedScale&&channel == mAVChannel) {
//            if (tempWidth != video_width || tempHeight != video_height || tempScreenHeight != screen_height || tempScreenWidth != screen_width) {
//                tempWidth = video_width;
//                tempHeight = video_height;
//                tempScreenHeight = screen_height;
//                tempScreenWidth = screen_width;
//                if (Math.abs((float) video_width / video_height - this.screen_width / this.screen_height) > 0.20) {
//                    int l;
//                    int t;
//                    int w;
//                    int h;
//                    if ((float) video_width / video_height > this.screen_width / this.screen_height) {
//                        h = video_height * (int) this.screen_width / video_width;
//                        t = (int) (this.screen_height - h) / 2;
//                        w = (int) screen_width;
//                        l = 0;
//                    } else {
//                        w = video_width * (int) this.screen_height / video_height;
//                        h = (int) screen_height;
//                        l = (int) (this.screen_width - w) / 2;
//                        t = 0;
//                    }
//                    setMatrix(l, t, w, h);
//                    this.left = l;
//                    this.bottom = t;
//                }
//            }
//        }
    }

    public void initMatrix() {
        int video_width = getmCurVideoWidth();
        int video_height = getmCurVideoHeight();
        if (video_height == 0 || video_width == 0) {
            video_width = (int) screen_width;
            video_height = (int) screen_height;
        }
        if (false && setFixedScale && Math.abs((float) video_width / video_height - this.screen_width / this.screen_height) > 0.20) {
            int l;
            int t;
            int w;
            int h;
            if ((float) video_width / video_height > this.screen_width / this.screen_height) {
                h = video_height * (int) this.screen_width / video_width;
                t = (int) (this.screen_height - h) / 2;
                w = (int) screen_width;
                l = 0;
            } else {
                w = video_width * (int) this.screen_height / video_height;
                h = (int) screen_height;
                l = (int) (this.screen_width - w) / 2;
                t = 0;
            }
            setMatrix(l, t, w, h);
            this.left = l;
            this.bottom = t;
            this.width = w;
            this.height = h;
        } else {
            this.left = 0;
            this.bottom = 0;
            this.width = (int) screen_width;
            this.height = (int) screen_height;
        }
    }
    public void attachCamera(MyCamera camera, int avChannel) {
        super.attachCamera((NSCamera) camera,avChannel);
        resizeVideoWrapper(camera);
    }

    public void  resizeVideoWrapper(IMyCamera camera){
        RelativeLayout rl = (RelativeLayout)getParent();
        ViewGroup.LayoutParams layoutParams = rl.getLayoutParams();
        float ratio = (camera).getVideoRatio(this.getContext());
        if((float)phoneScreenWidth/phoneScreenHeight > ratio){
            layoutParams.height = phoneScreenHeight;
            layoutParams.width = (int)(phoneScreenHeight*ratio);
        }
        else {
            layoutParams.height = (int) (phoneScreenWidth / ratio);
            layoutParams.width = phoneScreenWidth;
        }
        rl.setLayoutParams(layoutParams);
    }
}