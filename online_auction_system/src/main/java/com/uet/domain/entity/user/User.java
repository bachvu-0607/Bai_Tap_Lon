package com.uet.domain.entity.user;

import com.uet.domain.entity.Entity;

public abstract class User extends Entity {
    private String userName;
    private String name;
    private String citizenId;
    private String phoneNumber;
    private String password;
    private String address;
    private boolean isActive = true;

    public User(String citizenId, String name, String phoneNumber, String password, String address) {
        super();
        initializeUser(citizenId, name, phoneNumber, password, address);
    }

    public User(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id);
        initializeUser(citizenId, name, phoneNumber, password, address);
    }

    private void initializeUser(String citizenId, String name, String phoneNumber, String password, String address) {
        this.userName = phoneNumber;
        this.citizenId = citizenId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.address = address;
    }
    
    //getter and setter
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public boolean isActive() { return this.isActive; }
    public String getUserName() { return this.userName; }
    public String getName() { return this.name; }
    public String getCitizenId() { return this.citizenId; }
    public String getPassword() { return this.password; }
    public String getPhoneNumber() { return this.phoneNumber; }
    public String getAddress() { return this.address; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
}
