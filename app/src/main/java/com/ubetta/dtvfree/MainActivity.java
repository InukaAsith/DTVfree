package com.ubetta.dtvfree;
import static android.app.ProgressDialog.show;

import android.Manifest;

import android.app.AlertDialog;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import android.graphics.Bitmap;

import android.net.Uri;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;

import android.webkit.URLUtil;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    private ImageView mousePointer;
    private  CountDownTimer pointerVisibilityTimer;
    Timer pointerMoveTimer;
    private  int velocityX = 0,velocityY = 0,keyCode;
    private  int screenWidth,screenHeight;
    private final int RECORD_REQUEST_CODE= 101,pointerAcceleration = 1;
    private Browser browser;
    private WebClient webClient;
    private int x = 0, y = 0,row,column;
    private SearchManager searchManager;
    private EditText searchBar ;
    private RelativeLayout frame,dialogBack;
    private boolean firstDown = true;
    private ImageButton homeButton,forwardButton,backButton,refreshButton, editButton, closeButton, updateButton, cursorButton;
    private View[][] panelViews ;

    static final int PERMISSION_REQUEST_DOWNLOAD = 3;

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_DOWNLOAD);
        }
    }
    private String homePage = "https://datafreetv.live/";

    private boolean nocursor = false;
    private String sourcecode = "https://github.com/InukaAsith/DTVfree/releases";
    private String version = "v4.2.1";
    private final int UP = 0,DOWN = 1,LEFT = 2,RIGHT = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialogBack = findViewById(R.id.dialog_back);
        frame = findViewById(R.id.frame);
// check if the app is launched for the first time
        // get SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        // get the SharedPreferences.Editor object



        // get your string from SharedPreferences
        String homepge = sharedPref.getString("homepage", homePage);
        boolean cursormode = sharedPref.getBoolean("nocursor", false);
        nocursor = cursormode;



        boolean isFirstTime = sharedPref.getBoolean("isFirstTime", false);

// if yes, show the popup message

        if (isFirstTime) {
            // create an AlertDialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Welcome Back");
            builder.setMessage("If you want to load an custom website as homepage enter it below. ");
            // create an EditText for the user to input data
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint(" Default is dtv.tkonly.xyz");
            // set the EditText as the view of the AlertDialog
            builder.setView(input);

            // set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // get the input data and store it in SharedPreferences
                    String data = input.getText().toString();
                    String search = "https";
                    String webqr = ".com";
                    if (data.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                        String homestr = data;
                        SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                        editor.putString("homepage", homestr);

// apply the changes
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect  ", Toast.LENGTH_SHORT).show();
                    }
                    else if (data.toLowerCase().indexOf(webqr.toLowerCase()) != -1) {
                        String homestr = ("https://" + data);
                        SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                        editor.putString("homepage", homestr);
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect  ", Toast.LENGTH_SHORT).show();
// apply the changes

                    }
                    else {
                        String homestr = homePage;
                        SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                        editor.putString("homepage", homestr);

// apply the changes
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Entered format is invalid Homepage set to " + homestr + "  Please restart application in order for changes to take effect  ", Toast.LENGTH_SHORT).show();
                    }
                    //sharedPref.edit().putString("homepage", data).apply();

                    // set the flag to false so the popup will not show again
                    sharedPref.edit().putBoolean("isFirstTime", false).apply();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    sharedPref.edit().putBoolean("isFirstTime", false).apply();
                    editor.apply();
                    // cancel the dialog
                    dialog.cancel();
                }
            });

            // show the dialog
            builder.show();
        }

        homeButton = findViewById(R.id.home_button);
        cursorButton = findViewById(R.id.cursor_button);
        if (nocursor) {
            cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background));

        }

        closeButton = findViewById(R.id.close_button);
        clearButton = findViewById(R.id.clear_button);
        editButton = findViewById(R.id.edit_button);
        updateButton = findViewById(R.id.update_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(homepge);
                hideView(dialogBack);
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.clearCache (true);
                Toast.makeText(MainActivity.this, "Cleaned all saved website data. Reload websites to load the latest versions of sites", Toast.LENGTH_SHORT).show(); 
                hideView(dialogBack);
            }
        });
        cursorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SharedPreferences sharedPref2 = getSharedPreferences("myPref", MODE_PRIVATE);

                if (nocursor) {

                    nocursor = false;
                    Toast.makeText(MainActivity.this, "Mouse Cursor Enabled", Toast.LENGTH_SHORT).show();
                    cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background_def));
                    hideView(dialogBack)
                    SharedPreferences.Editor editor = sharedPref.edit();
                    sharedPref.edit().putBoolean("nocursor", false).apply();
                    editor.apply();
                    
                }
                else{
                    nocursor = true;
                    cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background));
                    Toast.makeText(MainActivity.this, "Mouse Cursor Disabled", Toast.LENGTH_SHORT).show();
                    hideView(dialogBack);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    sharedPref.edit().putBoolean("nocursor", true).apply();
                    editor.apply();
                    
                }

            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(sourcecode);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Download Update")
                        .setMessage("Current app version is " + version + ".  Goto assets and download latest apk file and install it from your file manager app")
                        .setPositiveButton("OK", (dialog1, which1) -> {})
                        .show();
                hideView(dialogBack);
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Edit Homepage");
                builder.setMessage("Enter new website or webaddress");

