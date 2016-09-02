package com.busywww.myliveevent.LecShareDB;

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

    public CourseUpdate(int courseID,String courseName,String school,String semester, String lecturer)
    {
        mCourse = new Course(courseID,courseName,school,semester, lecturer);
        Courses = new ArrayList<>();
       // UploadCourseToSql uploadCourse = new UploadCourseToSql(mCourse);

    }

    //find last lesson Num and check if it is todays lesson
    public static int getLastLessonNumFromCourse(String CourseName)
    {

        int lastLessonNum = -1;
        int LessonNum = -1;
      //  java.sql.Date sqlDate;
        java.util.Date newDate;

        StringBuilder query = new StringBuilder();
        query.append("SELECT lessonNum,date ");
        query.append("FROM lesson ");
        query.append(String.format("WHERE course = '%s' and lessonNum = (SELECT MAX(lessonNum) FROM lesson);",CourseName));

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
            query = new StringBuilder("INSERT INTO courses (courseID, name, school,lecturer,semester) values (?,?,?,?,?)");
            stm = (PreparedStatement) dbConnection.prepareStatement(query.toString());
            for (int i = 0; i < courses.size(); i++) {
                stm.setNull(1, java.sql.Types.INTEGER);
                stm.setString(2, courses.get(i).getCourseName());
                stm.setString(3, courses.get(i).getSchool());
                stm.setString(4, courses.get(i).getLecturer());
                stm.setString(5, courses.get(i).getSemester());
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

     StringBuilder query = new StringBuilder("SELECT * FROM lecshare.course ");
     query.append(String.format("WHERE school = '%s';",school));
        ResultSet rst;
        rst = stm.executeQuery(query.toString());
       ArrayList<Course> courses = new ArrayList<>();
        while (rst.next()) {
            Course course = new Course(rst.getInt("courseID"), rst.getString("name"), rst.getString("school"), rst.getString("lecturer"),rst.getString("semester"));
            courses.add(course);
            int size = courses.size();
        }
         stm.close();
         return courses;
    }

    public static void setUserCourses()
    {
        UserInfoSingelton userData = UserInfoSingelton.getInstance();
        int id = userData.getUserID();
        StringBuilder query;
        PreparedStatement stm = null;

        try {
            dbConnection.setAutoCommit(false);
            query = new StringBuilder("INSERT INTO lecshare.usercourse (courseID, userID) values (?,?)");
            stm = (PreparedStatement) dbConnection.prepareStatement(query.toString());
            for (int i = 0; i < userData.getUserCourses().size(); i++) {
                stm.setInt(1,userData.getUserCourses().get(i).getCourseId());
                stm.setInt(2,id);
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


    }






