package com.bethejustice.elecchargingstation.XmlParser;

import android.os.AsyncTask;
import android.util.Log;

import com.bethejustice.elecchargingstation.MainActivity;
import com.bethejustice.elecchargingstation.Model.ChargingStation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class EnviromentXmlParser extends AsyncTask<String, Void, Document> {


    private final String serviceKey = "rAopH2VCShxLfTMRv8%2F%2BnLGg2MI1s5TorkcJhO4Sj0EryZk3ILaHMB3oelOaDu%2BOeAeH1IhaBVGdhcEOd9CEtg%3D%3D";

    @Override
    protected Document doInBackground(String... urls) {

        URL url;
        Document doc = null;

        try {
            String serviceUrl = "http://open.ev.or.kr:8080/openapi/services/rest/EvChargerService?serviceKey=";
            serviceUrl = serviceUrl + serviceKey;
            url = new URL(serviceUrl);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

        } catch (Exception e) {
            Log.d("ParsingError",e.getMessage());
        }

        return doc;
    }

    @Override
    protected void onPostExecute(Document doc) {

        String s = "";
        ArrayList<ChargingStation> _01ChargingStations = new ArrayList<>(); //DC차 데모
        ArrayList<ChargingStation> _03ChargingStations = new ArrayList<>(); //AC3상
        ArrayList<ChargingStation> _06ChargingStations = new ArrayList<>(); //DC콤보  //뭔지모를 04있음
        NodeList nodeList = doc.getElementsByTagName("item");


        for(int i = 0; i< nodeList.getLength(); i++){

            Node node = nodeList.item(i);
            Element fstElmnt = (Element) node;

            NodeList idx = fstElmnt.getElementsByTagName("chgerId");  //충전기 아이디
            String c_juior_id = idx.item(0).getChildNodes().item(0).getNodeValue();

            idx = fstElmnt.getElementsByTagName("statId"); //충전소 아이디
            String c_id = idx.item(0).getChildNodes().item(0).getNodeValue();

            idx = fstElmnt.getElementsByTagName("statNm");  //충전소 이름
            String c_name = idx.item(0).getChildNodes().item(0).getNodeValue();

            idx = fstElmnt.getElementsByTagName("addrDoro");  //주소
            String c_addr = idx.item(0).getChildNodes().item(0).getNodeValue();

            String c_available_time = "정보없음";

            if(fstElmnt.hasAttribute("useTime")) {
                idx = fstElmnt.getElementsByTagName("useTime");  //사용가능 시간
                c_available_time = "chgerNam = " + idx.item(0).getChildNodes().item(0).getNodeValue();

            }

            idx = fstElmnt.getElementsByTagName("chgerType");  //충전기 타입
            String type = idx.item(0).getChildNodes().item(0).getNodeValue();
            int c_type = Integer.parseInt(type);

            idx = fstElmnt.getElementsByTagName("stat");  //충전기 상태
            String state = idx.item(0).getChildNodes().item(0).getNodeValue();
            int c_state = Integer.parseInt(state);

            idx = fstElmnt.getElementsByTagName("lat");  //위도
            String lat= idx.item(0).getChildNodes().item(0).getNodeValue();
            double c_lat =  Double.parseDouble(lat);

            idx = fstElmnt.getElementsByTagName("lng");  //경도
            String longi = idx.item(0).getChildNodes().item(0).getNodeValue();
            double c_longi = Double.parseDouble(longi);

            ChargingStation charging_item = new ChargingStation(c_id, c_juior_id, c_name, c_addr, c_available_time, c_type,
                    c_state, c_lat, c_longi);

            if(charging_item.getC_type() == 01 || charging_item.getC_type() == 03 || charging_item.getC_type() == 06){
                    _01ChargingStations.add(charging_item);
            }
            if(charging_item.getC_type() == 03 || charging_item.getC_type() == 06){
                _03ChargingStations.add(charging_item);
            }
            if(charging_item.getC_type() == 06){
                _06ChargingStations.add(charging_item);
            }

        }

        Log.d("갯수갯수",Integer.toString(_01ChargingStations.size()) );
        Log.d("갯수갯수",Integer.toString(_03ChargingStations.size()) );
        Log.d("갯수갯수",Integer.toString(_06ChargingStations.size()) );

        MainActivity.set_01ChargingStations(_01ChargingStations);
        MainActivity.set_03ChargingStations(_03ChargingStations);
        MainActivity.set_06ChargingStations(_06ChargingStations);

        super.onPostExecute(doc);
    }

}