// create an EditText object
                final EditText input = new EditText(MainActivity.this);

// set the input type and hint
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Enter new website or webaddress");

// set the EditText object as the view of the AlertDialog.Builder object
                builder.setView(input);

// set the positive and negative buttons of the AlertDialog.Builder object
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get the input value from the EditText object
                        String value = input.getText().toString();
                        String search = "https";
                        String webqr = ".com";

                        if (value.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                            String homestr = value;
                            SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                            editor.putString("homepage", homestr);

// apply the changes
                            editor.apply();
                            Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
                        }
                        else if (value.toLowerCase().indexOf(webqr.toLowerCase()) != -1) {
                            String homestr = ("https://" + value);
                            SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                            editor.putString("homepage", homestr);
                            editor.apply();
                            Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
// apply the changes

                        }
                        else {
                            String homestr = homePage;
                            SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                            editor.putString("homepage", homestr);

// apply the changes
                            editor.apply();
                            Toast.makeText(MainActivity.this, "Entered format is invalid Homepage set to " + homestr + "  Please restart application in order for changes to take effect  ", Toast.LENGTH_SHORT).show();


// apply the changes
                        }
                        // store it in the variable
                        // you can use any variable name you want

                        // get SharedPreferences

// get the SharedPreferences.Editor object
                        //SharedPreferences.Editor editor = sharedPref.edit();

                        // do something with the variable
                        // for example, display it in a Toast

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // cancel the dialog
                        dialog.cancel();
                    }
                });

