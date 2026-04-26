package com.uet.models;

import java.io.Serializable;
import java.util.ArrayList;

import javafx.util.Pair;

abstract public class User extends Entity implements Serializable{
    protected String Name;
    protected String Phone_Number;
    protected String Password;
    protected String Address;

    public User() {}
    public User( String Name, String Phone_Number,  String ID, String Password, String Address) {
        super(ID);
        this.Name = Name;
        this.Phone_Number = Phone_Number;
        this.Password = Password;
        this.Address = Address;
    }

    public String getName(){return this.Name;}
    public String getPassword(){return this.Password;}
    public String getPhone_Number(){return this.Phone_Number;}
    public String getAddress(){return this.Address;}
    public void setName(String Name){this.Name = Name;}
    public void setPassword(String Password){this.Password = Password;}
}