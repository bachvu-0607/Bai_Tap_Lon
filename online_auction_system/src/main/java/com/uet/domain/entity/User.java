package com.uet.domain.entity;

public abstract class User extends Entity {
    private String userName;
    private String name;
    private String phoneNumber;
    private String password;
    private boolean isActive = true;

    public User(String userName, String password, String id) {
        super(id);
        this.userName = userName;
        this.name = userName;
        this.password = password;
    }
    
    //getter and setter
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public boolean isActive() { return this.isActive; }
    public String getUserName() { return this.userName; }
    public String getName() { return this.name; }
    public String getPassword() { return this.password; }
    public String getPhoneNumber() { return this.phoneNumber; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
}