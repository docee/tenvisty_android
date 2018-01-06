package com.tws.commonlib.start;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tws.commonlib.base.MyConfig;

import java.io.IOException;
import java.io.InputStream;

/**
 * 自定义的?个SurfaceView类，用于显示欢迎界面的背景及版本?
 * @author Administrator
 *
 */
public class Splash extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mSurfaceHolder = null;
	private int mScreenWidth = 1;
	private int mScreenHeight = 1;
	private Context mContext;
	private String mVersion;


	public Splash(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		mScreenWidth = width;
		mScreenHeight = height;



		//定义支文字画笔，设置画笔属?，用于画版本号
		TextPaint paint = new TextPaint();
		paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		paint.setTextSize(20);
		paint.setAntiAlias(true);//抗锯?
		paint.setARGB(200, 255, 255, 255);

		Bitmap bmp = null;

		bmp = getTextureFromBitmapResource(mContext, MyConfig.getSplashImgSrc());//根据上下文对象及资源ID获取?张背景图（bitmap?

		/*
		 * if (width == 480 && height == 800) bmp =
		 * getTextureFromBitmapResource(mContext, R.drawable.splash_480x800);
		 * else if (width == 320 && height == 480) bmp =
		 * getTextureFromBitmapResource(mContext, R.drawable.splash_320x480);
		 * else bmp = getTextureFromBitmapResource(mContext,
		 * R.drawable.splash_480x800);
		 */

		Rect rect = new Rect(0, 0, mScreenWidth, mScreenHeight);//根据屏幕大小绘制?个长方形

		Canvas canvas = mSurfaceHolder.lockCanvas(null);

		if (canvas != null) {

			canvas.drawBitmap(bmp, null, rect, new Paint());//绘制背景?
			canvas.drawText(mVersion, 20, mScreenHeight - 30, paint);//绘制版本号（中间两个为x\y的绝对坐标）

			mSurfaceHolder.unlockCanvasAndPost(canvas);
			canvas = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	/**
	 * 通过上下文对象及资源文件ID获取资源文件（图片，并将获得的图片转为Bitmap?
	 * @param context  上下文对?
	 * @param resourceId  资源文件ID，此处为?张图?
	 * @return
	 */
	public static Bitmap getTextureFromBitmapResource(Context context, int resourceId) {

		InputStream is = context.getResources().openRawResource(resourceId);
		Bitmap bitmap = null;

		try {

			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			// Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {

			}
		}

		return bitmap;
	}

	/**
	 * 设置?要绘制的版本?
	 * @param ver
	 */
	public void setVersion(String ver) {

		mVersion = ver;
	}
}
