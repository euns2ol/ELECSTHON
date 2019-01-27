package com.bethejustice.elecchargingstation.Simulator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.MapFragment;
import com.bethejustice.elecchargingstation.Model.CarInfo;
import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.bethejustice.elecchargingstation.Model.Position;
import com.bethejustice.elecchargingstation.R;
import com.bethejustice.elecchargingstation.XmlParser.EnviromentXmlParser;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;

public class SimulationActivity extends AppCompatActivity {

    private static ArrayList<ChargingStation> _01ChargingStations; //DC차 데모
    private static ArrayList<ChargingStation> _03ChargingStations; //AC3상
    private static ArrayList<ChargingStation> _06ChargingStations; //DC콤보

    private static ArrayList<Position> routeList;  // 시뮬레이션을 위해 저장한 임의의 자동차 경로
    private static ArrayList<ChargingStation> optimal_list;
    private static ArrayList<ChargingStation> total_list;

    private static StationSearch stationSearch;



    public final static double PREDICT_DIST = 10.0;        // 차량예측거리

    public static CarInfo userCar = new CarInfo(1);   // 임의로 차량타입 1로 초기화 =========> 코드 합칠 때 사용자가 설정하도록 변경해야 함
    private static double reliefeDist = 50.0;               // 사용자 안심 거리 50km로 임의 설정 ==> 코드 합칠 때 사용자가 설정하도록 변경해야 함

    private boolean isFindOptimal;
    private boolean isQuit;
    private long time_start;

    Button btn_goToList;
    static TextView tempPos;

    //네트워크체크
    private static ConnectivityManager cm;

    // 푸쉬알람
    NotificationManager notificationManager;
    Intent intent;
    Notification.Builder builder;


    public static void set_01ChargingStations(ArrayList<ChargingStation> _01ChargingStations) {

        if (SimulationActivity._01ChargingStations != null)
            SimulationActivity._01ChargingStations.clear();

        SimulationActivity._01ChargingStations = _01ChargingStations;
    }

    public static ArrayList<ChargingStation> get_01ChargingStations() {

        return _01ChargingStations;
    }

    public static ArrayList<ChargingStation> get_03ChargingStations() {
        return _03ChargingStations;
    }

    public static void set_03ChargingStations(ArrayList<ChargingStation> _03ChargingStations) {

        if (SimulationActivity._03ChargingStations != null)
            SimulationActivity._03ChargingStations.clear();

        SimulationActivity._03ChargingStations = _03ChargingStations;
    }

    public static ArrayList<ChargingStation> get_06ChargingStations() {
        return _06ChargingStations;
    }

    public static void set_06ChargingStations(ArrayList<ChargingStation> _06Dc_demo_ChargingStations) {

        if (SimulationActivity._06ChargingStations != null)
            SimulationActivity._06ChargingStations.clear();

        SimulationActivity._06ChargingStations = _06Dc_demo_ChargingStations;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);

        btn_goToList = (Button) findViewById(R.id.btn_goToList);
        tempPos = (TextView) findViewById(R.id.temp_pos);

        userCar.setRest_dist(30.0);
        routeList = new ArrayList<>();
        optimal_list = new ArrayList<>();
        total_list = _01ChargingStations;

        isQuit = false;

        notificationManager = (NotificationManager)SimulationActivity.this.getSystemService(SimulationActivity.this.NOTIFICATION_SERVICE);
        intent = new Intent(SimulationActivity.this.getApplicationContext(), SimulationActivity.class);
        builder = new Notification.Builder(getApplicationContext());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendnoti = PendingIntent.getActivity(SimulationActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //푸시 알림에 대한 각종 설정
        builder.setSmallIcon(R.drawable.ic_launcher_background).setTicker("충전이 필요합니다.").setWhen(System.currentTimeMillis())
                .setNumber(1).setContentTitle("전기차 충전이 필요합니다.").setContentText("리스트를 확인하고 차량을 충전해주세요.")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendnoti).setAutoCancel(true).setOngoing(true);


