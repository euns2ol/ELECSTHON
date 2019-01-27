package com.bethejustice.elecchargingstation.JusoParser;


import com.bethejustice.elecchargingstation.Model.Juso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JusoDataConvert {


    private static final String TAG_Response="results";
    private static final String TAG_JusoItem = "juso";
    private static final String TAG_Jibun = "jibunAddr";
    private static final String TAG_Doro = "roadAddrPart1";

    private JSONArray jusoList;
    private ArrayList<Juso> juso_data; //주소

    public ArrayList<Juso> getData(String s){

        juso_data = new ArrayList<>();


        try {

            JSONObject jsonObject = new JSONObject(s);
            JSONObject jusoObject = jsonObject.getJSONObject(TAG_Response);
            jusoList = jusoObject.getJSONArray(TAG_JusoItem);

            // 주소 저장
            for(int i = 0; i< jusoList.length(); i++){

                Juso value = new Juso(jusoList.getJSONObject(i).getString(TAG_Jibun), jusoList.getJSONObject(i).getString(TAG_Doro));
                juso_data.add(value);
            }

/*
            for(int i=0;i<juso_data.size();i++){
                Log.d("주소","!"+juso_data.get(i).getJibun_addr()+juso_data.get(i).getDoro_addr());
            }*/

        }catch (JSONException e){
            e.printStackTrace();
        }

        return juso_data;
    }

}