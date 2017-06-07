package com.example.jinwoo.olddowntown_beaconapp;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.jinwoo.olddowntown_beaconapp.main.Constants;

import static com.example.jinwoo.olddowntown_beaconapp.main.Constants.gps1;
import static com.example.jinwoo.olddowntown_beaconapp.main.Constants.gps2;
import static com.example.jinwoo.olddowntown_beaconapp.main.Constants.lat;
import static com.example.jinwoo.olddowntown_beaconapp.main.Constants.lon;


/**
 * Created by TJ on 2016-12-20.
 */

public class MyApplication extends Application {


    private IntentFilter scrOnFilter;
    private IntentFilter scrOffFilter;
    SharedPreferences pref;
    public SharedPreferences.Editor editor = null;
    BluetoothAdapter adapter;

    public static Context context;

    public static LocationManager locationManager;
    public static LocationListener locationListener;

    public static boolean useGps = false;
    public static boolean firstCheck = false;

    public static DisplayMetrics displayMetrics;
    public static double deviceWidth, deviceHeight;

    public static String appRun = "";
    //String tourListUrl = "http://218.157.145.72:10/creativeEconomy/CourseUserLineMList.do?userKey=nndoonw";
    String poiListUrl = "http://218.157.145.72:10/creativeEconomy/CourseUserLineDList.do?userKey=nndoonw&courseMNo=";



    @Override
    public void onCreate() {
        super.onCreate();

        appRun = "1";
        Log.d("Oldtown",  "MyApplication =>         appRun : " + appRun);

        init();

        bluetoothStateCheck();
        pushStateCheck();

        //화면이 켜져 있을 때
        Constants.scrOnReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d("ko", "SCREEN ON ");

                Constants.mIsReceiverRegistered = true;
                //unregisterReceiver(scrOffReceiver);
            }
        };

        Log.d("Oldtown",  "MyApplication => Constants.pushState : " + Constants.pushState);

        scrOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        scrOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        registerReceiver(Constants.scrOnReceiver, scrOnFilter);
        registerReceiver(Constants.scrOffReceiver, scrOffFilter);

        find_Location();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        unregisterReceiver(Constants.scrOnReceiver);
        unregisterReceiver(Constants.scrOffReceiver);
    }

    public void bluetoothStateCheck() {

        if(adapter.isEnabled()) { // 블루투스가 켜져있을 경우
            Constants.bluetoothState = "1";
        } else {                    // 블루투스가 꺼져있을 경우
            Constants.bluetoothState = "0";
        }
    }

    /* 설정 푸시 상태 값 가져오기 위함. */
    public void pushStateCheck() {

        Log.d("Oldtown",  "pushState : " + Constants.pushState);
        if(Constants.pushState == null || "".equals(Constants.pushState)) {
            editor.putString("push", "1");
            editor.commit();

            Constants.pushState = "1";
        }
    }




    /* Application Class 초기화 함수kosaf24574  */
    public void init() {

        pref = this.getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        Constants.pushState = pref.getString("push", "");

        adapter = BluetoothAdapter.getDefaultAdapter();     //블루투스 어댑터

        displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        deviceWidth = displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;
        Log.i("Oldtown", " deviceWidth    ->" + deviceWidth);
        Log.i("Oldtown", " deviceHeight   ->" + deviceHeight);
    }

    public void find_Location() {

        Log.i("Oldtown", "in find_location");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {


            public void onLocationChanged(Location location) {

                gps1 = "";
                gps2 = "";

                gps1 = String.valueOf(location.getLatitude());
                gps2 = String.valueOf(location.getLongitude());

                Log.d("Oldtown", "before gps1 : " + gps1);
                Log.d("Oldtown", "before gps2 : " + gps2);

                int index1 = gps1.indexOf(".");
                int index2 = gps2.indexOf(".");

                String tempGps1 = gps1.substring(0,index1);
                if(gps1.length() > 9) {
                    tempGps1 += gps1.substring(index1, index1 +7);
                    Log.d("Oldtown", "Length()1 : " + gps1.length());
                } else
                    tempGps1 = gps1;

                String tempGps2 = gps2.substring(0,index2);

                if(gps2.length() > 9) {
                    tempGps2 += gps2.substring(index2, index2 + 7);
                    Log.d("Oldtown", "Length()2 : " + gps2.length());
                } else
                    tempGps2 = gps2;
//				tempGps2 += gps2.substring(index2,  index2 + 7);

                gps1 = tempGps1;
                gps2 = tempGps2;

                Log.d("Oldtown", "after gps1 : " + gps1);
                Log.d("Oldtown", "after gps2 : " + gps2);

                lat = Double.parseDouble(gps1);
                lon = Double.parseDouble(gps2);
                Log.d("Oldtown", "lat -> " + lat + "   lon -> " + lon);


//				Log.i("Oldtown", "in onLocationChanged - GPS1 IS : " + gps1);
//				Log.i("Oldtown", "in onLocationChanged - GPS2 IS : " + gps2);

                locationManager.removeUpdates(locationListener);
            }

            public void onProviderDisabled(String provider) {
                // Log.i("Oldtown", "onProviderDisabled");

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


    public static Context getAppContext() {
        return MyApplication.context;
    }





}
