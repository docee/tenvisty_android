package com.tws.commonlib.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hichip.base.HiLog;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiGLMonitor;
import com.hichip.data.HiDeviceInfo;
import com.hichip.sdk.HiChipP2P;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.L;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IMyCamera;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/10/11.
 */

public class HiLiveViewGLMonitor extends HiGLMonitor implements View.OnTouchListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private GestureDetector mGestureDetector;
    private static final int FLING_MIN_DISTANCE = 50;
    public static int PTZ_STEP = 50; // ��̨����
    private HichipCamera mCamera = null;
    Matrix matrix = new Matrix();
    private OnTouchListener mOnTouchListener;
    private Activity context;
    private int state = 0; // normal=0, larger=1,two finger touch=3
    private int touchMoved; // not move=0, move=1, two point=2
    public int left;
    public int width;
    public int height;
    public int bottom;
    public int screen_width;
    public int screen_height;
    int phoneScreenWidth;
    int phoneScreenHeight;
    // public int mOritation; // 1���� 0����
    public static int centerPoint;

    public HiLiveViewGLMonitor(Context context, AttributeSet attrs) {
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
        screen_width = dm.widthPixels;
        screen_height = dm.heightPixels;
        phoneScreenHeight = dm.heightPixels;
        phoneScreenWidth = dm.widthPixels;
        // mOritation = context.getResources().getConfiguration().orientation;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // View��ǰ��λ��
    private float rawX = 0;
    private float rawY = 0;
    // View֮ǰ��λ��
    private float lastX = 0;
    private float lastY = 0;

    int xlenOld;
    int ylenOld;

    private int pyl = 20;
    double nLenStart = 0;

    @SuppressLint("WrongCall")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOnTouchListener != null) {
            mOnTouchListener.onTouch(v, event);// ����Ҫ�ص��ǵ�ǰ��OnTouch����,��Ȼ��ջ�������
        }
        int nCnt = event.getPointerCount();
        if (state == 1) {// �Ŵ����1
            if (nCnt == 2) {
                return false;
            }
            // ����Ŵ��,�ƶ�����(�����ƶ���̨,ֻ������̨û�ж�)
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // ��ȡ��ָ���µ����겢����
                    rawX = (event.getRawX());
                    rawY = (event.getRawY());
                    lastX = rawX;
                    lastY = rawY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchMoved == 2) {
                        break;
                    }
                    HiLog.e("mMonitor.ACTION_MOVE");
                    // ��ָ�϶�ʱ����õ�ǰλ��
                    rawX = event.getRawX();
                    rawY = event.getRawY();
                    // ��ָ�ƶ���x���y��ƫ�����ֱ�Ϊ��ǰ����-�ϴ�����
                    float offsetX = rawX - lastX;
                    float offsetY = rawY - lastY;
                    // ͨ��View.layout������������������λ��
                    // ��õ�ǰ��left�����겢������Ӧƫ����
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
                    setMatrix(left, bottom, width, height);
                    // �ƶ����󣬸���lastX��lastY
                    lastX = rawX;
                    lastY = rawY;
                    break;
            }
            return true;
        } else if (state == 0 && nCnt == 1) {
            return mGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    public void saveMatrix(int left, int bottom, int width, int height) {
        this.left = left;
        this.bottom = bottom;
        this.width = width;
        this.height = height;
    }

    float resetWidth;
    float resetHeight;
    private int centerPointX;
    private int centerPointY;

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
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    private float down_x = 0;
    private float down_y = 0;
    private float move_X = 0;
    private float move_Y = 0;

    private int flag = 0;

    // distanceX Ϊe1-e2��ƫ���� e1Ϊ��ʼ������� e2Ϊ�ƶ��仯�������
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        down_x = move_X;
        down_y = move_Y;
        return true;
    }

    private void handlerFrameMode_3(MotionEvent e2, float distanceX) {
        move_X = e2.getRawX();
        move_Y = e2.getRawY();
        float disX = move_X - down_x;
        float disY = move_Y - down_y;
        float K = disY / disX;
        if (distanceX < 0 && Math.abs(K) < 1) {// �һ�
            this.SetGesture(HiGLMonitor.GESTURE_RIGHT);
            this.SetGesture(HiGLMonitor.GESTURE_RIGHT);
        } else if (distanceX > 0 && Math.abs(K) < 1) {// ��
            this.SetGesture(HiGLMonitor.GESTURE_LEFT);
            this.SetGesture(HiGLMonitor.GESTURE_LEFT);
        }
    }

    private void setGesture(int gesture, int num) {
        for (int i = 0; i < num; i++) {
            this.SetGesture(gesture);
        }
    }

    private void handlerFrameMode_4(float distanceX, float x, float y) {
        float pxMoniter = screen_width;
        float pxTopview = TwsTools.dip2px(getContext(), 45) + getStatusBarHeight();
        boolean area_one = false;
        boolean area_two = false;
        boolean area_there = false;
        boolean area_four = false;
        if (false) { // ����
            // ���� 1
            area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
            // ����2
            area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
            // ����3
            area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
            // ����4
            area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
        } else { // ����
            // ���� 1
            area_one = x < screen_height / 2 && y < screen_width / 2;
            // ����2
            area_two = x > screen_height / 2 && y < screen_width / 2;
            // ����3
            area_there = x < screen_height / 2 && y > screen_width / 2;
            // ����4
            area_four = x > screen_height / 2 && y > screen_width / 2;
        }
        int num = 1;
        if (area_one) {
            for (int i = 0; i <= num; i++) {
                if (distanceX > 0) {
                    this.SetGesture(HiGLMonitor.GESTURE_LEFT, 2);
                } else {
                    this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 2);
                }
            }
        } else if (area_two) {
            for (int i = 0; i <= num; i++) {
                if (distanceX > 0) {
                    this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
                } else {
                    this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
                }
            }
        } else if (area_there) {
            for (int i = 0; i <= num; i++) {
                if (distanceX > 0) {
                    this.SetGesture(HiGLMonitor.GESTURE_LEFT, 3);
                } else {
                    this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 3);
                }
            }
        } else if (area_four) {
            for (int i = 0; i <= num; i++) {
                if (distanceX > 0) {
                    this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
                } else {
                    this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
                }
            }
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    // velocityX����ÿ��x�᷽���ƶ������� ����; velocityY����ÿ��y�᷽���ƶ������� ����;
    // e1 ����ƶ�ʱ��
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mCamera == null)
            return false;
        PTZ_STEP = mCamera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_GOKE ? 25 : 50;
        if (state == 0) {
            if (true) {
                if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_LEFT, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
                } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > Math.abs(velocityY)) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_RIGHT, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
                } else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > Math.abs(velocityX)) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_UP, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
                } else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE && Math.abs(velocityY) > Math.abs(velocityX)) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_DOWN, HiChipDefines.HI_P2P_PTZ_MODE_STEP, (short) PTZ_STEP, (short) PTZ_STEP));
                }
            }

        }
        return true;
    }

    private void startRotateMode4(final int gesture, final int num, final int no) {
        new Thread() {
            public void run() {
                for (int i = 0; i <= num; i++) {
                    try {
                        HiLiveViewGLMonitor.this.SetGesture(gesture, no);
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }

    public void setCamera(HichipCamera mCamera) {
        this.mCamera = mCamera;
    }

    public void setOnTouchListener(OnTouchListener mOnTouchListener) {
        this.mOnTouchListener = mOnTouchListener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    public int mSetPosition = 0;

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    private void handlerFrameMode1(float x, float y, float pxTopview, float centerPoint, boolean area_one, boolean area_two, boolean area_there, boolean area_four) {
        if (area_one) {
            float distanceX = screen_width / 2 - x;
            float distanceY = screen_width / 2 + pxTopview - y;
            double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
            if (distance < screen_width / 2 && distance > centerPoint) {// ���ݹ��ɶ���������ľ������С�ڰ뾶��������1����
                float absX = Math.abs(x - centerPointX);
                float absY = Math.abs(y - centerPointY);
                if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// ����������6ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 6);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 6;
                    }
                } else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// ����������6ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 7);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 7;
                    }
                }
            } else if (distance < centerPoint) {
                if (!mIsZoom) {
                    this.SetPosition(true, 8);
                    mIsZoom = !mIsZoom;
                    mSetPosition = 8;
                }
            }
        } else if (area_two) {
            float distanceX = x - screen_width / 2;
            float distanceY = screen_width / 2 + pxTopview - y;
            double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
            if (distance < screen_width / 2 && distance > centerPoint) {
                float absX = Math.abs(x - centerPointX);
                float absY = Math.abs(y - centerPointY);
                if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// ����������1ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 1);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 1;
                    }
                } else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// ����������0ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 0);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 0;
                    }
                }
            } else if (distance < centerPoint) {
                if (!mIsZoom) {
                    this.SetPosition(true, 8);
                    mIsZoom = !mIsZoom;
                    mSetPosition = 8;
                }
            }
        } else if (area_there) {
            float distanceX = screen_width / 2 - x;
            float distanceY = y - (screen_width / 2 + pxTopview);
            double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
            if (distance < screen_width / 2 && distance > centerPoint) {
                float absX = Math.abs(x - centerPointX);
                float absY = Math.abs(y - centerPointY);
                if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// ����������5ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 5);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 5;
                    }
                } else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// ����������4ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 4);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 4;
                    }
                }
            } else if (distance < centerPoint) {
                if (!mIsZoom) {
                    this.SetPosition(true, 8);
                    mIsZoom = !mIsZoom;
                    mSetPosition = 8;
                }
            }
        } else if (area_four) {
            float distanceX = x - screen_width / 2;
            float distanceY = y - screen_width / 2 - pxTopview;
            double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
            if (distance < screen_width / 2 && distance > centerPoint) {
                float absX = Math.abs(x - centerPointX);
                float absY = Math.abs(y - centerPointY);
                if (absX > absY && distance < screen_width / 2 && distance > centerPoint) {// ����������2ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 2);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 2;
                    }
                } else if (absX < absY && distance < screen_width / 2 && distance > centerPoint) {// ����������3ģ��
                    if (!mIsZoom) {
                        this.SetPosition(true, 3);
                        mIsZoom = !mIsZoom;
                        mSetPosition = 3;
                    }
                }
            } else if (distance < centerPoint) {
                if (!mIsZoom) {
                    this.SetPosition(true, 8);
                    mIsZoom = !mIsZoom;
                    mSetPosition = 8;
                }
            }
        }
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    private void startRotate(final int gesture, final int num) {
        new Thread() {
            public void run() {
                for (int i = 0; i <= num; i++) {
                    try {
                        HiLiveViewGLMonitor.this.SetGesture(gesture);
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }

    public boolean mIsZoom = false;

    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
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
