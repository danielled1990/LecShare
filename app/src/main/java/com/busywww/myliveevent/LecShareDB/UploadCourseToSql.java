package com.busywww.myliveevent.LecShareDB;

import android.os.AsyncTask;

import com.busywww.myliveevent.util.Constants;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Alona on 8/28/2016.
 */
public class UploadCourseToSql extends AsyncTask<Void, Void, ArrayList<Course>> {


    private String mSchool;
    ArrayList<Course> courses = new ArrayList<>();

    int mDoAction;

    @Override
    protected ArrayList<Course> doInBackground(Void... params) {
        switch (mDoAction) {
            case Constants.GET_COURSES:
                try {
                    courses = CourseUpdate.getAllCourses(mSchool);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.SET_COURSES:
                CourseUpdate.setCourses(UserInfoSingelton.getInstance().getCourses());
                break;
            case Constants.SET_COURSER_USER:
                CourseUpdate.setUserCourses(Constants.REGISTER_COURSES);
                break;
            case Constants.UPDATE_COURSES:
                CourseUpdate.setUserCourses(Constants.UPDATE_COURSES);
                break;
            case Constants.DELETE_USER_COURSES:
                CourseUpdate.DeleteUserCourses();
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
