package com.labomeshi.t.labomeshi;

import java.io.Serializable;


public class User implements Serializable{
    public String username;
    public String Idm;
    public String Auth;//マスターならM


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String Idm,String Auth) {
        this.username = username;
        this.Idm = Idm;
        this.Auth = Auth;
    }
}
