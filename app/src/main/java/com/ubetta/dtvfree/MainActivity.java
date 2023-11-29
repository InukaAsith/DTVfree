package com.ubetta.dtvfree;
import static android.app.ProgressDialog.show;

import android.Manifest;

import android.app.AlertDialog;

import android.app.DownloadManager;
import android.app.PictureInPictureParams;
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

import android.os.Build;
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

import android.util.Rational;
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
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.Stack;
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
    private ImageButton hab, fab, homeButton,forwardButton,backButton,refreshButton, editButton, closeButton, updateButton, cursorButton,remsavedButton,addsavedButton,clearButton;
    private View[][] panelViews ;

    static final int PERMISSION_REQUEST_DOWNLOAD = 3;

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_DOWNLOAD);
        }
    }
    private String homePage = "https://datafreetv.live/";
    private String helppage = "https://telegra.ph/Help-Memu-11-22";

    private String site1 = "https://datafreetv.live/dtv.html";
    private String site2 = "https://datafreetv.live/peotvgo.html";
    private boolean nocursor = false;
    private String sourcecode = "https://github.com/InukaAsith/DTVfree/releases";
    private String version = "v4.3.2";
    private final int UP = 0,DOWN = 1,LEFT = 2,RIGHT = 3;
    private boolean isError; // A flag to indicate if there is an error
    private boolean isdarkm = false; 
    private String lastSuccessUrl;

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



//Check if the system dark mode is on
       int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
       if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
    //Check if the force dark feature is supported
         isdarkm = true;
       }    

        // get your string from SharedPreferences
        String homepge = sharedPref.getString("homepage", homePage);
        boolean cursormode = sharedPref.getBoolean("nocursor", true);
        boolean darkmode = sharedPref.getBoolean("darkmode", isdarkm);
        nocursor = cursormode;



        boolean isFirstTime = sharedPref.getBoolean("isFirstTime", true);
        SharedPreferences sitelist = getSharedPreferences ("saved_sites", MODE_PRIVATE);

