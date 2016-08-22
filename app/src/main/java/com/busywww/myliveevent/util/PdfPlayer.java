package com.busywww.myliveevent.util;

import android.text.format.Time;

/**
 * Created by Alona on 8/16/2016.
 *
 * hours , minutes,seconds are time in video to change pdf page
 *
 */
public class PdfPlayer {

    private int mMinutes;
    private int mSeconds;
    private int mHours;
    private int mPage;


    public PdfPlayer()
    {
        mMinutes = 0;
        mSeconds = 0;
        mHours = 0;
        mPage = 0;

    }
    public PdfPlayer(int hours,int minutes,int seconds,int page)
    {

        mMinutes = minutes;
        mHours = hours;
        mSeconds = seconds;
        mPage = page;

    }

    public PdfPlayer(long timeElapsed,int page)
    {

        mHours = (int) (timeElapsed / 3600000);
        mMinutes = (int) (timeElapsed - mHours * 3600000) / 60000;
        mSeconds = (int) (timeElapsed - mHours * 3600000 - mMinutes * 60000) / 1000;
        mPage = page;

    }

    // Expects a string in the form MM:SS or HH:MM:SS
    public static int getSecondsFromDurationString(String value){

        String [] parts = value.split(":");

        // Wrong format, no value for you.
        if(parts.length < 2 || parts.length > 3)
            return 0;

        int seconds = 0, minutes = 0, hours = 0;

        if(parts.length == 2){
            seconds = Integer.parseInt(parts[1]);
            minutes = Integer.parseInt(parts[0]);
        }
        else if(parts.length == 3){
            seconds = Integer.parseInt(parts[2]);
            minutes = Integer.parseInt(parts[1]);
            hours = Integer.parseInt(parts[1]);
        }

        return seconds + (minutes*60) + (hours*3600);
    }





}
