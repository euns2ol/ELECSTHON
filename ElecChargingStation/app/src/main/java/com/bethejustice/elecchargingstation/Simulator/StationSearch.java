package com.bethejustice.elecchargingstation.Simulator;


import com.bethejustice.elecchargingstation.Model.CarInfo;
import com.bethejustice.elecchargingstation.Model.ChargingStation;
import com.bethejustice.elecchargingstation.Model.Position;

import java.util.ArrayList;

public class StationSearch {

    ArrayList<Position> routeSet;
    ArrayList<ChargingStation> totalList;
    ArrayList<ChargingStation> optimal_list;
    CarInfo carInfo;


    public StationSearch() {
        carInfo = SimulationActivity.getUserCar();
        routeSet = SimulationActivity.getRouteList();
        totalList = SimulationActivity.getTotal_list();
        optimal_list = SimulationActivity.getOptimal_list();
    }


    public void updatePos(){
        updateUserCarPos();     // 자동차 위치 및 남은주행거리 업데이트
        updateStationDist();    // 충전소와 자동차 거리 업데이트
    }

    public void updateUserCarPos(){
        Position cur = new Position(carInfo.getLati(), carInfo.getLongi()); // 현재 위치를 직전 위치로 변경하고,
        if(!routeSet.isEmpty()){                                           // 위치 리스트에서 다음 위치를 받아와 저장한다.
            // 현재 위치를 이전 위치로 변경
            carInfo.setBefore_lati(cur.getLatitude());
            carInfo.setBefore_longi(cur.getLongitude());

            // 다음위치 계산해서 설정
            Position next = routeSet.remove(0);

            carInfo.setLati(next.getLatitude());
            carInfo.setLongi(next.getLongitude());
            carInfo.setRest_dist(carInfo.getRest_dist()     // 현재 남은 거리에서 직전 시점에서 현재 시점까지 움직인 거리를 빼준다.
                    - calcDist(next.getLatitude(), next.getLongitude(), cur.getLatitude(), cur.getLongitude()));
        }
    }

    public void updateStationDist(){
        // totalList 갱신
        for(int i=0; i<totalList.size(); i++){
            // 직전 자동차 위치와 현재 자동차 위치 사이의 거리를 계산하여 설정
            totalList.get(i).setC_before_dist(calcDist(totalList.get(i).getC_lat(), totalList.get(i).getC_longi(),
                    carInfo.getBefore_lati(), carInfo.getBefore_longi()));
            totalList.get(i).setC_dist(calcDist(totalList.get(i).getC_lat(), totalList.get(i).getC_longi(),
                    carInfo.getLati(), carInfo.getLongi()));
        }

        // optimalList 갱신
        for(int i=0; i<optimal_list.size(); i++){
            // 직전 자동차 위치와 현재 자동차 위치 사이의 거리를 계산하여 설정
            optimal_list.get(i).setC_before_dist(calcDist(optimal_list.get(i).getC_lat(), optimal_list.get(i).getC_longi(),
                    carInfo.getBefore_lati(), carInfo.getBefore_longi()));
            optimal_list.get(i).setC_dist(calcDist(optimal_list.get(i).getC_lat(), optimal_list.get(i).getC_longi(),
                    carInfo.getLati(), carInfo.getLongi()));
        }
    }

    // 최적리스트 찾을 시점이 되면 리스트를 만들고 true 리턴
    // 아직 충전할 시점이 되지 않으면 false 리턴
    public boolean searchStation(){
        // (남은주행거리 - 차량예측거리)로 갈 수 있는 충전소가 존재하는지 확인
        boolean isFindOptimal = false;
        boolean isExist = false;

        for(int i=0; i<totalList.size(); i++){
            if(carInfo.getRest_dist() - SimulationActivity.PREDICT_DIST >= totalList.get(i).getC_dist()) {

                // 이전값과 비교했을 때 더 길어지지 않았다면
                if(totalList.get(i).getC_before_dist() >= totalList.get(i).getC_dist()) {
                    isExist = true;
                    break;  // 하나라도 갈 수 있는 충전소가 있으면 빠져나옴
                }
            }
        }

        if(!isExist) {   // 갈 수 있는 충전소가 하나도 존재하지 않다면
            for(int i=0; i<totalList.size(); i++)
                if(totalList.get(i).getC_dist() <= carInfo.getRest_dist() + SimulationActivity.PREDICT_DIST){
                    // rest_dist_temp와 현재 자동차 위치 사이의 충전소를 리스트에 추가한다.
                    if(totalList.get(i).getC_dist() <= totalList.get(i).getC_before_dist() )
                        optimal_list.add(totalList.get(i));
                }

            isFindOptimal = true; // if 문이 한번만 실행되도록 값 변경
        }
        return isFindOptimal;
    }

    // 안심거리 주변의 리스트 탐색
    public void makeOptimalList(){
        for(int i=0; i<totalList.size(); i++)
            if(totalList.get(i).getC_dist() <= SimulationActivity.PREDICT_DIST){
                // 일정거리(10km) 이내에 있는 충전소를 리스트에 추가
                if(totalList.get(i).getC_dist() <= totalList.get(i).getC_before_dist() )
                    optimal_list.add(totalList.get(i));
            }
    }


    public double calcDist(double lat1, double lon1, double lat2, double lon2) {
        // 두 지점 사이의 거리 계산
        double p = 0.017453292519943295; // Pi/180
        double temp = 0.5 - Math.cos((lat2 - lat1) * p) / 2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;

        return 12742 * Math.asin(Math.sqrt(temp));
    }

}
