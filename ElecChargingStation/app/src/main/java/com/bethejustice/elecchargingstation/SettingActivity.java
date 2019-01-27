package com.bethejustice.elecchargingstation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    Toolbar toolbar;

    EditText nickName;
    Spinner chargingType;
    EditText safeDist;
    TextView bluetoothSet;

    SharedPreferences dataPref;
    SharedPreferences.Editor dataEditor;

    static public Context settingContext;

    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothService mbluetoorhservice;
    private static final String TAG = "Main";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

        ActivityCompat.requestPermissions(this,

                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},

                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        setContentView(R.layout.activity_setting);

        //setting activity context
        settingContext = SettingActivity.this;

        nickName = findViewById(R.id.edit_nickname);
        safeDist = findViewById(R.id.edit_safe);
        chargingType = findViewById(R.id.spinner_type);
        bluetoothSet = findViewById(R.id.btn_setbluetooth);

        dataPref = getSharedPreferences("data", MODE_PRIVATE);
        dataEditor = dataPref.edit();

        nickName.setText(dataPref.getString("nickname", null));
        safeDist.setText(Integer.toString(dataPref.getInt("safe", 50)));
        chargingType.setSelection(dataPref.getInt("type",0));

        bluetoothSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mbluetoorhservice.getDeviceState()) {
                    mbluetoorhservice.enableBluetooth();
                } else {
                    finish();
                }
            }
        });

        if(mbluetoorhservice == null) {
            mbluetoorhservice = new BluetoothService(this, ((MainActivity)MainActivity.mContext).myHandler);
        }

        Button saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dataEditor.putString("nickname", nickName.getText().toString());
                dataEditor.putInt("type", chargingType.getSelectedItemPosition());
                dataEditor.putInt("safe", Integer.parseInt(safeDist.getText().toString()));
                dataEditor.commit();

//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);

                finish();
            }
        });

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    mbluetoorhservice.getDeviceInfo(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Next Step
                    mbluetoorhservice.scanDevice();
                } else {

                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }
}
