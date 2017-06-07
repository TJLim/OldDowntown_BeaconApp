package com.example.jinwoo.olddowntown_beaconapp.trace;

import android.app.Activity;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.common.BackgroundService;
import com.example.jinwoo.olddowntown_beaconapp.common.MappingCheckClass;
import com.example.jinwoo.olddowntown_beaconapp.main.Constants;
import com.example.jinwoo.olddowntown_beaconapp.tamra.PageOpen;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.firstCheck;
import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.locationListener;
import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.locationManager;
import static com.example.jinwoo.olddowntown_beaconapp.MyApplication.useGps;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.FILE_PATH;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbName;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbVersion;

/**
 * Created by TJ on 2017-03-02.
 */

public class TraceMap extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, MapView.CurrentLocationEventListener{

    private MapView mMapView;
    MapPOIItem marker;

    String poiListUrl = "";
    ArrayList<PoiVO> poiDataList = new ArrayList<PoiVO>();
    ArrayList<String> tempList = new ArrayList<String>(); //코스 이름만 따로 배열 생성
    ArrayList<String> imgList = new ArrayList<String>(); //코스 이름만 따로 배열 생성

    AlertDialog.Builder course_alertbox;
    public static Switch sw1, sw2;
    SharedPreferences pref;
    public SharedPreferences.Editor editor = null;
    private BroadcastReceiver btOnReceiver;

    ActionBar actionBar;

    LinearLayout poiDetail, traceListDetailOpen, traceListDetailClose;

    public boolean isVisible = true;
    public boolean isClicked = false;
    public boolean isTrackingRunning = true;

    Activity act;
    ImageView LocationBtn;

    ArrayList<Bitmap> BitmapList;

    ArrayList<MapPOIItem> poiList = new ArrayList<MapPOIItem>();

    MapPoint.GeoCoordinate mapPointGeo;

    public double locationLat, locationLon;
    public String gpsX = "";
    public String gpsY = "";

    MappingCheckClass check;
    String relatedCourseUrl = "";
    ArrayList<RelatedVO> RelatedDataList = new ArrayList<RelatedVO>();
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

    String getCourseStr, getCourseStrNm;

    boolean poiListIsClicked = false;

