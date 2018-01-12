package com.tws.commonlib.base;

import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.net.PortUnreachableException;
import java.util.Date;

/**
 * Created by Administrator on 2017/10/15.
 */

public class DateScrollItem {

    public static final int SNAPSHOT = 0;
    public static final int RECORD_MANUAL = 1;
    public static final int RECORD_REMOTTE = 2;

    public DateScrollItem(Date time, String date, String title, BaseAdapter adapter,int type) {
        this.date = date;
        this.title = title;
        this.subAdatper = adapter;
        this.time = time;
        this.type = type;
    }

    public Date time;
    public String date;
    public String title;
    public boolean checked;
    public BaseAdapter subAdatper;
    public ImageView img_group_check;
    public boolean isRemoteRecord;
    //0 picture, 1 manual record, 2 remote record
    public int type;
}
