package com.busywww.myliveevent.LecShareDB;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Created by coral on 21/08/2016.
 */
public abstract class SqlConnection {
    private static final String SQL_DRIVER = "com.mysql.jdbc.Driver";

    public static Connection dbConnection = null;
    public static String s = "";
    public static void init(){}

    static {
        try{
            Class.forName(SQL_DRIVER).newInstance();
            dbConnection = DriverManager.getConnection("jdbc:mysql://lechsharetest.cvu9lx1ig8hh.us-west-2.rds.amazonaws.com:3306/lecShare1","DAC140916","DAC140916");
            //        dbConnection = DriverManager.getConnection(SERVER_URL);
            s="ok";

        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
            s="not ok";
        }
    }

}