// create and show the AlertDialog object from the AlertDialog.Builder object
                AlertDialog dialog = builder.create();
                dialog.show();





            }




        });
        backButton = findViewById(R.id.back_button);
        forwardButton = findViewById(R.id.forward_button);
        refreshButton = findViewById(R.id.refresh_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!webView.canGoBack()){

                    finish();
                }else {
                    webView.goBack();
                    hideView(dialogBack);
                }
            }
        });
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goForward();
                hideView(dialogBack);
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
                hideView(dialogBack);
            }
        });

        mousePointer = findViewById(R.id.mouse_pointer);
        searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    doSearch(v.getText().toString());
                    //searchView.onActionViewExpanded();
                    searchBar.clearFocus();
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String queary = webView.getUrl();
                    if (queary!= null) {
                        searchBar.setText(queary);
                    }
                    else {
                        searchBar.setText(queary);
                    }
                }
                return false;
            }
        });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });



        webView = findViewById(R.id.web_view);
        

        ProgressBar loadingIndicator = findViewById(R.id.loading_indicator);
        webView.setWebViewClient(browser = new Browser(searchBar,webView));
        webView.setWebChromeClient(webClient = new WebClient(this));

        WebSettings webSettings = webView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        

        // enable JavaScript
        webSettings.setJavaScriptEnabled(true);
        // enable web storage
        webSettings.setDomStorageEnabled(true)
        SharedPreferences sitelist = getSharedPreferences (“saved_sites”, MODE_PRIVATE); // get a SharedPreferences object with the name “saved_sites”

        

         // load the site in the WebView
        webView.setOnLongClickListener(v -> {
            String url = null, imageUrl = null;
            WebView.HitTestResult r = ((WebView) v).getHitTestResult();
            switch (r.getType()) {
                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                    url = r.getExtra();
                    break;
                case WebView.HitTestResult.IMAGE_TYPE:
                    imageUrl = r.getExtra();
                    break;
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                case WebView.HitTestResult.EMAIL_TYPE:
                case WebView.HitTestResult.UNKNOWN_TYPE:
                    Handler handler = new Handler();
                    Message message = handler.obtainMessage();
                    ((WebView)v).requestFocusNodeHref(message);
                    url = message.getData().getString("url");
                    if ("".equals(url)) {
                        url = null;
                    }
                    imageUrl = message.getData().getString("src");
                    if ("".equals(imageUrl)) {
                        imageUrl = null;
                    }
                    if (url == null && imageUrl == null) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
            showLongPressMenu(url, imageUrl);
            return true;
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Download")
                    .setMessage(String.format("Filename: %s\nSize: %.2f MB\nURL: %s",
                            filename,
                            contentLength / 1024.0 / 1024.0,
                            url))
                    .setPositiveButton("Download", (dialog, which) -> startDownload(url, filename))
                    .setNeutralButton("Open", (dialog, which) -> {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        try {
                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Open")
                                    .setMessage("Can't open files of this type. Try downloading instead.")
                                    .setPositiveButton("OK", (dialog1, which1) -> {})
                                    .show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {})
                    .show();
        });

    
        SharedPreferences sitelist = getSharedPreferences ("saved_sites", MODE_PRIVATE);
// Set a webview client to the webview
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Show the loading indicator when the webview starts loading
                loadingIndicator.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide the loading indicator when the webview finishes loading
                loadingIndicator.setVisibility(View.GONE);
                super.onPageFinished(view, url);
                searchBar.setHint(webView.getUrl());
            }

            @Override
             public boolean shouldOverrideUrlLoading (WebView view, String url) {
    // check if the URL is in the saved sites list
                boolean isSaved = sitelist.getBoolean (url, false);

    // set the cache mode according to the URL's status in the list
                if (isSaved) {
      // if the URL is in the list, set the cache mode to load from cache only
                    webSettings.setCacheMode (WebSettings.LOAD_CACHE_ONLY);
               } else {
      // if the URL is not in the list, set the cache mode to load from network only
                    webSettings.setCacheMode (WebSettings.LOAD_NO_CACHE);
                 }

    // return false to let the WebView load the URL
                  return false;
  }




            final InputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);

            String lastMainPage = "";


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                return super.shouldInterceptRequest(view, request);
            }



            final String[] sslErrors = {"Not yet valid", "Expired", "Hostname mismatch", "Untrusted CA", "Invalid date", "Unknown error"};




        });

        webView.getSettings().setSupportMultipleWindows(false);
        webView.loadUrl(homepge);

        panelViews = new View[][]{{searchBar, homeButton}, {backButton,forwardButton,cursorButton,refreshButton,editButton ,updateButton, closeButton}};
        row = 0;
        column = 1;
        panelViews[row][column].setFocusable(true);
        panelViews[row][column].requestFocus();
        panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.voice_button_focus_background));

    }
    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged (newConfig);
        // You can perform any custom actions here when the orientation changes
    }
    private  void hideView(View v){
        v.setVisibility(View.GONE);
    }
    private void startDownload(String url, String filename) {
        checkPermission();
        Toast.makeText(MainActivity.this, "Download Started", Toast.LENGTH_SHORT).show();
        if (filename == null) {
            filename = URLUtil.guessFileName(url, null, null);
        }
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(url));
        } catch (IllegalArgumentException e) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Can't Download URL")
                    .setMessage(url)
                    .setPositiveButton("OK", (dialog1, which1) -> {})
                    .show();
            return;
        }
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie != null) {
            request.addRequestHeader("Cookie", cookie);
        }
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        assert dm != null;
        dm.enqueue(request);
    }

    private void showLongPressMenu(String linkUrl, String imageUrl) {
        String url;
        String title;
        String[] options = new String[]{"Open in new tab", "Copy URL", "Show full URL", "Download"};

        if (imageUrl == null) {
            if (linkUrl == null) {
                throw new IllegalArgumentException("Bad null arguments in showLongPressMenu");
            } else {
                // Text link
                url = linkUrl;
                title = linkUrl;
            }
        } else {
            if (linkUrl == null) {
                // Image without link
                url = imageUrl;
                title = "Image: " + imageUrl;
            } else {
                // Image with link
                url = linkUrl;
                title = linkUrl;
                String[] newOptions = new String[options.length + 1];
                System.arraycopy(options, 0, newOptions, 0, options.length);
                newOptions[newOptions.length - 1] = "Image Options";
                options = newOptions;
            }
        }
        new AlertDialog.Builder(MainActivity.this).setTitle(title).setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    newTab(url);
                    break;
                case 1:
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    assert clipboard != null;
                    ClipData clipData = ClipData.newPlainText("URL", url);
                    clipboard.setPrimaryClip(clipData);
                    break;
                case 2:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Full URL")
                            .setMessage(url)
                            .setPositiveButton("OK", (dialog1, which1) -> {})
                            .show();
                    break;
                case 3:
                    startDownload(url, null);
                    break;
                case 4:
                    showLongPressMenu(null, imageUrl);
                    break;
            }
        }).show();
    }

    private void newTab(String url) {
        webView = findViewById(R.id.web_view);

        ProgressBar loadingIndicator = findViewById(R.id.loading_indicator);
        webView.setWebViewClient(browser = new Browser(searchBar,webView));
        webView.setWebChromeClient(webClient = new WebClient(this));

        WebSettings webSettings = webView.getSettings();
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        // enable JavaScript
        webSettings.setJavaScriptEnabled(true);
        // enable web storage
        webSettings.setDomStorageEnabled(true);

        webView.loadUrl(url);;

    }

    public void doSearch(String query){
        if(query != null) {
            String search = "https";
            String webqr = ".com";
            if (query.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                webView.loadUrl(query);
            }
            else if (query.toLowerCase().indexOf(webqr.toLowerCase()) != -1) {
                webView.loadUrl("https://" + query);
            }
            else {
                webView.loadUrl("https://google.com/search?q=" + query);
            }

            hideView(dialogBack);
        }
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        screenWidth = webView.getWidth();
        screenHeight = webView.getHeight();
    }
    public void nextView(int num){
        int rowTemp,columnTemp;
        rowTemp = row;
        columnTemp = column;
        switch (num){
            case UP:
            case DOWN:
                if(row == 0){
                    row = 1;
                    column = 0;
                }else if(row == 1){
                    row = 0;
                    column = 1;
                }
                break;
            case LEFT:
                if(row == 0){
                    if(column == 0){
                        column = 1;
                    }else{
                        column--;
                    }
                }else if(row == 1){
                    if(column == 0){
                        column = 6;
                    }else{
                        column --;
                    }
                }
                break;
            case RIGHT:
                if(row == 0){
                    if(column == 1){
                        column = 0;
                    }else{
                        column ++;
                    }
                }else if(row == 1){
                    if(column == 6){
                        column = 0;
                    }else{
                        column ++;
                    }
                }
                break;
            default:
                break;
        }
        panelViews[rowTemp][columnTemp].clearFocus();
        panelViews[row][column].setFocusable(true);
        panelViews[row][column].requestFocus();
        if(rowTemp == 0 ){
            if(columnTemp == 0) {
                panelViews[rowTemp][columnTemp].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.search_bar_background));
            }else{
                panelViews[rowTemp][columnTemp].setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.voice_button_background_default));
                    
            }
        }else {
            panelViews[rowTemp][columnTemp].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_default_background));
                    if (nocursor) {
            cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background));

        }
        }
        if(row == 0 ){
            if(column == 0) {
                panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.search_bar_focus_background));
            }else{
                panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.voice_button_focus_background));
            }
        }else {
            panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_focus_background));
                    if (nocursor) {
            cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background));

        }
        }
    }
    public void dialogEvent(int keyCode){
        //mousePointer.setVisibility(View.GONE);
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                Log.e("current focus",""+getCurrentFocus());
                dialogBack.setVisibility(View.GONE);
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                nextView(UP);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                nextView(DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                nextView(LEFT);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                nextView(RIGHT);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.e("current focus",""+getCurrentFocus());
                getCurrentFocus().performClick();
                break;
            default:
                break;
        }
    }
    public void movePointer(){

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                velocityX = 0;
                velocityY += pointerAcceleration;
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                velocityX = 0;
                velocityY -= pointerAcceleration;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                velocityX -= pointerAcceleration;
                velocityY = 0;
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                velocityX += pointerAcceleration;
                velocityY = 0;
                break;

            default:
                break;
        }
        x += velocityX;
        y += velocityY;
        if(x + mousePointer.getWidth() / 2 > screenWidth){
            x = screenWidth - mousePointer.getWidth() / 2;
        }else if(x < 0){
            x = 0;
        }
        if(y + mousePointer.getHeight() > screenHeight){
            y = screenHeight - mousePointer.getHeight();
            webView.scrollBy(0, velocityY);
        }else if(y < 0){
            webView.scrollBy(0,velocityY);
            y = 0;
        }
        mousePointer.setX(x);
        mousePointer.setY(y);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        SharedPreferences sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BACK || keyCode ==  KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

            if (dialogBack.getVisibility() == View.VISIBLE && event.getAction() != KeyEvent.ACTION_UP) {
                dialogEvent(keyCode);
            } else {//Dialog not visible
                if (nocursor) {
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        dialogBack.setVisibility(View.VISIBLE);
                        panelViews[row][column].requestFocus();

                    }else{
                        return super.dispatchKeyEvent(event);
                    }
                    }

                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (pointerMoveTimer != null) {
                        pointerMoveTimer.cancel();
                    }
                    firstDown = true;
                    velocityX = 0;
                    velocityY = 0;
                    pointerVisibilityTimer = new CountDownTimer(3 * 1000, 1000) {
                        @Override
                        public final void onTick(final long millisUntilFinished) {
                        }

                        @Override
                        public final void onFinish() {
                            mousePointer.setVisibility(View.GONE);
                        }
                    }.start();
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (pointerVisibilityTimer != null) {
                        pointerVisibilityTimer.cancel();
                    }
                    if (mousePointer.getVisibility() == View.GONE) {
                        mousePointer.setVisibility(View.VISIBLE);
                    }
                }
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        final long uMillis = SystemClock.uptimeMillis();
                        frame.dispatchTouchEvent(MotionEvent.obtain(uMillis, uMillis,
                                MotionEvent.ACTION_DOWN, x, y, 0));
                        frame.dispatchTouchEvent(MotionEvent.obtain(uMillis, uMillis,
                                MotionEvent.ACTION_UP, x, y, 0));
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        PackageManager pm = getPackageManager();

                        // Check if the device is an Android TV
                        boolean isTV = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);

                        // If the device is not an Android TV, hide the status bar and the navigation bar
                        if (!isTV) {
                            if (!webView.canGoBack()) {
                                hideView(dialogBack);
                                // create an array of items to display
                                CharSequence[] items = {"Exit", "Edit Homepage", "Check Update", "Cancel"};

// create an alert dialog builder
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Exitting Application");

// add the items to the dialog
                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // handle the click event of each item
                                        switch (which) {
                                            case 0:
                                                finish();
                                                break;
                                            case 1:
                                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                builder.setTitle("Edit Homepage");
                                                builder.setMessage("Enter new website or webaddress");
                                                final EditText input = new EditText(MainActivity.this);
                                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                                input.setHint("Enter new website or webaddress");
                                                builder.setView(input);
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // get the input value from the EditText object
                                                        String value = input.getText().toString();
                                                        String search = "https";
                                                        String webqr = ".com";

                                                        if (value.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                                                            String homestr = value;
                                                            SharedPreferences.Editor editor = sharedPref.edit();
                                                            editor.putString("homepage", homestr);
                                                            editor.apply();
                                                            Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else if (value.toLowerCase().indexOf(webqr.toLowerCase()) != -1) {
                                                            String homestr = ("https://" + value);
                                                            SharedPreferences.Editor editor = sharedPref.edit();
                                                            editor.putString("homepage", homestr);
                                                            editor.apply();
                                                            Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();

                                                        }
                                                        else {
                                                            String homestr = homePage;
                                                            SharedPreferences.Editor editor = sharedPref.edit();
                                                            editor.putString("homepage", homestr);
                                                            editor.apply();
                                                            Toast.makeText(MainActivity.this, "Entered format is invalid Homepage set to " + homestr + "  Please restart application in order for changes to take effect  ", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // cancel the dialog
                                                        dialog.cancel();
                                                    }
                                                });

// create and show the AlertDialog object from the AlertDialog.Builder object
                                                AlertDialog newdialog = builder.create();
                                                newdialog.show();
                                                hideView(dialogBack);
                                                break;
                                            case 2:
                                                webView.loadUrl(sourcecode);
                                                hideView(dialogBack);
                                                // do something for button 3
                                                break;
                                            case 3:
                                                hideView(dialogBack);
                                                // do something for button 4
                                                break;
                                        }
                                    }
                                });

// create and show the dialog
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            } else {
                                webView.goBack();
                                break;
                            }
                        }
                        if (webClient.isFullScreen()) {
                            webClient.onHideCustomView();
                        } else {
                           // if (nocursor) {
                              //  cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background));

                            //}
                            dialogBack.setVisibility(View.VISIBLE);
                            panelViews[row][column].requestFocus();
                        }
                        break;

                }
                if (firstDown) {
                    firstDown = false;
                    pointerMoveTimer = new Timer();
                    pointerMoveTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            movePointer();
                        }
                    }, 0, 1000 / 60);

                }
            }
            return true;
        }else{
            return super.dispatchKeyEvent(event);
        }
    }

}
