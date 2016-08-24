package com.busywww.myliveevent;

import android.app.Application;
import android.content.Context;

/**
 * Created by Alona on 8/22/2016.
 */
public class LecShareContext extends Application {

    private static Context context;
    private static final String TAG = LecShareContext.class.getSimpleName();

    public void onCreate(){
        super.onCreate();
        LecShareContext.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return LecShareContext.context;
    }

}
