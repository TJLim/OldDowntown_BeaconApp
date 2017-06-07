package com.example.jinwoo.olddowntown_beaconapp.introduction;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.common.BackgroundService;
import com.example.jinwoo.olddowntown_beaconapp.common.MappingCheckClass;
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

public class Olddowntown_intrdc extends AppCompatActivity implements View.OnClickListener {

    ActionBar actionBar;

    DialogInterface mPopupDlg = null;
    AlertDialog.Builder alertbox, odtown_alertbox;
    public static Switch sw1, sw2;
    String realPushState;
    MappingCheckClass check;
    SharedPreferences pref;
    public SharedPreferences.Editor editor = null;
    private BroadcastReceiver btOnReceiver;

    String relatedCourseUrl = "";
    ArrayList<RelatedVO> RelatedDataList = new ArrayList<RelatedVO>();
    ArrayList<MappingVO> MappingDataList = new ArrayList<MappingVO>();
    String mappingUrl="";
    String fileUrl="";
    String contentsNo = "";
    String titleNm="";
    DBHelper dbHelper;
    long spotId;

    ImageView odMainImg;
    TextView odText;

    CommonVO commonVO = new CommonVO();
    String localIp = commonVO.localIp;
    String serverIp = commonVO.serverIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.olddowntown_layout);

        setResources();
        init();
        odMainImg.setImageResource(R.drawable.sunggupgil);
        odText.setText("제주 성안, 성내로 일컬어지는 현 제주시 원도심 일대는 과거 탐라, 조선시대를 거쳐 20세기 후반까지 제주의 정치·경제·사회·문화의 중심지였다. 1980년대 새로운 택지개발로 인해 원도심의 기능이 도심 외곽으로 분산되기 시작하면서 원도심은 제주의 중심지 기능을 잃게 되었다. 제주시 원도심은 제주 역사문화자원의 보고로 다양한 시대의 기억과 경험을 공유하고 있다. 21세기 도시재생이라는 시대적 화두 아래 원도심은 재생사업에 없어서는 안될 제주만의 도심 정체성으로 재평가 받고 있다.\n\n제주 성안, 성내로 일컬어지는 현 제주시 원도심 일대는 과거 탐라, 조선시대를 거쳐 20세기 후반까지 제주의 정치·경제·사회·문화의 중심지로 기능했다. 탐라시대에는 탐라건국 신화인 고(髙)·양(良)·부(夫) 삼성(三姓)이 활을 쏘아 각기 화살이 꽂힌 자리를 중심으로 제일도(第一徒), 제이도(第二徒), 제삼도(第三徒)로 나누어 통치하였다는 신화가 전해져오고 있다. 또한 고려시대에는 탐라총관부(현, 북초등학교 북쪽 추정)가 설치되어 원제국의 직할지로 기능했으며, 조선시대에는 전라도 제주목으로 편입되어 목관아를 중심으로 한 지방행정이 이뤄졌다. 이렇듯 원도심은 제주 역사의 산실이자 제주 행정의 중심지였던 것이다.\n이후, 해방과 한국 전쟁을 거치며 제주시는 급속한 도시화를 경험하게 된다. 이때부터 옛 목관아지 일대는 도청, 경찰서, 법원 등 각종 관공서들이 밀집한 근대 도시로서의 모습을 갖추게 되었다. 하지만 오랜 기간 제주의 중심이었던 원도심 일대는 1980년대 새로운 택지개발로 인한 도심기능의 외곽이전에 따라 점진적인 쇠퇴를 경험하게 된다.\n그럼에도 불구하고 오늘날의 제주 원도심은 다양한 역사와 문화가 공존하는 제주만의 독톡한 도시문화와 도시경관을 탄생시켰다. 도시재생이 화두인 21세기, 제주 원도심에 대한 도민의 기억과 삶의 자취는 오늘날 도시재생 사업에 없어서는 안 될 귀중한 역사문화자원으로 재조명 받고 있다.");

        findViewById(R.id.od_course_intrdc).setOnClickListener(this);

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
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.to_home);

        odMainImg = (ImageView) findViewById(R.id.od_main_img);
        odText = (TextView) findViewById(R.id.od_text);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.od_course_intrdc :
                Intent intent = new Intent(Olddowntown_intrdc.this, OlddowntownCourseList.class);
                startActivity(intent);
                break;
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

            odtown_alertbox = new AlertDialog.Builder(Olddowntown_intrdc.this);
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
                Log.d("Oldtown", "   btOnReceiver onReceive   -> " + Constants.btAdapter.isEnabled());
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

        Log.d("Oldtown", "Olddowntown_intrdc => onDestroy 호출");
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

        Log.d("Oldtown", "Olddowntown_intrdc => 감지된 spotId ::::: " + spotId);

        // 비콘 id가 0이 아닐경우 진입
        if (spotId != 0) {
            //비콘 id가 0이 아니면서 매핑된 컨텐츠가 있을 경우 진입

            Log.d("Oldtown", "Olddowntown_intrdc => onBeaconId   " + spotId);

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

            Log.d("Oldtown", "Olddowntown_intrdc => uri : " + uri);
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                    Log.d("Oldtown", "Olddowntown_intrdc => json   " + json);
                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Oldtown", "Olddowntown_intrdc => result : " + result);


            JSONObject jsonRes = null;

            if (result == null) {
                Log.d("Oldtown", "Olddowntown_intrdc => result : null");

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
                    Log.d("Oldtown", "Olddowntown_intrdc => mappingVO.contentsTitle  매핑데이터 -> " + mappingVO.contentsTitle);
                    Log.d("Oldtown", "Olddowntown_intrdc => result : " + result);
                    fileUrl = mappingVO.filePath;
                    contentsNo = jsonRes.getString("contentsNo");
                    titleNm = jsonRes.getString("contentsTitle");
                    Log.d("Oldtown", "Olddowntown_intrdc => fileUrl1 : " + fileUrl);



                    Log.d("Oldtown", "Olddowntown_intrdc => fileUrl 2 : " + fileUrl);
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
            Log.d("Oldtown", "Olddowntown_intrdc => DB 인서트 됨?  ->");
            Log.d("Oldtown", "Olddowntown_intrdc => DB 인서트    ->  " + i + " 회   " + dbHelper.getResult().toString());
        }

        //로컬DB 끝

        if (alertbox != null) {
            mPopupDlg.dismiss();
            alertbox = null;
            mPopupDlg = null;
        }

        alertbox = new AlertDialog.Builder(Olddowntown_intrdc.this);
        alertbox.setTitle("비콘 신호 감지");
        alertbox.setMessage("페이지를 여시겠습니까?");
        alertbox.setIcon(R.drawable.ic_menu_gallery);
        alertbox.setCancelable(false); // back 버튼 막음
        alertbox.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Olddowntown_intrdc.this, PageOpen.class);
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

            Log.d("Oldtown", "Olddowntown_intrdc => ----didEnter ----" + region.name() + "에 진입");
        }

        @Override
        public void didExit(Region region) {

            Log.d("Oldtown", "Olddowntown_intrdc => ----didExit ----" + region.name() + "에서 이탈");
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
