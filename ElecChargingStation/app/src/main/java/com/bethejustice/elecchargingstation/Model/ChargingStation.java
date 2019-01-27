package com.bethejustice.elecchargingstation.Model;

import android.support.annotation.NonNull;

public class ChargingStation implements Comparable<ChargingStation>{
    private String c_id;    // 충전소 아이디
    private String c_juior_id; //충전기 아이디
    private String c_name;  // 충전소명
    private String c_addr;  // 주소
    private String c_available_time; //이용가능시간

    private int c_type;     // 충전기 종류 (01: DC차데모, 03: DC차데모+AC상, 06: DC차데모+AC상+DC콤보)
    private int c_state;    // 충전상태 (1: 통신이상, 2: 충전가능, 3: 충전중, 4: 운영중지, 5: 점검중)

    private double c_lat;   // 위도
    private double c_longi; // 경도
    private double c_dist;  // 현 위치에서 충전소까지의 거리
    private double c_before_dist; // 직전 거리

    public ChargingStation(String c_id,String c_juior_id, String c_name, String c_addr,String c_available_time, int c_type, int c_state
            ,double c_lat,double c_longi){

        this.c_id = c_id;
        this.c_juior_id = c_juior_id;
        this.c_name = c_name;
        this.c_addr = c_addr;
        this.c_available_time = c_available_time;
        this.c_type = c_type;
        this.c_state = c_state;
        this.c_lat = c_lat;
        this.c_longi = c_longi;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getC_juior_id() {
        return c_juior_id;
    }

    public void setC_juior_id(String c_juior_id) {
        this.c_juior_id = c_juior_id;
    }

    public String getC_name() {
        return c_name;
    }

    public void setC_name(String c_name) {
        this.c_name = c_name;
    }

    public String getC_addr() {
        return c_addr;
    }

    public void setC_addr(String c_addr) {
        this.c_addr = c_addr;
    }

    public String getC_available_time() {
        return c_available_time;
    }

    public void setC_available_time(String c_available_time) {
        this.c_available_time = c_available_time;
    }


    public int getC_type() {
        return c_type;
    }

    public void setC_type(int c_type) {
        this.c_type = c_type;
    }

    public int getC_state() {
        return c_state;
    }

    public void setC_state(int c_state) {
        this.c_state = c_state;
    }

    public double getC_lat() {
        return c_lat;
    }

    public void setC_lat(double c_lat) {
        this.c_lat = c_lat;
    }

    public double getC_longi() {
        return c_longi;
    }

    public void setC_longi(double c_longi) {
        this.c_longi = c_longi;
    }

    public double getC_dist() {
        return c_dist;
    }

    public void setC_dist(double c_dist) {
        this.c_dist = c_dist;
    }

    public double getC_before_dist() {
        return c_before_dist;
    }

    public void setC_before_dist(double c_before_dist) {
        this.c_before_dist = c_before_dist;
    }

    @Override
    public int compareTo(@NonNull ChargingStation station) {
        return Double.compare(c_before_dist, station.getC_dist());
    }
}
