# GWebView


###            ![](/screen/webview.gif) <br>

1. 增加下拉刷新，并且解决滑动冲突 下拉刷新参考  https://github.com/gzfgeh/GSwipeRefresh 
2. 增加超时判断，屏蔽webview自带错误页面，显示自定义错误页面
3. 增加默认的加载中、无网络判断、服务器错误页面
4. 一般app都有显示自己app特点的加载中、无网络判断、服务器错误页面，很简单就可以切换 <br>
    ```
    
    <com.gzfgeh.GWebView xmlns:android="http://schemas.android.com/apk/res/android"
              android:layout_width="match_parent"
              android:layout_height="match_parent"
              xmlns:app="http://schemas.android.com/apk/res-auto"
              android:id="@+id/web_view"
              app:layout_error="@layout/error_layout" />
	      
    ```
5. 一般错误页面都有重新加载按钮，所以代码中提供了设置重载方式
    ```java
    webView.loadUrl("https://www.google.com")
        .setErrorReloadId(R.id.reload);
    ```
6. 所有设置都是链式方式设置
7. 使用

    Add it in your root build.gradle at the end of repositories: <br>  
        maven { url "https://jitpack.io" } <br>  
    Add the dependency  <br>  
	    compile 'com.github.gzfgeh:GWebView:v1.0.0' 
