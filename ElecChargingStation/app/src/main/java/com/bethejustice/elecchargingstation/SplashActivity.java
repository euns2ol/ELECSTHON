package com.bethejustice.elecchargingstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    /**
     * 첫 방문을 확인하여 first 변수에 넣는다.
     * 처음방문하면 InitActivity로 화면전환한다.
     */

    private boolean first = true;
    Handler handler = new Handler();
    private boolean isGPSEnabled, isNetworkEnabled;
    private MyPermissionListener mPermissionListener;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        preferences = getSharedPreferences("firstVisit",MODE_PRIVATE);
        editor = preferences.edit();

        if(!preferences.contains("first")){
            editor.putBoolean("first", false);
        }
        editor.commit();


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //전체 GPS허용
                checkGPSService();

                //권한허용 리스너달기
                mPermissionListener = new MyPermissionListener();
                new TedPermission(SplashActivity.this)
                        .setPermissionListener(mPermissionListener)
                        .setDeniedMessage("위치 권한이 필요합니다\n1.설정을 누르세요\n2.권한을 누르세요\n3.위치를 켜주세요")
                        .setPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        .setGotoSettingButton(true)
                        .setGotoSettingButtonText("설정")
                        .check();

            }
        }, 2000);
    }

    public boolean checkGPSService() {

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("AlertDialog Title");
            builder.setMessage("AlertDialog Content");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(intent);
                        }
                    });
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                        }
                    });
            builder.show();

            return false;
        } else {
            return true;
        }
    }

    //권한설정리스너
    private class MyPermissionListener implements PermissionListener {
        private LocationManager locationManager = (LocationManager) SplashActivity.this.getSystemService(Context.LOCATION_SERVICE);

        @Override
        public void onPermissionGranted() {
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled || !isGPSEnabled) {

            }else{

                changeActivity();

            }
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(SplashActivity.this, "권한 필요 : " + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeActivity(){

        if(preferences.getBoolean("first", false)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            finish();

        }else{

            Intent intent = new Intent(getApplicationContext(), InitActivity.class);
            startActivity(intent);

            finish();
        }
    }

}
