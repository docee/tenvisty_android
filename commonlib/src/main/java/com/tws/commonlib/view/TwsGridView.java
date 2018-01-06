package com.tws.commonlib.view;

import android.view.View;
import android.widget.GridView;

/**
 * Created by Administrator on 2017/10/15.
 */

public class TwsGridView extends GridView {

        public TwsGridView(android.content.Context context,
                          android.util.AttributeSet attrs) {
            super(context, attrs);
        }

        /**
         * 设置不滚动
         */
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                    View.MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

        }

}
