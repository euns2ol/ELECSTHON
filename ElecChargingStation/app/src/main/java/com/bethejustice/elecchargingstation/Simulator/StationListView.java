package com.bethejustice.elecchargingstation.Simulator;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bethejustice.elecchargingstation.R;


public class StationListView extends LinearLayout {

    ImageView s_img;        // 충전기상태표시 이미지
    TextView s_name;        // 충전소명
    TextView s_dist_time;   // 거리(소요시간)
    TextView btn_guide;       // 경로안내버튼

    public StationListView(Context context) {
        super(context);
        init(context);
    }

    public StationListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_station_list, this, true);
        s_img = (ImageView)findViewById(R.id.list_marker);
        s_name = (TextView)findViewById(R.id.station_name);
        s_dist_time = (TextView)findViewById(R.id.station_dist_time);
        btn_guide = findViewById(R.id.btn_guide);
    }

    public void setStationName(String sName){
        s_name.setText(sName);
    }

    public void setStationDistTime(String sDist, String sTime){
        s_dist_time.setText(sDist + "(" + sTime + ")");
    }

    public void serImgColor(int state){
        // state: 충전상태 (1: 통신이상, 2: 충전가능, 3: 충전중, 4: 운영중지, 5: 점검중)
        switch (state){
            case 2:                 // 이용가능
                s_img.setImageResource(R.drawable.ic_station_state2);
                break;
            case 3:                 // 이용중
                s_img.setImageResource(R.drawable.ic_station_state3);
                break;
            case 1: case 4: case 5: // 이용불가능
                s_img.setImageResource(R.drawable.ic_station_state1);
                btn_guide.setVisibility(INVISIBLE); // 이용불가능이면 경로안내 버튼 안보이게
                break;
        }
    }

}
