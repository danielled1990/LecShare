package com.busywww.myliveevent.LecShareDB;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import com.mysql.jdbc.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * Created by coral on 21/08/2016.
 */
public class User extends SqlConnection {
    @TargetApi(Build.VERSION_CODES.KITKAT)


    public static boolean validateCredentials(String email, String password) throws SQLException {
        Integer validatedUserID = null;
        boolean flag = false;
        //We'll use a query to get the infromation we want to initialize a user with
        StringBuilder query = new StringBuilder();
        int i, j;
        query.append("SELECT email, password ");
        query.append("FROM userinfo ");
        query.append(String.format("WHERE email = '%s';", email));

        try (Statement statement = dbConnection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query.toString());

            if (resultSet.next()) {
                //If the credentials are correct, we'll return the id of the current user
                if (resultSet.getString("email").equals(email) &&
                        resultSet.getString("password").equals(password)) {
                    //      validatedUserID = resultSet.getInt("userid");
                    flag = true;
                }
            }

            statement.close();
        }

        return flag;
    }


    public static void getUserCoursesFromDB(int userID)
    {
        Statement  stm = null;
        UserInfoSingelton userData = UserInfoSingelton.getInstance();
        try {
           stm = dbConnection.createStatement();

        StringBuilder query = new StringBuilder();
        query.append("SELECT course.courseID,course.name,course.school,course.lecturer,course.semester,course.year ") ;
        query.append("FROM course JOIN usercourse ");
        query.append("ON course.courseID = usercourse.courseID ");
        query.append(String.format("WHERE usercourse.userID = '%d';", userID));

        ResultSet rst;
        rst = stm.executeQuery(query.toString());

        while (rst.next()) {
            Course course = new Course(rst.getInt("courseID"), rst.getString("name"), rst.getString("school"), rst.getString("lecturer"),rst.getString("semester"),rst.getInt("year"));
            userData.AddUserCourse(course);
            int size = userData.getUserCourses().size();
        }

        stm.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //when user is loggedIn
    public static void GetUserInfo(String email, String password) {

        UserInfoSingelton userInfo = UserInfoSingelton.getInstance();

       int userID =-1;

        StringBuilder query = new StringBuilder();

        query.append("SELECT name,email,password,school,userID ");
        query.append("FROM lecShare1.userinfo ");
        query.append(String.format("WHERE lecShare1.userinfo.email = '%s' and lecShare1.userinfo.password = '%s' ;", email,password));


        try {
            Statement statement = dbConnection.createStatement();
            ResultSet resultSet = statement.executeQuery(query.toString());


            if (resultSet.next()) {
                //Each singular result we iterate through is a row in the table
                userID = resultSet.getInt("userID");
                userInfo.setUserID(userID);
                userInfo.setUserName(resultSet.getString("name"));
                userInfo.setUserPassword(resultSet.getString("password"));
                userInfo.setUserEmail(resultSet.getString("email"));
                userInfo.setUserSchool(resultSet.getString("school"));

            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(userID>0)
        {
            getUserCoursesFromDB(userID);
        }


    }

    //set new user to db
    public static void InsertNewUserToDB(String name, String email, String password, String school) {
        StringBuilder query;


        try {
            dbConnection.setAutoCommit(false);
            //The insert new user query, it returns the ID of the added user.
            query = new StringBuilder("INSERT INTO userinfo (name, email, password, school,userID) values (?,?,?,?,?)");
            PreparedStatement insertUserStatement = (PreparedStatement) dbConnection.prepareStatement(query.toString());
            insertUserStatement.setString(1, name);
            insertUserStatement.setString(2, email);
            insertUserStatement.setString(3, password);
            insertUserStatement.setString(4, school);
            insertUserStatement.setNull(5, java.sql.Types.INTEGER);
            insertUserStatement.executeUpdate();
            dbConnection.commit();


            query = new StringBuilder("SELECT userID ");
            query.append("FROM lecShare1.userinfo ");
            query.append(String.format("WHERE email = '%s';", email));
            Statement selectIDStatement = dbConnection.createStatement();

            ResultSet resultSet = selectIDStatement.executeQuery(query.toString());
            if (resultSet.next()) {
                UserInfoSingelton.getInstance().setUserID(resultSet.getInt("userID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally{

            try {
                dbConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

    public static void SetUserInfoToDB(String name,String email,String pass,String school) {

        UserInfoSingelton userInfo = UserInfoSingelton.getInstance();
        userInfo.setUserName(name);
        userInfo.setUserEmail(email);
        userInfo.setUserPassword(pass);
        userInfo.setUserSchool(school);

        UploadUserToSql uploadUser = new UploadUserToSql(name,email,pass,school);
        uploadUser.execute();

    }




}
