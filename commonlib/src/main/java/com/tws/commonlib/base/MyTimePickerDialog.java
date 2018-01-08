package com.tws.commonlib.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;


import com.tws.commonlib.R;

import java.util.ArrayList;
import java.util.List;

public class MyTimePickerDialog {

	/**
     * 调用该方法弹出时间选择器dialog
     */
    public static AlertDialog showTimePicker(final Context context,final OnTimeSetListener callBack,int hour,int minutes){
    	String title = "Set Time";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = (LinearLayout) ((Activity)context).getLayoutInflater().inflate(R.layout.timepicker_main, null);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
        //Calendar calendar = Calendar.getInstance();
       // calendar.setTimeInMillis(System.currentTimeMillis());
        timePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minutes);
        //如果需要设置分钟的时间间隔则调用该方法
        setNumberPickerTextSize(timePicker,new String[]{"00","30","00","30","00","30"});
        builder.setView(view);
        builder.setTitle(title);
        builder.setPositiveButton(context.getText(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int mHour = timePicker.getCurrentHour();
                //如果之前调用了setNumberPickerTextSize方法，则mMinute不是你选择的实际的分钟数，
                //而是实际的分钟数所在数组的索引，mMinute*时间间隔才是实际的分钟数,
                // 例如你选了30,30在分钟数组的索引是2(即mMinute)，而分钟的间隔是15,则2*15=30
                int mMinute = timePicker.getCurrentMinute();
               
                callBack.onTimeSet(timePicker, mHour, mMinute%2==1?30:0);
                //HiToast.showToast(context, mHour + "hour: " + mMinute + "minute");
                dialog.cancel();
            }
        });
        builder.setNegativeButton(context.getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
        return dlg;
    }

    /**
     *
     * @param viewGroup 传入TimePicker
     * @param minutes 待显示的分钟间隔数组，例如：String[] minutes = new String[]{"00","15","30","45"};
     */
    public static  void setNumberPickerTextSize(ViewGroup viewGroup,String[] minutes){
        List<NumberPicker> npList = findNumberPicker(viewGroup);
        if (null != npList)
        {
            for (NumberPicker mMinuteSpinner : npList)
            {
                if(mMinuteSpinner.toString().contains("id/minute")){//对分钟进行间隔设置
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mMinuteSpinner.setMinValue(0);
                        mMinuteSpinner.setMaxValue(minutes.length - 1);
                        mMinuteSpinner.setDisplayedValues(minutes);  //分钟显示数组
                    }
                }
            }
        }
    }
    
    private static List<NumberPicker> findNumberPicker(ViewGroup viewGroup)
    {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;

        if (null != viewGroup)
        {
            for (int i = 0; i < viewGroup.getChildCount(); i++)
            {
                child = viewGroup.getChildAt(i);
                if (child instanceof NumberPicker)
                {
                    npList.add((NumberPicker)child);
                }
                else if (child instanceof LinearLayout)
                {
                    List<NumberPicker> result = findNumberPicker((ViewGroup)child);
                    if (result.size() > 0)
                    {
                        return result;
                    }
                }
            }
        }

        return npList;
    }

}
