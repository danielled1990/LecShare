package com.busywww.myliveevent.classes;



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
            dbConnection = DriverManager.getConnection("jdbc:mysql://192.168.1.15:3306/LecShare","root","DAnielle136");
            //        dbConnection = DriverManager.getConnection(SERVER_URL);
            s="ok";

        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e){
            e.printStackTrace();
            s="not ok";
        }
    }

}
