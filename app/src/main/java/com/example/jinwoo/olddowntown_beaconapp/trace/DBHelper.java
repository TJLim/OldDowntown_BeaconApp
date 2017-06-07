package com.example.jinwoo.olddowntown_beaconapp.trace;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.jinwoo.olddowntown_beaconapp.vo.PoiVO;

import java.util.ArrayList;

/**
 * Created by TJ on 2017-03-08.
 */

public class DBHelper extends SQLiteOpenHelper {

    ArrayList<String> CourseNoGetDataList = new ArrayList<String>();
    ArrayList<String> CourseGetDataList = new ArrayList<String>();
    ArrayList<String> RowCountGetDataList = new ArrayList<String>();
    ArrayList<String> DateGetDataList = new ArrayList<String>();
    ArrayList<String> BeaconIdGetDataList = new ArrayList<String>();

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d("Oldtown", "DBHelper" + name);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        /* 이름은 MONEYBOOK이고, 자동으로 값이 증가하는 _id 정수형 기본키 컬럼과
        item 문자열 컬럼, price 정수형 컬럼, create_at 문자열 컬럼으로 구성된 테이블을 생성. */
        db.execSQL("CREATE TABLE tb_test5 (OLDTOWN_NO INTEGER PRIMARY KEY AUTOINCREMENT, BEACON_ID TEXT, BEACON_NM TEXT, BEACON_X TEXT, BEACON_Y TEXT, " +
                "COURSE_M_NO TEXT, COURSE_M_NM TEXT, REG_DT TEXT);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String id, String title, String x, String y, String mno, String mnm,  String bDate) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        try {
            db.execSQL("INSERT INTO tb_test5 VALUES(null, '" + id + "', '" + title + "', '" + x + "', '" + y + "', '" + mno + "', '" + mnm + "', '" + bDate + "');");
            db.close();
        } catch (SQLiteException e){
            Log.d("Oldtown", "   SQLiteException   -> "+e.getMessage());
        }

    }

    public void update(String id, String title, String x, String y, String mno, String mnm,  String bDate) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        try {
            db.execSQL("UPDATE tb_test5 SET BEACON_NM='" + title + "', BEACON_X='" + x + "', BEACON_Y='" + y + "', COURSE_M_NO='" + mno + "', COURSE_M_NM='" + mnm + "', REG_DT='" + bDate + "'  WHERE BEACON_ID='" + id + "';");
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  update SQLiteException   -> "+e.getMessage());
        }
        db.close();
    }

    public void delete(String id) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        try {
            db.execSQL("DELETE FROM tb_test5 WHERE BEACON_ID='" + id + "';");
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }

        db.close();
    }

    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        try {
            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            Cursor cursor = db.rawQuery("SELECT * FROM tb_test5", null);

            while (cursor.moveToNext()) {
                result += cursor.getString(0)
                        + " : "
                        + cursor.getString(1)
                        + " | "
                        + cursor.getString(2)
                        + " | "
                        + cursor.getString(3)
                        + " | "
                        + cursor.getString(4)
                        + " | "
                        + cursor.getString(5)
                        + " | "
                        + cursor.getString(6)
                        + " | "
                        + cursor.getString(7)
                        + "\n";
            }
            Log.d("Oldtown", "                        SELECT   -> "+result.toString());
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }
        return result;
    }

    public ArrayList<String> CourseNoGetResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        try {
            Log.d("Oldtown", "발자취 => DB 클리어 전 사이즈    ->  " + CourseNoGetDataList.size());
            CourseNoGetDataList.clear();
            Log.d("Oldtown", "발자취 => DB 클리어 후 사이즈    ->  " + CourseNoGetDataList.size());

            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            Cursor cursor = db.rawQuery("SELECT DISTINCT COURSE_M_NO FROM tb_test5", null);

            while (cursor.moveToNext()) {
                result = cursor.getString(0);

                CourseNoGetDataList.add(result);
            }
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }
        /*Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.size());
        Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.get(0));
        Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.get(1));*/
        return CourseNoGetDataList;
    }

    public ArrayList<String> CourseGetResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        try {
            Log.d("Oldtown", "발자취 => DB 클리어 전 사이즈    ->  " + CourseGetDataList.size());
            CourseGetDataList.clear();
            Log.d("Oldtown", "발자취 => DB 클리어 후 사이즈    ->  " + CourseGetDataList.size());

            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            Cursor cursor = db.rawQuery("SELECT DISTINCT COURSE_M_NM FROM tb_test5", null);

            while (cursor.moveToNext()) {
                result = cursor.getString(0);

                CourseGetDataList.add(result);
            }
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }
        /*Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.size());
        Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.get(0));
        Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.get(1));*/
        return CourseGetDataList;
    }

    public String RowGetResult(String nm) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        try {
            RowCountGetDataList.clear();
            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력



                Cursor cursor = db.rawQuery("SELECT count(*) FROM tb_test5 WHERE COURSE_M_NM = '" + nm + "'", null);

                while (cursor.moveToNext()) {
                    result = cursor.getString(0);
                    /*if (result ==null){
                        RowCountGetDataList.add("0");
                    } else {
                        RowCountGetDataList.add(result);
                    }
*/

                }


        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }
        //Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + RowCountGetDataList.size());

        return result;
    }

    public String DateGetResult(String nm) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        try {
            DateGetDataList.clear();
            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            Cursor cursor = db.rawQuery("SELECT DISTINCT MAX(REG_DT) FROM tb_test5 WHERE COURSE_M_NM = '" + nm + "'", null);

            while (cursor.moveToNext()) {
                result = cursor.getString(0);
                if (result == null) {
                    result = "미방문";
                } else {
                    result = cursor.getString(0);
                }

            }
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }

        return result;
    }
