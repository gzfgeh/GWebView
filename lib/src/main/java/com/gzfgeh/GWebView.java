package com.gzfgeh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.gzfgeh.gwebview.R;
import com.gzfgeh.swipeheader.SwipeRefreshLayout;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Description:
 * Created by guzhenfu on 2016/11/23 11:19.
 */

public class GWebView extends FrameLayout implements android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener refreshListener;
    private ScrollView scrollView;

    private ViewGroup mProgressView;
    private ViewGroup mErrorView;
    private ViewGroup mNoNetView;
    private int mProgressId;
    private int mErrorId;
    private int mNoNetId;

    private WebView webview;
    private GWebView.OnLoadFinishListener listener;
    private SettingBuilder settingBuilder;

    private Observable<Long> mObservable;
    private Subscription subscription;
    private int timeOut = 5;

    public final static int Success = 0;
    public final static int Error = 1;
    public final static int NoNet = 2;
    public final static int Loading = 3;
    private int status;
    private boolean isError;
    private String url;

    public GWebView setTimeOut(int timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public SettingBuilder getSettingBuilder() {
        return settingBuilder;
    }

    public GWebView setOnLoadListener(GWebView.OnLoadFinishListener listener) {
        this.listener = listener;
        return this;
    }

    public GWebView(Context context) {
        super(context);
        initView();
    }

    public GWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public GWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initView();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.gwebview);
        try {
            mProgressId = a.getResourceId(R.styleable.gwebview_layout_progress, 0);
            mErrorId = a.getResourceId(R.styleable.gwebview_layout_error, 0);
            mNoNetId = a.getResourceId(R.styleable.gwebview_layout_nonet, 0);
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

        mNoNetView = (ViewGroup) v.findViewById(R.id.nonet);
        if(mNoNetId == 0)
            mNoNetId = R.layout.view_no_net;
        LayoutInflater.from(getContext()).inflate(mNoNetId,mNoNetView);

        if (mErrorId == R.layout.view_error){
            mErrorView.findViewById(R.id.reload).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadUrl(url);
                }
            });
        }

        if (mNoNetId == R.layout.view_no_net){
            mNoNetView.findViewById(R.id.reload).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    loadUrl(url);
                }
            });
        }

        initWebView(v);
        initSwipeView(v);
    }

    private void initSwipeView(View v) {
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        scrollView = (ScrollView) v.findViewById(R.id.scrollView);
    }

    private void initWebView(View v) {
        webview = (WebView) v.findViewById(R.id.web_view_in);
        new SettingBuilder(getContext(), webview);
        mObservable = Observable.timer(timeOut, TimeUnit.SECONDS);
        setWebViewClient(new GWebViewClient());
    }

    /**
     * 开始 webview 加载 定时
     */
    private void startTime(){
        subscription  = mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (subscription != null) {
                            isError = true;
                            setStatus(Error);
                            endTime();
                        }
                    }
                });
    }

    /**
     * 取消  webview 加载 定时
     */
    private void endTime(){
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
            webview.pauseTimers();
            webview.stopLoading();
        }
    }

    public GWebView loadUrl(@NonNull String url){
        this.url = url;
        webview.loadUrl(url);
        startTime();
        setStatus(Loading);
        return this;
    }

    public GWebView setWebViewClient(@NonNull GWebView.GWebViewClient client){
        webview.setWebViewClient(client);
        return this;
    }

    public GWebView setWebChromeClient(@NonNull WebChromeClient client){
        webview.setWebChromeClient(client);
        return this;
    }

    @IntDef({Success, Error, NoNet, Loading})
    public @interface Status {}

    public void setStatus(@GWebView.Status int status){
        this.status = status;
        switch (status){
            case Success:
                showWebView();
                break;

            case Error:
                showErrorView();
                break;

            case NoNet:
                showNoNetView();
                break;

            case Loading:
                showProgressView();
                break;
        }
    }

    public WebView getWebView(){
        return webview;
    }


    private void showErrorView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(VISIBLE);
        mNoNetView.setVisibility(GONE);
        scrollView.setFillViewport(true);
    }

    private void showProgressView(){
        isError = false;
        webview.setVisibility(GONE);
        mProgressView.setVisibility(VISIBLE);
        mErrorView.setVisibility(GONE);
        mNoNetView.setVisibility(GONE);
        scrollView.setFillViewport(true);
    }

    private void showNoNetView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(GONE);
        mNoNetView.setVisibility(VISIBLE);
        scrollView.setFillViewport(true);
    }

    private void showWebView(){
        webview.setVisibility(VISIBLE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(GONE);
        mNoNetView.setVisibility(GONE);
        scrollView.setFillViewport(true);
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null)
            refreshListener.onRefresh();

        webview.loadUrl(url);
        startTime();
    }


    public GWebView setProgressLoading(@LayoutRes int id){
        mProgressView.removeAllViews();
        mProgressId = id;
        LayoutInflater.from(getContext()).inflate(id,mProgressView);
        return this;
    }

    public GWebView setErrorLayout(@LayoutRes int id){
        mErrorView.removeAllViews();
        mErrorId = id;
        LayoutInflater.from(getContext()).inflate(id,mErrorView);
        return this;
    }

    public GWebView setNoNetLayout(@LayoutRes int id){
        mNoNetView.removeAllViews();
        mNoNetId = id;
        LayoutInflater.from(getContext()).inflate(id,mNoNetView);
        return this;
    }

    public GWebView setErrorReloadId(@IdRes int reloadId){
        mErrorView.findViewById(reloadId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUrl(url);
            }
        });
        return this;
    }

    public GWebView setNoNetReloadId(@IdRes int reloadId){
        mNoNetView.findViewById(reloadId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUrl(url);
            }
        });
        return this;
    }

    public static class SettingBuilder{
        private WebSettings webSettings;

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

        public WebSettings getWebSettings(){
            return this.webSettings;
        }
    }


    public class GWebViewClient extends WebViewClient{

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i("newProgress", url+"");
            if (listener != null)
                listener.loadFinish();
            endTime();
            swipeRefreshLayout.setRefreshing(false);

            if (!NetWorkUtils.isNetworkAvailable(getContext())){
                setStatus(NoNet);
            }else if (isError) {
                setStatus(Error);
            }else {
                setStatus(Success);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            isError = true;
        }
    }

    public interface OnLoadFinishListener{
        void loadFinish();
    }

}
