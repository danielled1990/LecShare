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

    public Course(int courseID,String courseName,String school, String lecturer,String semester)
    {
        this.mCourseId = courseID;
        this.mCourseName = courseName;
        this.mSchool = school;
        this.mLecturer = lecturer;
        this.mSemester = semester;
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