/*
    public String PoiDateGetResult(String id) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        String temp = "";

        try {
            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            Cursor cursor = db.rawQuery("SELECT REG_DT FROM tb_test5 WHERE BEACON_ID = '" + id + "'", null);

            while (cursor.moveToNext()) {
                result = cursor.getString(0);
                if (cursor.getString(0).isEmpty()) {
                    result = "미방문";
                } else {
                    result = "방문 : " + cursor.getString(0).substring(5);
                }

            }
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }
        Log.d("Oldtown", "  PoiDateGetResult  비콘 아이디 -> " + result);
        return result;
    }
*/

    public void BeaconIdGetResult(PoiVO poiVO) {

        Log.d("Oldtown", "poiVO.beaconId    ->  " + poiVO.beaconId);

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();

        try {
            Log.d("Oldtown", "발자취 => DB 클리어 전 사이즈    ->  " + BeaconIdGetDataList.size());
            BeaconIdGetDataList.clear();
            Log.d("Oldtown", "발자취 => DB 클리어 후 사이즈    ->  " + BeaconIdGetDataList.size());

            // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
            Cursor cursor = db.rawQuery("SELECT BEACON_ID, REG_DT FROM tb_test5 WHERE 1=1 AND BEACON_ID = " + poiVO.beaconId + " GROUP BY BEACON_ID ORDER BY REG_DT ASC", null);

            if(cursor.getCount() == 0){
                poiVO.beaconId = "";
                poiVO.visiteYn = "미방문";
            }
            else {
                while (cursor.moveToNext()) {

                    poiVO.regDt = cursor.getString(1).substring(5);
                    poiVO.visiteYn = "방문 : ";

                    Log.d("Oldtown", "  BeaconIdGetResult   -> " + poiVO.beaconId);
                    //BeaconIdGetDataList.add(result);
                }
            }
        } catch (SQLiteException e) {
            Log.d("Oldtown", "  getResult SQLiteException   -> "+e.getMessage());
        }
        /*Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.size());
        Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.get(0));
        Log.d("Oldtown", "  CourseGetResult 사이즈   -> " + CourseGetDataList.get(1));*/
        Log.d("Oldtown", "  BeaconIdGetResult2   -> " + poiVO.beaconId);
    }

}


