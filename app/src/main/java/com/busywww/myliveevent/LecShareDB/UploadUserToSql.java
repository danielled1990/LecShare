package com.busywww.myliveevent.LecShareDB;

import android.os.AsyncTask;

/**
 * Created by Alona on 8/28/2016.
 */

public class UploadUserToSql extends AsyncTask<Void, Void, Void> {

    String name;
    String email;
    String password;
    String school;

    @Override
    protected Void doInBackground(Void... params) {
        User.InsertNewUserToDB(name,email,password,school);
        return null;
    }

    public UploadUserToSql(String name,String email,String password,String school) {
        super();
        this.name=name;
        this.email = email;
        this.password = password;
        this.school = school;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}