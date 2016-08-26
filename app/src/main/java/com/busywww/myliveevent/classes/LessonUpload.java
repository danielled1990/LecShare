package com.busywww.myliveevent.classes;

import com.busywww.myliveevent.util.PdfPlayer;
import com.busywww.myliveevent.util.PdfView;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Danielle on 8/24/2016.
 */
public class LessonUpload extends SqlConnection{

    public static boolean uploadLesson(String videoId, ArrayList<PdfPlayer> pdfPlayerArray,String courseName,int lessonNum,ArrayList<String> lessonPhotos) throws SQLException {
        try{
            Gson  gson = new Gson();
          //  JSONObject pdfPlayerJson = new JSONObject();
          //  jsonPut(pdfPlayerJson,"pdfPlayer",pdfPlayerArray);
            String res = gson.toJson(pdfPlayerArray);
       //     SimpleDateFormat fmt =  new SimpleDateFormat("yyyy-MM-dd");
       //     String date = fmt.format(new Date());
        //    SimpleDateFormat curFormater = new SimpleDateFormat("dd/MM/yyyy");
             String lessonPhotonjson = gson.toJson(lessonPhotos);
            Date dateObj = new Date();
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
          // java.sql.Date dateDB = new java.sql.Date(dateObj);
            PreparedStatement pre;
            pre = dbConnection.prepareStatement("INSERT INTO Lesson (idLesson, lessonNum,date,course,videoId,pdfPlayer,lessonPhotos)  values (?,?,?,?,?,?,?)");
            pre.setString(1,null);
            pre.setInt(2, lessonNum);
            pre.setDate(3, sqlDate);
            pre.setString(4,courseName);
            pre.setString(5, videoId);
            pre.setString(6,res);
            if(lessonPhotos!= null){
                pre.setString(7,lessonPhotonjson);
            }
            else{
                pre.setString(7,null);
            }
            //   pre.setBinaryStream(4, fis, (int) picfile.length());
            int count = pre.executeUpdate();
            dbConnection.commit();
        }
        catch(SQLException ex){
            if(dbConnection != null){
                dbConnection.rollback();
            }
        }

        return true;

    }
    private static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
