package com.example.jinwoo.olddowntown_beaconapp.introduction;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.common.BackgroundService;
import com.example.jinwoo.olddowntown_beaconapp.common.MappingCheckClass;
import com.example.jinwoo.olddowntown_beaconapp.main.Constants;
import com.example.jinwoo.olddowntown_beaconapp.tamra.PageOpen;
import com.example.jinwoo.olddowntown_beaconapp.trace.DBHelper;
import com.example.jinwoo.olddowntown_beaconapp.vo.CommonVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.MappingVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.PoiVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.RelatedVO;
import com.kakao.oreum.common.function.Consumer;
import com.kakao.oreum.tamra.Tamra;
import com.kakao.oreum.tamra.base.NearbySpots;
import com.kakao.oreum.tamra.base.Region;
import com.kakao.oreum.tamra.base.Spot;
import com.kakao.oreum.tamra.base.TamraObserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.FILE_PATH;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbName;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbVersion;

/**
 * Created by TJ on 2017-03-29.
 */

public class OlddowntownCourseDetail extends AppCompatActivity {

    ActionBar actionBar;

    DialogInterface mPopupDlg = null;
    AlertDialog.Builder alertbox, odtown_alertbox;
    public static Switch sw1, sw2;
    MappingCheckClass check;
    SharedPreferences pref;
    public SharedPreferences.Editor editor = null;
    private BroadcastReceiver btOnReceiver;

    String relatedCourseUrl = "";
    ArrayList<RelatedVO> RelatedDataList = new ArrayList<RelatedVO>();
    ArrayList<MappingVO> MappingDataList = new ArrayList<MappingVO>();
    ArrayList<PoiVO> poiDataList = new ArrayList<PoiVO>();
    String mappingUrl="";
    String fileUrl="";
    String contentsNo = "";
    String titleNm="";
    DBHelper dbHelper;
    long spotId;

    CommonVO commonVO = new CommonVO();
    String localIp = commonVO.localIp;
    String serverIp = commonVO.serverIp;

    String poiListUrl = "";
    String getCourseStr, getCourseStrNm, getCourseStrText, getCourseStrImg;
    String imageUrl = serverIp;

    ImageView detailImg ;
    TextView detailTv, attractionTitle, attractionList;

