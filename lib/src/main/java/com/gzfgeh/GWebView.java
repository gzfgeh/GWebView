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

public class GWebView extends FrameLayout {
    protected ViewGroup mProgressView;
    protected ViewGroup mErrorView;
    private int mProgressId;
    private int mErrorId;

    private WebView webview;
    private OnLoadFinishListener listener;

    public void setOnLoadListener(OnLoadFinishListener listener) {
        this.listener = listener;
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

    public interface OnLoadFinishListener{
        void loadFinish();
    }

}
