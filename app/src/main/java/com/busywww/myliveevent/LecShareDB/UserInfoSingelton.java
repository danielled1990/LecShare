package com.busywww.myliveevent.LecShareDB;

import java.util.ArrayList;

/**
 * Created by Alona on 8/28/2016.
 */
public class UserInfoSingelton {

    private static UserInfoSingelton mInstance = null;

    private String mUserName;
    private String mPassword;
    private String mEmail;
    private String mSchool;
    private int mUserID;

    private ArrayList<Course> mCourses;
    private ArrayList<Course> mUserCourses;
    private ArrayList<Course> mUpdateCourses;
    private ArrayList<Course> mDeleteCourses;

    private UserInfoSingelton(){
        mCourses = new ArrayList<>();
        mUserCourses = new ArrayList<>();
        mUpdateCourses = new ArrayList<>();
        mDeleteCourses = new ArrayList<>();
    }

    public static UserInfoSingelton getInstance(){
        if(mInstance == null)
        {
            mInstance = new UserInfoSingelton();
        }
        return mInstance;
    }

    public String getUserName()
    {
        return mUserName;
    }
    public void setUserName(String name)
    {
        mUserName = name;
    }
    public String getUserPassword()
    {
        return mPassword;
    }
    public void setUserPassword( String password)
    {
        mPassword = password;
    }

    public String getUserEmail()
    {
        return mEmail;
    }
    public void setUserEmail( String email)
    {
        mEmail = email;
    }

    public String getUserSchool()
    {
        return mSchool;
    }
    public void setUserSchool( String school)
    {
        mSchool = school;
    }

    public void AddCourse(Course course)
    {
        mCourses.add(course);
    }

    public  void AddUserCourse(Course course){mUserCourses.add(course);}
    public ArrayList<Course> getUserCourses()
    {
        return mUserCourses;
    }


    public ArrayList<Course> getCourses()
    {
        return mCourses;
    }

    public int getUserID()
    {
        return mUserID;
    }

    public void setUserID(int id)
    {
        mUserID = id;
    }


    public void setCoursesToDelete(ArrayList<Course> deleteCourses)
    {
        mDeleteCourses = deleteCourses;
    }

    public ArrayList<Course> getCoursesToDelete()
    {
        return mDeleteCourses;
    }

    public void setNewCourses(ArrayList<Course> courses)
    {
        mUpdateCourses = courses;
    }

    public ArrayList<Course> getCoursesToUpdate()
    {
        return mUpdateCourses;
    }

    public void AddNewCourses()
    {
        int i;
        for(i = 0 ;i< mUpdateCourses.size();i++)
        {
            mUserCourses.add(mUpdateCourses.get(i));
        }
        mUpdateCourses.clear();

    }

    public void DeleteCourses()
    {
        int i;
        for(i = 0 ;i< mDeleteCourses.size();i++)
        {
            mUserCourses.remove(mDeleteCourses.get(i));
        }

        mUpdateCourses.clear();

    }

    public void AddCourseToUpdate(Course course)
    {
        mUpdateCourses.add(course);
    }

    public void AddCourseToDelete(Course course)
    {
        mDeleteCourses.add(course);
    }








}