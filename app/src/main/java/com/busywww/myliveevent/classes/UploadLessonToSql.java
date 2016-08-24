package com.busywww.myliveevent.classes;

import android.os.AsyncTask;

import com.busywww.myliveevent.util.PdfPlayer;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Danielle on 8/24/2016.
 */
public class UploadLessonToSql extends AsyncTask<Void, Void, Void> {
    private String videoId;
    private ArrayList<PdfPlayer> pdfPlayerArray;
    private String courseName;
    private int lessonNum;
    @Override
    protected Void doInBackground(Void... params) {
        try {
            LessonUpload.uploadLesson(videoId,pdfPlayerArray,courseName,lessonNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UploadLessonToSql(String videoId,ArrayList<PdfPlayer> pdfPlayerArray,String courseName,int lessonNum) {
        super();
        this.videoId=videoId;
        this.pdfPlayerArray = pdfPlayerArray;
        this.courseName = courseName;
        this.lessonNum = lessonNum;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
