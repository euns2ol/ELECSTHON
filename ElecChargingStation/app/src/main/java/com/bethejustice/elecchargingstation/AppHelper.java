package com.bethejustice.elecchargingstation;

import android.content.SharedPreferences;

public class AppHelper {

    private static boolean first = false;

    public static boolean getFirst(){
        return first;
    }

    public void setFirst(boolean first){
        this.first = first;
    }

}
