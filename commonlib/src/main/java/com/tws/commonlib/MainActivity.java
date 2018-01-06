package com.tws.commonlib;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.Camera;
import com.tutk.IOTC.L;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.MyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.fragment.AboutFragment;
import com.tws.commonlib.fragment.CameraFragment;
import com.tws.commonlib.fragment.FolderFragment;
import com.tws.commonlib.view.NoSlidingViewPaper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewpager_container;
    ArrayList<Fragment> fgLists = new ArrayList<>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        initView();
        requestEnd();
        L.isDebug = true;

    }
    @Override
    protected  void  onResume(){
        super.onResume();
    }

    void initView() {

        viewpager_container = (NoSlidingViewPaper) findViewById(R.id.viewpager_container);

        fgLists.add(new CameraFragment());
        fgLists.add(new FolderFragment());
        fgLists.add(new AboutFragment());
        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fgLists.get(position);
            }

            @Override
            public int getCount() {
                return fgLists.size();
            }
        };
        viewpager_container.setAdapter(mAdapter);
        //viewpager_container.setOffscreenPageLimit(2); //预加载剩下两页
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        TwsTools.checkPermissionAll(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.navigation_home) {
                viewpager_container.setCurrentItem(0);
                return true;
            } else if (i == R.id.navigation_folder) {
                viewpager_container.setCurrentItem(1);
                if(fgLists.size()>1 && ((FolderFragment)fgLists.get(1))!=null&& ((FolderFragment)fgLists.get(1)).isInited()) {
                    ((FolderFragment) fgLists.get(1)).initView();
                }
                return true;
            } else if (i == R.id.navigation_about) {
                viewpager_container.setCurrentItem(2);
                return true;
            }
            return false;
        }

    };

    public void requestEnd() {
        //鑾峰彇鏁版嵁瀹屾瘯锛屽彂閫佸箍鎾埌CameraFragment鐣岄潰鍘诲埛鏂癮dapter
        Intent intent = new Intent();
        intent.setAction(TwsDataValue.ACTION_CAMERA_INIT_END);
        sendBroadcast(intent);
    }


    /**
     * 提示连续两次点击两次返回则退出应用
     */
    int press_exit_num = 2;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            press_exit_num--;

            Toast.makeText(this, R.string.tips_press_again_to_exit, Toast.LENGTH_LONG).show();
            if (press_exit_num == 0) {
                finish();
                for(MyCamera camera:TwsDataValue.cameraList()){
                    camera.stop();
                }
                MyCamera.uninit();
                //杀死该应用进程
                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                press_exit_num = 2;
                            }
                        });
                    }
                };

                Timer timer = new Timer(true);
                timer.schedule(task, 3000);
            }


            return true;

        }

        return super.onKeyDown(keyCode, event);
    }
}
