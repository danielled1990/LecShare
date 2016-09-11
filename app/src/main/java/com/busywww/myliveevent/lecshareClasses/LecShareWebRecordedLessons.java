package com.busywww.myliveevent.lecshareClasses;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.busywww.myliveevent.R;

/**
 * Created by Alona on 9/8/2016.
 */
public class LecShareWebRecordedLessons  extends Activity {

    private WebView webViewLecShareRecordedLessons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lecshare_web_view);
        webViewLecShareRecordedLessons = (WebView) findViewById(R.id.webLecShare);
        webViewLecShareRecordedLessons.getSettings().setJavaScriptEnabled(true);
        webViewLecShareRecordedLessons.loadUrl("http://lecshare.us-west-2.elasticbeanstalk.com");






    }

}
