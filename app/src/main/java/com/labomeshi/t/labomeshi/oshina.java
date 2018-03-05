package com.labomeshi.t.labomeshi;


import android.widget.ImageView;

public class oshina {
    public String name;
    public String price;
    //public String number;
    public String image;
    public ImageView imageView;
    public oshina(){}
    public oshina(String image,String name,String price){
        this.name = name;
        this.price = price;
        this.image = image;
    }
}