        btn_goToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
                startActivity(intent);
            }
        });

        if (savedInstanceState == null) {
            SimulMapFragment mapView = new SimulMapFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.pager, mapView, "main")
                    .commit();
        }

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); //네트워크 체크를위한 변수

        if(isNetworkConnected()) {
            new EnviromentXmlParser().execute();
        }else{
            Toast.makeText(this, "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }

        // 충전소리스트 파싱 된 후(2초 후) 스레드 실행
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(total_list == null){
                        // 차량 타입에 따라 참조할 충전소 리스트 설정
                        if(userCar.getType() == 3)
                            total_list = _03ChargingStations;
                        else if(userCar.getType() == 6)
                            total_list = _06ChargingStations;
                        else
                            total_list = _01ChargingStations;

                    }

                    userCar.setRest_dist(30.0); // 초기 남은 잔량 설정
                    parseRoute();   // 임시경로 파싱
                    initInfo();     // 차량 및 충전소와의 거리 초기화
                    stationSearch = new StationSearch();


                    startThrad();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void startThrad(){
        // 맵 로드가 끝나면 충전소 리스트 찾는 코드 실행

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!isQuit){
                    try{
                        // 0.1초마다 자동차 거리 갱신
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stationSearch.updatePos();
                                tempPos.setText("(" + Double.parseDouble(String.format("%.4f", userCar.getLati())) + ","
                                        + Double.parseDouble(String.format("%.4f", userCar.getLongi())) + ")"
                                        + "   남은거리 : " + Double.parseDouble(String.format("%.4f", userCar.getRest_dist()))+ "km");

                                Double lati = Double.parseDouble(String.format("%.6f", userCar.getLati()));
                                Double longi = Double.parseDouble(String.format("%.6f", userCar.getLongi()));

                                Log.d(Double.toString(lati), Double.toString(longi));

                                SimulMapFragment mapFragment =  (SimulMapFragment) getSupportFragmentManager().findFragmentById(R.id.pager);
                                mapFragment.movemap(lati , longi);

                                if(stationSearch.searchStation()){
                                    // optiaml list가 완성되면 StationListActivity로 넘어감
                                    notificationManager.notify(1, builder.build()); // 푸쉬알람
                                    isQuit = true;
                                    Intent intent = new Intent(getApplicationContext(), StationListActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    private void initInfo(){
        // 차량 타입에 따라 참조할 충전소 리스트 설정
        if(userCar.getType() == 1)
            total_list = _01ChargingStations;
        else if(userCar.getType() == 3)
            total_list = _03ChargingStations;
        else if(userCar.getType() == 6)
            total_list = _06ChargingStations;

        // 자동차와의 거리(c_dist), 직전거리(c_before_dist) 초기화
        userCar.setLati(routeList.get(0).getLatitude());
        userCar.setLongi(routeList.get(0).getLongitude());

        for(int i=0; i<total_list.size(); i++){
            double lati = total_list.get(i).getC_lat();
            double longi = total_list.get(i).getC_longi();

            // 현재 자동차와 주유소 사이의 거리 계산
            total_list.get(i).setC_dist(calcDist(lati, longi, userCar.getLati(), userCar.getLongi()));
            total_list.get(i).setC_before_dist(total_list.get(i).getC_dist());  // 초기값은 두 값이 동일
        }

        // 현재 자동차와 가까운 순으로 정렬
        Collections.sort(total_list);

    }

    private void parseRoute() {
        // 경로 xml 파싱
        try {
            XmlPullParser parser = getResources().getXml(R.xml.routes);
            int eventType = parser.getEventType();
            String tag = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    // 태그 시작
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();  // 태그 이름 얻어오기
                        if (tag.equals("coordinates")) {
                            parser.next();
                            String str_pos = parser.getText();
                            int size = 1;
                            for (int i = 0; i < str_pos.length(); i++)
                                if (str_pos.charAt(i) == ' ')
                                    ++size;

                            String x_y_set[];
                            x_y_set = str_pos.split(" ");

                            for (int i = 0; i < size; i++) {
                                int index = x_y_set[i].indexOf(',');
                                if (index > 0) {
                                    double logi = Double.parseDouble(x_y_set[i].substring(0, index));
                                    double lati = Double.parseDouble(x_y_set[i].substring(index + 1));
                                    routeList.add(new Position(lati, logi));
                                }
                            }
                        }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private double calcDist(double lat1, double lon1, double lat2, double lon2) {
        // 두 지점 사이의 거리 계산
        double p = 0.017453292519943295; // Pi/180
        double temp = 0.5 - Math.cos((lat2 - lat1) * p) / 2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(temp));
    }


    public static boolean isNetworkConnected() {

        return cm.getActiveNetworkInfo() != null;
    }

    public static ArrayList<Position> getRouteList() {
        return routeList;
    }
    public static ArrayList<ChargingStation> getTotal_list() {
        return total_list;
    }
    public static CarInfo getUserCar() {
        return userCar;
    }
    public static ArrayList<ChargingStation> getOptimal_list() {
        return optimal_list;
    }
}


