package com.ubetta.dtvfree;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;


public class WebClient extends WebChromeClient {
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    private MainActivity mainActivity;
    private boolean fullScreen = false;
    private boolean isVideoFullScreen = false;
    private boolean ImmFullScreen = false;
    // Add a Bundle variable to store the state of the webview


    public WebClient(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }
    public boolean isimFullScreen() {
        return ImmFullScreen;
    }
    public Bitmap getDefaultVideoPoster() {
        if (mainActivity == null) {
            return null;
        }
        return BitmapFactory.decodeResource(mainActivity.getApplicationContext().getResources(), 2130837573);
    }

    public void onHideCustomView() {
        fullScreen = false;
        isVideoFullScreen = false;
        ImmFullScreen = false;
        ((FrameLayout) mainActivity.getWindow().getDecorView()).removeView(this.mCustomView);
        this.mCustomView = null;
        mainActivity.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
        mainActivity.setRequestedOrientation(this.mOriginalOrientation);
        this.mCustomViewCallback.onCustomViewHidden();
        this.mCustomViewCallback = null;
    }

    public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
        fullScreen = true;
        if (this.mCustomView != null) {
            onHideCustomView();
            return;
        }

        this.mCustomView = paramView;
        this.mOriginalSystemUiVisibility = mainActivity.getWindow().getDecorView().getSystemUiVisibility();
        this.mOriginalOrientation = mainActivity.getRequestedOrientation();
        this.mCustomViewCallback = paramCustomViewCallback;
        ((FrameLayout) mainActivity.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
        PackageManager pm = mainActivity.getPackageManager();

        // Check if the device is an Android TV
        boolean isTV = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);

        // If the device is not an Android TV, hide the status bar and the navigation bar
        if (!isTV) {
            isVideoFullScreen = true;
            mainActivity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            isVideoFullScreen = false;
            ImmFullScreen = true;
            mainActivity.getWindow().getDecorView().setSystemUiVisibility(3846);
            this.mCustomViewCallback.onCustomViewHidden();
        }
    }

    // Define a getter method for the isVideoFullScreen variable
    public boolean isVideoFullScreen() {
        return isVideoFullScreen;
    }
    }




