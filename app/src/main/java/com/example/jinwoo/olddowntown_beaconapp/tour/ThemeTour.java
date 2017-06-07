package com.example.jinwoo.olddowntown_beaconapp.tour;

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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinwoo.olddowntown_beaconapp.HorizontalListView.HorizontalListView;
import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.common.BackgroundService;
import com.example.jinwoo.olddowntown_beaconapp.common.MappingCheckClass;
import com.example.jinwoo.olddowntown_beaconapp.main.Constants;
import com.example.jinwoo.olddowntown_beaconapp.tamra.PageOpen;
import com.example.jinwoo.olddowntown_beaconapp.trace.DBHelper;
import com.example.jinwoo.olddowntown_beaconapp.vo.CommonVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.CourseVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.MappingVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.PoiVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.RelatedForBeaconVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.RelatedVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.SubImgVO;
import com.kakao.oreum.common.function.Consumer;
import com.kakao.oreum.tamra.Tamra;
import com.kakao.oreum.tamra.base.NearbySpots;
import com.kakao.oreum.tamra.base.Region;
import com.kakao.oreum.tamra.base.Spot;
import com.kakao.oreum.tamra.base.TamraObserver;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
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

import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.firstCheck;
import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.locationListener;
import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.locationManager;
import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.useGps;
import static com.example.jinwoo.olddowntown_beaconapp.main.Constants.lat;
import static com.example.jinwoo.olddowntown_beaconapp.main.Constants.lon;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.FILE_PATH;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbName;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbVersion;

/**
 * Created by TJ on 2017-03-02.
 */

public class ThemeTour extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener{

    FrameLayout mapFrame;
    private MapView mMapView;
    MapPOIItem marker;
    MapPoint centerPoint;
    int spinnerPosition = 0;

    CommonVO commonVO = new CommonVO();
    String localIp = commonVO.localIp;
    String serverIp = commonVO.serverIp;

    String tourListUrl = serverIp + "/creativeEconomy/OldTownGroupData.do?courseGroupNo=1";
    String poiListUrl = "";
    String courseMNo = "";
    ArrayList<CourseVO> courseList = new ArrayList<CourseVO>();
    ArrayList<PoiVO> poiDataList = new ArrayList<PoiVO>();
    ArrayList<String> courseTourList = new ArrayList<String>(); //코스 이름만 따로 배열 생성
    ArrayList<SubImgVO> SubDataList = new ArrayList<SubImgVO>();

    AlertDialog.Builder course_alertbox;
    public static Switch sw1, sw2;
    SharedPreferences pref;
    public SharedPreferences.Editor editor = null;
    private BroadcastReceiver btOnReceiver;

    ActionBar actionBar;

    LinearLayout mainCourse, poiDetail, backBtn;

    public boolean isVisible = true;
    public boolean isClicked = false;
    public boolean isTrackingRunning = true;
    public boolean detailVisible = false;

    Activity act;
    ImageView LocationBtn;
    TextView spotText, spotNm, findRoad, spotRelatedCourse, subText, themeTitle;

    Button otherTheme ;

    HorizontalListView hListview;

    ListViewAdapter ListAdapter;

    ArrayList<Bitmap> subImgBitmapList;

    ArrayList<MapPOIItem> poiList = new ArrayList<MapPOIItem>();

    MapPoint.GeoCoordinate mapPointGeo;

    public double locationLat, locationLon;
    public String gpsX = "";
    public String gpsY = "";

    MappingCheckClass check;
    String relatedCourseUrl = "";
    String relatedCourseForBeaconUrl = "";
    String subImgUrl = "";
    ArrayList<RelatedVO> RelatedDataList = new ArrayList<RelatedVO>();
    ArrayList<RelatedForBeaconVO> RelatedDataListForBeacon = new ArrayList<RelatedForBeaconVO>();
    ArrayList<MappingVO> MappingDataList = new ArrayList<MappingVO>();
    String mappingUrl="";
    String fileUrl="";
    String contentsNo = "";
    String titleNm="";
    DBHelper dbHelper;
    long spotId;
    DialogInterface mPopupDlg = null;
    AlertDialog.Builder alertbox, locationDialog;

    int beaconId;
    Window detailWin;
    LayoutInflater detailInflater;
    LinearLayout detailLinear;
    LinearLayout.LayoutParams detailParamlinear;

    String relatedCourseName = "";

    Gallery slideGallery;
    DetailSubAdapter adapter;

    ProgressDialog asyncDialog;

