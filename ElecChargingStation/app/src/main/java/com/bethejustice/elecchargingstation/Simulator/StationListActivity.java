package com.bethejustice.elecchargingstation.Simulator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.MapFragment;
import com.bethejustice.elecchargingstation.Model.CarInfo;
import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.bethejustice.elecchargingstation.Model.Position;
import com.bethejustice.elecchargingstation.R;
import com.bethejustice.elecchargingstation.XmlParser.EnviromentXmlParser;


import java.util.ArrayList;
import java.util.Collections;


public class StationListActivity extends AppCompatActivity {

    public final static double PREDICT_DIST = 10.0;        // 차량예측거리

    private double reliefeDist = 50.0;               // 사용자 안심 거리 50km로 임의 설정 ==> 코드 합칠 때 사용자가 설정하도록 변경해야 함
    private ArrayList<Position> routeSet;
    private ArrayList<ChargingStation> optimalList;
    private CarInfo carInfo;
    private StationSearch stationSearch;

    private long time_start;
    private boolean check_map = false;
    boolean isQuit;

    ListView listView;
    StationListAdapter adapter;


    //지도이동버튼 추가 - 은솔
    Button move_map;
    SimulMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_list);
        listView = findViewById(R.id.station_list);
        mapFragment = new SimulMapFragment();

        //지도이동버튼 추가 - 은솔
        move_map = (Button)findViewById(R.id.btn_gomap);
        move_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Fragment 변경
                check_map = true;
                getSupportFragmentManager()
                        .beginTransaction()
                .add(R.id.container, mapFragment,"frag")
                .commit();
    }
});

        carInfo = SimulationActivity.getUserCar();
        routeSet = SimulationActivity.getRouteList();
        optimalList = SimulationActivity.getOptimal_list();
        stationSearch = new StationSearch();

        time_start = 0;
        isQuit = false;
        adapter = new StationListAdapter();
        listView.setAdapter(adapter);

        Toast.makeText(getApplicationContext(), "까꿍", Toast.LENGTH_LONG).show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!routeSet.isEmpty() && !isQuit){
                    try {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stationSearch.updatePos();
                                updateList();
                                adapter.notifyDataSetChanged();

                                Double lati = Double.parseDouble(String.format("%.6f", carInfo.getLati()));
                                Double longi = Double.parseDouble(String.format("%.6f", carInfo.getLongi()));

                                if(check_map == true) {
                                    SimulMapFragment mapFragment = (SimulMapFragment) getSupportFragmentManager().findFragmentByTag("frag");
                                    if(mapFragment!= null) {
                                        mapFragment.movemap(lati, longi);
                                        mapFragment.DisplayMarker();
                                    }
                                }
/*
                                //
                                updatePos();
                                updateList();
                                adapter.notifyDataSetChanged();
                                for(int i=0; i<optimal_list.size(); i++)
                                    System.out.println("[TESTING] " + i + " : " + optimal_list.get(i).getC_dist() );*/
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                System.out.println("[TESTING] 끝");
            }
        }).start();

    }

    private void updateList(){
        // 5분이 경과할 때 마다 API 재파싱
        if(System.currentTimeMillis() - time_start > 300000) {
            new EnviromentXmlParser().execute();
            time_start = System.currentTimeMillis();
        }

        // 지나친 충전소 제외
        // (지나친 충전소 : 직전 자동차와 충전소 사이의 거리보다 현재 자동차와 충전소 사이의 거리가 길어진 경우)
        for(int i=0; i<optimalList.size(); i++)
            if(optimalList.get(i).getC_dist() > optimalList.get(i).getC_before_dist())
                optimalList.remove(i);

        // 갈 수 없는 충전소 제외
        for(int i=0; i<optimalList.size(); i++)
            if(!optimalList.isEmpty())
                if(optimalList.get(i).getC_dist() > carInfo.getRest_dist())
                    optimalList.remove(i);

        // 리스트 정렬
        Collections.sort(optimalList);

        if(optimalList.isEmpty())
            isQuit = true;
    }

    private double calcDist(double lat1, double lon1, double lat2, double lon2){
        // 두 지점 사이의 거리 계산
        double p = 0.017453292519943295; // Pi/180
        double temp = 0.5 - Math.cos((lat2 - lat1) * p)/2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(temp));
    }


    // 리스트뷰 Adapters
    public class StationListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return optimalList.size();
        }

        @Override
        public Object getItem(int position) {
            return optimalList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            StationListView view = new StationListView(getApplicationContext());
            ChargingStation item = optimalList.get(position);

            view.serImgColor(item.getC_state());        // 상태에 따른 마커이미지 색 변경
            view.setStationName(item.getC_name());
            view.setStationDistTime(Double.parseDouble(String.format("%.4f", item.getC_dist())) + "km",
                    "00분"); // 소요시간은 현재 속도를 기준으로 계산하여 보내줌
            return view;
        }
    }
}