// if yes, show the popup message

        if (isFirstTime) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Welcome Back");
            builder.setMessage("Thanks for installing application. 🎉 \n\nData Free TV" + version + "\nCurrent Homepage:" + homepge + "\n\nFor faster performance and reduced data usage this application uses offline webpage loading as default. \nWhen using offline mode you need to update or refresh website to get latest features. \n\n Android TV users can turn on or off mouse cursor based on your preference. \n\nYou can change these settings anytime from settings menu or back button menu. \n\nPlease don't forget to join telegram channel for latest updates.\nEnjoy 😊\n\nDo you want to use offline loading?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Offline loading enbled.", Toast.LENGTH_SHORT).show();
                    sitelist.edit().putBoolean(site1, true ).apply();
                    sitelist.edit().putBoolean(site2, true ).apply();
                    sitelist.edit().putBoolean(homePage , true ).apply();
                    sharedPref.edit().putBoolean("isFirstTime", false).apply();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, "Offline loading disabled.", Toast.LENGTH_SHORT).show();
                    sitelist.edit().putBoolean(site1, false ).apply();
                    sitelist.edit().putBoolean(site2, false ).apply();
                    sitelist.edit().putBoolean(homePage , false ).apply();

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
        hab =  findViewById(R.id.hab);
        closeButton = findViewById(R.id.close_button);
        clearButton = findViewById(R.id.clear_button);
        addsavedButton = findViewById(R.id.addsaved_button);
        remsavedButton = findViewById(R.id.remsaved_button);
        editButton = findViewById(R.id.edit_button);
        updateButton = findViewById(R.id.update_button);

        //SharedPreferences sitelist = getSharedPreferences ("saved_sites", MODE_PRIVATE);

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
        addsavedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String offsite0 = webView.getUrl();
                checkPermission();
                boolean issitethere = sitelist.getBoolean (offsite0, false);
                if (issitethere){
                    Toast.makeText(MainActivity.this, "Website already on offline list",Toast.LENGTH_SHORT).show();
                    hideView(dialogBack);
                }else{

                    String offsite = webView.getUrl();
                    if (offsite!= null) {
                        sitelist.edit().putBoolean(offsite, true).apply();
                        Toast.makeText(MainActivity.this, "Added to offline sites. This website will be loaded in offline mode", Toast.LENGTH_SHORT).show();

                    }
                    hideView(dialogBack);
                }
            }
        });
        remsavedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String offsite1 = webView.getUrl();
                boolean issitethere1 = sitelist.getBoolean(offsite1, false);
                if (issitethere1 != false) {

                    if (offsite1 != null) {
                        sitelist.edit().putBoolean(offsite1, false).apply();
                        Toast.makeText(MainActivity.this, "Removed from offline sites. ", Toast.LENGTH_SHORT).show();

                    }
                    hideView(dialogBack);
                } else {
                    Toast.makeText(MainActivity.this, "Site wasn't in offline sites. ", Toast.LENGTH_SHORT).show();
                }
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
                    hideView(dialogBack);
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
        hab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(homepge);

            }
        });


        hab.setOnLongClickListener (new View.OnLongClickListener () {
            @Override
            public boolean onLongClick (View v) {
                String offsite0 = webView.getUrl();
                checkPermission();
                boolean issitethere = sitelist.getBoolean (offsite0, false);
                if (issitethere){
                    if (offsite0 != null) {
                        sitelist.edit().putBoolean(offsite0, false).apply();
                        Toast.makeText(MainActivity.this, "Removed from offline sites. ", Toast.LENGTH_SHORT).show();
                    }
                }else{

                    String offsite = webView.getUrl();
                    if (offsite!= null) {
                        sitelist.edit().putBoolean(offsite, true).apply();
                        Toast.makeText(MainActivity.this, "Added to offline sites. This website will be loaded in offline mode", Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
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
        fab =  findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pipmode = getSharedPreferences ("pip_mode", MODE_PRIVATE);
                SharedPreferences darkmod = getSharedPreferences ("myPref", MODE_PRIVATE);
                boolean darkm = darkmod.getBoolean ("darkmode", isdarkm);
                boolean pipm = pipmode.getBoolean ("pip", false);
                SharedPreferences sitelist0 = getSharedPreferences ("saved_sites", MODE_PRIVATE);

                boolean site1load = sitelist0.getBoolean (site1, true);
                boolean site2load = sitelist0.getBoolean (site2, true);
                boolean site3load = sitelist0.getBoolean (homePage, true);
                String offmode = "Enable/Disable";
                String enpip = "Enable/Disable";
                String endark = "Enable/Disable";
                if (site1load == false || site2load == false || site3load == false ){
                    offmode = "Enable";
                }else{
                    offmode = "Disable";
                }
                if (pipm == true){
                    enpip = "Disable";
                }else{
                    enpip = "Enable";
                }
                if (darkm == true){
                    endark = "Disable";
                }else{
                    endark = "Enable";
               }

                CharSequence[] items = {"Exit","Refresh Website", "Edit Homepage", offmode + " offline loading", enpip +" Background Play",endark +" Dark Mode","Check Update","Help","About", "Cancel"};

// create an alert dialog builder
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Settings");

// add the items to the dialog
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                finish();
                                break;
                            case 1:
                                webView.clearCache (true);
                                Toast.makeText(MainActivity.this, "Cleaned all saved website data. Reload websites to load the latest versions of sites", Toast.LENGTH_SHORT).show();

                                hideView(dialogBack);
                                // do something for button 4
                                break;

                            case 2:
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

                            case 3:
                                if (site1load == false || site2load == false || site3load == false ){
                                    Toast.makeText(MainActivity.this, "Enabled offline loading ", Toast.LENGTH_SHORT).show();

                                    sitelist0.edit().putBoolean(site1, true ).apply();


                                    sitelist0.edit().putBoolean(site2, true).apply();

                                    sitelist0.edit().putBoolean(homePage, true).apply();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Add more pages as Offline Pages")
                                            .setMessage("Hold Home Button Add/Remove any site to offline sites list")
                                            .setPositiveButton("Got it", (dialog1, which1) -> {})
                                            .show();
                                    hideView(dialogBack);
                                }
                                else{
                                    Toast.makeText(MainActivity.this, "Disabled offline loading ", Toast.LENGTH_SHORT).show();

                                    sitelist0.edit().putBoolean(site1,false).apply();
                                    sitelist0.edit().putBoolean(site2,false).apply();

                                    sitelist0.edit().putBoolean(homePage,false).apply();
                                    hideView(dialogBack);
                                }

                                hideView(dialogBack);
                                // do something for button 4
                                break;
                            case 4:

                                if (pipm == true){
                                    pipmode.edit().putBoolean("pip", false ).apply();
                                    Toast.makeText(MainActivity.this, "Disabled Background Play", Toast.LENGTH_SHORT).show();

                                }else{
                                    pipmode.edit().putBoolean("pip", true ).apply();
                                    Toast.makeText(MainActivity.this, "Enabled background play", Toast.LENGTH_SHORT).show();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("How to watch Picture in Picture mode")
                                            .setMessage("Exit app by pressing home button or guesture to while on fullscreen to watch picture in picture ")
                                            .setPositiveButton("OK", (dialog1, which1) -> {})
                                            .show();

                                }

                                hideView(dialogBack);
                                // do something for button 4
                                break;

                           case 5:
                                if (darkm == true){
                                    darkmod.edit().putBoolean("darkmode", false ).apply();
                                    Toast.makeText(MainActivity.this, "Disabled Darkmode", Toast.LENGTH_SHORT).show();

                                }else{
                                    darkmod.edit().putBoolean("darkmode", true ).apply();
                                    Toast.makeText(MainActivity.this, "Enabled Darkmode", Toast.LENGTH_SHORT).show();
                                }
                                hideView(dialogBack);
                                
                                break;
                            case 6:
                                webView.loadUrl(sourcecode);
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Download Update")
                                        .setMessage("Current app version is " + version + ".  Goto assets and download latest apk file and install it from your file manager app")
                                        .setPositiveButton("OK", (dialog1, which1) -> {})
                                        .show();

                                hideView(dialogBack);
                                // do something for button 3
                                break;

                            case 7:
                                webView.loadUrl(helppage);
                                hideView(dialogBack);
                                // do something for button 3
                                break;

                            case 8:
                                String webver = webView.getSettings().getUserAgentString();
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("About")
                                        .setMessage("DTVFree "+ version + " \n\nCurrent Homapage: "+ homepge +  " \n\nDeveloper: "+ sourcecode+  " \n\nCurrent Webview Version:" +  webver+ "\n\n Privacy Policy\n\nThis application does not collect or store personal data.")
                                        .setPositiveButton("Cancel", (dialog1, which1) -> {})
                                        .show();
                                hideView(dialogBack);
                                // do something for button 3
                                break;

                            case 9:
                                hideView(dialogBack);
                                // do something for button 4
                                break;


                        }

                    }
                });
                AlertDialog dialog1 = builder.create();
                dialog1.show();
            }

        });





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
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        //webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        if (darkmode){
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        //Set the force dark mode to on
            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
            }
        }

        // get a SharedPreferences object with the name “saved_sites”



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

        isError = false;
        lastSuccessUrl = homepge;
