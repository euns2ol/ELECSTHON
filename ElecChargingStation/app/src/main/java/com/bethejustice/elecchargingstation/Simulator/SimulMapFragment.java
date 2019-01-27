package com.bethejustice.elecchargingstation.Simulator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.bethejustice.elecchargingstation.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class SimulMapFragment extends Fragment implements OnMapReadyCallback{

    private MapView mapView = null;
    private GoogleMap googleMap;
    private Marker cur_loc;

    boolean initCheck = true;
    private ArrayList<ChargingStation> optimalStation;
    private ArrayList<Marker> markers = null;

    public SimulMapFragment(){
        //Required
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        optimalStation = SimulationActivity.getOptimal_list();
        markers = new ArrayList<>();
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = getLayoutInflater().inflate(R.layout.simulator_map, container, false);

        mapView = (MapView)layout.findViewById(R.id.mapview3);
        mapView.getMapAsync(this);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        initCheck = true;
        markers = new ArrayList<>();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(mapView!=null){
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(SEOUL);

        markerOptions.title("서울");

        markerOptions.snippet("수도");

        googleMap.addMarker(markerOptions);

        googleMap.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));


    }

    public void movemap(double lat,double lon){

        if(googleMap != null) {
            if (cur_loc != null)
                cur_loc.remove();

            LatLng Current = new LatLng(lat, lon);
            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(Current);

            markerOptions.title("내위치");

            cur_loc = googleMap.addMarker(markerOptions);

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(Current));

            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }
    }


    //optimal데이터를 다시 표시해주는 함수(위치가 변경될 때 바뀐 정보를 반영해줌)
    public void DisplayMarker(){

        if(optimalStation != null && markers != null && googleMap != null) {
            //가장처음 마커표시
            if (initCheck) {

                //처음에는 모든마커 모두 표시
                for (int i = 0; i < optimalStation.size(); i++) {
                    //위치, 이름(찾을 것),
                    MarkerOptions markerOption = new MarkerOptions();
                    LatLng charge_loc = new LatLng(optimalStation.get(i).getC_lat(), optimalStation.get(i).getC_longi());
                    markerOption.position(charge_loc);
                    markerOption.title(optimalStation.get(i).getC_name());
                    Marker marker = googleMap.addMarker(markerOption);


                    markers.add(marker);

                }

                initCheck = false;

            } else {

                for (int i = 0; i < markers.size(); i++) {
                    int deleteCheck = -1;

                    String stationName = markers.get(i).getTitle();

                    for (int j = 0; j < optimalStation.size(); j++) {

                        if (optimalStation.get(j).getC_name().equals(stationName)) {
                            deleteCheck = 1; //삭제안함
                        }
                    }

                    if (deleteCheck == -1) {
                        markers.get(i).remove();
                        markers.remove(i);

                        i--;
                    }
                }

                for (int i = 0; i < optimalStation.size(); i++) {
                    int insertCheck = 1;

                    for (int j = 0; j < markers.size(); j++) {
                        if (optimalStation.get(i).getC_name().equals(markers.get(j).getTitle())) {
                            insertCheck = -1; //삽입안함.
                        }
                    }

                    if (insertCheck == 1) {
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
}
