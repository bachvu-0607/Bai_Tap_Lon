package com.uet.domain.summary;

import java.io.Serializable;

public class UserSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String systemId;
    private final String name;
    private final String phone;
    private final String role;
    private final boolean active;

    public UserSummary(String systemId, String name, String phone, String role, boolean active) {
        this.systemId = systemId;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    public String getSystemId() { return systemId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }

    public String getStatusText() {
        return active ? "ACTIVE" : "BANNED";
    }
}
