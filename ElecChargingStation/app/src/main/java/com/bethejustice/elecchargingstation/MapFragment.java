package com.bethejustice.elecchargingstation;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public interface OnFragmentChangeListener {
        public void onFragmentChange(String C_name);
    }

    private OnFragmentChangeListener changeListener;
    private MapView mapView;
    private GoogleMap googleMap = null;
    private LocationManager lm;

    //일단 대충의 충전소 데이터(나중에 optimal충전소로 변경예정) -> private static 쓴다...
    private static ArrayList<ChargingStation> optimalStation;

    //지도상에 표시된 마커들 (서비스 중지시 모두 바꿔줘야함.)
    private ArrayList<Marker> markers = null;
    private Marker cur_my_loc;
    private Marker before_my_loc;
    boolean curinitCheck = true;
    boolean initCheck = true;


    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static ArrayList<ChargingStation> getOptimalStation() {
        return optimalStation;
    }

    public static void setOptimalStation(ArrayList<ChargingStation> optimalStation) {
        MapFragment.optimalStation = optimalStation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        optimalStation = new ArrayList<>();
        ChargingStation station = new ChargingStation("29170003","01","광주문화예술회관", "광주광역시 북구 북문대로 60","24시간이용가능", 3,
                3,35.177718,126.881703);
        optimalStation.add(station);

        //마커를 위한 자원 초기화
        markers = new ArrayList<>();

        Log.d("OnCreate","화면");

        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);


        //위치매니저 객체 생성하고 얻어오기 (여기서 기준점 설정 몇 초마다 몇 mf로 데이터 가져올껀지)
        try {

            int gpsCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

         lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    1000, // 통지사이의 최소 시간간격 (miliSecond)
                    300, // 통지사이의 최소 변경거리 (m) .
                    mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    1000, // 통지사이의 최소 시간간격 (miliSecond)
                    300, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
        }catch (Exception e){
            Log.d("객체생성오류",e.getMessage());
        }
        //////////////////////////////////

    }

    //위치매니저 등록 리스너
    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            //이 값을 지도의 중심점으로 설정할 거임.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자

            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            Toast.makeText(getActivity(), "gps호출 위도 : " + longitude + "경도 : " + latitude, Toast.LENGTH_SHORT).show();

           if(googleMap != null){
            Movemaps(latitude,longitude);
            DisplayMarker();}

        }
        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView =(ViewGroup) inflater.inflate(R.layout.fragment_map, container, false);

        Button tolistButton = rootView.findViewById(R.id.btn_toList);
        tolistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().remove(MapFragment.this).commit();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "안내를 종료합니다.", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });

        mapView = (MapView)rootView.findViewById(R.id.mapView2);
        mapView.getMapAsync(this);

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            changeListener = (OnFragmentChangeListener) context;

        }catch (Exception e){
            Log.d("화면전환 리스너 오류",e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

        //마커표시를위한 자원 초기화
        initCheck = true;
        curinitCheck = true;
        markers = new ArrayList<>();

        try {

            int gpsCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    0, // 통지사이의 최소 변경거리 (m) 나중에 300M로 바꿔야함.
                    mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    0, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
        }catch (Exception e){
            Log.d("객체생성오류",e.getMessage());
        }

        Log.d("OnStart","화면");
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        lm.removeUpdates(mLocationListener);
        markers = null;
        Log.d("OnStop","화면");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        Log.d("OnResume","화면");
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(mLocationListener);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(mapView!=null){
            mapView.onCreate(savedInstanceState);
        }
    }

    //맵 가장처음 초기화 시키는 함수
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d("OnMapReady","1");

        this.googleMap = googleMap;

        int gpsCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
        googleMap.setMyLocationEnabled(true);

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13)); //줌조정
        googleMap.getUiSettings().setRotateGesturesEnabled(false);  //회전조정

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.d("눌렀냐 눌렀냐.",marker.getTitle());

        //다이얼로그를 띄워라~~

        //화면전환
        if(!marker.getTitle().equals("현재위치") ) {
            changeListener.onFragmentChange(marker.getTitle());
        }

        return false;
    }

    //내위치 변경시 지도의 중심 변경
    public void Movemaps(double lat, double lon){

       if(cur_my_loc != null)
            cur_my_loc.remove();

        LatLng cur_loc = new LatLng(lat, lon);

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(cur_loc);

        markerOptions.title("현재위치");

        cur_my_loc = googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cur_loc));

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        googleMap.setOnMarkerClickListener(this);

    }

    //optimal데이터를 다시 표시해주는 함수(위치가 변경될 때 바뀐 정보를 반영해줌)
    public void DisplayMarker(){

        //가장처음 마커표시
        if(initCheck){

            //처음에는 모든마커 모두 표시
            for(int i=0; i<optimalStation.size(); i++){
                //위치, 이름(찾을 것),
                MarkerOptions markerOption = new MarkerOptions();

                LatLng charge_loc = new LatLng(optimalStation.get(i).getC_lat(), optimalStation.get(i).getC_longi());
                markerOption.position(charge_loc);
                markerOption.title(optimalStation.get(i).getC_name());
                Marker marker = googleMap.addMarker(markerOption);
                markers.add(marker);

            }

            initCheck = false;

        }else{

            for(int i=0; i<markers.size(); i++) {
                int deleteCheck = -1;
                String stationName = markers.get(i).getTitle();

                for (int j = 0; j < optimalStation.size(); j++) {

                    if( optimalStation.get(i).getC_name().equals(stationName) ){
                        deleteCheck = 1; //삭제안함
                    }
                }

                if(deleteCheck == -1){
                    markers.get(i).remove();
                    markers.remove(i);

                    i--;
                }
            }

            for(int i=0; i<optimalStation.size(); i++){
                int insertCheck = 1;

                for(int j = 0; j< markers.size(); j++){
                    if(optimalStation.get(i).getC_name().equals(markers.get(j).getTitle())){
                        insertCheck = -1; //삽입안함.
                    }
                }

                if(insertCheck == 1){
                    MarkerOptions markerOption = new MarkerOptions();

                    LatLng charge_loc = new LatLng(optimalStation.get(i).getC_lat(), optimalStation.get(i).getC_longi());
                    markerOption.position(charge_loc);
                    markerOption.title(optimalStation.get(i).getC_name());

                    Marker addMarker = googleMap.addMarker(markerOption);
                    markers.add(addMarker);
                }
            }

        }
    }

}