// Set a webview client to the webview
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Show the loading indicator when the webview starts loading
                loadingIndicator.setVisibility(View.VISIBLE);
                PackageManager pm = getPackageManager();

                // Check if the device is an Android TV
                boolean isTV = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
             //   if (!isTV) {
                    if (url.equals(homepge)) {
                        hab.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    } else {
                        //Otherwise, hide the FAB

                        fab.setVisibility(View.GONE);
                        hab.setVisibility(View.VISIBLE);
                    }
             //   }
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide the loading indicator when the webview finishes loading
                loadingIndicator.setVisibility(View.GONE);
                super.onPageFinished(view, url);
                searchBar.setHint(webView.getUrl());
                PackageManager pm = getPackageManager();
                // Check if the device is an Android TV
                boolean isTV = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);
                //Assuming you have a WebView object named webView
                webView.evaluateJavascript("document.getElementById('fullscreenButton').click();", null);


                // If the device is not an Android TV, hide the status bar and the navigation bar
              //  if (!isTV) {

                    if (url.equals(homepge)) {
                        hab.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    } else {
                        //Otherwise, hide the FAB
                        fab.setVisibility(View.GONE);
                        hab.setVisibility(View.VISIBLE);
                    }
               // }
            }

            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                // check if the URL is in the saved sites list
                boolean isSaved = sitelist.getBoolean(url, false);


                if (url.startsWith("tg://")) {

                    // Create an Intent with the ACTION_VIEW action and the URL as data
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    // Start the activity with the Intent
                    startActivity(intent);

                    // Return true to indicate that the URL loading is handled
                    return true;
                }


                // set the cache mode according to the URL's status in the list
                if (isSaved) {
                    // if the URL is in the list, set the cache mode to load from cache only
                    webSettings.setCacheMode (WebSettings.LOAD_CACHE_ELSE_NETWORK);
                } else {
                    // if the URL is not in the list, set the cache mode to load from network only
                    webSettings.setCacheMode (WebSettings.LOAD_DEFAULT);
                }

                // return false to let the WebView load the URL
                return false;
            }

            // Override the onReceivedError method
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Pass the error parameters to the error page
                view.loadUrl("file:///android_asset/error.html?errorCode=" + errorCode + "&description=" + description + "&failingUrl=" + failingUrl);
                isError = true;
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
        boolean loadsite1 = sitelist.getBoolean (site1, true);
        if (loadsite1 != false){
            sitelist.edit().putBoolean(site1, true).apply();
        }
        boolean loadsite2 = sitelist.getBoolean (site2, true);
        if (loadsite2 != false){
            sitelist.edit().putBoolean(site2, true).apply();
        }
        boolean loadsite3 = sitelist.getBoolean (homePage, true);
        if (loadsite3 != false){
            sitelist.edit().putBoolean(homePage, true).apply();
        }

        panelViews = new View[][]{{searchBar, homeButton}, {backButton,forwardButton,cursorButton,refreshButton,editButton ,updateButton, closeButton,addsavedButton,remsavedButton, clearButton}};
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
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        // Hide or show the WebView controls based on the PIP mode
        if (isInPictureInPictureMode) {
            // Hide the controls in PIP mode
            webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            // Show the controls in normal mode
            webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        SharedPreferences pipmode = getSharedPreferences ("pip_mode", MODE_PRIVATE);
        boolean pipm = pipmode.getBoolean ("pip", true);
        if (pipm == true){
            hab.setVisibility(View.GONE);
            // Enter PIP mode when the user leaves the app and the webview is showing a video in full screen mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && webClient.isVideoFullScreen()) {
                enterPictureInPictureMode(new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(16, 9)) // Set the aspect ratio for the PIP window
                        .build());
            }
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
                        column = 9;
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
                    if(column == 9){
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
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        PackageManager pm = getPackageManager();

        // Check if the device is an Android TV
        boolean isTV = pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK);

        // If the device is not an Android TV, hide the status bar and the navigation bar
        if (isTV) {
            switch (keyCode) {

                case KeyEvent.KEYCODE_BACK:
                    //Perform the different action
                    if (webClient.isFullScreen()) {
                        webClient.onHideCustomView();
                    } else {
                        // if (nocursor) {
                        //  cursorButton.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.cursor_background));

                        //}
                        dialogBack.setVisibility(View.VISIBLE);
                        panelViews[row][column].requestFocus();
                    }

                    return true;
                default:
                    return super.onKeyLongPress(keyCode, event);

            }
        }else{
            return super.onKeyLongPress(keyCode, event);
        }
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
                    // return super.dispatchKeyEvent(event);
                    if (keyCode != KeyEvent.KEYCODE_BACK){
                        return super.dispatchKeyEvent(event);
                        //dialogBack.setVisibility(View.VISIBLE);
                        //panelViews[row][column].requestFocus();

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
                            if (webClient.isFullScreen()) {
                            webClient.onHideCustomView();
                        } else {
                                
                            if (!webView.canGoBack()) {
                                //hideView(dialogBack);
                                // create an array of items to display
                                new AlertDialog.Builder(this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Closing Application")
                                        .setMessage("Are you sure you want to close this application?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // call the finish method to end the activity
                                                finish();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // cancel the dialog
                                                dialog.cancel();
                                                hideView(dialogBack);
                                            }})
                                        .show();
                                hideView(dialogBack);

                                break;
                            
                            } else {
                                if (isError) {
                                    // Set the error status to false
                                    isError = false;
                                    // Go back to the previous page in the WebView
                                    webView.loadUrl(lastSuccessUrl);
                                } else {
                                    // Otherwise, call the super method
                                    webView.goBack();
                                }
                                //webView.goBack();
                                break;
                            }
                        }
                        }

                        if( nocursor != true) {

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
                        else{
                            if (webClient.isFullScreen()) {
                                webClient.onHideCustomView();
                            } else {
                                Toast.makeText(MainActivity.this, "Long Press Back Button for menu", Toast.LENGTH_SHORT).show();
                                if (!webView.canGoBack()) {
                                    //hideView(dialogBack);
                                    // create an array of items to display
                                    new AlertDialog.Builder(this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Closing Application")
                                            .setMessage("Are you sure you want to close this application?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // call the finish method to end the activity
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // cancel the dialog
                                                    dialog.cancel();
                                                    hideView(dialogBack);
                                                }})
                                            .show();
                                    hideView(dialogBack);

                                    break;

                                } else {
                                    if (isError) {
                                        // Set the error status to false
                                        isError = false;
                                        // Go back to the previous page in the WebView
                                        webView.loadUrl(lastSuccessUrl);
                                    } else {
                                        // Otherwise, call the super method
                                        webView.goBack();

                                    }
                                    //webView.goBack();
                                    break;
                                }
                            }


                        }

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
