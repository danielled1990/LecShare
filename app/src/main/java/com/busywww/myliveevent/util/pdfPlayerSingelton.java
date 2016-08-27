package com.busywww.myliveevent.util;

import java.util.ArrayList;

/**
 * Created by Danielle on 8/26/2016.
 */
public class pdfPlayerSingelton {
    private  int mHours;
    private  int mMinutes;
    private  int mSeconds;
    private  int mPage;

    private  ArrayList<pdfPlayerSingelton> array = null;
    private static int index =0;
    private  String  mImageUrl = "";
    private ArrayList<String> links =  new ArrayList<>();
    private static pdfPlayerSingelton instanceSingelton = null;
    private boolean isPdf = false;
    private ArrayList<String> imageLink = new ArrayList<>();
   /* public PdfPlayer(long timeElapsed, int page, String imageLink)
    {

        mHours = (int) (timeElapsed / 3600000);
        mMinutes = (int) (timeElapsed - mHours * 3600000) / 60000;
        mSeconds = (int) (timeElapsed - mHours * 3600000 - mMinutes * 60000) / 1000;
        mPage = page;
        mImageUrl = imageLink;

    }*/
    private pdfPlayerSingelton(int mHours, int mMinutes,int mSeconds, int page, String imageLink){
        this.mHours = mHours;
        this.mSeconds = mSeconds;
        this.mMinutes = mMinutes;
        this.mImageUrl = imageLink;
    }
    private  pdfPlayerSingelton(){}

    public  void   SetHoursMinutesSeconds(long timeElapsed,int page){
        mHours = (int) (timeElapsed / 3600000);
        mMinutes = (int) (timeElapsed - mHours * 3600000) / 60000;
        mSeconds = (int) (timeElapsed - mHours * 3600000 - mMinutes * 60000) / 1000;
        mPage = page;
        getArray();
        array.add(new pdfPlayerSingelton(mHours,mMinutes,mSeconds,mPage,null));

    }

    public ArrayList<pdfPlayerSingelton> getArray(){
        if(array == null){
            array = new ArrayList<>();
        }
        return array;
    }
    public void addToImageLink(String add){
        imageLink.add(add);
    }
    public ArrayList<String>  getImageinks() {
        return imageLink;
    }
    public void addToLinks(String add) {
        links.add(add);
    }
    public static pdfPlayerSingelton getInstanceSingelton(){
        if(instanceSingelton == null){
            instanceSingelton = new pdfPlayerSingelton();

        }
        return instanceSingelton;
    }
    public  int getIndex(){
        return index;
    }
    public  void incIndex(){
        index = index+1;
    }
    public  void setImageLink(String url){
     //   instance.get(index).setImageLink(mImageUrl);
        mImageUrl = url;
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

    public ArrayList<String>  getLinks() {
        return links;
    }
    public void setIsPdf(boolean is){
        isPdf = is;
    }
    public boolean getIsPdf(){
        return isPdf;
    }
    public void cleararrays(){
        array.clear();
        links.clear();
        imageLink.clear();

    }

}
