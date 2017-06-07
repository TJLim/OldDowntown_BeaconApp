package com.example.jinwoo.olddowntown_beaconapp.introduction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jinwoo.olddowntown_beaconapp.R;

import java.util.ArrayList;

/**
 * Created by TJ on 2017-03-14.
 */

public class OdAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<String> odCourseNm;
    LayoutInflater inf;

    public OdAdapter(Context context, int layout, ArrayList<String> odCourseNm) {
        this.context = context;
        this.layout = layout;
        this.odCourseNm = odCourseNm;

        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { // 보여줄 데이터의 총 개수 - 꼭 작성해야 함
        return odCourseNm.size();
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

        TextView tv = (TextView)convertView.findViewById(R.id.od_course_name);
        tv.setText(odCourseNm.get(position));

        return convertView;
    }
}