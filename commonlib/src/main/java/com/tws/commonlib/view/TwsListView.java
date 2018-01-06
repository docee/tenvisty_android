package com.tws.commonlib.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * Created by Administrator on 2017/7/18.
 */

public class TwsListView extends ListView {

    public TwsListView(Context context) {
        super(context);
    }

    public TwsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TwsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
//        int maxWidth = meathureWidthByChilds() + getPaddingLeft() + getPaddingRight();
//        super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.UNSPECIFIED), heightMeasureSpec);//注意，这个地方一定是MeasureSpec.UNSPECIFIED
//    }
//
//
//    public int meathureWidthByChilds() {
//        int maxWidth = 0;
//        View view = null;
//        for (int i = 0; i < getAdapter().getCount(); i++) {
//            view = getAdapter().getView(i, view, this);
//            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
//            if (view.getMeasuredWidth() > maxWidth) {
//                maxWidth = view.getMeasuredWidth();
//            }
//            view = null;
//        }
//        return maxWidth;
//
//    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = meathureWidthByChilds() + getPaddingLeft() + getPaddingRight(); super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
    }
    public int meathureWidthByChilds() {
        int maxWidth = 0;
        View view = null;
        for (int i = 0; i < getAdapter().getCount(); i++) {
            view = getAdapter().getView(i, view, this);
            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            if (view.getMeasuredWidth() > maxWidth){
                maxWidth = view.getMeasuredWidth();
            }
        }
        return maxWidth;
    }
}
