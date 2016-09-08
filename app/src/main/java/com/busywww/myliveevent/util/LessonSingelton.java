package com.busywww.myliveevent.util;

import com.busywww.myliveevent.LecShareDB.Course;

import java.util.ArrayList;

/**
 * Created by Danielle on 8/26/2016.
 */
public class LessonSingelton {
    private  int mHours;
    private  int mMinutes;
    private  int mSeconds;
    private  int mPage;

    private Course mCourse;
    private int mLessonID;
    private int mLessonNum;

   // private  ArrayList<LessonSingelton> array = null;


    private ArrayList<PdfPlayer> pdfPlayer = new ArrayList<>();

    private static int index =-1;
    private  String  mImageUrl = "";
    private ArrayList<String> pdfImageLinks =  new ArrayList<>();
    private static LessonSingelton instanceSingelton = null;
    private boolean isPdf = false;
    private ArrayList<String> imageLink = new ArrayList<>();

//    private LessonSingelton(int mHours, int mMinutes, int mSeconds, int page, String imageLink){
//        this.mHours = mHours;
//        this.mSeconds = mSeconds;
//        this.mMinutes = mMinutes;
//        this.mImageUrl = imageLink;
//    }


    private LessonSingelton(){}

    public void SetHoursMinutesSeconds(long timeElapsed,int page){
        mHours = (int) (timeElapsed / 3600000);
        mMinutes = (int) (timeElapsed - mHours * 3600000) / 60000;
        mSeconds = (int) (timeElapsed - mHours * 3600000 - mMinutes * 60000) / 1000;
        mPage = page;

        pdfPlayer.add(new PdfPlayer(mHours,mMinutes,mSeconds,mPage));


    }


    public void addToImageLink(String add){
        imageLink.add(add);
    }
    public ArrayList<String>  getImageinks() {
        return imageLink;
    }


    public void addToPdfImageLinks(String pdfUrl) {
        index++;
        pdfPlayer.get(index).setPdfURL(pdfUrl);

    }

    public ArrayList<PdfPlayer> getPdfPlayer()
    {
        return pdfPlayer;
    }

    public ArrayList<String> getPdfImageLinks() {
        return pdfImageLinks;
    }

    public  void setImageLink(String url){

        mImageUrl = url;
    }


    public static LessonSingelton getInstanceSingelton(){
        if(instanceSingelton == null){
            instanceSingelton = new LessonSingelton();

        }
        return instanceSingelton;
    }
    public  int getIndex(){
        return index;
    }
    public  void incIndex(){
        index = index+1;
    }

    public int getHours(){
        return mHours;
    }
    public int getMinutes(){
        return mMinutes;
    }
    public int getSeconds(){
        return mSeconds;
    }
    public int getPage(){
        return mPage;
    }
    public String getLink(){
        return mImageUrl;
    }


    public void setIsPdf(boolean is){
        isPdf = is;
    }
    public boolean getIsPdf(){
        return isPdf;
    }


    public void cleararrays(){

        pdfImageLinks.clear();
        imageLink.clear();

    }

    public void setPdfOn()
    {
        isPdf = true;
    }

    public void setNoPdf()
    {
        isPdf = false;
    }

    public int getLessonID()
    {
        return mLessonID;
    }

    public void setmLessonID(int id)
    {
        mLessonID = id;
    }

    public int getLessonNum()
    {
        return mLessonNum;
    }

    public void setLessonNum(int num)
    {
        mLessonNum = num;
    }

    public Course getLessonCourse()
    {
        return mCourse;
    }

    public void setLessonCourse(Course course)
    {
        mCourse = course;
    }
}
