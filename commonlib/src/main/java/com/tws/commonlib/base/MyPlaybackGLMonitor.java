package com.tws.commonlib.base;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.hichip.control.HiGLMonitor;

public class MyPlaybackGLMonitor extends HiGLMonitor implements View.OnTouchListener {
		public int left;
		public int width;
		public int height;
		public int bottom;
		public int screen_width;
		public int screen_height;
		private OnTouchListener mOnTouchListener;
		private int state = 0; // normal=0, larger=1,two finger touch=3
		private int touchMoved; // not move=0, move=1, two point=2


		public MyPlaybackGLMonitor(Context context, AttributeSet attrs) {
			super(context, attrs);

			super.setOnTouchListener(this);
			setOnTouchListener(this);
			setFocusable(true);
			setClickable(true);
			setLongClickable(true);

			DisplayMetrics dm = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
			screen_width = dm.widthPixels;
			screen_height = dm.heightPixels;

		}

		public void setOnTouchListener(OnTouchListener mOnTouchListener) {
			this.mOnTouchListener = mOnTouchListener;
		}

		@Override
		public void onPause() {
			super.onPause();
		}

		@Override
		public void onResume() {
			super.onResume();
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public int getTouchMove() {
			return this.touchMoved;
		}

		public void setTouchMove(int touchMoved) {
			this.touchMoved = touchMoved;
		}


		// View当前的位置
		private float rawX = 0;
		private float rawY = 0;
		// View之前的位置
		private float lastX = 0;
		private float lastY = 0;

		int xlenOld;
		int ylenOld;

		private int pyl = 20;
		double nLenStart = 0;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mOnTouchListener != null) {
				mOnTouchListener.onTouch(v, event);// 必须要回调非当前的OnTouch方法,不然会栈溢出崩溃
			}
			int nCnt = event.getPointerCount();
			if (state == 1) {// 放大就是1
				if (nCnt == 2) {
					return false;
				}
				// 处理放大后,移动界面(类似移动云台,只不过云台没有动)
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						// 获取手指落下的坐标并保存
						rawX = (event.getRawX());
						rawY = (event.getRawY());
						lastX = rawX;
						lastY = rawY;
						break;
					case MotionEvent.ACTION_MOVE:
						if (touchMoved == 2) {
							break;
						}
						// 手指拖动时，获得当前位置
						rawX = event.getRawX();
						rawY = event.getRawY();
						// 手指移动的x轴和y轴偏移量分别为当前坐标-上次坐标
						float offsetX = rawX - lastX;
						float offsetY = rawY - lastY;
						// 通过View.layout来设置左上右下坐标位置
						// 获得当前的left等坐标并加上相应偏移量
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
						// 移动过后，更新lastX与lastY
						lastX = rawX;
						lastY = rawY;
						break;
				}
				return false;
			}
			return false;
		}



	}
