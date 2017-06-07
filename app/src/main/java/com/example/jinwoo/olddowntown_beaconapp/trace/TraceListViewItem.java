package com.example.jinwoo.olddowntown_beaconapp.trace;

import android.graphics.Bitmap;

/**
 * Created by TJ on 2017-03-14.
 */

public class TraceListViewItem {
    private String course ;
    private String count ;
    private String ServerCount ;
    private Bitmap perfectImg ;
    private String date ;

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getServerCount() {
        return ServerCount;
    }

    public void setServerCount(String ServerCount) {
        this.ServerCount = ServerCount;
    }

    public Bitmap getPerfectImg() {
        return perfectImg;
    }

    public void setPerfectImg(Bitmap perfectImg) {
        this.perfectImg = perfectImg;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}