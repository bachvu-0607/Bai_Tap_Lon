package com.uet.domain;

import java.io.Serializable;

import com.uet.domain.entity.user.User;

public class UserSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String systemId;
    private final String name;
    private final String phone;
    private final String citizenId;
    private final String role;
    private final String address;

    public UserSummary(User user, String role) {
        this.systemId = user.getId();
        this.name = user.getName();
        this.phone = user.getPhoneNumber();
        this.citizenId = user.getCitizenId();
        this.role = role;
        this.address = user.getAddress();
    }

    public String getSystemId() { return systemId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCitizenId() { return citizenId; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
}
