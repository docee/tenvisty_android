package com.tws.commonlib.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.adapter.PopItemListAdapter;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsProgressDialog;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.view.TwsListView;

import java.io.BufferedOutputStream;

public class BaseActivity extends AppCompatActivity {
    protected TwsProgressDialog progressDialog;
    private final static int GO_ACTIVITY = 999;
    protected boolean needConnect = true;
    protected IMyCamera camera;
    // public SystemBarTintManager tintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
//        if (setStatusBarColor) {
//            MainActivity.initSystemBar(this);
//        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            for (IMyCamera c : TwsDataValue.cameraList()) {
                if (c.getUid().equals(getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID))) {
                    camera = c;
                    break;
                }
            }
        }

        if (needConnect && camera != null && camera.isNotConnect()) {
            back2Activity(MainActivity.class);
        }

    }

    protected void initView() {

        View barView = findViewById(R.id.title_top);
        if (barView != null) {
            NavigationBar title = (NavigationBar) barView;
            title.setTitle(this.getTitle().toString());
            title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
            title.setNavigationButtonLeftListner(new NavigationBar.NavigationBarButtonListener() {

                @Override
                public void OnNavigationButtonClick(int which) {
                    switch (which) {
                        case NavigationBar.NAVIGATION_BUTTON_LEFT:
                            finish();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        super.finish();
        overridePendingTransition(R.anim.out_to_right, R.anim.in_from_left);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        // TODO Auto-generated method stub
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.in_from_right,
                R.anim.out_to_left);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                overridePendingTransition(R.anim.in_from_right,
                        R.anim.out_to_left);
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void startActivity(Intent intent) {
        // TODO Auto-generated method stub
        super.startActivity(intent);
        overridePendingTransition(R.anim.in_from_right,
                R.anim.out_to_left);
    }

    public void showYesNoDialog(int msg, int title, DialogInterface.OnClickListener listener) {

//		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
//				case DialogInterface.BUTTON_POSITIVE:
//					// Yes button clicked
//					break;
//
//				case DialogInterface.BUTTON_NEGATIVE:
//					// No button clicked
//					break;
//				}
//			}
//		};
        AlertDialog.Builder builder = new AlertDialog.Builder(
                BaseActivity.this);

        builder.setMessage(
                getResources().getString(
                        msg))
                .setTitle(title)
                .setPositiveButton(
                        getResources().getString(R.string.ok),
                        listener)
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        listener).show();

    }

    public void showYesNoDialog(int msg, DialogInterface.OnClickListener listener) {

//		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				switch (which) {
//				case DialogInterface.BUTTON_POSITIVE:
//					// Yes button clicked
//					break;
//
//				case DialogInterface.BUTTON_NEGATIVE:
//					// No button clicked
//					break;
//				}
//			}
//		};
        AlertDialog.Builder builder = new AlertDialog.Builder(
                BaseActivity.this);

        builder.setMessage(
                getResources().getString(
                        msg))
                .setTitle(R.string.warning)
                .setPositiveButton(
                        getResources().getString(R.string.ok),
                        listener)
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        listener).show();

    }


    public AlertDialog showAlert(CharSequence message) {
        if (!this.isFinishing()) {
            AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
            dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dlgBuilder.setTitle(R.string.warning);
            dlgBuilder.setMessage(message);

            AlertDialog dialog = dlgBuilder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).show();
            return dialog;
        }
        return null;
    }

    public void showAlert(CharSequence message, DialogInterface.OnClickListener listener) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dlgBuilder.setTitle(R.string.warning);
        dlgBuilder.setMessage(message);
        dlgBuilder.setPositiveButton(getText(R.string.ok), listener).show();
    }

    public void showAlert(CharSequence message,String title, boolean cancelable, DialogInterface.OnClickListener listener) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dlgBuilder.setTitle(title);
        dlgBuilder.setCancelable(cancelable);
        dlgBuilder.setMessage(message);
        dlgBuilder.setPositiveButton(getText(R.string.ok), listener).show();
    }

    public void showAlertnew(int iconId, CharSequence title, CharSequence message, CharSequence cancel,
                             CharSequence okch, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder mDlgBuilder = new AlertDialog.Builder(this);
        mDlgBuilder.setIcon(iconId);
        mDlgBuilder.setTitle(title);
        mDlgBuilder.setMessage(message);
        mDlgBuilder.setPositiveButton(okch, listener).setNegativeButton(cancel, listener).show();
    }

    public AlertDialog showAlert(CharSequence message, int startPos, int length, int color) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dlgBuilder.setTitle(R.string.warning);
        dlgBuilder.setMessage(message);

        SpannableStringBuilder spanBuilder = new SpannableStringBuilder(message);
        spanBuilder.setSpan(new ForegroundColorSpan(color), startPos, startPos + length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        dlgBuilder.setMessage(spanBuilder);
        AlertDialog dialog = dlgBuilder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
        return dialog;
    }

    public interface MyDismiss {
        void OnDismiss();
    }

    MyDismiss myDismiss;

    public void setOnLoadingProgressDismissListener(MyDismiss dismiss) {
        this.myDismiss = dismiss;
    }

    public void showLoadingProgress() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = TwsProgressDialog.createProgressDialog(BaseActivity.this, 60000, new TwsProgressDialog.OnTimeOutListener() {
                @Override
                public void onTimeOut(TwsProgressDialog dialog) {
                    TwsToast.showToast(BaseActivity.this, getString(R.string.process_connect_timeout));
                }
            });
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setIcon(MyConfig.getAppIconSource());
            progressDialog.setMessage(getText(R.string.process_loading));

            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    progressDialog.setTimeOut(0, null);
                    if (myDismiss != null) {
                        myDismiss.OnDismiss();
                    }

                }
            });

            progressDialog.show();
        }

    }

    public boolean isLoadingShow() {
        return progressDialog != null && progressDialog.isShowing();
    }

    public void showLoadingProgress(String msg) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = TwsProgressDialog.createProgressDialog(BaseActivity.this, 60000, new TwsProgressDialog.OnTimeOutListener() {
                @Override
                public void onTimeOut(TwsProgressDialog dialog) {
                    TwsToast.showToast(BaseActivity.this, getString(R.string.process_connect_timeout));
                }
            });
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setIcon(MyConfig.getAppIconSource());
            progressDialog.setMessage(msg);

            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    progressDialog.setTimeOut(0, null);
                    if (myDismiss != null) {
                        myDismiss.OnDismiss();
                    }

                }
            });
            progressDialog.show();
        }
    }

    public void refreshProgressTest(String msg) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(msg);
        }
    }


    public void showLoadingProgress(String msg, boolean cancelable, int wait, TwsProgressDialog.OnTimeOutListener listener) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = TwsProgressDialog.createProgressDialog(BaseActivity.this, wait, listener);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(cancelable);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setIcon(MyConfig.getAppIconSource());
            progressDialog.setMessage(msg);

            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    progressDialog.setTimeOut(0, null);
                    if (myDismiss != null) {
                        myDismiss.OnDismiss();
                    }

                }
            });
            progressDialog.show();
        }
    }

    public void dismissLoadingProgress() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }

    protected void back2Activity(Class<?> toClass) {
        Intent intent = new Intent(this, toClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    protected void back2Activity(Class<?> toClass,Bundle bundle) {
        Intent intent = new Intent(this, toClass);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    protected void refreshList() {
        Intent intent = new Intent();
        intent.setAction(TwsDataValue.ACTION_CAMERA_INIT_END);
        sendBroadcast(intent);
    }

    protected void doFullScreenStatusBar(boolean b) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//
//            MainActivity.setTranslucentStatus(this, b);
//        }
//        tintManager.setStatusBarTintEnabled(b);
    }

    public PopupWindow showPopupWindow(View view, String[] sourceList, AdapterView.OnItemClickListener listener) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.pop_window, null);
        // 设置按钮的点击事件
        TwsListView listview_itemlist = (TwsListView) contentView.findViewById(R.id.listview_itemlist);
        PopItemListAdapter adapter = new PopItemListAdapter(this, sourceList);
        listview_itemlist.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listview_itemlist.setOnItemClickListener(listener);
        PopupWindow popupWindow = new PopupWindow(contentView,
                view.getWidth() - 40, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //popupWindow.setAnimationStyle(R.style.AnimationPreview);//设置动画样式
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        //  popupWindow.setBackgroundDrawable(getResources().getDrawable(
        //  R.drawable.btn_setting_bg));
        // 设置好参数之后再show
        popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        int popHeight = popupWindow.getContentView().getMeasuredHeight();
        int popWidth = popupWindow.getContentView().getMeasuredWidth();
        int[] location = new int[2];
        view.getLocationInWindow(location);
        popupWindow.showAtLocation(view, Gravity.TOP | Gravity.LEFT, location[0] + 20, location[1] - popHeight * 2);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                popupWindow.showAtLocation(view, Gravity.TOP|Gravity.LEFT, location[0], location[1] - popHeight*2 - view.getHeight()-20);
//            }
//        }, 200);
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                TwsTools.calculatePopWindowPos(view, contentView);
//            }
//        });
        return popupWindow;
        //popupWindow.showAsDropDown(view, 0, -sourceList.length * 140 - 40);
        //popupWindow.showAsDropDown(view);

    }

    public PopupWindow showPopupWindow(View view, String[] sourceList, AdapterView.OnItemClickListener listener, boolean full) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.pop_window_fullwidth, null);
        // 设置按钮的点击事件
        ListView listview_itemlist = (ListView) contentView.findViewById(R.id.listview_itemlist);
        PopItemListAdapter adapter = new PopItemListAdapter(this, sourceList);
        adapter.setLayout(R.layout.pop_list_item_result_fullwidth);
        listview_itemlist.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listview_itemlist.setOnItemClickListener(listener);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        //  popupWindow.setBackgroundDrawable(getResources().getDrawable(
        //  R.drawable.btn_setting_bg));
        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);

        return popupWindow;
        //popupWindow.showAsDropDown(view, 0, -sourceList.length * 140 - 40);
        //popupWindow.showAsDropDown(view);

    }

    protected void onResume() {
        super.onResume();
        //getKeepAliveHandler().postDelayed(keepAliveRunnable,1000);
    }

    protected void onPause() {
        super.onPause();
        //getKeepAliveHandler().removeCallbacks(keepAliveRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
        //getKeepAliveHandler().removeCallbacks(keepAliveRunnable);
    }

    protected void showLoadingView(int viewId) {
        View view = findViewById(viewId);
        if (view != null) {
            ViewGroup p = (ViewGroup) view.getParent();
            if (p != null) {
                for (int i = 0; i < p.getChildCount(); i++) {
                    if (p.getChildAt(i) instanceof TextView) {
                        if (((TextView) p.getChildAt(i)).getText().toString().equalsIgnoreCase(getString(R.string.loading))) {
                            p.getChildAt(i).setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void hideLoadingView(int viewId) {
        View view = findViewById(viewId);
        if (view != null) {
            ViewGroup p = (ViewGroup) view.getParent();
            if (p != null) {
                for (int i = 0; i < p.getChildCount(); i++) {
                    if (p.getChildAt(i) instanceof TextView) {
                        if (((TextView) p.getChildAt(i)).getText().toString().equalsIgnoreCase(getString(R.string.loading))) {
                            p.getChildAt(i).setVisibility(View.GONE);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected int getRequestCode(int requestCode) {
        if (requestCode != -1 && (requestCode & 0xffff0000) != 0) {
            requestCode = requestCode & 0x0000ffff;
        }
        return requestCode;
    }
}
