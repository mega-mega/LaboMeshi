package com.labomeshi.t.labomeshi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MyAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater layoutInflater = null;
    private ArrayList<oshina> list;
    private HttpURLConnection connection = null;
    private ImageView image = null;
    public MyAdapter(Context context){
        this.context = context;
        layoutInflater  = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOshinaList(ArrayList<oshina> list){
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.itemcell,parent,false);

        ((TextView)convertView.findViewById(R.id.listname)).setText(list.get(position).name);
        ((TextView)convertView.findViewById(R.id.listprice)).setText(list.get(position).price + "円");
        image = (ImageView)convertView.findViewById(R.id.listimage);
        //list.get(position).imageView = image;
        return convertView;
    }




}
