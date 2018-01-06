package com.tws.commonlib.base;

import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.Date;

/**
 * Created by Administrator on 2017/10/15.
 */

public class DateScrollItem {

    public DateScrollItem(Date time,String date,String title,BaseAdapter adapter){
        this.date=date;
        this.title = title;
        this.subAdatper =adapter;
        this.time = time;
    }

    public Date time;
    public String date;
    public String title;
    public  boolean checked;
    public BaseAdapter subAdatper;
    public ImageView img_group_check;
}
