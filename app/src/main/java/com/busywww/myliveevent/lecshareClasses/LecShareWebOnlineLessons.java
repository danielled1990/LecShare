package com.busywww.myliveevent.lecshareClasses;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.busywww.myliveevent.LecShareDB.UserInfoSingelton;
import com.busywww.myliveevent.R;

/**
 * Created by Alona on 9/8/2016.
 */
public class LecShareWebOnlineLessons extends Activity {

    private WebView webViewLecShareOnline;
    String email = UserInfoSingelton.getInstance().getUserEmail();
    String password = UserInfoSingelton.getInstance().getUserPassword();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecshare_web_view);
        webViewLecShareOnline = (WebView) findViewById(R.id.webLecShare);
        webViewLecShareOnline.getSettings().setJavaScriptEnabled(true);
        webViewLecShareOnline.getSettings().setDomStorageEnabled(true);
        //webViewLecShareOnline.loadUrl("http://lecshare.us-west-2.elasticbeanstalk.com");
        webViewLecShareOnline.loadUrl("javascript: {" +
                "document.getElementById('Email').value = '"+email +"';" +
                "document.getElementById('Password').value = '"+password+"';" +
                "var frms = document.getElementsByName('loginForm');" +
                "frms[0].submit(); };");




        webViewLecShareOnline.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                v.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView v, String url) {
                v.loadUrl("javascript:document.getElementById('mib').value = 'aaa';");
            }
        });
        setContentView(webViewLecShareOnline);



    }
}
