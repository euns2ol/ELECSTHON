package com.bethejustice.elecchargingstation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Point;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.bethejustice.elecchargingstation.Model.Juso;
import com.bethejustice.elecchargingstation.Simulator.SimulationActivity;
import com.bethejustice.elecchargingstation.XmlParser.EnviromentXmlParser;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static com.kakao.util.helper.Utility.getPackageInfo;

public class MainActivity extends AppCompatActivity {

    public static String destination=null;

    StartDialog dialog;
    ChangeDestDialog changeDestDialog;
    TextView destinationText;

    ImageView settingButton;

    private TextView mspeedview;
    private TextView mrestview;
    static public Context mContext;
    public MyHandler myHandler = new MyHandler(this);


    //시뮬
    private Button btn_simul;

    //handler state
    public static final int MESSAGE_NOTDEVICE = 3;
    public static final int MESSAGE_CONNECT = 4;
    public static final int MESSAGE_FAIL = 5;
    static boolean handlestate = false;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //환경부 전기차 파싱데이터
    private static ArrayList<ChargingStation> _01ChargingStations; //DC차 데모
    private static ArrayList<ChargingStation> _03ChargingStations; //
    private static ArrayList<ChargingStation> _06ChargingStations; //DC콤보

    //네트워크체크
    private static ConnectivityManager cm;

    public static void set_01ChargingStations(ArrayList<ChargingStation> _01ChargingStations) {

        if(MainActivity._01ChargingStations != null )
            MainActivity._01ChargingStations.clear();

        MainActivity._01ChargingStations = _01ChargingStations;
    }

    public static ArrayList<ChargingStation> get_01ChargingStations() {

        return _01ChargingStations;
    }

    public static ArrayList<ChargingStation> get_03ChargingStations() {
        return _03ChargingStations;
    }

    public static void set_03ChargingStations(ArrayList<ChargingStation> _03ChargingStations) {

        if(MainActivity._03ChargingStations != null )
            MainActivity._03ChargingStations.clear();

        MainActivity._03ChargingStations = _03ChargingStations;
    }

    public static ArrayList<ChargingStation> get_06ChargingStations() {
        return _06ChargingStations;
    }

    public static void set_06ChargingStations(ArrayList<ChargingStation> _06Dc_demo_ChargingStations) {

        if(MainActivity._06ChargingStations != null )
            MainActivity._06ChargingStations.clear();

        MainActivity._06ChargingStations = _06Dc_demo_ChargingStations;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==100){
            destination = data.getStringExtra("doro");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mspeedview = findViewById(R.id.textview_speed);
        mrestview = findViewById(R.id.textView_restDistance);

        destinationText = findViewById(R.id.textView_destination);

        settingButton = findViewById(R.id.btn_seeAll);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
            }
        });

        btn_simul = (Button)findViewById(R.id.btn_Simulation);
        btn_simul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //인텐트
                Intent intent=new Intent(MainActivity.this,SimulationActivity.class);
                startActivity(intent);
            }
        });

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); //네트워크 체크를위한 변수

        //파싱시작
        if(isNetworkConnected()) {
            new EnviromentXmlParser().execute();
        }else{
            Toast.makeText(this, "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
        }
        /**
         * 새로 생성될때 다이얼 로그 띄운다.
         */
        sharedPreferences = getSharedPreferences("destination", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(!sharedPreferences.contains("destination")) {
            Dialog();
        }

        Button setDestButton = findViewById(R.id.btn_setDest);
        setDestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeDialog();
//
//                String s = sharedPreferences.getString("destination", "실패");
//                destinationText.setText(s);
            }
        });

        Button tempButton = findViewById(R.id.btn_toNavi);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.remove("destination");
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String s = sharedPreferences.getString("destination", "목적지가 없습니다.");
        destinationText.setText(s);

    }

    public void Dialog(){
        dialog = new StartDialog(MainActivity.this);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Window window = dialog.getWindow();

        int x = (int)(size.x * 0.8f);
        int y = (int)(size.y * 0.5f);
        window.setLayout(x, y);
    }

    public void ChangeDialog(){
        changeDestDialog = new ChangeDestDialog(MainActivity.this);
        changeDestDialog.getWindow().setGravity(Gravity.CENTER);
        changeDestDialog.setCancelable(false);
        changeDestDialog.show();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Window window = changeDestDialog.getWindow();

        int x = (int)(size.x * 0.8f);
        int y = (int)(size.y * 0.4f);
        window.setLayout(x, y);
    }


    public static String getDoro(){
        return destination;
    }

    //핸들러 객체
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what) {
                case MESSAGE_CONNECT:
                    if(!handlestate){
                        Toast.makeText((SettingActivity)SettingActivity.settingContext,msg.getData().getString("connect") ,Toast.LENGTH_SHORT).show();
                        handlestate = true;
                    }
                    MainActivity activity = mActivity.get();
                    if (activity != null) {
                        activity.handleMessage(msg);
                    }
                    break;
                case MESSAGE_FAIL:
                    if(handlestate) handlestate = false;
                    Toast.makeText((SettingActivity)SettingActivity.settingContext,msg.getData().getString("fail") ,Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_NOTDEVICE:
                    if(handlestate) handlestate = false;
                    Toast.makeText((SettingActivity)SettingActivity.settingContext,msg.getData().getString("disconnected") ,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    //Handler 호출함수
    private void handleMessage(Message msg) {
        Bundle bundle = msg.getData();
        final String available = bundle.getString("available");
        final String velocity = bundle.getString("velocity");
        if (available != null && velocity != null) updateThreadTextView(available, velocity);
    }

    //textView
    public void updateThreadTextView(final String available, final String velocity) {
        Thread readyThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mrestview.setText(available);
                        mspeedview.setText(velocity);
                    }
                });
            }
        });
        readyThread.start();
    }

    public static boolean isNetworkConnected() {

        return cm.getActiveNetworkInfo() != null;
    }
}
