package com.example.jinwoo.olddowntown_beaconapp.tamra;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.common.BackgroundService;
import com.example.jinwoo.olddowntown_beaconapp.main.MainActivity;

/**
 * Created by ggg on 2016-12-18.
 */

public class PageOpen extends AppCompatActivity implements View.OnClickListener {

    WebView pageWebView;
    ImageView btnClose;
    TextView pageText;
    String myUrl;
    String PageNm;
    String getTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_open);
        Log.d("zz","   pageOpen() onCreate Start();;;;;;;;;;;;;;   ");
        /*ActionBar actionBar = getSupportActionBar();

        //메뉴바에 '<' 버튼이 생긴다.(두개는 항상 같이다닌다)
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);*/

        Intent intent = getIntent();

        btnClose = (ImageView) findViewById(R.id.btn_close);
        pageText = (TextView) findViewById(R.id.page_text);

        Log.d("zz","1111111111111111111");
        String myAddr = intent.getStringExtra("value");
        Log.d("zz","222222222222222222222222");
        PageNm = intent.getStringExtra("PageNm");
        getTitle = intent.getStringExtra("titleNm");
        pageText.setText(getTitle);
        //setTitle(getTitle);
        Log.d("zz","   myAddr   " + myAddr);
        Log.d("zz","   pageOpen => pageNm                       :::::::::::::   " + PageNm);

        pageWebView = (WebView)findViewById(R.id.pageWebview);

        WebSettings setting = pageWebView.getSettings();

        setting.setJavaScriptEnabled(true);
        pageWebView.setWebViewClient(new MyWebViewClient());

        if(myUrl == null){
            myUrl = myAddr;
        }
        pageWebView.loadUrl(myUrl);
        findViewById(R.id.btn_close).setOnClickListener(this);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            myUrl = url;
            view.loadUrl(url);
            return true;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close :
                Intent intent;
                if ("service".equals(PageNm)) {     //백그라운드의 푸시로 컨텐츠를 보고 닫기 버튼을 눌렀을 경우
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {        //앱이 실행된 상태에서 컨텐츠를 보고 닫기 버튼을 눌렀을 경우
                    this.finish();
                }
                break;
        }
    }

    @Override
    protected void onResume() {

        Log.d("PageOpen","PageOpen => onResume 시작");

        MyApplication.appRun = "1";

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);
        stopService(intent);

        super.onResume();
    }


    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent;

                if("MAIN".equals(PageNm)) {
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else if("intrdc".equals(PageNm)) {
                    intent = new Intent(this, Olddowntown_intrdc.class);
                    startActivity(intent);
                } else if("service".equals(PageNm)) {
                    intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                } else if("intrdcCourseList".equals(PageNm)) {
                    intent = new Intent(this, OlddowntownCourseList.class);
                    startActivity(intent);
                } else if("CourseDetail".equals(PageNm)) {
                    intent = new Intent(this, OlddowntownCourseDetail.class);
                    startActivity(intent);
                } else if("CourseTour".equals(PageNm)) {
                    intent = new Intent(this, CourseTour.class);
                    startActivity(intent);
                } else if("ThemeTour".equals(PageNm)) {
                    intent = new Intent(this, ThemeTour.class);
                    startActivity(intent);
                } else if("MyTrace".equals(PageNm)) {
                    intent = new Intent(this, MyTrace.class);
                    startActivity(intent);
                } else if("TraceMap".equals(PageNm)) {
                    intent = new Intent(this, TraceMap.class);
                    startActivity(intent);
                }

                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);


    }*/


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode != KeyEvent.KEYCODE_BACK) return false;

        if(pageWebView.canGoBack()) {
            pageWebView.goBack();
            return true;
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookie();

        Intent intent;

        if("service".equals(PageNm)) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        /*if("MAIN".equals(PageNm)) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if("intrdc".equals(PageNm)) {
            intent = new Intent(this, Olddowntown_intrdc.class);
            startActivity(intent);
        } else if("service".equals(PageNm)) {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if("intrdcCourseList".equals(PageNm)) {
            intent = new Intent(this, OlddowntownCourseList.class);
            startActivity(intent);
        } else if("CourseDetail".equals(PageNm)) {
            intent = new Intent(this, OlddowntownCourseDetail.class);
            startActivity(intent);
        } else if("CourseTour".equals(PageNm)) {
            intent = new Intent(this, CourseTour.class);
            startActivity(intent);
        } else if("ThemeTour".equals(PageNm)) {
            intent = new Intent(this, ThemeTour.class);
            startActivity(intent);
        } else if("MyTrace".equals(PageNm)) {
            intent = new Intent(this, MyTrace.class);
            startActivity(intent);
        } else if("TraceMap".equals(PageNm)) {
            intent = new Intent(this, TraceMap.class);
            startActivity(intent);
        }*/

        finish();

        return true;
    }

    @Override
    protected void onDestroy() {

        Log.d("PageOpen","PageOpen => onDestroy 호출");
        MyApplication.appRun = "0";

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);

        stopService(intent);

        pageWebView.loadUrl("about:blank"); //Destroy 될 때 미디어들 끄기위해 빈 url로 해줌

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        Log.d("PageOpen","PageOpen => onPause 호출");

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);
        intent.putExtra("PageNm", "JM_MAIN");
        startService(intent);
        Log.d("PageOpen","PageOpen => startService111111111111");



        super.onPause();
    }
}
