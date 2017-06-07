package com.example.jinwoo.olddowntown_beaconapp.tour;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jinwoo.olddowntown_beaconapp.R;

import java.io.InputStream;
import java.net.URLEncoder;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by TJ on 2017-03-29.
 */

public class ZoomImg extends AppCompatActivity implements View.OnClickListener{

    String imgName;
    String getContentsText;
    String imageUrl = "http://221.162.53.24:8080";
    TextView contentsText;
    PhotoViewAttacher attacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoom_layout);

        /*ActionBar actionBar = getSupportActionBar();
        //메뉴바에 '<' 버튼이 생긴다.(두개는 항상 같이다닌다)
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);*/

        ImageView zoomImg = (ImageView) findViewById(R.id.zoon_img);
        contentsText = (TextView) findViewById(R.id.contents_text);

        Intent intent = getIntent();
        imgName = intent.getStringExtra("img_name");
        getContentsText = intent.getStringExtra("contents_text");

        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        OpenHttpConnection opHttpCon = new OpenHttpConnection();
        opHttpCon.execute(zoomImg, imageUrl);

        findViewById(R.id.btn_back).setOnClickListener(this);

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back :
                this.finish();
                break;
        }
    }

    private class OpenHttpConnection extends AsyncTask<Object, Void, Bitmap> {

        private ImageView zoomImg;

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap mBitmap = null;
            zoomImg = (ImageView)params[0];
            String url = (String)params[1];

            try {
                String urlEncode = URLEncoder.encode( imgName, "UTF-8" );
                urlEncode = urlEncode.replace("%2F","/");
                url = url + urlEncode;
                Log.d("Oldtown", "   ZoomImg imgName   ->" + imgName);
                Log.d("Oldtown", "   ZoomImg urlEncode   ->" + urlEncode);
                Log.d("Oldtown", "   ZoomImg  url + urlEncode   ->" + url + urlEncode);
                Log.d("Oldtown", "   ZoomImg url result  ->" + url);
            } catch (Exception e) {
                e.printStackTrace();
            }

            InputStream in = null;
            try {
                in = new java.net.URL(url).openStream();
                mBitmap = BitmapFactory.decodeStream(in);
                in.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);
            zoomImg.setImageBitmap(bm);
            contentsText.setText(getContentsText);
            attacher = new PhotoViewAttacher(zoomImg);

            bm = null;
            Log.d("Oldtown", "   ZoomImg bm  ->" + bm);
        }
    }

}
