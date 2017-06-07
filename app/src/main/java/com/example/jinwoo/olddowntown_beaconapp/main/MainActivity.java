package com.example.jinwoo.olddowntown_beaconapp.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinwoo.olddowntown_beaconapp.MyApplication;
import com.example.jinwoo.olddowntown_beaconapp.R;
import com.example.jinwoo.olddowntown_beaconapp.common.BackgroundService;
import com.example.jinwoo.olddowntown_beaconapp.common.MappingCheckClass;
import com.example.jinwoo.olddowntown_beaconapp.introduction.Olddowntown_intrdc;
import com.example.jinwoo.olddowntown_beaconapp.tamra.PageOpen;
import com.example.jinwoo.olddowntown_beaconapp.tour.CourseTour;
import com.example.jinwoo.olddowntown_beaconapp.tour.ThemeTour;
import com.example.jinwoo.olddowntown_beaconapp.trace.DBHelper;
import com.example.jinwoo.olddowntown_beaconapp.trace.MyTrace;
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
 * Created by chihong on 2016-12-08.
 */

public class MainActivity extends AppCompatActivity {
    //commit test
    Activity act;

    DialogInterface mPopupDlg = null;
    AlertDialog.Builder alertbox, main_alertbox, networkCheckDialog;
    MappingCheckClass check;

    private ImageView fab;
    public Boolean isOpen = false;

    private long pressBackTimeInMillis = 0;

    public static Switch sw1, sw2;
    SharedPreferences pref;
    public SharedPreferences.Editor editor = null;
    private BroadcastReceiver btOnReceiver;

    TextView themeTour, courseTour;

    LinearLayout mainLayout, odIntrdc, odInfo, odTourlist, myTrace;
    FrameLayout hiddenLayout;
    private long mLastClickTime;

    String relatedCourseUrl = "";
    ArrayList<RelatedVO> RelatedDataList = new ArrayList<RelatedVO>();
    ArrayList<MappingVO> MappingDataList = new ArrayList<MappingVO>();
    String mappingUrl = "";
    String contentsNo = "";
    String titleNm = "";
    String fileUrl = "";
    DBHelper dbHelper;

    long spotId;

    Intent intent;

    CommonVO commonVO = new CommonVO();
    String localIp = commonVO.localIp;
    String serverIp = commonVO.serverIp;

    private boolean m_bFlag = false;
    private Handler m_hHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setResources();
        init();

        intent = new Intent(Intent.ACTION_SYNC, null, this, BackgroundService.class);

        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        odIntrdc = (LinearLayout) findViewById(R.id.od_Intrdc);
        odInfo = (LinearLayout) findViewById(R.id.od_Info);
        odTourlist = (LinearLayout) findViewById(R.id.od_tourlist);
        myTrace = (LinearLayout) findViewById(R.id.my_trace);
        hiddenLayout = (FrameLayout) findViewById(R.id.hidden_layout);
        themeTour = (TextView) findViewById(R.id.theme_tour);
        courseTour = (TextView) findViewById(R.id.course_tour);

        dbHelper = new DBHelper(getApplicationContext(), FILE_PATH + dbName, null, dbVersion);