    ProgressDialog asyncDialog;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.od_course_detail);

        asyncDialog = new ProgressDialog(
                OlddowntownCourseDetail.this);

        setResources();
        init();

        detailImg = (ImageView) findViewById(R.id.od_detail_img);
        detailTv = (TextView) findViewById(R.id.od_detail_text);
        attractionTitle = (TextView) findViewById(R.id.od_attraction_title);
        attractionList = (TextView) findViewById(R.id.od_attraction);

        Intent intent =getIntent();
        getCourseStr = intent.getExtras().getString("CourseMNo");
        getCourseStrNm = intent.getExtras().getString("CourseNm");
        getCourseStrText = intent.getExtras().getString("CourseText");
        getCourseStrImg = intent.getExtras().getString("CourseImg");
        Log.d("Oldtown", "    getCourseStrImg   ->" + getCourseStrImg);
        setTitle(getCourseStrNm);

        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        poiListUrl = serverIp + "/creativeEconomy/OldTownCourseData.do?userKey=nndoonw&courseMNo=" + getCourseStr;
        CoursePoiGetDataJSON pg = new CoursePoiGetDataJSON();
        pg.execute(poiListUrl);

        dbHelper = new DBHelper(getApplicationContext(), FILE_PATH + dbName, null, dbVersion);

    }

    public void setResources() {

        Constants.btAdapter = BluetoothAdapter.getDefaultAdapter();     //블루투스 어댑터

        pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        Constants.pushState = pref.getString("push", "");

    }

    /* 초기화 함수 */
    public void init() {

        actionBar = getSupportActionBar();

        //메뉴바에 '<' 버튼이 생긴다.(두개는 항상 같이다닌다)
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.to_home);

    }

    public class CoursePoiGetDataJSON extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("데이터 로딩중..");

            asyncDialog.setCancelable(false); //Back키 눌렀을 경우 Dialog Cancle 여부 설정
            asyncDialog.setCanceledOnTouchOutside(false); //Dialog 밖을 터치 했을 경우 Dialog 사라지지 않게

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            //Log.d("Oldtown", "   JSON 1   " + CourseTour.tourListUrl);
            String uri = params[0];

            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;

                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                }

                Log.d("Oldtown", "   JSON 1   " + sb.toString().trim());

                JSONArray jsonArray = new JSONArray(sb.toString().trim());

                Log.d("Oldtown", "   jsonArray   " + jsonArray);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    PoiVO poivo = new PoiVO();
                    poivo.contentsTitle = c.get("contentsTitle").toString();
                    poiDataList.add(poivo);
                }
                return sb.toString().trim();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            asyncDialog.dismiss();
            if (result != null){
                super.onPostExecute(result);
                OpenHttpConnection opHttpCon = new OpenHttpConnection();
                opHttpCon.execute(detailImg, imageUrl);

            } else {
                Log.i("Oldtown", "[onPostExecute( ) ] Image downloading failed !!!!!!!!!!!!!!!!!!! ");
            }


        }
    }

    private class OpenHttpConnection extends AsyncTask<Object, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("데이터 로딩중..");

            asyncDialog.setCancelable(false); //Back키 눌렀을 경우 Dialog Cancle 여부 설정
            asyncDialog.setCanceledOnTouchOutside(false); //Dialog 밖을 터치 했을 경우 Dialog 사라지지 않게

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap mBitmap = null;
            detailImg = (ImageView)params[0];
            String url = (String)params[1];

            try {
                String urlEncode = URLEncoder.encode( getCourseStrImg, "UTF-8" );
                urlEncode = urlEncode.replace("%2F","/");
                url = url + urlEncode;
                Log.d("Oldtown", "   ZoomImg imgName   ->" + getCourseStrImg);
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
            asyncDialog.dismiss();
            super.onPostExecute(bm);
            detailImg.setImageBitmap(bm);

            detailTv.setText(getCourseStrText);
            String temp = "";
            String temp2 = "";

            for (int i=0; i<poiDataList.size(); i++) {
                if ((poiDataList.size()-1) == i) {
                    temp = temp + poiDataList.get(i).contentsTitle;
                } else {
                    temp = temp + poiDataList.get(i).contentsTitle + ", ";
                }
            }
            temp2 = getCourseStrNm + (" 내 볼거리");
            attractionTitle.setText(temp2);
            attractionList.setText(temp);

            bm = null;
            Log.d("Oldtown", "   ZoomImg bm  ->" + bm);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            final LinearLayout linear = (LinearLayout) View.inflate(this, R.layout.settings_detail, null);

            odtown_alertbox = new AlertDialog.Builder(OlddowntownCourseDetail.this);
            odtown_alertbox.setCancelable(false);
            //course_alertbox.setIcon(R.drawable.setting_icon);
            odtown_alertbox.setTitle(Html.fromHtml("<font color='#6D5652'>비콘 신호 수신 설정</font>"));
            odtown_alertbox.setView(linear);
            odtown_alertbox.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    odtown_alertbox = null;
                    Log.d("Oldtown","   닫기 에서 odtown_alertbox   null ? ->" + odtown_alertbox);
                }
            });

            sw1 = (Switch) linear.findViewById(R.id.bt_switch);
            sw2 = (Switch) linear.findViewById(R.id.ps_switch);

            blueToothStateCheck();
            pushStateCheck();

            if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_ON) {
                sw1.setChecked(true);
            } else {
                sw1.setChecked(false);
            }

            //스위치1 on / off change
            sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_OFF) {
                        Constants.btAdapter.enable();     // Bluetooth On
                        Constants.bluetoothState = "1";
                        sw1.setChecked(true);

                        if ("1".equals(Constants.bluetoothState))
                            StartMonitoring();
                        else if ("0".equals(Constants.bluetoothState))
                            StopMonitoring();
                    }
                    else if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_ON){
                        Constants.btAdapter.disable();   // Bluetooth Off
                        Constants.bluetoothState = "0";

                        //블루투스를 끄면 푸시도 꺼지게
                        editor.putString("push", "0");
                        editor.commit();
                        Constants.pushState = "0";
                        sw2.setChecked(false);

                        Toast.makeText(OlddowntownCourseDetail.this, "블루투스 꺼짐", Toast.LENGTH_SHORT).show();
                        Log.d("Oldtown","블루투스  꺼짐");
                    }
                }
            });

            //스위치2 on / off change
            sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked == true) {

                        editor.putString("push", "1");
                        editor.commit();
                        Constants.pushState = "1";
                        Toast.makeText(OlddowntownCourseDetail.this, "푸시 켜짐", Toast.LENGTH_SHORT).show();
                        Log.d("Oldtown","푸시  켜짐");
                    } else { // OFF

                        editor.putString("push", "0");
                        editor.commit();
                        Constants.pushState = "0";
                        Toast.makeText(OlddowntownCourseDetail.this, "푸시 꺼짐", Toast.LENGTH_SHORT).show();
                        Log.d("Oldtown","푸시  꺼짐");
                    }
                }
            });

            AlertDialog alertDialog = odtown_alertbox.create();
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 다이얼로그 생성시 뒷배경 어둡게 처리
            alertDialog.setCanceledOnTouchOutside(false); //다이얼로그 밖 터치시 다이얼로그 닫기 막음 (true이면 닫힘)
            alertDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerReceiver() {

        if (btOnReceiver != null)
            return;

        IntentFilter btFilter = new IntentFilter();
        btFilter.addAction(Constants.btAdapter.ACTION_STATE_CHANGED);

        this.btOnReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                blueToothStateCheck();
            }
        };

        this.registerReceiver(this.btOnReceiver, btFilter);
    }

    private void unregisterReceiver() {

        try {
            getApplicationContext().unregisterReceiver(btOnReceiver);
        } catch (IllegalArgumentException e){
            Log.d("Oldtown", "   unregisterReceiver   -> " + e.getMessage());
        } catch (Exception e) {
            Log.d("Oldtown", "   unregisterReceiver   -> " + e.getMessage());
        }finally {
        }
    }

    /* 블루투스 상태 체크하여 스위치 on/off 시킴 */
    public void blueToothStateCheck() {
        Log.d("Oldtown","   blueToothStateCheck 에서 odtown_alertbox   null ? ->" + odtown_alertbox);
        if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_TURNING_OFF) {// 블루투스가 켜져있을 경우 스위치 on

            //설정 창이 뜬 상태일 때
            if (odtown_alertbox != null) {
                sw1.setChecked(false);
                Constants.bluetoothState = "0";

                sw2.setChecked(false);
                //블루투스를 끄면 푸시도 꺼지게
                editor.putString("push", "0");
                editor.commit();
                Constants.pushState = "0";

            } else {
                Constants.bluetoothState = "0";
                editor.putString("push", "0");
                editor.commit();
                Constants.pushState = "0";
            }
        }

        else if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_TURNING_ON){                    // 블루투스가 꺼져있을 경우 스위치 off

            if (odtown_alertbox != null) {
                sw1.setChecked(true);
                Constants.bluetoothState = "1";
            } else {
                Constants.bluetoothState = "1";
            }
        }
    }

    /* 푸시알림 상태 체크하여 스위치 on/off 시킴 */
    public void pushStateCheck() {

        if("0".equals(Constants.pushState))
            sw2.setChecked(false);
        else if("1".equals(Constants.pushState))
            sw2.setChecked(true);
    }

    @Override
    protected void onDestroy() {

        Log.d("Oldtown", "MainActivity => onDestroy 호출");
        MyApplication.appRun = "0";

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);

        stopService(intent);
        StopMonitoring();

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        Log.d("Oldtown", "CE_Beacon => onPause 호출");

        StopMonitoring();

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);
        intent.putExtra("PageNm", "CE_MAIN");
        startService(intent);
        Log.d("Oldtown", "CE_Beacon => startService111111111111");

        if (asyncDialog != null) {
            asyncDialog.dismiss();
        }

        unregisterReceiver();

        super.onPause();
    }

    @Override
    protected void onResume() {

        Log.d("Oldtown", "CE_Beacon => onResume 호출");
        MyApplication.appRun = "1";

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);

        stopService(intent);

        if ("1".equals(Constants.bluetoothState))
            StartMonitoring();
        else if ("0".equals(Constants.bluetoothState))
            StopMonitoring();

        registerReceiver();

        super.onResume();
    }

    public void BeaconScanning(long spotId) {

        Log.d("Oldtown", "OlddowntownCourseDetail => 감지된 spotId ::::: " + spotId);

        // 비콘 id가 0이 아닐경우 진입
        if (spotId != 0) {
            //비콘 id가 0이 아니면서 매핑된 컨텐츠가 있을 경우 진입

            Log.d("Oldtown", "OlddowntownCourseDetail => onBeaconId   " + spotId);

            mappingUrl = serverIp + "/creativeEconomy/UserMappingInfoData.do?beaconId=" + spotId + "&userKey=nndoonw";
            MappingDataJSON g = new MappingDataJSON();
            g.execute(mappingUrl);

        }
    }


    public class RelatedCourseGetDataJSON extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            //Log.d("Oldtown", "   JSON 1   " + CourseTour.tourListUrl);
            String uri = params[0];

            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;

                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                }

                Log.d("Oldtown", "   JSON 1   " + sb.toString().trim());

                JSONArray jsonArray = new JSONArray(sb.toString().trim());

                Log.d("Oldtown", "   jsonArray   " + jsonArray);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    RelatedVO relatedVO = new RelatedVO();
                    relatedVO.courseMNo = c.get("courseMNo").toString();
                    relatedVO.courseMNm = c.get("courseMNm").toString();
                    //courseMNm = c.get("courseMNm").toString();
                    Log.d("Oldtown", "   RelatedVO.courseMNm 연관코스 데이터   -> " + relatedVO.courseMNm);

                    RelatedDataList.add(relatedVO);

                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    private class MappingDataJSON extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String uri = params[0];

            Log.d("Oldtown", "OlddowntownCourseDetail => uri : " + uri);
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                    Log.d("Oldtown", "OlddowntownCourseDetail => json   " + json);
                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Oldtown", "OlddowntownCourseDetail => result : " + result);


            JSONObject jsonRes = null;

            if (result == null) {
                Log.d("Oldtown", "OlddowntownCourseDetail => result : null");

            } else {

                try {

                    jsonRes = new JSONObject(result);

                    MappingVO mappingVO = new MappingVO();
                    mappingVO.contentsNo = jsonRes.getString("contentsNo");
                    mappingVO.contentsTitle = jsonRes.getString("contentsTitle");
                    mappingVO.filePath = jsonRes.getString("filePath");
                    mappingVO.beaconX = jsonRes.getString("beaconX");
                    mappingVO.beaconY = jsonRes.getString("beaconY");
                    MappingDataList.add(mappingVO);
                    Log.d("Oldtown", "OlddowntownCourseDetail => mappingVO.contentsTitle  매핑데이터 -> " + mappingVO.contentsTitle);
                    Log.d("Oldtown", "OlddowntownCourseDetail => result : " + result);
                    fileUrl = mappingVO.filePath;
                    contentsNo = jsonRes.getString("contentsNo");
                    titleNm = jsonRes.getString("contentsTitle");
                    Log.d("Oldtown", "OlddowntownCourseDetail => fileUrl1 : " + fileUrl);

                    Log.d("Oldtown", "OlddowntownCourseDetail => fileUrl 2 : " + fileUrl);
                    if (!"FALSE".equals(contentsNo))
                        showList(fileUrl, titleNm);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    public void showList(final String fileUrl, final String titleNm) {

        Log.d("Oldtown", "showList() Start;");

        //로컬DB 시작
        Log.d("Oldtown", "RelatedDataList 클리어 전 사이즈 =>  : " + RelatedDataList.size());
        Log.d("Oldtown", "-----------------------------------------------  : ");
        Log.d("Oldtown", "MappingDataList 클리어 전 사이즈 =>  : " + MappingDataList.size());

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, kk:mm:ss");
        String bDate = simpleDateFormat.format(date);
        Log.d("Oldtown", "        bDate          =>  : " + bDate);

        String id;
        String title;
        String x;
        String y;
        String mno;
        String mnm;

        id = String.valueOf(spotId);

        dbHelper.delete(id);

        for (int i = 0; i < RelatedDataList.size(); i++) {
            Log.d("Oldtown", "---------------showList에서 받은 비콘 ID ---------------->" + spotId);
            title = MappingDataList.get(0).contentsTitle;
            Log.d("Oldtown", "---------------title ---------------->" + title);
            x = MappingDataList.get(0).beaconX;
            Log.d("Oldtown", "---------------x ---------------->" + x);
            y = MappingDataList.get(0).beaconY;
            Log.d("Oldtown", "---------------y ---------------->" + y);
            mno = RelatedDataList.get(i).courseMNo;
            Log.d("Oldtown", "---------------mno ---------------->" + mno);
            mnm = RelatedDataList.get(i).courseMNm;
            Log.d("Oldtown", "---------------mnm ---------------->" + mnm);
            dbHelper.insert(id, title, x, y, mno, mnm, bDate); // 로컬 DB에 인서트
            Log.d("Oldtown", "OlddowntownCourseDetail => DB 인서트 됨?  ->");
            Log.d("Oldtown", "OlddowntownCourseDetail => DB 인서트    ->  " + i + " 회   " + dbHelper.getResult().toString());
        }

        //로컬DB 끝

        if (alertbox != null) {
            mPopupDlg.dismiss();
            alertbox = null;
            mPopupDlg = null;
        }

        alertbox = new AlertDialog.Builder(OlddowntownCourseDetail.this);
        alertbox.setTitle("비콘 신호 감지");
        alertbox.setMessage("페이지를 여시겠습니까?");
        alertbox.setIcon(R.drawable.ic_menu_gallery);
        alertbox.setCancelable(false); // back 버튼 막음
        alertbox.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(OlddowntownCourseDetail.this, PageOpen.class);
//                    intent.putExtra("value", "http://218.157.145.72:10/creativeEconomy/MobileCommonPage.do?contentsNo="+contentsNo);
                intent.putExtra("value", fileUrl);
                intent.putExtra("titleNm", titleNm);
                intent.putExtra("PageNm", "MAIN");
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        alertbox.setNegativeButton("아니오", null);
        mPopupDlg = alertbox.show();

        Log.d("Oldtown", "RelatedDataList 클리어 전 사이즈 =>  : " + RelatedDataList.size());
        RelatedDataList.clear();
        Log.d("Oldtown", "RelatedDataList 클리어 후 사이즈 =>  : " + RelatedDataList.size());
        Log.d("Oldtown", "-----------------------------------------------  : ");

        Log.d("Oldtown", "MappingDataList 클리어 전 사이즈 =>  : " + MappingDataList.size());
        MappingDataList.clear();
        Log.d("Oldtown", "MappingDataList 클리어 후 사이즈 =>  : " + MappingDataList.size());
    }

    private final TamraObserver tamraObserver = new TamraObserver() {
        @Override
        public void didEnter(Region region) {

            Log.d("Oldtown", "OlddowntownCourseDetail => ----didEnter ----" + region.name() + "에 진입");
        }

        @Override
        public void didExit(Region region) {

            Log.d("Oldtown", "OlddowntownCourseDetail => ----didExit ----" + region.name() + "에서 이탈");
        }

        @Override
        public void ranged(NearbySpots nearbySpots) {
            final Set<Spot> droppedSet = new HashSet<>();

            Log.d("Oldtown", "MainActivity => ranged");
            droppedSet.addAll(Constants.spotSet);
            nearbySpots.orderBy(Spot.ACCURACY_ORDER).foreach(new Consumer<Spot>() {
                @Override
                public void accept(Spot spot) {
                    droppedSet.remove(spot);

                    //if (Constants.spotSet.contains(spot)) return;


                    Constants.spotSet.add(spot);
                    Log.d("Oldtown", "MainActivity => raged Start()");
                    Log.d("Oldtown", "----MainActivity() spotSet----   근처에" + spot.description() + "발견");
                    Log.d("Oldtown", "---- MainActivity() spot.id() ----" + spot.id());
                    spotId = spot.id();

                    if (check != null) check = null;

                    check = new MappingCheckClass();
                    if (check.isReceivedMappingContents(String.valueOf(spot.id()))) {

                        Log.d("Oldtown", "---------------ranged에서 받은 비콘 ID ---------------->" + spotId);
                        relatedCourseUrl = serverIp + "/creativeEconomy/OldTownRelationCourseData.do?beaconId=" + spot.id();
                        RelatedCourseGetDataJSON r = new RelatedCourseGetDataJSON();
                        r.execute(relatedCourseUrl);
                        Log.d("Oldtown", "---- relatedCourseUrl ----" + relatedCourseUrl);
                        BeaconScanning(spot.id());
                    }
                }
            });
            Constants.spotSet.removeAll(droppedSet);
            droppedSet.clear();
        }
    };

    public void StartMonitoring() {
        Tamra.addObserver(tamraObserver);
    }

    public void StopMonitoring() {
        Tamra.removeObserver(tamraObserver);
    }
}
