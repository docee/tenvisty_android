package com.tws.commonlib.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.List;

/**
 * PagerAdapter用来显示每个pager显示一个GridView
 * @author Administrator
 *
 */
public class GridViewPagerAdapter extends PagerAdapter {

    private List<GridView> gridViews;

    public GridViewPagerAdapter(List<GridView> gridViews) {
        this.gridViews = gridViews;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
    	GridView gridView = gridViews.get(position);
        container.addView(gridView);
        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    		if (position < gridViews.size()) {
    			container.removeView(gridViews.get(position));
		}
    	
    }

    @Override
    public int getCount() {
        return gridViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}
