package com.example.jinwoo.olddowntown_beaconapp.tour;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.jinwoo.olddowntown_beaconapp.R;

import java.util.ArrayList;

/**
 * Created by TJ on 2017-03-24.
 */

public class DetailSubAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<Bitmap> img;
    LayoutInflater inf;

    public DetailSubAdapter(Context context, int layout, ArrayList<Bitmap> img) {
        this.context = context;
        this.layout = layout;
        this.img = img;

        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 보여줄 데이터의 총 개수 - 꼭 작성해야 함
        return img.size();
    }

    @Override
    public Object getItem(int position) { // 해당행의 데이터- 안해도 됨
        return null;
    }

    @Override
    public long getItemId(int position) { // 해당행의 유니크한 id - 안해도 됨
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 보여줄 해당행의 detail_sub_list xml 파일의 데이터를 셋팅해서 뷰를 완성하는 작업
        if (convertView == null) {
            convertView = inf.inflate(layout, null);
        }

        ImageView iv = (ImageView)convertView.findViewById(R.id.sub_image);
        iv.setImageBitmap(img.get(position));

        return convertView;
    }
}