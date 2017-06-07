package com.example.jinwoo.olddowntown_beaconapp.main;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;

import com.example.jinwoo.olddowntown_beaconapp.vo.CommonVO;
import com.kakao.oreum.tamra.base.Spot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class Constants {
    public static final String mapKey = "3739d19f391e1e0f164a1710bbb384c1"; //다음 지도 키

    public static Set<Spot> spotSet = new LinkedHashSet<>();
    public static String bluetoothState;
    public static String pushState;
    public static String gps1 = "";
    public static String gps2 = "";
    public static double lat;
    public static double lon;

    public static int delayTime = 60;

    public static ArrayList<CommonVO> commonVOList = new ArrayList<>();

    public static BroadcastReceiver scrOnReceiver;
    public static BroadcastReceiver scrOffReceiver;
    public static boolean mIsReceiverRegistered = false;

    public static BroadcastReceiver btOnReceiver;
    public static boolean btIsReceiverRegistered = false;
    public static BluetoothAdapter btAdapter;

}
