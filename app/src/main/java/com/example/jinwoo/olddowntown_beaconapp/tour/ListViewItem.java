package com.example.jinwoo.olddowntown_beaconapp.tour;

import android.graphics.Bitmap;

/**
 * Created by TJ on 2017-03-14.
 */

public class ListViewItem {
    private Bitmap iconDrawable ;
    private String titleStr ;


    public void setIcon(Bitmap icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }

    public Bitmap getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }

}