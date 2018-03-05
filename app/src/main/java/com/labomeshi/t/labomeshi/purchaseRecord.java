package com.labomeshi.t.labomeshi;


import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class purchaseRecord implements Serializable{
    public String name;
    public String price;
    public String userIdm;
    public String date;

    public purchaseRecord(){}
    public purchaseRecord(oshina osn, User user){
        name = osn.name;
        price = osn.price;
        userIdm = user.Idm;
        Date d = new Date();
        date = new SimpleDateFormat("yyyy/MM/dd").format(d);
    }
}
