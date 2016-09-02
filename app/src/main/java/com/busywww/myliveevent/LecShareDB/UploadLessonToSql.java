package com.busywww.myliveevent.LecShareDB;

import android.os.AsyncTask;

import com.busywww.myliveevent.LecShareDB.LessonUpload;

import java.sql.SQLException;

/**
 * Created by Danielle on 8/24/2016.
 */
public class UploadLessonToSql extends AsyncTask<Void, Void, Void> {
    private String videoId;
    private String courseName;

    @Override
    protected Void doInBackground(Void... params) {
        try {
            LessonUpload.uploadLesson(videoId,courseName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UploadLessonToSql(String videoId, String courseName) {
        super();
        this.videoId=videoId;
        this.courseName = courseName;


    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
