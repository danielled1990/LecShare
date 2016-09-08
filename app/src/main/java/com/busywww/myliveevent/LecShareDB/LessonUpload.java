package com.busywww.myliveevent.LecShareDB;

import com.busywww.myliveevent.util.LessonSingelton;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by Danielle on 8/24/2016.
 */
public class LessonUpload extends SqlConnection {

    public static boolean uploadLesson(String videoId, String courseName) throws SQLException {

        StringBuilder query;
        int lessonID = -1;
        int LessonNUM = -1;


        LessonNUM = CourseUpdate.getLastLessonNumFromCourse(courseName);

        LessonSingelton.getInstanceSingelton().setLessonNum(LessonNUM);

        try{
            dbConnection.setAutoCommit(false);
            Gson  gson = new Gson();
          //  JSONObject pdfPlayerJson = new JSONObject();
          //  jsonPut(pdfPlayerJson,"pdfPlayer",pdfPlayerArray);

          //  ArrayList<pdf1> pdfA =  new ArrayList<>();
           // for(int i = 0; i< LessonSingelton.getInstanceSingelton().getArray().size(); i++){
              //  LessonSingelton.getInstanceSingelton().getArray().get(i).setImageLink(LessonSingelton.getInstanceSingelton().getPdfImageLinks().get(i));
           // }


            //   String res = gson.toJson(LessonSingelton.getInstanceSingelton().getArray());
                String res = gson.toJson(LessonSingelton.getInstanceSingelton().getPdfPlayer());


            String lessonPhotonjson = gson.toJson(LessonSingelton.getInstanceSingelton().getImageinks());
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            PreparedStatement pre;
            pre = dbConnection.prepareStatement("INSERT INTO lecShare1.lesson (idLesson, lessonNum,date,course,videoId,pdfPlayer,lessonPhotos)  values (?,?,?,?,?,?,?)");
            pre.setString(1,null);
            pre.setInt(2, LessonNUM);
            pre.setDate(3, sqlDate);
            pre.setString(4,courseName);
            pre.setString(5, videoId);
            pre.setString(6,res);
            if(lessonPhotonjson!= null){
                pre.setString(7,lessonPhotonjson);
            }
            else{
                pre.setString(7,null);
            }
            //   pre.setBinaryStream(4, fis, (int) picfile.length());
            int count = pre.executeUpdate();
            LessonSingelton.getInstanceSingelton().cleararrays();
            dbConnection.commit();

            query = new StringBuilder("SELECT idLesson ");
            query.append("FROM lecShare1.lesson ");
            query.append(String.format("WHERE videoId = '%s';", videoId));
            Statement selectIDStatement = dbConnection.createStatement();

            ResultSet resultSet = selectIDStatement.executeQuery(query.toString());
            if (resultSet.next()) {
                lessonID = resultSet.getInt("idLesson");
                LessonSingelton.getInstanceSingelton().setmLessonID(lessonID);
            }
        }
        catch(SQLException ex){
            if(dbConnection != null){
                dbConnection.rollback();
            }
        }

        if(lessonID>0)
        {
            updateCourseLesson(lessonID);
        }

        return true;

    }



    public static void updateCourseLesson(int lessonID)
    {
        int courseID = LessonSingelton.getInstanceSingelton().getLessonCourse().getCourseId();
        StringBuilder query;
        com.mysql.jdbc.PreparedStatement stm = null;

        try {
            dbConnection.setAutoCommit(false);
            query = new StringBuilder("INSERT INTO lecShare1.course_lesson (lessonID, courseID) values (?,?)");
            stm = (com.mysql.jdbc.PreparedStatement) dbConnection.prepareStatement(query.toString());
            stm.setInt(1,lessonID);
            stm.setInt(2,courseID);
            stm.executeUpdate();

            dbConnection.commit();
            stm.close();

        } catch (SQLException e) {
            if(dbConnection != null)
            {
                try {
                    dbConnection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        } finally{

            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public static class pdf1{
        public  int mHours;
        public  int mMinutes;
        public  int mSeconds;
        public  int mPage;
        public String link;
    }
}
