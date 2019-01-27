package com.bethejustice.elecchargingstation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bethejustice.elecchargingstation.JusoParser.JusoDataConvert;
import com.bethejustice.elecchargingstation.JusoParser.JusoRequest;
import com.bethejustice.elecchargingstation.Model.Juso;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class JusoListActivity extends AppCompatActivity {

    ListView listView;
    JusoListViewAdapter adapter;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final String service_url = "http://www.juso.go.kr/addrlink/addrLinkApi.do?currentPage=1&countPerPage=100&resultType=json&keyword=";
    private final String service_key = "U01TX0FVVEgyMDE4MDgyMzIyNDMxOTEwODA5MzU=";
    private String keyword = "";
    private ArrayList<Juso> juso_data_list;

    //네트워크 체크
    private static ConnectivityManager cm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jusolist);


        sharedPreferences = getSharedPreferences("destination", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.remove("destination");
        editor.commit();

        Intent intent = getIntent();
        keyword = intent.getExtras().getString("Keyword");

        //리스트뷰, 어댑터선언
        listView = findViewById(R.id.jusoList);
        adapter = new JusoListViewAdapter();

        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); //네트워크 체크를위한 변수

        //리스트뷰 어댑터 등록
        listView.setAdapter(adapter);

        //리스트뷰 클릭이벤트 등록
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                Juso item = (Juso) parent.getItemAtPosition(position) ;
                //데이터 전달 해야함

                editor.putString("destination", item.getDoro_addr());
                editor.commit();

                Log.d("destination", "listActivity"+item.getDoro_addr());

                finish();
            }
        }) ;


        //네트워크 상태를 체크하고 공공데이터 도로명주소API를 호출한다.
        if(isNetworkConnected()) {
            GetJusoList juso_data = new GetJusoList(service_url, keyword, service_key);
            juso_data.execute();
        }else {
            Toast.makeText(this, "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();

        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        try{

        }catch (Exception e){
            Log.d("화면전환 리스너 오류",e.getMessage());
        }
    }


    //네트워크 스레드
    private class GetJusoList extends AsyncTask<Void,Void,String> {

        private String url;
        private ProgressDialog nDialog;

        public GetJusoList(String url, String keyword, String service_key){

            try {
                this.url = url + keyword +  URLEncoder.encode(keyword, "UTF-8") + "&confmKey=" + service_key;
            }catch (Exception e){
                Log.d("인코딩 오류",e.getMessage());
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(JusoListActivity.this); //Here I get an error: The constructor ProgressDialog(PFragment) is undefined
            nDialog.setMessage("Loading..");
            nDialog.setTitle("주소를 찾는 중입니다.");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }


        @Override
        protected String doInBackground(Void... voids) {

            String result;
            JusoRequest jusoRequest =new JusoRequest();
            result = jusoRequest.request(url);

            if (result!=null)
                result=result.trim();

            if(result != null)
                Log.d("파싱결과",result);

            return result;

        }

        @Override
        protected void onPostExecute(String s) {

            JusoDataConvert jusoDataConvert = new JusoDataConvert();
            juso_data_list = jusoDataConvert.getData(s);

            /*for(int i=0;i<juso_data_list.size();i++){
                Log.d("주소","!"+juso_data_list.get(i).getJibun_addr()+juso_data_list.get(i).getDoro_addr());
            }*/

            adapter.addJusoList(juso_data_list);
            adapter.notifyDataSetChanged();
            if (nDialog  != null && nDialog .isShowing()) {
                nDialog .dismiss();
            }

        }
    }


    public static boolean isNetworkConnected() {

        return cm.getActiveNetworkInfo() != null;
    }

}