    int positionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_tour_main);

        asyncDialog = new ProgressDialog(
                ThemeTour.this);

        // 맵위에 "현위치" 기능 버튼의 레이아웃을 겹침
        Window win = getWindow();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout)inflater.inflate(R.layout.over_layout, null);
        LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        win.addContentView(linear, paramlinear);//이 부분이 레이아웃을 겹치는 부분
        //add는 기존의 레이아웃에 겹쳐서 배치하라는 뜻이다.

        detailWin = getWindow();
        detailInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        detailLinear = (LinearLayout)detailInflater.inflate(R.layout.spot_detail, null);
        detailParamlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        Log.d("Oldtown", "   poiListUrl  " + poiListUrl);

        themeTitle = (TextView) findViewById(R.id.theme_title);
        findViewById(R.id.theme_dialog).setOnClickListener(themeDialogClickListener);

        CourseGetDataJSON cg = new CourseGetDataJSON();
        cg.execute(tourListUrl);

        setResources();
        init();

        hListview = (HorizontalListView) findViewById(R.id.attraction_listview);
        hListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (subImgBitmapList != null) { // 서브 이미지 배열들이 값을 가지고 있을 때 클리어
                    subImgBitmapList.clear();
                    SubDataList.clear();
                    Log.d("Oldtown", "   hListview subImgBitmapList   " + subImgBitmapList.size());
                    Log.d("Oldtown", "   hListview SubDataList   " + SubDataList.size());

                }
                Log.d("Oldtown","hListview.setOnClickListener ->" + position);
                mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(poiDataList.get(position).beaconX), Double.parseDouble(poiDataList.get(position).beaconY)), true);
                mMapView.selectPOIItem(poiList.get(position), false);

                subImgUrl = serverIp + "/creativeEconomy/OldTownDetailSubImg.do?oldtownContentsNo=" + poiDataList.get(position).oldtownContentsNo;
                Log.d("Oldtown","                   subImgUrl                  ->" + subImgUrl);
                SubImgGetDataJSON s = new SubImgGetDataJSON();
                s.execute(subImgUrl);

                //연관코스 JSON
                relatedCourseUrl = serverIp + "/creativeEconomy/OldTownRelationCourseData.do?beaconId=" + poiDataList.get(position).beaconId;
                RelatedCourseGetDataJSON r = new RelatedCourseGetDataJSON();
                r.execute(relatedCourseUrl);

                Log.d("Oldtown","---- relatedCourseUrl ----" + relatedCourseUrl);
                Log.d("Oldtown","---- RelatedDataList.size() ---->" + RelatedDataList.size());

                Log.d("Oldtown","        hListview           get(position).contentsTitle -> " + poiDataList.get(position).contentsTitle + ",   get(i).contentsTitle -> " + poiDataList.get(position).contentsTitle);
                detailWin.addContentView(detailLinear, detailParamlinear);//이 부분이 레이아웃을 겹치는 부분
                backBtn = (LinearLayout) findViewById(R.id.back_btn);
                spotNm = (TextView) findViewById(R.id.spot_detail_nm);
                //spotImage = (ImageView) findViewById(R.id.spot_detail_image);
                spotText = (TextView) findViewById(R.id.spot_detail_text);
                findRoad = (TextView) findViewById(R.id.spot_find_road);
                spotRelatedCourse = (TextView) findViewById(R.id.spot_related_course);
                act.findViewById(R.id.back_btn).setOnClickListener(backClickListener);
                act.findViewById(R.id.spot_find_road).setOnClickListener(frClickListener);
                slideGallery = (Gallery)findViewById(R.id.gallery1);

                Log.d("Oldtown","        hListview           position -> " + position + ",   contentsTitle -> " + poiDataList.get(position).contentsTitle);
                spotNm.setText(poiDataList.get(position).contentsTitle);
                //spotImage.setImageBitmap(BitmapList.get(position));
                spotText.setText((poiDataList.get(position).contentsText));

                subText = (TextView) findViewById(R.id.sub_image_text);
                slideGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) { // 선택되었을 때 콜백메서드
                        subText.setText(SubDataList.get(position).contentsDText);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                slideGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ThemeTour.this, ZoomImg.class);
                        intent.putExtra("img_name", SubDataList.get(position).contentsDImg);
                        intent.putExtra("contents_text", SubDataList.get(position).contentsDText);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                Log.d("Oldtown","        hListview          position_spotNm  -> " + poiDataList.get(position).contentsTitle + ",   i_spotNm -> " + poiDataList.get(position).contentsTitle);

                isClicked = true;

                Log.d("Oldtown","           hListview의 항목 클릭           ->" + position);
            }
        });

        dbHelper = new DBHelper(getApplicationContext(), FILE_PATH+dbName, null, dbVersion);
    }

    LinearLayout.OnClickListener themeDialogClickListener = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View v) {
            String[] tempArray = courseTourList.toArray(new String[courseTourList.size()]);

            for (String s : tempArray) {
                Log.d("Oldtown","           tempArray           ->" + s);
            }
            AlertDialog.Builder ab = new AlertDialog.Builder(ThemeTour.this);
            ab.setTitle("코스선택");
            ab.setSingleChoiceItems(tempArray, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 각 리스트를 선택했을때
                            //Toast.makeText(ThemeTour.this, "선택 : " + whichButton + " 코스명 : " + courseTourList.get(whichButton), Toast.LENGTH_SHORT).show();
                            positionIndex = whichButton + 1;
                        }
                    }).setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                            //Toast.makeText(ThemeTour.this, "선택 : " + (whichButton+2), Toast.LENGTH_SHORT).show();
                            spinnerPosition = positionIndex;
                            //positionIndex = whichButton + 2;

                            if (isTrackingRunning == false) { //현위치 트래킹모드가 실행 중이면 off 시킴
                                Log.d("Oldtown", "   isTrackingRunning -> "+isTrackingRunning);
                                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                                mMapView.setShowCurrentLocationMarker(false);
                                LocationBtn.setImageResource(R.drawable.marker_gps);
                                isTrackingRunning = true;
                            }

                            if(positionIndex != 0){

                                if (isClicked == true) {
                                    ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //디테일 뷰 제거
                                    isClicked = false;
                                }

                                mainCourse.setVisibility(View.VISIBLE);
                                poiDataList.clear();
                                //코스의 항목들 불러옴
                                poiListUrl = serverIp + "/creativeEconomy/OldTownCourseData.do?userKey=nndoonw&courseMNo="
                                        + courseList.get(positionIndex-1).courseMNo;
                                Log.d("Oldtown", "   courseMNo2  " + courseMNo );
                                Log.d("Oldtown", "   poiListUrl  " + poiListUrl );
                                CoursePoiGetDataJSON pg = new CoursePoiGetDataJSON();
                                pg.execute(poiListUrl);
                                themeTitle.setText(courseList.get(positionIndex-1).courseMNm);
                                isVisible = true;
                            }

                        }
                    }).setNegativeButton("Reset",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Reset 버튼 클릭시
                            Log.d("Oldtown", "           else    poiListUrl     ->" + poiListUrl);
                            mMapView.removeAllPOIItems();   //마커 다 제거
                            mainCourse.setVisibility(View.GONE);    //볼거리 사라짐
                            if (isVisible == true && isClicked == true) {   //볼거리랑 보고있는 디테일 화면 사라짐
                                ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //뷰제거
                                isClicked = false;
                            }
                            themeTitle.setText("테마를 선택하세요.");
                            isVisible = false;
                        }
                    });
            ab.show();

        }
    };

    public class SubImgGetDataJSON extends AsyncTask<String, String, ArrayList<Bitmap>> {

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("데이터 로딩중..");

            // show dialog
            asyncDialog.setCancelable(false); //Back키 눌렀을 경우 Dialog Cancle 여부 설정
            asyncDialog.setCanceledOnTouchOutside(false); //Dialog 밖을 터치 했을 경우 Dialog 사라지지 않게
            asyncDialog.show();
            super.onPreExecute();

            if (detailVisible) {
                slideGallery.setVisibility(View.GONE);
                detailVisible = false;
            }
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(String... params) {
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

                    SubImgVO subImgVO = new SubImgVO();

                    subImgVO.contentsDSeq = c.get("contentsDSeq").toString();
                    subImgVO.contentsMNo = c.get("contentsMNo").toString();
                    subImgVO.contentsDImg = c.get("contentsDImg").toString();
                    subImgVO.contentsDText = c.get("contentsDText").toString();

                    Log.d("Oldtown", "   subImgVO.contentsDImg   " + subImgVO.contentsDImg);

                    SubDataList.add(subImgVO);
                }
                //subImgBitmapList = null;
                subImgBitmapList = new ArrayList<Bitmap>();
                //subImgbitmap = null;
                int tempI = 0, tempJ = 0;
                tempI = (int) MyApplication.deviceWidth / 3;
                tempJ = (int) MyApplication.deviceWidth * 2 / 9;

                for (int i=0;i<SubDataList.size();i++) {

                    String urlEncode = URLEncoder.encode( SubDataList.get(i).contentsDImg, "UTF-8" );
                    urlEncode = urlEncode.replace("%2F","/");

                    try{
                        Bitmap subImgbitmap;
                        subImgbitmap = SubImgdownloadUrl(serverIp + urlEncode);

                        Log.d("Oldtown", "   downloadUrl   " +subImgbitmap);

                        Bitmap resized;
                        resized = Bitmap.createScaledBitmap(subImgbitmap, tempI, tempJ, true);

                        int[] intArray;
                        intArray = new int[tempI*tempJ];
                        resized.getPixels(intArray, 0, tempI, 0, 0, tempI, tempJ);
                        subImgBitmapList.add(resized);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                //return sb.toString().trim();
                Log.d("Oldtown", "   SubImgList   " + SubDataList.size());
                Log.d("Oldtown", "   subImgBitmapList   " + subImgBitmapList.size());
                return subImgBitmapList;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {

            asyncDialog.dismiss();

            adapter = null;
            adapter = new DetailSubAdapter(getApplicationContext(), R.layout.detail_sub_list, subImgBitmapList);
            slideGallery.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            //intArray = null;
            /*subImgbitmap = null;
            resized = null;*/

            if (detailVisible == false) {
                slideGallery.setVisibility(View.VISIBLE);
                detailVisible = true;
            }
        }
    }

    private Bitmap SubImgdownloadUrl(String imageUrl) throws IOException {
        Log.i("Oldtown", "        SubImgdownloadUrl            "+imageUrl);

        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        Bitmap bitmap = null;
        try{
            URL url = new URL(imageUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(iStream);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return bitmap;
    }

    public class CourseGetDataJSON extends AsyncTask<String, String, String> {

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

                Log.d("GetJson", "   JSON 1   " + sb.toString().trim());

                JSONArray jsonArray = new JSONArray(sb.toString().trim());

                Log.d("GetJson", "   jsonArray   " + jsonArray);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    CourseVO courseVO = new CourseVO();
                    courseVO.courseGroupNo = c.get("courseGroupNo").toString();
                    courseVO.courseMNo = c.get("courseMNo").toString();
                    courseVO.courseMNm = c.get("courseMNm").toString();
                    //courseMNm = c.get("courseMNm").toString();
                    Log.d("GetJson", "   courseVO.courseMNm   " + courseVO.courseMNm);

                    courseList.add(courseVO);
                    courseTourList.add(courseVO.courseMNm); //코스 이름만 따로 배열 생성


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

    public class CoursePoiGetDataJSON extends AsyncTask<String, String, ArrayList<Bitmap>> {

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
        protected ArrayList<Bitmap> doInBackground(String... params) {
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
                    poivo.courseMNo = c.get("courseMNo").toString();
                    poivo.courseMNm = c.get("courseMNm").toString();
                    poivo.beaconId = c.get("beaconId").toString();
                    poivo.beaconNm = c.get("beaconNm").toString();
                    poivo.beaconX = c.get("beaconX").toString();
                    poivo.beaconY = c.get("beaconY").toString();
                    poivo.contentsTitle = c.get("contentsTitle").toString();
                    poivo.contentsText = c.get("contentsText").toString();
                    poivo.contentsImg = c.get("contentsImg").toString();
                    poivo.courseContentsYn = c.get("courseContentsYn").toString();
                    poivo.oldtownContentsNo = c.get("oldtownContentsNo").toString();

                    Log.d("Oldtown", "   poivo.courseMNo   " + poivo.courseMNo);

//                    tempList.add(poivo.contentsTitle);
                    poiDataList.add(poivo);

//                    if (poivo.courseContentsYn.equals("Y")){
//                        imgList.add(poivo.contentsImg);
//                    }
                    //Log.d("Oldtown", "   poiDataList    -> " + poiDataList.size() + "   tempList    -> " + tempList.size());
                    Log.d("Oldtown", "   poiList add 후에 size   " + poiDataList.size());
                }

                ArrayList<Bitmap> BitmapList;
                BitmapList = new ArrayList<Bitmap>();
                int tempI = 0, tempJ = 0;
                tempI = (int) MyApplication.deviceWidth / 3;
                tempJ = (int) MyApplication.deviceWidth * 2 / 9;
                for (int i = 0; i < poiDataList.size(); i++) {

                    if(poiDataList.get(i).courseContentsYn.equals("Y")) {

                        String urlEncode = URLEncoder.encode( poiDataList.get(i).contentsImg, "UTF-8" );
                        urlEncode = urlEncode.replace("%2F","/");

                        try{
                            Bitmap bitmap;
                            Bitmap resized;
                            bitmap = downloadUrl(serverIp + urlEncode);

                            resized = Bitmap.createScaledBitmap(bitmap, tempI, tempJ, true);
                            int[] intArray;
                            intArray = new int[tempI*tempJ];
                            resized.getPixels(intArray, 0, tempI, 0, 0, tempI, tempJ);
                            BitmapList.add(resized);

                            //BitmapList.add(bitmap);
                            Log.d("Oldtown", "   tempI   " +tempI);
                            Log.d("Oldtown", "   tempJ   " +tempJ);
                            Log.d("Oldtown", "   downloadUrl   " +bitmap);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                }
                //return sb.toString().trim();
                return BitmapList;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {
            asyncDialog.dismiss();
            if (result != null){
                super.onPostExecute(result);

                /*testImg.setImageBitmap(result);
                testImg.invalidate();*/
                ListAdapter = null;
                ListAdapter = new ListViewAdapter();
                hListview.setAdapter(ListAdapter);

                for( int i = 0 ; i < result.size(); i++) {
                    Log.d("Oldtown","result->"+result);
                    ListAdapter.addItem(result.get(i), poiDataList.get(i).contentsTitle);
                }
            } else {
                Log.i("Oldtown", "[onPostExecute( ) ] Image downloading failed !!!!!!!!!!!!!!!!!!! ");
            }
            /*hListview.setAdapter(new HAdapter());
            Log.d("Oldtown", "           누가 먼저타냐 씨발         -> "+ new HAdapter());*/
            markerChange();
            //intArray = null;
            /*bitmap = null;
            resized = null;*/

        }
    }

    private Bitmap downloadUrl(String imageUrl) throws IOException {
        Log.i("Oldtown", "                    "+imageUrl);

        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        Bitmap bitmap = null;
        try{
            URL url = new URL(imageUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(iStream);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return bitmap;
    }

    public void setResources() {
        setTitle("테마투어");

        Constants.btAdapter = BluetoothAdapter.getDefaultAdapter();     //블루투스 어댑터

        pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        Constants.pushState = pref.getString("push", "");

        act = this;

        mainCourse = (LinearLayout) findViewById(R.id.tour_detail);
        poiDetail = (LinearLayout) findViewById(R.id.tour_balloon);
        otherTheme = (Button) findViewById(R.id.other_theme);
        LocationBtn = (ImageView) findViewById(R.id.location_btn);

    }

    /* 초기화 함수 */
    public void init() {

        actionBar = getSupportActionBar();

        //메뉴바에 '<' 버튼이 생긴다.(두개는 항상 같이다닌다)
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.to_home);

        act.findViewById(R.id.other_theme).setOnClickListener(otClickListener);
        //act.findViewById(R.id.spot_find_road).setOnClickListener(frClickListener);
        act.findViewById(R.id.location_btn).setOnClickListener(locationClickListener);
    }

    public void markerChange() {

        Log.d("Oldtown", "   poiList.size()+1  " + poiDataList.size());

        //mMapView.removePOIItem(marker);

        mMapView.removeAllPOIItems();

        mMapView.setPOIItemEventListener(this);

        Log.d("Oldtown", "   poiList.size()+2  " + poiDataList.size());
        for (int i=0; i<poiDataList.size(); i++) {
            Log.d("Oldtown", "   for - poiDataList.size()  " + poiDataList.size());
            Log.d("Oldtown", "   poiList X " + poiDataList.get(i).beaconX);
            Log.d("Oldtown", "   poiList Y" + poiDataList.get(i).beaconY);
            Log.d("Oldtown", "   poiList Y" + poiDataList.get(i).courseMNo);

            marker = new MapPOIItem();
            marker.setItemName(poiDataList.get(i).contentsTitle);
            marker.setTag(Integer.parseInt(poiDataList.get(i).beaconId));
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(poiDataList.get(i).beaconX), Double.parseDouble(poiDataList.get(i).beaconY)));
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
            //marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
            //marker.setCustomImageResourceId(R.drawable.pin_img_blue); // 마커 이미지.
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
            marker.setCustomImageAnchorPointOffset(new MapPOIItem.ImageOffset(30,0));
            marker.setShowDisclosureButtonOnCalloutBalloon(false); //POI 클릭시, '>' 모양 안 나타게
            poiList.add(marker);
            mMapView.addPOIItem(marker);
            Log.d("Oldtown", "끝");
        }
        mMapView.fitMapViewAreaToShowAllPOIItems();
        //mMapView.setZoomLevel(3, true);
        Log.d("Oldtown", "      markerChange()       poiDataList.clear();" + poiDataList.size());
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        mMapView.getMapCenterPoint();
        centerPoint =  mMapView.getMapCenterPoint();

        if (spinnerPosition !=0) { //코스를 선택한 상태라면,
            mMapView.setMapCenterPoint(centerPoint, true); //앱이 백으로 갔다가 다시 켜졌을때를 위함임.
        } else {
            mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(33.512801, 126.523920), true); // 맵시작시 중앙로로 잡아줌
        }

        Log.d("Oldtown", "      onMapViewInitialized()       ->" + centerPoint);
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

        if (subImgBitmapList != null) { // 서브 이미지 배열들이 값을 가지고 있을 때 클리어
            subImgBitmapList.clear();
            SubDataList.clear();
            //intArray = null;
            /*subImgbitmap = null;
            resized = null;*/
            Log.d("Oldtown", "    subImgBitmapList   " + subImgBitmapList.size());
            Log.d("Oldtown", "    SubDataList   " + SubDataList.size());

        }

        String tempContentsNo ="";
        //POI 클릭시 해당 POI를 화면 중앙으로 잡아줌
        for( int i = 0 ; i < poiDataList.size(); i++) {
            if( Integer.parseInt(poiDataList.get(i).beaconId) == mapPOIItem.getTag()) {
                mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(poiDataList.get(i).beaconX), Double.parseDouble(poiDataList.get(i).beaconY)), true);
                tempContentsNo = poiDataList.get(i).oldtownContentsNo;
            }
        }

        beaconId = mapPOIItem.getTag();

        subImgUrl = serverIp + "/creativeEconomy/OldTownDetailSubImg.do?oldtownContentsNo=" + tempContentsNo;
        Log.d("Oldtown","                   subImgUrl                  ->" + subImgUrl);
        SubImgGetDataJSON s = new SubImgGetDataJSON();
        s.execute(subImgUrl);

        if (isVisible == true && isClicked ==false ) { //주요장소에서 POI 클릭했을 때
            detailWin.addContentView(detailLinear, detailParamlinear); //이 부분이 레이아웃을 겹치는 부분
            backBtn = (LinearLayout) findViewById(R.id.back_btn);
            spotNm = (TextView) findViewById(R.id.spot_detail_nm);
            spotText = (TextView) findViewById(R.id.spot_detail_text);
            findRoad = (TextView) findViewById(R.id.spot_find_road);
            act.findViewById(R.id.back_btn).setOnClickListener(backClickListener);
            act.findViewById(R.id.spot_find_road).setOnClickListener(frClickListener);

            //POI에 이름, 이미지, 텍스트 박아줌
            for(int i = 0 ; i < poiDataList.size(); i++) {
                if( beaconId == Integer.parseInt(poiDataList.get(i).beaconId)) {
                    spotNm.setText(poiDataList.get(i).contentsTitle);
                    spotText.setText((poiDataList.get(i).contentsText));
                    break;
                }
            }

            slideGallery = (Gallery)findViewById(R.id.gallery1);
            subText = (TextView) findViewById(R.id.sub_image_text);

            slideGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) { // 선택되었을 때 콜백메서드
                    subText.setText(SubDataList.get(position).contentsDText);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // 디테일 이미지 클릭시 이미지 확대
            slideGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ThemeTour.this, ZoomImg.class);
                    intent.putExtra("img_name", SubDataList.get(position).contentsDImg);
                    intent.putExtra("contents_text", SubDataList.get(position).contentsDText);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            });

            isClicked = true;

        } else if(isVisible == true && isClicked ==true) { //디테일 보는 도중 다른 POI 클릭했을 때

            backBtn = (LinearLayout) findViewById(R.id.back_btn);
            spotNm = (TextView) findViewById(R.id.spot_detail_nm);
            spotText = (TextView) findViewById(R.id.spot_detail_text);
            findRoad = (TextView) findViewById(R.id.spot_find_road);
            act.findViewById(R.id.back_btn).setOnClickListener(backClickListener);
            act.findViewById(R.id.spot_find_road).setOnClickListener(frClickListener);

            for(int i = 0 ; i < poiDataList.size(); i++) {
                if( beaconId == Integer.parseInt(poiDataList.get(i).beaconId)) {
                    spotNm.setText(poiDataList.get(i).contentsTitle);
                    spotText.setText((poiDataList.get(i).contentsText));
                    break;
                }
            }

            slideGallery = (Gallery)findViewById(R.id.gallery1);
            //adapter.notifyDataSetChanged();
            subText = (TextView) findViewById(R.id.sub_image_text);

            slideGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) { // 선택되었을 때 콜백메서드
                    subText.setText(SubDataList.get(position).contentsDText);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            // 디테일 이미지 클릭시 이미지 확대
            slideGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ThemeTour.this, ZoomImg.class);
                    intent.putExtra("img_name", SubDataList.get(position).contentsDImg);
                    intent.putExtra("contents_text", SubDataList.get(position).contentsDText);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            });

            isClicked = true;
        }
        Log.d("Oldtown","   Clicked => getItemName  " + isClicked);
    }

    // CalloutBalloonAdapter 인터페이스 구현
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            //((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.ic_launcher);
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            //((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    ImageView.OnClickListener locationClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isTrackingRunning) {
                chkGpsService();

                Toast.makeText(ThemeTour.this, "현재 위치를 표시합니다.", Toast.LENGTH_SHORT).show();
                LocationBtn.setImageResource(R.drawable.marker_gps_clicked);
                //find_Location();
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                mMapView.setShowCurrentLocationMarker(true);

                //mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude), true);
                isTrackingRunning = false;
            } else {
                Toast.makeText(ThemeTour.this, "현재 위치 표시 기능을 종료합니다.", Toast.LENGTH_SHORT).show();
                LocationBtn.setImageResource(R.drawable.marker_gps);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                mMapView.setShowCurrentLocationMarker(false);
                isTrackingRunning = true;
            }


        }
    };

    //GPS 설정 체크
    private boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            locationDialog = new AlertDialog.Builder(ThemeTour.this);
            locationDialog.setCancelable(false);
            //locationDialog.setIcon(R.drawable.setting_icon);
            locationDialog.setTitle(Html.fromHtml("<font color='#6D5652'>위치 서비스 설정</font>"));
            locationDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            //locationDialog.setView(linear);
            locationDialog.setNegativeButton("닫기", null);
            locationDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;
        } else {
            return true;
        }
    }

    Button.OnClickListener otClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (isTrackingRunning == false) { //현위치 트래킹모드가 실행 중이면 off 시킴
                Log.d("Oldtown", "   isTrackingRunning -> "+isTrackingRunning);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                mMapView.setShowCurrentLocationMarker(false);
                LocationBtn.setImageResource(R.drawable.marker_gps);
                isTrackingRunning = true;
            }

            Intent intent = new Intent(ThemeTour.this, CourseTour.class);
            startActivity(intent);
        }
    };

    LinearLayout.OnClickListener backClickListener = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //뷰제거
            isClicked = false;
        }
    };

    TextView.OnClickListener frClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Double.parseDouble(poiDataList.get(position).beaconX), Double.parseDouble(poiDataList.get(position).beaconY)
            try {
                for (int i=0; i<poiDataList.size(); i++) {
                    if (spotNm.getText().equals(poiDataList.get(i).contentsTitle) ){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("daummaps://route?sp=" + lat + "," + lon + "&ep=" + poiDataList.get(i).beaconX + "," + poiDataList.get(i).beaconY + "&by=FOOT"));
                        startActivity(intent);
                        Log.d("Oldtown", "길찾기 -> " + Double.parseDouble(poiDataList.get(i).beaconX));
                    }
                }
            } catch (Exception e) {
                Log.d("CE", "ㅇㅇㅇㅇㅇㅇㅇㅇㅇ" + e);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=net.daum.android.map"));
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i("Oldtown", "     onCurrentLocationUpdate      ->" + String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, v));
        Log.d("Oldtown", "------------------- ");
        Log.d("Oldtown", "   X좌표   -> " + mapPointGeo.latitude + "   Y좌표   -> " + mapPointGeo.longitude);
        Log.d("Oldtown", "------------------- ");
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        mapPointGeo = mapPoint.getMapPointGeoCoord();
        //Log.i("Oldtown", "     onMapViewSingleTapped      ->" + String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude));
        Log.d("Oldtown", "------------------- ");
        Log.d("Oldtown", "   onMapViewSingleTapped    X좌표   -> " + mapPointGeo.latitude + "   Y좌표   -> " + mapPointGeo.longitude);
        Log.d("Oldtown", "------------------- ");

        if (isClicked == true) {
            ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //뷰제거
            isClicked = false;
            Log.d("Oldtown","   Clicked => 닫힘  ");
        }

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.right_menu, menu);
        return true;
    }

    public void find_Location() {

        Log.i("Oldtown", "in find_location");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {


            public void onLocationChanged(Location location) {

                gpsX = "";
                gpsY = "";

                gpsX = String.valueOf(location.getLatitude());
                gpsY = String.valueOf(location.getLongitude());

                Log.d("Oldtown", "before gpsX : " + gpsX);
                Log.d("Oldtown", "before gpsY : " + gpsY);

                int index1 = gpsX.indexOf(".");
                int index2 = gpsY.indexOf(".");

                String tempGpsX = gpsX.substring(0,index1);
                if(gpsX.length() > 9) {
                    tempGpsX += gpsX.substring(index1, index1 +7);
                    Log.d("Oldtown", "Length()1 : " + gpsX.length());
                } else
                    tempGpsX = gpsX;

                String tempGpsY = gpsY.substring(0,index2);

                if(gpsY.length() > 9) {
                    tempGpsY += gpsY.substring(index2, index2 + 7);
                    Log.d("Oldtown", "Length()2 : " + gpsY.length());
                } else
                    tempGpsY = gpsY;
//				tempGpsY += gpsY.substring(index2,  index2 + 7);

                gpsX = tempGpsX;
                gpsY = tempGpsY;

                Log.d("Oldtown", "after gpsX : " + gpsX);
                Log.d("Oldtown", "after gpsY : " + gpsY);

                locationLat = Double.parseDouble(gpsX);
                locationLon = Double.parseDouble(gpsY);
                Log.d("Oldtown", "------------------- ");
                Log.d("Oldtown", "   X좌표   -> " + locationLat + "   Y좌표   -> " + locationLon);
                Log.d("Oldtown", "------------------- ");


//				Log.i("Oldtown", "in onLocationChanged - GPS1 IS : " + gpsX);
//				Log.i("Oldtown", "in onLocationChanged - GPS2 IS : " + gpsY);

                locationManager.removeUpdates(locationListener);
            }

            public void onProviderDisabled(String provider) {
                // Log.i("log", "onProviderDisabled");

                useGps = false;

                if (!firstCheck) {
                    firstCheck = true;
                    //gpsSetting(ShcsAppIntro.this);
                }
            }

            public void onProviderEnabled(String provider) {

                useGps = true;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            final LinearLayout linear = (LinearLayout) View.inflate(this, R.layout.settings_detail, null);

            course_alertbox = new AlertDialog.Builder(ThemeTour.this);
            course_alertbox.setCancelable(false);
            //course_alertbox.setIcon(R.drawable.setting_icon);
            course_alertbox.setTitle(Html.fromHtml("<font color='#6D5652'>비콘 신호 수신 설정</font>"));
            course_alertbox.setView(linear);
            course_alertbox.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    course_alertbox = null;
                    Log.d("Oldtown","   닫기 에서 odtown_alertbox   null ? ->" + course_alertbox);
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
                        Log.d("Oldtown","푸시  켜짐");
                    } else { // OFF

                        editor.putString("push", "0");
                        editor.commit();
                        Constants.pushState = "0";
                        Log.d("Oldtown","푸시  꺼짐");
                    }
                }
            });

            AlertDialog alertDialog = course_alertbox.create();
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 다이얼로그 생성시 뒷배경 어둡게 처리
            alertDialog.setCanceledOnTouchOutside(false); //다이얼로그 밖 터치시 다이얼로그 닫기 막음 (true이면 닫힘)
            alertDialog.show();

            /*IntentFilter btFilter = new IntentFilter();
            btFilter.addAction(Constants.btAdapter.ACTION_STATE_CHANGED);
            Constants.btOnReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Constants.btIsReceiverRegistered = true;
                    //unregisterReceiver(scrOffReceiver);
                    blueToothStateCheck();
                }
            };
            registerReceiver(Constants.btOnReceiver, btFilter);*/

            //registerReceiver();

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

    public void  BeaconScanning(long spotId) {

        Log.d("Oldtown", "CourseTour => 감지된 spotId ::::: " + spotId);

        // 비콘 id가 0이 아닐경우 진입
        if(spotId != 0) {
            //비콘 id가 0이 아니면서 매핑된 컨텐츠가 있을 경우 진입

            Log.d("Oldtown", "CourseTour => onBeaconId   " + spotId);

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

//            int k = RelatedDataList.size();
//            Log.d("Oldtown","        RelatedDataList.size()           -> " + RelatedDataList.size());
//            Log.d("Oldtown","        (k-1)           -> " + (k-1));
            spotRelatedCourse.setText(null);
            relatedCourseName = "연관코스 : ";
            Log.d("Oldtown","        relatedCourseName = null;           -> " + relatedCourseName);

            for (int j=0; j<RelatedDataList.size(); j++) {

                if ((RelatedDataList.size() - 1) == j){ //배열의 마지막일 때
                    relatedCourseName += RelatedDataList.get(j).courseMNm;
                } else {
                    relatedCourseName += RelatedDataList.get(j).courseMNm + ", ";
                }
            }

            Log.d("Oldtown","        relatedCourseName            -> " + relatedCourseName);

            spotRelatedCourse.setText(relatedCourseName);
            Log.d("Oldtown","        RelatedDataList.size() 전          -> " + RelatedDataList.size());
            Log.d("Oldtown","        RelatedDataList.size() 후          -> " + RelatedDataList.size());

        }
    }

    public class RelatedCourseForBeaconGetDataJSON extends AsyncTask<String, String, String> {

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

                    RelatedForBeaconVO relatedForBeaconVO = new RelatedForBeaconVO();
                    relatedForBeaconVO.courseMNo = c.get("courseMNo").toString();
                    relatedForBeaconVO.courseMNm = c.get("courseMNm").toString();
                    //courseMNm = c.get("courseMNm").toString();
                    Log.d("Oldtown", "   RelatedVO.courseMNm 연관코스 데이터   -> " + relatedForBeaconVO.courseMNm);

                    RelatedDataListForBeacon.add(relatedForBeaconVO);

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

            Log.d("Oldtown","CourseTour => uri : " + uri);
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                    Log.d("Oldtown","CourseTour => json   " + json);
                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Oldtown", "CourseTour => result : " + result);


            JSONObject jsonRes = null;

            if(result == null) {
                Log.d("Oldtown", "CourseTour => result : null");

            } else {

                try{

                    jsonRes = new JSONObject(result);

                    MappingVO mappingVO = new MappingVO();
                    mappingVO.contentsNo = jsonRes.getString("contentsNo");
                    mappingVO.contentsTitle = jsonRes.getString("contentsTitle");
                    mappingVO.filePath = jsonRes.getString("filePath");
                    mappingVO.beaconX = jsonRes.getString("beaconX");
                    mappingVO.beaconY = jsonRes.getString("beaconY");
                    MappingDataList.add(mappingVO);
                    Log.d("Oldtown", "CourseTour => mappingVO.contentsTitle  매핑데이터 -> " + mappingVO.contentsTitle);
                    Log.d("Oldtown", "CourseTour => result : " + result);
                    fileUrl = jsonRes.getString("filePath");
                    contentsNo = jsonRes.getString("contentsNo");
                    titleNm = jsonRes.getString("contentsTitle");
                    Log.d("Oldtown", "CourseTour => fileUrl1 : " + fileUrl);

                    Log.d("Oldtown", "CourseTour => fileUrl 2 : " + fileUrl);
                    if(!"FALSE".equals(contentsNo))
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
        Log.d("Oldtown", "RelatedDataList 클리어 전 사이즈 =>  : " + RelatedDataListForBeacon.size());
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

        for (int i=0; i<RelatedDataListForBeacon.size(); i++) {
            Log.d("Oldtown","---------------showList에서 받은 비콘 ID ---------------->" + spotId);
            title = MappingDataList.get(0).contentsTitle;
            Log.d("Oldtown","---------------title ---------------->" + title);
            x = MappingDataList.get(0).beaconX;
            Log.d("Oldtown","---------------x ---------------->" + x);
            y = MappingDataList.get(0).beaconY;
            Log.d("Oldtown","---------------y ---------------->" + y);
            mno = RelatedDataListForBeacon.get(i).courseMNo;
            Log.d("Oldtown","---------------mno ---------------->" + mno);
            mnm = RelatedDataListForBeacon.get(i).courseMNm;
            Log.d("Oldtown","---------------mnm ---------------->" + mnm);
            dbHelper.insert(id, title, x, y, mno, mnm, bDate); // 로컬 DB에 인서트
            Log.d("Oldtown", "CourseTour => DB 인서트 됨?  ->");
            Log.d("Oldtown", "CourseTour => DB 인서트    ->  " + i + " 회   " + dbHelper.getResult().toString());
        }

        //로컬DB 끝

        if(alertbox != null) {
            mPopupDlg.dismiss();
            alertbox = null;
            mPopupDlg = null;
        }

        alertbox =  new AlertDialog.Builder(ThemeTour.this);
        alertbox.setTitle("비콘 신호 감지");
        alertbox.setMessage("페이지를 여시겠습니까?");
        alertbox.setIcon(R.drawable.ic_menu_gallery);
        alertbox.setCancelable(false); // back 버튼 막음
        alertbox.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ThemeTour.this, PageOpen.class);
//                    intent.putExtra("value", "http://218.157.145.72:10/creativeEconomy/MobileCommonPage.do?contentsNo="+contentsNo);
                intent.putExtra("value", fileUrl);
                intent.putExtra("PageNm", "ThemeTour");
                intent.putExtra("titleNm", titleNm);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
        alertbox.setNegativeButton("아니오", null);
        mPopupDlg = alertbox.show();

        Log.d("Oldtown", "RelatedDataListForBeacon 클리어 전 사이즈 =>  : " + RelatedDataListForBeacon.size());
        RelatedDataListForBeacon.clear();
        Log.d("Oldtown", "RelatedDataListForBeacon 클리어 후 사이즈 =>  : " + RelatedDataListForBeacon.size());
        Log.d("Oldtown", "-----------------------------------------------  : ");

        Log.d("Oldtown", "MappingDataList 클리어 전 사이즈 =>  : " + MappingDataList.size());
        MappingDataList.clear();
        Log.d("Oldtown", "MappingDataList 클리어 후 사이즈 =>  : " + MappingDataList.size());
    }

    /* 블루투스 상태 체크하여 스위치 on/off 시킴 */
    public void blueToothStateCheck() {
        Log.d("Oldtown","   blueToothStateCheck 에서 odtown_alertbox   null ? ->" + course_alertbox);
        if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_TURNING_OFF) {// 블루투스가 켜져있을 경우 스위치 on

            //설정 창이 뜬 상태일 때
            if (course_alertbox != null) {
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

            if (course_alertbox != null) {
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

        Log.d("Oldtown","CourseTour => onDestroy 호출");
        MyApplication.appRun = "0";

        LocationBtn.setImageResource(R.drawable.marker_gps);
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);
        isTrackingRunning = true;

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);

        stopService(intent);
        StopMonitoring();

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        Log.d("Oldtown","CourseTour => onPause 호출");

        /*LocationBtn.setImageResource(R.drawable.marker_gps);
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);
        isTrackingRunning = true;*/

        mapFrame.removeView(mMapView);

        StopMonitoring();

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);
        intent.putExtra("PageNm", "MAIN");
        startService(intent);
        Log.d("Oldtown","CourseTour => startService111111111111");

        if (asyncDialog != null) {
            asyncDialog.dismiss();
        }

        unregisterReceiver();

        super.onPause();
    }

    @Override
    protected void onResume() {

        Log.d("Oldtown","CourseTour => onResume 호출");
        MyApplication.appRun = "1";

        mapFrame = (FrameLayout) findViewById(R.id.theme_mapview_frame);
        mMapView = new MapView(this);
        mapFrame.addView(mMapView);

        //mMapView = (MapView)findViewById(R.id.mapview);
        mMapView.setDaumMapApiKey(Constants.mapKey);
        mMapView.setMapViewEventListener(this);
        mMapView.setCurrentLocationEventListener(this);
        mMapView.setShowCurrentLocationMarker(false);

        mMapView.setCalloutBalloonAdapter(new ThemeTour.CustomCalloutBalloonAdapter());

        if (spinnerPosition !=0) { //코스를 선택한 상태라면
            markerChange();
        }

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);

        stopService(intent);

        if("1".equals(Constants.bluetoothState))
            StartMonitoring();
        else if("0".equals(Constants.bluetoothState))
            StopMonitoring();

        registerReceiver();

        super.onResume();
    }

    @Override
    protected void onRestart() {

        Log.d("Oldtown","CourseTour => onRestart())))))))))))))))))))))))))))))");

        super.onRestart();
    }

    //디테일볼때 백키 누르면 디테일만 사라지고, 한 번 더 백키누르면 종료
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (isClicked == true) {
            ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //디테일 뷰 제거
            isClicked = false;
        } else {
            finish();
        }
    }

    private final TamraObserver tamraObserver = new TamraObserver() {
        @Override
        public void didEnter(Region region) {

            Log.d("Oldtown","CourseTour => ----didEnter ----"+region.name()+"에 진입");
        }

        @Override
        public void didExit(Region region) {

            Log.d("Oldtown","CourseTour => ----didExit ----"+region.name()+"에서 이탈");
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
                        RelatedCourseForBeaconGetDataJSON r = new RelatedCourseForBeaconGetDataJSON();
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