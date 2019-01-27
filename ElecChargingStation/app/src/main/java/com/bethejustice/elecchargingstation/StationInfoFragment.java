package com.bethejustice.elecchargingstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaonavi.KakaoNaviParams;
import com.kakao.kakaonavi.KakaoNaviService;
import com.kakao.kakaonavi.Location;
import com.kakao.kakaonavi.NaviOptions;
import com.kakao.kakaonavi.options.CoordType;
import com.kakao.kakaonavi.options.RpOption;
import com.kakao.kakaonavi.options.VehicleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class StationInfoFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_PARAM1 = "param1";

    private String charge_name; //충전소 이름

    private OnFragmentInteractionListener mListener;

    private MapView mapView;
    private GoogleMap googleMap;

    //충전기 정보 표시 UI
    private TextView tx_name;
    private TextView tx_addr;
    private TextView tx_charge_type;
    private TextView tx_state;
    private TextView tx_km;

    //Destination
    private double Destination_latitude;
    private double Destination_longitude;
    private double waypoint_longitude;
    private double waypoint_latitude;

    private Geocoder geocoder;

    public interface OnFragmentChangeListener {
        public void onToMapFragment();
    }

    private OnFragmentChangeListener changeListener;


    private ArrayList<ChargingStation> optimalStations = new ArrayList<>();
    private int index = -1;

    public StationInfoFragment() {
        // Required empty public constructor
    }



    public static StationInfoFragment newInstance(String param1) {
        StationInfoFragment fragment = new StationInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //geocode
        geocoder = new Geocoder(getContext());
        if (getArguments() != null) {
            charge_name = getArguments().getString(ARG_PARAM1);

        }

        optimalStations = MapFragment.getOptimalStation();



        if(optimalStations != null ){

            for(int i=0; i<optimalStations.size(); i++){

                if(optimalStations.get(i).getC_name().equals(charge_name)){
                    index = i;
                    break;
                }
            }

        }else{
            Toast.makeText(getActivity(), "오류로인해 데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_station_info, container, false);

        tx_name = rootView.findViewById(R.id.text_station_name);
        tx_addr = (TextView)rootView.findViewById(R.id.text_address);
        tx_charge_type = (TextView)rootView.findViewById(R.id.text_type);
        tx_state = (TextView)rootView.findViewById(R.id.text_status);
        tx_km = (TextView)rootView.findViewById(R.id.text_distance);
        mapView = (MapView)rootView.findViewById(R.id.mapView);
        mapView.getMapAsync(this);

        Button toMapFragButton = rootView.findViewById(R.id.btn_toMapFragment);
        toMapFragButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .remove(StationInfoFragment.this).commit();

                getActivity().getSupportFragmentManager().popBackStack();
                //changeListener.onToMapFragment();
            }
        });

        Button toNaviButton = rootView.findViewById(R.id.btn_startNavi);
        toNaviButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Address> list = null;
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("destination", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                KakaoNaviParams params;
                KakaoNaviParams.Builder builder= null;
                Address address;
                Location destination;
                String des_s = sharedPreferences.getString("destination", null);
                String way_s = optimalStations.get(index).getC_addr();
                try {
                    list = geocoder.getFromLocationName(way_s, 1);
                    address = list.get(0);
                    waypoint_latitude = address.getLatitude();
                    waypoint_longitude = address.getLongitude();
                }catch (IOException e) {
                }
                if(des_s!=null || des_s == "Not") {
                    try {
                        list = geocoder.getFromLocationName(des_s, 1);
                        address = list.get(0);
                        Destination_latitude = address.getLatitude();
                        Destination_longitude = address.getLatitude();

                    } catch (IOException e) {
                        destination = Location.newBuilder("목적지", waypoint_longitude, waypoint_latitude).build();
                        NaviOptions options = NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setVehicleType(VehicleType.FIRST).setRpOption(RpOption.SHORTEST).build();
                        builder = KakaoNaviParams.newBuilder(destination).setNaviOptions(options);
                    }
                    destination = Location.newBuilder("목적지", Destination_longitude, Destination_latitude).build();
                    List<Location> waypoint = new ArrayList<Location>();
                    waypoint.add(Location.newBuilder("경유지", waypoint_longitude, waypoint_latitude).build());

                    NaviOptions options = NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setVehicleType(VehicleType.FIRST).setRpOption(RpOption.SHORTEST).build();
                    builder = KakaoNaviParams.newBuilder(destination).setNaviOptions(options).setViaList(waypoint);
                }else{
                    destination = Location.newBuilder("목적지", waypoint_longitude, waypoint_latitude).build();
                    NaviOptions options = NaviOptions.newBuilder().setCoordType(CoordType.WGS84).setVehicleType(VehicleType.FIRST).setRpOption(RpOption.SHORTEST).build();
                    builder = KakaoNaviParams.newBuilder(destination).setNaviOptions(options);
                }
                params = builder.build();
                KakaoNaviService.getInstance().navigate(getContext(), params);
            }

        });


        if(optimalStations.size()>0){

            if(index != -1 && index < optimalStations.size()) {
                tx_name.setText(optimalStations.get(index).getC_name());
                tx_addr.setText(optimalStations.get(index).getC_addr());
                tx_charge_type.setText(optimalStations.get(index).getC_available_time());
                tx_state.setText(Integer.toString(optimalStations.get(index).getC_state()));
                tx_km.setText(Double.toString(optimalStations.get(index).getC_dist()));
            }
        }



        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "안내를 종료합니다.", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        });

        return  rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
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

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        this.googleMap = googleMap;


        Log.d("stationOnMapReady","2");

        if(optimalStations.size()>0){

            Log.d("DDD","DDadadad");
            if(index != -1 && index < optimalStations.size()) {
                LatLng loc = new LatLng(optimalStations.get(index).getC_lat(),optimalStations.get(index).getC_longi());
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));

                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(loc);

                markerOptions.title(charge_name);

                this.googleMap.addMarker(markerOptions);
            }
        }else{

        }


        this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(13)); //줌조정
        this.googleMap.getUiSettings().setRotateGesturesEnabled(false);  //회전조정*/

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