        //네트워크 연결이 안됐을 시, 설정 다이얼로그 띄움
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifi.isConnected() || mobile.isConnected()) {   // wifi 또는 모바일 네트워크 어느 하나라도 연결이 되어있다면,
            Log.d("Oldtown" , "연결이 되었습니다.");

        } else {
            networkCheckDialog = new AlertDialog.Builder(MainActivity.this);
            networkCheckDialog.setTitle(Html.fromHtml("<font color='#6D5652'>연결할 수 없음</font>"));
            networkCheckDialog.setMessage("네트워크 연결이 되어 있지 않습니다. \n네트워크를 연결하시겠습니까?");
            networkCheckDialog.setCancelable(false);
            networkCheckDialog.setNegativeButton("닫기", null);
            networkCheckDialog.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                }
            }).create().show();
        }

        m_hHandler = new Handler() {    //백키 핸들러
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    m_bFlag = false;
                }
            }
        };
    }

    FrameLayout.OnClickListener HiddenLayoutClickListener = new FrameLayout.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, MyTrace.class);
            startActivity(intent);
        }
    };

    /*플로팅 버튼 클릭 리스너*/
    ImageView.OnClickListener FabClickListener = new ImageView.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab:

                    /*animateFAB();
                    Intent i = new Intent(MainActivity.this, SettingsMain.class);
                    startActivity(i);
                    overridePendingTransition(0,0);*/

                    //background_dimmer.setVisibility(View.VISIBLE);

                    final LinearLayout linear = (LinearLayout) View.inflate(MainActivity.this, R.layout.settings_detail, null);

                    main_alertbox = new AlertDialog.Builder(MainActivity.this);
                    main_alertbox.setCancelable(false);
                    //main_alertbox.setIcon(R.drawable.setting_icon);
                    main_alertbox.setTitle(Html.fromHtml("<font color='#6D5652'>비콘 신호 수신 설정</font>"));
                    main_alertbox.setView(linear);
                    //main_alertbox.setNegativeButton("닫기", null);
                    main_alertbox.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            main_alertbox = null;
                            Log.d("Oldtown","   닫기 에서 odtown_alertbox   null ? ->" + main_alertbox);
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

                    AlertDialog alertDialog = main_alertbox.create();

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

                    break;

            }
        }
    };

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


    /* 초기화 함수 */
    public void init() {

        //viewPager.setAdapter(new adapter(getSupportFragmentManager()));
        act.findViewById(R.id.fab).setOnClickListener(FabClickListener);
        //act.findViewById(R.id.fab2).setOnClickListener(FabClickListener2);
        act.findViewById(R.id.hidden_layout).setOnClickListener(HiddenLayoutClickListener);

    }

    /* 리소스 세팅 */
    public void setResources() {

        act = this;
        fab = (ImageView) findViewById(R.id.fab);
        //fab2 = (ImageView) findViewById(fab2);
        //viewPager=(ViewPager)findViewById(R.id.viewpager);
        Constants.btAdapter = BluetoothAdapter.getDefaultAdapter();     //블루투스 어댑터

        pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        Constants.pushState = pref.getString("push", "");


    }

    /* 블루투스 상태 체크하여 스위치 on/off 시킴 */
    public void blueToothStateCheck() {
        Log.d("Oldtown","   blueToothStateCheck 에서 odtown_alertbox   null ? ->" + main_alertbox);
        if (Constants.btAdapter.getState()==BluetoothAdapter.STATE_TURNING_OFF) {// 블루투스가 켜져있을 경우 스위치 on

            //설정 창이 뜬 상태일 때
            if (main_alertbox != null) {
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

            if (main_alertbox != null) {
                sw1.setChecked(true);
                Constants.bluetoothState = "1";
            } else {
                Constants.bluetoothState = "1";
            }
        }
    }

    /* 푸시알림 상태 체크하여 스위치 on/off 시킴 */
    public void pushStateCheck() {

        if ("0".equals(Constants.pushState))
            sw2.setChecked(false);
        else if ("1".equals(Constants.pushState))
            sw2.setChecked(true);
    }

    /*창경 go 버튼 이벤트*/
    public void txtClick(View v) {
        switch (v.getId()) {
            case R.id.od_Intrdc:
                Intent intent = new Intent(MainActivity.this, Olddowntown_intrdc.class);
                Log.d("jinwoo", "   창경   ");
                startActivity(intent);
                //overridePendingTransition(0, 0);
                TranslateAnimation an2;
                break;

            case R.id.od_Info:
                if (isOpen == false) {
                    odTourlist.setVisibility(View.VISIBLE);
                    myTrace.setVisibility(View.VISIBLE);
                    hiddenLayout.setVisibility(View.GONE);

                    int height = 0 - (myTrace.getHeight()) / 2;
                    Log.d("Oldtown", "   음수-> " + height);

                    an2 = new TranslateAnimation(0, 0, height, 0);
                    an2.setFillAfter(true);
                    an2.setDuration(500);
                    myTrace.startAnimation(an2);
                    Log.d("Oldtown", "   odTourlist.getHeight() 높이  헿-> " + height);
                    Log.d("Oldtown", "   odTourlist.getHeight() 높이  111111-> " + odTourlist.getHeight());

                    //더블터치 방지를 위한 처음 클릭한 시간 저장
                    mLastClickTime = SystemClock.elapsedRealtime();

                    isOpen = true;

                    themeTour.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            courseTour.setClickable(false);
                            Intent intent = new Intent(MainActivity.this, ThemeTour.class);
                            Log.d("jinwoo", "   창경   ");
                            startActivity(intent);
                            //overridePendingTransition(0, 0);
                        }
                    });

                    courseTour.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            themeTour.setClickable(false);
                            Intent intent = new Intent(MainActivity.this, CourseTour.class);
                            Log.d("jinwoo", "   창경   ");
                            startActivity(intent);
                            //overridePendingTransition(0, 0);
                        }
                    });

                } else {

                    //애니메이션이 끝나기전에 다시 클릭하면 GONE이 안되게 처리
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1001) {
                        Log.d("Oldtown", SystemClock.elapsedRealtime() + "   -   " + mLastClickTime + " = " + (SystemClock.elapsedRealtime() - mLastClickTime));
                        return;
                    }

                    int height = 0 - odTourlist.getHeight();

                    an2 = new TranslateAnimation(0, 0, 0, height);
                    an2.setFillAfter(true);
                    an2.setFillEnabled(true);
                    an2.setDuration(500);
                    myTrace.startAnimation(an2);

                    isOpen = false;

                    an2.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            odTourlist.setVisibility(View.GONE);
                            myTrace.setVisibility(View.GONE);
                            hiddenLayout.setVisibility(View.VISIBLE);
                            hiddenLayout.invalidate();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                break;

            case R.id.my_trace:
                Intent intent2 = new Intent(MainActivity.this, MyTrace.class);
                Log.d("jinwoo", "   창경   ");
                startActivity(intent2);
                //overridePendingTransition(0, 0);
                break;
        }
    }

    public void BeaconScanning(long spotId) {

        Log.d("Oldtown", "MainActivity => 감지된 spotId ::::: " + spotId);

        // 비콘 id가 0이 아닐경우 진입
        if (spotId != 0) {
            //비콘 id가 0이 아니면서 매핑된 컨텐츠가 있을 경우 진입

            Log.d("Oldtown", "MainActivity => onBeaconId   " + spotId);

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

            Log.d("Oldtown", "MainActivity => uri : " + uri);
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();

                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                    Log.d("Oldtown", "MainActivity => json   " + json);
                }

                return sb.toString().trim();

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("Oldtown", "MainActivity => result : " + result);


            JSONObject jsonRes = null;

            if (result == null) {
                Log.d("Oldtown", "MainActivity => result : null");

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
                    Log.d("Oldtown", "MainActivity => mappingVO.contentsTitle  매핑데이터 -> " + mappingVO.contentsTitle);
                    Log.d("Oldtown", "MainActivity => result : " + result);
                    fileUrl = mappingVO.filePath;
                    contentsNo = jsonRes.getString("contentsNo");
                    titleNm = jsonRes.getString("contentsTitle");
                    Log.d("Oldtown", "MainActivity => fileUrl1 : " + fileUrl);



                    Log.d("Oldtown", "MainActivity => fileUrl 2 : " + fileUrl);
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
            Log.d("Oldtown", "MainActivity => DB 인서트    ->  " + i + " 회   " + dbHelper.getResult().toString());
        }

        //로컬DB 끝

        if (alertbox != null) {
            mPopupDlg.dismiss();
            alertbox = null;
            mPopupDlg = null;
        }

        alertbox = new AlertDialog.Builder(MainActivity.this);
        alertbox.setTitle("비콘 신호 감지");
        alertbox.setMessage("페이지를 여시겠습니까?");
        alertbox.setIcon(R.drawable.ic_menu_gallery);
        alertbox.setCancelable(false); // back 버튼 막음
        alertbox.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, PageOpen.class);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {

            if (!m_bFlag) {
                Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한 번 더 누르시면 종료합니다.", Toast.LENGTH_SHORT).show();
                m_bFlag = true;
                m_hHandler.sendEmptyMessageDelayed(0, 2000);
                return false;
            }
            else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        Log.d("Oldtown", "MainActivity => onDestroy 호출");
        MyApplication.appRun = "0";

        stopService(intent);
        StopMonitoring();

        super.onDestroy();
    }

    @Override
    protected void onPause() {

        Log.d("Oldtown", "MainActivity => onPause 호출");

        StopMonitoring();
        intent.putExtra("PageNm", "MAIN");
        startService(intent);
        Log.d("Oldtown", "MainActivity => startService111111111111");

        unregisterReceiver();

        super.onPause();
    }

    @Override
    protected void onResume() {

        Log.d("Oldtown", "MainActivity => onResume 호출");
        MyApplication.appRun = "1";

        stopService(intent);

        if("1".equals(Constants.bluetoothState))
            StartMonitoring();
        else if("0".equals(Constants.bluetoothState))
            StopMonitoring();

        hiddenLayout.setVisibility(View.GONE);
        odTourlist.setVisibility(View.GONE);
        myTrace.setVisibility(View.VISIBLE);
        isOpen = false;

        registerReceiver();

        super.onResume();
    }

    @Override
    protected void onRestart() {

        Log.d("Oldtown", "MainActivity => onRestart())))))))))))))))))))))))))))))");

        super.onRestart();
    }

    private final TamraObserver tamraObserver = new TamraObserver() {
        @Override
        public void didEnter(Region region) {       //지역 진입

            Log.d("Oldtown", "MainActivity => ----didEnter ----" + region.name() + "에 진입");
        }

        @Override
        public void didExit(Region region) {        //지역 이탈

            Log.d("Oldtown", "MainActivity => ----didExit ----" + region.name() + "에서 이탈");
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
