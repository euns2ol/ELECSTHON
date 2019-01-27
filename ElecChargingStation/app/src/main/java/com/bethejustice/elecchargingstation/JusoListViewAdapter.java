package com.bethejustice.elecchargingstation;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bethejustice.elecchargingstation.Model.Juso;

import java.util.ArrayList;

public class JusoListViewAdapter extends BaseAdapter {

    ArrayList<Juso> juso_data = new ArrayList<>();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JusoItemView view = new JusoItemView(parent.getContext());

        view.setTx_doro(juso_data.get(position).getDoro_addr());
        view.setTx_jibun(juso_data.get(position).getJibun_addr());

        return view;
    }

    @Override
    public int getCount() {
        return juso_data.size();
    }

    @Override
    public Object getItem(int position) {
        return juso_data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }




    public void addJusoItem(Juso juso){
        juso_data.add(juso);
    }

    public void addJusoList(ArrayList<Juso> list){

        juso_data.clear();
        juso_data = list;
    }
}
