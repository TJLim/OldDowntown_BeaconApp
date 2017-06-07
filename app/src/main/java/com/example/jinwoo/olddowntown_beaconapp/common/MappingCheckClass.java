package com.example.jinwoo.olddowntown_beaconapp.common;

import android.util.Log;

import com.example.jinwoo.olddowntown_beaconapp.main.Constants;
import com.example.jinwoo.olddowntown_beaconapp.vo.CommonVO;

import java.util.Calendar;

/**
 * Created by chihong on 2017-01-17.
 */

public class MappingCheckClass {

    public boolean isReceivedMappingContents(String mappingId) {

        boolean isMappingedOk = false;
        int i = 0;

        Calendar cal = Calendar.getInstance();
        long currentTime = cal.getTimeInMillis() / 1000;

        Log.d("Oldtown", "MappingCHeckClass.currentTime : " + currentTime);
        Log.d("Oldtown", "MappingCHeckClass.mappingId : " + mappingId);

        for (i = 0; i < Constants.commonVOList.size(); i++) {

            if (mappingId.equals(Constants.commonVOList.get(i).maappingID)) {
                isMappingedOk = true;
                break;
            }
            isMappingedOk = false;
        }

        if (isMappingedOk) {

            if( Constants.commonVOList.get(i).mappingTime + Constants.delayTime <  currentTime){

                Constants.commonVOList.remove(i);

                CommonVO vo = new CommonVO();

                vo.mappingTime = currentTime;
                vo.maappingID = mappingId;

                Constants.commonVOList.add(vo);

                return true;

            } else {

                return false;
            }

        } else {
            CommonVO vo = new CommonVO();

            vo.mappingTime = currentTime;
            vo.maappingID = mappingId;
            Constants.commonVOList.add(vo);

            return true;
        }
    }
}
