package com.bethejustice.elecchargingstation.Model;

public class Juso {

    private String jibun_addr;
    private String doro_addr;

    public Juso(String jibun_addr, String doro_addr){
        this.jibun_addr = jibun_addr;
        this.doro_addr = doro_addr;
    }

    public String getDoro_addr() {
        return doro_addr;
    }

    public void setDoro_addr(String doro_addr) {
        this.doro_addr = doro_addr;
    }

    public String getJibun_addr() {
        return jibun_addr;
    }

    public void setJibun_addr(String jibun_addr) {
        this.jibun_addr = jibun_addr;
    }
}