    CommonVO commonVO = new CommonVO();
    String localIp = commonVO.localIp;
    String serverIp = commonVO.serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trace_map);

        // 맵위에 "현위치" 기능 버튼의 레이아웃을 겹침
        Window win = getWindow();
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout)inflater.inflate(R.layout.over_layout, null);
        LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        win.addContentView(linear, paramlinear);//이 부분이 레이아웃을 겹치는 부분
        //add는 기존의 레이아웃에 겹쳐서 배치하라는 뜻이다.

        // 맵위에 "현위치" 기능 버튼의 레이아웃을 겹침
        detailWin = getWindow();
        detailInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        detailLinear = (LinearLayout)detailInflater.inflate(R.layout.trace_list_detail, null);
        detailParamlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //detailWin.addContentView(detailLinear, detailParamlinear);//이 부분이 레이아웃을 겹치는 부분
        //((ViewManager) detailLinear.getParent()).removeView(detailLinear); //뷰제거
        //add는 기존의 레이아웃에 겹쳐서 배치하라는 뜻이다.

        setResources();
        init();

        mMapView = (MapView)findViewById(R.id.mapview);
        mMapView.setDaumMapApiKey(Constants.mapKey);
        mMapView.setMapViewEventListener(this);
        mMapView.setCurrentLocationEventListener(this);
        mMapView.setShowCurrentLocationMarker(true);
        mMapView.setCalloutBalloonAdapter(new TraceMap.CustomCalloutBalloonAdapter());

        dbHelper = new DBHelper(getApplicationContext(), FILE_PATH+dbName, null, dbVersion);

        CoursePoiGetDataJSON pg = new CoursePoiGetDataJSON();
        pg.execute(poiListUrl);
    }


    public class CoursePoiGetDataJSON extends AsyncTask<String, String, ArrayList<Bitmap>> {

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

                Log.d("GetJson", "   JSON 1   " + sb.toString().trim());

                JSONArray jsonArray = new JSONArray(sb.toString().trim());

                Log.d("GetJson", "   jsonArray   " + jsonArray);

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

                    Log.d("GetJson", "   poivo.courseMNo   " + poivo.courseMNo);

                    tempList.add(poivo.contentsTitle);
                    poiDataList.add(poivo);
                    imgList.add(poivo.contentsImg);

                    /*String s = poivo.contentsImg.substring(8);
                    Log.d("Oldtown", "   substring   " + s);
                    imgList.add(s);*/

                    Log.d("Oldtown", "   poiDataList    -> " + poiDataList.size() + "   tempList    -> " + tempList.size());
                    Log.d("Oldtown", "   poiList add 후에 size   " + poiDataList.size());
                }

  /*              BitmapList = new ArrayList<Bitmap>();
                for (int i=0;i<imgList.size();i++) {

                    String urlEncode = URLEncoder.encode( imgList.get(i), "UTF-8" );
                    urlEncode = urlEncode.replace("%2F","/");

                    try{
                        bitmap = downloadUrl("http://www.jeju-showcase.com" + urlEncode);
                        BitmapList.add(bitmap);
                        Log.d("Oldtown", "   downloadUrl   " +bitmap);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }*/
                //return sb.toString().trim();
                return BitmapList;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> result) {

            if (result != null){
                super.onPostExecute(result);

            } else {
                Log.i("Oldtown", "[onPostExecute( ) ] Image downloading failed !!!!!!!!!!!!!!!!!!! ");
            }

            markerChange();

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
        Intent intent =getIntent();
        getCourseStr = intent.getExtras().getString("Course");
        getCourseStrNm = intent.getExtras().getString("CourseNm");
        setTitle("나의 발자취 : " + getCourseStrNm);

        Constants.btAdapter = BluetoothAdapter.getDefaultAdapter();     //블루투스 어댑터

        pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        Constants.pushState = pref.getString("push", "");

        act = this;

        poiDetail = (LinearLayout) findViewById(R.id.tour_balloon);
        LocationBtn = (ImageView) findViewById(R.id.location_btn);
        traceListDetailOpen = (LinearLayout) findViewById(R.id.trace_list_detail_open);
        traceListDetailClose = (LinearLayout) findViewById(R.id.trace_list_detail_close);

        poiListUrl = serverIp + "/creativeEconomy/OldTownCourseData.do?userKey=nndoonw&courseMNo=" + getCourseStr;
        Log.d("Oldtown", "   poiListUrl  ->" + poiListUrl);
    }

    /* 초기화 함수 */
    public void init() {

        actionBar = getSupportActionBar();

        //메뉴바에 '<' 버튼이 생긴다.(두개는 항상 같이다닌다)
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.to_home);

        act.findViewById(R.id.location_btn).setOnClickListener(locationClickListener);
        act.findViewById(R.id.trace_list_detail_open).setOnClickListener(TraceListOpenListener);
        //act.findViewById(R.id.trace_list_detail_close).setOnClickListener(TraceListCloseListener);
    }

    public void markerChange() {

        Log.d("Oldtown", "   poiList.size()+1  " + poiDataList.size());

        //mMapView.removePOIItem(marker);

        mMapView.removeAllPOIItems();

        mMapView.setPOIItemEventListener(this);

        PoiVO poivo = new PoiVO();

        Log.d("Oldtown", "   poiList.size()+2  " + poiDataList.size());
        for (int i=0; i<poiDataList.size(); i++) {
            Log.d("Oldtown", "   for - poiDataList.size()  " + poiDataList.size());
            Log.d("Oldtown", "   poiList X " + poiDataList.get(i).beaconX);
            Log.d("Oldtown", "   poiList Y" + poiDataList.get(i).beaconY);
            Log.d("Oldtown", "   poiList Y" + poiDataList.get(i).courseMNo);

            poivo.beaconId = poiDataList.get(i).beaconId;

            dbHelper.BeaconIdGetResult(poivo);

            marker = new MapPOIItem();

            marker.setTag(Integer.parseInt(poiDataList.get(i).beaconId));
            marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(poiDataList.get(i).beaconX), Double.parseDouble(poiDataList.get(i).beaconY)));
            //marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.

            Log.d("Oldtown", "   visiteYn: " + poivo.visiteYn);
            Log.d("Oldtown", "   regDt: " + poivo.regDt);

            if (poivo.visiteYn.equals("방문 : ")){
                marker.setCustomImageResourceId(R.drawable.marker_star); // 마커 이미지.
                marker.setItemName(poiDataList.get(i).contentsTitle + "\n" + poivo.visiteYn + poivo.regDt);
            } else {
                marker.setCustomImageResourceId(R.drawable.marker_red_notvisit); // 마커 이미지.
                marker.setItemName(poiDataList.get(i).contentsTitle + "\n" + poivo.visiteYn);
            }
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.BluePin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
            marker.setCustomImageAnchorPointOffset(new MapPOIItem.ImageOffset(30,0));
            marker.setShowDisclosureButtonOnCalloutBalloon(false); //POI 클릭시, '>' 모양 안 나타게
            poiList.add(marker);
            mMapView.addPOIItem(marker);

            Log.d("Oldtown", "끝");
        }
        mMapView.fitMapViewAreaToShowAllPOIItems();
        mMapView.setZoomLevel(3, true);
        Log.d("Oldtown", "      markerChange()       poiDataList.clear();" + poiDataList.size());
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(33.512801, 126.523920), true);

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

        //POI 클릭시 해당 POI를 화면 중앙으로 잡아줌
        for( int i = 0 ; i < poiDataList.size(); i++) {
            if( Integer.parseInt(poiDataList.get(i).beaconId) == mapPOIItem.getTag()) {
                mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(poiDataList.get(i).beaconX), Double.parseDouble(poiDataList.get(i).beaconY)), true);
            }
        }

        Log.d("Oldtown","mapPOIItemmapPOIItemmapPOIItemmapPOIItemmapPOIItem ::::: " + mapPOIItem );

        beaconId = mapPOIItem.getTag();

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

    LinearLayout.OnClickListener locationClickListener = new LinearLayout.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isTrackingRunning) {
                chkGpsService();

                Toast.makeText(TraceMap.this, "현재 위치를 표시합니다.", Toast.LENGTH_SHORT).show();
                LocationBtn.setImageResource(R.drawable.marker_gps_clicked);

                //find_Location();
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                mMapView.setShowCurrentLocationMarker(true);

                //mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude), true);
                isTrackingRunning = false;
            } else {
                Toast.makeText(TraceMap.this, "현재 위치 표시 기능을 종료합니다.", Toast.LENGTH_SHORT).show();
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
            locationDialog = new AlertDialog.Builder(TraceMap.this);
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

    LinearLayout.OnClickListener TraceListOpenListener = new LinearLayout.OnClickListener() { //목록 열기
        @Override
        public void onClick(View v) {

            PoiVO poiVO = new PoiVO();

            if (poiListIsClicked == false) {

                detailWin.addContentView(detailLinear, detailParamlinear);//이 부분이 레이아웃을 겹치는 부분
                traceListDetailClose = (LinearLayout) findViewById(R.id.trace_list_detail_close);
                act.findViewById(R.id.trace_list_detail_close).setOnClickListener(TraceListCloseListener);


                VisitListAdapter visitListAdapter;
                visitListAdapter = new VisitListAdapter();

                ListView VisitListView;
                VisitListView = (ListView) findViewById(R.id.visit_listview);
                VisitListView.setAdapter(visitListAdapter);

                for (int i=0; i<poiDataList.size(); i++) {

                    poiVO.beaconId = poiDataList.get(i).beaconId;

                    dbHelper.BeaconIdGetResult(poiVO);

                    Log.d("Oldtown", "poiDataList.get(i).contentsTitle -> " + poiVO.beaconId);

                    if (poiDataList.get(i).beaconId.equals(poiVO.beaconId)){
                        visitListAdapter.addItem("- " + poiDataList.get(i).contentsTitle + " (방문)");
                    } else {
                        visitListAdapter.addItem("- " + poiDataList.get(i).contentsTitle);
                    }
                    Log.d("Oldtown", "visitListAdapter.addItem  장소명 -> " + poiDataList.get(i).contentsTitle);
                }
                VisitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d("Oldtown","VisitListView.setOnItemClickListener ->" + position);
                        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(poiDataList.get(position).beaconX), Double.parseDouble(poiDataList.get(position).beaconY)), true);
                        mMapView.selectPOIItem(poiList.get(position), false);
                    }
                });

                poiListIsClicked = true;
                Log.d("Oldtown", "PoiListClickListener -> " + poiListIsClicked + "디테일 보여줌");
            }
        }
    };

    LinearLayout.OnClickListener TraceListCloseListener = new LinearLayout.OnClickListener() { //목록 닫기
        @Override
        public void onClick(View v) {
            if (poiListIsClicked == true) {
                ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //뷰제거
                poiListIsClicked = false;
                Log.d("Oldtown", "PoiListClickListener -> " + poiListIsClicked + "디테일 끔");
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

        if (poiListIsClicked) {
            ((ViewManager) detailLinear.getParent()).removeView(detailLinear); //뷰제거
            poiListIsClicked = false;
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

            course_alertbox = new AlertDialog.Builder(TraceMap.this);
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

    public void BeaconScanning(long spotId) {

        Log.d("Oldtown", "TraceMap => 감지된 spotId ::::: " + spotId);

        // 비콘 id가 0이 아닐경우 진입
        if (spotId != 0) {
            //비콘 id가 0이 아니면서 매핑된 컨텐츠가 있을 경우 진입

            Log.d("Oldtown", "TraceMap => onBeaconId   " + spotId);

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
                Log.d("Oldtown", "   dbHelper.delete(String.valueOf(spotId));    1" + spotId);
                dbHelper.delete(String.valueOf(spotId));
                Log.d("Oldtown", "   dbHelper.delete(String.valueOf(spotId));    2" + spotId);
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

            Log.d("Oldtown", "TraceMap => uri : " + uri);
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                    Log.d("Oldtown", "TraceMap => json   " + json);
                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Oldtown", "TraceMap => result : " + result);


            JSONObject jsonRes = null;

            if (result == null) {
                Log.d("Oldtown", "TraceMap => result : null");

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
                    Log.d("Oldtown", "TraceMap => mappingVO.contentsTitle  매핑데이터 -> " + mappingVO.contentsTitle);
                    Log.d("Oldtown", "TraceMap => result : " + result);
                    fileUrl = mappingVO.filePath;
                    contentsNo = jsonRes.getString("contentsNo");
                    titleNm = jsonRes.getString("contentsTitle");
                    Log.d("Oldtown", "TraceMap => fileUrl1 : " + fileUrl);

                    Log.d("Oldtown", "TraceMap => fileUrl 2 : " + fileUrl);
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
            Log.d("Oldtown", "TraceMap => DB 인서트 됨?  ->");
            Log.d("Oldtown", "TraceMap => DB 인서트    ->  " + i + " 회   " + dbHelper.getResult().toString());
        }

        //로컬DB 끝

        if (alertbox != null) {
            mPopupDlg.dismiss();
            alertbox = null;
            mPopupDlg = null;
        }

        alertbox = new AlertDialog.Builder(TraceMap.this);
        alertbox.setTitle("비콘 신호 감지");
        alertbox.setMessage("페이지를 여시겠습니까?");
        alertbox.setIcon(R.drawable.ic_menu_gallery);
        alertbox.setCancelable(false); // back 버튼 막음
        alertbox.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(TraceMap.this, PageOpen.class);
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

            Log.d("Oldtown", "TraceMap => ----didEnter ----" + region.name() + "에 진입");
        }

        @Override
        public void didExit(Region region) {

            Log.d("Oldtown", "TraceMap => ----didExit ----" + region.name() + "에서 이탈");
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

        StopMonitoring();

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);
        intent.putExtra("PageNm", "MAIN");
        startService(intent);
        Log.d("Oldtown","CourseTour => startService111111111111");

        unregisterReceiver();

        super.onPause();
    }

    @Override
    protected void onResume() {

        Log.d("Oldtown","CourseTour => onResume 호출");
        MyApplication.appRun = "1";

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

}