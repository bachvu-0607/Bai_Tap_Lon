package com.uet.models;

import java.util.ArrayList;
import javafx.util.Pair;

abstract public class User{
    protected String Name;
    protected String Phone_Number;
    protected String ID;
    protected String Password;
    protected String Address;

    public User() {}
    public User(String Name, String Phone_Number,  String ID, String Password, String Address) {
        this.Name = Name;
        this.Phone_Number = Phone_Number;
        this.ID = ID;
        this.Password = Password;
        this.Address = Address;
    }

    public String getName(){return this.Name;}
    public String getPassword(){return this.Password;}
    public String getID(){return this.ID;}
    public void setName(String Name){this.Name = Name;}
    public void setPassword(String Password){this.Password = Password;}
}