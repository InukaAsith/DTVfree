package com.ubetta.dtvfree;
import android.Manifest;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

//android:theme="@style/Theme.VitaBrowser"
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
    private ImageButton homeButton,forwardButton,backButton,refreshButton, editButton, closeButton;
    private View[][] panelViews ;
    private View focusTemp;



    private String homePage = "https://dtv.up.railway.app";
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
        String homepge = sharedPref.getString("homepage", "https://dtv.up.railway.app");


        boolean isFirstTime = sharedPref.getBoolean("isFirstTime", true);

// if yes, show the popup message

        if (isFirstTime) {
            // create an AlertDialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set a Custom homepage");

            // create an EditText for the user to input data
            final EditText input = new EditText(this);

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
                        Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
                    }
                    else if (data.toLowerCase().indexOf(webqr.toLowerCase()) != -1) {
                        String homestr = ("https://" + data);
                        SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                        editor.putString("homepage", homestr);
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
// apply the changes

                    }
                    else {
                        String homestr = ("https://google.com/search?q=" + data);
                        SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                        editor.putString("homepage", homestr);

// apply the changes
                        editor.apply();
                        Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
                    }
                    //sharedPref.edit().putString("homepage", data).apply();

                    // set the flag to false so the popup will not show again
                    sharedPref.edit().putBoolean("isFirstTime", false).apply();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // cancel the dialog
                    dialog.cancel();
                }
            });

            // show the dialog
            builder.show();
        }

        homeButton = findViewById(R.id.home_button);
        closeButton = findViewById(R.id.close_button);
        editButton = findViewById(R.id.edit_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(homepge);
                hideView(dialogBack);
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                input.setHint("Enter a value");

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
                            String homestr = ("https://google.com/search?q=" + value);
                            SharedPreferences.Editor editor = sharedPref.edit();
// edit the value of myString
                            editor.putString("homepage", homestr);

// apply the changes
                            editor.apply();
                            Toast.makeText(MainActivity.this, "Homepage set to " + homestr + "Please restart application in order for changes to take effect", Toast.LENGTH_SHORT).show();
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
        // enable JavaScript
        webSettings.setJavaScriptEnabled(true);
        // enable web storage
        webSettings.setDomStorageEnabled(true);
        webView.loadUrl(homepge);
        webView.getSettings().setJavaScriptEnabled(true);
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
        });

        webView.getSettings().setSupportMultipleWindows(false);
        panelViews = new View[][]{{searchBar, homeButton}, {backButton,forwardButton,refreshButton,editButton ,closeButton}};
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
                        column = 4;
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
                    if(column == 4){
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
        }
        if(row == 0 ){
            if(column == 0) {
                panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.search_bar_focus_background));
            }else{
                panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.voice_button_focus_background));
            }
        }else {
            panelViews[row][column].setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.button_focus_background));
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
        keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_BACK || keyCode ==  KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

            if (dialogBack.getVisibility() == View.VISIBLE && event.getAction() != KeyEvent.ACTION_UP) {
                dialogEvent(keyCode);
            } else {//Dialog not visible
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
                        if (webClient.isFullScreen()) {
                            return super.dispatchKeyEvent(event);
                        }
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
                                        .setNegativeButton("No", null)
                                        .show();
                                hideView(dialogBack);

                            } else {
                                webView.goBack();
                                break;
                            }
                        }
                        if (webClient.isFullScreen()) {
                            webClient.onHideCustomView();
                        } else {
                            dialogBack.setVisibility(View.VISIBLE);
                            panelViews[row][column].requestFocus();
                        }
                        break;
                    default:
                        return super.dispatchKeyEvent(event);
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
