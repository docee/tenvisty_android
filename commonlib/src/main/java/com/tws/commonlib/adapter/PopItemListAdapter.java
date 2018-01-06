package com.tws.commonlib.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tws.commonlib.R;


/**
 * Created by Administrator on 2017/7/14.
 */


public class PopItemListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    public PopItemListAdapter(Context context) {

        this.mInflater = LayoutInflater.from(context);
    }

    public PopItemListAdapter(Context context, String[] _sourceList) {
        this.mInflater = LayoutInflater.from(context);
        this.sourceList = _sourceList;
    }

    public PopItemListAdapter(Context context, String[] _sourceList, int _settedPos) {
        this.mInflater = LayoutInflater.from(context);
        this.sourceList = _sourceList;
        this.settedPos = _settedPos;
    }

    public void setSource(String[] _sourceList) {
        this.sourceList = _sourceList;
    }

    public void setPos(int pos) {
        this.settedPos = pos;
    }

    public int getPos() {
        return this.settedPos;
    }

    private String[] sourceList;
    private int settedPos;

    public void setLayout(int layout) {
        this.layout = layout;
    }

    private int getLayout() {
        return this.layout;
    }

    int layout;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return sourceList.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return sourceList[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (sourceList == null || sourceList.length < position - 1) {
            return null;
        }
        final String title = sourceList[position];
        if (title == null)
            return null;

        ViewHolder holder = null;

        if (convertView == null) {

            convertView = mInflater.inflate(layout == 0 ? R.layout.pop_list_item_result : layout, null);
            holder = new ViewHolder();
            holder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
           // holder.view_split = (View)convertView.findViewById(R.id.view_split);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder != null) {
            holder.txt_title.setText(sourceList[position]);
            if(position == sourceList.length -1){
               // holder.view_split.setVisibility(View.GONE);
            }
            else{
              //  holder.view_split.setVisibility(View.VISIBLE);
            }
        }
        return convertView;

    }

    public final class ViewHolder {
        public TextView txt_title;
        public  View view_split;
    }

}