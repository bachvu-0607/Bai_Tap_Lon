package com.uet.domain.request;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String phone;
    private final String citizenId;
    private final String password;
    private final String address;
    private final String role;

    public RegisterRequest(String name, String phone, String citizenId, String password, String address, String role) {
        this.name = name;
        this.phone = phone;
        this.citizenId = citizenId;
        this.password = password;
        this.address = address;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role;
    }
}
