package com.ubetta.dtvfree;

import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.SearchView;

public class Browser extends WebViewClient {
    EditText searchBar;
    WebView webView;
    public Browser(EditText searchBar,WebView webView) {
        this.searchBar = searchBar;
        this.webView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String s) {
       view.loadUrl(s);
       return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        searchBar.setHint(webView.getUrl());

    }

    //ignore SSL handshake errors which usually occurs in emulator
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }
    // Override the onPause method to save the state of the webview

}
