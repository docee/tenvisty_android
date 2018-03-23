package com.tws.commonlib.bean;

import android.content.Context;

import com.tws.commonlib.R;

import java.util.Date;

/**
 * Created by Administrator on 2018/3/22.
 */

public class BatteryStatus {
    public BatteryStatus(int mode, int percent, long time) {
        this.batPercent = percent;
        this.workMode = mode;
        this.time = time;
    }

    public BatteryStatus() {
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    private long time;

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    // 0:bat; 1:usb无电池；2:usb有电池，充电中；3:usb有电池，充电满
    private int workMode = -1;

    public int getBatPercent() {
        return batPercent;
    }

    public void setBatPercent(int batPercent) {
        this.batPercent = batPercent;
    }

    private int batPercent = -1;


    public int getBatteryDrawable() {
        if (batPercent < 0 && workMode < 0) {
            return 0;
        }
        int drawableSource = 0;
        //1个小时以前
        if (workMode != -1 && Math.abs(this.time - new Date().getTime()) > 1000 * 60 * 10) {
            drawableSource = R.drawable.ic_battery_unknown_black_24dp;
        } else if (workMode == 0) {
            if (batPercent < 10) {
                drawableSource = R.drawable.ic_battery_alert_black_24dp;
            } else if (batPercent < 30) {
                drawableSource = R.drawable.ic_battery_20_black_24dp;
            } else if (batPercent < 40) {
                drawableSource = R.drawable.ic_battery_30_black_24dp;
            } else if (batPercent < 60) {
                drawableSource = R.drawable.ic_battery_50_black_24dp;
            } else if (batPercent < 70) {
                drawableSource = R.drawable.ic_battery_60_black_24dp;
            } else if (batPercent < 90) {
                drawableSource = R.drawable.ic_battery_80_black_24dp;
            } else if (batPercent < 95) {
                drawableSource = R.drawable.ic_battery_90_black_24dp;
            } else {
                drawableSource = R.drawable.ic_battery_full_black_24dp;
            }
        } else if (workMode == 1) {
            drawableSource = R.drawable.ic_battery_no_charging_black_24dp;
        } else if (workMode == 2) {
            if (batPercent < 30) {
                drawableSource = R.drawable.ic_battery_charging_20_black_24dp;
            } else if (batPercent < 40) {
                drawableSource = R.drawable.ic_battery_charging_30_black_24dp;
            } else if (batPercent < 60) {
                drawableSource = R.drawable.ic_battery_charging_50_black_24dp;
            } else if (batPercent < 70) {
                drawableSource = R.drawable.ic_battery_charging_60_black_24dp;
            } else if (batPercent < 90) {
                drawableSource = R.drawable.ic_battery_charging_80_black_24dp;
            } else if (batPercent < 95) {
                drawableSource = R.drawable.ic_battery_charging_90_black_24dp;
            } else {
                drawableSource = R.drawable.ic_battery_charging_full_black_24dp;
            }
        } else if (workMode == 3) {
            drawableSource = R.drawable.ic_battery_charging_full_black_24dp;
        }
        return drawableSource;
    }
}
