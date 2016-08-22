package com.busywww.myliveevent.classes;

import android.annotation.TargetApi;
import android.os.Build;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
        String kaki;
        String kaki2;
        String kakiwitheggplant;
         int i,j;
        query.append("SELECT email, password ");
        query.append("FROM userinfo ");
        query.append(String.format("WHERE email = '%s';", email));

        try (Statement statement = dbConnection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query.toString());

            if(resultSet.next()){
                //If the credentials are correct, we'll return the id of the current user
                if(resultSet.getString("email").equals(email) &&
                        resultSet.getString("password").equals(password)){
              //      validatedUserID = resultSet.getInt("userid");
                    flag =  true;
                }

            }


            statement.close();
        }

        return flag;
    }
}
