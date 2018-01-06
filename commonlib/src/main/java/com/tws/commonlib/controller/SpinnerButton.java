package com.tws.commonlib.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tws.commonlib.R;


/**
 * Created by Administrator on 2017/6/27.
 */

public class SpinnerButton extends LinearLayout implements View.OnClickListener {
    private Context context;
    private SpinnerButtonListener btnListener;
    private Button[] btns;

    public SpinnerButton(Context context) {
        super(context);
        initView(context);
    }

    public SpinnerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public SpinnerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spinner_btn_view, this, true);
    }

    public void setTitles(String[] titles) {
        // this.removeAllViews();
        btns = new Button[Math.min(titles.length, ((LinearLayout) this.getChildAt(0)).getChildCount())];

        for (int i = 0; i < ((LinearLayout) this.getChildAt(0)).getChildCount(); i++) {
            Button btn = (Button) ((LinearLayout) this.getChildAt(0)).getChildAt(i);
            if (i >= titles.length) {
                btn.setVisibility(View.GONE);
            } else {
                btn.setText(titles[i]);
                if (i == 0) {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_left_hor);
                } else if (i == titles.length - 1) {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_right_hor);
                } else {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_center_hor);
                }
                btn.setTag(i);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(this);
                btns[i] = btn;
            }
        }

//        for (int i = 0; i < titles.length; i++) {
//            Button btn = new Button(this.context);
//            btn.setText(titles[i]);
//            btn.setWidth(200);
//            btn.setHeight(100);
//
//            if (i == 0) {
//                btn.setBackgroundResource(R.drawable.spinner_btn_bg_left_hor);
//            } else if (i == titles.length - 1) {
//                btn.setBackgroundResource(R.drawable.spinner_btn_bg_right_hor);
//            } else {
//                btn.setBackgroundResource(R.drawable.spinner_btn_bg_center_hor);
//            }
//            btn.setTag(i);
//            btn.setVisibility(View.VISIBLE);
//            btn.setOnClickListener(this);
//            this.addView(btn);
//            btns[i] = btn;
//        }
    }

    public void Click(int which) {
        if (which < btns.length) {
            btns[which].performClick();
        }
    }

    @Override
    public void onClick(View v) {
        int which = ((Integer) v.getTag()).intValue();
        for (int i = 0; i < btns.length; i++) {
            Button btn = btns[i];
            if (i == which) {
                btn.setTextColor(ColorStateList.valueOf(Color.WHITE));
                if (i == 0) {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_left_hor_press);
                } else if (i == btns.length - 1) {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_right_hor_press);
                } else {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_center_hor_press);
                }
            } else {
                btn.setTextColor(ColorStateList.valueOf(Color.BLACK));
                if (i == 0) {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_left_hor);
                } else if (i == btns.length - 1) {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_right_hor);
                } else {
                    btn.setBackgroundResource(R.drawable.spinner_btn_bg_center_hor);
                }
            }
        }
        if (btnListener != null) {
            btnListener.OnSpinnerButtonClick(which);
        }

    }

    public interface SpinnerButtonListener {

        void OnSpinnerButtonClick(int which);
    }

    public void setSpinnerButtonListener(SpinnerButtonListener listener) {
        btnListener = listener;
    }

}
