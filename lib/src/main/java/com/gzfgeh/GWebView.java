package com.gzfgeh;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
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

public class GWebView extends FrameLayout implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeRefreshLayout.OnRefreshListener refreshListener;

    private ViewGroup mProgressView;
    private ViewGroup mErrorView;
    private ViewGroup mNoNetView;
    private ViewGroup mHeaderView;
    private ViewGroup mFooterView;
    private int mProgressId;
    private int mErrorId;
    private int mNoNetId;
    private int mHeaderId;
    private int mFooterId;

    private WebView webview;
    private OnLoadFinishListener listener;
    private Builder builder;

    public final static int Success = 0;
    public final static int Error = 1;
    public final static int NoNet = 2;
    public final static int Loading = 3;
    private int status;
    private boolean isError;

    public Builder getBuilder() {
        return builder;
    }

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
            mNoNetId = a.getResourceId(R.styleable.gwebview_layout_nonet, 0);
            mHeaderId = a.getResourceId(R.styleable.gwebview_layout_header, 0);
            mFooterId = a.getResourceId(R.styleable.gwebview_layout_footer, 0);
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

        mHeaderView = (ViewGroup) v.findViewById(R.id.header);
        mFooterView = (ViewGroup) v.findViewById(R.id.footer);

        mErrorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setStatus(Loading);
                webview.reload();
            }
        });
        mNoNetView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setStatus(Loading);
                webview.reload();
            }
        });
        initWebView(v);
        initSwipeView(v);
        setStatus(Loading);
    }

    private void initSwipeView(View v) {
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void initWebView(View v) {
        webview = (WebView) v.findViewById(R.id.web_view_in);
        webview.setWebChromeClient(new GWebChromeClient());
    }

    private GWebView setHeaderView(View view){
        if (view == null)
            mHeaderView.setVisibility(GONE);
        else{
            mHeaderView.removeAllViews();
            mHeaderView.addView(view);
        }
        return this;
    }

    private GWebView setFooterView(View view){
        if (view == null)
            mFooterView.setVisibility(GONE);
        else{
            mFooterView.removeAllViews();
            mFooterView.addView(view);
        }
        return this;
    }

    @IntDef({Success, Error, NoNet, Loading})
    public @interface Status {}

    public void setStatus(@Status int status){
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
    }

    private void showProgressView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(VISIBLE);
        mErrorView.setVisibility(GONE);
        mNoNetView.setVisibility(GONE);
    }

    private void showNoNetView(){
        webview.setVisibility(GONE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(GONE);
        mNoNetView.setVisibility(VISIBLE);
    }

    private void showWebView(){
        webview.setVisibility(VISIBLE);
        mProgressView.setVisibility(GONE);
        mErrorView.setVisibility(GONE);
        mNoNetView.setVisibility(GONE);
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null)
            refreshListener.onRefresh();

        webview.reload();
    }

    public static class Builder{
        private Context context;
        private String url;
        private WebViewClient client;
        private WebChromeClient chromeClient;
        private View headerView, footerView;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder loadUrl(@NonNull String s){
            url = s;
            return this;
        }

        public Builder setWebViewClient(@NonNull GWebViewClient webClient){
            client = webClient;
            return this;
        }

        public Builder setWebChromeClient(@NonNull GWebChromeClient chromeClient){
            this.chromeClient = chromeClient;
            return this;
        }

        public Builder addHeaderView(@NonNull View view){
            headerView = view;
            return this;
        }

        public Builder addHeaderView(@LayoutRes int layout){
            headerView = LayoutInflater.from(context).inflate(layout, null);
            return this;
        }

        public Builder addFooterView(@NonNull View view){
            footerView = view;
            return this;
        }

        public Builder addFooterView(@LayoutRes int layout){
            footerView = LayoutInflater.from(context).inflate(layout, null);
            return this;
        }

        public void setGWebView(GWebView gwebview){
            gwebview.getWebView().loadUrl(url);
            gwebview.getWebView().setWebViewClient(client);
            gwebview.getWebView().setWebChromeClient(chromeClient);
            gwebview.setHeaderView(headerView);
            gwebview.setFooterView(footerView);
        }
    }

    public class GWebViewClient extends WebViewClient{
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            isError = true;
        }
    }

    public class GWebChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100){
                if (listener != null)
                    listener.loadFinish();
                swipeRefreshLayout.setRefreshing(false);

                if (!NetWorkUtils.isNetworkAvailable(getContext())){
                    setStatus(NoNet);
                }else if (isError) {
                    setStatus(Error);
                }else {
                    setStatus(Success);
                }
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    public interface OnLoadFinishListener{
        void loadFinish();
    }

}
