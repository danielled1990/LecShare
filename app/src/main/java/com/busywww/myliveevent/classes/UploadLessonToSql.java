package com.busywww.myliveevent.classes;

import android.os.AsyncTask;

import com.busywww.myliveevent.util.PdfPlayer;
import com.busywww.myliveevent.util.pdfPlayerSingelton;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Danielle on 8/24/2016.
 */
public class UploadLessonToSql extends AsyncTask<Void, Void, Void> {
    private String videoId;
    private ArrayList<pdfPlayerSingelton> pdfPlayerArray;
    private String courseName;
    private int lessonNum;
    private ArrayList<String> lessonphotos;
    @Override
    protected Void doInBackground(Void... params) {
        try {
            LessonUpload.uploadLesson(videoId,pdfPlayerArray,courseName,lessonNum,lessonphotos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UploadLessonToSql(String videoId, ArrayList<pdfPlayerSingelton> pdfPlayerArray, String courseName, int lessonNum, ArrayList<String> lessonphotos) {
        super();
        this.videoId=videoId;
        this.pdfPlayerArray = pdfPlayerArray;
        this.courseName = courseName;
        this.lessonNum = lessonNum;
        this.lessonphotos = lessonphotos;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
