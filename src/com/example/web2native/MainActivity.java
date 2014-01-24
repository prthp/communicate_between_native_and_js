package com.example.web2native;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements	OnClickListener {

	/** Class to handle javascript call native function. */
	public class WebAppInterface {
		public final static String INTERFACE_NAME = "Android";
		
	    private Context mContext;
	    
	    /** Instantiate the interface and set the context */
	    public WebAppInterface(Context c) {
	        this.mContext = c;
	    }

	    /** Show a toast from the web page */
	    @JavascriptInterface
	    public void showToast(String toast) {
	        Toast.makeText(this.mContext, toast, Toast.LENGTH_SHORT).show();
	    }

	    @JavascriptInterface
	    public void onComplete(boolean success, String msg) {
	    	if (success) {
	    		Toast.makeText(this.mContext, "Oh Success!" + msg, Toast.LENGTH_SHORT).show();
	    	} else {
	    		Toast.makeText(this.mContext, "Oops, Failure!" + msg, Toast.LENGTH_SHORT).show();
	    	}
	    }
	    
	    @JavascriptInterface
	    public void onError(String msg) {
	    	Toast.makeText(this.mContext, msg, Toast.LENGTH_SHORT).show();
	    }
	    
	}
	
	private WebViewClient webviewClient = new WebViewClient() {
		@Override
		public void onPageFinished(WebView view, String url) {
		    super.onPageFinished(view, url);
		    view.clearCache(true);
		}
	};
	
	private String html = null;
	
	// UI Components
	private EditText input = null;
	private Button button1 = null;
	
	private FrameLayout webViewContainer = null;
	private WebView webView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		this.initWebView();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		this.destroyWebView();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
	        this.webView.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
	
	private void init() {
		this.html = getFromAssets();  
		this.bindUIComponents();
	}
	
	private void bindUIComponents() {
		this.webViewContainer = (FrameLayout)this.findViewById(R.id.webview_container);
		this.input = (EditText)this.findViewById(android.R.id.input);
		this.button1 = (Button)this.findViewById(android.R.id.button1);
		this.button1.setOnClickListener(this);
	}
	
	private void initWebView() {
		this.webView = new WebView(this);    
    	this.webView.addJavascriptInterface(new WebAppInterface(this), WebAppInterface.INTERFACE_NAME);        
        this.webView.setBackgroundColor(Color.TRANSPARENT);
        this.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);                     
        this.webView.setWebViewClient(this.webviewClient);
        
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(false);
		settings.setAppCacheEnabled(false);
		settings.setSupportZoom(false);		 
		settings.setDefaultTextEncodingName("utf-8");

        this.webView.loadDataWithBaseURL(null, this.html, "text/html", "utf-8", null);       
		
		// Add WebView into FrameLayout
		this.webViewContainer.addView(this.webView);
	}
	
	private void destroyWebView() {
		if(this.webViewContainer != null){
			this.webViewContainer.removeAllViews();		
			if(this.webView != null){
		        this.webView.destroy();
		        this.webView = null;
	    	}
		}
		System.gc();
	}
	
    public String getFromAssets() {  
        try {  
            InputStreamReader inputReader = new InputStreamReader(getResources().openRawResource(R.raw.testing));  
            BufferedReader bufReader = new BufferedReader(inputReader);  
            String line = "";  
            String Result = "";  
            while ((line = bufReader.readLine()) != null) {
                Result += line;  
            }
            return Result;  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return "";  
    }  
	
	@Override
	public void onClick(View v) {
		if (v.equals(this.button1)) {
			this.updateWebContent(this.input.getText().toString());
		}
	}
	
	private void updateWebContent(String content) {
		this.webView.loadUrl("javascript:updateContent('" + content + "');");
	}
	
}
