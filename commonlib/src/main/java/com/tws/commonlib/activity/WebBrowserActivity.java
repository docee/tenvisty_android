package com.tws.commonlib.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ProgressBar;

import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.view.ProgressWebView;

import java.util.Locale;


public class WebBrowserActivity extends BaseActivity {
//WebView webView;

    String strTitle;
    String strUrl;
    protected ProgressWebView mWebView;
    private ProgressBar web_progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webbrowser);
        overridePendingTransition(R.anim.menlistfadein, R.anim.menlistfadeout);
        strTitle = this.getIntent().getStringExtra("title");
        strUrl = this.getIntent().getStringExtra("url");
        NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setTitle(strTitle);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        if (mWebView.canGoBack()) {
                            mWebView.goBack();
                        } else {
                            WebBrowserActivity.this.finish();
                        }
                        break;
                }
            }
        });
        mWebView = (ProgressWebView) findViewById(R.id.baseweb_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        initData();
//		webView = (WebView)findViewById(R.id.webView);
//		WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setDomStorageEnabled(true);
//
//        // webView.loadUrl((String)getText(R.string.tenvis_url_weijia));
//        webView.loadUrl(strUrl+"?app="+GlobalConfig.GetInstance((MyApp)this.getApplicationContext()).getAppName()+"&lang="+getResources().getConfiguration().locale.getLanguage());
//
//        final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progressBar1);
//        webView.setWebChromeClient(new WebChromeClient() {
//
//            @Override
//			public void onProgressChanged(WebView view, int progress) {
//
//                if (progress == 100) {
//                	webView.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.GONE);
//
//                }
//            }
//        });
//
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                    return false;
//            }
//          });
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.reload();
        }

        super.onPause();
    }

    private void initData() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = Locale.getDefault();
        }
        mWebView.loadUrl(url + "?app=" + MyConfig.getAppName() + "&lang=" + locale.getLanguage());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    WebBrowserActivity.this.finish();
                }

                break;
        }
        return true;
    }
}
