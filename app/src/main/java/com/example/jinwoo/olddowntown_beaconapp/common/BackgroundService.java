package com.example.jinwoo.olddowntown_beaconapp.common;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.main.Constants;
import com.example.jinwoo.olddowntown_beaconapp.tamra.PageOpen;
import com.example.jinwoo.olddowntown_beaconapp.trace.DBHelper;
import com.example.jinwoo.olddowntown_beaconapp.vo.CommonVO;
import com.example.jinwoo.olddowntown_beaconapp.vo.MappingVO;
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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.FILE_PATH;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbName;
import static com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace.dbVersion;

/**
 * Created by chihong on 2017-01-17.
 */

public class BackgroundService extends Service{

    Context con;
    String PageNm="";

    MappingCheckClass check;
    String fileUrl="";
    String contentsNo;
    String titleNm="";

    long spotId;
    String relatedCourseUrl = "";
    String mappingUrl = "";
    ArrayList<RelatedVO> RelatedDataList = new ArrayList<RelatedVO>();
    ArrayList<MappingVO> MappingDataList = new ArrayList<MappingVO>();
    DBHelper dbHelper;

    CommonVO commonVO = new CommonVO();
    String localIp = commonVO.localIp;
    String serverIp = commonVO.serverIp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Oldtown", "BackgroundService => onCreate()");
        dbHelper = new DBHelper(getApplicationContext(), FILE_PATH + dbName, null, dbVersion);
        unregisterRestartAlarm();
        
        con = getApplicationContext();

        if (Constants.pushState.equals("0")) {
            StopMonitoring();
        } else {
            StartMonitoring();
        }
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        PageNm = arg0.getStringExtra("PageNm");

        Log.d("Oldtown", "BackgroundService => onBind()");
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Log.d("Oldtown", "BackgroundService => onStart()");
        Log.d("Oldtown", "BackgroundService => MyApplication.appRun : " + MyApplication.appRun);
        if (Constants.pushState.equals("0")) {
            StopMonitoring();
        } else {
            StartMonitoring();
        }

    }

    @Override
    public void onDestroy()
    {
        if (MyApplication.appRun.equals("1")) { //앱이 켜졌을 때
            Log.d("Oldtown", "BackgroundService => MyApplication.appRun == 1: " + MyApplication.appRun);
            unregisterRestartAlarm();
        } else if (MyApplication.appRun.equals("0")){   //앱이 백그라운드 혹은 꺼졌을 때
            Log.d("Oldtown", "BackgroundService => MyApplication.appRun == 2: " + MyApplication.appRun);
            registerRestartAlarm();
            if (Constants.pushState.equals("0")) {
                StopMonitoring();
            } else {
                StartMonitoring();
            }
        }
        Log.d("Oldtown", "BackgroundService => onDestroy()");
        StopMonitoring();
        super.onDestroy();
    }
    
    void registerRestartAlarm() {
        Log.d("Oldtown", "registerRestartAlarm");
        Intent intent = new Intent(BackgroundService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                BackgroundService.this, 0, intent, 0); // 브로드케스트할 Intent
        long firstTime = SystemClock.elapsedRealtime();  // 현재 시간
        firstTime += 1 * 1000; // 10초 후에 알람이벤트 발생
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE); // 알람 서비스 등록
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,
                10 * 1000, sender); // 알람이

    }

    void unregisterRestartAlarm() {
        Log.d("Oldtown", "unregisterRestartAlarm");
        Intent intent = new Intent(BackgroundService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                BackgroundService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);

    }

    private final TamraObserver tamraObserver = new TamraObserver() {
        @Override
        public void didEnter(Region region) {

            Log.d("Oldtown", "BackgroundService => ----didEnter ----" + region.name() + "에 진입");
        }

        @Override
        public void didExit(Region region) {

            Log.d("Oldtown", "BackgroundService => ----didExit ----" + region.name() + "에서 이탈");
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

    public void BeaconScanning(long spotId) {

        Log.d("Oldtown", "BackgroundService => 감지된 spotId ::::: " + spotId);

        // 비콘 id가 0이 아닐경우 진입
        if (spotId != 0) {
            //비콘 id가 0이 아니면서 매핑된 컨텐츠가 있을 경우 진입

            Log.d("Oldtown", "BackgroundService => onBeaconId   " + spotId);

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

            Log.d("Oldtown", "BackgroundService => uri : " + uri);
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                    Log.d("Oldtown", "BackgroundService => json   " + json);
                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Oldtown", "BackgroundService => result : " + result);


            JSONObject jsonRes = null;

            if (result == null) {
                Log.d("Oldtown", "BackgroundService => result : null");

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
                    Log.d("Oldtown", "BackgroundService => mappingVO.contentsTitle  매핑데이터 -> " + mappingVO.contentsTitle);
                    Log.d("Oldtown", "BackgroundService => result : " + result);
                    fileUrl = mappingVO.filePath;
                    contentsNo = jsonRes.getString("contentsNo");
                    titleNm = jsonRes.getString("contentsTitle");
                    Log.d("Oldtown", "BackgroundService => fileUrl1 : " + fileUrl);

                    Log.d("Oldtown", "BackgroundService => fileUrl 2 : " + fileUrl);
                    if (!"FALSE".equals(contentsNo))
                        showList();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    public void showList() {

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
            Log.d("Oldtown", "BackgroundService => DB 인서트 됨?  ->");
            Log.d("Oldtown", "BackgroundService => DB 인서트    ->  " + i + " 회   " + dbHelper.getResult().toString());
        }

        //로컬DB 끝

        Log.d("Oldtown", "BackgroundService : " + Constants.pushState);
        if("1".equals(Constants.pushState))
            NotificationSomethings(con);

        Log.d("Oldtown", "RelatedDataList 클리어 전 사이즈 =>  : " + RelatedDataList.size());
        RelatedDataList.clear();
        Log.d("Oldtown", "RelatedDataList 클리어 후 사이즈 =>  : " + RelatedDataList.size());
        Log.d("Oldtown", "-----------------------------------------------  : ");

        Log.d("Oldtown", "MappingDataList 클리어 전 사이즈 =>  : " + MappingDataList.size());
        MappingDataList.clear();
        Log.d("Oldtown", "MappingDataList 클리어 후 사이즈 =>  : " + MappingDataList.size());
    }

    private void NotificationSomethings(Context context) {

        Log.d("Oldtown", "BackgroundService NotificationSomethings()");
        Resources res = context.getResources();

        Intent notificationIntent = new Intent(context, PageOpen.class);
        notificationIntent.putExtra("value", fileUrl); //전달할 값
        notificationIntent.putExtra("PageNm", "service"); //전달할 값
        notificationIntent.putExtra("titleNm", titleNm);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle("원도심 투어중 비콘감지!")
                .setContentText(titleNm + "에 연결된 컨텐츠를 감지하였습니다.")
                .setTicker("원도심 투어중 비콘감지!")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.app_icon))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1234, builder.build());
    }
}
