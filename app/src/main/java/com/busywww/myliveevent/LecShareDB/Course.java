package com.busywww.myliveevent.LecShareDB;

/**
 * Created by Alona on 8/29/2016.
 */
public class Course {

    private String mLecturer;
    private String mSemester;
    private String mCourseName;
    private int mCourseId;
    private String mSchool;
    private int mYear;

    public Course(int courseID,String courseName,String school, String lecturer,String semester,int year)
    {
        this.mCourseId = courseID;
        this.mCourseName = courseName;
        this.mSchool = school;
        this.mLecturer = lecturer;
        this.mSemester = semester;
        this.mYear = year;
    }

    public int getCourseId()
    {
        return mCourseId;
    }
    public void setCourseId(int id)
    {
        mCourseId = id;
    }
    public String getCourseName()
    {
        return mCourseName;
    }
    public int getYear(){return mYear;}

    public String getSchool()
    {
        return mSchool;
    }
    public String getLecturer()
    {
        return mLecturer;
    }
    public String getSemester()
    {
        return mSemester;
    }
}
