package com.bethejustice.elecchargingstation;

import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class NavigationActivity extends AppCompatActivity implements MapFragment.OnFragmentChangeListener, StationInfoFragment.OnFragmentChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Button seeMapButton = findViewById(R.id.btn_seeAll);
        seeMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(1);
            }
        });

        Button tempButton = findViewById(R.id.btn_temp);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onFragmentChange("station1");
            }
        });

        /**
         * fab
         */

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "안내를 종료합니다.", Toast.LENGTH_LONG);

                finish();
            }
        });
    }

    public void changeFragment(int id) {
        if (id == 1) {
            MapFragment fragment = MapFragment.newInstance("test1", "String2");

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)  // fragment 스택에서 관리, 뒤로가기버튼눌렀을때 여기로 돌아옴.
                    .commit();
        }
    }

    @Override
    public void onFragmentChange(String C_name) {
        //여기서 프레그먼트에 보내줄 값 설정 및 프래그먼트 체인지 (충전소정보화면으로)
       /* Bundle args = new Bundle(1);
        args.putString("CName", C_name);
        StationInfoFragment.setArguments(args);
        changeFragment(FRAGMENT_STATION_INFO);*/


       StationInfoFragment fragment = StationInfoFragment.newInstance(C_name);

       getSupportFragmentManager()
               .beginTransaction()
               .replace(R.id.container, fragment,"info")
               .addToBackStack(null)
               .commit();
    }

    public void onToMapFragment() {
        //여기서 프레그먼트에 보내줄 값 설정 및 프래그먼트 체인지 (충전소정보화면으로)
       /* Bundle args = new Bundle(1);
        args.putString("CName", C_name);
        StationInfoFragment.setArguments(args);
        changeFragment(FRAGMENT_STATION_INFO);*/

        Log.d("dddd","인터페이스 안 호출1");

        Fragment mFragment = getSupportFragmentManager().findFragmentByTag("info");

        getSupportFragmentManager()
                .beginTransaction()
                .remove(mFragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences = getSharedPreferences("destination", MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();

        editor.remove("destination").commit();

    }
}
