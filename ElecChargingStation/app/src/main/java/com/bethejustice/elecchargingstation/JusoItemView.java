package com.bethejustice.elecchargingstation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;


public class JusoItemView extends LinearLayout {

    private TextView tx_doro;
    private TextView tx_jibun;

    public JusoItemView(Context context){
        super(context);
        init(context);
    }

    public JusoItemView(Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    private void init(Context context){

        LayoutInflater inflater  = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.item_jusolist,this,true);

        tx_doro = (TextView)findViewById(R.id.tx_dorojuso);
        tx_jibun = (TextView)findViewById(R.id.tx_jibunjuso);

    }

    public void setTx_doro(String doro_addr){

        tx_doro.setText(doro_addr);
    }

    public void setTx_jibun(String jibun_addr){

        tx_jibun.setText(jibun_addr);
    }


}
