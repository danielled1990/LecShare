package com.busywww.myliveevent.LecShareDB;

import com.busywww.myliveevent.util.Constants;
import com.mysql.jdbc.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Alona on 8/28/2016.
 */
public class CourseUpdate extends SqlConnection {

   private Course mCourse;

    public static ArrayList<Course> Courses;

    public CourseUpdate(int courseID,String courseName,String school,String semester, String lecturer,int year)
    {
        mCourse = new Course(courseID,courseName,school,semester, lecturer,year);
        Courses = new ArrayList<>();
       // UploadCourseToSql uploadCourse = new UploadCourseToSql(mCourse);

    }

    //find last lesson Num and check if it is todays lesson
    public static int getLastLessonNumFromCourse(String CourseName)
    {

        int lastLessonNum = 0;
        int LessonNum = 0;
      //  java.sql.Date sqlDate;
        java.util.Date newDate;

        StringBuilder query = new StringBuilder();
        query.append("SELECT lessonNum,date ");
        query.append("FROM lecshare.lesson ");
        query.append(String.format("WHERE lessonNum = (SELECT MAX(lessonNum) FROM lecShare1.lesson WHERE course = '%s'); ",CourseName));

        try{
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query.toString());

            if(resultSet.next()){

                lastLessonNum = resultSet.getInt("lessonNum");
                newDate= resultSet.getDate("date");

                Format formatter = new SimpleDateFormat("yyyy-MM-dd");
                String date = formatter.format(newDate);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String today = df.format(new Date());

               if(today.equals(date))
               {
                   LessonNum = lastLessonNum;
               }
                else
               {
                   LessonNum = lastLessonNum+1;
               }
            }

            statement.close();

        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return LessonNum;
    }


    public static void setCourses(ArrayList<Course> courses) {
        StringBuilder query;
        PreparedStatement stm = null;

        try {
            dbConnection.setAutoCommit(false);
            query = new StringBuilder("INSERT INTO courses (courseID, name, school,lecturer,semester,year) values (?,?,?,?,?,?)");
            stm = (PreparedStatement) dbConnection.prepareStatement(query.toString());
            for (int i = 0; i < courses.size(); i++) {
                stm.setNull(1, java.sql.Types.INTEGER);
                stm.setString(2, courses.get(i).getCourseName());
                stm.setString(3, courses.get(i).getSchool());
                stm.setString(4, courses.get(i).getLecturer());
                stm.setString(5, courses.get(i).getSemester());
                stm.setInt(6,courses.get(i).getYear());
                stm.addBatch();

            }
              stm.executeBatch();
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
        }
    }



 public static ArrayList<Course> getAllCourses(String school) throws ClassNotFoundException, SQLException {

     Statement  stm = dbConnection.createStatement();

     StringBuilder query = new StringBuilder("SELECT * FROM lecShare1.course ");
     query.append(String.format("WHERE school = '%s';",school));
        ResultSet rst;
        rst = stm.executeQuery(query.toString());
       ArrayList<Course> courses = new ArrayList<>();
        while (rst.next()) {
            Course course = new Course(rst.getInt("courseID"), rst.getString("name"), rst.getString("school"), rst.getString("lecturer"),
                    rst.getString("semester"),rst.getInt("year"));
            courses.add(course);

        }
         stm.close();
         return courses;
    }

    public static void setUserCourses(int coursesType)
    {
        int i;
        UserInfoSingelton userData = UserInfoSingelton.getInstance();
        int id = userData.getUserID();
        StringBuilder query;
        PreparedStatement stm = null;
        ArrayList<Course> courses = new ArrayList<>();
        int size = 0;

        switch (coursesType)
        {
            case Constants.REGISTER_COURSES:
                courses = userData.getUserCourses();
                size = userData.getUserCourses().size();
                break;
            case Constants.UPDATE_COURSES:
                courses = userData.getCoursesToUpdate();
                size = userData.getCoursesToUpdate().size();
        }

        try {
            dbConnection.setAutoCommit(false);
            query = new StringBuilder("INSERT INTO lecShare1.usercourse (userID,courseID) values (?,?)");
            stm = (PreparedStatement) dbConnection.prepareStatement(query.toString());
            for ( i = 0; i < size; i++) {
                stm.setInt(1,id);
                stm.setInt(2,courses.get(i).getCourseId());
                stm.addBatch();

            }
            stm.executeBatch();
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
        }

        if(coursesType == Constants.UPDATE_COURSES)
        {
            userData.AddNewCourses();
        }
}


    public static void DeleteUserCourses()
    {
        StringBuilder query;
        PreparedStatement stm = null;
        UserInfoSingelton userData = UserInfoSingelton.getInstance();
        ArrayList<Course> deleteCourses = userData.getCoursesToDelete();
        int userID = userData.getUserID();

        try {
            dbConnection.setAutoCommit(false);
            query = new StringBuilder("DELETE FROM lecShare1.usercourse ");
            query.append("WHERE userID = ? and courseID = ?; ");

            stm = ((PreparedStatement) dbConnection.prepareStatement(query.toString()));
            for (int i = 0; i < deleteCourses.size(); i++) {
                stm.setInt(1,userID);
                stm.setInt(2,deleteCourses.get(i).getCourseId());
                stm.addBatch();

            }
            stm.executeBatch();
            dbConnection.commit();
            stm.close();

        } catch(SQLException ex){
            if(dbConnection != null){
                try{
                    dbConnection.rollback();
                }catch (SQLException e)
                {
                    e.printStackTrace();
                }

            }
        }
        finally{
            try{
                dbConnection.setAutoCommit(true);
            }catch (SQLException e)
            {
                e.printStackTrace();
            }

            userData.DeleteCourses();

        }






    }


    }






