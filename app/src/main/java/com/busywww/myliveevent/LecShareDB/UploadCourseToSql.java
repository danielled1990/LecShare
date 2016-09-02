package com.busywww.myliveevent.LecShareDB;

import android.os.AsyncTask;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Alona on 8/28/2016.
 */
public class UploadCourseToSql extends AsyncTask<Void, Void, ArrayList<Course>> {

    private static final int SET_COURSES = 1;
    private static final int GET_COURSES = 0;
    private static final int SET_COURSER_USER = 2;
    private String mSchool;
    ArrayList<Course> courses = new ArrayList<>();

    int mDoAction;

    @Override
    protected ArrayList<Course> doInBackground(Void... params) {
        switch (mDoAction) {
            case GET_COURSES:
                try {
                    courses = CourseUpdate.getAllCourses(mSchool);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;

            case SET_COURSES:
                CourseUpdate.setCourses(UserInfoSingelton.getInstance().getCourses());
                break;
            case SET_COURSER_USER:
                CourseUpdate.setUserCourses();
                break;

        }

        return courses;
    }

    public UploadCourseToSql(int DoAction,String school){//String name,String school,String lecturer ,String semester) {
        super();
        mDoAction = DoAction;
        mSchool = school;

        }


    @Override
    protected void onPostExecute(ArrayList<Course> aVoid){
        super.onPostExecute(aVoid);

    }
}
