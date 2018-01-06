package com.tws.commonlib.start;

/**
 * Created by Administrator on 2017/6/27.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.push.RedirectAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * 寮曞椤电晫闈�(viewpager)
 */
public class IntroActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 搴曢儴鏍囪鏈夊嚑椤碉紙鍑犱釜鐐癸級鍙婂湪绗�?嚑椤碉紙璇ョ偣鏍囪涓鸿摑鑹诧級鐨勭偣鐨勫竷灞�
     */
    private LinearLayout layoutBottom;

    List<View> images = new ArrayList();
    private ViewPager vp;
    private Button bt;

    private int currentPage;
    /**
     * 搴曢儴鏍囪鏈夊嚑椤碉紙鍑犱釜鐐癸級鍙婂湪绗�?嚑椤碉紙璇ョ偣鏍囪涓鸿摑鑹诧級鐨勭偣鐨処mageView
     */
    private ImageView imgCur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        //灏嗗浘鐗囨坊鍔犲埌list<view>闆嗗悎涓�?
        for (int i = 0; i < MyConfig.getIntroImg().length; i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(MyConfig.getIntroImg()[i]);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            images.add(iv);

        }
        vp = (ViewPager) findViewById(R.id.vp);
        bt = (Button) findViewById(R.id.gointo_app_bt);
        layoutBottom = (LinearLayout) findViewById(R.id.layout_scr_bottom);
        GuildPagerAdapter adapter = new GuildPagerAdapter(images);
        vp.setAdapter(adapter);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //currentpage鍚庨潰瑕佺敤
                currentPage = position;
                setPage(position);
                if (position == 2) {
                    //璁剧疆鍙,濡傛灉鐢ㄦ寜閽篃鍙互瀹炵幇activity鐨勮烦杞�?,涓ょ璺宠浆鍔熻兘閮藉叿澶�
                    bt.setVisibility(View.VISIBLE);
                    bt.setOnClickListener(IntroActivity.this);
                } else {
                    bt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //璁剧疆ViewPager鐨勬粦鍔ㄧ洃鍚�,涓轰簡婊戝姩鍒版渶鍚庝竴椤�,缁х画婊戝姩�?�炵幇椤甸潰鐨勮烦杞�?
        vp.setOnTouchListener(new View.OnTouchListener() {
            float startX;

            float endX;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();

                        break;
                    case MotionEvent.ACTION_UP:
                        endX = event.getX();
//鑾峰彇灞忓箷鐨勫搴�?
                        int width = TwsTools.getScreenWidth(IntroActivity.this);
                        //鏍规嵁婊戝姩鐨勮窛绂绘潵鍒囨崲鐣岄潰
                        if (currentPage == 2 && startX - endX >= (width / 5)) {

                            startApp();//鍒囨崲鐣岄潰
                        }

                        break;
                }
                return false;
            }
        });
        setPage(0);

    }

    private void startApp() {
        Intent intent = new Intent(this, com.tws.commonlib.MainActivity.class);
        startActivity(intent);
        finish();
    }


    //button 鐐瑰嚮浜嬩欢
    public void onClick(View view) {
        startApp();

    }

    /**
     * @param
     * @author Administrator
     * @date 2016/12/2 0002  涓婂�? 9:26
     * @Description viewpager 鐨勯�傞厤鍣�?,鍐呭涓嶅鐩存帴鍐欏唴閮ㄧ被浜�?
     * @retrun
     */
    public class GuildPagerAdapter extends PagerAdapter {
        List<View> list;

        public GuildPagerAdapter(List<View> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(list.get(position));
            return list.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(list.get(position));

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private void setPage(int position) {
        layoutBottom.removeAllViews();
        //鏄剧ず褰撳墠椤电殑鎸囩ず
        for (int i = 0; i < MyConfig.getIntroImg().length; i++) {
            imgCur = new ImageView(this);
            imgCur.setImageResource(R.drawable.offline);//鐏拌�?
            imgCur.setPadding(8, 5, 8, 5);
            imgCur.setId(i);
            if (imgCur.getId() == position) {
                imgCur.setImageResource(R.drawable.online);//钃濊�?
            }
            layoutBottom.addView(imgCur);//灏嗙偣鐨勫浘鐗嘨iew娣诲姞鑷矻ayout涓樉绀�?
        }
    }

}