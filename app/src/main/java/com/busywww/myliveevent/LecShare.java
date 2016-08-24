package com.busywww.myliveevent;

import android.app.Application;
import android.content.Context;

/**
 * Created by Alona on 8/22/2016.
 */
public class LecShare extends Application {

    private static Context context;
    private static final String TAG = LecShare.class.getSimpleName();

    public void onCreate(){
        super.onCreate();
        LecShare.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return LecShare.context;
    }

}
