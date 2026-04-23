package com.uet.domain.entity;


import java.util.ArrayList;

import javafx.util.Pair;

abstract public class User extends Entity{
    protected String UserName;
    protected String Name;
    protected String Phone_Number;
    protected String Password;
    

    //construtor
    public User(){}

    public User(String Username, String Password, String Id){
        super(Id);
        this.UserName = Username;
        this.Name = Username;
        this.Password = Password;
    }

    public User(String UserName, String Password, String Phone_Number,  String ID) {
        super(ID);
        this.UserName = UserName;
        this.Name = UserName;
        this.Phone_Number = Phone_Number;
        this.Password = Password;
    }

    public User(String Name, String UserName, String Password, String Phone_Number,  String ID) {
        super(ID);
        this.Name = Name;
        this.UserName = UserName;
        this.Name = UserName;
        this.Phone_Number = Phone_Number;
        this.Password = Password;
    }

    //getter and setter
    public String getUserName(){return this.UserName;}
    public String getName(){return this.Name;}
    public String getPassword(){return this.Password;}
    public String getPhone_Number(){return this.Phone_Number;}
    public void setName(String Name){this.Name = Name;}
    public void setPassword(String Password){this.Password = Password;}
}