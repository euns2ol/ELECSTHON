package com.bethejustice.elecchargingstation.Model;

public class CarInfo {

    private int type;                           // 차량 충전기 타입 ( DC차 데모(1), AC3상(2), DC콤보(3) )
    private double lati, longi;                 // 차량의 현재 위치
    private double before_lati, before_longi;   // 차량의 이전 위치
    private double rest_dist;                   // 남은 주행 거리

    public CarInfo(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public double getBefore_lati() {
        return before_lati;
    }

    public void setBefore_lati(double before_lati) {
        this.before_lati = before_lati;
    }

    public double getBefore_longi() {
        return before_longi;
    }

    public void setBefore_longi(double before_longi) {
        this.before_longi = before_longi;
    }

    public double getRest_dist() {
        return rest_dist;
    }

    public void setRest_dist(double rest_dist) {
        this.rest_dist = rest_dist;
    }
}
