package com.tws.commonlib.controller;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tws.commonlib.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Created by Administrator on 2017/6/27.
 */

public class NavigationBar extends RelativeLayout implements View.OnClickListener {
    private Context context;
    public static final int NAVIGATION_BUTTON_LEFT = 0;
    public static final int NAVIGATION_BUTTON_RIGHT = 1;
    public static final int NAVIGATION_BUTTON_RIGHT2 = 2;
    private NavigationBarButtonListener btnListener;
    private NavigationBarButtonListener btnListenerLeft;

    public NavigationBar(Context context) {
        super(context);
        initView(context);
    }

    public NavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public NavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context=context;
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bar_navigation, this,true);
    }

    public void setTitle(String txt){
        TextView tv=(TextView)findViewById(R.id.title_middle);
        tv.setVisibility(View.VISIBLE);
        tv.setText(txt);
    }
    public void setTitle(String txt,int size){
        TextView tv=(TextView)findViewById(R.id.title_middle);
        tv.setVisibility(View.VISIBLE);
        tv.setText(txt);
        tv.setTextSize(size);
    }

    public void setRightBtnText(String str){
        TextView tv=(TextView)findViewById(R.id.btn_finish);
        tv.getLayoutParams().width = WRAP_CONTENT;
        tv.setText(str);
    }
    public void setRightBtnTextSide(int sp){
        TextView tv=(TextView)findViewById(R.id.btn_finish);
        tv.setTextSize(sp);
    }

    public void setRightBtn2Background(int res){
        Button tv = (Button)findViewById(R.id.btn_finish2);
        tv.setBackgroundResource(res);
        ViewGroup.LayoutParams lytp = tv.getLayoutParams();
        Resources resources = getResources();
        float fPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, resources.getDisplayMetrics());
        lytp.height = Math.round(fPx);
        lytp.width = Math.round(fPx);
        //((LinearLayout.LayoutParams)lytp).setMargins(5, 0, 5, 0);
        //tv.refreshDrawableState();
        //FrameLayout.LayoutParams lytp = new FrameLayout.LayoutParams(20,20);
        //lytp.gravity=Gravity.CENTER_VERTICAL;
        //tv.setLayoutParams(lytp);
    }

    public void setRightBtnBackground(int res){
        Button tv = (Button)findViewById(R.id.btn_finish);
        tv.setBackgroundResource(res);
        ViewGroup.LayoutParams lytp = tv.getLayoutParams();
        Resources resources = getResources();
        float fPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,32, resources.getDisplayMetrics());
        lytp.height = Math.round(fPx);
        lytp.width = Math.round(fPx);
        //((LinearLayout.LayoutParams)lytp).setMargins(5, 0, 5, 0);
        //tv.refreshDrawableState();
        //FrameLayout.LayoutParams lytp = new FrameLayout.LayoutParams(20,20);
        //lytp.gravity=Gravity.CENTER_VERTICAL;
        //tv.setLayoutParams(lytp);
    }

    public void setRightBtn2Text(String str){
        TextView tv = (TextView)findViewById(R.id.btn_finish2);
        tv.setText(str);
    }

    public View GetView(int which){
        switch (which) {
            case NAVIGATION_BUTTON_LEFT:
            {
                Button btn=(Button)findViewById(R.id.btn_return);
                return btn;
            }
            case NAVIGATION_BUTTON_RIGHT: {
                Button btn = (Button) findViewById(R.id.btn_finish);
                return btn;
            }
            case NAVIGATION_BUTTON_RIGHT2: {
                Button btn2 = (Button) findViewById(R.id.btn_finish2);
                return btn2;
            }
        }
        return  null;
    }

    public void setButton(int which){
		/*Button oldBtn=(Button)this.findViewWithTag((Integer)which);
		if(oldBtn!=null)
			this.removeView(oldBtn);
		*/

        switch (which) {
            case NAVIGATION_BUTTON_LEFT:
            {
                Button btn=(Button)findViewById(R.id.btn_return);
                btn.setTag(which);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(this);
            }
            break;
            case NAVIGATION_BUTTON_RIGHT:
                Button btn=(Button)findViewById(R.id.btn_finish);
                btn.setTag(which);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(this);
                break;
            case NAVIGATION_BUTTON_RIGHT2:
                Button btn2=(Button)findViewById(R.id.btn_finish2);
                btn2.setTag(which);
                btn2.setVisibility(View.VISIBLE);
                btn2.setOnClickListener(this);
                break;
        }


    }
    public void setButton(int which,int backgroundResouceId){
		/*Button oldBtn=(Button)this.findViewWithTag((Integer)which);
		if(oldBtn!=null)
			this.removeView(oldBtn);
		*/

        switch (which) {
            case NAVIGATION_BUTTON_LEFT:
            {
                Button btn=(Button)findViewById(R.id.btn_return);
                btn.setTag(which);
                btn.setVisibility(View.VISIBLE);
                btn.setBackgroundResource(backgroundResouceId);
                btn.setOnClickListener(this);
            }
            break;
            case NAVIGATION_BUTTON_RIGHT:
                Button btn=(Button)findViewById(R.id.btn_finish);
                btn.setTag(which);
                btn.setVisibility(View.VISIBLE);
                btn.setBackgroundResource(backgroundResouceId);
                btn.setOnClickListener(this);
                break;
            case NAVIGATION_BUTTON_RIGHT2:
                Button btn2=(Button)findViewById(R.id.btn_finish2);
                btn2.setTag(which);
                btn2.setVisibility(View.VISIBLE);
                btn2.setBackgroundResource(backgroundResouceId);
                btn2.setOnClickListener(this);
                break;
        }


    }
    @Override
    public void onClick(View v) {
        int which=((Integer) v.getTag()).intValue();
        if(btnListener!=null){
            btnListener.OnNavigationButtonClick(which);
        }
        if(btnListenerLeft != null){
            btnListenerLeft.OnNavigationButtonClick(which);
        }
    }

    public interface NavigationBarButtonListener{

        void OnNavigationButtonClick(int which);
    }

    public void setNavigationBarButtonListener(NavigationBarButtonListener listener) {
        btnListener = listener;
    }

    public void  setNavigationButtonLeftListner(NavigationBarButtonListener listener){
        btnListenerLeft = listener;
    }
}
