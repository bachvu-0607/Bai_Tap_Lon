package com.uet.domain;

import java.io.Serializable;

public class WalletTransaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String type;
    private double amount;
    private String description;
    private String createdAt;

    public WalletTransaction(String id, String type, double amount, String description, String createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
}
