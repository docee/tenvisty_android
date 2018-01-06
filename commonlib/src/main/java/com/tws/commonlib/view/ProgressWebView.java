package com.tws.commonlib.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tws.commonlib.R;
import com.tws.commonlib.base.MyConfig;

/**
 * @Description:带进度条的WebView
 * @author http://blog.csdn.net/finddreams
 */
@SuppressWarnings("deprecation")
public class ProgressWebView extends WebView {

    private ProgressBar progressbar;

    public ProgressWebView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        progressbar = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal);
        progressbar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                10, 0, 0));

        Drawable drawable = context.getResources().getDrawable(R.drawable.progress_bar_states);
        progressbar.setProgressDrawable(drawable);
        addView(progressbar);
        // setWebViewClient(new WebViewClient(){});
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String c = "";

                if(url.indexOf("lang=")==-1){
                    if(url.indexOf('?')>-1){
                        c = "&";
                    }else{
                        c = "?";
                    }
                    c = c+"app="+ MyConfig.getAppName()+"&lang="+getResources().getConfiguration().locale.getLanguage();
                }
                view.loadUrl(url+c);
                return true;
            }
        });
        //是否可以缩放
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
    }

    public class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                progressbar.setVisibility(GONE);
            } else {
                if (progressbar.getVisibility() == GONE)
                    progressbar.setVisibility(VISIBLE);
                progressbar.setProgress(newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressbar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}