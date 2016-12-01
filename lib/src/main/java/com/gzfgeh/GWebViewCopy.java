package com.gzfgeh;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.gzfgeh.gwebview.R;

/**
 * Description:
 * Created by guzhenfu on 2016/11/23 11:19.
 */

public class GWebViewCopy extends FrameLayout {
    protected ViewGroup mProgressView;
    protected ViewGroup mErrorView;
    private WebView webview;

    private int mProgressId;
    private int mErrorId;

    private SettingBuilder settingBuilder;
    private OnLoadFinishListener listener;

    public void setListener(OnLoadFinishListener listener) {
        this.listener = listener;
    }

    public GWebViewCopy(Context context) {
        super(context);
        initView();
    }

    public GWebViewCopy(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public GWebViewCopy(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initView();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.gwebview);
        try {
            mProgressId = a.getResourceId(R.styleable.gwebview_layout_progress, 0);
            mErrorId = a.getResourceId(R.styleable.gwebview_layout_error, 0);
        }finally {
            a.recycle();
        }
    }

    private void initView() {
        if(isInEditMode())
            return;

        View v = LayoutInflater.from(getContext()).inflate(R.layout.web_view_layout, this);
        mProgressView = (ViewGroup) v.findViewById(R.id.progress);

        if (mProgressId == 0)
            mProgressId = R.layout.view_progress;
        LayoutInflater.from(getContext()).inflate(mProgressId,mProgressView);

        mErrorView = (ViewGroup) v.findViewById(R.id.error);
        if(mErrorId == 0)
            mErrorId = R.layout.view_error;
        LayoutInflater.from(getContext()).inflate(mErrorId,mErrorView);

        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                webview.reload();
            }
        });
        initWebView(v);
    }

    private void initWebView(View v) {
        webview = (WebView) v.findViewById(R.id.web_view_in);
        settingBuilder = new SettingBuilder(getContext() , webview);

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100){
                    showWebView();
                    if (listener != null)
                        listener.loadFinish();
                }else{
                    showProgressView();
                }
                if (!NetWorkUtils.isNetworkAvailable(getContext())){
                    showErrorView();
                    if (listener != null)
                        listener.loadFinish();
                }
                super.onProgressChanged(view, newProgress);
            }
        });

    }

    public WebView getWebView(){
        return webview;
    }

    public void loadUrl(String url){
        webview.loadUrl(url);
    }

    public void setWebChromeClient(WebChromeClient client){
        webview.setWebChromeClient(client);
    }

    public void setWebViewClient(WebViewClient client){
        webview.setWebViewClient(client);
    }


    public SettingBuilder getSettingBuilder() {
        return settingBuilder;
    }

    public void showErrorView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(VISIBLE);
    }

    public void showEmptyView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(GONE);
    }

    public void showProgressView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(VISIBLE);
        mErrorView.setVisibility(GONE);
    }

    public void showWebView(){
        webview.setVisibility(VISIBLE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(GONE);
    }

    public  class SettingBuilder{

        WebSettings webSettings;
        public SettingBuilder(Context context , WebView webView){
            webSettings = webView.getSettings();
            webSettings.setSupportMultipleWindows(true);// 支持多窗口
            webSettings.setSupportZoom(false);// 设置可以支持缩放
            webSettings.setBuiltInZoomControls(false); // 设置支持缩放
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                webSettings.setDisplayZoomControls(false); // 隐藏webview缩放按钮
            }
            // 设置是否显示网络图像---true,封锁网络图片，不显示 false----允许显示网络图片
            webSettings.setBlockNetworkImage(false);
            webSettings.setJavaScriptEnabled(true);// 访问页面中有JavaScript,必须设置支持JavaScript
            webSettings.setDefaultTextEncodingName("UTF-8");
            webSettings.setLoadsImagesAutomatically(true); // 设置自动加载图片
            webSettings.setUseWideViewPort(true); // 将图片调整到适合WebView大小
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setDatabaseEnabled(true);// 启用数据库
            webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
            String dir = context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();  // 设置定位的数据库路径
            webSettings.setGeolocationDatabasePath(dir);
            webSettings.setGeolocationEnabled(true);// 启用地理定位
            webSettings.setDomStorageEnabled(true);

            webSettings.setDefaultFontSize((int)15);
        }
        public WebSettings build(){
            return this.webSettings;
        }
    }

    public interface OnLoadFinishListener{
        void loadFinish();
    }

}
